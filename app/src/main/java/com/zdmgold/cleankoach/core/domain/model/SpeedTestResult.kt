package com.zdmgold.cleankoach.core.domain.model

data class SpeedTestResult(
    val downloadMbps: Double,
    val bytesTransferred: Long,
    val durationMillis: Long,
    val completedAt: Long,
    /** True for live samples while the test runs; false for the final result. */
    val inProgress: Boolean = false
)
