package com.zdmgold.cleankoach.feature.networkspeed

import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult

enum class SpeedTestPhase { READY, PREPARING, TESTING, DONE, FAILED }

data class NetworkSpeedUiState(
    val phase: SpeedTestPhase = SpeedTestPhase.READY,
    val result: SpeedTestResult? = null
)
