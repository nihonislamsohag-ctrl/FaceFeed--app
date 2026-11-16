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
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Auto navigate after 1.8 seconds
    LaunchedEffect(Unit) {
        isVisible = true
        delay(1800)
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
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 36.dp, horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.7f))

                // Center Block: Logo, Brand Name, Welcome Bangladesh, Niyon Islam Sohag
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. App Icon Squircle (Bangladesh Green, Red Circle, White F Dove)
                    Surface(
                        modifier = Modifier
                            .size(92.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(22.dp),
                                spotColor = Color(0x22000000)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        color = BangladeshGreen
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_facefeed_logo),
                            contentDescription = "FaceFeed App Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. FotoFilm App Name in bold Green & Red
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Foto",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = BangladeshGreen,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Film",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = BangladeshRed,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Welcome Bangladesh 🇧🇩
                    Text(
                        text = "Welcome Bangladesh 🇧🇩",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Creator Name: Niyon Islam Sohag
                    Text(
                        text = "Niyon Islam Sohag",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF65676B),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Footer: "from FOTOFILM" & green loading bar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = BangladeshGreen,
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "from",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8D91)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FOTO",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = BangladeshGreen
                        )
                        Text(
                            text = "FILM",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = BangladeshRed
                        )
                    }
                }
            }
        }
    }
}
