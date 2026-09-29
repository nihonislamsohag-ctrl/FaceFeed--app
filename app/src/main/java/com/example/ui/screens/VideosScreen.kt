package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.Translations
import com.example.ui.components.PostCard
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

@Composable
fun VideosScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val posts by viewModel.posts.collectAsState()

    var isReelsSelected by remember { mutableStateOf(false) }

    val videoPosts = posts.filter { it.isVideo }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header: "ভিডিও (Videos)" vs "রিলস (Reels)" + Upload Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isReelsSelected,
                    onClick = { isReelsSelected = false },
                    label = { Text(strings.watchVideos, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = isReelsSelected,
                    onClick = { isReelsSelected = true },
                    label = { Text(strings.reels, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Upload Video/Reel Action Button
            FilledTonalButton(
                onClick = {
                    if (isReelsSelected) {
                        viewModel.openMediaPicker(MediaPickerTarget.SHORT_VIDEO)
                    } else {
                        viewModel.openMediaPicker(MediaPickerTarget.LONG_VIDEO)
                    }
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isReelsSelected) BangladeshRed.copy(alpha = 0.15f) else FacebookBlue.copy(alpha = 0.15f),
                    contentColor = if (isReelsSelected) BangladeshRed else FacebookBlue
                )
            ) {
                Icon(
                    imageVector = if (isReelsSelected) Icons.Default.VideoCall else Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isReelsSelected) "রিল তৈরি" else "ভিডিও আপলোড",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isReelsSelected) {
            // Full Reels experience
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val reelList = posts.filter { it.isReel }.ifEmpty { posts.take(3) }
                items(reelList) { post ->
                    ReelCardItem(post = post, viewModel = viewModel)
                }
            }
        } else {
            // Long Videos Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val listToShow = videoPosts.ifEmpty { posts }
                items(listToShow) { post ->
                    PostCard(
                        post = post,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ReelCardItem(
    post: Post,
    viewModel: FaceFeedViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .padding(vertical = 4.dp)
            .background(Color.Black)
    ) {
        // Reel Video Image Thumbnail
        AsyncImage(
            model = post.images.firstOrNull() ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            contentDescription = "Reel",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Play icon overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
        }

        // Right-Side Interaction Column (Like, Comment, Share, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like (10 reactions)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.reactToPost(post.id, com.example.model.ReactionType.LOVE) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Text(text = post.userReaction?.emoji ?: "❤️", fontSize = 22.sp)
                }
                Text(
                    text = "${post.totalReactions}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { /* Open comments */ },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = "${post.comments.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { viewModel.sharePost(post.id) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = "${post.shareCount}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Creator Info & Title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = post.author.fullName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.author.fullName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BangladeshGreen
                ) {
                    Text(
                        text = "Follow",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.text,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2,
                lineHeight = 18.sp
            )

            // Audio line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Audio - ${post.author.fullName}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
