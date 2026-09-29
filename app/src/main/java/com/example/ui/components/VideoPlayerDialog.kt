package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.ReactionType
import com.example.model.SocialAd
import com.example.data.SampleData
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    title: String,
    authorName: String,
    post: Post? = null,
    onDismiss: () -> Unit
) {
    val isShortVideo = post?.isReel == true
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(0) }
    val totalSeconds = if (isShortVideo) 20 else 45 // Simulated video duration
    var showControls by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(post?.totalReactions ?: 1250) }
    var selectedQuality by remember { mutableStateOf("1080p HD") }

    // Social Ad state (Long video: Start 1, Middle 1, End 1. Short video: Start 1, End 1)
    var activeAd by remember { mutableStateOf<SocialAd?>(null) }
    var dismissedAdSlots by remember { mutableStateOf(setOf<String>()) }

    // Playback loop and social ad triggers
    LaunchedEffect(isPlaying, currentSeconds) {
        if (!isShortVideo) {
            // Long Video: শুরুতে ১টি (0-4s), মাঝে ১টি (22-26s), শেষে ১টি (41-45s)
            if (currentSeconds in 0..4 && "start" !in dismissedAdSlots) {
                activeAd = SampleData.sampleSocialAds.getOrNull(0)
            } else if (currentSeconds in 22..26 && "mid" !in dismissedAdSlots) {
                activeAd = SampleData.sampleSocialAds.getOrNull(1)
            } else if (currentSeconds in 41..45 && "end" !in dismissedAdSlots) {
                activeAd = SampleData.sampleSocialAds.getOrNull(2)
            } else if (activeAd != null && currentSeconds !in listOf(0..4, 22..26, 41..45).flatten()) {
                activeAd = null
            }
        } else {
            // Short Video: শুরুতে ১টি (0-3s), শেষে ১টি (16-20s)
            if (currentSeconds in 0..3 && "short_start" !in dismissedAdSlots) {
                activeAd = SampleData.sampleSocialAds.getOrNull(3)
            } else if (currentSeconds in 16..20 && "short_end" !in dismissedAdSlots) {
                activeAd = SampleData.sampleSocialAds.getOrNull(4)
            } else if (activeAd != null && currentSeconds !in listOf(0..3, 16..20).flatten()) {
                activeAd = null
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentSeconds < totalSeconds) {
                currentSeconds++
            } else {
                currentSeconds = 0 // Loop video
                dismissedAdSlots = emptySet() // Reset ad triggers on loop
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showControls = !showControls }
            ) {
                // Video visual representation
                AsyncImage(
                    model = videoUrl.ifBlank { "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=1200" },
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Subtle animated scanline or ambient pulse to indicate active playing
                if (isPlaying) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.05f,
                        targetValue = 0.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = alpha))
                    )
                }

                // Overlay Controls (when visible)
                AnimatedVisibility(
                    visible = showControls,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        // Top Bar: Back, Title, Quality badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .align(Alignment.TopCenter),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = title.ifBlank { "FaceFeed HD Video Player" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = authorName,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Free Stream & Quality Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BangladeshGreen.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "🟢 ফ্রি $selectedQuality",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Center Play / Pause Large Button
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Bottom Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            // Progress bar & time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val progress = currentSeconds.toFloat() / totalSeconds.toFloat()
                                Slider(
                                    value = progress,
                                    onValueChange = { newProgress ->
                                        currentSeconds = (newProgress * totalSeconds).toInt()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = BangladeshGreen,
                                        activeTrackColor = BangladeshGreen,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val currentFormatted = String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60)
                                val totalFormatted = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)

                                Text(
                                    text = "$currentFormatted / $totalFormatted",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Bottom Buttons: Mute, Like, Fullscreen
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Mute/Unmute
                                    IconButton(onClick = { isMuted = !isMuted }) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Mute",
                                            tint = Color.White
                                        )
                                    }

                                    // Like
                                    IconButton(
                                        onClick = {
                                            isLiked = !isLiked
                                            likeCount += if (isLiked) 1 else -1
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (isLiked) BangladeshRed else Color.White
                                        )
                                    }
                                    Text(
                                        text = "$likeCount",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "সবাই ফ্রিতে ভিডিও দেখতে পারবে",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BangladeshGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Social Ads schedule badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FacebookBlue.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isShortVideo) "📢 সামাজিক বিজ্ঞাপন: শুরুতে ১টি • শেষে ১টি" else "📢 সামাজিক বিজ্ঞাপন: শুরুতে ১টি • মাঝে ১টি • শেষে ১টি",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = {
                                            activeAd = SampleData.sampleSocialAds.random()
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("বিজ্ঞাপন চালান", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Social Advertisement Overlay (Long: 1 start, 1 mid, 1 end. Short: 1 start, 1 end)
                if (activeAd != null) {
                    val adTitle = when {
                        !isShortVideo && currentSeconds in 0..4 -> "শুরুর সামাজিক বিজ্ঞাপন (১/৩)"
                        !isShortVideo && currentSeconds in 22..26 -> "মাঝের সামাজিক বিজ্ঞাপন (২/৩)"
                        !isShortVideo && currentSeconds in 41..45 -> "শেষের সামাজিক বিজ্ঞাপন (৩/৩)"
                        isShortVideo && currentSeconds in 0..3 -> "শুরুর সামাজিক বিজ্ঞাপন (১/২)"
                        else -> "শেষের সামাজিক বিজ্ঞাপন (২/২)"
                    }
                    SocialAdOverlay(
                        ad = activeAd!!,
                        adSlotTitle = adTitle,
                        modifier = Modifier.align(Alignment.Center),
                        onAdFinished = {
                            val slot = when {
                                !isShortVideo && currentSeconds in 0..4 -> "start"
                                !isShortVideo && currentSeconds in 22..26 -> "mid"
                                !isShortVideo && currentSeconds in 41..45 -> "end"
                                isShortVideo && currentSeconds in 0..3 -> "short_start"
                                else -> "short_end"
                            }
                            dismissedAdSlots = dismissedAdSlots + slot
                            activeAd = null
                        }
                    )
                }
            }
        }
    }
}
