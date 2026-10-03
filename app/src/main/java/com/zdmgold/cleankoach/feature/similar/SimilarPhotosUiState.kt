package com.zdmgold.cleankoach.feature.similar

import com.zdmgold.cleankoach.core.domain.model.ScanStatus
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup

data class SimilarPhotosUiState(
    val loading: Boolean = true,
    val scanStatus: ScanStatus = ScanStatus(),
    val groups: List<SimilarGroup> = emptyList(),
    val bestIds: Set<Long> = emptySet()
) {
    val totalReclaimable: Long get() = groups.sumOf { it.reclaimableBytes }
}
