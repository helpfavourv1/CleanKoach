package com.zdmgold.cleankoach

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zdmgold.cleankoach.core.locale.LocaleManager
import com.zdmgold.cleankoach.core.ui.theme.CleanKoachTheme
import com.zdmgold.cleankoach.navigation.CleanKoachNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyStoredLocaleSafely()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanKoachTheme {
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

    private fun applyStoredLocaleSafely() {
        runCatching {
            val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
            val stored = prefs.getString("locale", null) ?: return
            if (stored != LocaleManager.current()) {
                LocaleManager.apply(stored)
            }
        }
    }
}
