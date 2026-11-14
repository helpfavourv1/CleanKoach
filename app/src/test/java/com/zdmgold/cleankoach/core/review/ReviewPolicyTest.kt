package com.zdmgold.cleankoach.core.review

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReviewPolicyTest {
    private val day = 24 * 60 * 60_000L
    private val now = 100 * day
    private val ready = ReviewSnapshot(cleanups = 10, cleanupDays = 3)

    @Test fun cardNeedsTenCleanupsOnThreeDays() {
        assertThat(ReviewPolicy.shouldShowCard(ready, now, true)).isTrue()
        assertThat(ReviewPolicy.shouldShowCard(ready.copy(cleanups = 9), now, true)).isFalse()
        assertThat(ReviewPolicy.shouldShowCard(ready.copy(cleanupDays = 2), now, true)).isFalse()
    }

    @Test fun cardWaitsForAdQuiet() {
        assertThat(ReviewPolicy.shouldShowCard(ready, now, false)).isFalse()
    }

    @Test fun dismissCooldowns() {
        assertThat(ReviewPolicy.cooldownDays(1)).isEqualTo(30)
        assertThat(ReviewPolicy.cooldownDays(2)).isEqualTo(30)
        assertThat(ReviewPolicy.cooldownDays(3)).isEqualTo(60)
        assertThat(ReviewPolicy.cooldownDays(4)).isEqualTo(90)
        assertThat(ReviewPolicy.cooldownDays(5)).isEqualTo(180)
        assertThat(ReviewPolicy.cooldownDays(12)).isEqualTo(180)
    }

    @Test fun cardStaysHiddenDuringCooldownThenReturns() {
        val dismissed = ready.copy(cardDismissCount = 1, cardLastDismissAt = now - 29 * day)
        assertThat(ReviewPolicy.shouldShowCard(dismissed, now, true)).isFalse()
        assertThat(ReviewPolicy.shouldShowCard(dismissed.copy(cardLastDismissAt = now - 30 * day), now, true)).isTrue()
        val third = ready.copy(cardDismissCount = 3, cardLastDismissAt = now - 59 * day)
        assertThat(ReviewPolicy.shouldShowCard(third, now, true)).isFalse()
        assertThat(ReviewPolicy.shouldShowCard(third.copy(cardLastDismissAt = now - 60 * day), now, true)).isTrue()
    }

    @Test fun cardNeverGoesOffForGood() {
        val late = ready.copy(cardDismissCount = 9, cardLastDismissAt = now - 179 * day)
        assertThat(ReviewPolicy.shouldShowCard(late, now, true)).isFalse()
        assertThat(ReviewPolicy.shouldShowCard(late.copy(cardLastDismissAt = now - 180 * day), now, true)).isTrue()
        val fourth = ready.copy(cardDismissCount = 4, cardLastDismissAt = now - 90 * day)
        assertThat(ReviewPolicy.shouldShowCard(fourth, now, true)).isTrue()
    }

    @Test fun cardSkippedAfterPopup() {
        assertThat(ReviewPolicy.shouldShowCard(ready.copy(autoPrompted = true), now, true)).isFalse()
    }

    @Test fun resolvedNeverShowsAgain() {
        assertThat(ReviewPolicy.shouldShowCard(ready.copy(resolved = true), now, true)).isFalse()
    }

    @Test fun autoPromptNeedsFifteenCleanupsOnFiveDays() {
        val s = ReviewSnapshot(cleanups = 15, cleanupDays = 5)
        assertThat(ReviewPolicy.shouldAutoPrompt(s, true)).isTrue()
        assertThat(ReviewPolicy.shouldAutoPrompt(s.copy(cleanupDays = 4), true)).isFalse()
        assertThat(ReviewPolicy.shouldAutoPrompt(s.copy(cleanups = 14), true)).isFalse()
    }

    @Test fun autoPromptNeverNearAnAdOrAfterCard() {
        val s = ReviewSnapshot(cleanups = 15, cleanupDays = 5)
        assertThat(ReviewPolicy.shouldAutoPrompt(s, false)).isFalse()
        assertThat(ReviewPolicy.shouldAutoPrompt(s.copy(cardEverShown = true), true)).isFalse()
        assertThat(ReviewPolicy.shouldAutoPrompt(s.copy(autoPrompted = true), true)).isFalse()
    }

    @Test fun adQuietWindow() {
        assertThat(ReviewPolicy.isAdQuiet(now, adShowing = true, lastAdAt = 0L)).isFalse()
        assertThat(ReviewPolicy.isAdQuiet(now, false, now - 60_000L)).isFalse()
        assertThat(ReviewPolicy.isAdQuiet(now, false, now - 120_000L)).isTrue()
        assertThat(ReviewPolicy.isAdQuiet(now, false, 0L)).isTrue()
    }
}
