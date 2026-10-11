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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
    // Respect Android's "remove animations": keep the ring, drop the spinning and particles.
    val motion = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }

    val progress = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val count = remember { Animatable(0f) }

    LaunchedEffect(done, awaitingAndroid) {
        when {
            done -> progress.animateTo(1f, tween(450))
            awaitingAndroid -> progress.animateTo(0.88f, tween(600))
            else -> progress.animateTo(0.92f, tween(3200, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(done) {
        if (done) {
            launch { burst.animateTo(1f, tween(900)) }
            launch {
                delay(250)
                check.animateTo(1f, tween(450))
            }
            count.animateTo(freedBytes.toFloat(), tween(1100))
        }
    }

    val transition = rememberInfiniteTransition(label = "cleanup")
    val spin by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "spin"
    )
    val flow by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "flow"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    val particles = remember {
        val r = Random(11)
        List(28) {
            Particle(
                angle = r.nextFloat() * (2f * PI.toFloat()),
                offset = r.nextFloat(),
                size = 1.6f + r.nextFloat() * 2.2f,
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
            val ringR = 110.dp.toPx()
            val stroke = 14.dp.toPx()
            val arcTopLeft = Offset(c.x - ringR, c.y - ringR)
            val arcSize = Size(ringR * 2f, ringR * 2f)

            if (!done && motion) {
                // Soft waves spreading out from the ring.
                for (k in 0..1) {
                    val p = (pulse + k * 0.5f) % 1f
                    drawCircle(
                        color = blue.copy(alpha = 0.30f * (1f - p)),
                        radius = ringR + 10.dp.toPx() + ringR * 0.5f * p,
                        center = c,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                // Dots swept into the ring from outside.
                for (pt in particles) {
                    val t = (flow + pt.offset) % 1f
                    val dist = ringR + ringR * 1.5f * (1f - t)
                    val fade = minOf(1f, t / 0.2f) * minOf(1f, (1f - t) / 0.15f)
                    drawCircle(
                        color = (if (pt.warm) orange else blue).copy(alpha = 0.9f * fade),
                        radius = pt.size.dp.toPx(),
                        center = Offset(c.x + cos(pt.angle) * dist, c.y + sin(pt.angle) * dist)
                    )
                }
            }

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

            // Bright head that keeps circling while it works.
            if (!done && motion) {
                drawArc(
                    color = Color.White.copy(alpha = 0.55f),
                    startAngle = spin - 90f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke * 0.45f, cap = StrokeCap.Round)
                )
            }

            // Finish burst.
            val b = burst.value
            if (b > 0f && b < 1f) {
                drawCircle(
                    color = orange.copy(alpha = 0.8f * (1f - b)),
                    radius = ringR + ringR * 0.9f * b,
                    center = c,
                    style = Stroke(width = 4.dp.toPx())
                )
                for (i in 0 until 16) {
                    val a = i * (2f * PI.toFloat() / 16f)
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
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        val w = size.width
                        val h = size.height
                        val p1 = Offset(w * 0.16f, h * 0.54f)
                        val p2 = Offset(w * 0.40f, h * 0.76f)
                        val p3 = Offset(w * 0.86f, h * 0.26f)
                        val a = (check.value / 0.4f).coerceIn(0f, 1f)
                        val bb = ((check.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
                        val sw = 5.dp.toPx()
                        if (a > 0f) drawLine(orange, p1, lerp(p1, p2, a), sw, StrokeCap.Round)
                        if (bb > 0f) drawLine(orange, p2, lerp(p2, p3, bb), sw, StrokeCap.Round)
                    }
                    Spacer(Modifier.height(4.dp))
                    val short = FormatUtils.bytesShort(count.value.toLong()).split(" ")
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 38.sp)) { append(short.first()) }
                            if (short.size > 1) withStyle(SpanStyle(fontSize = 18.sp)) { append(" " + short[1]) }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.W800
                        ),
                        color = Color.White
                    )
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = fontFamily),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.W800
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
                .offset(y = 168.dp)
                .widthIn(max = 280.dp)
                .padding(horizontal = 16.dp)
        )
    }
}
