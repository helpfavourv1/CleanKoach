package com.zdmgold.cleankoach.feature.networkspeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.cleankoach.core.data.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkSpeedViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkSpeedUiState())
    val state: StateFlow<NetworkSpeedUiState> = _state.asStateFlow()

    fun run() {
        viewModelScope.launch {
            _state.update { it.copy(phase = SpeedTestPhase.PREPARING, error = null, result = null) }

            securityRepository.runSpeedTest()
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            phase = SpeedTestPhase.FAILED,
                            error = throwable.message ?: "Test failed."
                        )
                    }
                }
                .onCompletion {
                    if (_state.value.phase == SpeedTestPhase.TESTING) {
                        _state.update { it.copy(phase = SpeedTestPhase.DONE) }
                    }
                }
                .collect { result ->
                    _state.update {
                        it.copy(phase = SpeedTestPhase.TESTING, result = result)
                    }
                    _state.update {
                        it.copy(
                            phase = SpeedTestPhase.DONE,
                            result = result
                        )
                    }
                }
        }
    }
}
