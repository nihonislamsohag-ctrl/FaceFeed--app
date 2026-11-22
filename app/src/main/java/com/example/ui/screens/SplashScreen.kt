package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BangladeshGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Auto navigate after 2.2 seconds (tap anywhere to enter immediately)
    LaunchedEffect(Unit) {
        isVisible = true
        delay(2200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onTimeout()
            }
            .testTag("fotofilm_welcome_splash"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(350)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(450, easing = FastOutSlowInEasing)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_fotofilm_welcome_splash),
                    contentDescription = "FotoFilm Welcome Bangladesh - Niyon Islam Sohag",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.45f)
                        .padding(bottom = 14.dp)
                        .height(3.dp),
                    color = BangladeshGreen,
                    trackColor = BangladeshGreen.copy(alpha = 0.18f)
                )
            }
        }
    }
}
