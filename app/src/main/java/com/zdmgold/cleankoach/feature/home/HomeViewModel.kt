package com.zdmgold.cleankoach.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val consentDataStore: ConsentDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            runCatching { mediaRepository.storageStats() }
                .onSuccess { stats ->
                    _state.value = _state.value.copy(
                        loading = false,
                        storage = stats
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(loading = false)
                }
        }
    }

    fun onAllowAccess() {
        _state.value = _state.value.copy(mediaDisclosureVisible = true)
    }

    fun onDisclosureDismiss() {
        _state.value = _state.value.copy(mediaDisclosureVisible = false)
    }

    fun onDisclosureAccepted() {
        viewModelScope.launch {
            consentDataStore.setMediaDisclosureAccepted(true)
            _state.value = _state.value.copy(
                mediaDisclosureVisible = false,
                mediaPermissionGranted = true
            )
            refresh()
        }
    }

    fun onCleanUpPressed() {
        if (!_state.value.cleanUpEnabled) return
        _state.value = _state.value.copy(cleanUpSheetVisible = true)
    }

    fun onCleanUpCancelled() {
        _state.value = _state.value.copy(cleanUpSheetVisible = false)
    }

    fun onCleanUpConfirmed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cleanUpSheetVisible = false,
                scanning = true,
                scanProgress = 0f
            )

            val result: CleanupResult = runCatching {
                val cacheFreed = mediaRepository.clearAppCache()
                val deleteResult = mediaRepository.deleteMedia(emptyList())
                deleteResult.copy(cacheClearedBytes = cacheFreed)
            }.getOrElse {
                CleanupResult(0, 0L, 0L, System.currentTimeMillis())
            }

            _state.value = _state.value.copy(
                scanning = false,
                scanProgress = 1f,
                cleanUpResultVisible = true,
                lastCleanupResult = CleanUpSummary(
                    itemCount = result.deletedItemCount,
                    freedBytes = result.totalFreedBytes
                )
            )
            refresh()
        }
    }

    fun onResultDismissed() {
        _state.value = _state.value.copy(cleanUpResultVisible = false)
    }
}
