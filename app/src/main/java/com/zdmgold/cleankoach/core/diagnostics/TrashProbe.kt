package com.zdmgold.cleankoach.core.diagnostics

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

/**
 * Read-only probe: reports what Android lets this app see in the system trash, per collection.
 * It never deletes, restores or launches anything.
 */
object TrashProbe {

    fun run(context: Context): String = buildString {
        appendLine("TRASH PROBE  (read-only)")
        appendLine("Android API ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            appendLine("System trash does not exist before Android 11.")
            return@buildString
        }
        val perms = listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        perms.forEach {
            val granted = context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            appendLine("  ${it.substringAfterLast('.')}: ${if (granted) "granted" else "no"}")
        }
        appendLine("  all-files access: ${runCatching { Environment.isExternalStorageManager() }.getOrNull()}")
        appendLine()

        val volume = MediaStore.VOLUME_EXTERNAL
        val collections = listOf(
            "Images" to MediaStore.Images.Media.getContentUri(volume),
            "Video" to MediaStore.Video.Media.getContentUri(volume),
            "Audio" to MediaStore.Audio.Media.getContentUri(volume),
            "Downloads" to MediaStore.Downloads.getContentUri(volume),
            "Files (everything)" to MediaStore.Files.getContentUri(volume)
        )
        collections.forEach { (label, uri) ->
            val r = runCatching { count(context, uri) }
            appendLine("$label: " + r.fold({ "${it.first} items, ${mb(it.second)}" }, { "error ${it.javaClass.simpleName}: ${it.message}" }))
        }

        appendLine()
        appendLine("Files collection, by media type (0 = none/other, 1 image, 2 audio, 3 video, 4 playlist, 5 subtitle, 6 document):")
        val filesUri = MediaStore.Files.getContentUri(volume)
        val rows = runCatching { rows(context, filesUri) }.getOrElse {
            appendLine("  error ${it.javaClass.simpleName}: ${it.message}")
            emptyList()
        }
        rows.groupBy { it.mediaType }.toSortedMap().forEach { (type, list) ->
            appendLine("  type $type: ${list.size} items, ${mb(list.sumOf { it.size })}")
        }
        appendLine("  owned by this app: ${rows.count { it.owner == context.packageName }}")
        appendLine("  owner unknown/other: ${rows.count { it.owner != context.packageName }}")

        appendLine()
        appendLine("Largest 20 trashed items seen:")
        rows.sortedByDescending { it.size }.take(20).forEach {
            appendLine("  ${mb(it.size)}  t${it.mediaType}  ${it.name}  [${it.owner ?: "?"}]  ${it.path ?: ""}")
        }

        appendLine()
        val sample = rows.take(5).map { it.uri }
        if (sample.isEmpty()) {
            appendLine("Delete-request test: nothing to test.")
        } else {
            val t = runCatching { MediaStore.createDeleteRequest(context.contentResolver, sample) }
            appendLine(
                "Delete-request test (built only, not launched) for ${sample.size} items: " +
                    t.fold({ "builds OK" }, { "FAILED ${it.javaClass.simpleName}: ${it.message}" })
            )
        }
    }

    private class Row(
        val uri: Uri, val name: String, val size: Long,
        val mediaType: Int, val owner: String?, val path: String?
    )

    private fun args(): Bundle = Bundle().apply {
        putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
    }

    private fun count(context: Context, uri: Uri): Pair<Int, Long> {
        var n = 0
        var bytes = 0L
        context.contentResolver.query(
            uri, arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.SIZE), args(), null
        )?.use { c ->
            while (c.moveToNext()) {
                n += 1
                bytes += c.getLong(1)
            }
        }
        return n to bytes
    }

    private fun rows(context: Context, uri: Uri): List<Row> {
        val out = mutableListOf<Row>()
        context.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.MediaColumns.OWNER_PACKAGE_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH
            ),
            args(),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                out += Row(
                    uri = ContentUris.withAppendedId(uri, c.getLong(0)),
                    name = c.getString(1) ?: "?",
                    size = c.getLong(2),
                    mediaType = c.getInt(3),
                    owner = c.getString(4),
                    path = c.getString(5)
                )
            }
        }
        return out
    }

    private fun mb(bytes: Long): String = "%.2f MB".format(bytes / 1_048_576.0)
}
