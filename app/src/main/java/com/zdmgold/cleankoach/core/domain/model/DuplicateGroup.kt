package com.zdmgold.cleankoach.core.domain.model

data class DuplicateGroup(
    val id: Long,
    val sha256: String,
    val items: List<MediaItem>,
    val totalBytes: Long
) {
    val keptId: Long get() = items.firstOrNull()?.id ?: -1L

    val reclaimableBytes: Long
        get() = if (items.size <= 1) 0L
        else totalBytes - items.first().sizeBytes
}
