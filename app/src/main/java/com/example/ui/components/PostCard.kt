package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
    val strings = Translations.get(currentLang)
    var showReactionPicker by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showVideoPlayer by remember { mutableStateOf(false) }
    val currentPlayingTrackId by com.example.util.AppSoundManager.currentPlayingTrackId.collectAsState()
    val isAudioEnginePlaying by com.example.util.AppSoundManager.isAudioPlaying.collectAsState()
    val audioProgress by com.example.util.AppSoundManager.playbackProgress.collectAsState()

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
                    modifier = Modifier.weight(1f)
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
                        }
                    }
                }

                // Post options & close buttons (like Facebook screenshot)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* Menu */ },
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

            // Reaction Counts Bar (Reactions summary, comments count, shares count)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top reactions preview
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val topReactions = post.reactionCounts.keys.take(3)
                    topReactions.forEach { rx ->
                        Text(text = rx.emoji, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.totalReactions}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comments & Shares count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${post.comments.size} ${strings.comment}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { showComments = !showComments }
                    )
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${post.shareCount} ${strings.share}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            .offset(y = (-90).dp, x = 12.dp)
                            .shadow(10.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Row 1 (উপরে ৫টি): 👍 👎 👌 ❤️ 🥰
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val top5 = listOf(
                                    ReactionType.LIKE,
                                    ReactionType.DISLIKE,
                                    ReactionType.OK,
                                    ReactionType.LOVE,
                                    ReactionType.CARE
                                )
                                top5.forEach { reaction ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.reactToPost(post.id, reaction)
                                                com.example.util.AppSoundManager.playReactionChime()
                                                showReactionPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = reaction.emoji, fontSize = 22.sp)
                                    }
                                }
                            }

                            // Row 2 (নিচে ৫টি): 😮 😄 😢 😡 💯
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bottom5 = listOf(
                                    ReactionType.WOW,
                                    ReactionType.HAHA,
                                    ReactionType.SAD,
                                    ReactionType.ANGRY,
                                    ReactionType.HUNDRED
                                )
                                bottom5.forEach { reaction ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.reactToPost(post.id, reaction)
                                                com.example.util.AppSoundManager.playReactionChime()
                                                showReactionPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = reaction.emoji, fontSize = 22.sp)
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
                        onClick = { viewModel.sharePost(post.id) },
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
                            comment = comment,
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

                    // Comment Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text(strings.writeComment, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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

    // Fullscreen Image Dialog
    if (fullscreenImageUrl != null) {
        AlertDialog(
            onDismissRequest = { fullscreenImageUrl = null },
            confirmButton = {
                TextButton(onClick = { fullscreenImageUrl = null }) {
                    Text(strings.cancel)
                }
            },
            text = {
                AsyncImage(
                    model = fullscreenImageUrl,
                    contentDescription = "Fullscreen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    // Video Player Dialog (Smooth full playback)
    if (showVideoPlayer) {
        VideoPlayerDialog(
            videoUrl = post.images.firstOrNull() ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=1200",
            title = post.videoTitle ?: post.title ?: post.text.take(30),
            authorName = post.author.fullName,
            post = post,
            onDismiss = { showVideoPlayer = false }
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
    comment: Comment,
    currentLang: AppLanguage,
    onReplyClick: () -> Unit
) {
    val strings = Translations.get(currentLang)

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

            Column {
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

                Row(
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = comment.timestamp,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.like,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { /* Like comment */ }
                    )
                    Text(
                        text = strings.reply,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onReplyClick() }
                    )
                }

                // Replies list
                comment.replies.forEach { reply ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
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
                }
            }
        }
    }
}
