package com.zdmgold.cleankoach.feature.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.core.ui.components.EmptyState
import com.zdmgold.cleankoach.core.ui.components.FullScreenLoading
import com.zdmgold.cleankoach.core.ui.components.MediaRow
import com.zdmgold.cleankoach.core.util.FormatUtils

@Composable
fun ScreenshotsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScreenshotsViewModel = hiltViewModel()
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Screenshots",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.items.isNotEmpty()) {
                        Text(
                            text = "${state.items.size} items · ${FormatUtils.bytes(state.totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (state.items.isNotEmpty()) {
                    TextButton(onClick = viewModel::toggleAll) {
                        Text(if (state.allSelected) "Clear" else "All")
                    }
                }
            }

            if (state.loading) {
                FullScreenLoading()
            } else if (state.items.isEmpty()) {
                EmptyState(
                    title = "No screenshots",
                    message = "Nothing captured on this device yet.",
                    icon = Icons.Filled.Phone
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.items, key = { it.id }) { item ->
                        MediaRow(
                            item = item,
                            selected = item.id in state.selectedIds,
                            onToggle = { viewModel.toggle(item.id) }
                        )
                    }
                }

                if (state.canDelete) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${state.selectionCount} selected · ${FormatUtils.bytes(state.selectedBytes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = viewModel::delete) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
