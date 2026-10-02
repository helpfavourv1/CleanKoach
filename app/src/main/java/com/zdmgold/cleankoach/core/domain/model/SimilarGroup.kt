package com.zdmgold.cleankoach.core.domain.model

data class SimilarGroup(
    val id: Long,
    val items: List<MediaItem>,
    val bestMediaId: Long,
    val hammingDistance: Int
) {
    val reclaimableBytes: Long
        get() {
            val best = items.firstOrNull { it.id == bestMediaId } ?: return 0L
            return items.filter { it.id != bestMediaId }.sumOf { it.sizeBytes }
        }
}
