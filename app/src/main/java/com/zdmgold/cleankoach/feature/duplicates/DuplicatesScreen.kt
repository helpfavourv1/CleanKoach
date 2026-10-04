package com.zdmgold.cleankoach.feature.duplicates

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
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
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import com.zdmgold.cleankoach.core.ui.components.DeleteBar
import com.zdmgold.cleankoach.core.ui.components.EmptyState
import com.zdmgold.cleankoach.core.ui.components.ScanProgress
import com.zdmgold.cleankoach.core.ui.components.MediaRow
import com.zdmgold.cleankoach.core.util.FormatUtils

@Composable
fun DuplicatesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DuplicatesViewModel = hiltViewModel()
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
                        text = stringResource(R.string.duplicates_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.groups.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.duplicates_group_count_line,
                                state.groups.size,
                                FormatUtils.bytes(state.totalReclaimable)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.loading) {
                ScanProgress(state.scanStatus, Modifier.weight(1f))
            } else if (state.groups.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.duplicates_empty_title),
                    message = stringResource(R.string.duplicates_empty_message),
                    icon = Icons.Filled.Star
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.groups, key = { it.id }) { group ->
                        DuplicateGroupCard(
                            group = group,
                            keptId = state.keptIdFor(group),
                            onSetKept = { viewModel.setKept(group.id, it) }
                        )
                    }
                }

                if (state.canDelete) {
                    DeleteBar(
                        summary = stringResource(
                            R.string.duplicates_delete_line,
                            state.toDelete.size,
                            FormatUtils.bytes(state.deleteBytes)
                        ),
                        onDelete = viewModel::delete
                    )
                }
            }

            BottomAdBar()
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    group: DuplicateGroup,
    keptId: Long,
    onSetKept: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.duplicates_identical_items,
                        group.items.size
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.duplicates_reclaimable_label,
                        FormatUtils.bytes(group.reclaimableBytes)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            group.items.forEach { item ->
                val isKept = item.id == keptId
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MediaRow(
                        item = item,
                        selected = isKept,
                        onToggle = { onSetKept(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                    if (isKept) {
                        TextButton(onClick = { onSetKept(item.id) }) {
                            Text(
                                text = stringResource(R.string.duplicates_keep_label),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
