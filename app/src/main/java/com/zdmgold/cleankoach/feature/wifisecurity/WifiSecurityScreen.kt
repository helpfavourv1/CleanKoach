package com.zdmgold.cleankoach.feature.wifisecurity

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
import androidx.compose.material3.Card
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
import com.zdmgold.cleankoach.core.domain.model.SecurityVerdict
import com.zdmgold.cleankoach.core.ui.components.FullScreenLoading
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.util.DateUtils

@Composable
fun WifiSecurityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WifiSecurityViewModel = hiltViewModel()
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
                    text = "Wi-Fi Security",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.loading) {
                FullScreenLoading()
            } else {
                val report = state.report
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    if (report == null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.error ?: "Unable to inspect connection.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        CheckRow(
                            title = "Internet access",
                            detail = if (report.internetAccess) "Connected." else "No active connection."
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = "Encryption and authentication",
                            detail = report.encryptionType ?: "unavailable on this device"
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = "SSL strip test",
                            detail = verdictDetail(report.sslStripVerdict, detectedText = "Detected. A device on this network is downgrading HTTPS connections.", safeText = "Not detected."),
                            alert = report.sslStripVerdict == SecurityVerdict.DETECTED
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = "SSL split test",
                            detail = verdictDetail(report.sslSplitVerdict, detectedText = "Detected. Your connection is passing through an interceptor.", safeText = "Not detected."),
                            alert = report.sslSplitVerdict == SecurityVerdict.DETECTED
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Checked ${DateUtils.relative(report.checkedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = "Re-run checks",
                        onClick = viewModel::runCheck,
                        enabled = !state.loading
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CheckRow(
    title: String,
    detail: String,
    alert: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = if (alert) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun verdictDetail(verdict: SecurityVerdict, detectedText: String, safeText: String): String =
    when (verdict) {
        SecurityVerdict.DETECTED -> detectedText
        SecurityVerdict.SECURE -> safeText
        SecurityVerdict.UNAVAILABLE -> "unavailable on this device"
    }
