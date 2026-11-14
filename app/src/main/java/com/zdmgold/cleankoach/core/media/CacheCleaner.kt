package com.zdmgold.cleankoach.core.media

import android.content.Context
import coil3.SingletonImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheCleaner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun cacheSizeBytes(): Long {
        var total = 0L
        total += directorySize(context.cacheDir)
        context.externalCacheDir?.let { total += directorySize(it) }
        return total
    }

    fun clear(): Long {
        val before = cacheSizeBytes()
        context.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
        context.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
        runCatching { SingletonImageLoader.get(context).diskCache?.clear() }
        runCatching { SingletonImageLoader.get(context).memoryCache?.clear() }
        val after = cacheSizeBytes()
        return (before - after).coerceAtLeast(0L)
    }

    /** Free bytes on the app's storage volume. */
    fun freeBytes(): Long = runCatching {
        android.os.StatFs(context.cacheDir.path).availableBytes
    }.getOrDefault(Long.MAX_VALUE)

    /**
     * Deletes scratch files older than [olderThanMs] in CleanKoach's own cache folders.
     * The image-loader cache (thumbnails) is skipped, and nothing outside the app's cache is touched,
     * so compressed copies saved to the gallery are never removed.
     */
    fun sweepStale(olderThanMs: Long = SWEEP_AGE_MS): Long {
        val cutoff = System.currentTimeMillis() - olderThanMs
        var freed = 0L
        listOfNotNull(context.cacheDir, context.externalCacheDir).forEach { root ->
            root.listFiles()?.forEach { top ->
                if (top.name == IMAGE_CACHE_DIR) return@forEach
                freed += sweep(top, cutoff)
            }
        }
        return freed
    }

    /** Size of CleanKoach's own scratch files, not counting the thumbnail cache. */
    fun tempBytes(): Long = listOfNotNull(context.cacheDir, context.externalCacheDir).sumOf { root ->
        root.listFiles()?.filter { it.name != IMAGE_CACHE_DIR }?.sumOf { directorySize(it) } ?: 0L
    }

    private fun sweep(file: java.io.File, cutoff: Long): Long {
        if (file.isDirectory) {
            var freed = 0L
            file.listFiles()?.forEach { freed += sweep(it, cutoff) }
            if (file.list().isNullOrEmpty() && file.lastModified() < cutoff) file.delete()
            return freed
        }
        if (file.lastModified() >= cutoff) return 0L
        val size = file.length()
        return if (file.delete()) size else 0L
    }

    private companion object {
        const val SWEEP_AGE_MS = 10 * 60_000L
        const val IMAGE_CACHE_DIR = "image_cache"
    }

    private fun directorySize(dir: java.io.File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return dir.walkBottomUp()
            .filter { it.isFile }
            .sumOf { it.length() }
    }
}
