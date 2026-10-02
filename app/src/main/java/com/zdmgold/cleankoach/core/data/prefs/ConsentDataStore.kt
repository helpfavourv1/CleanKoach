package com.zdmgold.cleankoach.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.consentDataStore: DataStore<Preferences> by preferencesDataStore(name = "consent")

class ConsentDataStore(private val context: Context) {

    companion object {
        val UMP_RESOLVED = booleanPreferencesKey("ump_resolved")
        val UMP_CAN_REQUEST_ADS = booleanPreferencesKey("ump_can_request_ads")
        val LAST_UPDATED = longPreferencesKey("last_updated")
        val MEDIA_DISCLOSURE_ACCEPTED = booleanPreferencesKey("media_disclosure_accepted")
        val USAGE_DISCLOSURE_ACCEPTED = booleanPreferencesKey("usage_disclosure_accepted")
    }

    val umpResolved: Flow<Boolean> = context.consentDataStore.data.map { it[UMP_RESOLVED] ?: false }
    val umpCanRequestAds: Flow<Boolean> = context.consentDataStore.data.map { it[UMP_CAN_REQUEST_ADS] ?: false }
    val mediaDisclosureAccepted: Flow<Boolean> = context.consentDataStore.data.map { it[MEDIA_DISCLOSURE_ACCEPTED] ?: false }
    val usageDisclosureAccepted: Flow<Boolean> = context.consentDataStore.data.map { it[USAGE_DISCLOSURE_ACCEPTED] ?: false }

    suspend fun setUmpResolved(value: Boolean) { context.consentDataStore.edit { it[UMP_RESOLVED] = value } }
    suspend fun setUmpCanRequestAds(value: Boolean) { context.consentDataStore.edit { it[UMP_CAN_REQUEST_ADS] = value } }
    suspend fun setMediaDisclosureAccepted(value: Boolean) { context.consentDataStore.edit { it[MEDIA_DISCLOSURE_ACCEPTED] = value } }
    suspend fun setUsageDisclosureAccepted(value: Boolean) { context.consentDataStore.edit { it[USAGE_DISCLOSURE_ACCEPTED] = value } }
    suspend fun touchUpdatedAt() { context.consentDataStore.edit { it[LAST_UPDATED] = System.currentTimeMillis() } }
}
