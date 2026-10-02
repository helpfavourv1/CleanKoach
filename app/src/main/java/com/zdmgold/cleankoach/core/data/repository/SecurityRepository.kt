package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult
import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun inspectWifi(): WifiSecurityReport
    fun runSpeedTest(): Flow<SpeedTestResult>
}
