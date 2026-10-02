package com.zdmgold.cleankoach.di

import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import com.zdmgold.cleankoach.core.data.repository.DefaultMediaRepository
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.data.repository.SecurityRepository
import com.zdmgold.cleankoach.core.data.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryStubsModule {

    @Provides
    @Singleton
    fun provideMediaRepository(impl: DefaultMediaRepository): MediaRepository = impl


    @Provides
    @Singleton
    fun provideBillingRepository(impl: com.zdmgold.cleankoach.core.billing.BillingConnector): BillingRepository = impl

    @Provides
    @Singleton
    fun provideUsageRepository(impl: com.zdmgold.cleankoach.core.data.repository.DefaultUsageRepository): UsageRepository = impl

    @Provides
    @Singleton
    fun provideSecurityRepository(impl: com.zdmgold.cleankoach.core.data.repository.DefaultSecurityRepository): SecurityRepository = impl
}




