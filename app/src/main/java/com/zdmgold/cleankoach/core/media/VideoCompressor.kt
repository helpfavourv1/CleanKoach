package com.zdmgold.cleankoach.core.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MimeTypes
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
        val outputPath: String?,
        val originalBytes: Long,
        val compressedBytes: Long
    ) {
        val savedBytes: Long get() = (originalBytes - compressedBytes).coerceAtLeast(0L)
    }

    suspend fun compress(
        inputUri: Uri,
        originalBytes: Long,
        preset: Preset = Preset.BALANCED
    ): Result? = suspendCancellableCoroutine { cont ->
        val outputFile = File(
            context.cacheDir,
            "ck_video_${System.currentTimeMillis()}.mp4"
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
                    if (cont.isActive) {
                        cont.resume(
                            Result(
                                outputPath = outputFile.absolutePath,
                                originalBytes = originalBytes,
                                compressedBytes = outputFile.length()
                            )
                        )
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    outputFile.delete()
                    if (cont.isActive) cont.resume(null)
                }
            })
            .build()

        cont.invokeOnCancellation {
            runCatching { transformer.cancel() }
            outputFile.delete()
        }

        runCatching { transformer.start(edited, outputFile.absolutePath) }
            .onFailure {
                if (cont.isActive) cont.resume(null)
            }
    }
}
