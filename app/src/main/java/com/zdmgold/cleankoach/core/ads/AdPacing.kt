package com.zdmgold.cleankoach.core.ads

/** Pure pacing rule for interstitials, kept free of Android so unit tests can cover it. No hourly, daily or session caps. */
object AdPacing {
    const val MIN_GAP_MS = 7 * 60_000L

    /** @param lastShownAt epoch millis of the last interstitial, or 0 when none was shown yet. */
    fun canShow(now: Long, lastShownAt: Long): Boolean =
        lastShownAt <= 0L || now - lastShownAt >= MIN_GAP_MS
}
