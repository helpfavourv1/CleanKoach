package com.zdmgold.cleankoach.core.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.zdmgold.cleankoach.R

object NotificationChannels {
    const val CLEANUP_REMINDER = "cleanup_reminder"
    const val STORAGE_ALERT = "storage_alert"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return

        val cleanup = NotificationChannel(
            CLEANUP_REMINDER,
            context.getString(R.string.channel_cleanup_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_cleanup_description)
        }

        val alert = NotificationChannel(
            STORAGE_ALERT,
            context.getString(R.string.channel_storage_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_storage_description)
        }

        manager.createNotificationChannel(cleanup)
        manager.createNotificationChannel(alert)
    }
}
