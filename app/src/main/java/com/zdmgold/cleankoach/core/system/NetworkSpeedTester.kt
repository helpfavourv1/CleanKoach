package com.zdmgold.cleankoach.core.system

import com.zdmgold.cleankoach.core.domain.model.SpeedTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkSpeedTester @Inject constructor() {

    private val testUrl = "https://speed.cloudflare.com/__down?bytes=25000000"

    fun run(): Flow<SpeedTestResult> = flow {
        val start = System.currentTimeMillis()
        var bytesRead = 0L

        runCatching {
            val url = URL(testUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.requestMethod = "GET"
            conn.connect()

            conn.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                var read = input.read(buffer)
                val deadline = start + 15_000L
                while (read > 0 && System.currentTimeMillis() < deadline) {
                    bytesRead += read
                    read = input.read(buffer)
                }
            }
            conn.disconnect()
        }

        if (bytesRead == 0L) throw IOException("No data received")

        val elapsedMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        val mbps = if (bytesRead > 0) {
            (bytesRead * 8.0) / (elapsedMs / 1000.0) / 1_000_000.0
        } else {
            0.0
        }

        emit(
            SpeedTestResult(
                downloadMbps = mbps,
                bytesTransferred = bytesRead,
                durationMillis = elapsedMs,
                completedAt = System.currentTimeMillis()
            )
        )
    }.flowOn(Dispatchers.IO)
}
