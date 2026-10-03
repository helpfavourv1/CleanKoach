package com.zdmgold.cleankoach.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.zdmgold.cleankoach.core.ui.components.LanguageIconButton
import com.zdmgold.cleankoach.core.ui.components.ThemeIconButton
import com.zdmgold.cleankoach.core.ui.theme.LocalDarkTheme
import com.zdmgold.cleankoach.feature.settings.LanguageOptions
import com.zdmgold.cleankoach.feature.settings.SettingsViewModel
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.media.MediaPermissions
import com.zdmgold.cleankoach.core.ui.components.PhoneToolCard
import com.zdmgold.cleankoach.core.ui.components.SectionHeader
import com.zdmgold.cleankoach.core.ui.components.SettingsChip
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
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.state.collectAsStateWithLifecycle()
    val darkTheme = LocalDarkTheme.current
    var languageSheetVisible by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        viewModel.onPermissionsResult(granted)
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        viewModel.onDeleteDialogClosed()
    }

    LaunchedEffect(state.deleteRequest) {
        state.deleteRequest?.let {
            deleteLauncher.launch(it)
            viewModel.onDeleteRequestLaunched()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
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
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wordmark(stringResource(R.string.app_name)),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.W800,
                    modifier = Modifier.weight(1f)
                )
                LanguageIconButton(
                    onClick = { languageSheetVisible = true },
                    description = stringResource(R.string.settings_language)
                )
                ThemeIconButton(
                    onClick = {
                        settingsViewModel.setTheme(if (darkTheme) "light" else "dark")
                    },
                    description = stringResource(R.string.settings_theme)
                )
                SettingsChip(onClick = onOpenSettings)
            }

            Spacer(Modifier.height(4.dp))

            val fill by animateFloatAsState(
                targetValue = if (state.scanning) 1f else 0f,
                animationSpec = tween(
                    durationMillis = if (state.scanning) 4000 else 600,
                    easing = LinearEasing
                ),
                label = "cleanup_fill"
            )
            val heroShape = RoundedCornerShape(20.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(heroShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fill)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ringValue,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.W700,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.home_ring_caption),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = storageLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .fillMaxHeight()
                        .clip(heroShape)
                        .background(
                            if (state.cleanUpButtonEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(enabled = state.cleanUpButtonEnabled) {
                            if (!state.mediaPermissionGranted) {
                                viewModel.showDisclosure()
                            } else {
                                viewModel.onCleanUpPressed()
                            }
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val contentColor = if (state.cleanUpButtonEnabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ringValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.W700,
                            color = contentColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(state.cleanUpLabelRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.W700,
                            color = contentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    text = stringResource(R.string.home_section_photos_videos),
                    padding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                SectionHeader(
                    text = stringResource(R.string.home_section_phone_tools),
                    padding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                Spacer(Modifier.height(8.dp))
            }
        }

        if (languageSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { languageSheetVisible = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    LanguageOptions(
                        current = settings.language,
                        onSelect = {
                            languageSheetVisible = false
                            settingsViewModel.setLanguage(it)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        if (state.cleanUpSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = viewModel::onCleanUpCancelled,
                sheetState = rememberModalBottomSheetState()
            ) {
                CleanUpConfirmSheet(
                    itemCount = state.storage?.trashCount ?: 0,
                    sizeBytes = state.storage?.cacheBytes ?: 0L,
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

@Composable
private fun wordmark(name: String): androidx.compose.ui.text.AnnotatedString {
    val dark = LocalDarkTheme.current
    val orange = MaterialTheme.colorScheme.primary
    val blue = if (dark) Color(0xFF6FA3FF) else Color(0xFF1F5FD6)
    val split = name.indexOf("Koach").takeIf { it > 0 } ?: name.length
    return buildAnnotatedString {
        withStyle(SpanStyle(color = orange)) { append(name.substring(0, split)) }
        withStyle(SpanStyle(color = blue)) { append(name.substring(split)) }
    }
}
