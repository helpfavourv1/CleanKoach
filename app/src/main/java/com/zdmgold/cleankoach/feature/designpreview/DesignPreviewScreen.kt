package com.zdmgold.cleankoach.feature.designpreview

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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@Composable
fun DesignPreviewScreen(modifier: Modifier = Modifier) {
    var cleaning by remember { mutableStateOf(false) }

    val media = mediaTint()
    val optimizer = optimizerTint()
    val tools = toolsTint()

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
                    text = "CleanKoach",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                SettingsChip(onClick = {})
            }

            Spacer(Modifier.height(4.dp))

            SectionHeader(
                text = "Phone Tools",
                padding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                PhoneToolCard(
                    title = "Activity Monitor",
                    icon = Icons.Filled.List,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = "24 H",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                PhoneToolCard(
                    title = "Wi-Fi Security",
                    icon = Icons.Filled.Lock,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = "Check",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                PhoneToolCard(
                    title = "Network Speed",
                    icon = Icons.Filled.Refresh,
                    iconContainerColor = tools.container,
                    iconContentColor = tools.content,
                    badge = "Test",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StorageRing(
                    valueText = "1.24 GB",
                    progress = if (cleaning) 0.62f else null,
                    caption = "Trash size",
                    subline = null,
                    size = 220.dp,
                    strokeWidth = 16.dp
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "48.2 GB of 128 GB used · 38%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                text = "CLEAN UP",
                onClick = { cleaning = !cleaning },
                loading = cleaning
            )

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    text = "Photos & Videos",
                    padding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ToolCard(
                        title = "Large files",
                        description = "Biggest photos, videos and audio.",
                        icon = Icons.Filled.Delete,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = "2.4 GB",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = "Duplicates",
                        description = "Identical photos, videos and audio.",
                        icon = Icons.Filled.Star,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = "612 MB",
                        onClick = {},
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
                        title = "Similar photos",
                        description = "Near-identical shots, keep the best.",
                        icon = Icons.Filled.Favorite,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = "184",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = "Screenshots",
                        description = "Every screenshot, one pass.",
                        icon = Icons.Filled.Phone,
                        iconContainerColor = media.container,
                        iconContentColor = media.content,
                        badge = "1.1 GB",
                        onClick = {},
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
                        title = "Photo optimizer",
                        description = "Compress photos, save space.",
                        icon = Icons.Filled.Create,
                        iconContainerColor = optimizer.container,
                        iconContentColor = optimizer.content,
                        badge = "2,310",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    ToolCard(
                        title = "Video optimizer",
                        description = "Compress videos, save space.",
                        icon = Icons.Filled.PlayArrow,
                        iconContainerColor = optimizer.container,
                        iconContentColor = optimizer.content,
                        badge = "37",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            AdBannerPlaceholder(
                visible = true,
                modifier = Modifier.navigationBarsPadding()
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}
