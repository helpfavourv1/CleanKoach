package com.zdmgold.cleankoach.core.domain.model

data class ScanStatus(
    val done: Int = 0,
    val total: Int = 0
) {
    val fraction: Float
        get() = if (total <= 0) 0f else (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}
