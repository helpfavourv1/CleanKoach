package com.zdmgold.cleankoach.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observe()
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setWeeklyReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeeklyReminder(enabled)
        }
    }

    fun setStorageAlerts(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStorageAlerts(enabled)
        }
    }

    fun launchPurchase() {
        viewModelScope.launch { billingRepository.launchPurchase() }
    }

    fun restorePurchase() {
        viewModelScope.launch { billingRepository.restore() }
    }

    private fun observe() {
        viewModelScope.launch {
            settingsRepository.theme.collect { value ->
                _state.update { it.copy(theme = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.language.collect { value ->
                _state.update { it.copy(language = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.notificationsEnabled.collect { value ->
                _state.update { it.copy(notificationsEnabled = value) }
            }
        }
        viewModelScope.launch {
            billingRepository.proEntitled.collect { value ->
                _state.update { it.copy(proEntitled = value) }
            }
        }
    }
}
