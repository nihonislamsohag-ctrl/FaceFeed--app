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
 * Facebook Official Style Blue Scalloped Verified Badge ("নিলা রংগের গোলে রেগ রেগ থাকবে ফেসবুক ঐ করে দেন বিতরে সাদা টিক থাকবে")
 * Features:
 * - 16-point scalloped / serrated rosette ridges ("গোলে রেগ রেগ")
 * - Vibrant Facebook Blue (#1877F2)
 * - Clean crisp white checkmark in the center
 */
@Composable
fun FidaVerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
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

        // Draw Facebook Blue Scalloped Rosette
        drawPath(
            path = path,
            color = Color(0xFF1877F2)
        )

        // Draw white checkmark inside
        val checkPath = Path().apply {
            moveTo(cx - outerRadius * 0.40f, cy - outerRadius * 0.02f)
            lineTo(cx - outerRadius * 0.10f, cy + outerRadius * 0.32f)
            lineTo(cx + outerRadius * 0.42f, cy - outerRadius * 0.32f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = outerRadius * 0.28f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
