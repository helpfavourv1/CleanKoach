package com.zdmgold.cleankoach.core.media

import com.google.common.truth.Truth.assertThat
import com.zdmgold.cleankoach.core.domain.model.MediaItem
import com.zdmgold.cleankoach.core.domain.model.MediaKind
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class SimilarityGrouperTest {

    private val dHashCalculator: DHashCalculator = mockk()
    private lateinit var grouper: SimilarityGrouper

    @Before
    fun setup() {
        every { dHashCalculator.hammingDistance(any(), any()) } answers {
            val a = firstArg<Long>()
            val b = secondArg<Long>()
            java.lang.Long.bitCount(a xor b)
        }
        grouper = SimilarityGrouper(dHashCalculator)
    }

    private fun item(
        id: Long,
        size: Long = 1000L,
        name: String = "photo_$id.jpg"
    ): MediaItem = MediaItem(
        id = id,
        uri = "content://media/$id",
        displayName = name,
        kind = MediaKind.IMAGE,
        sizeBytes = size,
        width = 100,
        height = 100,
        durationMs = 0L,
        dateAdded = 100L + id,
        mimeType = "image/jpeg",
        relativePath = "DCIM/",
        isTrashed = false
    )

    @Test
    fun `empty input returns empty list`() {
        assertThat(grouper.group(emptyList())).isEmpty()
    }

    @Test
    fun `identical dhashes form a group`() {
        val entries = listOf(
            SimilarityEntry(item(1, size = 1000L), dhash = 0x0000000000000000L),
            SimilarityEntry(item(2, size = 2000L), dhash = 0x0000000000000000L)
        )
        val groups = grouper.group(entries)
        assertThat(groups).hasSize(1)
        assertThat(groups[0].items).hasSize(2)
    }

    @Test
    fun `dhashes beyond the threshold stay separate`() {
        // Two hashes differing in more than 10 bits
        val entries = listOf(
            SimilarityEntry(item(1), dhash = 0x0000000000000000L),
            SimilarityEntry(item(2), dhash = -1L)
        )
        val groups = grouper.group(entries, threshold = 10)
        assertThat(groups).isEmpty()
    }

    @Test
    fun `dhashes within the threshold are grouped`() {
        // Only 3 bits differ
        val a = 0x0000000000000000L
        val b = 0x0000000000000007L  // 3 bits set
        val entries = listOf(
            SimilarityEntry(item(1), dhash = a),
            SimilarityEntry(item(2), dhash = b)
        )
        val groups = grouper.group(entries, threshold = 10)
        assertThat(groups).hasSize(1)
    }

    @Test
    fun `bestMediaId points to the largest item`() {
        val entries = listOf(
            SimilarityEntry(item(1, size = 100L), dhash = 0L),
            SimilarityEntry(item(2, size = 5000L), dhash = 0L),
            SimilarityEntry(item(3, size = 200L), dhash = 0L)
        )
        val groups = grouper.group(entries)
        assertThat(groups).hasSize(1)
        assertThat(groups[0].bestMediaId).isEqualTo(2L)
    }

    @Test
    fun `groups are sorted by reclaimable bytes descending`() {
        val entries = listOf(
            // Group 1: small
            SimilarityEntry(item(1, size = 100L), dhash = 0L),
            SimilarityEntry(item(2, size = 100L), dhash = 0L),
            // Group 2: big
            SimilarityEntry(item(3, size = 50000L), dhash = -1L),
            SimilarityEntry(item(4, size = 50000L), dhash = -1L)
        )
        val groups = grouper.group(entries)
        assertThat(groups).hasSize(2)
        assertThat(groups[0].reclaimableBytes).isAtLeast(groups[1].reclaimableBytes)
    }
}
