package com.zdmgold.cleankoach.feature.designpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.zdmgold.cleankoach.core.ui.components.AdBannerPlaceholder
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.ui.components.SectionHeader
import com.zdmgold.cleankoach.core.ui.components.SecondaryButton
import com.zdmgold.cleankoach.core.ui.components.SettingsChip
import com.zdmgold.cleankoach.core.ui.components.StorageRing
import com.zdmgold.cleankoach.core.ui.components.ToolCard

@Composable
fun DesignPreviewScreen(modifier: Modifier = Modifier) {
    var cleaning by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    StorageRing(
                        valueText = "1.24 GB",
                        progress = if (cleaning) 0.62f else null,
                        caption = "Trash size",
                        subline = "Trashed photos and videos plus app cache"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "48.2 GB of 128 GB used · 38%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PrimaryButton(
                        text = "CLEAN UP",
                        onClick = { cleaning = !cleaning },
                        loading = cleaning
                    )
                }

                Spacer(Modifier.height(8.dp))

                SectionHeader(text = "Photos & Videos")

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolCard(
                        title = "Large files",
                        description = "Lists your biggest photos, videos and audio.",
                        onClick = {},
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    ToolCard(
                        title = "Duplicates",
                        description = "Finds identical photos, videos and audio.",
                        onClick = {},
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                SectionHeader(text = "Phone Tools")

                Spacer(Modifier.height(24.dp))

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SecondaryButton(text = "Secondary action", onClick = {})
                }

                Spacer(Modifier.height(32.dp))
            }

            AdBannerPlaceholder(visible = true)
        }
    }
}
