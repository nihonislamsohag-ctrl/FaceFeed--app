package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.*
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: Post,
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isFreeMode by viewModel.isFreeMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val strings = Translations.get(currentLang)
    var showReactionPicker by remember { mutableStateOf(false) }
    var showReactorsDialog by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showVideoPlayer by remember { mutableStateOf(false) }
    val currentPlayingTrackId by com.example.util.AppSoundManager.currentPlayingTrackId.collectAsState()
    val isAudioEnginePlaying by com.example.util.AppSoundManager.isAudioPlaying.collectAsState()
    val audioProgress by com.example.util.AppSoundManager.playbackProgress.collectAsState()
    val audioPositionMs by com.example.util.AppSoundManager.playbackPositionMs.collectAsState()
    val audioDurationMs by com.example.util.AppSoundManager.playbackDurationMs.collectAsState()
    val postCardContext = androidx.compose.ui.platform.LocalContext.current
    var showBoostDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showPostActionMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showStickerTray by remember { mutableStateOf(false) }
    var showGifTray by remember { mutableStateOf(false) }
    var showMentionPicker by remember { mutableStateOf(false) }

    DisposableEffect(showComments, post.id) {
        if (showComments) {
            viewModel.observeCommentsForPost(post.id)
        }
        onDispose {
            if (showComments) {
                viewModel.stopObservingCommentsForPost(post.id)
            }
        }
    }

    val attachedAudio = remember(post.id, post.audioTrack, post.musicInfo) {
        post.audioTrack ?: post.musicInfo?.let { info ->
            PostAudioTrack("aud_post_${post.id}", info, "শিল্পীগোষ্ঠী", "অডিও গান", "৩:৩০")
        }
    }
    val isThisAudioPlaying = attachedAudio != null && isAudioEnginePlaying && (currentPlayingTrackId == attachedAudio.id)
    val toggleAttachedAudio = {
        if (attachedAudio != null) {
            if (isThisAudioPlaying) {
                com.example.util.AppSoundManager.pauseAudio()
            } else {
                com.example.util.AppSoundManager.playAudio(attachedAudio, postCardContext)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Author, Avatar, Online Dot, Verified Badge, Timestamp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openUserProfile(post.author) }
                ) {
                    // Avatar with online status
                    Box(contentAlignment = Alignment.BottomEnd) {
                        FotoFilmAvatar(
                            imageUrl = post.author.avatarUrl,
                            name = post.author.fullName,
                            contentDescription = post.author.fullName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                        if (post.author.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.author.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (post.author.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 16.dp)
                            }
                            if (post.location != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "is at ",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = post.location,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.timestamp,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            if (attachedAudio != null) {
                                val musicHeaderText = remember(post.musicInfo, attachedAudio) {
                                    post.musicInfo?.takeIf { it.isNotBlank() } ?: run {
                                        val artistPart = attachedAudio.artist.takeIf { it.isNotBlank() && it != "শিল্পীগোষ্ঠী" }
                                        listOfNotNull(artistPart, attachedAudio.title.takeIf { it.isNotBlank() })
                                            .joinToString(" • ")
                                    }
                                }
                                if (musicHeaderText.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "•",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .clickable { toggleAttachedAudio() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = "Attached Audio",
                                            tint = if (isThisAudioPlaying) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = musicHeaderText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isThisAudioPlaying) BangladeshGreen else FacebookBlue,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• 🌐",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Reach views indicator
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "👁️ ${post.reachViews}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            if (post.isBoosted) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FacebookBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "🚀 Sponsored",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Post options & close buttons (like Facebook screenshot)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showPostActionMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { /* Hide/Close */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide post",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Post Title / Caption (if provided)
            if (!post.title.isNullOrBlank()) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Post Text Content
            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Hashtags (Up to 10 hashtags)
            if (post.hashtags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.hashtags.take(10).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = FacebookBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = tag,
                                color = FacebookBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Media Grid: Up to 15 Pictures
            if (post.images.isNotEmpty() && !post.isVideo) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TenPhotoGrid(
                        images = post.images,
                        onImageClick = { fullscreenImageUrl = it }
                    )

                    if (attachedAudio != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (isThisAudioPlaying) BangladeshGreen.copy(alpha = 0.88f) else Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .size(34.dp)
                                .clickable { toggleAttachedAudio() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isThisAudioPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = if (isThisAudioPlaying) "Mute post audio" else "Play post audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // User requested: যে কোনো ছবির নিচে Banner এই এড চালু করে দেন
                StickyVideoBottomBanner(
                    modifier = Modifier.padding(top = 4.dp),
                    adUnitId = GoogleAdConfig.BANNER_ID
                )
            }

            // Video Player Preview
            if (post.isVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(top = 6.dp)
                        .background(Color.Black)
                        .clickable { showVideoPlayer = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (post.images.isNotEmpty()) {
                        AsyncImage(
                            model = post.images.first(),
                            contentDescription = "Video Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { showVideoPlayer = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Free Video Stream Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = BangladeshGreen.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 সম্পূর্ণ ফ্রি প্লে",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Video Duration Pill & Title
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = post.videoTitle ?: if (post.isReel) "FotoFilm Short Video" else "Video",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = post.videoDuration ?: "0:45",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Rule 4: আর যেকোনো ভিডিওর নিচে সবসময় ১টা ছোট Banner এড লেগে থাকবে।
                StickyVideoBottomBanner(
                    modifier = Modifier.padding(top = 2.dp),
                    adUnitId = GoogleAdConfig.BANNER_ID
                )
            }

            // Attached Audio Track / Music Player Bar
            if (attachedAudio != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isThisAudioPlaying) BangladeshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isThisAudioPlaying) BangladeshGreen else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play/Pause Button
                            Surface(
                                shape = CircleShape,
                                color = if (isThisAudioPlaying) BangladeshGreen else FacebookBlue,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        if (isThisAudioPlaying) {
                                            com.example.util.AppSoundManager.pauseAudio()
                                        } else {
                                            com.example.util.AppSoundManager.playAudio(attachedAudio, postCardContext)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isThisAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isThisAudioPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = attachedAudio.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${attachedAudio.artist} • ${attachedAudio.category}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${attachedAudio.duration})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (isThisAudioPlaying) {
                                Spacer(modifier = Modifier.width(8.dp))
                                EqualizerAnimation()
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FacebookBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "🎵 বাজান",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Playback Progress Bar & Sound Active Indicator
                        if (isThisAudioPlaying) {
                            LinearProgressIndicator(
                                progress = { audioProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = BangladeshGreen,
                                trackColor = BangladeshGreen.copy(alpha = 0.2f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BangladeshGreen.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val posSec = (audioPositionMs / 1000).coerceAtLeast(0)
                                val durSec = (audioDurationMs / 1000).coerceAtLeast(1)
                                Text(
                                    text = if (!attachedAudio.audioUrl.isNullOrBlank()) {
                                        String.format("🔊 ভয়েস/অডিও চলছে (%02d:%02d / %02d:%02d)", posSec / 60, posSec % 60, durSec / 60, durSec % 60)
                                    } else {
                                        "🔊 সাউন্ড প্লে হচ্ছে (ফোন স্পিকার চালু)"
                                    },
                                    fontSize = 10.sp,
                                    color = BangladeshGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(audioProgress * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = BangladeshGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Facebook-Style Stats Summary Row (Screenshot 1: ❤️👍 Md ... and others    16 comments • 👁️ 751)
            val totalReactions = post.totalReactions
            val syncedCommentCount = maxOf(post.comments.size, post.cloudCommentCount)
            val formattedViews = post.reachViews.replace("🚀", "").trim().ifBlank { "0" }
            val reactorNameText = remember(post.reactors, post.userReaction, totalReactions) {
                val recordedReactors = post.reactors
                when {
                    recordedReactors.isNotEmpty() -> {
                        val first = recordedReactors.first()
                        val name = if (first.user.id == currentUser.id) "আপনি (${first.user.fullName})" else first.user.fullName
                        if (totalReactions > 1) "$name এবং আরও ${totalReactions - 1} জন" else name
                    }
                    post.userReaction != null -> {
                        if (totalReactions > 1) "আপনি এবং আরও ${totalReactions - 1} জন" else currentUser.fullName
                    }
                    totalReactions > 0 -> {
                        "$totalReactions জন"
                    }
                    else -> "0"
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Overlapping ❤️👍 circles + reactor summary + quick 10-reaction trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showReactorsDialog = true }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(19.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(19.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❤️", fontSize = 9.sp)
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = 12.dp)
                                .size(19.dp)
                                .clip(CircleShape)
                                .background(FacebookBlue)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = post.userReaction?.emoji ?: "👍", fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = reactorNameText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Small 10-reactions picker quick trigger
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showReactionPicker = !showReactionPicker }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AddReaction,
                                contentDescription = "সব রিঅ্যাকশন",
                                tint = FacebookBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Comments & Views count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$syncedCommentCount comments",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.clickable { showComments = true }
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showBoostDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "ভিউ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formattedViews,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Interactive Floating Reaction Picker (10 REACTION EMOJIS: 5 on top, 5 on bottom)
            Box(modifier = Modifier.fillMaxWidth()) {
                if (showReactionPicker) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = (-110).dp, x = 8.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    ReactionType.LIKE,
                                    ReactionType.DISLIKE,
                                    ReactionType.OK,
                                    ReactionType.LOVE,
                                    ReactionType.CARE
                                ).forEach { rx ->
                                    Surface(
                                        onClick = {
                                            viewModel.reactToPost(post.id, rx)
                                            com.example.util.AppSoundManager.playReactionChime()
                                            showReactionPicker = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (post.userReaction == rx) Color(rx.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = rx.emoji, fontSize = 20.sp)
                                            Text(
                                                text = if (currentLang == AppLanguage.BENGALI) rx.bengaliName else rx.englishName,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(rx.colorHex)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    ReactionType.WOW,
                                    ReactionType.HAHA,
                                    ReactionType.SAD,
                                    ReactionType.ANGRY,
                                    ReactionType.HUNDRED
                                ).forEach { rx ->
                                    Surface(
                                        onClick = {
                                            viewModel.reactToPost(post.id, rx)
                                            com.example.util.AppSoundManager.playReactionChime()
                                            showReactionPicker = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (post.userReaction == rx) Color(rx.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = rx.emoji, fontSize = 20.sp)
                                            Text(
                                                text = if (currentLang == AppLanguage.BENGALI) rx.bengaliName else rx.englishName,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(rx.colorHex)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Facebook Rounded Pill Action Bar (Screenshot 1: Like, Comment, Share, Views, Boost)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pillBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)

                    // 1. Like Pill (Tap to Like/Unlike, Long-Press for all 10 reactions)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (post.userReaction != null) Color(post.userReaction.colorHex).copy(alpha = 0.14f) else pillBg,
                        modifier = Modifier
                            .weight(1.05f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .combinedClickable(
                                onClick = {
                                    if (post.userReaction == null) {
                                        viewModel.reactToPost(post.id, ReactionType.LIKE)
                                        com.example.util.AppSoundManager.playReactionChime()
                                    } else {
                                        viewModel.reactToPost(post.id, post.userReaction)
                                    }
                                },
                                onLongClick = {
                                    showReactionPicker = !showReactionPicker
                                }
                            )
                            .testTag("post_like_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (post.userReaction != null) {
                                Text(text = post.userReaction.emoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (totalReactions > 0) totalReactions else 1}",
                                    color = Color(post.userReaction.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ThumbUp,
                                    contentDescription = strings.like,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (totalReactions > 0) totalReactions else 40}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // 2. Comment Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = pillBg,
                        modifier = Modifier
                            .weight(0.95f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showComments = true }
                            .testTag("post_comment_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = strings.comment,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$syncedCommentCount",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // 3. Share Pill (Curved Facebook Share arrow + count)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = pillBg,
                        modifier = Modifier
                            .weight(0.95f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showShareDialog = true }
                            .testTag("post_share_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = strings.share,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(17.dp)
                                    .graphicsLayer { scaleX = -1f }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (post.shareCount > 0) "${post.shareCount}" else strings.share,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // 4. Views Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = pillBg,
                        modifier = Modifier
                            .weight(1.0f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showBoostDialog = true }
                            .testTag("post_views_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = "ভিউ",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formattedViews,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // 5. Boost Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (post.isBoosted) BangladeshGreen.copy(alpha = 0.14f) else FacebookBlue.copy(alpha = 0.10f),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showBoostDialog = true }
                            .testTag("post_boost_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🚀", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (post.isBoosted) "চলছে" else if (post.isVideo) "প্রোমোট" else "বুস্ট",
                                color = if (post.isBoosted) BangladeshGreen else FacebookBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Active Boost / Sponsored Banner
                if (post.isBoosted) {
                    Surface(
                        color = BangladeshGreen.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚀", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "স্পন্সরড বুস্টিং সক্রিয় (${post.boostDays ?: 7} দিন) • বাজেট ৳${post.boostBudget ?: 500}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BangladeshGreen
                                )
                            }
                            Text(
                                text = "Trx: ${post.boostTrxId?.take(8) ?: "BKH928"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Facebook-Style Comment Modal Bottom Sheet (Matching Screenshot 2)
    if (showComments) {
        FacebookCommentBottomSheet(
            post = post,
            currentUser = currentUser,
            viewModel = viewModel,
            currentLang = currentLang,
            totalReactions = post.totalReactions,
            newCommentText = newCommentText,
            onNewCommentTextChange = { newCommentText = it },
            replyingToCommentId = replyingToCommentId,
            onReplyingToCommentIdChange = { replyingToCommentId = it },
            replyText = replyText,
            onReplyTextChange = { replyText = it },
            showGifTray = showGifTray,
            onToggleGifTray = { showGifTray = !showGifTray },
            onOpenReactors = { showReactorsDialog = true },
            onOpenShare = { showShareDialog = true },
            onDismiss = { showComments = false }
        )
    }

    // Post 3-Dots Action Bottom Sheet (Screenshot 2 for Video, Screenshot 3 for Regular)
    if (showPostActionMenu) {
        PostActionBottomSheet(
            post = post,
            viewModel = viewModel,
            onDismiss = { showPostActionMenu = false },
            onOpenBoost = { showBoostDialog = true }
        )
    }

    // Fullscreen Image Dialog (Screenshot 1: Top Bar ✕, avatar, media, tag, 3 dots -> Make Profile, Make Cover, Delete, Save, Edit Privacy)
    if (fullscreenImageUrl != null) {
        FullScreenPostPhotoViewer(
            imageUrl = fullscreenImageUrl!!,
            post = post,
            viewModel = viewModel,
            onDismiss = { fullscreenImageUrl = null }
        )
    }

    // Video Player Dialog (Smooth full playback)
    if (showVideoPlayer) {
        VideoPlayerDialog(
            videoUrl = post.images.firstOrNull().orEmpty(),
            title = post.videoTitle ?: post.title ?: post.text.take(30),
            authorName = post.author.fullName,
            post = post,
            viewModel = viewModel,
            onDismiss = { showVideoPlayer = false }
        )
    }

    // Reaction Users Dialog (Shows list of who gave which reaction)
    if (showReactorsDialog) {
        ReactionUsersDialog(
            post = post,
            onDismiss = { showReactorsDialog = false },
            onOpenUserProfile = { targetUser ->
                showReactorsDialog = false
                showComments = false
                viewModel.openUserProfile(targetUser)
            },
            onMessageUser = { targetUser ->
                showReactorsDialog = false
                showComments = false
                viewModel.openChatWithUser(targetUser)
            }
        )
    }

    // Boost & Promote Dialog
    if (showBoostDialog) {
        BoostPostDialog(
            post = post,
            viewModel = viewModel,
            onDismiss = { showBoostDialog = false }
        )
    }

    // Interactive Share Bottom Sheet
    if (showShareDialog) {
        SharePostBottomSheet(
            post = post,
            viewModel = viewModel,
            onDismiss = { showShareDialog = false }
        )
    }
}

// Photo Grid Layout: supports up to 15 photos (organized cleanly in 5 per row)
@Composable
fun TenPhotoGrid(
    images: List<String>,
    onImageClick: (String) -> Unit
) {
    if (images.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (images.size >= 11) {
            // 3 rows for up to 15 images (5 per row)
            val row1 = images.take(5)
            val row2 = images.drop(5).take(5)
            val row3 = images.drop(10).take(5)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row1.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo row 1",
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row2.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo row 2",
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row3.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo row 3",
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else if (images.size >= 6) {
            // 2 rows of 5 images
            val top5 = images.take(5)
            val bottomRemainder = images.drop(5)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                top5.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo top",
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                bottomRemainder.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo bottom",
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            // If fewer than 10 images, responsive grid
            val topHalf = images.take((images.size + 1) / 2)
            val bottomHalf = images.drop(topHalf.size)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                topHalf.forEach { img ->
                    AsyncImage(
                        model = img,
                        contentDescription = "Photo",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onImageClick(img) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (bottomHalf.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bottomHalf.forEach { img ->
                        AsyncImage(
                            model = img,
                            contentDescription = "Photo",
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onImageClick(img) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    postId: String,
    comment: Comment,
    viewModel: FaceFeedViewModel,
    currentLang: AppLanguage,
    onReplyClick: () -> Unit,
    postAuthor: User? = null,
    onUserClick: (User) -> Unit = { viewModel.openUserProfile(it) }
) {
    val strings = Translations.get(currentLang)
    var showCommentReactionPicker by remember { mutableStateOf(false) }
    var showOffenderLegalDialog by remember { mutableStateOf(false) }
    var legalActionStatus by remember { mutableStateOf<String?>(null) }

    val hasReplies = comment.replies.isNotEmpty()
    val authorReactionBadge = comment.userReaction ?: if (comment.totalReactions > 0) ReactionType.LOVE else null

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // Left avatar + curved vertical thread line when replies exist (Screenshot 2)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(38.dp).fillMaxHeight()
            ) {
                FotoFilmAvatar(
                    imageUrl = comment.author.avatarUrl,
                    name = comment.author.fullName,
                    contentDescription = comment.author.fullName,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onUserClick(comment.author) }
                )
                if (hasReplies) {
                    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .width(24.dp)
                            .padding(top = 4.dp, bottom = 14.dp)
                    ) {
                        val startX = size.width / 2f
                        val path = Path().apply {
                            moveTo(startX, 0f)
                            lineTo(startX, (size.height - 14f).coerceAtLeast(0f))
                            quadraticBezierTo(
                                startX,
                                size.height,
                                startX + 16f,
                                size.height
                            )
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Facebook Rounded Comment Bubble with "❤️ by Author" pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showCommentReactionPicker = !showCommentReactionPicker }
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = comment.author.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { onUserClick(comment.author) }
                            )
                            if (comment.author.isVerified) {
                                FidaVerifiedBadge(size = 12.dp)
                            }
                            if (authorReactionBadge != null) {
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(authorReactionBadge.colorHex).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = authorReactionBadge.emoji, fontSize = 10.sp)
                                }
                                Text(
                                    text = if (postAuthor != null && comment.author.id == postAuthor.id) "Author" else "by Author",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val isPureEmojiComment = comment.text.trim().length <= 6 && comment.text.any { it.code > 0x2000 }
                        Text(
                            text = comment.text,
                            fontSize = if (isPureEmojiComment) 22.sp else 13.5.sp,
                            lineHeight = if (isPureEmojiComment) 26.sp else 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 10 Reactions Picker for Comment (Floating when activated)
                if (showCommentReactionPicker) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(ReactionType.LIKE, ReactionType.DISLIKE, ReactionType.OK, ReactionType.LOVE, ReactionType.CARE).forEach { rx ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.reactToComment(postId, comment.id, rx)
                                                com.example.util.AppSoundManager.playReactionChime()
                                                showCommentReactionPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = rx.emoji, fontSize = 17.sp)
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(ReactionType.WOW, ReactionType.HAHA, ReactionType.SAD, ReactionType.ANGRY, ReactionType.HUNDRED).forEach { rx ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.reactToComment(postId, comment.id, rx)
                                                com.example.util.AppSoundManager.playReactionChime()
                                                showCommentReactionPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = rx.emoji, fontSize = 17.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Row Matching Screenshot 2: "9h   Reply   1 ❤️       👍   👎"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, top = 4.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.timestamp,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = strings.reply,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.clickable { onReplyClick() }
                    )

                    if (comment.totalReactions > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCommentReactionPicker = !showCommentReactionPicker }
                        ) {
                            Text(
                                text = "${comment.totalReactions}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val rxBadge = comment.userReaction ?: comment.reactionCounts.keys.firstOrNull() ?: ReactionType.LOVE
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(Color(rxBadge.colorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = rxBadge.emoji, fontSize = 9.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Trigger all 10 reactions
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable {
                            showCommentReactionPicker = !showCommentReactionPicker
                        }
                    ) {
                        Text(
                            text = "🔟",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Quick 👍 Like & 👎 Dislike icons on right (Screenshot 2)
                    IconButton(
                        onClick = {
                            viewModel.reactToComment(postId, comment.id, ReactionType.LIKE)
                            com.example.util.AppSoundManager.playReactionChime()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.userReaction == ReactionType.LIKE) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = strings.like,
                            tint = if (comment.userReaction == ReactionType.LIKE) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = {
                            viewModel.reactToComment(postId, comment.id, ReactionType.DISLIKE)
                            com.example.util.AppSoundManager.playReactionChime()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.userReaction == ReactionType.DISLIKE) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                            contentDescription = "Dislike",
                            tint = if (comment.userReaction == ReactionType.DISLIKE) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Feature 12: খারাপ মন্তব্যকারী আইনগত প্রোফাইল ও অডিট বাটন
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Red.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.Red.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable {
                            viewModel.reportCyberHarassment(
                                userId = comment.author.id,
                                userName = comment.author.fullName,
                                comment = comment.text,
                                postTitle = "পোস্ট আইডি: $postId"
                            )
                            showOffenderLegalDialog = true
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("⚖️", fontSize = 9.5.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "আইনি প্রোফাইল",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Feature 12: খারাপ মন্তব্যকারীর আইনগত প্রোফাইল ও অডিট ডায়ালগ
                if (showOffenderLegalDialog) {
                    val nidHash = Math.abs(comment.author.id.hashCode() % 900000000 + 1000000000L)
                    val simNumber = "017" + String.format("%07d", Math.abs(comment.author.id.hashCode() % 10000000))
                    val ipAddress = "103.114.152." + (Math.abs(comment.author.id.hashCode() % 200) + 10)

                    AlertDialog(
                        onDismissRequest = { showOffenderLegalDialog = false },
                        icon = {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(36.dp)
                            )
                        },
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚖️ সাইবার আইনগত প্রোফাইল",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "ডিজিটাল ও সাইবার নিরাপত্তা আইন ২০২৩",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Offender Info Card
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FotoFilmAvatar(
                                            imageUrl = comment.author.avatarUrl,
                                            name = comment.author.fullName,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = comment.author.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "ইউজার আইডি: ${comment.author.id}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }

                                // Reported Comment
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Red.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "⚠️ মন্তব্য রেকর্ড:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                        Text(
                                            text = "\"${comment.text}\"",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Legal Records
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("🪪 জাতীয় পরিচয়পত্র (NID): 199$nidHash", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("📱 নিবন্ধিত সিম: +88$simNumber (Biometric)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("🌐 আইপি অ্যাড্রেস: $ipAddress", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("📱 ডিভাইস: Android 14 / SM-A546E", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("📍 অবস্থান: মিরপুর-১০, ঢাকা (Cell Tower)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        Text(
                                            text = "⚖️ লঙ্ঘিত আইনগত ধারা:\n• ডিজিটাল নিরাপত্তা আইন ২০১৮ ধারা ২৯ (মানহানি)\n• সাইবার নিরাপত্তা আইন ২০২৩ ধারা ২৫ ও ২৯",
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (legalActionStatus != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BangladeshGreen.copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = legalActionStatus!!,
                                            color = BangladeshGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.toggleUserAccountStatus(
                                        comment.author.id,
                                        false,
                                        "সাইবার নিরাপত্তা আইন ২০২৩ লঙ্ঘনের কারণে স্থায়ী ব্যান"
                                    )
                                    legalActionStatus = "🛑 অপরাধীর আইডি স্থায়ীভাবে বন্ধ ও ডিভাইস ব্যান করা হয়েছে!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                            ) {
                                Text("🛑 স্থায়ী ব্যান", fontSize = 11.sp)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showOffenderLegalDialog = false }) {
                                Text("বন্ধ করুন", fontSize = 11.sp)
                            }
                        }
                    )
                }

                // Replies list matching Screenshot 2 ([mini avatar] Author Name  Commenter ❤️❤️)
                comment.replies.forEach { reply ->
                    var showReplyReactionPicker by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FotoFilmAvatar(
                                imageUrl = reply.author.avatarUrl,
                                name = reply.author.fullName,
                                contentDescription = reply.author.fullName,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .clickable { onUserClick(reply.author) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reply.author.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { onUserClick(reply.author) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reply.text,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .combinedClickable(
                                        onClick = {
                                            if (reply.userReaction == null) {
                                                viewModel.reactToCommentReply(postId, comment.id, reply.id, ReactionType.LOVE)
                                                com.example.util.AppSoundManager.playReactionChime()
                                            } else {
                                                viewModel.reactToCommentReply(postId, comment.id, reply.id, reply.userReaction)
                                            }
                                        },
                                        onLongClick = { showReplyReactionPicker = !showReplyReactionPicker }
                                    )
                            )
                        }

                        // Reply reaction preview
                        if (reply.totalReactions > 0) {
                            Row(
                                modifier = Modifier.padding(start = 30.dp, top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val topRx = if (reply.reactionCounts.isNotEmpty()) {
                                    reply.reactionCounts.keys.take(2)
                                } else {
                                    listOf(ReactionType.LIKE)
                                }
                                topRx.forEach { rx ->
                                    Text(text = rx.emoji, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${reply.totalReactions}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 10 Reactions Picker for Reply
                        if (showReplyReactionPicker) {
                            Surface(
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    ReactionType.ALL_REACTIONS.forEach { rx ->
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.reactToCommentReply(postId, comment.id, reply.id, rx)
                                                    com.example.util.AppSoundManager.playReactionChime()
                                                    showReplyReactionPicker = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = rx.emoji, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Reply actions
                        Row(
                            modifier = Modifier.padding(start = 30.dp, top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reply.timestamp,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (reply.userReaction == null) {
                                        viewModel.reactToCommentReply(postId, comment.id, reply.id, ReactionType.LIKE)
                                    } else {
                                        viewModel.reactToCommentReply(postId, comment.id, reply.id, reply.userReaction)
                                    }
                                }
                            ) {
                                if (reply.userReaction != null) {
                                    Text(text = reply.userReaction.emoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.BENGALI) reply.userReaction.bengaliName else reply.userReaction.englishName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(reply.userReaction.colorHex)
                                    )
                                } else {
                                    Text(
                                        text = strings.like,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "🔟",
                                fontSize = 10.sp,
                                modifier = Modifier.clickable {
                                    showReplyReactionPicker = !showReplyReactionPicker
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionUsersDialog(
    post: Post,
    onDismiss: () -> Unit,
    onOpenUserProfile: ((User) -> Unit)? = null,
    onMessageUser: ((User) -> Unit)? = null
) {
    var selectedReactionTab by remember { mutableStateOf<ReactionType?>(null) }
    val totalReactions = post.totalReactions

    val displayReactors = remember(post.reactors) {
        post.reactors.toList()
    }

    val filteredList = if (selectedReactionTab == null) {
        displayReactors
    } else {
        displayReactors.filter { it.reaction == selectedReactionTab }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("reaction_dialog_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "প্রতিক্রিয়া প্রদানকারী (${totalReactions})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedReactionTab == null) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedReactionTab = null }
                    ) {
                        Text(
                            text = "সব (${totalReactions})",
                            color = if (selectedReactionTab == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    ReactionType.ALL_REACTIONS.forEach { reaction ->
                        val count = post.reactionCounts[reaction] ?: 0
                        if (count > 0 || post.reactors.any { it.reaction == reaction }) {
                            val isSelected = selectedReactionTab == reaction
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(reaction.colorHex) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedReactionTab = reaction }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = reaction.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$count",
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { reactor ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (onOpenUserProfile != null) {
                                        onOpenUserProfile(reactor.user)
                                    } else {
                                        onDismiss()
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                FotoFilmAvatar(
                                    imageUrl = reactor.user.avatarUrl,
                                    name = reactor.user.fullName,
                                    contentDescription = reactor.user.fullName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    text = reactor.reaction.emoji,
                                    fontSize = 14.sp,
                                    modifier = Modifier.offset(x = 4.dp, y = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = reactor.user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (reactor.user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FidaVerifiedBadge(size = 12.dp)
                                    }
                                }
                                Text(
                                    text = reactor.reaction.bengaliName,
                                    fontSize = 11.sp,
                                    color = Color(reactor.reaction.colorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                if (onMessageUser != null) {
                                    onMessageUser(reactor.user)
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Text(
                                text = "বার্তা পাঠান",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FacebookCommentBottomSheet(
    post: Post,
    currentUser: User,
    viewModel: FaceFeedViewModel,
    currentLang: AppLanguage,
    totalReactions: Int,
    newCommentText: String,
    onNewCommentTextChange: (String) -> Unit,
    replyingToCommentId: String?,
    onReplyingToCommentIdChange: (String?) -> Unit,
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    showGifTray: Boolean,
    onToggleGifTray: () -> Unit,
    onOpenReactors: () -> Unit,
    onOpenShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = Translations.get(currentLang)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQuickSuggestionChips by remember { mutableStateOf(true) }
    var showEmojiQuickTray by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Top Header Row (Screenshot 2: Back button + ❤️👍 count on left, 👍 💬 ↗ action icons on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("comment_sheet_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenReactors() }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "❤️", fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .offset(x = 13.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = post.userReaction?.emoji ?: "👍", fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (totalReactions > 0) totalReactions else 40}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (post.userReaction == null) {
                                viewModel.reactToPost(post.id, ReactionType.LIKE)
                                com.example.util.AppSoundManager.playReactionChime()
                            } else {
                                viewModel.reactToPost(post.id, post.userReaction)
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (post.userReaction != null) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = strings.like,
                            tint = if (post.userReaction != null) Color(post.userReaction.colorHex) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showQuickSuggestionChips = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = strings.comment,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenShare,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = strings.share,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(21.dp)
                                .graphicsLayer { scaleX = -1f }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Threaded Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (post.comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💬", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "এখনো কোনো কমেন্ট নেই — প্রথম কমেন্ট করুন!",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(post.comments, key = { it.id }) { comment ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CommentItem(
                            postId = post.id,
                            comment = comment,
                            viewModel = viewModel,
                            currentLang = currentLang,
                            onReplyClick = {
                                onReplyingToCommentIdChange(
                                    if (replyingToCommentId == comment.id) null else comment.id
                                )
                            },
                            postAuthor = post.author,
                            onUserClick = { targetUser ->
                                onDismiss()
                                viewModel.openUserProfile(targetUser)
                            }
                        )

                        if (replyingToCommentId == comment.id) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 46.dp, top = 6.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = onReplyTextChange,
                                    placeholder = { Text("${comment.author.fullName}-কে উত্তর দিন...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (replyText.isNotBlank()) {
                                            viewModel.replyToComment(post.id, comment.id, replyText)
                                            onReplyTextChange("")
                                            onReplyingToCommentIdChange(null)
                                        }
                                    },
                                    enabled = replyText.isNotBlank(),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (replyText.isNotBlank()) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Reply",
                                        tint = if (replyText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Quick Suggestion Chips Bar (Screenshot 2: "I love this so much!", "❤️❤️❤️❤️", "So adorable! ❤️", "Great picture!!!")
            if (showQuickSuggestionChips) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val quickSuggestions = listOf(
                        "I love this so much!",
                        "❤️❤️❤️❤️",
                        "So adorable! ❤️",
                        "Great picture!!!"
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 34.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickSuggestions.forEach { suggestion ->
                            Surface(
                                onClick = {
                                    onNewCommentTextChange(
                                        if (newCommentText.isBlank()) suggestion else "$newCommentText $suggestion"
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Circular X dismiss button on top-right
                    Surface(
                        onClick = { showQuickSuggestionChips = false },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss suggestions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Optional Emoji / GIF Trays
            if (showEmojiQuickTray) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("👍", "❤️", "🔥", "😄", "🥰", "👏", "💯", "😮", "✨", "🇧🇩").forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.clickable { onNewCommentTextChange(newCommentText + emoji) }
                        ) {
                            Text(text = emoji, fontSize = 18.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }

            if (showGifTray) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("🎉 Congratulations!", "🔥 Awesome!", "❤️ Love it!", "👏 Well done!", "🌟 Outstanding!").forEach { gifText ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FacebookBlue.copy(alpha = 0.1f),
                            modifier = Modifier.clickable {
                                viewModel.addComment(post.id, "[GIF] $gifText")
                                onToggleGifTray()
                            }
                        ) {
                            Text(
                                text = gifText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FacebookBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Bottom Facebook Comment Input Bar (Screenshot 2: Gallery + "Comment as <Name>" + GIF + @ + 😊 + Send)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNewCommentTextChange(newCommentText + " 📷 [Photo]") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Add Photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = newCommentText,
                        onValueChange = onNewCommentTextChange,
                        placeholder = {
                            Text(
                                text = "Comment as ${currentUser.fullName}",
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .clickable { onToggleGifTray() }
                                        .padding(1.dp)
                                ) {
                                    Text(
                                        text = "GIF",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onNewCommentTextChange(newCommentText + " @${currentUser.username}") },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AlternateEmail,
                                        contentDescription = "Mention",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showEmojiQuickTray = !showEmojiQuickTray },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfied,
                                        contentDescription = "Emoji",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comment_input_field")
                    )
                }

                if (newCommentText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            viewModel.addComment(post.id, newCommentText)
                            onNewCommentTextChange("")
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue)
                            .testTag("send_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Comment",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

