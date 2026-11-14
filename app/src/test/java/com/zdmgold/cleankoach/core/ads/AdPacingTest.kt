package com.zdmgold.cleankoach.core.ads

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdPacingTest {
    private val now = 10_000_000_000L
    private val min = 60_000L

    @Test fun firstAdIsAllowed() {
        assertThat(AdPacing.canShow(now, 0L)).isTrue()
    }

    @Test fun gapUnderSevenMinutesBlocks() {
        assertThat(AdPacing.canShow(now, now - 6 * min)).isFalse()
        assertThat(AdPacing.canShow(now, now - 7 * min)).isTrue()
    }

    @Test fun noOtherCapsExist() {
        // Many ads spread 7 minutes apart are always allowed: only the gap matters.
        assertThat(AdPacing.canShow(now, now - 8 * min)).isTrue()
        assertThat(AdPacing.canShow(now, now - 24 * 60 * min)).isTrue()
    }
}
