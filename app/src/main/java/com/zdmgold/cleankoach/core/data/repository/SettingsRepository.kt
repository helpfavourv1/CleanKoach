package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.data.prefs.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {
    val theme: Flow<String> = dataStore.theme
    val language: Flow<String> = dataStore.language
    val notificationsEnabled: Flow<Boolean> = dataStore.notificationsEnabled
    val weeklyReminder: Flow<Boolean> = dataStore.weeklyReminder
    val storageAlerts: Flow<Boolean> = dataStore.storageAlerts

    suspend fun setTheme(value: String) = dataStore.setTheme(value)
    suspend fun setLanguage(value: String) = dataStore.setLanguage(value)
    suspend fun setNotificationsEnabled(value: Boolean) = dataStore.setNotificationsEnabled(value)
    suspend fun setWeeklyReminder(value: Boolean) = dataStore.setWeeklyReminder(value)
    suspend fun setStorageAlerts(value: Boolean) = dataStore.setStorageAlerts(value)
    suspend fun recordCleanupAndShouldPromptReview(): Boolean =
        dataStore.recordCleanupAndShouldPromptReview()
}
