package com.zdmgold.cleankoach

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.feature.settings.SettingsViewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zdmgold.cleankoach.core.locale.LocaleManager
import com.zdmgold.cleankoach.core.ui.theme.CleanKoachTheme
import com.zdmgold.cleankoach.navigation.CleanKoachNavHost
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zdmgold.cleankoach.core.ads.AdsManager
import com.zdmgold.cleankoach.core.diagnostics.CrashReporter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashReporter.note("MainActivity onCreate (restored=${savedInstanceState != null})")
        applyStoredLocaleSafely()
        super.onCreate(savedInstanceState)
        adsManager.start(this)
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> adsManager.onAppBackgrounded()
                Lifecycle.Event.ON_START -> adsManager.onAppForegrounded(this)
                else -> Unit
            }
        })
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.theme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                        android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
                    ) { darkTheme }
                )
                onDispose { }
            }
            CleanKoachTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CleanKoachNavHost(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        CrashReporter.note("MainActivity onDestroy (changingConfig=$isChangingConfigurations, finishing=$isFinishing)")
        super.onDestroy()
    }

    private fun applyStoredLocaleSafely() {
        runCatching {
            val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
            val stored = prefs.getString("locale", null) ?: return
            if (stored == LocaleManager.SYSTEM) return
            if (stored != LocaleManager.current()) {
                LocaleManager.apply(stored)
            }
        }
    }
}
