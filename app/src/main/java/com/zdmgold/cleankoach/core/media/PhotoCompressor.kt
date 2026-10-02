package com.zdmgold.cleankoach.core.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class Result(
        val outputUri: String?,
        val originalBytes: Long,
        val compressedBytes: Long,
        val outputMime: String
    ) {
        val savedBytes: Long get() = (originalBytes - compressedBytes).coerceAtLeast(0L)
    }

    fun compress(
        inputUri: Uri,
        originalBytes: Long,
        originalName: String,
        quality: Int = 85,
        forceJpeg: Boolean = false
    ): Result? = runCatching {
        val bitmap = decode(inputUri) ?: return null
        val outputMime = if (forceJpeg) "image/jpeg" else "image/webp"

        val displayName = buildOutputName(originalName, outputMime)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, outputMime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CleanKoach/optimized")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val outputUri = context.contentResolver.insert(collection, values) ?: return null
        val outStream: OutputStream? = context.contentResolver.openOutputStream(outputUri)
        outStream?.use { stream ->
            val format = if (outputMime == "image/webp" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else if (outputMime == "image/webp") {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, quality, stream)
        }
        bitmap.recycle()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val finalize = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(outputUri, finalize, null, null)
        }

        val compressedSize = context.contentResolver.openAssetFileDescriptor(outputUri, "r")
            ?.use { it.length }
            ?: 0L

        Result(
            outputUri = outputUri.toString(),
            originalBytes = originalBytes,
            compressedBytes = compressedSize,
            outputMime = outputMime
        )
    }.getOrNull()

    private fun decode(uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    private fun buildOutputName(original: String, mime: String): String {
        val base = original.substringBeforeLast('.', original)
        val ext = if (mime == "image/webp") "webp" else "jpg"
        return "ck_${base.take(60)}_optimized.$ext"
    }
}
