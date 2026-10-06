package com.zdmgold.cleankoach.feature.home

import androidx.activity.result.IntentSenderRequest
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
    val awaitingAndroid: Boolean = false,
    val cleanUpResultVisible: Boolean = false,
    val reviewRequested: Boolean = false,
    val nothingToClean: Boolean = false,
    val lastCleanupResult: CleanUpSummary? = null,
    val mediaDisclosureVisible: Boolean = false
)

data class CleanUpSummary(
    val itemCount: Int,
    val freedBytes: Long
)
