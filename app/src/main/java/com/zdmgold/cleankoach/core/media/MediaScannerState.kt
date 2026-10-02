package com.zdmgold.cleankoach.core.media

sealed class MediaScannerState {
    object Idle : MediaScannerState()
    data class Scanning(val progress: Float, val processed: Int, val total: Int) : MediaScannerState()
    data class Complete(val itemCount: Int, val durationMillis: Long) : MediaScannerState()
    data class Failed(val message: String) : MediaScannerState()
}
