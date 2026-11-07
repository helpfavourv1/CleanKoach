package com.zdmgold.cleankoach.core.ads

/** Pure pacing rules for interstitials, kept free of Android so unit tests can cover them. */
object AdPacing {
    const val MIN_GAP_MS = 5 * 60_000L
    const val MAX_PER_DAY = 21
    const val MAX_PER_HOUR = 6
    const val MAX_PER_SESSION = 4
    const val HOUR_MS = 60 * 60_000L

    /**
     * @param shownTimes epoch millis of recent interstitials (at least the last hour)
     * @param shownToday interstitials shown on the current calendar day
     * @param shownThisSession interstitials shown since the app process started
     */
    fun canShow(now: Long, shownTimes: List<Long>, shownToday: Int, shownThisSession: Int): Boolean {
        if (shownThisSession >= MAX_PER_SESSION) return false
        if (shownToday >= MAX_PER_DAY) return false
        val last = shownTimes.maxOrNull()
        if (last != null && now - last < MIN_GAP_MS) return false
        if (shownTimes.count { now - it in 0 until HOUR_MS } >= MAX_PER_HOUR) return false
        return true
    }

    fun parseTimes(raw: String?): List<Long> =
        raw.orEmpty().split(',').mapNotNull { it.trim().toLongOrNull() }

    /** Keeps only the last hour so the saved list stays short. */
    fun pruned(now: Long, times: List<Long>): List<Long> =
        times.filter { now - it in 0 until HOUR_MS }
}
