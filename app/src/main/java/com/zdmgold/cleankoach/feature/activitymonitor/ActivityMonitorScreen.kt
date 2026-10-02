package com.zdmgold.cleankoach.feature.activitymonitor

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.core.domain.model.AppUsageItem
import com.zdmgold.cleankoach.core.ui.components.EmptyState
import com.zdmgold.cleankoach.core.ui.components.FullScreenLoading
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.util.DateUtils
import com.zdmgold.cleankoach.core.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityMonitorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityMonitorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

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
                    text = "Activity Monitor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                state.loading -> FullScreenLoading()

                !state.hasAccess -> EmptyState(
                    title = "Usage access needed",
                    message = "Allow Usage Access in system settings to see launches and foreground time.",
                    icon = Icons.Filled.List,
                    action = {
                        PrimaryButton(
                            text = "Open settings",
                            onClick = { openUsageAccessSettings(context) }
                        )
                    }
                )

                state.items.isEmpty() -> EmptyState(
                    title = "No activity",
                    message = "Nothing used in the last 24 hours.",
                    icon = Icons.Filled.List
                )

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.packageName }) { item ->
                        AppUsageCard(item)
                    }
                }
            }
        }

        if (state.disclosureVisible) {
            ModalBottomSheet(
                onDismissRequest = viewModel::onDisclosureDismissed,
                sheetState = rememberModalBottomSheetState()
            ) {
                UsageDisclosureContent(
                    onContinue = {
                        viewModel.onDisclosureAccepted()
                        openUsageAccessSettings(context)
                    },
                    onNotNow = viewModel::onDisclosureDismissed
                )
            }
        }
    }
}

@Composable
private fun AppUsageCard(item: AppUsageItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.appLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.launchCount} launches · ${DateUtils.relative(item.lastUsedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Foreground: ${DateUtils.duration(item.foregroundMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
