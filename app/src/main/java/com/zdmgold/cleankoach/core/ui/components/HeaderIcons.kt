package com.zdmgold.cleankoach.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .semantics { contentDescription = description }
        ) {
            content()
        }
    }
}

@Composable
fun LanguageIconButton(
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    val tint = MaterialTheme.colorScheme.onBackground
    HeaderIconButton(onClick, description, modifier) {
        Canvas(modifier = Modifier.size(22.dp)) { drawGlobe(tint) }
    }
}

@Composable
fun ThemeIconButton(
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    val tint = MaterialTheme.colorScheme.onBackground
    HeaderIconButton(onClick, description, modifier) {
        Canvas(modifier = Modifier.size(22.dp)) { drawHalfMoon(tint) }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlobe(color: Color) {
    val stroke = Stroke(width = 2.dp.toPx())
    val inset = stroke.width / 2f
    val d = size.minDimension - stroke.width
    drawCircle(color = color, radius = d / 2f, center = center, style = stroke)
    drawLine(color, Offset(inset, center.y), Offset(size.width - inset, center.y), stroke.width)
    drawOval(
        color = color,
        topLeft = Offset(center.x - d * 0.2f, inset),
        size = Size(d * 0.4f, d),
        style = stroke
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHalfMoon(color: Color) {
    val stroke = Stroke(width = 2.dp.toPx())
    val d = size.minDimension - stroke.width
    drawCircle(color = color, radius = d / 2f, center = center, style = stroke)
    drawArc(
        color = color,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(stroke.width / 2f, stroke.width / 2f),
        size = Size(d, d)
    )
}
