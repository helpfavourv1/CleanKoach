package com.zdmgold.cleankoach.core.review

/** Saved review-related counters. */
data class ReviewSnapshot(
    val cleanups: Int = 0,
    val cleanupDays: Int = 0,
    val cardDismissCount: Int = 0,
    val cardLastDismissAt: Long = 0L,
    val cardEverShown: Boolean = false,
    val resolved: Boolean = false,
    val autoPrompted: Boolean = false
)

/** Pure rules for the rate card on Home and the automatic in-app review prompt. */
object ReviewPolicy {
    const val CARD_MIN_CLEANUPS = 5
    const val CARD_MIN_DAYS = 2
    const val AUTO_MIN_CLEANUPS = 10
    const val AUTO_MIN_DAYS = 3
    const val AD_QUIET_MS = 2 * 60_000L
    private const val DAY_MS = 24 * 60 * 60_000L

    /** Days the card stays hidden after the n-th dismissal; null means never again. */
    fun cooldownDays(dismissCount: Int): Int? = when (dismissCount) {
        0 -> 0
        1 -> 30
        2 -> 30
        3 -> 60
        else -> null
    }

    fun shouldShowCard(s: ReviewSnapshot, now: Long, adQuiet: Boolean): Boolean {
        if (s.resolved || !adQuiet) return false
        if (s.cleanups < CARD_MIN_CLEANUPS || s.cleanupDays < CARD_MIN_DAYS) return false
        val cooldown = cooldownDays(s.cardDismissCount) ?: return false
        return now - s.cardLastDismissAt >= cooldown * DAY_MS
    }

    /** Fallback prompt when the card was never shown. Only after 10 clean-ups on 3 different days. */
    fun shouldAutoPrompt(s: ReviewSnapshot, adQuiet: Boolean): Boolean =
        adQuiet && !s.resolved && !s.autoPrompted && !s.cardEverShown &&
            s.cleanups >= AUTO_MIN_CLEANUPS && s.cleanupDays >= AUTO_MIN_DAYS

    /** True when no interstitial is showing and none was shown in the last [AD_QUIET_MS]. */
    fun isAdQuiet(now: Long, adShowing: Boolean, lastAdAt: Long): Boolean =
        !adShowing && (lastAdAt == 0L || now - lastAdAt >= AD_QUIET_MS)
}
