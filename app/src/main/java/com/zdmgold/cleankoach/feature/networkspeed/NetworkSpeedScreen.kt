package com.zdmgold.cleankoach.feature.networkspeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.core.ui.components.DeterminateProgress
import com.zdmgold.cleankoach.core.ui.components.IndeterminateProgress
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.util.FormatUtils

@Composable
fun NetworkSpeedScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NetworkSpeedViewModel = hiltViewModel()
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
                    text = "Network Speed",
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (state.phase) {
                    SpeedTestPhase.READY -> {
                        Text(
                            text = "Measure your download speed.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    SpeedTestPhase.PREPARING -> {
                        Text(
                            text = "Preparing",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        IndeterminateProgress()
                    }

                    SpeedTestPhase.TESTING -> {
                        Text(
                            text = "Testing download speed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        DeterminateProgress(progress = 0.5f)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.result?.let { FormatUtils.bytes(it.bytesTransferred) + " transferred" } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SpeedTestPhase.DONE -> {
                        Text(
                            text = "Download speed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.result?.let { FormatUtils.mbps(it.downloadMbps) } ?: "—",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.W300,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.result?.let {
                                "Transferred ${FormatUtils.bytes(it.bytesTransferred)} in ${it.durationMillis / 1000}s"
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    SpeedTestPhase.FAILED -> {
                        Text(
                            text = "Test failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                PrimaryButton(
                    text = when (state.phase) {
                        SpeedTestPhase.READY -> "Start test"
                        SpeedTestPhase.PREPARING,
                        SpeedTestPhase.TESTING -> "Testing…"
                        SpeedTestPhase.DONE -> "Test again"
                        SpeedTestPhase.FAILED -> "Retry"
                    },
                    onClick = viewModel::run,
                    enabled = state.phase != SpeedTestPhase.PREPARING &&
                        state.phase != SpeedTestPhase.TESTING
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Download only. Upload and ping are not measured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
