package com.zdmgold.cleankoach.feature.settings

data class SettingsUiState(
    val theme: String = "system",
    val language: String = "system",
    val notificationsEnabled: Boolean = false,
    val weeklyReminder: Boolean = true,
    val storageAlerts: Boolean = true,
    val proEntitled: Boolean = false,
    val proPrice: String? = null,
    val proPending: Boolean = false,
    val proUnavailable: Boolean = false
)
