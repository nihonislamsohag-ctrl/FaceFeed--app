package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Custom FotoFilm / Fida Verified Badge
 * Uses the official Green Circle with White Ring & Yellow Checkmark emblem (ic_fida_verified_badge).
 */
@Composable
fun FidaVerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    badgeColor: Color = Color(0xFF159947),
    isGreenRedCheck: Boolean = false
) {
    Image(
        painter = painterResource(id = R.drawable.ic_fida_verified_badge),
        contentDescription = "Fida Verified",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}
