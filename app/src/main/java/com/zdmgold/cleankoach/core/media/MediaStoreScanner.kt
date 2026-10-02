package com.zdmgold.cleankoach.core.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.MediaKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    private val context: Context
) {

    fun scan(kinds: Set<MediaKind> = setOf(MediaKind.IMAGE, MediaKind.VIDEO, MediaKind.AUDIO)): List<MediaItem> {
        val results = mutableListOf<MediaItem>()
        if (MediaKind.IMAGE in kinds) results += queryImages()
        if (MediaKind.VIDEO in kinds) results += queryVideos()
        if (MediaKind.AUDIO in kinds) results += queryAudio()
        return results
    }

    fun scanImages(): List<MediaItem> = queryImages()

    fun scanVideos(): List<MediaItem> = queryVideos()

    fun scanScreenshots(): List<MediaItem> =
        queryImages().filter { item ->
            val path = item.relativePath ?: ""
            path.contains("Screenshot", ignoreCase = true) ||
                item.displayName.contains("Screenshot", ignoreCase = true) ||
                item.displayName.contains("screenshot", ignoreCase = true)
        }

    private fun queryImages(): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.IS_TRASHED
        )
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val items = mutableListOf<MediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val wCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
            val hCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val pathCol = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val trashCol = cursor.getColumnIndex(MediaStore.Images.Media.IS_TRASHED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += MediaItem(
                    id = id,
                    uri = uri.toString(),
                    displayName = cursor.getString(nameCol) ?: "",
                    kind = MediaKind.IMAGE,
                    sizeBytes = cursor.getLong(sizeCol),
                    width = if (wCol >= 0) cursor.getInt(wCol) else 0,
                    height = if (hCol >= 0) cursor.getInt(hCol) else 0,
                    durationMs = 0L,
                    dateAdded = cursor.getLong(dateCol) * 1000L,
                    mimeType = cursor.getString(mimeCol) ?: "image/*",
                    relativePath = if (pathCol >= 0) cursor.getString(pathCol) else null,
                    isTrashed = if (trashCol >= 0) cursor.getInt(trashCol) == 1 else false
                )
            }
        }
        return items
    }

    private fun queryVideos(): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.IS_TRASHED
        )
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val items = mutableListOf<MediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val wCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
            val hCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
            val durCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val pathCol = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
            val trashCol = cursor.getColumnIndex(MediaStore.Video.Media.IS_TRASHED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += MediaItem(
                    id = id,
                    uri = uri.toString(),
                    displayName = cursor.getString(nameCol) ?: "",
                    kind = MediaKind.VIDEO,
                    sizeBytes = cursor.getLong(sizeCol),
                    width = if (wCol >= 0) cursor.getInt(wCol) else 0,
                    height = if (hCol >= 0) cursor.getInt(hCol) else 0,
                    durationMs = if (durCol >= 0) cursor.getLong(durCol) else 0L,
                    dateAdded = cursor.getLong(dateCol) * 1000L,
                    mimeType = cursor.getString(mimeCol) ?: "video/*",
                    relativePath = if (pathCol >= 0) cursor.getString(pathCol) else null,
                    isTrashed = if (trashCol >= 0) cursor.getInt(trashCol) == 1 else false
                )
            }
        }
        return items
    }

    private fun queryAudio(): List<MediaItem> {
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.IS_TRASHED
        )

        val items = mutableListOf<MediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val durCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            val trashCol = cursor.getColumnIndex(MediaStore.Audio.Media.IS_TRASHED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += MediaItem(
                    id = id,
                    uri = uri.toString(),
                    displayName = cursor.getString(nameCol) ?: "",
                    kind = MediaKind.AUDIO,
                    sizeBytes = cursor.getLong(sizeCol),
                    width = 0,
                    height = 0,
                    durationMs = if (durCol >= 0) cursor.getLong(durCol) else 0L,
                    dateAdded = cursor.getLong(dateCol) * 1000L,
                    mimeType = cursor.getString(mimeCol) ?: "audio/*",
                    relativePath = if (pathCol >= 0) cursor.getString(pathCol) else null,
                    isTrashed = if (trashCol >= 0) cursor.getInt(trashCol) == 1 else false
                )
            }
        }
        return items
    }

    fun trashedBytes(): Long =
        scan().filter { it.isTrashed }.sumOf { it.sizeBytes }
}
