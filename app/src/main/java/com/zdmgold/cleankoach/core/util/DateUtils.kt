package com.zdmgold.cleankoach.core.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.text.format.DateUtils as AndroidDateUtils

/** All text here follows the app language (per-app locale), never a fixed English format. */
object DateUtils {

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "—"
        val diff = now - millis
        if (diff >= TimeUnit.DAYS.toMillis(7)) return formatted(millis, "MMMd")
        return AndroidDateUtils.getRelativeTimeSpanString(
            millis,
            now,
            AndroidDateUtils.MINUTE_IN_MILLIS,
            AndroidDateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun dayAndTime(millis: Long): String {
        if (millis <= 0L) return "—"
        return formatted(millis, "MMMdHm")
    }

    fun duration(millis: Long): String {
        if (millis <= 0L) return "0s"
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    private fun formatted(millis: Long, skeleton: String): String {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
        return SimpleDateFormat(pattern, locale).format(Date(millis))
    }
}
