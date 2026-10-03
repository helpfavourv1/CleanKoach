package com.zdmgold.cleankoach.feature.similar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.domain.model.SimilarGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimilarPhotosViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SimilarPhotosUiState())
    val state: StateFlow<SimilarPhotosUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            mediaRepository.observeScanStatus().collect { status ->
                _state.update { it.copy(scanStatus = status) }
            }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val groups: List<SimilarGroup> = runCatching { mediaRepository.scanSimilarPhotos() }
                .getOrDefault(emptyList())
            val defaults = groups.map { it.bestMediaId }.toSet()
            _state.update {
                it.copy(loading = false, groups = groups, bestIds = defaults)
            }
        }
    }

    fun setBest(groupId: Long, mediaId: Long) {
        _state.update { it.copy(bestIds = it.bestIds + mediaId) }
    }
}
