package com.zdmgold.cleankoach.core.ads

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdPacingTest {
    private val now = 10_000_000_000L
    private val min = 60_000L

    @Test fun firstAdIsAllowed() {
        assertThat(AdPacing.canShow(now, emptyList(), 0, 0)).isTrue()
    }

    @Test fun gapUnderFiveMinutesBlocks() {
        assertThat(AdPacing.canShow(now, listOf(now - 4 * min), 1, 1)).isFalse()
        assertThat(AdPacing.canShow(now, listOf(now - 5 * min), 1, 1)).isTrue()
    }

    @Test fun sixPerHourIsTheCap() {
        val six = (1..6).map { now - it * 6 * min } // 6 ads, 6 minutes apart, all inside the hour
        assertThat(AdPacing.canShow(now, six, 6, 3)).isFalse()
        val five = six.drop(1)
        assertThat(AdPacing.canShow(now, five, 5, 3)).isTrue()
    }

    @Test fun twentyOnePerDayIsTheCap() {
        assertThat(AdPacing.canShow(now, emptyList(), 20, 0)).isTrue()
        assertThat(AdPacing.canShow(now, emptyList(), 21, 0)).isFalse()
    }

    @Test fun fourPerSessionIsTheCap() {
        assertThat(AdPacing.canShow(now, emptyList(), 0, 4)).isFalse()
    }

    @Test fun parseAndPrune() {
        assertThat(AdPacing.parseTimes("1, 2,x,3")).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(AdPacing.parseTimes(null)).isEmpty()
        assertThat(AdPacing.pruned(now, listOf(now - 2 * AdPacing.HOUR_MS, now - min))).containsExactly(now - min)
    }
}
