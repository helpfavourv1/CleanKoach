package com.zdmgold.cleankoach.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.BillingRepository
import com.zdmgold.cleankoach.core.data.repository.SettingsRepository
import com.zdmgold.cleankoach.core.locale.LocaleManager
import com.zdmgold.cleankoach.core.system.NotificationScheduler
import kotlinx.coroutines.flow.first
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
    private val billingRepository: BillingRepository,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observe()
        refreshBilling()
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            LocaleManager.apply(language)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            reconcileNotifications()
        }
    }

    fun setWeeklyReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWeeklyReminder(enabled)
            reconcileNotifications()
        }
    }

    fun setStorageAlerts(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStorageAlerts(enabled)
            reconcileNotifications()
        }
    }

    private suspend fun reconcileNotifications() {
        scheduler.reconcile(
            master = settingsRepository.notificationsEnabled.first(),
            weekly = settingsRepository.weeklyReminder.first(),
            storage = settingsRepository.storageAlerts.first()
        )
    }

    fun launchPurchase(activity: Activity) {
        viewModelScope.launch {
            val opened = billingRepository.launchPurchase(activity)
            _state.update { it.copy(proUnavailable = !opened) }
        }
    }

    fun refreshBilling() {
        viewModelScope.launch { billingRepository.refresh() }
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
            settingsRepository.weeklyReminder.collect { value ->
                _state.update { it.copy(weeklyReminder = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.storageAlerts.collect { value ->
                _state.update { it.copy(storageAlerts = value) }
            }
        }
        viewModelScope.launch {
            billingRepository.proEntitled.collect { value ->
                _state.update { it.copy(proEntitled = value) }
            }
        }
        viewModelScope.launch {
            billingRepository.proPrice.collect { value ->
                _state.update { it.copy(proPrice = value, proUnavailable = false) }
            }
        }
        viewModelScope.launch {
            billingRepository.purchasePending.collect { value ->
                _state.update { it.copy(proPending = value) }
            }
        }
    }
}
