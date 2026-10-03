package com.zdmgold.cleankoach.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.zdmgold.cleankoach.core.media.MediaPermissions
import com.zdmgold.cleankoach.core.ui.components.AdBannerPlaceholder
import com.zdmgold.cleankoach.core.ui.components.PhoneToolCard
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.ui.components.SectionHeader
import com.zdmgold.cleankoach.core.ui.components.SettingsChip
import com.zdmgold.cleankoach.core.ui.components.StorageRing
import com.zdmgold.cleankoach.core.ui.components.ToolCard
import com.zdmgold.cleankoach.core.ui.theme.mediaTint
import com.zdmgold.cleankoach.core.ui.theme.optimizerTint
import com.zdmgold.cleankoach.core.ui.theme.toolsTint
import com.zdmgold.cleankoach.core.util.FormatUtils
import com.zdmgold.cleankoach.feature.cleanup.CleanUpConfirmSheet
import com.zdmgold.cleankoach.feature.cleanup.CleanUpResultSheet
import com.zdmgold.cleankoach.feature.permission.MediaAccessDisclosureContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenLargeFiles: () -> Unit,
    onOpenDuplicates: () -> Unit,
    onOpenSimilar: () -> Unit,
    onOpenScreenshots: () -> Unit,
    onOpenPhotoOptimizer: () -> Unit,
    onOpenVideoOptimizer: () -> Unit,
    onOpenActivityMonitor: () -> Unit,
    onOpenWifiSecurity: () -> Unit,
    onOpenNetworkSpeed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        viewModel.onPermissionsResult(granted)
    }

    val media = mediaTint()
    val optimizer = optimizerTint()
    val tools = toolsTint()

    val storage = state.storage
    val ringValue = storage?.let { FormatUtils.bytesShort(it.totalReclaimableBytes) } ?: "—"
    val storageLine = storage?.let {
        stringResource(
            R.string.home_storage_line,
            FormatUtils.bytes(it.usedBytes),
            FormatUtils.bytes(it.totalBytes),
            it.percentUsed
        )
    } ?: ""

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                SettingsChip(onClick = onOpenSettings)
            }

            Spacer(Modifier.height(4.dp))

            SectionHeader(
                text = stringResource(R.string.home_section_phone_tools),
                padding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                PhoneToolCard(
                    title = stringResource(R.string.tool_activity_monitor_title),
                    icon = Icons.AutoMirrored.Filled.List,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = stringResource(R.string.tool_activity_monitor_badge),
                    onClick = onOpenActivityMonitor,
                    modifier = Modifier.weight(1f)
                )
                PhoneToolCard(
                    title = stringResource(R.string.tool_wifi_security_title),
                    icon = Icons.Filled.Lock,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = stringResource(R.string.tool_wifi_security_badge),
                    onClick = onOpenWifiSecurity,
                    modifier = Modifier.weight(1f)
                )
                PhoneToolCard(
                    title = stringResource(R.string.tool_network_speed_title),
                    icon = Icons.Filled.Refresh,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = stringResource(R.string.tool_network_speed_badge),
                    onClick = onOpenNetworkSpeed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StorageRing(
                    valueText = ringValue,
                    progress = if (state.scanning) state.scanProgress else null,
                    caption = stringResource(R.string.home_ring_caption),
                    subline = null,
                    size = 220.dp,
                    strokeWidth = 16.dp
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = storageLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                text = stringResource(state.cleanUpLabelRes),
                onClick = {
                    if (!state.mediaPermissionGranted) {
                        viewModel.showDisclosure()
                    } else {
                        viewModel.onCleanUpPressed()
                    }
                },
                enabled = state.mediaPermissionGranted || !state.scanning,
                loading = state.scanning
            )

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    text = stringResource(R.string.home_section_photos_videos),
                    padding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ToolCard(
                        title = stringResource(R.string.tool_large_files_title),
                        description = stringResource(R.string.tool_large_files_desc),
                        icon = Icons.Filled.Delete,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = state.largeFilesBadge,
                        onClick = onOpenLargeFiles,
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = stringResource(R.string.tool_duplicates_title),
                        description = stringResource(R.string.tool_duplicates_desc),
                        icon = Icons.Filled.Star,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = state.duplicatesBadge,
                        onClick = onOpenDuplicates,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ToolCard(
                        title = stringResource(R.string.tool_similar_title),
                        description = stringResource(R.string.tool_similar_desc),
                        icon = Icons.Filled.Favorite,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = state.similarBadge,
                        onClick = onOpenSimilar,
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = stringResource(R.string.tool_screenshots_title),
                        description = stringResource(R.string.tool_screenshots_desc),
                        icon = Icons.Filled.Phone,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = state.screenshotsBadge,
                        onClick = onOpenScreenshots,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ToolCard(
                        title = stringResource(R.string.tool_photo_optimizer_title),
                        description = stringResource(R.string.tool_photo_optimizer_desc),
                        icon = Icons.Filled.Create,
                        iconContainerColor = optimizer.container,
                        iconContentColor = optimizer.content,
                        badge = state.photoOptimizerBadge,
                        onClick = onOpenPhotoOptimizer,
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = stringResource(R.string.tool_video_optimizer_title),
                        description = stringResource(R.string.tool_video_optimizer_desc),
                        icon = Icons.Filled.PlayArrow,
                        iconContainerColor = optimizer.container,
                        iconContentColor = optimizer.content,
                        badge = state.videoOptimizerBadge,
                        onClick = onOpenVideoOptimizer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            AdBannerPlaceholder(
                visible = state.adVisible,
                modifier = Modifier.navigationBarsPadding()
            )

            Spacer(Modifier.height(4.dp))
        }

        if (state.cleanUpSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = viewModel::onCleanUpCancelled,
                sheetState = rememberModalBottomSheetState()
            ) {
                CleanUpConfirmSheet(
                    itemCount = 0,
                    sizeBytes = state.storage?.totalReclaimableBytes ?: 0L,
                    onConfirm = viewModel::onCleanUpConfirmed,
                    onCancel = viewModel::onCleanUpCancelled
                )
            }
        }

        if (state.cleanUpResultVisible) {
            ModalBottomSheet(
                onDismissRequest = viewModel::onResultDismissed,
                sheetState = rememberModalBottomSheetState()
            ) {
                CleanUpResultSheet(
                    freedBytes = state.lastCleanupResult?.freedBytes ?: 0L,
                    onDismiss = viewModel::onResultDismissed
                )
            }
        }

        if (state.mediaDisclosureVisible) {
            ModalBottomSheet(
                onDismissRequest = viewModel::onDisclosureDismiss,
                sheetState = rememberModalBottomSheetState()
            ) {
                MediaAccessDisclosureContent(
                    onAllow = {
                        viewModel.onDisclosureAccepted()
                        permissionLauncher.launch(MediaPermissions.required())
                    },
                    onNotNow = viewModel::onDisclosureDismiss
                )
            }
        }
    }
}
