package com.zdmgold.cleankoach.core.ui.components

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A chart panel with a square body and one rounded top-left corner that uses every pixel of its box.
 *
 * At rest the whole bottom of the box swells upward in a wave across the full width (5% to 75% of the box height),
 * and falls back in a slow loop: 3 s up, 3 s down, 18 s pause at the bottom, at 40% of the clean-up strength.
 * While a clean-up runs ([progress] above 0) the same waves take over at full strength: four deeper
 * layers, taller crests and a thick glowing front edge climb from the bottom to the top.
 * With Android's "remove animations" on, the box stays still.
 */
@Composable
fun TrashChart(
    valueText: AnnotatedString,
    progress: Float,
    caption: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val motion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val accent = MaterialTheme.colorScheme.primary
    val active = progress > 0.001f

    val transition = rememberInfiniteTransition(label = "trash_chart")
    // Whole cycles per loop only (see the integer speeds below) so the wave never jumps on restart.
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "trash_drift"
    )
    // 3 s rise, 3 s fall, then an 18 s pause at the bottom: one loop every 24 s.
    val swell by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 24000
                0f at 0 using FastOutSlowInEasing
                1f at 3000 using FastOutSlowInEasing
                0f at 6000
                0f at 24000
            },
            RepeatMode.Restart
        ),
        label = "trash_swell"
    )
    // 0 = resting loop, 1 = clean-up; eased so the hand-over never jumps.
    val mix by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(450),
        label = "trash_mix"
    )

    val phase = if (motion) drift else 0f
    val idleLevel = if (motion) 0.05f + 0.70f * swell else 0.45f
    val level = idleLevel * (1f - mix) + progress * mix
    val strength = 0.40f + 0.60f * mix

    val onFillCleaning = mix > 0.5f && progress > 0.55f
    val textColor = if (onFillCleaning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val captionColor = if (onFillCleaning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val glow = Shadow(color = track.copy(alpha = 0.85f), offset = Offset.Zero, blurRadius = 10f)

    Box(
        modifier = modifier.clip(AbsoluteRoundedCornerShape(topLeft = 6.dp)).background(track),
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

            val edge = (2f + 6f * mix).dp.toPx()
            val crest = 0.75f + 1.15f * mix
            // lag, crest height (share of box height), crests across the width, speed (whole number), offset,
            // top alpha, bottom alpha, edge width
            drawWave(level, phase, 0.34f, 0.050f * crest, 1.0f, 1, 0.0f, accent, 0.36f * strength, 0.18f * strength, 0f)
            drawWave(level, phase, 0.22f, 0.060f * crest, 1.5f, -1, 1.3f, accent, 0.55f * strength, 0.30f * strength, 0f)
            drawWave(level, phase, 0.11f, 0.070f * crest, 2.0f, 2, 2.6f, accent, 0.80f * strength, 0.42f * strength, 0f)
            drawWave(level, phase, 0.00f, 0.080f * crest, 2.5f, -2, 3.9f, accent, 0.97f * strength, 0.55f * strength, edge)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineLarge.copy(shadow = glow),
                fontWeight = FontWeight.W800,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall.copy(shadow = glow),
                fontWeight = FontWeight.W700,
                color = captionColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * One full-width wave layer. [level] 0..1 is how high the front stands; the front reaches past the top
 * at 1 so the box fills completely. [lag] drops this layer below the front layer.
 */
private fun DrawScope.drawWave(
    level: Float,
    phase: Float,
    lag: Float,
    crestHeight: Float,
    crests: Float,
    speed: Int,
    offset: Float,
    accent: Color,
    topAlpha: Float,
    bottomAlpha: Float,
    edgeWidth: Float
) {
    val w = size.width
    val h = size.height
    val base = level * 1.30f - 0.08f - lag
    val steps = 48
    val front = Path()
    val area = Path()
    val twoPi = (2 * PI).toFloat()
    for (i in 0..steps) {
        val t = i.toFloat() / steps
        val wave = sin(t * crests * twoPi + (phase * speed) + offset) * 0.65f +
            sin(t * crests * 2f * twoPi - (phase * speed) * 2f + offset) * 0.35f
        val y = (1f - (base + wave * crestHeight)) * h
        val x = t * w
        if (i == 0) {
            front.moveTo(x, y)
            area.moveTo(x, y)
        } else {
            front.lineTo(x, y)
            area.lineTo(x, y)
        }
    }
    area.lineTo(w, h * 2f)
    area.lineTo(0f, h * 2f)
    area.close()
    drawPath(
        path = area,
        brush = Brush.verticalGradient(
            colors = listOf(accent.copy(alpha = topAlpha), accent.copy(alpha = bottomAlpha)),
            startY = 0f,
            endY = h
        )
    )
    if (edgeWidth > 0f) {
        drawPath(front, accent.copy(alpha = 0.30f), style = Stroke(edgeWidth * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(front, accent, style = Stroke(edgeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
