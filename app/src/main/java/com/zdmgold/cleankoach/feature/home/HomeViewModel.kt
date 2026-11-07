package com.zdmgold.cleankoach.feature.home

import android.os.SystemClock
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.repository.MediaRepository
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import com.zdmgold.cleankoach.core.domain.model.StorageStats
import com.zdmgold.cleankoach.core.review.ReviewPolicy
import com.zdmgold.cleankoach.core.util.FormatUtils
import com.zdmgold.cleankoach.core.media.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    companion object {
        /** The rate card appears at most once per app launch (process). */
        @Volatile private var reviewCardShownThisLaunch = false
    }

    private var cleanupBefore: StorageStats? = null
    private var cleanupStartedAt = 0L

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        val full = permissionChecker.hasFullMediaAccess()
        val partial = permissionChecker.hasPartialMediaAccess()
        _state.update {
            it.copy(mediaPermissionGranted = full || partial, partialMediaAccess = partial && !full)
        }
        // First launch: explain and ask for media access once, up front.
        if (!(full || partial)) {
            viewModelScope.launch {
                val accepted = runCatching { consentDataStore.mediaDisclosureAccepted.first() }.getOrDefault(false)
                if (!accepted) _state.update { it.copy(mediaDisclosureVisible = true) }
            }
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
        if (_state.value.scanning || _state.value.cleanUpResultVisible) return
        val reclaimable = _state.value.storage?.totalReclaimableBytes
        if (reclaimable != null && reclaimable <= 0L) {
            _state.update { it.copy(nothingToClean = true) }
            return
        }
        startCleanUp()
    }

    private fun startCleanUp() {
        viewModelScope.launch {
            cleanupStartedAt = SystemClock.elapsedRealtime()
            _state.update {
                it.copy(scanning = true, scanProgress = 0f, awaitingAndroid = false)
            }
            cleanupBefore = runCatching { mediaRepository.storageStats() }.getOrNull()
            val sender = runCatching {
                mediaRepository.buildDeleteRequest(mediaRepository.trashedUris())
            }.getOrNull()
            if (sender != null) {
                _state.update {
                    it.copy(
                        awaitingAndroid = true,
                        deleteRequest = IntentSenderRequest.Builder(sender).build()
                    )
                }
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
        runCatching { settingsRepository.recordCleanup() }
        val snapshot = runCatching { settingsRepository.reviewSnapshot() }.getOrNull()
        // The ad check is repeated by the screen right before the prompt launches.
        val promptReview = snapshot?.let { ReviewPolicy.shouldAutoPrompt(it, adQuiet = true) } ?: false
        // Keep the clean-up animation on screen long enough to be seen; the numbers stay real.
        val shown = SystemClock.elapsedRealtime() - cleanupStartedAt
        if (shown < MIN_CLEANUP_MS) delay(MIN_CLEANUP_MS - shown)
        _state.update {
            it.copy(
                reviewRequested = it.reviewRequested || promptReview,
                scanning = false,
                awaitingAndroid = false,
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

    fun onNothingToCleanShown() {
        _state.update { it.copy(nothingToClean = false) }
    }

    /** [launched] true only when the prompt really started; otherwise it stays eligible next time. */
    fun onReviewHandled(launched: Boolean) {
        _state.update { it.copy(reviewRequested = false) }
        if (launched) viewModelScope.launch { runCatching { settingsRepository.markAutoPrompted() } }
    }

    /** Called when Home is idle and no ad is on screen. Shows the rate card if the rules allow it. */
    fun evaluateReviewCard(adQuiet: Boolean) {
        val s = _state.value
        if (s.reviewCardVisible || reviewCardShownThisLaunch) return
        if (!s.mediaPermissionGranted || s.scanning || s.cleanUpResultVisible || s.mediaDisclosureVisible) return
        viewModelScope.launch {
            val snapshot = runCatching { settingsRepository.reviewSnapshot() }.getOrNull() ?: return@launch
            if (ReviewPolicy.shouldShowCard(snapshot, System.currentTimeMillis(), adQuiet) && !reviewCardShownThisLaunch) {
                reviewCardShownThisLaunch = true
                runCatching { settingsRepository.markReviewCardShown() }
                _state.update { it.copy(reviewCardVisible = true) }
            }
        }
    }

    fun onReviewCardDismissed() {
        _state.update { it.copy(reviewCardVisible = false) }
        viewModelScope.launch { runCatching { settingsRepository.markReviewCardDismissed() } }
    }

    fun onReviewCardRated() {
        _state.update { it.copy(reviewCardVisible = false) }
        viewModelScope.launch { runCatching { settingsRepository.markReviewResolved() } }
    }

    fun onResultDismissed() {
        _state.update { it.copy(cleanUpResultVisible = false) }
    }

    private companion object {
        const val MIN_CLEANUP_MS = 3500L
    }
}
