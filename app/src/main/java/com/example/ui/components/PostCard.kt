package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.*
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

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
                        AsyncImage(
                            model = post.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                            contentDescription = post.author.fullName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (post.musicInfo != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• 🎵 ${post.musicInfo}",
                                    fontSize = 12.sp,
                                    color = FacebookBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                TenPhotoGrid(
                    images = post.images,
                    onImageClick = { fullscreenImageUrl = it }
                )

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
                            text = post.videoTitle ?: if (post.isReel) "FaceFeed Reel" else "Video",
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
            val attachedAudio = post.audioTrack ?: post.musicInfo?.let { info ->
                PostAudioTrack("aud_post_${post.id}", info, "শিল্পীগোষ্ঠী", "অডিও গান", "৩:৩০")
            }
            if (attachedAudio != null) {
                val isThisAudioPlaying = isAudioEnginePlaying && (currentPlayingTrackId == attachedAudio.id)
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
                                            com.example.util.AppSoundManager.playAudio(attachedAudio)
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
                                Text(
                                    text = "🔊 সাউন্ড প্লে হচ্ছে (ফোন স্পিকার চালু)",
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

            // Reaction Counts Bar & Reactor Names (Clicking opens details dialog with all 10 reaction tabs)
            val totalReactions = post.totalReactions
            val reactorNameText = remember(post.reactors, post.userReaction, totalReactions) {
                val recordedReactors = post.reactors
                when {
                    recordedReactors.isNotEmpty() -> {
                        val first = recordedReactors.first()
                        val name = if (first.user.id == currentUser.id) "আপনি (${first.user.fullName})" else first.user.fullName
                        if (totalReactions > 1) {
                            "$name এবং আরও ${totalReactions - 1} জন"
                        } else {
                            name
                        }
                    }
                    post.userReaction != null -> {
                        if (totalReactions > 1) {
                            "আপনি (${currentUser.fullName}) এবং আরও ${totalReactions - 1} জন"
                        } else {
                            "আপনি (${currentUser.fullName})"
                        }
                    }
                    totalReactions > 0 -> {
                        "Tanvir Ahmed এবং আরও ${totalReactions - 1} জন"
                    }
                    else -> ""
                }
            }

            // Reaction, Comment, Share, View Stats Bar (Matching Screenshot 2: 👍 17  💬 2  ↗ 106  👁 751)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like / Reactions Count (Clicking opens Reactors list Dialog)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showReactorsDialog = true }
                        .padding(vertical = 2.dp)
                ) {
                    val topReactionEmoji = post.userReaction?.emoji ?: "👍"
                    Text(text = topReactionEmoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (totalReactions > 0) "$totalReactions" else "17",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comments, Shares & Views (Exact layout from user's screenshot: 💬 2   ↗ 106   👁 751)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Comment count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showComments = !showComments }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val syncedCommentCount = maxOf(post.comments.size, post.cloudCommentCount)
                        Text(
                            text = "$syncedCommentCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.sharePost(post.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.shareCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // View count (e.g. 👁 751 or 1.2M)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showBoostDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.reachViews.replace("🚀", "").trim().ifBlank { "751" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Interactive Floating Reaction Picker (10 REACTION EMOJIS: 5 on top, 5 on bottom as requested: 👍👎👌❤️🥰 😮😄😢😡💯)
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
                            // Row 1 (প্রথম ৫টি): Like 👍, Dislike 👎, Nice 👌, Love ❤️, Adorable 🥰
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val top5 = listOf(
                                    ReactionType.LIKE,
                                    ReactionType.DISLIKE,
                                    ReactionType.OK,
                                    ReactionType.LOVE,
                                    ReactionType.CARE
                                )
                                top5.forEach { rx ->
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

                            // Row 2 (পরের ৫টি): Wow 😮, Happy 😄, Sad 😢, Angry 😡, 100 💯
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bottom5 = listOf(
                                    ReactionType.WOW,
                                    ReactionType.HAHA,
                                    ReactionType.SAD,
                                    ReactionType.ANGRY,
                                    ReactionType.HUNDRED
                                )
                                bottom5.forEach { rx ->
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

                // Actions: Like, Comment, Share Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Like button (toggles default LIKE or opens 10-reaction picker)
                    TextButton(
                        onClick = {
                            if (post.userReaction == null) {
                                viewModel.reactToPost(post.id, ReactionType.LIKE)
                                com.example.util.AppSoundManager.playReactionChime()
                            } else {
                                viewModel.reactToPost(post.id, post.userReaction)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.userReaction != null) {
                                Text(text = post.userReaction.emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.BENGALI) post.userReaction.bengaliName else post.userReaction.englishName,
                                    color = Color(post.userReaction.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = strings.like,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.like,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Long press / quick toggle button to open all 10 reactions
                    IconButton(
                        onClick = { showReactionPicker = !showReactionPicker },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddReaction,
                            contentDescription = "All Reactions",
                            tint = FacebookBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Comment Button
                    TextButton(
                        onClick = { showComments = !showComments },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = strings.comment,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.comment,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Share Button
                    TextButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = strings.share,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.share,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // View / Insights Action Button (লাইক, কমেন্ট, শেয়ার, ভিউ)
                    TextButton(
                        onClick = { showBoostDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "ভিউ",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ভিউ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Boost / Promote Post or Video Button
                    TextButton(
                        onClick = { showBoostDialog = true },
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (post.isBoosted) "বুস্ট চলছে" else if (post.isVideo) "প্রোমোট" else "বুস্ট",
                                color = if (post.isBoosted) BangladeshGreen else FacebookBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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

            // Expandable Comments Section
            AnimatedVisibility(visible = showComments) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // List existing comments
                    post.comments.forEach { comment ->
                        CommentItem(
                            postId = post.id,
                            comment = comment,
                            viewModel = viewModel,
                            currentLang = currentLang,
                            onReplyClick = { replyingToCommentId = comment.id }
                        )

                        // If replying to this comment
                        if (replyingToCommentId == comment.id) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("${strings.reply}...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.replyToComment(post.id, comment.id, replyText)
                                        replyText = ""
                                        replyingToCommentId = null
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Send Reply", tint = FacebookBlue)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Facebook Avatar Stickers Tray matching user screenshot 2
                    val avatarStickers = listOf(
                        "🙋‍♂️ Hi!" to "👋",
                        "💖 Love" to "🥰",
                        "🌹 Rose" to "🌹",
                        "✨ WOW!" to "🤩",
                        "👌 Very Nice" to "💯",
                        "👏 Bravo" to "👏",
                        "🎂 HBD" to "🎉"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        avatarStickers.forEach { (label, emoji) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable {
                                    newCommentText += " $emoji $label"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                                }
                            }
                        }
                    }

                    // Quick emoji picker bar for commenting (user requested emojis in comments!)
                    val quickEmojis = listOf("👍", "❤️", "🔥", "😄", "🥰", "👏", "💯", "😮", "✨", "🇧🇩")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickEmojis.forEach { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    newCommentText += emoji
                                }
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    // GIF tray if toggled
                    if (showGifTray) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("🎉 Congratulations!", "🔥 Awesome!", "❤️ Love it!", "👏 Well done!", "🌟 Outstanding!").forEach { gifText ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = FacebookBlue.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            viewModel.addComment(post.id, "[GIF] $gifText")
                                            showGifTray = false
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
                    }

                    // Comment Input Field with Camera, GIF, @ Mention, Emoji (matching Screenshot 2: "Comment as Niyon Islam Sohag")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image attachment icon
                        IconButton(
                            onClick = { newCommentText += " 📷 [Photo]" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = {
                                Text(
                                    text = "Comment as ${currentUser.fullName}",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    // GIF button
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clickable { showGifTray = !showGifTray }
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = "GIF",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    // Mention button (@)
                                    IconButton(
                                        onClick = { newCommentText += " @${currentUser.username}" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AlternateEmail,
                                            contentDescription = "Mention",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    // Emoji button
                                    IconButton(
                                        onClick = { newCommentText += " 😊" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SentimentSatisfied,
                                            contentDescription = "Emoji",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                viewModel.addComment(post.id, newCommentText)
                                newCommentText = ""
                            },
                            enabled = newCommentText.isNotBlank(),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (newCommentText.isNotBlank()) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Comment",
                                tint = if (newCommentText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
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
            videoUrl = post.images.firstOrNull() ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=1200",
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
            onDismiss = { showReactorsDialog = false }
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

@Composable
fun CommentItem(
    postId: String,
    comment: Comment,
    viewModel: FaceFeedViewModel,
    currentLang: AppLanguage,
    onReplyClick: () -> Unit
) {
    val strings = Translations.get(currentLang)
    var showCommentReactionPicker by remember { mutableStateOf(false) }
    var showOffenderLegalDialog by remember { mutableStateOf(false) }
    var legalActionStatus by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = comment.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                contentDescription = comment.author.fullName,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.author.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (comment.author.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 12.dp)
                            }
                        }
                        Text(
                            text = comment.text,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Comment reaction preview (if any reactions exist)
                if (comment.totalReactions > 0) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val topRx = if (comment.reactionCounts.isNotEmpty()) {
                            comment.reactionCounts.keys.take(2)
                        } else {
                            listOf(ReactionType.LIKE)
                        }
                        topRx.forEach { rx ->
                            Text(text = rx.emoji, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${comment.totalReactions}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 10 Reactions Picker for Comment (Floating when activated: 👍👎👌❤️🥰 😮😄😢😡💯)
                if (showCommentReactionPicker) {
                    Surface(
                        modifier = Modifier
                            .offset(y = (-4).dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Row 1 (👍 👎 👌 ❤️ 🥰)
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
                            // Row 2 (😮 😄 😢 😡 💯)
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

                // Action buttons: Timestamp, Reaction (10 types), 🔟 Picker, Reply
                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.timestamp,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Reaction button (shows active reaction or "Like")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (comment.userReaction == null) {
                                viewModel.reactToComment(postId, comment.id, ReactionType.LIKE)
                                com.example.util.AppSoundManager.playReactionChime()
                            } else {
                                viewModel.reactToComment(postId, comment.id, comment.userReaction)
                            }
                        }
                    ) {
                        if (comment.userReaction != null) {
                            Text(text = comment.userReaction.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (currentLang == AppLanguage.BENGALI) comment.userReaction.bengaliName else comment.userReaction.englishName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(comment.userReaction.colorHex)
                            )
                        } else {
                            Text(
                                text = strings.like,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Button to trigger 10-reaction picker
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

                    Text(
                        text = strings.reply,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onReplyClick() }
                    )

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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚖️", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "আইনি প্রোফাইল",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
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
                                        AsyncImage(
                                            model = comment.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
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

                // Replies list with 10 reactions support
                comment.replies.forEach { reply ->
                    var showReplyReactionPicker by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            AsyncImage(
                                model = reply.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                contentDescription = reply.author.fullName,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(
                                        text = reply.author.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = reply.text,
                                        fontSize = 12.sp
                                    )
                                }
                            }
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
    onDismiss: () -> Unit
) {
    var selectedReactionTab by remember { mutableStateOf<ReactionType?>(null) }
    val totalReactions = post.totalReactions

    val displayReactors = remember(post.reactors, post.reactionCounts) {
        val list = mutableListOf<ReactionUser>()
        list.addAll(post.reactors)

        val mockFriends = listOf(
            SampleData.friendTanvir to ReactionType.LOVE,
            SampleData.friendNusrat to ReactionType.CARE,
            SampleData.friendHasan to ReactionType.LIKE,
            SampleData.friendSamrat to ReactionType.OK,
            SampleData.friendArifur to ReactionType.HUNDRED,
            SampleData.friendSadia to ReactionType.WOW
        )
        for ((friend, rx) in mockFriends) {
            if (list.none { it.user.id == friend.id }) {
                list.add(ReactionUser(friend, rx, "1h ago"))
            }
        }
        list
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
                Text(
                    text = "প্রতিক্রিয়া প্রদানকারী (${totalReactions})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AsyncImage(
                                    model = reactor.user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                    contentDescription = reactor.user.fullName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = reactor.reaction.emoji,
                                    fontSize = 14.sp,
                                    modifier = Modifier.offset(x = 4.dp, y = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = reactor.user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
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

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { }
                        ) {
                            Text(
                                text = "বার্তা পাঠান",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
