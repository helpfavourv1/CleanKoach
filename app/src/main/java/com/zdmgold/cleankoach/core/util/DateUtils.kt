package com.zdmgold.cleankoach.core.util

import android.icu.text.DisplayContext
import android.icu.text.MeasureFormat
import android.icu.text.RelativeDateTimeFormatter
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.ULocale
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** All text here follows the app language (per-app locale), never a fixed English format. */
object DateUtils {

    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "—"
        val diff = now - millis
        if (diff >= TimeUnit.DAYS.toMillis(7)) return formatted(millis, "MMMd")
        val formatter = RelativeDateTimeFormatter.getInstance(
            ULocale.forLocale(Locale.getDefault()),
            null,
            RelativeDateTimeFormatter.Style.SHORT,
            DisplayContext.CAPITALIZATION_NONE
        )
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff.coerceAtLeast(0L))
        val hours = TimeUnit.MILLISECONDS.toHours(diff.coerceAtLeast(0L))
        val days = TimeUnit.MILLISECONDS.toDays(diff.coerceAtLeast(0L))
        return when {
            minutes < 1 -> formatter.format(
                RelativeDateTimeFormatter.Direction.THIS,
                RelativeDateTimeFormatter.AbsoluteUnit.NOW
            )
            hours < 1 -> formatter.format(minutes.toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.MINUTES)
            days < 1 -> formatter.format(hours.toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.HOURS)
            else -> formatter.format(days.toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.DAYS)
        }
    }

    fun dayAndTime(millis: Long): String {
        if (millis <= 0L) return "—"
        return formatted(millis, "MMMdHm")
    }

    fun duration(millis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis.coerceAtLeast(0L))
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val format = MeasureFormat.getInstance(
            ULocale.forLocale(Locale.getDefault()),
            MeasureFormat.FormatWidth.NARROW
        )
        return when {
            hours > 0 -> format.formatMeasures(Measure(hours, MeasureUnit.HOUR), Measure(minutes, MeasureUnit.MINUTE))
            minutes > 0 -> format.formatMeasures(Measure(minutes, MeasureUnit.MINUTE))
            else -> format.formatMeasures(Measure(seconds, MeasureUnit.SECOND))
        }
    }

    private fun formatted(millis: Long, skeleton: String): String {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
        return SimpleDateFormat(pattern, locale).format(Date(millis))
    }
}
