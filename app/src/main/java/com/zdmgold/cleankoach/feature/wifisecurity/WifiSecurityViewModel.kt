package com.zdmgold.cleankoach.feature.wifisecurity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiSecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WifiSecurityUiState())
    val state: StateFlow<WifiSecurityUiState> = _state.asStateFlow()

    init {
        runCheck()
    }

    fun runCheck() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val report = runCatching { securityRepository.inspectWifi() }.getOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    report = report,
                    error = if (report == null) "Could not inspect this connection." else null
                )
            }
        }
    }
}
