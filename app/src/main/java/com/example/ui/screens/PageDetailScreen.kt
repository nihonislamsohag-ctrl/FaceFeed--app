package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.PostAudioTrack
import com.example.model.SocialPage
import com.example.ui.components.ProfessionalDashboardDialog
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

/**
 * Page Profile Screen ("পেইজ আইডি")
 * Exactly matches Screenshots 4, 5, 6, and 7:
 * - Screenshot 4: Cover with camera icon, Profile Picture "N" with camera icon, Creator Dashboard, Add to story, Tabs, Contact info
 * - Screenshot 5: Posts composer, "কুয়াকাটা সমুদ্র সৈকত 🏖️🏖️" post with See insights & Boost post
 * - Screenshot 6: Profile Picture Camera Bottom Sheet (Add to story, Add frame, Take photo, Upload photo, Select photo on Facebook, See profile picture)
 * - Screenshot 7: Cover Photo Camera Bottom Sheet (Take photo, Upload photo, Select photo on Facebook)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailScreen(
    page: SocialPage,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit,
    onOpenHomeFeed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var showProfileCameraSheet by remember { mutableStateOf(false) }
    var showCoverCameraSheet by remember { mutableStateOf(false) }
    var showFullProfilePicDialog by remember { mutableStateOf(false) }
    var showCreatorDashboardDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showBoostDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Photos, 2 = Reels, 3 = Mentions

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // State for interactive features requested by user
    var showAddToStoryDialog by remember { mutableStateOf(false) }
    var showAddFrameDialog by remember { mutableStateOf(false) }
    var showSelectPhotoFotoFilmDialog by remember { mutableStateOf(false) }
    var showSelectCoverFotoFilmDialog by remember { mutableStateOf(false) }
    var selectedProfileFrame by remember { mutableStateOf<PageProfileFrame?>(null) }

    // Screenshot 2: 3-dot Page Settings Sheet & Dialogs
    var showPageSettingsSheet by remember { mutableStateOf(false) }
    var showAdvertiseDialog by remember { mutableStateOf(false) }
    var showPageStatusDialog by remember { mutableStateOf(false) }
    var showActionButtonDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showActivityLogDialog by remember { mutableStateOf(false) }
    var showReviewPostsTagsDialog by remember { mutableStateOf(false) }
    var showAddHighlightsDialog by remember { mutableStateOf(false) }
    var showPageSearchDialog by remember { mutableStateOf(false) }
    var showInviteFriendsDialog by remember { mutableStateOf(false) }
    var currentActionButtonText by remember { mutableStateOf("Message") }

    // Photo pickers for Profile and Cover photos (Android Photo Picker)
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            profileBitmap = null
            toastMessage = "গ্যালারি থেকে প্রোফাইল ছবি সফলভাবে আপডেট করা হয়েছে! 📸"
        }
    }

    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverImageUri = uri
            coverBitmap = null
            toastMessage = "গ্যালারি থেকে কভার ছবি সফলভাবে আপডেট করা হয়েছে! 🖼️"
        }
    }

    // Camera Launchers (TakePicturePreview returns captured Bitmap directly)
    val takeProfilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            profileBitmap = bitmap
            profileImageUri = null
            toastMessage = "ক্যামেরা দিয়ে প্রোফাইল ছবি সফলভাবে তোলা হয়েছে! 📸"
        } else {
            // Fallback to gallery
            profilePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val takeCoverPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coverBitmap = bitmap
            coverImageUri = null
            toastMessage = "ক্যামেরা দিয়ে কভার ছবি সফলভাবে তোলা হয়েছে! 🖼️"
        } else {
            coverPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // Story Photo Picker
    val storyPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addStory(
                mediaUrl = uri.toString(),
                caption = "🌟 ${page.name} পেইজের নতুন স্টোরি!"
            )
            toastMessage = "গ্যালারি থেকে স্টোরিতে ছবি সফলভাবে যুক্ত হয়েছে! 🌟"
        }
    }

    // Local posts on this page (real user posts only)
    val pagePosts = remember { mutableStateListOf<Post>() }

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
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { onOpenHomeFeed?.invoke() }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = page.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Red 9+ Badge from Screenshot 4
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE53935),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "9+",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Home button: 🏕️/🏠 ঘরের ভেতরে "Home" লেখা ও ট্যাপ করলে Home Feed চালু হয়
                    Surface(
                        onClick = { onOpenHomeFeed?.invoke() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.2.dp, BangladeshGreen),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("page_home_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = BangladeshGreen,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Home",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangladeshGreen
                            )
                        }
                    }
                    IconButton(onClick = { toastMessage = "এডিট অপশন চালু হয়েছে" }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showPageSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showPageSettingsSheet = true }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 1. Cover Photo & Profile Picture with Camera Icons (Screenshot 4)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        // Cover Photo Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .background(Color(0xFF9E9E9E))
                        ) {
                            if (coverBitmap != null) {
                                AsyncImage(
                                    model = coverBitmap,
                                    contentDescription = "Cover photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (coverImageUri != null) {
                                AsyncImage(
                                    model = coverImageUri,
                                    contentDescription = "Cover photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (page.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = page.coverUrl,
                                    contentDescription = "Cover photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Camera Button on Cover Photo (Bottom Right of Cover) -> Opens Screenshot 7 Sheet
                            Surface(
                                onClick = { showCoverCameraSheet = true },
                                shape = CircleShape,
                                color = Color(0xFFE4E6EB),
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Cover camera",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Profile Picture Circle overlapping Cover Photo (Screenshot 4)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(130.dp)
                        ) {
                            // Circular Profile Picture
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFCE93D8), // Light purple matching Screenshot 4
                                border = BorderStroke(
                                    width = if (selectedProfileFrame?.borderColor != null) 5.dp else 4.dp,
                                    color = selectedProfileFrame?.borderColor ?: MaterialTheme.colorScheme.surface
                                ),
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (profileBitmap != null) {
                                        AsyncImage(
                                            model = profileBitmap,
                                            contentDescription = page.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (profileImageUri != null) {
                                        AsyncImage(
                                            model = profileImageUri,
                                            contentDescription = page.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (page.avatarUrl.isNotBlank() && !page.avatarUrl.contains("default")) {
                                        AsyncImage(
                                            model = page.avatarUrl,
                                            contentDescription = page.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Large letter "N" matching Screenshot 4
                                        Text(
                                            text = page.name.take(1).ifEmpty { "N" },
                                            fontSize = 62.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF4A148C) // Dark purple
                                        )
                                    }

                                    // Badge overlay for selected frame
                                    if (!selectedProfileFrame?.badgeEmoji.isNullOrEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = selectedProfileFrame?.borderColor ?: FacebookBlue,
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "${selectedProfileFrame?.badgeEmoji} ${selectedProfileFrame?.badgeLabel}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Camera Button on Profile Picture (Bottom Right) -> Opens Screenshot 6 Sheet
                            Surface(
                                onClick = { showProfileCameraSheet = true },
                                shape = CircleShape,
                                color = Color(0xFFE4E6EB),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Profile camera",
                                        tint = Color.Black,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Page Name, Followers, Category (Screenshot 4)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = page.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${page.followerCount} followers · 0 following",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = page.category.ifEmpty { "Personal blog" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Creator Dashboard (Blue) & Add to story (Grey)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCreatorDashboardDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "Creator Dashboard",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Creator Dashboard",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { toastMessage = "স্টোরিতে যুক্ত করুন চালু হয়েছে" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE4E6EB)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircleOutline,
                                        contentDescription = "Add to story",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Add to story",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Navigation Tabs: All, Photos, Short Video, Mentions
                    val pageTabs = listOf("All", "Photos", "Short Video", "Mentions")
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        pageTabs.forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = tabTitle,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }

                // 3. Contact info Section (Screenshot 4)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contact info",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit contact",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { toastMessage = "কনট্যাক্ট ইনফো এডিট করুন" }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = FacebookBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = "Messenger",
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = page.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                // 4. Posts Section & Composer (Screenshot 5)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Posts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Purple Avatar "N"
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFCE93D8),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = page.name.take(1).ifEmpty { "N" },
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A148C),
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                onClick = { showCreatePostDialog = true },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Share your moment?",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = { showCreatePostDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Photo",
                                    tint = Color(0xFF43A047),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sub-action buttons: Photo, Reels, Life Event (Screenshot 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TextButton(onClick = { showCreatePostDialog = true }) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }

                            TextButton(onClick = { toastMessage = "শর্ট ভিডিও আপলোড চালু হয়েছে 🎬" }) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Short Video", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }

                            TextButton(onClick = { toastMessage = "লাইফ ইভেন্ট যুক্ত করুন" }) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Life Event", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 6.dp)
                }

                // 5. Page Posts List (Screenshot 5: Kuakata Sea Beach Post)
                items(pagePosts) { post ->
                    PagePostCard(
                        post = post,
                        pageName = page.name,
                        onSeeInsights = { showInsightsDialog = true },
                        onBoostPost = { showBoostDialog = true },
                        onLike = { toastMessage = "লাইক দেওয়া হয়েছে" },
                        onComment = { toastMessage = "কমেন্ট বক্স খোলা হয়েছে" },
                        onShare = { toastMessage = "পোস্ট শেয়ার করা হয়েছে" }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // Toast Floating Message
            toastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // Screenshot 6: Profile Picture Camera Bottom Sheet
    // ----------------------------------------------------
    if (showProfileCameraSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileCameraSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Option 1: Add to story (ACTIVE)
                SheetActionRow(
                    icon = Icons.Default.AddCircleOutline,
                    text = "Add to story",
                    onClick = {
                        showProfileCameraSheet = false
                        showAddToStoryDialog = true
                    }
                )

                // Option 2: Add frame (ACTIVE)
                SheetActionRow(
                    icon = Icons.Default.CropSquare,
                    text = "Add frame",
                    onClick = {
                        showProfileCameraSheet = false
                        showAddFrameDialog = true
                    }
                )

                // Option 3: Take photo (ACTIVE - Camera + Gallery fallback)
                SheetActionRow(
                    icon = Icons.Default.CameraAlt,
                    text = "Take photo",
                    onClick = {
                        showProfileCameraSheet = false
                        takeProfilePhotoLauncher.launch(null)
                    }
                )

                // Option 4: Upload photo (ACTIVE - Android Photo Picker / Phone Gallery)
                SheetActionRow(
                    icon = Icons.Default.FolderOpen,
                    text = "Upload photo",
                    onClick = {
                        showProfileCameraSheet = false
                        profilePhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                // Option 5: Select photo on FotoFilm (ACTIVE - Renamed as requested)
                SheetActionRow(
                    icon = Icons.Default.Image,
                    text = "Select photo on FotoFilm",
                    onClick = {
                        showProfileCameraSheet = false
                        showSelectPhotoFotoFilmDialog = true
                    }
                )

                // Option 6: See profile picture (ACTIVE - Full screen preview)
                SheetActionRow(
                    icon = Icons.Default.AccountCircle,
                    text = "See profile picture",
                    onClick = {
                        showProfileCameraSheet = false
                        showFullProfilePicDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ----------------------------------------------------
    // Screenshot 7: Cover Photo Camera Bottom Sheet
    // ----------------------------------------------------
    if (showCoverCameraSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCoverCameraSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Option 1: Take photo (ACTIVE - Camera + Gallery fallback)
                SheetActionRow(
                    icon = Icons.Default.CameraAlt,
                    text = "Take photo",
                    onClick = {
                        showCoverCameraSheet = false
                        takeCoverPhotoLauncher.launch(null)
                    }
                )

                // Option 2: Upload photo (ACTIVE - Android Photo Picker / Phone Gallery)
                SheetActionRow(
                    icon = Icons.Default.FolderOpen,
                    text = "Upload photo",
                    onClick = {
                        showCoverCameraSheet = false
                        coverPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                // Option 3: Select photo on FotoFilm (ACTIVE - Renamed as requested)
                SheetActionRow(
                    icon = Icons.Default.Image,
                    text = "Select photo on FotoFilm",
                    onClick = {
                        showCoverCameraSheet = false
                        showSelectCoverFotoFilmDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ----------------------------------------------------
    // Functional Dialog: Add to Story (স্টোরিতে ছবি দিন)
    // ----------------------------------------------------
    if (showAddToStoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddToStoryDialog = false },
            icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Add to story", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("স্টোরিতে ছবি যুক্ত করার অপশন বেছে নিন:", fontSize = 13.sp)

                    Button(
                        onClick = {
                            val media = profileImageUri?.toString() ?: page.avatarUrl
                            if (media.isNotBlank()) {
                                viewModel.addStory(
                                    mediaUrl = media,
                                    caption = "🌟 ${page.name} পেইজের অফিসিয়াল প্রোফাইল স্টোরি!"
                                )
                                showAddToStoryDialog = false
                                toastMessage = "প্রোফাইল ছবি স্টোরিতে সফলভাবে পোস্ট হয়েছে! 🌟"
                            } else {
                                showAddToStoryDialog = false
                                storyPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বর্তমান প্রোফাইল ছবি দিয়ে স্টোরি দিন")
                    }

                    OutlinedButton(
                        onClick = {
                            showAddToStoryDialog = false
                            storyPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📁 ফোনের গ্যালারি থেকে নতুন ছবি দিন")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToStoryDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Functional Dialog: Add Frame (প্রোফাইল ফ্রেম সিলেক্টর)
    // ----------------------------------------------------
    if (showAddFrameDialog) {
        val availableFrames = remember {
            listOf(
                PageProfileFrame("none", "কোনো ফ্রেম নেই (Normal)", "স্বাভাবিক প্রোফাইল ছবি", null, "", ""),
                PageProfileFrame("bd_50", "বাংলাদেশ ৫০ বছর / জাতীয় পতাকা 🇧🇩", "লাল-সবুজ গৌরবের ফ্রেম", Color(0xFF006A4E), "🇧🇩", "বাংলাদেশ"),
                PageProfileFrame("vip_gold", "FotoFilm গোল্ডেন ভিআইপি 👑", "গোল্ডেন ক্রাউন ভিআইপি ফ্রেম", Color(0xFFFFD700), "👑", "VIP"),
                PageProfileFrame("verified_green", "FotoFilm ভেরিফাইড 💎", "সবুজ-হলুদ ভেরিফাইড মেম্বার ফ্রেম", Color(0xFF159947), "💎", "VERIFIED"),
                PageProfileFrame("eid_mubarak", "ঈদ মোবারক ফ্রেম 🌙", "পবিত্র ঈদ মোবারক ফ্রেম", Color(0xFF4CAF50), "🌙", "EID MUBARAK"),
                PageProfileFrame("love_heart", "লাভ & হার্ট ফ্রেম ❤️", "ভালোবাসা ও লাভ ফ্রেম", Color(0xFFE91E63), "❤️", "LOVE"),
                PageProfileFrame("flower_crown", "ফুলের রিং ক্রাউন 🌺", "গোলাপ ও রঙিন ফুলের ফ্রেম", Color(0xFFFF4081), "🌺", "FLOWER")
            )
        }

        AlertDialog(
            onDismissRequest = { showAddFrameDialog = false },
            icon = { Icon(Icons.Default.CropSquare, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Add Frame (প্রোফাইল ফ্রেম)", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableFrames) { frame ->
                        val isSelected = selectedProfileFrame?.id == frame.id || (selectedProfileFrame == null && frame.id == "none")
                        Surface(
                            onClick = {
                                selectedProfileFrame = if (frame.id == "none") null else frame
                                showAddFrameDialog = false
                                toastMessage = "${frame.name} ফ্রেম সফলভাবে যুক্ত হয়েছে! ✨"
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) FacebookBlue else Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (frame.borderColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(frame.borderColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(frame.badgeEmoji, fontSize = 16.sp)
                                    }
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(28.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(frame.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(frame.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddFrameDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Functional Dialog: Select Photo on FotoFilm (প্রোফাইল)
    // ----------------------------------------------------
    // ----------------------------------------------------
    // Functional Dialog: Select Photo on FotoFilm (প্রোফাইল)
    // ----------------------------------------------------
    if (showSelectPhotoFotoFilmDialog) {
        val pageGalleryPhotos = remember(pagePosts) {
            pagePosts.flatMap { it.images }.filter { it.isNotBlank() }.distinct()
        }

        AlertDialog(
            onDismissRequest = { showSelectPhotoFotoFilmDialog = false },
            icon = { Icon(Icons.Default.Image, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Select photo on FotoFilm", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            showSelectPhotoFotoFilmDialog = false
                            profilePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📁 ফোনের গ্যালারি থেকে পছন্দ করুন", fontWeight = FontWeight.Bold)
                    }

                    if (pageGalleryPhotos.isEmpty()) {
                        Text(
                            text = "পেইজে এখনো কোনো ছবি পোস্ট করা হয়নি। ফোনের গ্যালারি থেকে নতুন ছবি নির্বাচন করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(pageGalleryPhotos) { photoUrl ->
                                Surface(
                                    onClick = {
                                        profileImageUri = Uri.parse(photoUrl)
                                        profileBitmap = null
                                        showSelectPhotoFotoFilmDialog = false
                                        toastMessage = "FotoFilm থেকে প্রোফাইল ছবি সফলভাবে সেট করা হয়েছে! 📸"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(80.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "FotoFilm Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectPhotoFotoFilmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Functional Dialog: Select Cover Photo on FotoFilm (কভার)
    // ----------------------------------------------------
    if (showSelectCoverFotoFilmDialog) {
        val pageCoverPhotos = remember(pagePosts) {
            pagePosts.flatMap { it.images }.filter { it.isNotBlank() }.distinct()
        }

        AlertDialog(
            onDismissRequest = { showSelectCoverFotoFilmDialog = false },
            icon = { Icon(Icons.Default.Image, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Select photo on FotoFilm (কভার)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            showSelectCoverFotoFilmDialog = false
                            coverPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📁 ফোনের গ্যালারি থেকে কভার ছবি বাছুন", fontWeight = FontWeight.Bold)
                    }

                    if (pageCoverPhotos.isEmpty()) {
                        Text(
                            text = "পেইজে এখনো কোনো কভার ছবি নেই। ফোনের গ্যালারি থেকে নতুন কভার ছবি বাছুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pageCoverPhotos) { coverUrl ->
                                Surface(
                                    onClick = {
                                        coverImageUri = Uri.parse(coverUrl)
                                        coverBitmap = null
                                        showSelectCoverFotoFilmDialog = false
                                        toastMessage = "FotoFilm থেকে কভার ছবি সফলভাবে সেট করা হয়েছে! 🖼️"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp),
                                    shadowElevation = 2.dp
                                ) {
                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = "FotoFilm Cover",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectCoverFotoFilmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Fullscreen Profile Picture Preview Dialog (See Profile Picture)
    // ----------------------------------------------------
    if (showFullProfilePicDialog) {
        Dialog(onDismissRequest = { showFullProfilePicDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18191A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(page.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showFullProfilePicDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFCE93D8),
                        border = BorderStroke(
                            width = if (selectedProfileFrame?.borderColor != null) 6.dp else 4.dp,
                            color = selectedProfileFrame?.borderColor ?: Color.White
                        ),
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (profileBitmap != null) {
                                AsyncImage(
                                    model = profileBitmap,
                                    contentDescription = page.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = page.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (page.avatarUrl.isNotBlank() && !page.avatarUrl.contains("default")) {
                                AsyncImage(
                                    model = page.avatarUrl,
                                    contentDescription = page.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = page.name.take(1).ifEmpty { "N" },
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A148C)
                                )
                            }

                            if (!selectedProfileFrame?.badgeEmoji.isNullOrEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = selectedProfileFrame?.borderColor ?: FacebookBlue,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "${selectedProfileFrame?.badgeEmoji} ${selectedProfileFrame?.badgeLabel}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showFullProfilePicDialog = false
                                profilePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("গ্যালারি খুলুন", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                showFullProfilePicDialog = false
                                showAddFrameDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CropSquare, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ফ্রেম দিন", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Profile Picture Preview Dialog
    if (showFullProfilePicDialog) {
        Dialog(onDismissRequest = { showFullProfilePicDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(page.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showFullProfilePicDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFCE93D8),
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = page.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = page.name.take(1).ifEmpty { "N" },
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A148C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Creator Dashboard Dialog (Professional Dashboard matching Screenshot 2)
    if (showCreatorDashboardDialog) {
        ProfessionalDashboardDialog(
            onDismiss = { showCreatorDashboardDialog = false }
        )
    }

    // Insights Dialog
    if (showInsightsDialog) {
        AlertDialog(
            onDismissRequest = { showInsightsDialog = false },
            title = { Text("পোস্ট ইনসাইটস (Post Insights)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• মানুষের কাছে পৌঁছেছে (People Reached): 8,420 জন", fontSize = 13.sp)
                    Text("• এঙ্গেজমেন্ট (Engagements): 1,240", fontSize = 13.sp)
                    Text("• শেয়ার সংখ্যা: 24 বার", fontSize = 13.sp)
                    Text("• লাইক ও রিয়্যাকশন: 485 টি", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showInsightsDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Boost Dialog
    if (showBoostDialog) {
        AlertDialog(
            onDismissRequest = { showBoostDialog = false },
            title = { Text("পোস্ট বুস্ট করুন (Boost Post)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার পোস্টটি আরও বেশি মানুষের কাছে পৌঁছে দিন!", fontSize = 13.sp)
                    Text("• বাজেট: ৫০০ টাকা / ৩ দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("• সম্ভাব্য রিচ: 15,000 - 30,000 মানুষ", fontSize = 13.sp)
                    Text("• পেমেন্ট মেথড: bKash / Nagad / Card", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBoostDialog = false
                        toastMessage = "🎉 পোস্ট বুস্ট সফলভাবে সম্পন্ন হয়েছে!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("কনফার্ম ও বুস্ট")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBoostDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Create Post Dialog
    if (showCreatePostDialog) {
        val pageAuthor = remember(page) {
            User(
                id = "page_${page.id}",
                username = page.name.lowercase().replace(" ", "_"),
                fullName = page.name,
                avatarUrl = page.avatarUrl,
                isVerified = true
            )
        }
        GroupCreatePostDialog(
            groupName = page.name,
            currentUser = pageAuthor,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = { text, uri, audioTrack ->
                val newPost = Post(
                    id = "page_post_${System.currentTimeMillis()}",
                    author = pageAuthor,
                    timestamp = "Just now • 🌐",
                    text = text,
                    images = if (uri != null) listOf(uri.toString()) else emptyList(),
                    audioTrack = audioTrack,
                    musicInfo = audioTrack?.let { "${it.title} • ${it.artist}" }
                )
                pagePosts.add(0, newPost)
                viewModel.createPost(
                    text = text,
                    hashtags = emptyList(),
                    images = if (uri != null) listOf(uri.toString()) else emptyList(),
                    audioTrack = audioTrack,
                    musicInfo = audioTrack?.let { "${it.title} • ${it.artist}" },
                    customAuthor = pageAuthor
                )
                showCreatePostDialog = false
                toastMessage = "পেইজে পোস্ট সফলভাবে প্রকাশিত হয়েছে! ✅"
            }
        )
    }

    // ----------------------------------------------------
    // Screenshot 2: Page Settings 3-Dot Bottom Sheet
    // Matches Screenshot_20261105_221247_05e00dcd.jpg:
    // 1. Advertise
    // 2. Page status
    // 3. Add action button
    // 4. Archive
    // 5. Activity log
    // 6. Review posts and tags
    // 7. Add highlights
    // 8. Search
    // 9. Invite people to connect
    // 10. Copy link to Page
    // ----------------------------------------------------
    if (showPageSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPageSettingsSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // 1. Advertise
                item {
                    SheetActionRow(
                        icon = Icons.Default.Campaign,
                        text = "Advertise",
                        onClick = {
                            showPageSettingsSheet = false
                            showAdvertiseDialog = true
                        }
                    )
                }

                // 2. Page status
                item {
                    SheetActionRow(
                        icon = Icons.Default.Shield,
                        text = "Page status",
                        onClick = {
                            showPageSettingsSheet = false
                            showPageStatusDialog = true
                        }
                    )
                }

                // 3. Add action button
                item {
                    SheetActionRow(
                        icon = Icons.Default.TouchApp,
                        text = "Add action button",
                        onClick = {
                            showPageSettingsSheet = false
                            showActionButtonDialog = true
                        }
                    )
                }

                // 4. Archive
                item {
                    SheetActionRow(
                        icon = Icons.Default.Archive,
                        text = "Archive",
                        onClick = {
                            showPageSettingsSheet = false
                            showArchiveDialog = true
                        }
                    )
                }

                // 5. Activity log
                item {
                    SheetActionRow(
                        icon = Icons.Default.ListAlt,
                        text = "Activity log",
                        onClick = {
                            showPageSettingsSheet = false
                            showActivityLogDialog = true
                        }
                    )
                }

                // 6. Review posts and tags
                item {
                    SheetActionRow(
                        icon = Icons.Default.LocalOffer,
                        text = "Review posts and tags",
                        onClick = {
                            showPageSettingsSheet = false
                            showReviewPostsTagsDialog = true
                        }
                    )
                }

                // 7. Add highlights
                item {
                    SheetActionRow(
                        icon = Icons.Default.FavoriteBorder,
                        text = "Add highlights",
                        onClick = {
                            showPageSettingsSheet = false
                            showAddHighlightsDialog = true
                        }
                    )
                }

                // 8. Search
                item {
                    SheetActionRow(
                        icon = Icons.Default.Search,
                        text = "Search",
                        onClick = {
                            showPageSettingsSheet = false
                            showPageSearchDialog = true
                        }
                    )
                }

                // 9. Invite people to connect
                item {
                    SheetActionRow(
                        icon = Icons.Default.PersonAdd,
                        text = "Invite people to connect",
                        onClick = {
                            showPageSettingsSheet = false
                            showInviteFriendsDialog = true
                        }
                    )
                }

                // 10. Copy link to Page
                item {
                    SheetActionRow(
                        icon = Icons.Default.Link,
                        text = "Copy link to Page",
                        onClick = {
                            showPageSettingsSheet = false
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Page Link", "https://fotofilm.app/page/${page.name.lowercase().replace(" ", "_")}")
                            clipboard?.setPrimaryClip(clip)
                            toastMessage = "পেইজের লিংক সফলভাবে কপি করা হয়েছে! 📋"
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // 1. Advertise Dialog (ACTIVE)
    if (showAdvertiseDialog) {
        var selectedBudget by remember { mutableStateOf("৳৫০০ / ৩ দিন (15,000 মানুষ)") }
        AlertDialog(
            onDismissRequest = { showAdvertiseDialog = false },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Advertise (পেইজ বিজ্ঞাপন)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("আপনার পেইজের রিচ ও ফলোয়ার বাড়াতে বিজ্ঞাপন চালান:", fontSize = 13.sp)

                    listOf(
                        "৳৫০০ / ৩ দিন (15,000 মানুষ)",
                        "৳১,০০০ / ৭ দিন (45,000 মানুষ)",
                        "৳২,৫০০ / ১৫ দিন (120,000 মানুষ)"
                    ).forEach { budgetOption ->
                        Surface(
                            onClick = { selectedBudget = budgetOption },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedBudget == budgetOption) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (selectedBudget == budgetOption) FacebookBlue else Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = budgetOption,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdvertiseDialog = false
                        toastMessage = "বিজ্ঞাপন ক্যাম্পেইন সফলভাবে চালু হয়েছে! 🚀 ($selectedBudget)"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("বিজ্ঞাপন রান করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdvertiseDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 2. Page Status Dialog (ACTIVE)
    if (showPageStatusDialog) {
        AlertDialog(
            onDismissRequest = { showPageStatusDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = BangladeshGreen) },
            title = { Text("Page status", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️ Page has no issues", fontWeight = FontWeight.Bold, color = BangladeshGreen, fontSize = 15.sp)
                    Text("Good news: No violations or restrictions on your Page.", fontSize = 13.sp)
                    HorizontalDivider(thickness = 0.6.dp)
                    Text("• কমিউনিটি স্ট্যান্ডার্ড: ১০০% অনুগত ✅", fontSize = 12.sp)
                    Text("• মনিটাইজেশন স্ট্যাটাস: যোগ্য (Eligible) ✅", fontSize = 12.sp)
                    Text("• পেইজ কোয়ালিটি: গুড স্ট্যান্ডিং ⭐", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPageStatusDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // 3. Add Action Button Dialog (ACTIVE)
    if (showActionButtonDialog) {
        val buttonsList = listOf(
            "Send Messenger Message",
            "Send WhatsApp Message",
            "Call Now",
            "Send Email",
            "Learn More",
            "Shop Now"
        )
        AlertDialog(
            onDismissRequest = { showActionButtonDialog = false },
            icon = { Icon(Icons.Default.TouchApp, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Add action button", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("পেইজে দর্শনার্থীদের জন্য প্রাইমারি বাটন বেছে নিন:", fontSize = 13.sp)
                    buttonsList.forEach { btn ->
                        Surface(
                            onClick = {
                                currentActionButtonText = btn
                                showActionButtonDialog = false
                                toastMessage = "অ্যাকশন বাটন '$btn' সফলভাবে সেট করা হয়েছে! 🎯"
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentActionButtonText == btn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = btn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showActionButtonDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 4. Archive Dialog (ACTIVE)
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            icon = { Icon(Icons.Default.Archive, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Archive (পেইজ আর্কাইভ)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আর্কাইভ করা পোস্ট ও কনটেন্ট:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("বর্তমানে কোনো আর্কাইভ করা পোস্ট বা স্টোরি নেই।", fontSize = 12.sp, color = Color.Gray)
                    Text("এখানে সংরক্ষিত কনটেন্ট দর্শনার্থীরা দেখতে পাবে না।", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showArchiveDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // 5. Activity Log Dialog (ACTIVE)
    if (showActivityLogDialog) {
        AlertDialog(
            onDismissRequest = { showActivityLogDialog = false },
            icon = { Icon(Icons.Default.ListAlt, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Activity log (অ্যাক্টিভিটি লগ)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Page: ${page.name}", fontSize = 12.sp)
                    Text("• Category: ${page.category}", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showActivityLogDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // 6. Review Posts and Tags Dialog (ACTIVE)
    if (showReviewPostsTagsDialog) {
        AlertDialog(
            onDismissRequest = { showReviewPostsTagsDialog = false },
            icon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Review posts and tags", fontWeight = FontWeight.Bold) },
            text = {
                Text("বর্তমানে কোনো পেন্ডিং ট্যাগ বা মেনশন রিভিউ নেই ✅\nসবকিছু ক্লিয়ার আছে।", fontSize = 13.sp)
            },
            confirmButton = {
                Button(onClick = { showReviewPostsTagsDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // 7. Add Highlights Dialog (ACTIVE)
    if (showAddHighlightsDialog) {
        var highlightTitle by remember { mutableStateOf("My Highlights 🌟") }
        AlertDialog(
            onDismissRequest = { showAddHighlightsDialog = false },
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Add highlights", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("পেইজ প্রোফাইলে স্টোরি হাইলাইটস যুক্ত করুন:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = highlightTitle,
                        onValueChange = { highlightTitle = it },
                        label = { Text("হাইলাইটস নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddHighlightsDialog = false
                        toastMessage = "নতুন হাইলাইটস '$highlightTitle' সফলভাবে যুক্ত হয়েছে! 💖"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHighlightsDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 8. Page Search Dialog (ACTIVE)
    if (showPageSearchDialog) {
        var searchQuery by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPageSearchDialog = false },
            icon = { Icon(Icons.Default.Search, contentDescription = null, tint = FacebookBlue) },
            title = { Text("পেইজে সার্চ করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("পোস্ট, ছবি বা ভিডিও খুঁজুন...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPageSearchDialog = false
                        toastMessage = if (searchQuery.isNotBlank()) "'$searchQuery' সম্পর্কিত ১টি পোস্ট পাওয়া গেছে ✅" else "সার্চ সম্পন্ন হয়েছে"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("সার্চ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPageSearchDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 9. Invite Friends Dialog (ACTIVE)
    if (showInviteFriendsDialog) {
        val allUsers by viewModel.allAppUsers.collectAsState()
        val realFriends = allUsers.filter { it.id != currentUser.id }.map { it.fullName }
        AlertDialog(
            onDismissRequest = { showInviteFriendsDialog = false },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = FacebookBlue) },
            title = { Text("Invite people to connect", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (realFriends.isEmpty()) {
                        Text("ইনভাইট করার মতো কোনো বন্ধু পাওয়া যায়নি।", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        Text("আপনার বন্ধুদের পেইজে ইনভাইট পাঠান:", fontSize = 13.sp)
                        realFriends.forEach { friend ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Surface(shape = CircleShape, color = Color(0xFFE4E6EB), modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(friend, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text("ইনভাইট ✓", color = FacebookBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInviteFriendsDialog = false
                        toastMessage = "সকল বন্ধুদের পেইজ ফলো করার ইনভাইটেশন পাঠানো হয়েছে! 📨"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("সকলকে পাঠান")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteFriendsDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Action row in Bottom Sheet (Images 6 & 7)
 */
@Composable
fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF0F2F5),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

/**
 * Page Post Card matching Screenshot 5
 */
@Composable
fun PagePostCard(
    post: Post,
    pageName: String,
    onSeeInsights: () -> Unit,
    onBoostPost: () -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Post Author Header: Purple circle "N" with green dot + Name + Just now • 🌐
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFCE93D8),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = pageName.take(1).ifEmpty { "N" },
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C),
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Green online dot (Screenshot 5)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pageName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = post.timestamp,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Text: "কুয়াকাটা সমুদ্র সৈকত 🏖️🏖️" (Screenshot 5)
            Text(
                text = post.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Post Image (Kuakata Beach)
            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.images.first(),
                    contentDescription = "Kuakata Beach",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Like, Comment, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    onLike()
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (isLiked) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onComment) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Buttons from Screenshot 5: "See insights and ..." & "Boost post"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSeeInsights,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7F3FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(
                        text = "See insights and ...",
                        color = FacebookBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onBoostPost,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(
                        text = "Boost post",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Profile Frame Model for Page
 */
data class PageProfileFrame(
    val id: String,
    val name: String,
    val description: String,
    val borderColor: Color?,
    val badgeEmoji: String,
    val badgeLabel: String
)
