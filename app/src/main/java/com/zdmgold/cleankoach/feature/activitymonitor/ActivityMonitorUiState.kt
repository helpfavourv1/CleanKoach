package com.zdmgold.cleankoach.feature.activitymonitor

import com.zdmgold.cleankoach.core.domain.model.AppUsageItem

data class ActivityMonitorUiState(
    val loading: Boolean = true,
    val hasAccess: Boolean = false,
    val items: List<AppUsageItem> = emptyList(),
    val disclosureVisible: Boolean = false
)
