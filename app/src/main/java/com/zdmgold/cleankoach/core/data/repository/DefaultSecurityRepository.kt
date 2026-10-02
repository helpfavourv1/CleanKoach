package com.zdmgold.cleankoach.core.data.repository

import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult
import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport
import com.zdmgold.cleankoach.core.system.NetworkSpeedTester
import com.zdmgold.cleankoach.core.system.WifiSecurityInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSecurityRepository @Inject constructor(
    private val inspector: WifiSecurityInspector,
    private val speedTester: NetworkSpeedTester
) : SecurityRepository {

    override suspend fun inspectWifi(): WifiSecurityReport = withContext(Dispatchers.IO) {
        inspector.inspect()
    }

    override fun runSpeedTest(): Flow<SpeedTestResult> = speedTester.run()
}
