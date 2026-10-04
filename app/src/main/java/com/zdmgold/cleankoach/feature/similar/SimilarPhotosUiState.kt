package com.zdmgold.cleankoach.feature.similar

import androidx.activity.result.IntentSenderRequest
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.ScanStatus
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup

data class SimilarPhotosUiState(
    val loading: Boolean = true,
    val scanStatus: ScanStatus = ScanStatus(),
    val groups: List<SimilarGroup> = emptyList(),
    /** Group id to the one photo the user keeps. Missing means the suggested best shot. */
    val bestByGroup: Map<Long, Long> = emptyMap(),
    val deleteRequest: IntentSenderRequest? = null
) {
    val totalReclaimable: Long get() = groups.sumOf { it.reclaimableBytes }

    fun bestIdFor(group: SimilarGroup): Long = bestByGroup[group.id] ?: group.bestMediaId

    /** Every photo that is not the kept one in its group. */
    val toDelete: List<MediaItem>
        get() = groups.flatMap { group ->
            val best = bestIdFor(group)
            group.items.filter { it.id != best }
        }
    val deleteBytes: Long get() = toDelete.sumOf { it.sizeBytes }
    val canDelete: Boolean get() = toDelete.isNotEmpty()
}
