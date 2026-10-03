package com.zdmgold.cleankoach.core.system

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
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
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
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
