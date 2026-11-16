package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fida AI (ফিদা এআই) flower logo matching IMG_20261017_041542.jpg
 * 8 swirling oval petals with purple, magenta, and indigo gradients.
 */
@Composable
fun FidaAILogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FidaAILogoRotation")
    val rotationAngle by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "FidaAILogoAngle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val petalColors = remember {
        listOf(
            Pair(Color(0xFFE040FB), Color(0xFFD500F9)), // Magenta
            Pair(Color(0xFFBA68C8), Color(0xFF9C27B0)), // Violet/Purple
            Pair(Color(0xFF9575CD), Color(0xFF7E57C2)), // Deep Purple
            Pair(Color(0xFF7986CB), Color(0xFF5C6BC0)), // Indigo
            Pair(Color(0xFF5C6BC0), Color(0xFF3F51B5)), // Blue/Indigo
            Pair(Color(0xFF7E57C2), Color(0xFF673AB7)), // Royal Purple
            Pair(Color(0xFF9C27B0), Color(0xFF8E24AA)), // Rich Violet
            Pair(Color(0xFFCE93D8), Color(0xFFBA68C8))  // Light Magenta
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(0.5.dp, Color(0xFFE1BEE7), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.12f)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f
            val petalWidth = baseRadius * 0.38f
            val petalHeight = baseRadius * 0.72f
            val orbitDistance = baseRadius * 0.44f

            rotate(degrees = rotationAngle, pivot = center) {
                for (i in 0 until 8) {
                    val angleDeg = i * 45f - 20f
                    val (colorStart, colorEnd) = petalColors[i]

                    rotate(degrees = angleDeg, pivot = center) {
                        // Offset petal from center along the Y axis
                        val petalTopLeft = Offset(
                            x = center.x - petalWidth / 2f,
                            y = center.y - orbitDistance - petalHeight / 2f
                        )

                        // Rotate petal slightly to create the gentle swirling effect
                        rotate(degrees = 15f, pivot = Offset(center.x, center.y - orbitDistance)) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(colorStart, colorEnd),
                                    startY = petalTopLeft.y,
                                    endY = petalTopLeft.y + petalHeight
                                ),
                                topLeft = petalTopLeft,
                                size = Size(petalWidth, petalHeight),
                                cornerRadius = CornerRadius(petalWidth / 2f, petalWidth / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
