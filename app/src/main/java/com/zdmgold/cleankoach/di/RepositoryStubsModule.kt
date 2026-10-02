package com.zdmgold.cleankoach.di

import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import com.zdmgold.cleankoach.core.data.repository.DefaultMediaRepository
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.data.repository.SecurityRepository
import com.zdmgold.cleankoach.core.data.repository.UsageRepository
import com.zdmgold.cleankoach.core.domain.model.AppUsageItem
import com.zdmgold.cleankoach.core.domain.model.SecurityVerdict
import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult
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
    fun provideMediaRepository(impl: DefaultMediaRepository): MediaRepository = impl


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
