package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StorageRing(
    valueText: AnnotatedString,
    progress: Float?,
    caption: String,
    modifier: Modifier = Modifier,
    subline: String? = null,
    size: Dp = 220.dp,
    strokeWidth: Dp = 16.dp,
    valueStyle: TextStyle = MaterialTheme.typography.headlineLarge
) {
    val targetProgress = progress ?: 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600),
        label = "storage_ring_progress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokePx = strokeWidth.toPx()
                val inset = strokePx / 2f
                val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                if (progress != null && animatedProgress > 0.001f) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = valueText,
                    style = valueStyle,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (subline != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = subline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
