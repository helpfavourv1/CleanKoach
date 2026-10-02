package com.zdmgold.cleankoach.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val WEEKLY_REMINDER = booleanPreferencesKey("weekly_reminder")
        val STORAGE_ALERTS = booleanPreferencesKey("storage_alerts")
    }

    val theme: Flow<String> = context.settingsDataStore.data.map { it[THEME] ?: "system" }
    val language: Flow<String> = context.settingsDataStore.data.map { it[LANGUAGE] ?: "en" }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: false }
    val weeklyReminder: Flow<Boolean> = context.settingsDataStore.data.map { it[WEEKLY_REMINDER] ?: true }
    val storageAlerts: Flow<Boolean> = context.settingsDataStore.data.map { it[STORAGE_ALERTS] ?: true }

    suspend fun setTheme(value: String) { context.settingsDataStore.edit { it[THEME] = value } }
    suspend fun setLanguage(value: String) { context.settingsDataStore.edit { it[LANGUAGE] = value } }
    suspend fun setNotificationsEnabled(value: Boolean) { context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = value } }
    suspend fun setWeeklyReminder(value: Boolean) { context.settingsDataStore.edit { it[WEEKLY_REMINDER] = value } }
    suspend fun setStorageAlerts(value: Boolean) { context.settingsDataStore.edit { it[STORAGE_ALERTS] = value } }
}
