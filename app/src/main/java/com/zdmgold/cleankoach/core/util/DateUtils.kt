package com.zdmgold.cleankoach.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val dayFormat = SimpleDateFormat("MMM d", Locale.US)
    private val dayTimeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.US)

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "—"
        val diff = now - millis
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val m = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$m min ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val h = TimeUnit.MILLISECONDS.toHours(diff)
                "$h h ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val d = TimeUnit.MILLISECONDS.toDays(diff)
                "$d d ago"
            }
            else -> dayFormat.format(Date(millis))
        }
    }

    fun dayAndTime(millis: Long): String {
        if (millis <= 0L) return "—"
        return dayTimeFormat.format(Date(millis))
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
}
