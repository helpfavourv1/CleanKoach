package com.zdmgold.cleankoach.core.domain.model

data class SpeedTestResult(
    val downloadMbps: Double,
    val bytesTransferred: Long,
    val durationMillis: Long,
    val completedAt: Long
)
