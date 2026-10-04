package com.zdmgold.cleankoach.feature.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zdmgold.cleankoach.R

private data class LicenseEntry(val name: String, val license: String)

private val entries = listOf(
    LicenseEntry("Plus Jakarta Sans", "SIL Open Font License 1.1"),
    LicenseEntry("Jetpack Compose, Material 3 and Material Icons", "Apache License 2.0"),
    LicenseEntry("AndroidX (Core, Lifecycle, Activity, AppCompat, Navigation, DataStore, Room, WorkManager, Hilt, Media3)", "Apache License 2.0"),
    LicenseEntry("Dagger and Hilt", "Apache License 2.0"),
    LicenseEntry("Kotlin and kotlinx.coroutines", "Apache License 2.0"),
    LicenseEntry("Coil", "Apache License 2.0"),
    LicenseEntry("OkHttp", "Apache License 2.0"),
    LicenseEntry("Google Play Billing, Play In-App Review, Google Mobile Ads SDK, User Messaging Platform", "Google APIs Terms of Service")
)

private const val FONT_NOTICE =
    "Plus Jakarta Sans\n" +
        "Copyright 2020 The Plus Jakarta Sans Project Authors (https://github.com/tokotype/PlusJakartaSans)\n\n" +
        "This Font Software is licensed under the SIL Open Font License, Version 1.1. " +
        "This license is available with a FAQ at: https://scripts.sil.org/OFL"

@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = stringResource(R.string.licenses_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.licenses_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                entries.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider()
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = entry.license,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()
                Text(
                    text = FONT_NOTICE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
