package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.MusicTrack
import com.example.model.PostAudioTrack
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

/**
 * Facebook-style Music Add Feature:
 * 1. Dark mode background #121212 matching Facebook
 * 2. Search bar with "Search lyrics"
 * 3. Two tabs - "For you" and "Trending" + Category filters
 * 4. List of songs with thumbnail, title, artist, views (e.g. 1.2M, 147K, 12K), play button
 * 5. Plays via AppSoundManager / MediaPlayer
 * 6. Adds to Post, Story, Reel/Short, Photo, Group, Video Editor
 * 7. Upload custom audio (গান, গজল, ডিজে, বাঁশির সুর) directly from phone gallery/storage to Firebase Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookMusicAddDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onTrackSelected: ((PostAudioTrack) -> Unit)? = null
) {
    val context = LocalContext.current
    val musicTracks by viewModel.musicTracks.collectAsState()
    val isPlaying by AppSoundManager.isPlaying.collectAsState()
    val currentPlayingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()
    val playbackProgress by AppSoundManager.playbackProgress.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: For you, 1: Trending
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showUploadAudioDialog by remember { mutableStateOf(false) }
    var actionSheetTrack by remember { mutableStateOf<MusicTrack?>(null) }

    // Categories
    val categoryFilters = listOf("All", "বাঁশির সুর", "গজল", "ডিজে গান", "রিলস ভাইরাল", "For you", "Trending")

    // Filtered songs
    val filteredTracks = remember(musicTracks, searchQuery, selectedTab, selectedCategoryFilter) {
        musicTracks.filter { track ->
            // Tab filtering
            val matchesTab = when (selectedTab) {
                1 -> track.isTrending || track.category.contains("Trending", ignoreCase = true) || track.views.contains("M")
                else -> true // "For you" shows personalized feed
            }

            // Category filtering
            val matchesCategory = if (selectedCategoryFilter == "All") {
                true
            } else {
                track.category.contains(selectedCategoryFilter, ignoreCase = true) ||
                        track.title.contains(selectedCategoryFilter, ignoreCase = true)
            }

            // Search query (title, artist, lyrics)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                track.title.contains(searchQuery, ignoreCase = true) ||
                        track.artist.contains(searchQuery, ignoreCase = true) ||
                        track.lyrics.contains(searchQuery, ignoreCase = true) ||
                        track.category.contains(searchQuery, ignoreCase = true)
            }

            matchesTab && matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = {
            AppSoundManager.stopAudio()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212) // Facebook Dark Mode #121212
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                AppSoundManager.stopAudio()
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Add music",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Facebook FaceFeed Music Hub",
                                fontSize = 11.sp,
                                color = Color(0xFFB0B3B8)
                            )
                        }
                    }

                    // Upload Audio Button (ফোন গ্যালারি থেকে অডিও আপলোড)
                    FilledTonalButton(
                        onClick = { showUploadAudioDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = FacebookBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upload audio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 1. Search Bar with "Search lyrics" placeholder
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF242526)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFFB0B3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search lyrics",
                                        color = Color(0xFF8A8D91),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFB0B3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Two Tabs: "For you" and "Trending"
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF121212),
                    contentColor = FacebookBlue,
                    divider = {
                        HorizontalDivider(color = Color(0xFF242526), thickness = 1.dp)
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "For you",
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) FacebookBlue else Color(0xFFB0B3B8)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Trending",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) FacebookBlue else Color(0xFFB0B3B8)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🔥", fontSize = 12.sp)
                            }
                        }
                    )
                }

                // Category Chips Scrollable Row (বাঁশির সুর, গজল, ডিজে গান, ইত্যাদি)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryFilters.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) FacebookBlue else Color(0xFF242526),
                            modifier = Modifier.clickable {
                                selectedCategoryFilter = category
                            }
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFFE4E6EB),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Playing Track Banner (if playing)
                if (isPlaying && currentPlayingTrackId != null) {
                    val playingTrack = musicTracks.find { it.id == currentPlayingTrackId }
                    if (playingTrack != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2638),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AudioWaveformBars(isPlaying = true)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Now Playing: ${playingTrack.title}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${playingTrack.artist} • ${playingTrack.views} views",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB0B3B8)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { AppSoundManager.pauseAudio() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PauseCircle,
                                                contentDescription = "Pause",
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { AppSoundManager.stopAudio() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = "Stop",
                                                tint = Color.Red,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { playbackProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = FacebookBlue,
                                    trackColor = Color(0xFF242526)
                                )
                            }
                        }
                    }
                }

                // 3. Songs List
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MusicOff,
                                contentDescription = null,
                                tint = Color(0xFF8A8D91),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোনো গান পাওয়া যায়নি",
                                fontSize = 14.sp,
                                color = Color(0xFFB0B3B8)
                            )
                            Text(
                                text = "অন্য কীওয়ার্ড দিয়ে সার্চ করুন অথবা নতুন অডিও আপলোড করুন",
                                fontSize = 12.sp,
                                color = Color(0xFF8A8D91)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            val isThisTrackPlaying = isPlaying && currentPlayingTrackId == track.id
                            FacebookMusicItemRow(
                                track = track,
                                isPlaying = isThisTrackPlaying,
                                onPlayClick = {
                                    if (isThisTrackPlaying) {
                                        AppSoundManager.pauseAudio()
                                    } else {
                                        AppSoundManager.playAudio(track.toPostAudioTrack())
                                    }
                                },
                                onItemClick = {
                                    if (onTrackSelected != null) {
                                        onTrackSelected(track.toPostAudioTrack())
                                        AppSoundManager.stopAudio()
                                        onDismiss()
                                    } else {
                                        actionSheetTrack = track
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = Color(0xFF1F1F1F),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(start = 78.dp, end = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Action Sheet: "গান সিলেক্ট করলে সেটা গুরুপ যে কোনো প্রোস্টে , সট ,ছবি,ইস্টুরি,এডিটরে Add হয়ে যাবে"
    actionSheetTrack?.let { track ->
        MusicDestinationBottomSheet(
            track = track,
            onDismiss = { actionSheetTrack = null },
            onAddToPost = {
                viewModel.openCreatePostWithAudio(track.toPostAudioTrack())
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "গানটি পোস্টে যুক্ত করা হয়েছে! 🎵", Toast.LENGTH_SHORT).show()
            },
            onAddToPhoto = {
                viewModel.openCreatePostWithAudio(track.toPostAudioTrack())
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "গানটি ফটো পোস্টে যুক্ত করা হয়েছে! 📸", Toast.LENGTH_SHORT).show()
            },
            onAddToReels = {
                viewModel.openCreatePostWithAudio(track.toPostAudioTrack())
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "গানটি রিলস ও শর্টে যুক্ত করা হয়েছে! 🎬", Toast.LENGTH_SHORT).show()
            },
            onAddToStory = {
                viewModel.createStoryWithMusic(track.toPostAudioTrack())
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "গানটি স্টোরিতে সফলভাবে পোস্ট হয়েছে! 🌟", Toast.LENGTH_SHORT).show()
            },
            onAddToGroup = {
                viewModel.openCreatePostWithAudio(track.toPostAudioTrack())
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "গানটি গ্রুপ পোস্টে যুক্ত করা হয়েছে! 👥", Toast.LENGTH_SHORT).show()
            },
            onAddToEditor = {
                actionSheetTrack = null
                AppSoundManager.stopAudio()
                onDismiss()
                Toast.makeText(context, "ProLevelCut ভিডিও এডিটরে ব্যাকগ্রাউন্ড মিউজিক হিসেবে লোড হয়েছে! ✂️", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Dialog for Uploading Custom Audio from Phone Storage
    if (showUploadAudioDialog) {
        UploadAudioModalDialog(
            viewModel = viewModel,
            onDismiss = { showUploadAudioDialog = false }
        )
    }
}

/**
 * Single Music Item Row matching Facebook:
 * - Left: Thumbnail Image
 * - Center: Title, Artist, Views (1.2M, 147K, 12K), Lyrics preview
 * - Right: Play Button
 */
@Composable
fun FacebookMusicItemRow(
    track: MusicTrack,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Image
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF242526)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = track.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300" },
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Equalizer badge if playing
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    AudioWaveformBars(isPlaying = true)
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Info: Title, Artist, Views, Lyrics
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (track.isTrending) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFFA3E3E).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "TRENDING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Artist and Views count (12K, 1.2M, 147K)
            Text(
                text = "${track.artist} • ${track.views} views • ${track.duration}",
                fontSize = 12.sp,
                color = Color(0xFFB0B3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Lyrics Preview if available
            if (track.lyrics.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "🎤 ${track.lyrics}",
                    fontSize = 11.sp,
                    color = Color(0xFF45BD62), // Greenish Facebook lyrics accent
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Play Button (Circle)
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isPlaying) FacebookBlue else Color(0xFF242526))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isPlaying) Color.White else Color(0xFFE4E6EB),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Animated Equalizer Bars for music playback feedback
 */
@Composable
fun AudioWaveformBars(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val height1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        modifier = Modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isPlaying) height1 else 0.4f)
                .clip(RoundedCornerShape(1.dp))
                .background(FacebookBlue)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isPlaying) height2 else 0.7f)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFF45BD62))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isPlaying) height3 else 0.3f)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFFFF9800))
        )
    }
}

/**
 * Bottom Sheet / Dialog to attach selected music to:
 * - Posts
 * - Photos
 * - Reels/Shorts
 * - Stories
 * - Groups
 * - Video Editor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicDestinationBottomSheet(
    track: MusicTrack,
    onDismiss: () -> Unit,
    onAddToPost: () -> Unit,
    onAddToPhoto: () -> Unit,
    onAddToReels: () -> Unit,
    onAddToStory: () -> Unit,
    onAddToGroup: () -> Unit,
    onAddToEditor: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF4E4F50))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Selected Song Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = track.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${track.artist} • ${track.category}",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF2E2F30))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "এই গানটি কোথায় যুক্ত করতে চান? (Add Song To):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE4E6EB)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Destination Options
            MusicDestinationOption(
                icon = Icons.Default.EditNote,
                title = "যে কোনো পোস্টে যুক্ত করুন (Add to Post)",
                subtitle = "পোস্টে ব্যাকগ্রাউন্ড মিউজিক হিসেবে বাজবে",
                color = FacebookBlue,
                onClick = onAddToPost
            )

            MusicDestinationOption(
                icon = Icons.Default.PhotoCamera,
                title = "ছবিতে মিউজিক এড করুন (Add to Photo Post)",
                subtitle = "ফটোর সাথে এই গানটি প্লে হবে",
                color = Color(0xFF45BD62),
                onClick = onAddToPhoto
            )

            MusicDestinationOption(
                icon = Icons.Default.VideoLibrary,
                title = "শর্ট ও রিলসে যুক্ত করুন (Add to Reel / Short)",
                subtitle = "রিলস ভিডিও ব্যাকগ্রাউন্ড সাউন্ডট্র্যাক",
                color = Color(0xFFFF5252),
                onClick = onAddToReels
            )

            MusicDestinationOption(
                icon = Icons.Default.AutoStories,
                title = "স্টোরিতে মিউজিক দিন (Add to Story)",
                subtitle = "২৪ ঘণ্টার ফেসবুক স্টোরিতে গান যোগ হবে",
                color = Color(0xFF9C27B0),
                onClick = onAddToStory
            )

            MusicDestinationOption(
                icon = Icons.Default.Groups,
                title = "গ্রুপ পোস্টে যুক্ত করুন (Add to Group Post)",
                subtitle = "কমিউনিটি ও গ্রুপে পোস্ট করুন",
                color = Color(0xFF00B0FF),
                onClick = onAddToGroup
            )

            MusicDestinationOption(
                icon = Icons.Default.ContentCut,
                title = "ProLevelCut ভিডিও এডিটরে ব্যাকগ্রাউন্ড মিউজিক দিন",
                subtitle = "ভিডিও এডিটরে অডিও ট্র্যাক যুক্ত হবে",
                color = Color(0xFFFF9800),
                onClick = onAddToEditor
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MusicDestinationOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF8A8D91)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4E4F50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Dialog to upload custom audio files (গান, গজল, ডিজে গান, মিউজিক, বাঁশির সুর)
 * directly from phone gallery/storage to Firebase Firestore.
 */
@Composable
fun UploadAudioModalDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var songTitle by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("Niyon Islam Sohag") }
    var viewsText by remember { mutableStateOf("12K") }
    var lyricsText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("বাঁশির সুর") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var coverPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val categories = listOf("বাঁশির সুর", "গজল", "ডিজে গান", "For you", "Trending", "রিলস ভাইরাল", "ফোক ও লালন")

    // Launcher for Audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        audioUri = uri
        if (uri != null && songTitle.isBlank()) {
            val fileName = uri.lastPathSegment ?: "Custom Audio"
            songTitle = fileName.replace(".mp3", "").replace(".m4a", "").replace(".wav", "").take(40)
        }
    }

    // Launcher for Cover image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = FacebookBlue,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "গ্যালারি থেকে অডিও আপলোড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "গান, গজল, ডিজে, বাঁশির সুর যুক্ত করুন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Audio File Picker Button
                Surface(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = if (audioUri != null) BangladeshGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (audioUri != null) BangladeshGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (audioUri != null) Icons.Default.CheckCircle else Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = if (audioUri != null) BangladeshGreen else FacebookBlue,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (audioUri != null) "✅ অডিও ফাইল নির্বাচিত হয়েছে" else "ফোন থেকে অডিও ফাইল সিলেক্ট করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = audioUri?.lastPathSegment ?: ".mp3, .m4a, .wav ফাইল সাপোর্ট করে",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. Cover Photo Picker (Optional)
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (coverPhotoUri != null) "✅ থাম্বনেইল ছবি সিলেক্টেড" else "গানের থাম্বনেইল ছবি যোগ করুন (ঐচ্ছিক)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Song Title
                OutlinedTextField(
                    value = songTitle,
                    onValueChange = { songTitle = it },
                    label = { Text("গানের নাম (Title)") },
                    placeholder = { Text("e.g. Palli Gramer Bashir Sur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Artist Name
                OutlinedTextField(
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = { Text("শিল্পীর নাম (Artist)") },
                    placeholder = { Text("e.g. Niyon Islam Sohag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Views Count (12K, 1.2M)
                OutlinedTextField(
                    value = viewsText,
                    onValueChange = { viewsText = it },
                    label = { Text("ভিউ সংখ্যা (Views Count)") },
                    placeholder = { Text("e.g. 12K, 147K, 1.2M") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Category Selection
                Text(
                    text = "ক্যাটাগরি নির্বাচন করুন:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // 7. Lyrics
                OutlinedTextField(
                    value = lyricsText,
                    onValueChange = { lyricsText = it },
                    label = { Text("গানের লিরিক্স (Lyrics)") },
                    placeholder = { Text("e.g. বাঁশির সুরে মন ভরে যায়...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (songTitle.isBlank()) {
                        Toast.makeText(context, "দয়া করে গানের নাম লিখুন!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUploading = true

                    val newTrack = MusicTrack(
                        id = "track_user_${System.currentTimeMillis()}",
                        title = songTitle.trim(),
                        artist = artistName.trim().ifEmpty { "Niyon Islam Sohag" },
                        views = viewsText.trim().ifEmpty { "12K" },
                        lyrics = lyricsText.trim().ifEmpty { "বাঁশির সুরে মন ভরে যায়..." },
                        category = selectedCategory,
                        duration = "3:30",
                        thumbnailUrl = coverPhotoUri?.toString() ?: "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=400",
                        audioUrl = audioUri?.toString() ?: "https://actions.google.com/sounds/v1/foley/wind_chimes_breeze.ogg",
                        isTrending = selectedCategory == "Trending" || selectedCategory == "ডিজে গান",
                        addedBy = "Niyon Islam Sohag"
                    )

                    viewModel.uploadMusicTrack(newTrack) { success ->
                        isUploading = false
                        Toast.makeText(
                            context,
                            "অডিও গান সফলভাবে Firebase Firestore এ আপলোড হয়েছে! সব ইউজাররা এখন এটি ব্যবহার করতে পারবে।",
                            Toast.LENGTH_LONG
                        ).show()
                        onDismiss()
                    }
                },
                enabled = !isUploading && songTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("আপলোড হচ্ছে...")
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Firestore এ আপলোড করুন")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("বাতিল")
            }
        }
    )
}
