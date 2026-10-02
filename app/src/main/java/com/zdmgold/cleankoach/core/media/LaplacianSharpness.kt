package com.zdmgold.cleankoach.core.media

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class LaplacianSharpness @Inject constructor() {

    fun compute(bitmap: Bitmap): Float {
        val downscaled = if (bitmap.width > 512 || bitmap.height > 512) {
            val scale = 512f / maxOf(bitmap.width, bitmap.height).toFloat()
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }

        val w = downscaled.width
        val h = downscaled.height
        if (w < 3 || h < 3) return 0f

        val pixels = IntArray(w * h)
        downscaled.getPixels(pixels, 0, w, 0, 0, w, h)

        val gray = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            gray[i] = 0.299f * r + 0.587f * g + 0.114f * b
        }

        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        val kernel = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, -4, 1),
            intArrayOf(0, 1, 0)
        )

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var acc = 0f
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        acc += gray[(y + ky) * w + (x + kx)] * kernel[ky + 1][kx + 1]
                    }
                }
                sum += acc
                sumSq += acc * acc
                count++
            }
        }

        if (downscaled !== bitmap) downscaled.recycle()
        if (count == 0) return 0f
        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return sqrt(variance.coerceAtLeast(0.0)).toFloat()
    }
}
