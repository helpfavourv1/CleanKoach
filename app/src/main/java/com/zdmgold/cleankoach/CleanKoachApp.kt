package com.zdmgold.cleankoach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import com.zdmgold.cleankoach.core.system.NotificationChannels
import com.zdmgold.cleankoach.core.system.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.zdmgold.cleankoach.core.locale.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CleanKoachApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var scheduler: NotificationScheduler

    @Inject
    lateinit var settings: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensure(this)
        // Keep scheduled work in step with the saved switches on every start.
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                scheduler.reconcile(
                    master = settings.notificationsEnabled.first(),
                    weekly = settings.weeklyReminder.first(),
                    storage = settings.storageAlerts.first()
                )
            }
        }
        // Locale is applied in MainActivity after the settings flow is read,
        // so the very first frame already reflects the user's language.
    }
}
