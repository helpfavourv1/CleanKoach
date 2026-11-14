package com.zdmgold.cleankoach.core.media

/** Pure free-space rule for the optimizers, kept free of Android so unit tests can cover it. */
object StoragePolicy {
    /** Largest single item plus a 10% margin; items run one at a time, so that is the peak need. */
    fun neededBytes(itemSizes: List<Long>, copies: Int): Long {
        val largest = itemSizes.maxOrNull() ?: return 0L
        return (largest * copies * 1.1).toLong()
    }

    fun isLow(freeBytes: Long, neededBytes: Long): Boolean = freeBytes < neededBytes
}
