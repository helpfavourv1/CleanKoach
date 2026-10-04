package com.zdmgold.cleankoach.feature.duplicates

import androidx.activity.result.IntentSenderRequest
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.ScanStatus

data class DuplicatesUiState(
    val loading: Boolean = true,
    val scanStatus: ScanStatus = ScanStatus(),
    val groups: List<DuplicateGroup> = emptyList(),
    /** Group id to the one item the user keeps. Missing means the default (first item). */
    val keptByGroup: Map<Long, Long> = emptyMap(),
    val deleteRequest: IntentSenderRequest? = null
) {
    val totalReclaimable: Long get() = groups.sumOf { it.reclaimableBytes }

    fun keptIdFor(group: DuplicateGroup): Long = keptByGroup[group.id] ?: group.keptId

    /** Every item that is not the kept one in its group. */
    val toDelete: List<MediaItem>
        get() = groups.flatMap { group ->
            val kept = keptIdFor(group)
            group.items.filter { it.id != kept }
        }
    val deleteBytes: Long get() = toDelete.sumOf { it.sizeBytes }
    val canDelete: Boolean get() = toDelete.isNotEmpty()
}
