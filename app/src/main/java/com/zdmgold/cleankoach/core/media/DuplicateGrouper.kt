package com.zdmgold.cleankoach.core.media

import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateGrouper @Inject constructor() {

    fun group(entries: List<Pair<MediaItem, String>>): List<DuplicateGroup> {
        val byHash = entries.groupBy({ it.second }, { it.first })
        var id = 0L
        return byHash
            .filter { it.value.size > 1 }
            .map { (hash, items) ->
                DuplicateGroup(
                    id = id++,
                    sha256 = hash,
                    items = items.sortedByDescending { it.dateAdded },
                    totalBytes = items.sumOf { it.sizeBytes }
                )
            }
            .sortedByDescending { it.reclaimableBytes }
    }
}
