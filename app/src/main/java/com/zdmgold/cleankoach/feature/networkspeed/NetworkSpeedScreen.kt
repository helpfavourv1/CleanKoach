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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.ads.rememberAdActions
import com.zdmgold.cleankoach.core.ui.components.BottomAdBar
import com.zdmgold.cleankoach.core.system.NetworkSpeedTester
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
    val adActions = rememberAdActions()

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
                    text = stringResource(R.string.network_speed_title),
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
                            text = stringResource(R.string.network_speed_ready),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    SpeedTestPhase.PREPARING -> {
                        Text(
                            text = stringResource(R.string.network_speed_preparing),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        IndeterminateProgress()
                    }

                    SpeedTestPhase.TESTING -> {
                        Text(
                            text = stringResource(R.string.network_speed_testing),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = state.result?.let { FormatUtils.mbps(it.downloadMbps) } ?: "—",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.W300,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        DeterminateProgress(
                            progress = state.result?.let {
                                maxOf(
                                    it.bytesTransferred.toFloat() / NetworkSpeedTester.TEST_BYTES,
                                    it.durationMillis.toFloat() / NetworkSpeedTester.MAX_DURATION_MS
                                ).coerceIn(0f, 1f)
                            } ?: 0f
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.result?.let {
                                stringResource(
                                    R.string.network_speed_transferred,
                                    FormatUtils.bytes(it.bytesTransferred)
                                )
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SpeedTestPhase.DONE -> {
                        Text(
                            text = stringResource(R.string.network_speed_download_label),
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
                                stringResource(
                                    R.string.network_speed_transferred_detail,
                                    FormatUtils.bytes(it.bytesTransferred),
                                    it.durationMillis / 1000
                                )
                            } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    SpeedTestPhase.FAILED -> {
                        Text(
                            text = stringResource(R.string.network_speed_failed),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.network_speed_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                PrimaryButton(
                    text = stringResource(
                        when (state.phase) {
                            SpeedTestPhase.READY -> R.string.network_speed_button_start
                            SpeedTestPhase.PREPARING,
                            SpeedTestPhase.TESTING -> R.string.network_speed_button_testing
                            SpeedTestPhase.DONE -> R.string.network_speed_button_again
                            SpeedTestPhase.FAILED -> R.string.network_speed_button_retry
                        }
                    ),
                    onClick = {
                        if (state.phase == SpeedTestPhase.DONE) adActions.actionCompleted()
                        viewModel.run()
                    },
                    enabled = state.phase != SpeedTestPhase.PREPARING &&
                        state.phase != SpeedTestPhase.TESTING
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.network_speed_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            BottomAdBar()
        }
    }
}
