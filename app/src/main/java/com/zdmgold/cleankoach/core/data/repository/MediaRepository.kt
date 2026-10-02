package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun scanLargeFiles(limit: Int = 200): List<MediaItem>
    suspend fun scanDuplicates(): List<DuplicateGroup>
    suspend fun scanSimilarPhotos(): List<SimilarGroup>
    suspend fun scanScreenshots(): List<MediaItem>
    suspend fun storageStats(): StorageStats
    suspend fun deleteMedia(ids: List<Long>): CleanupResult
    suspend fun clearAppCache(): Long
    fun observeScanProgress(): Flow<Float>
}
