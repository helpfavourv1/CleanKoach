package com.zdmgold.cleankoach.feature.duplicates

import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.domain.model.DuplicateGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuplicatesViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DuplicatesUiState())
    val state: StateFlow<DuplicatesUiState> = _state.asStateFlow()

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
            val groups: List<DuplicateGroup> = runCatching { mediaRepository.scanDuplicates() }
                .getOrDefault(emptyList())
            _state.update {
                it.copy(loading = false, groups = groups, keptByGroup = emptyMap())
            }
        }
    }

    fun setKept(groupId: Long, mediaId: Long) {
        _state.update { it.copy(keptByGroup = it.keptByGroup + (groupId to mediaId)) }
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
