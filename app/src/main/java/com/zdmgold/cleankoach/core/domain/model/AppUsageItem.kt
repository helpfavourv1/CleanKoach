package com.zdmgold.cleankoach.core.domain.model

data class AppUsageItem(
    val packageName: String,
    val appLabel: String,
    val launchCount: Int,
    val lastUsedAt: Long,
    val foregroundMillis: Long
)
