package com.zdmgold.cleankoach.feature.screenshots

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.zdmgold.cleankoach.core.ui.components.DeleteBar
import com.zdmgold.cleankoach.core.ads.rememberAdActions
import com.zdmgold.cleankoach.core.ui.components.BottomAdBar
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

    val adActions = rememberAdActions()
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeleteDialogClosed()
        if (result.resultCode == android.app.Activity.RESULT_OK) adActions.actionCompleted()
    }

    LaunchedEffect(state.deleteRequest) {
        state.deleteRequest?.let {
            deleteLauncher.launch(it)
            viewModel.onDeleteRequestLaunched()
        }
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
                        text = stringResource(R.string.screenshots_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.items.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.large_files_count_line,
                                state.items.size,
                                FormatUtils.bytes(state.totalBytes)
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

            if (state.loading) {
                FullScreenLoading(Modifier.weight(1f))
            } else if (state.items.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.screenshots_empty_title),
                    message = stringResource(R.string.screenshots_empty_message),
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
                    DeleteBar(
                        summary = stringResource(
                            R.string.large_files_selection_line,
                            state.selectionCount,
                            FormatUtils.bytes(state.selectedBytes)
                        ),
                        onDelete = viewModel::delete
                    )
                }
            }

            BottomAdBar()
        }
    }
}
