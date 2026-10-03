package com.zdmgold.cleankoach.core.data.repository

import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.MediaStore
import com.zdmgold.cleankoach.core.data.db.dao.MediaHashDao
import com.zdmgold.cleankoach.core.data.db.entity.MediaHashEntity
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.MediaKind
import com.zdmgold.cleankoach.core.domain.model.ScanStatus
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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
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
    private val scanStatus = MutableStateFlow(ScanStatus())

    private companion object {
        const val LARGE_FILE_BYTES = 50L * 1024L * 1024L
        const val PHOTO_OPTIMIZER_BYTES = 1L * 1024L * 1024L
        const val VIDEO_OPTIMIZER_BYTES = 20L * 1024L * 1024L
        const val SAVE_BATCH = 50
    }

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

        // Identical files always have identical sizes, so only same-size files need hashing.
        val candidates = all.groupBy { it.sizeBytes }.values.filter { it.size > 1 }.flatten()
        val cached = mediaHashDao.getAll().associateBy { it.mediaId }
        val pending = mutableListOf<MediaHashEntity>()
        val pairs = mutableListOf<Pair<MediaItem, String>>()

        candidates.forEachIndexed { index, item ->
            coroutineContext.ensureActive()
            val existing = cached[item.id]?.takeIf { it.dateAdded == item.dateAdded }
            val sha = if (existing != null && existing.sha256.isNotBlank()) {
                existing.sha256
            } else {
                runCatching { hashCalculator.sha256(Uri.parse(item.uri)) }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.also { pending += withSha(item, existing, it) }
            }
            if (sha != null) pairs += item to sha

            if (pending.size >= SAVE_BATCH) {
                mediaHashDao.upsertAll(pending.toList())
                pending.clear()
            }
            scanStatus.value = ScanStatus(index + 1, candidates.size)
            progress.value = scanStatus.value.fraction
        }

        if (pending.isNotEmpty()) mediaHashDao.upsertAll(pending.toList())
        runCatching { mediaHashDao.pruneMissing(all.map { it.id }) }

        scanStatus.value = ScanStatus()
        progress.value = 0f
        duplicateGrouper.group(pairs)
    }

    override suspend fun scanSimilarPhotos(): List<SimilarGroup> = withContext(Dispatchers.IO) {
        val images = scanner.scanImages().filter { !it.isTrashed }
        if (images.isEmpty()) return@withContext emptyList()

        val cached = mediaHashDao.getAll().associateBy { it.mediaId }
        val pending = mutableListOf<MediaHashEntity>()
        val entries = mutableListOf<SimilarityEntry>()

        images.forEachIndexed { index, item ->
            coroutineContext.ensureActive()
            val existing = cached[item.id]?.takeIf { it.dateAdded == item.dateAdded }
            val dhash = if (existing != null && existing.dhash != 0L) {
                existing.dhash
            } else {
                (runCatching { dHashCalculator.dhash(Uri.parse(item.uri)) }.getOrNull() ?: 0L)
                    .also { if (it != 0L) pending += withDhash(item, existing, it) }
            }
            if (dhash != 0L) entries += SimilarityEntry(item, dhash)

            if (pending.size >= SAVE_BATCH) {
                mediaHashDao.upsertAll(pending.toList())
                pending.clear()
            }
            scanStatus.value = ScanStatus(index + 1, images.size)
            progress.value = scanStatus.value.fraction
        }

        if (pending.isNotEmpty()) mediaHashDao.upsertAll(pending.toList())

        scanStatus.value = ScanStatus()
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
        val trashed = runCatching { scanner.trashedItems() }.getOrDefault(emptyList())
        val cacheBytes = cacheCleaner.cacheSizeBytes()
        val library = runCatching { scanner.scan().filter { !it.isTrashed } }.getOrDefault(emptyList())

        StorageStats(
            usedBytes = used,
            totalBytes = total,
            trashBytes = trashed.sumOf { it.second },
            cacheBytes = cacheBytes,
            trashCount = trashed.size,
            screenshotBytes = library
                .filter { it.kind == MediaKind.IMAGE && scanner.isScreenshot(it) }
                .sumOf { it.sizeBytes },
            largeFilesBytes = library
                .filter { it.sizeBytes >= LARGE_FILE_BYTES }
                .sumOf { it.sizeBytes },
            photoOptimizerCount = library
                .count { it.kind == MediaKind.IMAGE && it.sizeBytes >= PHOTO_OPTIMIZER_BYTES },
            videoOptimizerCount = library
                .count { it.kind == MediaKind.VIDEO && it.sizeBytes >= VIDEO_OPTIMIZER_BYTES }
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

    override suspend fun trashedUris(): List<Uri> = withContext(Dispatchers.IO) {
        runCatching { scanner.trashedItems().map { it.first } }.getOrDefault(emptyList())
    }

    override fun buildDeleteRequest(uris: List<Uri>): IntentSender? =
        if (uris.isEmpty()) null else runCatching { trashManager.buildDeleteRequest(uris) }.getOrNull()

    override fun observeScanProgress(): Flow<Float> = progress

    override fun observeScanStatus(): Flow<ScanStatus> = scanStatus

    private fun withSha(item: MediaItem, existing: MediaHashEntity?, sha: String): MediaHashEntity =
        existing?.copy(sha256 = sha, computedAt = System.currentTimeMillis())
            ?: MediaHashEntity(
                mediaId = item.id,
                sha256 = sha,
                dhash = 0L,
                sharpness = 0f,
                width = item.width,
                height = item.height,
                sizeBytes = item.sizeBytes,
                mimeType = item.mimeType,
                dateAdded = item.dateAdded,
                computedAt = System.currentTimeMillis()
            )

    private fun withDhash(item: MediaItem, existing: MediaHashEntity?, dhash: Long): MediaHashEntity =
        existing?.copy(dhash = dhash, computedAt = System.currentTimeMillis())
            ?: MediaHashEntity(
                mediaId = item.id,
                sha256 = "",
                dhash = dhash,
                sharpness = 0f,
                width = item.width,
                height = item.height,
                sizeBytes = item.sizeBytes,
                mimeType = item.mimeType,
                dateAdded = item.dateAdded,
                computedAt = System.currentTimeMillis()
            )
}
