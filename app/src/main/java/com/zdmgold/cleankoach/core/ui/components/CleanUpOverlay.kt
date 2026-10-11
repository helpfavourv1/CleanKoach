package com.zdmgold.cleankoach.core.ui.components

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp as colorLerp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.cleankoach.core.util.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(val angle: Float, val offset: Float, val size: Float, val warm: Boolean)

private const val TWO_PI = 2f * PI.toFloat()
private const val DEG_TO_RAD = PI.toFloat() / 180f

/**
 * Full-screen clean-up animation: a big ring that spins and fills while particles are swept
 * into it, then a burst, a check mark and the freed size counting up. The numbers shown are the
 * real result; only the pacing is padded (see the minimum duration in HomeViewModel).
 */
@Composable
fun CleanUpOverlay(
    visible: Boolean,
    done: Boolean,
    awaitingAndroid: Boolean,
    freedBytes: Long,
    caption: String,
    detail: String,
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = visible) { if (done) onDismiss() }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(300)),
        modifier = modifier.fillMaxSize()
    ) {
        OverlayContent(done, awaitingAndroid, freedBytes, caption, detail, fontFamily, onDismiss)
    }
}

@Composable
private fun OverlayContent(
    done: Boolean,
    awaitingAndroid: Boolean,
    freedBytes: Long,
    caption: String,
    detail: String,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val orange = MaterialTheme.colorScheme.primary
    val blue = Color(0xFF6FA3FF)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    // Respect Android's "remove animations": keep the ring, drop the spinning and particles.
    val motion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }

    val progress = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val burst2 = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val count = remember { Animatable(0f) }
    val bounce = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    LaunchedEffect(done, awaitingAndroid) {
        when {
            done -> progress.animateTo(1f, tween(450))
            awaitingAndroid -> progress.animateTo(0.88f, tween(600))
            else -> progress.animateTo(0.92f, tween(3200, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(done) {
        if (done) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            launch { burst.animateTo(1f, tween(900)) }
            launch {
                delay(180)
                burst2.animateTo(1f, tween(900))
            }
            launch {
                delay(250)
                check.animateTo(1f, tween(450))
            }
            launch {
                bounce.animateTo(1.06f, tween(180))
                bounce.animateTo(1f, tween(320))
            }
            count.animateTo(freedBytes.toFloat(), tween(1100))
        }
    }

    val transition = rememberInfiniteTransition(label = "cleanup")
    val spin by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )
    val flow by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "flow"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val glow by transition.animateFloat(
        initialValue = 0.10f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val particles = remember {
        val r = Random(11)
        List(64) {
            Particle(
                angle = r.nextFloat() * TWO_PI,
                offset = r.nextFloat(),
                size = 2f + r.nextFloat() * 3f,
                warm = it % 2 == 0
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2080C12))
            .pointerInput(done) { detectTapGestures { if (done) onDismiss() } }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val ringR = 122.dp.toPx()
            val stroke = 20.dp.toPx()
            val arcTopLeft = Offset(c.x - ringR, c.y - ringR)
            val arcSize = Size(ringR * 2f, ringR * 2f)

            // Soft light behind the ring so the background is not flat.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(blue.copy(alpha = 0.10f), Color.Transparent),
                    center = c,
                    radius = size.maxDimension * 0.6f
                )
            )

            scale(scale = bounce.value, pivot = c) {
                if (!done && motion) {
                    // Soft waves spreading out from the ring.
                    for (k in 0..2) {
                        val p = (pulse + k / 3f) % 1f
                        drawCircle(
                            color = blue.copy(alpha = 0.38f * (1f - p)),
                            radius = ringR + 12.dp.toPx() + ringR * 0.6f * p,
                            center = c,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                    // Dots swept into the ring from outside, each with a short streak.
                    for (pt in particles) {
                        val t = (flow + pt.offset) % 1f
                        val dist = ringR + ringR * 1.8f * (1f - t)
                        val fade = minOf(1f, t / 0.2f) * minOf(1f, (1f - t) / 0.15f)
                        val col = if (pt.warm) orange else blue
                        val x = c.x + cos(pt.angle) * dist
                        val y = c.y + sin(pt.angle) * dist
                        val tail = 10.dp.toPx()
                        drawLine(
                            color = col.copy(alpha = 0.45f * fade),
                            start = Offset(x, y),
                            end = Offset(x + cos(pt.angle) * tail, y + sin(pt.angle) * tail),
                            strokeWidth = pt.size.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = col.copy(alpha = 0.95f * fade),
                            radius = pt.size.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // Breathing glow inside the ring.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orange.copy(alpha = if (motion) glow else 0.14f), Color.Transparent),
                        center = c,
                        radius = ringR
                    ),
                    radius = ringR - stroke / 2f,
                    center = c
                )

                // Track.
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = ringR,
                    center = c,
                    style = Stroke(width = stroke)
                )

                // Filling arc with a brand gradient, rolling around the ring.
                val sweepBrush = Brush.sweepGradient(listOf(orange, blue, orange), center = c)
                rotate(degrees = if (motion) spin else 0f, pivot = c) {
                    drawArc(
                        brush = sweepBrush,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.value,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                // Bright head with a fading tail that keeps circling while it works.
                if (!done && motion) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.22f),
                        startAngle = spin - 90f - 120f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke * 0.5f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.60f),
                        startAngle = spin - 90f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = stroke * 0.5f, cap = StrokeCap.Round)
                    )
                }

                // Dial ticks around the outside that light up as the fill passes them.
                val tickIn = ringR + stroke / 2f + 6.dp.toPx()
                val tickOut = tickIn + 8.dp.toPx()
                for (i in 0 until 60) {
                    val a = (-90f + i * 6f) * DEG_TO_RAD
                    val lit = (i / 60f) < progress.value
                    drawLine(
                        color = if (lit) colorLerp(orange, blue, i / 60f).copy(alpha = 0.95f)
                        else Color.White.copy(alpha = 0.12f),
                        start = Offset(c.x + cos(a) * tickIn, c.y + sin(a) * tickIn),
                        end = Offset(c.x + cos(a) * tickOut, c.y + sin(a) * tickOut),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Three bright sparks orbiting the other way.
                if (!done && motion) {
                    val orbit = ringR + stroke / 2f + 28.dp.toPx()
                    for (k in 0..2) {
                        val a = (-spin * 1.3f + k * 120f) * DEG_TO_RAD
                        val p = Offset(c.x + cos(a) * orbit, c.y + sin(a) * orbit)
                        drawCircle(orange.copy(alpha = 0.30f), 9.dp.toPx(), p)
                        drawCircle(Color.White.copy(alpha = 0.90f), 4.dp.toPx(), p)
                    }
                }

                // Finish: two burst rings and a ring of sparks.
                val b = burst.value
                if (b > 0f && b < 1f) {
                    drawCircle(
                        color = orange.copy(alpha = 0.8f * (1f - b)),
                        radius = ringR + ringR * 0.9f * b,
                        center = c,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    for (i in 0 until 28) {
                        val a = i * (TWO_PI / 28f)
                        val r1 = ringR * (1.15f + 0.25f * b)
                        val r2 = ringR * (1.25f + 0.55f * b)
                        drawLine(
                            color = (if (i % 2 == 0) orange else blue).copy(alpha = 1f - b),
                            start = Offset(c.x + cos(a) * r1, c.y + sin(a) * r1),
                            end = Offset(c.x + cos(a) * r2, c.y + sin(a) * r2),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                val b2 = burst2.value
                if (b2 > 0f && b2 < 1f) {
                    drawCircle(
                        color = blue.copy(alpha = 0.7f * (1f - b2)),
                        radius = ringR + ringR * 0.7f * b2,
                        center = c,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        val w = size.width
                        val h = size.height
                        val p1 = Offset(w * 0.16f, h * 0.54f)
                        val p2 = Offset(w * 0.40f, h * 0.76f)
                        val p3 = Offset(w * 0.86f, h * 0.26f)
                        val a = (check.value / 0.4f).coerceIn(0f, 1f)
                        val bb = ((check.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
                        val sw = 6.dp.toPx()
                        if (a > 0f) drawLine(orange, p1, lerp(p1, p2, a), sw, StrokeCap.Round)
                        if (bb > 0f) drawLine(orange, p2, lerp(p2, p3, bb), sw, StrokeCap.Round)
                    }
                    Spacer(Modifier.height(4.dp))
                    val short = FormatUtils.bytesShort(count.value.toLong()).split(" ")
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 44.sp)) { append(short.first()) }
                            if (short.size > 1) withStyle(SpanStyle(fontSize = 20.sp)) { append(" " + short[1]) }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.W800
                        ),
                        color = Color.White
                    )
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = fontFamily,
                            fontSize = 15.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.W800,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 186.dp)
                .widthIn(max = 280.dp)
                .padding(horizontal = 16.dp)
        )
    }
}
