package com.zdmgold.cleankoach.core.media

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StoragePolicyTest {
    @Test fun needsLargestItemTimesCopiesWithMargin() {
        assertThat(StoragePolicy.neededBytes(listOf(100L, 400L, 250L), copies = 2)).isEqualTo(880L)
        assertThat(StoragePolicy.neededBytes(emptyList(), copies = 1)).isEqualTo(0L)
    }

    @Test fun lowWhenFreeBelowNeeded() {
        assertThat(StoragePolicy.isLow(879L, 880L)).isTrue()
        assertThat(StoragePolicy.isLow(880L, 880L)).isFalse()
    }
}
