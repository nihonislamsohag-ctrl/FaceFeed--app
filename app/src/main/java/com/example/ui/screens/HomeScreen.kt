package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Translations
import com.example.ui.components.AdBanner
import com.example.ui.components.FotoFilmAvatar
import com.example.ui.components.PostCard
import com.example.ui.components.StoryTray
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val isLoadingMorePosts by viewModel.isLoadingMorePosts.collectAsState()
    val isHomeRefreshing by viewModel.isHomeRefreshing.collectAsState()
    val postUploadProgress by viewModel.postUploadProgress.collectAsState()
    val cloudQuotaNotice by viewModel.cloudQuotaNotice.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val isInternetConnected by viewModel.isInternetConnected.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val visiblePosts = remember(posts, blockedUserIds, isInternetConnected) {
        if (!isInternetConnected) emptyList()
        else posts.filter { !blockedUserIds.contains(it.author.id) && !viewModel.isUserBlocked(it.author.id) }
    }

    // Real Phone Gallery Launcher for Feed Posts
    val phoneGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.openCreatePostWithImages(uris.map { it.toString() })
        } else {
            viewModel.setShowCreatePostDialog(true)
        }
    }

    // Auto-scroll to top when a new post upload begins so upload progress below FotoFilm is visible
    LaunchedEffect(postUploadProgress?.postId) {
        if (postUploadProgress != null && listState.firstVisibleItemIndex > 1) {
            listState.animateScrollToItem(0)
        }
    }

    // Clear Home badge when user scrolls to top
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex == 0) {
            viewModel.clearHomeBadge()
        }
    }

    // Cursor-based infinite scroll: load next 30 posts when nearing the end
    LaunchedEffect(listState, visiblePosts.size, hasMorePosts, isLoadingMorePosts) {
        snapshotFlow {
            val info = listState.layoutInfo
            (info.visibleItemsInfo.lastOrNull()?.index ?: -1) to info.totalItemsCount
        }.collect { (lastIndex, totalCount) ->
            if (hasMorePosts && !isLoadingMorePosts && visiblePosts.isNotEmpty() && totalCount > 0 && lastIndex >= totalCount - 3) {
                viewModel.loadMorePosts()
            }
        }
    }

    // =========================================================================
    // FACEBOOK-STYLE FULL SCROLLABLE HOME FEED WITH PULL-TO-REFRESH:
    // Pull down at the top to show circular Facebook spinner and load fresh posts!
    // =========================================================================
    PullToRefreshBox(
        isRefreshing = isHomeRefreshing,
        onRefresh = { viewModel.refreshHomeFeed() },
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isHomeRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = FacebookBlue,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item(key = "home_composer_card") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Icon: Clicking takes directly into Profile Screen!
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { viewModel.setTab(AppTab.PROFILE) }
                    ) {
                        FotoFilmAvatar(
                            imageUrl = currentUser.avatarUrl,
                            name = currentUser.fullName,
                            contentDescription = currentUser.fullName,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setShowCreatePostDialog(true) }
                    ) {
                        Text(
                            text = strings.whatsOnYourMind,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Photo Icon Button on the right of the input pill (Direct Phone Gallery Launch)
                    IconButton(
                        onClick = { phoneGalleryLauncher.launch("image/*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Photos",
                            tint = Color(0xFF45BD62),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // 3 Actions: Live, Photo/Video, Feeling/Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Live Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.openLiveStudio() }
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = BangladeshRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.live, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Photo/Video Button (Direct Phone Gallery Launch)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { phoneGalleryLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF45BD62), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.photosAndVideos, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Feeling / Status Text Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setShowCreatePostDialog(true) }
                    ) {
                        Icon(Icons.Default.Mood, contentDescription = null, tint = Color(0xFFF7B125), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.feeling, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        } // End home_composer_card item

        // 1. Stories Tray (Add Story + Friends' stories - scrolls cleanly with feed)
        item(key = "home_stories_tray") {
            StoryTray(
                viewModel = viewModel,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // 2. FotoFilm লোগু ও প্রোফাইল কার্ড (Welcome Bangladesh 🇧🇩 - Niyon Islam Sohag)
        item(key = "home_welcome_profile_card") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = BangladeshGreen,
                    shadowElevation = 2.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fotofilm_logo),
                        contentDescription = "FotoFilm Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Foto",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = BangladeshGreen
                        )
                        Text(
                            text = "Film",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = BangladeshRed
                        )
                    }
                    Text(
                        text = "Welcome Bangladesh 🇧🇩",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Niyon Islam Sohag",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        viewModel.setTab(AppTab.PROFILE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("প্রোফাইল", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 2.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.8.dp
        )
        } // End home_welcome_profile_card item

        // 3. Post Upload Progress Log Card (ইস্টুরি ও FotoFilm-এর নিচে লাইভ আপলোড ও বাকি %)
        item(key = "home_post_upload_progress_card") {
            AnimatedVisibility(
                visible = postUploadProgress != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                postUploadProgress?.let { upload ->
                    val remainingPercent = (100 - upload.progressPercent).coerceIn(0, 100)
                    val animatedFraction by animateFloatAsState(
                        targetValue = (upload.progressPercent / 100f).coerceIn(0f, 1f),
                        label = "post_upload_progress_anim"
                    )
                    val accentColor = if (upload.isCompleted) BangladeshGreen else FacebookBlue

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("home_upload_progress_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.35f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // সামনে FotoFilm লোগো + গোলাকার প্রোগ্রেস রিং
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { animatedFraction },
                                        modifier = Modifier.fillMaxSize(),
                                        color = accentColor,
                                        trackColor = accentColor.copy(alpha = 0.16f),
                                        strokeWidth = 3.dp
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_fotofilm_logo),
                                        contentDescription = "FotoFilm Upload Logo",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (upload.isCompleted) {
                                                "✅ ${upload.mediaTypeLabel} আপলোড সম্পন্ন!"
                                            } else {
                                                "⬆️ ${upload.mediaTypeLabel} আপলোড হচ্ছে..."
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = accentColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (upload.isCompleted) {
                                                    "100% সম্পন্ন"
                                                } else {
                                                    "বাকি ${remainingPercent}%"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${upload.postPreview} • ${upload.statusLog}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "আপলোড হয়েছে: ${upload.progressPercent}%  •  বাকি আছে: ${remainingPercent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.dismissPostUploadProgress() },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "আপলোড বার লুকান",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = accentColor,
                                trackColor = accentColor.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // Quota limit notice banner (if Firebase Spark daily limit reached)
        if (cloudQuotaNotice != null) {
            item(key = "home_quota_notice") {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Quota Warning", tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cloudQuotaNotice!!,
                            fontSize = 11.sp,
                            color = Color(0xFF4E342E),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(com.example.data.FIREBASE_BLAZE_UPGRADE_URL)
                                } catch (_: Exception) { }
                            }
                        ) {
                            Text("Blaze", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BangladeshRed)
                        }
                        IconButton(onClick = { viewModel.dismissQuotaNotice() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

            // Empty feed area stays completely clean until real posts load

            // Feed Posts with Google AdBanner after every 3 posts (whether photo or video, by any user)
            itemsIndexed(visiblePosts, key = { _, post -> post.id }) { index, post ->
                PostCard(
                    post = post,
                    viewModel = viewModel,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Show Google AdBanner after every 3 posts
                if ((index + 1) % 3 == 0) {
                    AdBanner(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        creativeIndex = index / 3
                    )
                }
            }

            item {
                if (isLoadingMorePosts) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = BangladeshGreen)
                    }
                } else if (hasMorePosts && visiblePosts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(onClick = { viewModel.loadMorePosts() }) {
                            Text("আরও ৩০টি পোস্ট লোড করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
    }
    } // End PullToRefreshBox
}
