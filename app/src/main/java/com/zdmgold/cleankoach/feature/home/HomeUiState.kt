package com.zdmgold.cleankoach.feature.home

import androidx.activity.result.IntentSenderRequest
import androidx.annotation.StringRes
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.domain.model.StorageStats

data class HomeUiState(
    val loading: Boolean = true,
    val scanning: Boolean = false,
    val scanProgress: Float = 0f,
    val storage: StorageStats? = null,
    val mediaPermissionGranted: Boolean = false,
    val partialMediaAccess: Boolean = false,
    val adVisible: Boolean = false,
    val deleteRequest: IntentSenderRequest? = null,
    val largeFilesBadge: String? = null,
    val duplicatesBadge: String? = null,
    val similarBadge: String? = null,
    val screenshotsBadge: String? = null,
    val photoOptimizerBadge: String? = null,
    val videoOptimizerBadge: String? = null,
    val cleanUpSheetVisible: Boolean = false,
    val cleanUpResultVisible: Boolean = false,
    val reviewRequested: Boolean = false,
    val lastCleanupResult: CleanUpSummary? = null,
    val mediaDisclosureVisible: Boolean = false
) {
    val cleanUpEnabled: Boolean
        get() = !scanning && (storage?.totalReclaimableBytes ?: 0L) > 0L

    val cleanUpButtonEnabled: Boolean
        get() = if (mediaPermissionGranted) cleanUpEnabled else !scanning

    @get:StringRes
    val cleanUpLabelRes: Int
        get() = when {
            scanning -> R.string.home_button_scanning
            !mediaPermissionGranted -> R.string.home_button_allow_access
            (storage?.totalReclaimableBytes ?: 0L) <= 0L -> R.string.home_button_nothing_to_clean
            else -> R.string.home_button_cleanup
        }
}

data class CleanUpSummary(
    val itemCount: Int,
    val freedBytes: Long
)
