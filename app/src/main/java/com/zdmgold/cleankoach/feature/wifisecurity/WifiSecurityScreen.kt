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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.R
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back)
                    )
                }
                Text(
                    text = stringResource(R.string.wifi_security_title),
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
                                text = stringResource(R.string.wifi_check_error),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        CheckRow(
                            title = stringResource(R.string.wifi_check_internet_title),
                            detail = stringResource(
                                if (report.internetAccess) R.string.wifi_check_internet_connected
                                else R.string.wifi_check_internet_disconnected
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = stringResource(R.string.wifi_check_encryption_title),
                            detail = report.encryptionType
                                ?: stringResource(R.string.wifi_check_unavailable)
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = stringResource(R.string.wifi_check_ssl_strip_title),
                            detail = verdictDetail(
                                report.sslStripVerdict,
                                detectedRes = R.string.wifi_check_ssl_strip_detected,
                                safeRes = R.string.wifi_check_ssl_strip_safe
                            ),
                            alert = report.sslStripVerdict == SecurityVerdict.DETECTED
                        )
                        Spacer(Modifier.height(10.dp))
                        CheckRow(
                            title = stringResource(R.string.wifi_check_ssl_split_title),
                            detail = verdictDetail(
                                report.sslSplitVerdict,
                                detectedRes = R.string.wifi_check_ssl_split_detected,
                                safeRes = R.string.wifi_check_ssl_split_safe
                            ),
                            alert = report.sslSplitVerdict == SecurityVerdict.DETECTED
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(
                                R.string.wifi_check_checked_at,
                                DateUtils.relative(report.checkedAt)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = stringResource(R.string.wifi_check_rerun),
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

@Composable
private fun verdictDetail(
    verdict: SecurityVerdict,
    detectedRes: Int,
    safeRes: Int
): String = when (verdict) {
    SecurityVerdict.DETECTED -> stringResource(detectedRes)
    SecurityVerdict.SECURE -> stringResource(safeRes)
    SecurityVerdict.UNAVAILABLE -> stringResource(R.string.wifi_check_unavailable)
}
