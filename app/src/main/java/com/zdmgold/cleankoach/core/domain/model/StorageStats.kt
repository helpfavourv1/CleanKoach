package com.zdmgold.cleankoach.core.domain.model

data class StorageStats(
    val usedBytes: Long,
    val totalBytes: Long,
    val trashBytes: Long,
    val cacheBytes: Long,
    val trashCount: Int = 0,
    val screenshotBytes: Long = 0L,
    val largeFilesBytes: Long = 0L,
    val photoOptimizerCount: Int = 0,
    val videoOptimizerCount: Int = 0
) {
    val percentUsed: Int
        get() = if (totalBytes <= 0L) 0
        else ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)

    val totalReclaimableBytes: Long
        get() = trashBytes + cacheBytes
}
