package com.zdmgold.cleankoach.di

import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.data.repository.SecurityRepository
import com.zdmgold.cleankoach.core.data.repository.UsageRepository
import com.zdmgold.cleankoach.core.domain.model.AppUsageItem
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.SecurityVerdict
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryStubsModule {

    @Provides
    @Singleton
    fun provideMediaRepository(): MediaRepository = StubMediaRepository

    @Provides
    @Singleton
    fun provideBillingRepository(): BillingRepository = StubBillingRepository

    @Provides
    @Singleton
    fun provideUsageRepository(): UsageRepository = StubUsageRepository

    @Provides
    @Singleton
    fun provideSecurityRepository(): SecurityRepository = StubSecurityRepository
}

object StubMediaRepository : MediaRepository {
    override suspend fun scanLargeFiles(limit: Int): List<MediaItem> = emptyList()
    override suspend fun scanDuplicates(): List<DuplicateGroup> = emptyList()
    override suspend fun scanSimilarPhotos(): List<SimilarGroup> = emptyList()
    override suspend fun scanScreenshots(): List<MediaItem> = emptyList()
    override suspend fun storageStats(): StorageStats = StorageStats(0L, 0L, 0L, 0L)
    override suspend fun deleteMedia(ids: List<Long>): CleanupResult =
        CleanupResult(0, 0L, 0L, System.currentTimeMillis())
    override suspend fun clearAppCache(): Long = 0L
    override fun observeScanProgress(): Flow<Float> = flowOf(0f)
}

object StubBillingRepository : BillingRepository {
    override val proEntitled: Flow<Boolean> = flowOf(false)
    override suspend fun refresh() = Unit
    override suspend fun launchPurchase() = Unit
    override suspend fun restore() = Unit
}

object StubUsageRepository : UsageRepository {
    override fun hasUsageAccess(): Boolean = false
    override suspend fun last24Hours(): List<AppUsageItem> = emptyList()
}

object StubSecurityRepository : SecurityRepository {
    override suspend fun inspectWifi(): WifiSecurityReport = WifiSecurityReport(
        internetAccess = true,
        encryptionType = null,
        sslStripVerdict = SecurityVerdict.UNAVAILABLE,
        sslSplitVerdict = SecurityVerdict.UNAVAILABLE,
        checkedAt = System.currentTimeMillis()
    )
    override fun runSpeedTest(): Flow<SpeedTestResult> = flowOf(
        SpeedTestResult(0.0, 0L, 0L, System.currentTimeMillis())
    )
}
