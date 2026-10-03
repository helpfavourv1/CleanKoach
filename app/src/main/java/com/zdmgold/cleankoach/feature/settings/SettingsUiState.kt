package com.zdmgold.cleankoach.feature.settings

data class SettingsUiState(
    val theme: String = "system",
    val language: String = "system",
    val notificationsEnabled: Boolean = false,
    val proEntitled: Boolean = false,
    val appVersion: String = ""
)
