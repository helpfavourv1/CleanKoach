package com.zdmgold.cleankoach.core.system

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val NAME = "cleanup_weekly_reminder"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        NotificationChannels.ensure(applicationContext)

        val manager = applicationContext.getSystemService<NotificationManager>() ?: return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CLEANUP_REMINDER)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Time for a cleanup")
            .setContentText("Review large files, duplicates and screenshots.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
        return Result.success()
    }
}
