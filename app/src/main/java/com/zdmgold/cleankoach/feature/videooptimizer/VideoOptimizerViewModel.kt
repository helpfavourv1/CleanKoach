package com.zdmgold.cleankoach.feature.videooptimizer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.media.VideoCompressor
import com.zdmgold.cleankoach.core.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoOptimizerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val videoCompressor: VideoCompressor
) : ViewModel() {

    private val _state = MutableStateFlow(VideoOptimizerUiState())
    val state: StateFlow<VideoOptimizerUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val items = runCatching { mediaRepository.scanLargeFiles(limit = 200) }
                .getOrDefault(emptyList())
                .filter { it.mimeType.startsWith("video/") }
            _state.update { it.copy(loading = false, items = items) }
        }
    }

    fun setPreset(preset: VideoCompressor.Preset) {
        _state.update { it.copy(preset = preset) }
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
        val preset = current.preset

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
                        videoCompressor.compress(
                            inputUri = Uri.parse(item.uri),
                            originalBytes = item.sizeBytes,
                            originalName = item.displayName,
                            preset = preset
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
                    lastRunSummary = "Optimized ${success} videos · saved ${FormatUtils.bytes(totalSaved)}"
                )
            }
        }
    }
}
