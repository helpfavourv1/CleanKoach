package com.zdmgold.cleankoach.core.data.repository

import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import com.zdmgold.cleankoach.core.media.CacheCleaner
import com.zdmgold.cleankoach.core.media.DHashCalculator
import com.zdmgold.cleankoach.core.media.DuplicateGrouper
import com.zdmgold.cleankoach.core.media.HashCalculator
import com.zdmgold.cleankoach.core.media.MediaStoreScanner
import com.zdmgold.cleankoach.core.media.SimilarityEntry
import com.zdmgold.cleankoach.core.media.SimilarityGrouper
import com.zdmgold.cleankoach.core.media.TrashManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: MediaStoreScanner,
    private val hashCalculator: HashCalculator,
    private val dHashCalculator: DHashCalculator,
    private val duplicateGrouper: DuplicateGrouper,
    private val similarityGrouper: SimilarityGrouper,
    private val cacheCleaner: CacheCleaner,
    private val trashManager: TrashManager
) : MediaRepository {

    private val progress = MutableStateFlow(0f)

    override suspend fun scanLargeFiles(limit: Int): List<MediaItem> = withContext(Dispatchers.IO) {
        scanner.scan()
            .filter { !it.isTrashed }
            .sortedByDescending { it.sizeBytes }
            .take(limit)
    }

    override suspend fun scanDuplicates(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val all = scanner.scan().filter { !it.isTrashed && it.sizeBytes > 0L }
        val pairs = mutableListOf<Pair<MediaItem, String>>()
        all.forEachIndexed { index, item ->
            val hash = hashCalculator.sha256(Uri.parse(item.uri)) ?: return@forEachIndexed
            pairs += item to hash
            progress.value = (index + 1).toFloat() / all.size.toFloat()
        }
        progress.value = 0f
        duplicateGrouper.group(pairs)
    }

    override suspend fun scanSimilarPhotos(): List<SimilarGroup> = withContext(Dispatchers.IO) {
        val images = scanner.scanImages().filter { !it.isTrashed }
        val entries = mutableListOf<SimilarityEntry>()
        images.forEachIndexed { index, item ->
            val hash = dHashCalculator.dhash(Uri.parse(item.uri)) ?: return@forEachIndexed
            entries += SimilarityEntry(item, hash)
            progress.value = (index + 1).toFloat() / images.size.toFloat()
        }
        progress.value = 0f
        similarityGrouper.group(entries)
    }

    override suspend fun scanScreenshots(): List<MediaItem> = withContext(Dispatchers.IO) {
        scanner.scanScreenshots().filter { !it.isTrashed }
    }

    override suspend fun storageStats(): StorageStats = withContext(Dispatchers.IO) {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val uuid = StorageManager.UUID_DEFAULT

        val total = runCatching { statsManager.getTotalBytes(uuid) }.getOrDefault(0L)
        val free = runCatching { statsManager.getFreeBytes(uuid) }.getOrDefault(0L)
        val used = (total - free).coerceAtLeast(0L)
        val trashBytes = runCatching { scanner.trashedBytes() }.getOrDefault(0L)
        val cacheBytes = cacheCleaner.cacheSizeBytes()

        StorageStats(
            usedBytes = used,
            totalBytes = total,
            trashBytes = trashBytes,
            cacheBytes = cacheBytes
        )
    }

    override suspend fun deleteMedia(ids: List<Long>): CleanupResult = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) {
            val cacheFreed = cacheCleaner.clear()
            return@withContext trashManager.buildCleanupResult(
                itemCount = 0,
                freedBytes = 0L,
                cacheFreedBytes = cacheFreed
            )
        }
        val uris = ids.map { id ->
            Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )
        }
        var freed = 0L
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.delete(uri, null, null)
            }.onSuccess { count -> if (count > 0) freed += 1L }
        }
        trashManager.buildCleanupResult(
            itemCount = uris.size,
            freedBytes = freed,
            cacheFreedBytes = cacheCleaner.clear()
        )
    }

    override suspend fun clearAppCache(): Long = withContext(Dispatchers.IO) {
        cacheCleaner.clear()
    }

    override fun observeScanProgress(): Flow<Float> = progress
}
