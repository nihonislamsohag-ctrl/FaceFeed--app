package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

/**
 * Saved Videos & Posts Dialog
 * Per user request:
 * "Saved জারা যে কোনো ভিডিও ছেইভ করবে সেই ভিডিও এখানে এসে জমা হবে আর দেখা জাবে"
 */
@Composable
fun SavedVideosDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val savedPosts by viewModel.savedPosts.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // "all", "videos", "posts"
    var selectedPlayingPost by remember { mutableStateOf<Post?>(null) }

    val filteredList = remember(savedPosts, selectedFilter) {
        when (selectedFilter) {
            "videos" -> savedPosts.filter { it.isVideo || it.isReel }
            "posts" -> savedPosts.filter { !it.isVideo && !it.isReel }
            else -> savedPosts
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "সংরক্ষিত ভিডিও ও পোস্ট (Saved)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = CircleShape,
                            color = FacebookBlue.copy(alpha = 0.12f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "${savedPosts.size}টি",
                                color = FacebookBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Filter Tabs (সবগুলো, ভিডিও, পোস্ট)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("সব সংরক্ষিত (${savedPosts.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "videos",
                        onClick = { selectedFilter = "videos" },
                        label = { Text("ভিডিও (${savedPosts.count { it.isVideo || it.isReel }})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "posts",
                        onClick = { selectedFilter = "posts" },
                        label = { Text("পোস্ট (${savedPosts.count { !it.isVideo && !it.isReel }})") }
                    )
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "কোনো সংরক্ষিত আইটেম পাওয়া যায়নি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ফিডে যেকোনো পোস্ট বা ভিডিওর ৩-ডট মেনুতে ক্লিক করে সেভ করতে পারেন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList, key = { it.id }) { post ->
                            SavedPostCard(
                                post = post,
                                onPlayVideo = { selectedPlayingPost = post },
                                onUnsave = { viewModel.unsavePost(post.id) },
                                onShare = { viewModel.sharePost(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Video Player Dialog if user clicked Play
    selectedPlayingPost?.let { post ->
        VideoPlayerDialog(
            videoUrl = post.images.firstOrNull().orEmpty(),
            title = post.videoTitle ?: post.title ?: post.text.take(30),
            authorName = post.author.fullName,
            post = post,
            viewModel = viewModel,
            onDismiss = { selectedPlayingPost = null }
        )
    }
}

@Composable
private fun SavedPostCard(
    post: Post,
    onPlayVideo: () -> Unit,
    onUnsave: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val isVideo = post.isVideo || post.isReel
            val thumbnail = post.images.firstOrNull()

            if (isVideo && thumbnail != null) {
                // Video thumbnail with play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { onPlayVideo() }
                ) {
                    AsyncImage(
                        model = thumbnail,
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    // Play Button Circle
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.9f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Duration pill
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = post.videoDuration ?: "০২:৪৫",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Reel / Video Tag
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (post.isReel) Color(0xFFFF4081) else BangladeshGreen
                    ) {
                        Text(
                            text = if (post.isReel) "🎬 শর্ট ভিডিও" else "▶ সংরক্ষিত ভিডিও",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Info section
            Column(modifier = Modifier.padding(14.dp)) {
                // Author row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoFilmAvatar(
                        imageUrl = post.author.avatarUrl,
                        name = post.author.fullName,
                        modifier = Modifier.size(36.dp),
                        iconSize = 18.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.author.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${post.timestamp} • সংরক্ষিত",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Post Title / Caption
                val displayTitle = post.title ?: post.videoTitle ?: post.text
                if (displayTitle.isNotBlank()) {
                    Text(
                        text = displayTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons: Play/Watch, Share, Unsave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.isVideo || post.isReel) {
                        Button(
                            onClick = onPlayVideo,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভিডিও চালান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onShare,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("শেয়ার করুন", fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = onUnsave,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.BookmarkRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সংরক্ষণ মুছুন", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
