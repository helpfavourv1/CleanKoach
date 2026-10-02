package com.zdmgold.cleankoach.core.media

import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimilarityGrouper @Inject constructor(
    private val dHashCalculator: DHashCalculator
) {
    companion object {
        const val DEFAULT_HAMMING_THRESHOLD = 10
    }

    fun group(
        entries: List<SimilarityEntry>,
        threshold: Int = DEFAULT_HAMMING_THRESHOLD
    ): List<SimilarGroup> {
        if (entries.isEmpty()) return emptyList()

        val visited = BooleanArray(entries.size)
        val groups = mutableListOf<SimilarGroup>()
        var id = 0L

        for (i in entries.indices) {
            if (visited[i]) continue
            visited[i] = true
            val bucket = mutableListOf(i)
            var maxDistance = 0

            for (j in i + 1 until entries.size) {
                if (visited[j]) continue
                val distance = dHashCalculator.hammingDistance(entries[i].dhash, entries[j].dhash)
                if (distance <= threshold) {
                    visited[j] = true
                    bucket += j
                    if (distance > maxDistance) maxDistance = distance
                }
            }

            if (bucket.size < 2) continue

            val items = bucket.map { entries[it].item }
            val best = items.maxByOrNull { it.sizeBytes } ?: items.first()

            groups += SimilarGroup(
                id = id++,
                items = items,
                bestMediaId = best.id,
                hammingDistance = maxDistance
            )
        }

        return groups.sortedByDescending { it.reclaimableBytes }
    }
}

data class SimilarityEntry(
    val item: MediaItem,
    val dhash: Long
)
