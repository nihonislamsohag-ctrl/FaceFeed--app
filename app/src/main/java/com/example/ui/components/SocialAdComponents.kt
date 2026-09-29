package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SocialAd
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import kotlinx.coroutines.delay

/**
 * Social Ad Overlay for Long Videos, Reels, and Live Streams
 */
@Composable
fun SocialAdOverlay(
    ad: SocialAd,
    adSlotTitle: String, // e.g., "শুরুর সামাজিক বিজ্ঞাপন (১/৩)"
    onAdFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var countdown by remember(ad.id) { mutableStateOf(ad.durationSeconds) }
    var canSkip by remember(ad.id) { mutableStateOf(false) }

    LaunchedEffect(ad.id) {
        AppSoundManager.playSocialAdChime()
        countdown = ad.durationSeconds
        canSkip = false
        while (countdown > 0) {
            delay(1000L)
            countdown--
            if (countdown <= 2) {
                canSkip = true
            }
        }
        onAdFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
    ) {
        // Ad Banner Image Background
        AsyncImage(
            model = ad.bannerImageUrl,
            contentDescription = ad.title,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // Gradient Protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Top Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BangladeshGreen.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = adSlotTitle,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Countdown / Skip Button
            if (canSkip) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.clickable { onAdFinished() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্কিপ করুন ⏭️",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "বিজ্ঞাপন $countdown সে.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Campaign Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = BangladeshRed.copy(alpha = 0.9f)
            ) {
                Text(
                    text = ad.verifiedBadge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = ad.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = ad.organization,
                color = Color(0xFFFFD54F),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ad.description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CTA Button
            Button(
                onClick = { /* Open awareness campaign */ onAdFinished() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(ad.ctaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * Compact Ad Banner for in-feed / post display
 */
@Composable
fun SocialAdFeedCard(
    ad: SocialAd,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BangladeshGreen.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = BangladeshGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সামাজিক জনসচেতনতা বিজ্ঞাপন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BangladeshGreen
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BangladeshGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = ad.category,
                        color = BangladeshGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ad.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = ad.slogan,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = ad.bannerImageUrl,
                contentDescription = ad.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ad.organization,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { /* Action */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(ad.ctaText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
