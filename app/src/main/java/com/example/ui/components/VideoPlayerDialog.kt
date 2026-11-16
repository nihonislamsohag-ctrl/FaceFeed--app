package com.example.ui.components

import android.widget.Toast
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
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    title: String,
    authorName: String,
    post: Post? = null,
    viewModel: FaceFeedViewModel? = null,
    onDismiss: () -> Unit
) {
    val isShortVideo = post?.isReel == true
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(0) }
    val totalSeconds = if (isShortVideo) 20 else 45 // Simulated video duration
    var showControls by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(post?.totalReactions ?: 1250) }
    var selectedQuality by remember { mutableStateOf("1080p HD") }
    var showVideoOptionsSheet by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    // Google AdMob Interstitial State (Rules 1, 2, 3)
    var showGoogleInterstitialAd by remember { mutableStateOf(false) }
    var hasTriggeredMidAd by remember { mutableStateOf(false) }
    var isEndVideoAd by remember { mutableStateOf(false) }

    // Playback loop and Google AdMob Interstitial triggers
    // রিল ভিডিও: ৫ সেকেন্ড চলার পর এড চালু হবে এবং এড শেষ হলে আবার ঐখান থেকেই (৫ সে.) চলা শুরু হবে।
    // সাধারণ ভিডিও: ৩ সেকেন্ড পর এড চালু হবে।
    val midAdTriggerSeconds = if (isShortVideo) 5 else 3

    LaunchedEffect(isPlaying, currentSeconds) {
        if (currentSeconds == midAdTriggerSeconds && !hasTriggeredMidAd) {
            hasTriggeredMidAd = true
            isPlaying = false // Pause video at 5s (for reels) or 3s (for videos)!
            isEndVideoAd = false
            showGoogleInterstitialAd = true
        } else if (currentSeconds >= totalSeconds && !showGoogleInterstitialAd) {
            // Rule 3: Video completed -> Trigger End Interstitial Ad immediately
            isPlaying = false
            isEndVideoAd = true
            showGoogleInterstitialAd = true
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentSeconds < totalSeconds) {
                currentSeconds++
            } else {
                // Video finished
                isPlaying = false
                isEndVideoAd = true
                showGoogleInterstitialAd = true
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

                            // Free Stream & Quality Badge + Download Button + 3-dots Menu
                            Row(verticalAlignment = Alignment.CenterVertically) {
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

                                Spacer(modifier = Modifier.width(4.dp))

                                // Direct Download Video Button (সবার ভিডিও ডাউনলোড করার সুবিধা)
                                IconButton(
                                    onClick = {
                                        isDownloading = true
                                        downloadProgress = 0f
                                        Toast.makeText(context, "📥 '${title.ifBlank { "ভিডিও" }}' ডাউনলোড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                        coroutineScope.launch {
                                            for (p in 1..10) {
                                                delay(100)
                                                downloadProgress = p / 10f
                                            }
                                            delay(150)
                                            isDownloading = false
                                            Toast.makeText(
                                                context,
                                                "✅ ভিডিওটি সফলভাবে ডাউনলোড হয়ে আপনার ফোনের গ্যালারি ও ডাউনলোডে সংরক্ষিত হয়েছে!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download Video",
                                        tint = if (isDownloading) BangladeshGreen else Color.White
                                    )
                                }

                                // 3 Dots Button (Screenshot 2 Features)
                                IconButton(onClick = { showVideoOptionsSheet = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = Color.White
                                    )
                                }
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

                        // Bottom Controls (padding bottom 56.dp to leave space for sticky banner)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 56.dp)
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

                            // AdMob Status Badge
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
                                        text = if (isShortVideo) "📢 AdMob: রিলস ৫ সে. পর Interstitial • শেষে Interstitial" else "📢 AdMob: ৩ সে. পর Interstitial • শেষে Interstitial",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = {
                                                isPlaying = false
                                                isEndVideoAd = false
                                                showGoogleInterstitialAd = true
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("টেস্ট এড", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Google AdMob Interstitial Video Ad Overlay (Rule 2: at 5s for reels or 3s for videos, Rule 3: at video end)
                // 5-sec countdown & Skip button, pauses main video and resumes from pause point
                if (showGoogleInterstitialAd) {
                    VideoMidAd(
                        adTitle = if (isEndVideoAd) {
                            "Google AdMob Interstitial (ভিডিও সমাপ্ত বিজ্ঞাপন)"
                        } else if (isShortVideo) {
                            "Google AdMob Interstitial (রিলস ৫ সে. পর বিজ্ঞাপন)"
                        } else {
                            "Google AdMob Interstitial (৩ সে. পর বিজ্ঞাপন)"
                        },
                        adUnitId = GoogleAdConfig.INTERSTITIAL_ID,
                        onAdFinished = {
                            viewModel?.recordUserAdEvent(
                                userId = "user_me",
                                adType = if (isEndVideoAd) "ভিডিও সমাপ্তি Interstitial" else if (isShortVideo) "রিলস ৫ সে. Interstitial" else "ভিডিও ৩ সে. Interstitial",
                                wasSkipped = false
                            )
                            showGoogleInterstitialAd = false
                            if (isEndVideoAd) {
                                currentSeconds = 0
                                hasTriggeredMidAd = false
                                isPlaying = true // Restart video
                            } else {
                                isPlaying = true // Resumes right from where it stopped (5s for reel, 3s for video)!
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Rule 4: আর যেকোনো ভিডিওর নিচে সবসময় ১টা ছোট Banner এড লেগে থাকবে। স্ক্রল করলেও যাবে না।
                StickyVideoBottomBanner(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    adUnitId = GoogleAdConfig.BANNER_ID
                )
            }
        }

        // Screenshot 2 Video Options Bottom Sheet
        if (showVideoOptionsSheet) {
            if (post != null && viewModel != null) {
                PostActionBottomSheet(
                    post = post,
                    viewModel = viewModel,
                    onDismiss = { showVideoOptionsSheet = false }
                )
            } else {
                ModalBottomSheet(
                    onDismissRequest = { showVideoOptionsSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Surface(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .width(42.dp)
                                .height(4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ) {}
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        // 1. Pin post
                        PostActionItem(
                            icon = Icons.Default.PushPin,
                            title = "Pin post",
                            onClick = {
                                Toast.makeText(context, "📌 ভিডিওটি পিন করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 2. Turn off notifications
                        PostActionItem(
                            icon = Icons.Default.NotificationsNone,
                            title = "Turn off notifications for this post",
                            onClick = {
                                Toast.makeText(context, "🔕 এই ভিডিওর নোটিফিকেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 3. Save video
                        PostActionItem(
                            icon = Icons.Default.BookmarkBorder,
                            title = "Save video",
                            onClick = {
                                Toast.makeText(context, "🔖 ভিডিওটি সেভ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 4. Share
                        PostActionItem(
                            icon = Icons.Default.Reply,
                            title = "Share",
                            onClick = {
                                Toast.makeText(context, "↗️ ভিডিওটি শেয়ার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 5. Download video (সবার ভিডিও ডাউনলোড করার অপশন)
                        PostActionItem(
                            icon = Icons.Default.Download,
                            title = "Download video (ভিডিও ডাউনলোড করুন)",
                            iconTint = BangladeshGreen,
                            titleColor = BangladeshGreen,
                            onClick = {
                                showVideoOptionsSheet = false
                                isDownloading = true
                                downloadProgress = 0f
                                coroutineScope.launch {
                                    for (p in 1..10) {
                                        delay(100)
                                        downloadProgress = p / 10f
                                    }
                                    delay(150)
                                    isDownloading = false
                                    Toast.makeText(
                                        context,
                                        "✅ ভিডিওটি সফলভাবে ডাউনলোড হয়ে ফোনে সংরক্ষিত হয়েছে!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )

                        // 6. Edit Privacy
                        PostActionItem(
                            icon = Icons.Default.Public,
                            title = "Edit Privacy",
                            onClick = {
                                Toast.makeText(context, "🔒 প্রাইভেসির নিয়মাবলী", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 7. Edit Post
                        PostActionItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Post",
                            onClick = {
                                Toast.makeText(context, "✏️ ভিডিও পোস্ট এডিট অপশন", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 8. Move to archive
                        PostActionItem(
                            icon = Icons.Default.Archive,
                            title = "Move to archive",
                            onClick = {
                                Toast.makeText(context, "🗄️ পোস্টটি আর্কাইভে পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 9. Move to trash
                        PostActionItem(
                            icon = Icons.Default.Delete,
                            title = "Move to trash",
                            subtitle = "Items in your trash are deleted after 30 days.",
                            iconTint = Color.Red,
                            onClick = {
                                Toast.makeText(context, "🗑️ পোস্টটি ট্র্যাশে সরানো হয়েছে", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )

                        // 10. Copy link
                        PostActionItem(
                            icon = Icons.Default.Link,
                            title = "Copy link",
                            onClick = {
                                Toast.makeText(context, "🔗 ভিডিও লিঙ্ক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                showVideoOptionsSheet = false
                            }
                        )
                    }
                }
            }
        }

        // Downloading Dialog with Visual Progress
        if (isDownloading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ভিডিও ডাউনলোড হচ্ছে...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BangladeshGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(downloadProgress * 100).toInt()}% সম্পন্ন • ডিভাইস স্টোরেজে সেভ হচ্ছে",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
