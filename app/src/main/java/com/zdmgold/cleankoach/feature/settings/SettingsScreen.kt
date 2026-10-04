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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.BuildConfig
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.AppLinks
import com.zdmgold.cleankoach.core.util.ExternalActions

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
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
                    label = stringResource(R.string.settings_theme),
                    value = themeLabel(state.theme),
                    onClick = onOpenTheme
                )
                SettingsRow(
                    label = stringResource(R.string.settings_language),
                    value = languageLabel(state.language),
                    onClick = onOpenLanguage
                )
                SettingsRow(
                    label = stringResource(R.string.settings_notifications),
                    value = stringResource(
                        if (state.notificationsEnabled) R.string.settings_notifications_on
                        else R.string.settings_notifications_off
                    ),
                    onClick = onOpenNotifications
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = stringResource(
                        if (state.proEntitled) R.string.settings_pro_active
                        else R.string.settings_go_pro
                    ),
                    value = if (state.proEntitled) null
                    else stringResource(R.string.settings_pro_remove_ads),
                    onClick = onOpenPro
                )
                SettingsRow(
                    label = stringResource(R.string.settings_restore_purchase),
                    onClick = viewModel::restorePurchase
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = stringResource(R.string.settings_share_app),
                    onClick = { ExternalActions.shareApp(context) }
                )
                SettingsRow(
                    label = stringResource(R.string.settings_rate_app),
                    onClick = { ExternalActions.openStorePage(context) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = stringResource(R.string.settings_privacy_policy),
                    onClick = { ExternalActions.openUrl(context, AppLinks.PRIVACY_URL) }
                )
                SettingsRow(
                    label = stringResource(R.string.settings_terms),
                    onClick = { ExternalActions.openUrl(context, AppLinks.TERMS_URL) }
                )
                SettingsRow(
                    label = stringResource(R.string.settings_licenses),
                    onClick = onOpenLicenses
                )
                SettingsRow(
                    label = stringResource(R.string.settings_support),
                    onClick = { ExternalActions.emailSupport(context) }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

                SettingsRow(
                    label = stringResource(R.string.settings_about),
                    value = stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                    onClick = onOpenAbout
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun themeLabel(theme: String): String = stringResource(
    when (theme) {
        "light" -> R.string.settings_theme_light
        "dark" -> R.string.settings_theme_dark
        else -> R.string.settings_theme_system
    }
)

@Composable
private fun languageLabel(code: String): String = stringResource(
    when (code) {
        "en" -> R.string.language_en
        "es" -> R.string.language_es
        "pt" -> R.string.language_pt
        "fr" -> R.string.language_fr
        "de" -> R.string.language_de
        "it" -> R.string.language_it
        "hi" -> R.string.language_hi
        "in" -> R.string.language_in
        "ja" -> R.string.language_ja
        "ko" -> R.string.language_ko
        "ru" -> R.string.language_ru
        else -> R.string.language_en
    }
)
