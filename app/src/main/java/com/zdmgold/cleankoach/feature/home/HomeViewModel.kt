package com.zdmgold.cleankoach.feature.home

import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import com.zdmgold.cleankoach.core.util.FormatUtils
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
    private val permissionChecker: PermissionChecker,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var cleanupBefore: StorageStats? = null

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        val full = permissionChecker.hasFullMediaAccess()
        val partial = permissionChecker.hasPartialMediaAccess()
        _state.update {
            it.copy(mediaPermissionGranted = full || partial, partialMediaAccess = partial && !full)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val full = permissionChecker.hasFullMediaAccess()
            val partial = permissionChecker.hasPartialMediaAccess()
            _state.update {
                it.copy(
                    loading = true,
                    mediaPermissionGranted = full || partial,
                    partialMediaAccess = partial && !full
                )
            }
            val stats = runCatching { mediaRepository.storageStats() }.getOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    storage = stats,
                    largeFilesBadge = stats?.largeFilesBytes?.takeIf { b -> b > 0L }?.let(FormatUtils::bytesShort),
                    screenshotsBadge = stats?.screenshotBytes?.takeIf { b -> b > 0L }?.let(FormatUtils::bytesShort),
                    photoOptimizerBadge = stats?.photoOptimizerCount?.takeIf { c -> c > 0 }?.let(FormatUtils::count),
                    videoOptimizerBadge = stats?.videoOptimizerCount?.takeIf { c -> c > 0 }?.let(FormatUtils::count)
                )
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
        refresh()
    }

    fun onCleanUpPressed() {
        if (_state.value.scanning) return
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
            cleanupBefore = runCatching { mediaRepository.storageStats() }.getOrNull()
            val sender = runCatching {
                mediaRepository.buildDeleteRequest(mediaRepository.trashedUris())
            }.getOrNull()
            if (sender != null) {
                _state.update { it.copy(deleteRequest = IntentSenderRequest.Builder(sender).build()) }
            } else {
                finishCleanUp()
            }
        }
    }

    fun onDeleteRequestLaunched() {
        _state.update { it.copy(deleteRequest = null) }
    }

    fun onDeleteDialogClosed() {
        viewModelScope.launch { finishCleanUp() }
    }

    private suspend fun finishCleanUp() {
        val before = cleanupBefore
        val cacheFreed = runCatching { mediaRepository.clearAppCache() }.getOrDefault(0L)
        val after = runCatching { mediaRepository.storageStats() }.getOrNull()
        val trashFreed = if (before != null && after != null) {
            (before.trashBytes - after.trashBytes).coerceAtLeast(0L)
        } else 0L
        val removed = if (before != null && after != null) {
            (before.trashCount - after.trashCount).coerceAtLeast(0)
        } else 0
        cleanupBefore = null
        val promptReview = runCatching { settingsRepository.recordCleanupAndShouldPromptReview() }
            .getOrDefault(false)
        _state.update {
            it.copy(
                reviewRequested = it.reviewRequested || promptReview,
                scanning = false,
                scanProgress = 1f,
                cleanUpResultVisible = true,
                lastCleanupResult = CleanUpSummary(
                    itemCount = removed,
                    freedBytes = trashFreed + cacheFreed
                )
            )
        }
        refresh()
    }

    fun onReviewHandled() {
        _state.update { it.copy(reviewRequested = false) }
    }

    fun onResultDismissed() {
        _state.update { it.copy(cleanUpResultVisible = false) }
    }
}
