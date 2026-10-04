package com.zdmgold.cleankoach.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import com.zdmgold.cleankoach.core.ui.components.StorageRing
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
import com.zdmgold.cleankoach.core.util.ExternalActions
import com.zdmgold.cleankoach.core.util.FormatUtils
import com.zdmgold.cleankoach.feature.cleanup.CleanUpConfirmSheet
import com.zdmgold.cleankoach.feature.cleanup.CleanUpResultSheet
import com.zdmgold.cleankoach.feature.permission.MediaAccessDisclosureContent

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val jakartaSans = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.W900,
        variationSettings = FontVariation.Settings(FontVariation.weight(900))
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.W700,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

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

    val hostContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(state.reviewRequested, state.cleanUpResultVisible) {
        if (state.reviewRequested && !state.cleanUpResultVisible) {
            with(ExternalActions) { hostContext.findActivity() }?.let(ExternalActions::requestInAppReview)
            viewModel.onReviewHandled()
        }
    }

    val media = mediaTint()
    val optimizer = optimizerTint()
    val tools = toolsTint()

    val storage = state.storage
    val ringValue = buildAnnotatedString {
        val short = storage?.let { FormatUtils.bytesShort(it.totalReclaimableBytes) }
        if (short == null) {
            withStyle(SpanStyle(fontSize = 26.sp)) { append("—") }
        } else {
            val parts = short.split(" ")
            withStyle(SpanStyle(fontSize = 26.sp)) { append(parts.first()) }
            if (parts.size > 1) {
                withStyle(SpanStyle(fontSize = 13.sp)) { append(" " + parts[1]) }
            }
        }
    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wordmark(stringResource(R.string.app_name)),
                    style = TextStyle(
                        fontFamily = jakartaSans,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        letterSpacing = (-1).sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { languageSheetVisible = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = stringResource(R.string.settings_language),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        settingsViewModel.setTheme(if (darkTheme) "light" else "dark")
                    }
                ) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.settings_theme),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
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
            val brandBlue = if (darkTheme) Color(0xFF6FA3FF) else Color(0xFF1F5FD6)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StorageRing(
                        valueText = ringValue,
                        progress = fill,
                        caption = stringResource(R.string.home_ring_caption),
                        subline = null,
                        size = 108.dp,
                        strokeWidth = 12.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = storageLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(
                                        ((storage?.percentUsed ?: 0) / 100f).coerceIn(0f, 1f)
                                    )
                                    .background(brandBlue)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    if (!state.mediaPermissionGranted) {
                                        viewModel.showDisclosure()
                                    } else {
                                        viewModel.onCleanUpPressed()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.home_button_cleanup),
                                style = TextStyle(
                                    fontFamily = jakartaSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W700,
                                    letterSpacing = 0.9.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
