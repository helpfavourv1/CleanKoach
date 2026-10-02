package com.zdmgold.cleankoach.core.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationChannels {
    const val CLEANUP_REMINDER = "cleanup_reminder"
    const val STORAGE_ALERT = "storage_alert"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return

        val cleanup = NotificationChannel(
            CLEANUP_REMINDER,
            "Cleanup reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Weekly reminders to review large files and duplicates."
        }

        val alert = NotificationChannel(
            STORAGE_ALERT,
            "Storage alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when device storage is running low."
        }

        manager.createNotificationChannel(cleanup)
        manager.createNotificationChannel(alert)
    }
}
