package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sin

/**
 * A chart-style panel that uses its whole box. As [progress] goes from 0 to 1 the fill climbs
 * from the bottom-left corner to the top-right corner behind a rising-and-falling chart edge.
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(track),
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

            if (progress <= 0.001f) return@Canvas

            // Diagonal front: x/w + (1 - y/h) = s. It starts at the bottom-left corner and
            // passes the top-right corner at s = 2, so 2.2 leaves room for the jagged edge.
            val s = progress * 2.2f
            val jag = min(s, 1f)
            val steps = 60
            val front = Path()
            val area = Path()
            for (i in 0..steps) {
                val u = -3f + 6f * i / steps
                val wave = 0.07f * sin(u * 7f + progress * 22f) + 0.04f * sin(u * 17f - progress * 37f)
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
                    colors = listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.18f)),
                    start = Offset(0f, h),
                    end = Offset(w, 0f)
                )
            )
            drawPath(
                path = front,
                color = accent,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
