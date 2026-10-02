package com.zdmgold.cleankoach.core.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
@androidx.annotation.OptIn(UnstableApi::class)
class VideoCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    enum class Preset(val videoBitrate: Int, val label: String) {
        HIGH(8_000_000, "High quality"),
        BALANCED(5_000_000, "Balanced"),
        SMALL(2_500_000, "Small file")
    }

    data class Result(
        val outputUri: String?,
        val originalBytes: Long,
        val compressedBytes: Long
    ) {
        val savedBytes: Long get() = (originalBytes - compressedBytes).coerceAtLeast(0L)
    }

    suspend fun compress(
        inputUri: Uri,
        originalBytes: Long,
        originalName: String,
        preset: Preset = Preset.BALANCED
    ): Result? = suspendCancellableCoroutine { cont ->
        val workingFile = File(
            context.cacheDir,
            "ck_video_working_${System.currentTimeMillis()}.mp4"
        )

        val mediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .build()

        val edited = EditedMediaItem.Builder(mediaItem)
            .setRemoveAudio(false)
            .build()

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    val published = publishToMovies(workingFile, originalName)
                    workingFile.delete()
                    if (cont.isActive) {
                        cont.resume(
                            Result(
                                outputUri = published?.toString(),
                                originalBytes = originalBytes,
                                compressedBytes = published?.let { sizeOf(it) } ?: 0L
                            )
                        )
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    workingFile.delete()
                    if (cont.isActive) cont.resume(null)
                }
            })
            .build()

        cont.invokeOnCancellation {
            runCatching { transformer.cancel() }
            workingFile.delete()
        }

        runCatching { transformer.start(edited, workingFile.absolutePath) }
            .onFailure {
                workingFile.delete()
                if (cont.isActive) cont.resume(null)
            }
    }

    private fun publishToMovies(source: File, originalName: String): Uri? {
        if (!source.exists() || source.length() == 0L) return null

        val base = originalName.substringBeforeLast('.').take(60)
        val displayName = "ck_${base}_optimized.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CleanKoach/optimized")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val outputUri = context.contentResolver.insert(collection, values) ?: return null
        context.contentResolver.openOutputStream(outputUri)?.use { out ->
            source.inputStream().use { input -> input.copyTo(out, bufferSize = 64 * 1024) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val finalize = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(outputUri, finalize, null, null)
        }
        return outputUri
    }

    private fun sizeOf(uri: Uri): Long =
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
}
