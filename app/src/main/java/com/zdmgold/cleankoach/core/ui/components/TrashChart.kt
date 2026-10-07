package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * A chart panel with square corners that uses every pixel of its box. As [progress] goes from
 * 0 to 1, three layers of fill climb diagonally from the bottom-left corner to the top-right
 * corner behind rising-and-falling wave edges that keep drifting while the fill is active.
 */
@Composable
fun TrashChart(
    valueText: AnnotatedString,
    progress: Float,
    caption: String,
    modifier: Modifier = Modifier
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val accent = MaterialTheme.colorScheme.primary
    val active = progress > 0.001f

    val drift by rememberInfiniteTransition(label = "trash_drift").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "trash_drift_phase"
    )
    val phase = if (active) drift else 0f

    Box(
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(track),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val hairline = 1.dp.toPx()

            for (i in 1..3) {
                val y = h * i / 4f
                drawLine(grid, Offset(0f, y), Offset(w, y), hairline)
            }
            for (i in 1..2) {
                val x = w * i / 3f
                drawLine(grid, Offset(x, 0f), Offset(x, h), hairline)
            }

            if (!active) return@Canvas

            // Back, middle and front layers: each trails the front by a different distance,
            // with its own wave size and speed, so the fill reads as deep and thick.
            drawLayer(progress, 0.30f, 0.10f, 5f, 1.0f, phase, 0.0f, accent, 0.42f, 0.22f, 0f)
            drawLayer(progress, 0.15f, 0.08f, 7f, -1.3f, phase, 2.1f, accent, 0.70f, 0.40f, 0f)
            drawLayer(progress, 0.00f, 0.06f, 9f, 1.6f, phase, 4.2f, accent, 0.97f, 0.55f, 6.dp.toPx())
        }

        val onFill = if (progress > 0.55f) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.W700,
                color = onFill,
                maxLines = 1
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = if (progress > 0.55f) onFill else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * One diagonal fill layer. Front line: x/w + (1 - y/h) = s, which starts at the bottom-left
 * corner and passes the top-right corner at s = 2, so progress maps onto 0..2.25.
 */
private fun DrawScope.drawLayer(
    progress: Float,
    lag: Float,
    amplitude: Float,
    frequency: Float,
    speed: Float,
    phase: Float,
    offset: Float,
    accent: Color,
    topAlpha: Float,
    bottomAlpha: Float,
    edgeWidth: Float
) {
    val w = size.width
    val h = size.height
    val s = (progress * 2.25f - lag).coerceAtLeast(0f)
    if (s <= 0f) return
    val jag = min(s, 1f)
    val steps = 64
    val front = Path()
    val area = Path()
    for (i in 0..steps) {
        val u = -3f + 6f * i / steps
        val wave = amplitude * (
            sin(u * frequency + phase * speed + offset) * 0.65f +
                sin(u * frequency * 2.3f - phase * speed * 1.7f + offset) * 0.35f
            )
        val su = s + wave * jag
        val x = (su + u) / 2f * w
        val y = (1f - (su - u) / 2f) * h
        if (i == 0) {
            front.moveTo(x, y)
            area.moveTo(x, y)
        } else {
            front.lineTo(x, y)
            area.lineTo(x, y)
        }
    }
    area.lineTo(-3f * w, 4f * h)
    area.close()

    drawPath(
        path = area,
        brush = Brush.linearGradient(
            colors = listOf(accent.copy(alpha = topAlpha), accent.copy(alpha = bottomAlpha)),
            start = Offset(0f, h),
            end = Offset(w, 0f)
        )
    )
    if (edgeWidth > 0f) {
        drawPath(
            path = front,
            color = accent.copy(alpha = 0.30f),
            style = Stroke(width = edgeWidth * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = front,
            color = accent,
            style = Stroke(width = edgeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
