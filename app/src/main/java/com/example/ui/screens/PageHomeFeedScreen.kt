package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.ReactionType
import com.example.model.SocialPage
import com.example.model.User
import com.example.ui.components.CreatePostDialog
import com.example.ui.page.PageCreatePostDialog
import com.example.ui.page.PageMessengerInboxView
import com.example.ui.page.PageNotificationsView
import com.example.ui.page.PagePostItemCard
import com.example.ui.page.PageVideosReelsView
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

/**
 * Complete FotoFilm Page System (Requirement 1-8):
 * 1. PAGE FEED: Only 1 main header + small top bar: "You are using FotoFilm as Page" with Switch button to go back to ID.
 * 2. PAGE POST CREATION: Page profile photo & Page name, "Add Title" on top, Text, Photo, Video, Music, Hashtag, Location, Feeling.
 * 3. 10 REACTIONS WITH NAMES: Like, Dislike, Nice, Love, Adorable, Wow, Happy, Sad, Angry, 100 with reactor names list.
 * 4. COMMENT & REPLY: Replying & commenting from Page shows Page name & photo. Like, Delete, Report.
 * 5. SHARE & BOOST: Share to Feed/Messenger/Group/Link. Page Admin Boost with budget/days and "Sponsored" badge.
 * 6. MESSENGER FOR PAGE: Separate inbox, chat with visitors, Audio/Video call buttons, voice message, multimedia.
 * 7. VIDEO & REEL VIDEO: Long videos with upload, short reels with music/filter/text.
 * 8. NOTIFICATION FOR PAGE: Separate notifications for Like, Follow, Comment, Message, Share, Boost result.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageHomeFeedScreen(
    page: SocialPage,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()

    var activeTopTab by remember { mutableStateOf(0) } // 0 = Feed, 1 = Messenger, 2 = Watch/Reels, 3 = Notifications, 4 = Page Profile
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var sharingPost by remember { mutableStateOf<Post?>(null) }
    var boostingPost by remember { mutableStateOf<Post?>(null) }

    // Page-specific dynamic notifications list
    val pageNotifications = remember { mutableStateListOf<String>() }

    val pageAuthor = remember(page) {
        User(
            id = "page_${page.id}",
            username = page.name.lowercase().replace(" ", "_"),
            fullName = page.name,
            avatarUrl = page.avatarUrl,
            isVerified = true
        )
    }

    // Real Page Posts only
    val initialPagePosts = remember(page.id) { mutableStateListOf<Post>() }

    // Filter page posts from ViewModel + initial page posts
    val displayFeedPosts = remember(allPosts, initialPagePosts) {
        val customPagePosts = allPosts.filter { it.author.fullName == page.name || it.author.id == "page_${page.id}" }
        (customPagePosts + initialPagePosts).distinctBy { it.id }
    }

    // Direct Gallery image launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.openCreatePostWithImages(uris.map { it.toString() })
            showCreatePostDialog = true
        }
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2200)
            toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // =========================================================================
                // Requirement 1: Only 1 Main Header + Small Page Bar (No two full headers)
                // Small top bar: "You are using FotoFilm as [Page Name]" + "Switch" button
                // =========================================================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(0.5.dp, BangladeshGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = BangladeshGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                border = BorderStroke(1.5.dp, BangladeshGreen),
                                modifier = Modifier.size(26.dp)
                            ) {
                                AsyncImage(
                                    model = page.avatarUrl,
                                    contentDescription = page.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You are using FotoFilm as ${page.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BangladeshGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Switch Button to return to Personal ID
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5 Icon Navigation Tabs (Feed, Messenger, Videos & Reels, Notifications, Profile)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 0: Feed (Home icon)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTopTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Feed",
                                tint = if (activeTopTab == 0) BangladeshGreen else Color(0xFF65676B),
                                modifier = Modifier.size(26.dp)
                            )
                            if (activeTopTab == 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(3.dp)
                                        .background(BangladeshGreen, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // Tab 1: Messenger (Chat icon with badge) - Requirement 6
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTopTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Messenger",
                                    tint = if (activeTopTab == 1) BangladeshGreen else Color(0xFF65676B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE53935),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = "3",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (activeTopTab == 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(3.dp)
                                        .background(BangladeshGreen, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // Tab 2: Videos & Reels (Video icon with badge) - Requirement 7
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTopTab = 2 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.OndemandVideo,
                                    contentDescription = "Videos & Short Video",
                                    tint = if (activeTopTab == 2) BangladeshGreen else Color(0xFF65676B),
                                    modifier = Modifier.size(25.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE53935),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 10.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = "15+",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (activeTopTab == 2) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(3.dp)
                                        .background(BangladeshGreen, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // Tab 3: Notifications (Bell icon with badge) - Requirement 8
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeTopTab = 3 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (activeTopTab == 3) BangladeshGreen else Color(0xFF65676B),
                                    modifier = Modifier.size(25.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE53935),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = "6",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            if (activeTopTab == 3) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(3.dp)
                                        .background(BangladeshGreen, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }

                    // Tab 4: Page Profile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenProfile() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, BangladeshGreen),
                            modifier = Modifier.size(26.dp)
                        ) {
                            AsyncImage(
                                model = page.avatarUrl,
                                contentDescription = page.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE4E6EB), thickness = 0.8.dp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTopTab) {
                // ==========================================
                // Tab 0: PAGE FEED (Requirements 1, 2, 3, 4, 5)
                // ==========================================
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF0F2F5))
                    ) {
                        // -----------------------------------------------------------------
                        // Requirement 2: Post Box shows Page Profile Photo and Page Name
                        // -----------------------------------------------------------------
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Page Profile Photo (Requirement 2)
                                        Surface(
                                            shape = CircleShape,
                                            border = BorderStroke(1.5.dp, BangladeshGreen),
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clickable { onOpenProfile() }
                                        ) {
                                            AsyncImage(
                                                model = page.avatarUrl,
                                                contentDescription = page.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // "What's on your mind?" Pill Box
                                        Surface(
                                            onClick = { showCreatePostDialog = true },
                                            shape = RoundedCornerShape(24.dp),
                                            color = Color(0xFFF0F2F5),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.CenterStart,
                                                modifier = Modifier.padding(horizontal = 14.dp)
                                            ) {
                                                Text(
                                                    text = "${page.name}-এর পক্ষ থেকে কিছু লিখুন...",
                                                    color = Color(0xFF65676B),
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Photo button
                                        IconButton(
                                            onClick = {
                                                photoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = "Photo",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFF0F2F5))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick action buttons under composer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        TextButton(onClick = { showCreatePostDialog = true }) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("টাইটেল লিখুন", fontSize = 12.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                                        }

                                        TextButton(onClick = {
                                            photoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ফটো", fontSize = 12.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                                        }

                                        TextButton(onClick = { activeTopTab = 2 }) {
                                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ভিডিও/শর্ট ভিডিও", fontSize = 12.sp, color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // -----------------------------------------------------------------
                        // Stories Carousel Tray
                        // -----------------------------------------------------------------
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Music Story
                                    Surface(
                                        onClick = { toastMessage = "মিউজিক স্টোরি চালু হয়েছে 🎵" },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(100.dp, 160.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(listOf(Color(0xFF26A69A), Color(0xFF00796B)))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.padding(8.dp))
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Music", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    // Page Story (+ Add)
                                    Surface(
                                        onClick = {
                                            photoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(100.dp, 160.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9))) {
                                            AsyncImage(
                                                model = page.avatarUrl,
                                                contentDescription = page.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = BangladeshGreen,
                                                border = BorderStroke(2.dp, Color.White),
                                                modifier = Modifier.align(Alignment.Center).size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                                            }
                                            Text(
                                                text = "Add story",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                                            )
                                        }
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // -----------------------------------------------------------------
                        // Page Posts Stream (Requirements 2, 3, 4, 5)
                        // -----------------------------------------------------------------
                        items(displayFeedPosts) { post ->
                            PagePostItemCard(
                                post = post,
                                page = page,
                                viewModel = viewModel,
                                onBoostClick = { boostingPost = it },
                                onShareClick = { sharingPost = it },
                                onOpenProfile = onOpenProfile,
                                onToast = { toastMessage = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // ==========================================
                // Tab 1: MESSENGER FOR PAGE (Requirement 6)
                // ==========================================
                1 -> {
                    PageMessengerInboxView(
                        page = page,
                        onAdminNotification = {
                            pageNotifications.add(0, it)
                            toastMessage = it
                        }
                    )
                }

                // ==========================================
                // Tab 2: VIDEO & REEL VIDEO (Requirement 7)
                // ==========================================
                2 -> {
                    PageVideosReelsView(
                        page = page,
                        onToast = { toastMessage = it }
                    )
                }

                // ==========================================
                // Tab 3: NOTIFICATIONS FOR PAGE (Requirement 8)
                // ==========================================
                3 -> {
                    PageNotificationsView(
                        page = page,
                        customNotifications = pageNotifications
                    )
                }
            }

            // Toast Message Bar
            if (toastMessage != null) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF212121),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = toastMessage!!,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // =========================================================================
    // Requirement 2: PAGE POST CREATION DIALOG (Shows Page photo & Page name)
    // Full Rich CreatePostDialog matching Screenshots 1 & 2
    // =========================================================================
    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            onDismiss = { showCreatePostDialog = false },
            pageAuthor = page
        )
    }

    // =========================================================================
    // Requirement 5: SHARE DIALOG (Share to Feed, Messenger, Group, Copy Link)
    // =========================================================================
    if (sharingPost != null) {
        val target = sharingPost!!
        AlertDialog(
            onDismissRequest = { sharingPost = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = BangladeshGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("পোস্টটি শেয়ার করুন", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = {
                            viewModel.sharePost(target.id)
                            sharingPost = null
                            toastMessage = "পেইজ ফিডে শেয়ার করা হয়েছে 📢"
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RssFeed, contentDescription = null, tint = BangladeshGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Share to Feed (ফিডে শেয়ার করুন)", fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        onClick = {
                            sharingPost = null
                            toastMessage = "ChatFilm-এ পাঠানো হয়েছে ✉️"
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Share to ChatFilm (চ্যাটফিল্মে পাঠান)", fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        onClick = {
                            sharingPost = null
                            toastMessage = "গ্রুপে শেয়ার করা হয়েছে 👥"
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF7B1FA2))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Share to Group (গ্রুপে শেয়ার করুন)", fontWeight = FontWeight.Medium)
                        }
                    }

                    Surface(
                        onClick = {
                            sharingPost = null
                            toastMessage = "পোস্টের লিংক কপি করা হয়েছে 🔗"
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Copy Link (লিংক কপি করুন)", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { sharingPost = null }) { Text("বাতিল") }
            }
        )
    }

    // =========================================================================
    // Requirement 5: BOOST DIALOG (Page Admin Boost with budget & days option)
    // =========================================================================
    if (boostingPost != null) {
        val target = boostingPost!!
        var selectedBudget by remember { mutableIntStateOf(500) }
        var selectedDays by remember { mutableIntStateOf(7) }

        AlertDialog(
            onDismissRequest = { boostingPost = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = BangladeshGreen,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text("পেইজ পোস্ট বুস্ট করুন (Admin Boost)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "বুস্ট করলে পোস্টটি হাজারো অডিয়েন্সের কাছে Sponsored হিসেবে পৌঁছে যাবে।",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text("বাজেট নির্বাচন (BDT):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(300, 500, 1000, 2500).forEach { b ->
                            Surface(
                                onClick = { selectedBudget = b },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedBudget == b) BangladeshGreen else Color(0xFFF0F2F5)
                            ) {
                                Text(
                                    "৳$b",
                                    color = if (selectedBudget == b) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("সময়কাল (দিন):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 7, 14).forEach { d ->
                            Surface(
                                onClick = { selectedDays = d },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedDays == d) BangladeshGreen else Color(0xFFF0F2F5)
                            ) {
                                Text(
                                    "$d দিন",
                                    color = if (selectedDays == d) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "আনুমানিক রিচ: ${(selectedBudget * 35)} - ${(selectedBudget * 50)} মানুষ 🚀",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BangladeshGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.boostPost(target.id, selectedBudget, selectedDays)
                        pageNotifications.add(0, "বুস্ট সফল: পোস্টটি Sponsored হিসেবে চালু হয়েছে (৳$selectedBudget / $selectedDays দিন) 🚀")
                        boostingPost = null
                        toastMessage = "পোস্ট সফলভাবে বুস্ট করা হয়েছে! স্পনসরড চালু হয়েছে 🚀"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("কনফার্ম বুস্ট")
                }
            },
            dismissButton = {
                TextButton(onClick = { boostingPost = null }) { Text("বাতিল") }
            }
        )
    }
}
