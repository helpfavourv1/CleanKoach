package com.zdmgold.cleankoach.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(4.dp))

                SettingsRow(
                    label = "Theme",
                    value = state.theme.replaceFirstChar { it.uppercase() },
                    onClick = onOpenTheme
                )
                SettingsRow(
                    label = "Language",
                    value = languageLabel(state.language),
                    onClick = onOpenLanguage
                )
                SettingsRow(
                    label = "Notifications",
                    value = if (state.notificationsEnabled) "On" else "Off",
                    onClick = onOpenNotifications
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = if (state.proEntitled) "Pro · active" else "Go Pro",
                    value = if (state.proEntitled) null else "Remove ads",
                    onClick = onOpenPro
                )
                SettingsRow(
                    label = "Restore purchase",
                    onClick = viewModel::restorePurchase
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = "Share this app",
                    onClick = { }
                )
                SettingsRow(
                    label = "Rate this app",
                    onClick = { }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = "Privacy policy",
                    onClick = { }
                )
                SettingsRow(
                    label = "Terms of service",
                    onClick = { }
                )
                SettingsRow(
                    label = "Support",
                    onClick = { }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = "About",
                    value = "v${state.appVersion.ifBlank { "0.1.0" }}",
                    onClick = onOpenAbout
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun languageLabel(code: String): String = when (code) {
    "en" -> "English"
    "es" -> "Español"
    "pt" -> "Português"
    "fr" -> "Français"
    "de" -> "Deutsch"
    "it" -> "Italiano"
    "hi" -> "हिन्दी"
    "in" -> "Bahasa Indonesia"
    "ja" -> "日本語"
    "ko" -> "한국어"
    "ru" -> "Русский"
    else -> "English"
}
