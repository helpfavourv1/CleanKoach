package com.zdmgold.cleankoach.core.media

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.scale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DHashCalculator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun dhash(uri: Uri): Long? = runCatching {
        val source = context.contentResolver.openInputStream(uri)?.use { input ->
            android.graphics.BitmapFactory.decodeStream(input)
        } ?: return null

        val small = source.scale(9, 8, true)
        val pixels = IntArray(9 * 8)
        small.getPixels(pixels, 0, 9, 0, 0, 9, 8)

        var hash = 0L
        var bit = 0
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val left = luminance(pixels[row * 9 + col])
                val right = luminance(pixels[row * 9 + col + 1])
                if (left > right) {
                    hash = hash or (1L shl bit)
                }
                bit++
            }
        }
        if (small !== source) source.recycle()
        small.recycle()
        hash
    }.getOrNull()

    private fun luminance(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)
}
