package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Facebook Official Style Blue Verified Badge
 * Vibrant Blue Scalloped Rosette (#1877F2) with crisp pure White checkmark inside.
 */
@Composable
fun FidaVerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    badgeColor: Color = Color(0xFF1877F2),
    isGreenRedCheck: Boolean = false
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerRadius = minOf(w, h) / 2f
        val innerRadius = outerRadius * 0.86f

        // Draw 32 vertices (16 outer scalloped peaks, 16 inner valleys)
        val numPoints = 32
        val path = Path()
        for (i in 0 until numPoints) {
            val angle = (i.toFloat() / numPoints) * 2f * Math.PI.toFloat() - (Math.PI.toFloat() / 2f)
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        // 1. Draw Vibrant Facebook Blue Scalloped Rosette (#1877F2)
        drawPath(
            path = path,
            color = badgeColor
        )

        // Coordinates for crisp white checkmark inside the blue badge
        val startX = cx - outerRadius * 0.36f
        val startY = cy - outerRadius * 0.01f
        val midX = cx - outerRadius * 0.09f
        val midY = cy + outerRadius * 0.28f
        val endX = cx + outerRadius * 0.38f
        val endY = cy - outerRadius * 0.28f

        val strokeWidth = outerRadius * 0.24f

        val checkPath = Path().apply {
            moveTo(startX, startY)
            lineTo(midX, midY)
            lineTo(endX, endY)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
