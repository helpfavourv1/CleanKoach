package com.zdmgold.cleankoach.feature.activitymonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.prefs.ConsentDataStore
import com.zdmgold.cleankoach.core.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityMonitorViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val consentDataStore: ConsentDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityMonitorUiState())
    val state: StateFlow<ActivityMonitorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accepted = runCatching {
                consentDataStore.usageDisclosureAccepted.first()
            }.getOrDefault(false)

            val hasAccess = usageRepository.hasUsageAccess()

            if (!hasAccess && !accepted) {
                _state.update {
                    it.copy(loading = false, hasAccess = false, disclosureVisible = true)
                }
            } else {
                _state.update { it.copy(hasAccess = hasAccess) }
                if (hasAccess) load()
                else _state.update { it.copy(loading = false) }
            }
        }
    }

    fun onDisclosureAccepted() {
        viewModelScope.launch {
            consentDataStore.setUsageDisclosureAccepted(true)
            _state.update { it.copy(disclosureVisible = false) }
        }
    }

    fun onUsageAccessRequested(open: () -> Unit) {
        viewModelScope.launch {
            val accepted = runCatching { consentDataStore.usageDisclosureAccepted.first() }.getOrDefault(false)
            if (accepted) open() else _state.update { it.copy(disclosureVisible = true) }
        }
    }

    fun onDisclosureDismissed() {
        _state.update { it.copy(disclosureVisible = false, loading = false) }
    }

    fun refresh() {
        if (usageRepository.hasUsageAccess()) load()
        else _state.update { it.copy(loading = false, hasAccess = false) }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val items = runCatching { usageRepository.last24Hours() }.getOrDefault(emptyList())
            _state.update {
                it.copy(loading = false, hasAccess = true, items = items)
            }
        }
    }
}
