package com.zdmgold.cleankoach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zdmgold.cleankoach.core.system.NotificationChannels
import com.zdmgold.cleankoach.core.locale.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CleanKoachApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensure(this)
        // Locale is applied in MainActivity after the settings flow is read,
        // so the very first frame already reflects the user's language.
    }
}
