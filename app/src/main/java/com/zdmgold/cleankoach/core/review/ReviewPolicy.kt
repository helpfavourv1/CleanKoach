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
    const val CARD_MIN_CLEANUPS = 10
    const val CARD_MIN_DAYS = 3
    const val AUTO_MIN_CLEANUPS = 15
    const val AUTO_MIN_DAYS = 5
    const val AD_QUIET_MS = 2 * 60_000L
    private const val DAY_MS = 24 * 60 * 60_000L

    /** Days the card stays hidden after the n-th dismissal (or Rate tap). It always comes back: 30, 30, 60, 90, then every 180 days. */
    fun cooldownDays(dismissCount: Int): Int = when (dismissCount) {
        0 -> 0
        1 -> 30
        2 -> 30
        3 -> 60
        4 -> 90
        else -> 180
    }

    fun shouldShowCard(s: ReviewSnapshot, now: Long, adQuiet: Boolean): Boolean {
        if (s.resolved || s.autoPrompted || !adQuiet) return false
        if (s.cleanups < CARD_MIN_CLEANUPS || s.cleanupDays < CARD_MIN_DAYS) return false
        val cooldown = cooldownDays(s.cardDismissCount)
        return now - s.cardLastDismissAt >= cooldown * DAY_MS
    }

    /** Fallback popup, only if the card was never shown. After 15 clean-ups on 5 different days. */
    fun shouldAutoPrompt(s: ReviewSnapshot, adQuiet: Boolean): Boolean =
        adQuiet && !s.resolved && !s.autoPrompted && !s.cardEverShown &&
            s.cleanups >= AUTO_MIN_CLEANUPS && s.cleanupDays >= AUTO_MIN_DAYS

    /** True when no interstitial is showing and none was shown in the last [AD_QUIET_MS]. */
    fun isAdQuiet(now: Long, adShowing: Boolean, lastAdAt: Long): Boolean =
        !adShowing && (lastAdAt == 0L || now - lastAdAt >= AD_QUIET_MS)
}
