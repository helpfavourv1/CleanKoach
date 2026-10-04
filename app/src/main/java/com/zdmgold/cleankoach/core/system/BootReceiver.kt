package com.zdmgold.cleankoach.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: NotificationScheduler

    @Inject
    lateinit var settings: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                runCatching {
                    scheduler.reconcile(
                        master = settings.notificationsEnabled.first(),
                        weekly = settings.weeklyReminder.first(),
                        storage = settings.storageAlerts.first()
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
