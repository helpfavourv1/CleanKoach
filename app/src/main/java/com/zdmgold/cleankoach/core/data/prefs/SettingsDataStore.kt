package com.zdmgold.cleankoach.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.zdmgold.cleankoach.core.review.ReviewSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val WEEKLY_REMINDER = booleanPreferencesKey("weekly_reminder")
        val STORAGE_ALERTS = booleanPreferencesKey("storage_alerts")
        val CLEANUPS_COMPLETED = intPreferencesKey("cleanups_completed")
        val REVIEW_PROMPTED = booleanPreferencesKey("review_prompted")
        val CLEANUP_DAYS = stringSetPreferencesKey("cleanup_days")
        val REVIEW_CARD_DISMISSES = intPreferencesKey("review_card_dismisses")
        val REVIEW_CARD_LAST_DISMISS = longPreferencesKey("review_card_last_dismiss")
        val REVIEW_CARD_SHOWN = booleanPreferencesKey("review_card_shown")
        val REVIEW_RESOLVED = booleanPreferencesKey("review_resolved")
    }

    val theme: Flow<String> = context.settingsDataStore.data.map { it[THEME] ?: "system" }
    val language: Flow<String> = context.settingsDataStore.data.map { it[LANGUAGE] ?: "system" }
    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: false }
    val weeklyReminder: Flow<Boolean> = context.settingsDataStore.data.map { it[WEEKLY_REMINDER] ?: true }
    val storageAlerts: Flow<Boolean> = context.settingsDataStore.data.map { it[STORAGE_ALERTS] ?: true }

    suspend fun setTheme(value: String) { context.settingsDataStore.edit { it[THEME] = value } }
    suspend fun setLanguage(value: String) {
        context.settingsDataStore.edit { it[LANGUAGE] = value }
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("locale", value)
            .apply()
    }
    suspend fun setNotificationsEnabled(value: Boolean) { context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = value } }
    suspend fun setWeeklyReminder(value: Boolean) { context.settingsDataStore.edit { it[WEEKLY_REMINDER] = value } }
    suspend fun setStorageAlerts(value: Boolean) { context.settingsDataStore.edit { it[STORAGE_ALERTS] = value } }

    /** Counts a finished clean-up and remembers the calendar day it happened on. */
    suspend fun recordCleanup() {
        val day = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        context.settingsDataStore.edit { prefs ->
            prefs[CLEANUPS_COMPLETED] = (prefs[CLEANUPS_COMPLETED] ?: 0) + 1
            prefs[CLEANUP_DAYS] = (prefs[CLEANUP_DAYS] ?: emptySet()) + day
        }
    }

    suspend fun reviewSnapshot(): ReviewSnapshot {
        val prefs = context.settingsDataStore.data.first()
        return ReviewSnapshot(
            cleanups = prefs[CLEANUPS_COMPLETED] ?: 0,
            cleanupDays = prefs[CLEANUP_DAYS]?.size ?: 0,
            cardDismissCount = prefs[REVIEW_CARD_DISMISSES] ?: 0,
            cardLastDismissAt = prefs[REVIEW_CARD_LAST_DISMISS] ?: 0L,
            cardEverShown = prefs[REVIEW_CARD_SHOWN] ?: false,
            resolved = prefs[REVIEW_RESOLVED] ?: false,
            autoPrompted = prefs[REVIEW_PROMPTED] ?: false
        )
    }

    suspend fun markReviewCardShown() { context.settingsDataStore.edit { it[REVIEW_CARD_SHOWN] = true } }

    suspend fun markReviewCardDismissed() {
        context.settingsDataStore.edit {
            it[REVIEW_CARD_DISMISSES] = (it[REVIEW_CARD_DISMISSES] ?: 0) + 1
            it[REVIEW_CARD_LAST_DISMISS] = System.currentTimeMillis()
        }
    }

    suspend fun markReviewResolved() { context.settingsDataStore.edit { it[REVIEW_RESOLVED] = true } }

    suspend fun markAutoPrompted() { context.settingsDataStore.edit { it[REVIEW_PROMPTED] = true } }
}
