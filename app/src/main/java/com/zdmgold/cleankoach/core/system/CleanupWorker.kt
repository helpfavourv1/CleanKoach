package com.zdmgold.cleankoach.core.system

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zdmgold.cleankoach.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settings: SettingsRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val NAME = "cleanup_weekly_reminder"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        if (!settings.notificationsEnabled.first() || !settings.weeklyReminder.first()) return Result.success()
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return Result.success()

        NotificationChannels.ensure(applicationContext)

        val manager = applicationContext.getSystemService<NotificationManager>()
            ?: return Result.success()

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.CLEANUP_REMINDER
        )
            .setSmallIcon(R.drawable.ic_stat_cleankoach)
            .setContentIntent(NotificationChannels.contentIntent(applicationContext))
            .setContentTitle(applicationContext.getString(R.string.notification_cleanup_title))
            .setContentText(applicationContext.getString(R.string.notification_cleanup_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching { manager.notify(NOTIFICATION_ID, notification) }
        return Result.success()
    }
}
