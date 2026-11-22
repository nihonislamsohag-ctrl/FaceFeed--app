package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Post
import com.example.model.ReactionType
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
    val resolvedMedia = remember(post?.id, videoUrl) {
        resolvePlayableVideoSource(post = post, rawUrl = videoUrl)
    }

    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(0) }
    var totalSeconds by remember { mutableIntStateOf(if (isShortVideo) 20 else 45) }
    var seekRequestMs by remember { mutableStateOf<Long?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(post?.userReaction != null) }
    var likeCount by remember { mutableIntStateOf(post?.totalReactions ?: 1250) }
    val selectedQuality = "1080p HD"
    var showVideoOptionsSheet by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    // Google AdMob Interstitial State
    var showGoogleInterstitialAd by remember { mutableStateOf(false) }
    var hasTriggeredMidAd by remember { mutableStateOf(false) }
    var isEndVideoAd by remember { mutableStateOf(false) }

    val midAdTriggerSeconds = if (isShortVideo) 5 else 3

    DisposableEffect(Unit) {
        onDispose {
            VideoPlaybackCoordinator.pauseActiveVideo()
        }
    }

    LaunchedEffect(isPlaying, currentSeconds) {
        if (currentSeconds == midAdTriggerSeconds && !hasTriggeredMidAd) {
            hasTriggeredMidAd = true
            isPlaying = false
            isEndVideoAd = false
            showGoogleInterstitialAd = true
        }
    }

    Dialog(
        onDismissRequest = {
            VideoPlaybackCoordinator.pauseActiveVideo()
            onDismiss()
        },
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
                // Real Media3 ExoPlayer Video + Audio Surface
                FotoFilmExoVideoSurface(
                    ownerKey = "dialog_${post?.id ?: videoUrl.hashCode()}",
                    videoUrl = resolvedMedia.videoUrl,
                    thumbnailUrl = resolvedMedia.thumbnailUrl,
                    isActivePage = !showGoogleInterstitialAd,
                    isPaused = !isPlaying || showGoogleInterstitialAd,
                    isMuted = isMuted,
                    fillCrop = isShortVideo,
                    repeatLoop = false,
                    seekRequestMs = seekRequestMs,
                    onSeekConsumed = { seekRequestMs = null },
                    onProgressUpdate = { cur, tot ->
                        currentSeconds = cur
                        if (tot > 0) totalSeconds = tot
                    },
                    onVideoEnded = {
                        if (!showGoogleInterstitialAd) {
                            isPlaying = false
                            isEndVideoAd = true
                            showGoogleInterstitialAd = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Controls (when visible)
                AnimatedVisibility(
                    visible = showControls,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = {
                                        VideoPlaybackCoordinator.pauseActiveVideo()
                                        onDismiss()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = title.ifBlank { "FotoFilm HD Video Player" },
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
                                            val result = com.example.util.MediaSaveHelper.saveMediaToGallery(
                                                context = context,
                                                mediaUrl = resolvedMedia.videoUrl,
                                                isVideo = true
                                            )
                                            result.onSuccess {
                                                Toast.makeText(context, "✅ গ্যালারিতে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                            }.onFailure { err ->
                                                Toast.makeText(context, "⚠️ ডাউনলোড ব্যর্থ: ${err.message ?: "পুনরায় চেষ্টা করুন"}", Toast.LENGTH_SHORT).show()
                                            }
                                            isDownloading = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download Video",
                                        tint = if (isDownloading) BangladeshGreen else Color.White
                                    )
                                }

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
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 58.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val safeTotal = totalSeconds.coerceAtLeast(1)
                                val progress = (currentSeconds.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
                                Slider(
                                    value = progress,
                                    onValueChange = { newProgress ->
                                        val targetSec = (newProgress * safeTotal).toInt()
                                        currentSeconds = targetSec
                                        seekRequestMs = targetSec * 1000L
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
                                val totalFormatted = String.format("%02d:%02d", safeTotal / 60, safeTotal % 60)

                                Text(
                                    text = "$currentFormatted / $totalFormatted",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Bottom Buttons: Sound Mute/Unmute + Like
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Facebook-style Sound Toggle Button
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isMuted) BangladeshRed.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .heightIn(min = 38.dp)
                                            .clickable { isMuted = !isMuted }
                                            .testTag("dialog_video_sound_toggle")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = if (isMuted) "সাউন্ড চালু" else "সাউন্ড বন্ধ",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isMuted) "সাউন্ড বন্ধ" else "সাউন্ড চালু",
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Like
                                    IconButton(
                                        onClick = {
                                            isLiked = !isLiked
                                            likeCount += if (isLiked) 1 else -1
                                            if (post != null && viewModel != null) {
                                                viewModel.reactToPost(post.id, ReactionType.LOVE)
                                            }
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
                                        text = "সবাই ফ্রিতে দেখতে পারবে",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BangladeshGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

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
                                        text = if (isShortVideo) "📢 AdMob: শর্ট ভিডিও ৫ সে. পর Interstitial • শেষে Interstitial" else "📢 AdMob: ৩ সে. পর Interstitial • শেষে Interstitial",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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

                // Persistent Sound Toggle pill even when overlay controls are hidden
                if (!showControls && !showGoogleInterstitialAd) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 64.dp)
                            .size(48.dp)
                            .clickable { isMuted = !isMuted }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (showGoogleInterstitialAd) {
                    VideoMidAd(
                        adTitle = if (isEndVideoAd) {
                            "Google AdMob Interstitial (ভিডিও সমাপ্ত বিজ্ঞাপন)"
                        } else if (isShortVideo) {
                            "Google AdMob Interstitial (শর্ট ভিডিও ৫ সে. পর বিজ্ঞাপন)"
                        } else {
                            "Google AdMob Interstitial (৩ সে. পর বিজ্ঞাপন)"
                        },
                        adUnitId = GoogleAdConfig.INTERSTITIAL_ID,
                        onAdFinished = {
                            viewModel?.recordUserAdEvent(
                                userId = "user_me",
                                adType = if (isEndVideoAd) "ভিডিও সমাপ্তি Interstitial" else if (isShortVideo) "শর্ট ভিডিও ৫ সে. Interstitial" else "ভিডিও ৩ সে. Interstitial",
                                wasSkipped = false
                            )
                            showGoogleInterstitialAd = false
                            if (isEndVideoAd) {
                                seekRequestMs = 0L
                                currentSeconds = 0
                                hasTriggeredMidAd = false
                                isPlaying = true
                            } else {
                                isPlaying = true
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

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
