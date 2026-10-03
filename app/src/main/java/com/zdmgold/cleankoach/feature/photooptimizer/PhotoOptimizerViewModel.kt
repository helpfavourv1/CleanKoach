package com.zdmgold.cleankoach.feature.photooptimizer

import android.content.Context
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.media.PhotoCompressor
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PhotoOptimizerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaRepository: MediaRepository,
    private val photoCompressor: PhotoCompressor
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoOptimizerUiState())
    val state: StateFlow<PhotoOptimizerUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val items = runCatching { mediaRepository.scanLargeFiles(limit = 300) }
                .getOrDefault(emptyList())
                .filter { it.mimeType.startsWith("image/") }
            _state.update { it.copy(loading = false, items = items) }
        }
    }

    fun toggle(id: Long) {
        _state.update { current ->
            val next = if (id in current.selectedIds) current.selectedIds - id
            else current.selectedIds + id
            current.copy(selectedIds = next, lastRunSummary = null)
        }
    }

    fun toggleAll() {
        _state.update { current ->
            current.copy(
                selectedIds = if (current.allSelected) emptySet()
                else current.items.map { it.id }.toSet(),
                lastRunSummary = null
            )
        }
    }

    fun run() {
        val current = _state.value
        if (!current.canRun) return
        viewModelScope.launch {
            val targets = current.items.filter { it.id in current.selectedIds }
            _state.update {
                it.copy(
                    processing = true,
                    processed = 0,
                    totalToProcess = targets.size,
                    totalSavedBytes = 0L,
                    lastRunSummary = null
                )
            }

            var totalSaved = 0L
            var success = 0

            withContext(Dispatchers.IO) {
                targets.forEachIndexed { index, item ->
                    val result = runCatching {
                        photoCompressor.compress(
                            inputUri = Uri.parse(item.uri),
                            originalBytes = item.sizeBytes,
                            originalName = item.displayName
                        )
                    }.getOrNull()

                    if (result != null) {
                        totalSaved += result.savedBytes
                        success++
                    }
                    _state.update {
                        it.copy(
                            processed = index + 1,
                            totalSavedBytes = totalSaved
                        )
                    }
                }
            }

            _state.update {
                it.copy(
                    processing = false,
                    selectedIds = emptySet(),
                    lastRunSummary = context.getString(
                        R.string.photo_optimizer_summary,
                        success,
                        FormatUtils.bytes(totalSaved)
                    )
                )
            }
        }
    }

    fun clearSummary() {
        _state.update { it.copy(lastRunSummary = null) }
    }
}
