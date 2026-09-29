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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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

    // Auto navigate after 2.8 seconds
    LaunchedEffect(Unit) {
        isVisible = true
        delay(2800)
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
                // Tap to enter instantly
                onTimeout()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp, horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.6f))

                // Center Block: Logo, Brand Name, Welcome Bangladesh, Niyon Islam Sohag
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. App Icon Squircle (Bangladesh Green, Red Circle, White F Dove)
                    Surface(
                        modifier = Modifier
                            .size(200.dp)
                            .shadow(
                                elevation = 18.dp,
                                shape = RoundedCornerShape(46.dp),
                                spotColor = Color(0x33000000)
                            ),
                        shape = RoundedCornerShape(46.dp),
                        color = BangladeshGreen
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facefeed_logo),
                            contentDescription = "FaceFeed App Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(46.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // 2. FaceFeed App Name in bold Facebook Blue (#1877F2)
                    Text(
                        text = "FaceFeed",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = FacebookBlue,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Welcome Bangladesh 🇧🇩
                    Text(
                        text = "Welcome Bangladesh 🇧🇩",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Creator Name: Niyon Islam Sohag
                    Text(
                        text = "Niyon Islam Sohag",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF65676B),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Footer: Facebook style "from Niyon Islam Sohag" & loading bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = FacebookBlue,
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "from",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8D91)
                    )
                    Text(
                        text = "FACEFEED",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = FacebookBlue
                    )
                }
            }
        }
    }
}
