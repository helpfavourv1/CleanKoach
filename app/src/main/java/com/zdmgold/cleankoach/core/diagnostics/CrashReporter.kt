package com.zdmgold.cleankoach.core.diagnostics

import android.content.Context
import android.os.Build
import com.zdmgold.cleankoach.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

/**
 * Keeps the last crash and a short trail of recent app events on the device, so a crash can be
 * read inside the app (Settings > Diagnostics) without a computer. Nothing is sent anywhere.
 */
object CrashReporter {

    private const val CRASH_FILE = "last_crash.txt"
    private const val TRAIL_FILE = "event_trail.txt"
    private const val MAX_TRAIL = 60

    private val trail = ArrayDeque<String>()
    private var dir: File? = null

    fun install(context: Context) {
        val appContext = context.applicationContext
        dir = appContext.filesDir
        loadTrail()
        note("process start")
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { writeCrash(thread, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    /** Adds one line to the recent-events trail (kept across a crash or a restart). */
    @Synchronized
    fun note(message: String) {
        val line = "${stamp()}  $message"
        trail.addLast(line)
        while (trail.size > MAX_TRAIL) trail.removeFirst()
        runCatching { file(TRAIL_FILE)?.writeText(trail.joinToString("\n")) }
    }

    fun lastCrash(): String? = runCatching { file(CRASH_FILE)?.takeIf { it.exists() }?.readText() }.getOrNull()

    fun recentEvents(): String = synchronized(this) { trail.joinToString("\n") }

    fun clear() {
        runCatching { file(CRASH_FILE)?.delete() }
        synchronized(this) {
            trail.clear()
            runCatching { file(TRAIL_FILE)?.delete() }
        }
    }

    private fun writeCrash(thread: Thread, error: Throwable) {
        note("CRASH on thread ${thread.name}: ${error.javaClass.simpleName}")
        val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
        val text = buildString {
            appendLine("Time: ${stamp()}")
            appendLine("App: ${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Locale: ${Locale.getDefault().toLanguageTag()}")
            appendLine("Thread: ${thread.name}")
            appendLine()
            appendLine(trace)
        }
        file(CRASH_FILE)?.writeText(text)
    }

    private fun loadTrail() {
        runCatching {
            file(TRAIL_FILE)?.takeIf { it.exists() }?.readLines()?.takeLast(MAX_TRAIL)?.forEach { trail.addLast(it) }
        }
    }

    private fun file(name: String): File? = dir?.let { File(it, name) }

    private fun stamp(): String =
        SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
}
