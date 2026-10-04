package com.zdmgold.cleankoach.feature.photooptimizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.ads.rememberAdActions
import com.zdmgold.cleankoach.core.ui.components.BottomAdBar
import com.zdmgold.cleankoach.core.ui.components.DeterminateProgress
import com.zdmgold.cleankoach.core.ui.components.EmptyState
import com.zdmgold.cleankoach.core.ui.components.FullScreenLoading
import com.zdmgold.cleankoach.core.ui.components.MediaRow
import com.zdmgold.cleankoach.core.ui.components.PrimaryButton
import com.zdmgold.cleankoach.core.util.FormatUtils

@Composable
fun PhotoOptimizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhotoOptimizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val adActions = rememberAdActions()
    LaunchedEffect(state.lastRunSummary) {
        // A batch just finished: natural pause for an ad.
        if (state.lastRunSummary != null) adActions.actionCompleted()
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.photo_optimizer_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.items.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.photo_optimizer_count_line,
                                state.items.size,
                                FormatUtils.bytes(state.items.sumOf { it.sizeBytes })
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (state.items.isNotEmpty()) {
                    TextButton(onClick = viewModel::toggleAll) {
                        Text(
                            text = if (state.allSelected) stringResource(R.string.action_clear)
                            else stringResource(R.string.action_all)
                        )
                    }
                }
            }

            if (state.processing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.photo_optimizer_optimizing,
                            state.processed,
                            state.totalToProcess
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    DeterminateProgress(progress = state.progress)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.photo_optimizer_saved_so_far,
                            FormatUtils.bytes(state.totalSavedBytes)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                }
            } else if (state.lastRunSummary != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = state.lastRunSummary ?: "",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (state.loading) {
                FullScreenLoading(Modifier.weight(1f))
            } else if (state.items.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.photo_optimizer_empty_title),
                    message = stringResource(R.string.photo_optimizer_empty_message),
                    icon = Icons.Filled.Create
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

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.photo_optimizer_selected_line,
                                    state.selectionCount
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormatUtils.bytes(state.selectedBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = stringResource(
                                if (state.processing) R.string.photo_optimizer_button_optimizing
                                else R.string.photo_optimizer_button_optimize
                            ),
                            onClick = viewModel::run,
                            enabled = state.canRun,
                            loading = state.processing
                        )
                    }
                }
            }

            BottomAdBar()
        }
    }
}
