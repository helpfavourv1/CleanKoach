package com.zdmgold.cleankoach.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import com.zdmgold.cleankoach.core.media.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val consentDataStore: ConsentDataStore,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val disclosureAccepted = runCatching {
                consentDataStore.mediaDisclosureAccepted.first()
            }.getOrDefault(false)

            _state.update {
                it.copy(mediaPermissionGranted = permissionChecker.hasFullMediaAccess())
            }

            if (!permissionChecker.hasFullMediaAccess() && !disclosureAccepted) {
                _state.update { it.copy(mediaDisclosureVisible = true, loading = false) }
            } else {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, mediaPermissionGranted = permissionChecker.hasFullMediaAccess()) }
            val stats = runCatching { mediaRepository.storageStats() }.getOrNull()
            _state.update {
                it.copy(loading = false, storage = stats)
            }
        }
    }

    fun showDisclosure() {
        _state.update { it.copy(mediaDisclosureVisible = true) }
    }

    fun onDisclosureDismiss() {
        _state.update { it.copy(mediaDisclosureVisible = false) }
    }

    fun onDisclosureAccepted() {
        viewModelScope.launch {
            consentDataStore.setMediaDisclosureAccepted(true)
            _state.update { it.copy(mediaDisclosureVisible = false) }
        }
    }

    fun onPermissionsResult(granted: Boolean) {
        _state.update { it.copy(mediaPermissionGranted = granted) }
        refresh()
    }

    fun onCleanUpPressed() {
        if (!_state.value.cleanUpEnabled) return
        _state.update { it.copy(cleanUpSheetVisible = true) }
    }

    fun onCleanUpCancelled() {
        _state.update { it.copy(cleanUpSheetVisible = false) }
    }

    fun onCleanUpConfirmed() {
        viewModelScope.launch {
            _state.update {
                it.copy(cleanUpSheetVisible = false, scanning = true, scanProgress = 0f)
            }

            val result: CleanupResult = runCatching {
                val cacheFreed = mediaRepository.clearAppCache()
                val deleteResult = mediaRepository.deleteMedia(emptyList())
                deleteResult.copy(cacheClearedBytes = cacheFreed)
            }.getOrElse {
                CleanupResult(0, 0L, 0L, System.currentTimeMillis())
            }

            _state.update {
                it.copy(
                    scanning = false,
                    scanProgress = 1f,
                    cleanUpResultVisible = true,
                    lastCleanupResult = CleanUpSummary(
                        itemCount = result.deletedItemCount,
                        freedBytes = result.totalFreedBytes
                    )
                )
            }
            refresh()
        }
    }

    fun onResultDismissed() {
        _state.update { it.copy(cleanUpResultVisible = false) }
    }
}
