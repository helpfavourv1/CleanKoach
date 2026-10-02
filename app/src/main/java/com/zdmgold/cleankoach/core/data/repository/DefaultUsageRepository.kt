package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.domain.model.AppUsageItem
import com.zdmgold.cleankoach.core.system.UsageStatsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUsageRepository @Inject constructor(
    private val provider: UsageStatsProvider
) : UsageRepository {

    override fun hasUsageAccess(): Boolean = provider.hasAccess()

    override suspend fun last24Hours(): List<AppUsageItem> = withContext(Dispatchers.IO) {
        provider.queryLast24Hours()
    }
}
