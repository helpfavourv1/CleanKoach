package com.zdmgold.cleankoach.core.domain.model

data class StorageStats(
    val usedBytes: Long,
    val totalBytes: Long,
    val trashBytes: Long,
    val cacheBytes: Long
) {
    val percentUsed: Int
        get() = if (totalBytes <= 0L) 0
        else ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)

    val totalReclaimableBytes: Long
        get() = trashBytes + cacheBytes
}
