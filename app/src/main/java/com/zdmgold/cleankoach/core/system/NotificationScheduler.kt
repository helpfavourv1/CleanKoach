package com.zdmgold.cleankoach.core.system

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Makes WorkManager match the user's switches. Safe to call any number of times. */
    fun reconcile(master: Boolean, weekly: Boolean, storage: Boolean) {
        if (master && weekly) scheduleWeeklyReminder() else cancelWeeklyReminder()
        if (master && storage) scheduleStorageAlert() else cancelStorageAlert()
    }

    fun scheduleWeeklyReminder() {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextSunday10Am(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CleanupWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelWeeklyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(CleanupWorker.NAME)
    }

    fun scheduleStorageAlert() {
        val request = PeriodicWorkRequestBuilder<StorageAlertWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            StorageAlertWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelStorageAlert() {
        WorkManager.getInstance(context).cancelUniqueWork(StorageAlertWorker.NAME)
    }

    private fun millisUntilNextSunday10Am(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 10)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
        }
        return (calendar.timeInMillis - now).coerceAtLeast(60_000L)
    }
}
