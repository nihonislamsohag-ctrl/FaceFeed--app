package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Auto navigate after 2.5 seconds
    LaunchedEffect(Unit) {
        isVisible = true
        delay(2500)
        onTimeout()
    }

    // Infinite pulsing animation for subtle modern feel
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap to enter instantly
                onTimeout()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                initialScale = 0.88f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // 1. App Icon with curved rounded corners & soft elevation shadow
                Surface(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(44.dp),
                            spotColor = Color.Black.copy(alpha = 0.22f)
                        ),
                    shape = RoundedCornerShape(44.dp),
                    color = BangladeshGreen
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.facefeed_logo_1789791030932),
                        contentDescription = "FaceFeed Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(44.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(42.dp))

                // 2. FaceFeed App Name in bold Facebook Blue (#1877F2)
                Text(
                    text = "FaceFeed",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    color = FacebookBlue,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 3. Welcome Bangladesh 🇧🇩
                Text(
                    text = "Welcome Bangladesh 🇧🇩",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Creator Name: Niyon Islam Sohag
                Text(
                    text = "Niyon Islam Sohag",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF757575)
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
