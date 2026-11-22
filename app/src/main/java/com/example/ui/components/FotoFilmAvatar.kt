package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Clean Material 3 avatar component that renders a real image URL when present,
 * or a crisp initial/icon badge when a user has not uploaded a photo yet.
 */
@Composable
fun FotoFilmAvatar(
    imageUrl: String?,
    name: String = "",
    modifier: Modifier = Modifier,
    contentDescription: String? = name.ifBlank { "Profile" },
    isGroup: Boolean = false,
    fallbackIcon: ImageVector = if (isGroup) Icons.Default.Groups else Icons.Default.Person,
    iconSize: Dp = 20.dp
) {
    val cleanUrl = imageUrl?.trim().orEmpty()
    if (cleanUrl.isNotBlank()) {
        AsyncImage(
            model = cleanUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNotBlank() && !isGroup && initial[0].isLetterOrDigit()) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = (iconSize.value * 0.72f).sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun FotoFilmCoverBackground(
    imageUrl: String?,
    contentDescription: String? = "Cover photo",
    modifier: Modifier = Modifier
) {
    val cleanUrl = imageUrl?.trim().orEmpty()
    if (cleanUrl.isNotBlank()) {
        AsyncImage(
            model = cleanUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF006A4E).copy(alpha = 0.28f),
                        Color(0xFF1877F2).copy(alpha = 0.22f)
                    )
                )
            )
        )
    }
}
