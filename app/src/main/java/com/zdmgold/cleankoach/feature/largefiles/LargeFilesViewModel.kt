package com.zdmgold.cleankoach.feature.largefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LargeFilesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LargeFilesUiState())
    val state: StateFlow<LargeFilesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val items = runCatching { mediaRepository.scanLargeFiles(limit = 300) }
                .getOrDefault(emptyList())
            _state.update {
                it.copy(loading = false, items = items)
            }
        }
    }

    fun toggle(id: Long) {
        _state.update { current ->
            val next = if (id in current.selectedIds) current.selectedIds - id
            else current.selectedIds + id
            current.copy(selectedIds = next)
        }
    }

    fun toggleAll() {
        _state.update { current ->
            current.copy(
                selectedIds = if (current.allSelected) emptySet()
                else current.items.map { it.id }.toSet()
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun delete() {
        viewModelScope.launch {
            val ids = _state.value.selectedIds.toList()
            if (ids.isEmpty()) return@launch
            runCatching { mediaRepository.deleteMedia(ids) }
            _state.update { it.copy(selectedIds = emptySet()) }
            load()
        }
    }
}
