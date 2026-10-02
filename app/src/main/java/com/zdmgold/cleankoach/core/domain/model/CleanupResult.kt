package com.zdmgold.cleankoach.core.domain.model

data class CleanupResult(
    val deletedItemCount: Int,
    val freedBytes: Long,
    val cacheClearedBytes: Long,
    val performedAt: Long
) {
    val totalFreedBytes: Long get() = freedBytes + cacheClearedBytes
}
