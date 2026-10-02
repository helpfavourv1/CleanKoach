package com.zdmgold.cleankoach.feature.largefiles

import com.zdmgold.cleankoach.core.domain.model.MediaItem

data class LargeFilesUiState(
    val loading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val permissionGranted: Boolean = true
) {
    val selectionCount: Int get() = selectedIds.size
    val selectedBytes: Long
        get() = items.filter { it.id in selectedIds }.sumOf { it.sizeBytes }
    val totalBytes: Long get() = items.sumOf { it.sizeBytes }
    val allSelected: Boolean get() = items.isNotEmpty() && selectedIds.size == items.size
    val canDelete: Boolean get() = selectedIds.isNotEmpty()
}
