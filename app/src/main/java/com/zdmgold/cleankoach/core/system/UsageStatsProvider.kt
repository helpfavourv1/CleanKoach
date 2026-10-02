package com.zdmgold.cleankoach.core.system

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.zdmgold.cleankoach.core.domain.model.AppUsageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val usageManager: UsageStatsManager
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val packageManager: PackageManager
        get() = context.packageManager

    fun hasAccess(): Boolean {
        val now = System.currentTimeMillis()
        val events = usageManager.queryEvents(now - 60_000L, now)
        val event = UsageEvents.Event()
        return events.hasNextEvent() && events.getNextEvent(event)
    }

    fun queryLast24Hours(): List<AppUsageItem> {
        val now = System.currentTimeMillis()
        val start = now - 24L * 60L * 60L * 1000L

        val events = usageManager.queryEvents(start, now)
        val event = UsageEvents.Event()

        val foregroundStart = mutableMapOf<String, Long>()
        val foregroundTotal = mutableMapOf<String, Long>()
        val launchCount = mutableMapOf<String, Int>()
        val lastSeen = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (pkg == context.packageName) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    foregroundStart[pkg] = event.timeStamp
                    launchCount[pkg] = (launchCount[pkg] ?: 0) + 1
                    lastSeen[pkg] = event.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val began = foregroundStart.remove(pkg)
                    if (began != null) {
                        val duration = (event.timeStamp - began).coerceAtLeast(0L)
                        foregroundTotal[pkg] = (foregroundTotal[pkg] ?: 0L) + duration
                    }
                }
            }
        }

        // Close any open foreground sessions at now
        foregroundStart.forEach { (pkg, began) ->
            val duration = (now - began).coerceAtLeast(0L)
            foregroundTotal[pkg] = (foregroundTotal[pkg] ?: 0L) + duration
        }

        val packages = foregroundTotal.keys + launchCount.keys
        return packages.mapNotNull { pkg ->
            val label = runCatching {
                val info = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(pkg)

            AppUsageItem(
                packageName = pkg,
                appLabel = label,
                launchCount = launchCount[pkg] ?: 0,
                lastUsedAt = lastSeen[pkg] ?: 0L,
                foregroundMillis = foregroundTotal[pkg] ?: 0L
            )
        }.sortedByDescending { it.foregroundMillis }
    }
}
