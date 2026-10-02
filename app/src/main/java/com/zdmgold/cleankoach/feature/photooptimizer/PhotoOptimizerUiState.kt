package com.zdmgold.cleankoach.feature.photooptimizer

import com.zdmgold.cleankoach.core.domain.model.MediaItem

data class PhotoOptimizerUiState(
    val loading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val processing: Boolean = false,
    val processed: Int = 0,
    val totalToProcess: Int = 0,
    val totalSavedBytes: Long = 0L,
    val lastRunSummary: String? = null
) {
    val selectionCount: Int get() = selectedIds.size
    val selectedBytes: Long
        get() = items.filter { it.id in selectedIds }.sumOf { it.sizeBytes }
    val allSelected: Boolean get() = items.isNotEmpty() && selectedIds.size == items.size
    val canRun: Boolean get() = selectedIds.isNotEmpty() && !processing
    val progress: Float
        get() = if (totalToProcess <= 0) 0f
        else processed.toFloat() / totalToProcess.toFloat()
}
