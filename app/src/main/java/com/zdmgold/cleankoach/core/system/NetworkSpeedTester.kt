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

    companion object {
        const val TEST_BYTES = 25_000_000L
        const val MAX_DURATION_MS = 15_000L
        private const val SAMPLE_INTERVAL_MS = 250L
    }

    private val testUrl = "https://speed.cloudflare.com/__down?bytes=$TEST_BYTES"

    /**
     * Emits a live sample every quarter second (inProgress = true), then one final result
     * averaged over the whole transfer. Throws IOException when no data arrives at all.
     */
    fun run(): Flow<SpeedTestResult> = flow {
        val start = System.currentTimeMillis()
        var bytesRead = 0L
        var lastSampleAt = start
        var lastSampleBytes = 0L
        var live = 0.0

        try {
            val conn = URL(testUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.requestMethod = "GET"
            conn.connect()

            conn.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                val deadline = start + MAX_DURATION_MS
                while (System.currentTimeMillis() < deadline) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    bytesRead += read
                    val now = System.currentTimeMillis()
                    if (now - lastSampleAt >= SAMPLE_INTERVAL_MS) {
                        val instant = ((bytesRead - lastSampleBytes) * 8.0) /
                            ((now - lastSampleAt) / 1000.0) / 1_000_000.0
                        live = if (live == 0.0) instant else live * 0.7 + instant * 0.3
                        emit(
                            SpeedTestResult(
                                downloadMbps = live,
                                bytesTransferred = bytesRead,
                                durationMillis = now - start,
                                completedAt = now,
                                inProgress = true
                            )
                        )
                        lastSampleAt = now
                        lastSampleBytes = bytesRead
                    }
                }
            }
            conn.disconnect()
        } catch (_: IOException) {
            // Handled below: a transfer that produced no bytes is reported as a failure.
        }

        if (bytesRead == 0L) throw IOException("No data received")

        val elapsedMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        val mbps = (bytesRead * 8.0) / (elapsedMs / 1000.0) / 1_000_000.0

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
