package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.domain.model.AppUsageItem

interface UsageRepository {
    fun hasUsageAccess(): Boolean
    suspend fun last24Hours(): List<AppUsageItem>
}
