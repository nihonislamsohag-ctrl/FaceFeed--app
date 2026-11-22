package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.ReactionType
import com.example.model.Translations
import com.example.ui.components.AdBanner
import com.example.ui.components.FotoFilmAvatar
import com.example.ui.components.FotoFilmExoVideoSurface
import com.example.ui.components.GoogleAdConfig
import com.example.ui.components.PostActionBottomSheet
import com.example.ui.components.PostCard
import com.example.ui.components.StickyVideoBottomBanner
import com.example.ui.components.VideoPlaybackCoordinator
import com.example.ui.components.resolvePlayableVideoSource
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.MediaSaveHelper
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideosScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val posts by viewModel.posts.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val isLoadingMorePosts by viewModel.isLoadingMorePosts.collectAsState()
    val isFreeMode by viewModel.isFreeMode.collectAsState()
    val currentSim by viewModel.currentSimOperator.collectAsState()
    val isInternetConnected by viewModel.isInternetConnected.collectAsState()
    val videoListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Default directly into full-screen Facebook Reels vertical scroll when entering Watch / Reels
    var isReelsSelected by rememberSaveable { mutableStateOf(true) }
    var isReelsMuted by rememberSaveable { mutableStateOf(false) }

    val videoPosts = remember(posts, isInternetConnected) {
        if (!isInternetConnected) emptyList()
        else posts.filter { it.isVideo || it.isReel }
    }
    val reelList = remember(videoPosts) {
        val reelsFirst = videoPosts.filter { it.isReel }
        val otherVideos = videoPosts.filter { !it.isReel }
        reelsFirst + otherVideos
    }

    // Stop active video playback when leaving Watch / Reels or switching to list mode
    DisposableEffect(Unit) {
        onDispose {
            VideoPlaybackCoordinator.pauseActiveVideo()
        }
    }

    LaunchedEffect(isReelsSelected) {
        if (!isReelsSelected) {
            VideoPlaybackCoordinator.pauseActiveVideo()
        }
    }

    LaunchedEffect(videoListState, videoPosts.size, hasMorePosts, isLoadingMorePosts) {
        snapshotFlow { videoListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastIndex ->
                val count = videoPosts.size
                if (hasMorePosts && !isLoadingMorePosts && count > 0 && lastIndex >= count - 2) {
                    viewModel.loadMorePosts()
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isReelsSelected) Color.Black else MaterialTheme.colorScheme.background)
    ) {
        if (isReelsSelected) {
            // Full-Screen Facebook Reels Vertical Scroll (1 video per screen, swipe up for next)
            val pagerState = rememberPagerState(pageCount = { reelList.size.coerceAtLeast(1) })

            LaunchedEffect(pagerState.currentPage, reelList.size, hasMorePosts, isLoadingMorePosts) {
                if (hasMorePosts && !isLoadingMorePosts && reelList.isNotEmpty() && pagerState.currentPage >= reelList.size - 2) {
                    viewModel.loadMorePosts()
                }
            }

            if (isFreeMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 64.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.OndemandVideo,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "📶 ${currentSim.banglaName} ফ্রি মোড চালু আছে",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "৬ সিমের ফ্রি মোডে ছবি ও পোস্ট দেখা-দেওয়া, লাইক-কমেন্ট-শেয়ার-রিপ্লাই, ChatFilm টেক্সট-ছবি, লুডু কিং, ফিদা এআই ও Marketplace ফ্রি চলে।\n\nভিডিও বা শর্ট ভিডিও দেখতে নিচে থেকে 'ডাটা মোড' চালু করুন:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.setFreeMode(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("switch_to_data_mode_reels_btn")
                    ) {
                        Text("📶 ডাটা মোড চালু করুন (ভিডিও দেখুন)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else if (reelList.isNotEmpty()) {
                VerticalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    key = { index -> reelList[index].id },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 52.dp)
                        .testTag("reels_vertical_pager")
                ) { pageIndex ->
                    val reelPost = reelList[pageIndex]
                    val isCurrentPage = pagerState.currentPage == pageIndex
                    FullScreenReelPage(
                        post = reelPost,
                        viewModel = viewModel,
                        isCurrentPage = isCurrentPage,
                        isMuted = isReelsMuted,
                        onToggleMute = { isReelsMuted = !isReelsMuted }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এখনো কোনো আসল শর্ট ভিডিও নেই। উপরে + আপলোড চাপুন!",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Sleek Floating Top Overlay Bar (Reels / Watch Switch + Upload)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BangladeshRed,
                        modifier = Modifier
                            .clickable { isReelsSelected = true }
                            .testTag("reels_mode_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.SlowMotionVideo,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.reels,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Black.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clickable { isReelsSelected = false }
                            .testTag("watch_feed_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.watchVideos,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Upload Reel/Video button
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BangladeshGreen.copy(alpha = 0.9f),
                    modifier = Modifier
                        .clickable { viewModel.openMediaPicker(MediaPickerTarget.SHORT_VIDEO) }
                        .testTag("reels_upload_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = "Short Video তৈরি",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Short Video আপলোড",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Long Videos Feed Mode (with instant switch back to Reels Vertical Scroll)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 54.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { isReelsSelected = false },
                            label = { Text(strings.watchVideos, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        FilterChip(
                            selected = false,
                            onClick = { isReelsSelected = true },
                            label = { Text(strings.reels, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.openMediaPicker(MediaPickerTarget.LONG_VIDEO) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = FacebookBlue.copy(alpha = 0.15f),
                            contentColor = FacebookBlue
                        )
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ভিডিও আপলোড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isFreeMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = FacebookBlue,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ফ্রি মোডে ভিডিও দেখা বন্ধ রয়েছে",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ভিডিও দেখতে মোবাইল ডাটা বা ওয়াই-ফাই ব্যবহার করে ডাটা মোড চালু করুন।",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.setFreeMode(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("📶 ডাটা মোড চালু করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (videoPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এখনো কোনো আসল ভিডিও পোস্ট নেই। উপরে 'ভিডিও আপলোড' চাপুন!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                LazyColumn(
                    state = videoListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(videoPosts, key = { _, post -> post.id }) { index, post ->
                        PostCard(
                            post = post,
                            viewModel = viewModel
                        )

                        if ((index + 1) % 3 == 0) {
                            AdBanner(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                creativeIndex = (index / 3) + 1
                            )
                        }
                    }
                    item {
                        if (isLoadingMorePosts) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(26.dp), color = BangladeshGreen)
                            }
                        } else if (hasMorePosts && videoPosts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(onClick = { viewModel.loadMorePosts() }) {
                                    Text("আরও ৩০টি ভিডিও লোড করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                }
            }
        }

        // Sticky Video Bottom Banner
        StickyVideoBottomBanner(
            modifier = Modifier.align(Alignment.BottomCenter),
            adUnitId = GoogleAdConfig.BANNER_ID
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenReelPage(
    post: Post,
    viewModel: FaceFeedViewModel,
    isCurrentPage: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)

    val mediaSource = remember(post.id, post.images) {
        resolvePlayableVideoSource(post = post)
    }

    var isUserPaused by remember(post.id) { mutableStateOf(false) }
    var showPausePulse by remember(post.id) { mutableStateOf(false) }
    var isDownloadingReel by remember(post.id) { mutableStateOf(false) }
    var showCommentsSheet by remember(post.id) { mutableStateOf(false) }
    var showReelOptions by remember(post.id) { mutableStateOf(false) }
    var commentDraft by remember(post.id) { mutableStateOf("") }
    var currentSec by remember(post.id) { mutableIntStateOf(0) }
    var totalSec by remember(post.id) { mutableIntStateOf(0) }

    // Always auto-play when swiped onto this page
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            isUserPaused = false
        }
    }

    // Brief play/pause feedback badge when tapping center of video
    LaunchedEffect(showPausePulse) {
        if (showPausePulse) {
            delay(750)
            showPausePulse = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isUserPaused = !isUserPaused
                showPausePulse = true
            }
    ) {
        // Real Media3 ExoPlayer Video & Crystal-Clear Audio Surface
        FotoFilmExoVideoSurface(
            ownerKey = "reel_${post.id}",
            videoUrl = mediaSource.videoUrl,
            thumbnailUrl = mediaSource.thumbnailUrl,
            isActivePage = isCurrentPage,
            isPaused = isUserPaused,
            isMuted = isMuted,
            fillCrop = true,
            repeatLoop = true,
            onProgressUpdate = { cur, tot ->
                currentSec = cur
                totalSec = tot
            },
            modifier = Modifier.fillMaxSize()
        )

        // Subtle top & bottom gradient scrims for crisp readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.35f),
                            0.25f to Color.Transparent,
                            0.62f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Play / Pause Indicator on tap
        AnimatedVisibility(
            visible = isUserPaused || showPausePulse,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUserPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isUserPaused) "Play" else "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Right-Side Vertical Interaction Column (Like, Comment, Share, Download, Menu)
        // Tapping Like or Comment NEVER navigates away or stops the video!
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Like Button (keeps video playing right here)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val isReacted = post.userReaction != null
                IconButton(
                    onClick = {
                        val rx = if (post.userReaction == ReactionType.LIKE) ReactionType.LOVE else ReactionType.LIKE
                        viewModel.reactToPost(post.id, rx)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isReacted) FacebookBlue.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.42f))
                        .testTag("reel_like_button_${post.id}")
                ) {
                    if (isReacted) {
                        Text(text = post.userReaction?.emoji ?: "👍", fontSize = 22.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    text = "${post.totalReactions}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment Button (opens in-place bottom sheet while video continues playing!)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val commentCount = maxOf(post.comments.size, post.cloudCommentCount)
                IconButton(
                    onClick = { showCommentsSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.42f))
                        .testTag("reel_comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "$commentCount",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        viewModel.sharePost(post.id)
                        Toast.makeText(context, "↗️ শর্ট ভিডিওটি শেয়ার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.42f))
                        .testTag("reel_share_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "${post.shareCount}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Download Video Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        if (isDownloadingReel) return@IconButton
                        isDownloadingReel = true
                        Toast.makeText(context, "📥 '${post.videoTitle ?: post.title ?: "Short Video"}' ডাউনলোড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            val result = MediaSaveHelper.saveMediaToGallery(
                                context = context,
                                mediaUrl = mediaSource.videoUrl,
                                isVideo = true
                            )
                            result.onSuccess {
                                Toast.makeText(context, "✅ শর্ট ভিডিও গ্যালারিতে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "⚠️ ডাউনলোড ব্যর্থ: ${err.message ?: "পুনরায় চেষ্টা করুন"}", Toast.LENGTH_SHORT).show()
                            }
                            isDownloadingReel = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDownloadingReel) BangladeshGreen.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.42f))
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download Video",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isDownloadingReel) "..." else "ডাউনলোড",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Owner Free Video Booster Button (Niyon Islam Sohag)
            if (viewModel.isCurrentUserNiyonIslamSohag()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            viewModel.freeBoostAnyVideo(post.id)
                            Toast.makeText(context, "🚀 Niyon Islam Sohag ফ্রী ভিডিও বুস্টার: '${post.videoTitle ?: post.title ?: "ভিডিও"}' ১M+ ভিউসহ বুস্ট করা হয়েছে!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE040FB).copy(alpha = 0.55f))
                    ) {
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = "Free Boost Video",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "ফ্রী বুস্ট 🚀",
                        color = Color(0xFFEA80FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 3-Dots Options Button
            IconButton(
                onClick = { showReelOptions = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.42f))
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        // Bottom-Left Creator Info, Video Title, Caption & Dedicated Sound Mute/Unmute Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoFilmAvatar(
                    imageUrl = post.author.avatarUrl,
                    name = post.author.fullName,
                    contentDescription = post.author.fullName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.author.fullName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (post.author.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    com.example.ui.components.FidaVerifiedBadge(size = 15.dp)
                }
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
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val videoHeading = post.videoTitle?.takeIf { it.isNotBlank() } ?: post.title?.takeIf { it.isNotBlank() }
            if (videoHeading != null) {
                Text(
                    text = videoHeading,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Row + Facebook-style Sound Toggle Button right at the bottom of the video
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isMuted) BangladeshRed.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clickable { onToggleMute() }
                        .testTag("reel_sound_toggle_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "সাউন্ড চালু করুন" else "সাউন্ড বন্ধ করুন",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isMuted) "সাউন্ড বন্ধ" else "সাউন্ড চালু",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val timeLabel = if (totalSec > 0) {
                        String.format(" • %02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)
                    } else ""
                    Text(
                        text = "Original Audio$timeLabel",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Thin real-time video progress bar at the very bottom edge of the Reel
        if (totalSec > 0) {
            LinearProgressIndicator(
                progress = { (currentSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter),
                color = BangladeshGreen,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
    }

    // In-Place Comment Sheet (video keeps playing smoothly behind it!)
    if (showCommentsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "কমেন্ট (${maxOf(post.comments.size, post.cloudCommentCount)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { showCommentsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close comments")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                if (post.comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "প্রথম কমেন্টটি আপনিই করুন! 💬",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(post.comments, key = { it.id }) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                FotoFilmAvatar(
                                    imageUrl = comment.author.avatarUrl,
                                    name = comment.author.fullName,
                                    contentDescription = comment.author.fullName,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = comment.author.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = comment.text,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentDraft,
                        onValueChange = { commentDraft = it },
                        placeholder = { Text(strings.writeComment, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reel_comment_input_${post.id}"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val clean = commentDraft.trim()
                            if (clean.isNotEmpty()) {
                                viewModel.addComment(post.id, clean)
                                commentDraft = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = BangladeshGreen),
                        modifier = Modifier.testTag("reel_comment_send_${post.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Comment", tint = Color.White)
                    }
                }
            }
        }
    }

    if (showReelOptions) {
        PostActionBottomSheet(
            post = post,
            viewModel = viewModel,
            onDismiss = { showReelOptions = false }
        )
    }
}
