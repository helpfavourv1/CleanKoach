package com.zdmgold.cleankoach.feature.similar

import android.net.Uri
import androidx.activity.result.IntentSenderRequest
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
            _state.update {
                it.copy(loading = false, groups = groups, bestByGroup = emptyMap())
            }
        }
    }

    fun setBest(groupId: Long, mediaId: Long) {
        _state.update { it.copy(bestByGroup = it.bestByGroup + (groupId to mediaId)) }
    }

    fun delete() {
        viewModelScope.launch {
            val doomed = _state.value.toDelete
            if (doomed.isEmpty()) return@launch
            val sender = runCatching {
                mediaRepository.buildDeleteRequest(doomed.map { Uri.parse(it.uri) })
            }.getOrNull()
            if (sender != null) {
                _state.update { it.copy(deleteRequest = IntentSenderRequest.Builder(sender).build()) }
            } else {
                runCatching { mediaRepository.deleteMedia(doomed.map { Uri.parse(it.uri) }) }
                load()
            }
        }
    }

    fun onDeleteRequestLaunched() {
        _state.update { it.copy(deleteRequest = null) }
    }

    fun onDeleteDialogClosed() {
        load()
    }
}
