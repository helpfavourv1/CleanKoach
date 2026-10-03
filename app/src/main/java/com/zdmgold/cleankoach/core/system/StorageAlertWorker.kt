package com.zdmgold.cleankoach.core.system

import android.app.NotificationManager
import android.content.Context
import android.os.storage.StorageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zdmgold.cleankoach.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StorageAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val NAME = "storage_alert_check"
        const val NOTIFICATION_ID = 1002
        const val THRESHOLD_PERCENT = 90
    }

    override suspend fun doWork(): Result {
        val statsManager = applicationContext
            .getSystemService<android.app.usage.StorageStatsManager>()
            ?: return Result.success()

        val total = runCatching {
            statsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
        }.getOrDefault(0L)
        if (total <= 0L) return Result.success()

        val free = runCatching {
            statsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
        }.getOrDefault(0L)
        val used = (total - free).coerceAtLeast(0L)
        val percent = ((used.toDouble() / total.toDouble()) * 100).toInt()

        if (percent < THRESHOLD_PERCENT) return Result.success()

        NotificationChannels.ensure(applicationContext)
        val manager = applicationContext.getSystemService<NotificationManager>()
            ?: return Result.success()

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.STORAGE_ALERT
        )
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(
                applicationContext.getString(R.string.notification_storage_title, percent)
            )
            .setContentText(applicationContext.getString(R.string.notification_storage_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
        return Result.success()
    }
}
