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

    private fun directorySize(dir: java.io.File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return dir.walkBottomUp()
            .filter { it.isFile }
            .sumOf { it.length() }
    }
}
