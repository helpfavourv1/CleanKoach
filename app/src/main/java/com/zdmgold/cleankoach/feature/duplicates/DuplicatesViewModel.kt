package com.zdmgold.cleankoach.feature.duplicates

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
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val groups: List<DuplicateGroup> = runCatching { mediaRepository.scanDuplicates() }
                .getOrDefault(emptyList())
            val defaults = groups.mapNotNull { it.items.firstOrNull()?.id }.toSet()
            _state.update {
                it.copy(
                    loading = false,
                    groups = groups,
                    keptIds = defaults
                )
            }
        }
    }

    fun setKept(groupId: Long, mediaId: Long) {
        _state.update { it.copy(keptIds = it.keptIds + mediaId) }
    }
}
