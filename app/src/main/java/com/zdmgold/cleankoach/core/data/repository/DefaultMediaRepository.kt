package com.zdmgold.cleankoach.core.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.MediaStore
import com.zdmgold.cleankoach.core.data.db.dao.MediaHashDao
import com.zdmgold.cleankoach.core.data.db.entity.MediaHashEntity
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import com.zdmgold.cleankoach.core.media.CacheCleaner
import com.zdmgold.cleankoach.core.media.DHashCalculator
import com.zdmgold.cleankoach.core.media.DuplicateGrouper
import com.zdmgold.cleankoach.core.media.HashCalculator
import com.zdmgold.cleankoach.core.media.LaplacianSharpness
import com.zdmgold.cleankoach.core.media.MediaStoreScanner
import com.zdmgold.cleankoach.core.media.SimilarityEntry
import com.zdmgold.cleankoach.core.media.SimilarityGrouper
import com.zdmgold.cleankoach.core.media.TrashManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: MediaStoreScanner,
    private val hashCalculator: HashCalculator,
    private val dHashCalculator: DHashCalculator,
    private val laplacianSharpness: LaplacianSharpness,
    private val duplicateGrouper: DuplicateGrouper,
    private val similarityGrouper: SimilarityGrouper,
    private val cacheCleaner: CacheCleaner,
    private val trashManager: TrashManager,
    private val mediaHashDao: MediaHashDao
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
        if (all.isEmpty()) {
            mediaHashDao.clearAll()
            return@withContext emptyList()
        }

        val cached = mediaHashDao.getAll().associateBy { it.mediaId }
        val currentIds = all.map { it.id }
        val freshHashes = mutableListOf<MediaHashEntity>()
        val pairs = mutableListOf<Pair<MediaItem, String>>()

        all.forEachIndexed { index, item ->
            val existing = cached[item.id]
            val reusable = existing != null && existing.dateAdded == item.dateAdded

            val entity = if (reusable) {
                existing
            } else {
                computeHash(item)
            }

            if (entity != null && entity.sha256.isNotBlank()) {
                pairs += item to entity.sha256
                if (!reusable) freshHashes += entity
            }

            progress.value = (index + 1).toFloat() / all.size.toFloat()
        }

        if (freshHashes.isNotEmpty()) {
            mediaHashDao.upsertAll(freshHashes)
        }
        runCatching { mediaHashDao.pruneMissing(currentIds) }

        progress.value = 0f
        duplicateGrouper.group(pairs)
    }

    override suspend fun scanSimilarPhotos(): List<SimilarGroup> = withContext(Dispatchers.IO) {
        val images = scanner.scanImages().filter { !it.isTrashed }
        if (images.isEmpty()) return@withContext emptyList()

        val cached = mediaHashDao.getAll().associateBy { it.mediaId }
        val currentIds = images.map { it.id }
        val freshHashes = mutableListOf<MediaHashEntity>()
        val entries = mutableListOf<SimilarityEntry>()

        images.forEachIndexed { index, item ->
            val existing = cached[item.id]
            val reusable = existing != null &&
                existing.dateAdded == item.dateAdded &&
                existing.dhash != 0L

            val entity = if (reusable) {
                existing
            } else {
                computeHash(item)
            }

            if (entity != null && entity.dhash != 0L) {
                entries += SimilarityEntry(item, entity.dhash)
                if (!reusable) freshHashes += entity
            }

            progress.value = (index + 1).toFloat() / images.size.toFloat()
        }

        if (freshHashes.isNotEmpty()) {
            mediaHashDao.upsertAll(freshHashes)
        }

        progress.value = 0f
        similarityGrouper.group(entries)
    }

    override suspend fun scanScreenshots(): List<MediaItem> = withContext(Dispatchers.IO) {
        scanner.scanScreenshots().filter { !it.isTrashed }
    }

    override suspend fun storageStats(): StorageStats = withContext(Dispatchers.IO) {
        val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as android.app.usage.StorageStatsManager
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

    private fun computeHash(item: MediaItem): MediaHashEntity? {
        val uri = Uri.parse(item.uri)
        val sha = runCatching { hashCalculator.sha256(uri) }.getOrNull()
        val dhash = if (item.mimeType.startsWith("image/")) {
            runCatching { dHashCalculator.dhash(uri) }.getOrNull() ?: 0L
        } else {
            0L
        }
        val sharpness = if (item.mimeType.startsWith("image/")) {
            runCatching {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it)
                }
                bitmap?.let {
                    val score = laplacianSharpness.compute(it)
                    it.recycle()
                    score
                } ?: 0f
            }.getOrDefault(0f)
        } else {
            0f
        }

        if (sha.isNullOrBlank()) return null

        return MediaHashEntity(
            mediaId = item.id,
            sha256 = sha,
            dhash = dhash,
            sharpness = sharpness,
            width = item.width,
            height = item.height,
            sizeBytes = item.sizeBytes,
            mimeType = item.mimeType,
            dateAdded = item.dateAdded,
            computedAt = System.currentTimeMillis()
        )
    }
}
