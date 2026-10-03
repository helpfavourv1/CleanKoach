package com.zdmgold.cleankoach.core.media

import com.google.common.truth.Truth.assertThat
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.MediaKind
import org.junit.Test

class DuplicateGrouperTest {

    private val grouper = DuplicateGrouper()

    private fun item(
        id: Long,
        size: Long = 1000L,
        added: Long = 100L,
        name: String = "file_$id.jpg"
    ): MediaItem = MediaItem(
        id = id,
        uri = "content://media/$id",
        displayName = name,
        kind = MediaKind.IMAGE,
        sizeBytes = size,
        width = 100,
        height = 100,
        durationMs = 0L,
        dateAdded = added,
        mimeType = "image/jpeg",
        relativePath = "DCIM/",
        isTrashed = false
    )

    @Test
    fun `empty input returns empty list`() {
        assertThat(grouper.group(emptyList())).isEmpty()
    }

    @Test
    fun `unique hashes produce no groups`() {
        val input = listOf(
            item(1) to "hash_a",
            item(2) to "hash_b",
            item(3) to "hash_c"
        )
        assertThat(grouper.group(input)).isEmpty()
    }

    @Test
    fun `two items with the same hash produce one group`() {
        val input = listOf(
            item(1, size = 5000L) to "same_hash",
            item(2, size = 5000L) to "same_hash"
        )
        val groups = grouper.group(input)
        assertThat(groups).hasSize(1)
        assertThat(groups[0].items).hasSize(2)
        assertThat(groups[0].sha256).isEqualTo("same_hash")
        assertThat(groups[0].totalBytes).isEqualTo(10000L)
    }

    @Test
    fun `reclaimable bytes equals total minus the kept item`() {
        val input = listOf(
            item(1, size = 5000L) to "same_hash",
            item(2, size = 5000L) to "same_hash"
        )
        val groups = grouper.group(input)
        assertThat(groups[0].reclaimableBytes).isEqualTo(5000L)
    }

    @Test
    fun `single-item buckets are dropped`() {
        val input = listOf(
            item(1) to "unique_a",
            item(2) to "same_hash",
            item(3) to "same_hash"
        )
        val groups = grouper.group(input)
        assertThat(groups).hasSize(1)
        assertThat(groups[0].items.map { it.id }).containsExactly(2L, 3L)
    }

    @Test
    fun `groups are sorted by reclaimable bytes descending`() {
        val input = listOf(
            item(1, size = 1000L) to "small_hash",
            item(2, size = 1000L) to "small_hash",
            item(3, size = 50000L) to "big_hash",
            item(4, size = 50000L) to "big_hash",
            item(5, size = 50000L) to "big_hash"
        )
        val groups = grouper.group(input)
        assertThat(groups).hasSize(2)
        assertThat(groups[0].totalBytes).isGreaterThan(groups[1].totalBytes)
    }

    @Test
    fun `within a group items are ordered by dateAdded descending`() {
        val input = listOf(
            item(1, added = 100L) to "same_hash",
            item(2, added = 300L) to "same_hash",
            item(3, added = 200L) to "same_hash"
        )
        val groups = grouper.group(input)
        val dates = groups[0].items.map { it.dateAdded }
        assertThat(dates).isEqualTo(listOf(300L, 200L, 100L))
    }
}
