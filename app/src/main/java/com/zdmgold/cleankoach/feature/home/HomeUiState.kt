package com.zdmgold.cleankoach.feature.home

import com.zdmgold.cleankoach.core.domain.model.StorageStats

data class HomeUiState(
    val loading: Boolean = true,
    val scanning: Boolean = false,
    val scanProgress: Float = 0f,
    val storage: StorageStats? = null,
    val mediaPermissionGranted: Boolean = false,
    val partialMediaAccess: Boolean = false,
    val adVisible: Boolean = true,
    val largeFilesBadge: String? = null,
    val duplicatesBadge: String? = null,
    val similarBadge: String? = null,
    val screenshotsBadge: String? = null,
    val photoOptimizerBadge: String? = null,
    val videoOptimizerBadge: String? = null,
    val cleanUpSheetVisible: Boolean = false,
    val cleanUpResultVisible: Boolean = false,
    val lastCleanupResult: CleanUpSummary? = null,
    val mediaDisclosureVisible: Boolean = false
) {
    val cleanUpEnabled: Boolean
        get() = !scanning && (storage?.totalReclaimableBytes ?: 0L) > 0L

    val cleanUpLabel: String
        get() = when {
            scanning -> "Scanning…"
            !mediaPermissionGranted -> "Allow access"
            (storage?.totalReclaimableBytes ?: 0L) <= 0L -> "Nothing to clean"
            else -> "CLEAN UP"
        }
}

data class CleanUpSummary(
    val itemCount: Int,
    val freedBytes: Long
)
