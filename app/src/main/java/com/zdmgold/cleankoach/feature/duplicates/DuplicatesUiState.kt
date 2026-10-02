package com.zdmgold.cleankoach.feature.duplicates

import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup

data class DuplicatesUiState(
    val loading: Boolean = true,
    val groups: List<DuplicateGroup> = emptyList(),
    val keptIds: Set<Long> = emptySet()
) {
    val totalReclaimable: Long get() = groups.sumOf { it.reclaimableBytes }
    val totalItems: Int get() = groups.sumOf { it.items.size - 1 }
}

data class DuplicateItemUi(
    val groupId: Long,
    val itemId: Long,
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val isKept: Boolean
)
