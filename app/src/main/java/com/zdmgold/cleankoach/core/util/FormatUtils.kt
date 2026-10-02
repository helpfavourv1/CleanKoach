package com.zdmgold.cleankoach.core.util

import java.util.Locale
import kotlin.math.abs

object FormatUtils {

    fun bytes(value: Long): String {
        if (value <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = value.toDouble()
        var unitIndex = 0
        while (size >= 1024.0 && unitIndex < units.lastIndex) {
            size /= 1024.0
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${value} ${units[0]}"
        } else {
            String.format(Locale.US, "%.2f %s", size, units[unitIndex])
        }
    }

    fun bytesShort(value: Long): String {
        if (value <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = value.toDouble()
        var unitIndex = 0
        while (size >= 1024.0 && unitIndex < units.lastIndex) {
            size /= 1024.0
            unitIndex++
        }
        return if (size >= 100 || unitIndex == 0) {
            "${size.toInt()} ${units[unitIndex]}"
        } else {
            String.format(Locale.US, "%.1f %s", size, units[unitIndex])
        }
    }

    fun count(value: Int): String =
        String.format(Locale.US, "%,d", value)

    fun mbps(value: Double): String =
        String.format(Locale.US, "%.2f Mbps", value)

    fun storageLine(used: Long, total: Long, percent: Int): String =
        "${bytes(used)} of ${bytes(total)} used · ${percent}%"

    fun percent(value: Int): String = "$value%"

    fun storageAlertTitle(percent: Int): String = "Storage is ${percent}% full"

    fun isMeaningfulSize(bytes: Long): Boolean = abs(bytes) > 1024L
}
