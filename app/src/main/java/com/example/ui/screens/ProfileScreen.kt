package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.FollowerUser
import com.example.model.ProfileFriend
import com.example.model.Translations
import com.example.ui.components.*
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.util.AppSoundManager
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

enum class ProfileSubTab {
    ALL, REELS, PHOTOS
}

enum class ActiveStatType {
    NONE, FOLLOWERS, FOLLOWING, POSTS
}

@Composable
fun ProfileScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val user by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val followers by viewModel.followers.collectAsState()
    val following by viewModel.following.collectAsState()

    // Filter to user's own 2 posts
    val userPosts = remember(allPosts, user.id) {
        val filtered = allPosts.filter { it.author.id == user.id }
        if (filtered.isNotEmpty()) filtered else allPosts.take(2)
    }

    var activeSubTab by remember { mutableStateOf(ProfileSubTab.ALL) }
    var selectedStat by remember { mutableStateOf(ActiveStatType.NONE) }

    // Dialog controllers
    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }
    var showDashboardDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showAllFriendsDialog by remember { mutableStateOf(false) }
    var showLifeEventDialog by remember { mutableStateOf(false) }
    var selectedFriendDetail by remember { mutableStateOf<ProfileFriend?>(null) }

    // Action Sheets & Modal States for Profile & Cover Photos (matching Screenshot 1 & 2)
    var showProfileAvatarModal by remember { mutableStateOf(false) }
    var showCoverPhotoModal by remember { mutableStateOf(false) }
    var showFramePickerDialog by remember { mutableStateOf(false) }
    var showSelectPhotoOnFaceFeedDialog by remember { mutableStateOf(false) }
    var isSelectingForCover by remember { mutableStateOf(false) }
    var showViewProfilePictureDialog by remember { mutableStateOf(false) }
    var showViewCoverPhotoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Real Phone Gallery Launcher for Profile Avatar (Screenshot 1)
    val profileAvatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileAvatar(uri.toString())
            Toast.makeText(context, "ফোন গ্যালারি থেকে প্রোফাইল ছবি আপডেট হয়েছে! 📸", Toast.LENGTH_SHORT).show()
            AppSoundManager.playReactionChime()
            showProfileAvatarModal = false
        }
    }

    // Real Phone Gallery Launcher for Cover Photo (Screenshot 2)
    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateCoverPhoto(uri.toString())
            Toast.makeText(context, "ফোন গ্যালারি থেকে কভার ফটো আপডেট হয়েছে! 🖼️", Toast.LENGTH_SHORT).show()
            AppSoundManager.playReactionChime()
            showCoverPhotoModal = false
        }
    }

    // Real Phone Gallery Launcher for Posts created from Profile
    val profilePostGalleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.openCreatePostWithImages(uris.map { it.toString() })
        } else {
            viewModel.setShowCreatePostDialog(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Facebook Style Top Bar for Profile Screen (matching screenshot)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setTab(AppTab.HOME) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEditProfileDialog = true }
                ) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = { viewModel.setTab(AppTab.HOME) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { showEditProfileDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cover Photo & Avatar Header
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Cover Photo (height 200dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                showCoverPhotoModal = true
                            }
                    ) {
                        AsyncImage(
                            model = user.coverUrl.ifEmpty { "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200" },
                            contentDescription = strings.coverPhoto,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Subtle dark gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                                    )
                                )
                        )

                        // Camera button on cover photo (triggers CoverPhotoBottomSheet matching Screenshot 2)
                        IconButton(
                            onClick = {
                                showCoverPhotoModal = true
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = strings.changePhoto,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Profile Avatar & "Share a note..." bubble
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 135.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Avatar Box with Frame Support (matching Screenshot 1)
                            val activeFrame = AVAILABLE_PROFILE_FRAMES.find { it.id == user.avatarFrame }
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(if (activeFrame != null) 124.dp else 116.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (activeFrame != null) {
                                                Brush.sweepGradient(
                                                    listOf(
                                                        activeFrame.borderColor,
                                                        activeFrame.secondaryColor,
                                                        activeFrame.borderColor
                                                    )
                                                )
                                            } else {
                                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
                                            }
                                        )
                                        .padding(if (activeFrame != null) 4.dp else 0.dp)
                                        .clickable {
                                            showProfileAvatarModal = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(116.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(if (activeFrame == null) 4.dp else 2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" },
                                            contentDescription = strings.profilePhoto,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                // Badge or Camera button on avatar
                                if (activeFrame != null) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        shadowElevation = 4.dp,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clickable { showProfileAvatarModal = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(activeFrame.badgeIcon, fontSize = 18.sp)
                                        }
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            showProfileAvatarModal = true
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = strings.changePhoto,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // "Share a note..." bubble from screenshot
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .padding(bottom = 20.dp)
                                    .clickable { showEditNoteDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (user.note.isNotBlank()) user.note else "Share a note...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // User Full Name & Blue Verified Checkmark
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                fontWeight = FontWeight.Black,
                                fontSize = 23.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (user.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                FidaVerifiedBadge(size = 20.dp)
                            }
                        }

                        // Online Status Pill (Active Now)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OnlineGreen.copy(alpha = 0.12f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "এখন সক্রিয় আছেন (Active now)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnlineGreen
                                )
                            }
                        }

                        // ==========================================
                        // STATS ROW: 50 followers • 5 following • 2 posts
                        // User requirement: "প্রোতেক্টা অফসোনে টিপ দিলে নিলা হয়ে জাবে আর তার বিতরে ডুকাইবে"
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. 50 Followers Item
                            StatTextButton(
                                text = "${user.followerCount} followers",
                                isSelected = selectedStat == ActiveStatType.FOLLOWERS,
                                onClick = {
                                    selectedStat = ActiveStatType.FOLLOWERS
                                    showFollowersDialog = true
                                }
                            )

                            Text(
                                text = " · ",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )

                            // 2. 5 Following Item
                            StatTextButton(
                                text = "${user.followingCount} following",
                                isSelected = selectedStat == ActiveStatType.FOLLOWING,
                                onClick = {
                                    selectedStat = ActiveStatType.FOLLOWING
                                    showFollowingDialog = true
                                }
                            )

                            Text(
                                text = " · ",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )

                            // 3. 2 Posts Item
                            StatTextButton(
                                text = "${user.postCount} posts",
                                isSelected = selectedStat == ActiveStatType.POSTS,
                                onClick = {
                                    selectedStat = ActiveStatType.POSTS
                                    activeSubTab = ProfileSubTab.ALL
                                }
                            )
                        }

                        // 5,000 Friends Limit pill (from user screenshot)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FacebookBlue.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "5,000 Friends (সর্বোচ্চ লিমিট পূর্ণ • অটো ফলোয়ার সক্রিয়)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue
                                )
                            }
                        }

                        // Bio text from screenshot
                        Text(
                            text = "অনবরত মা মাটি মানুষের জন্য🌱\nAlhamdulillah for everything🤲",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        // Info Chips Line (matching screenshot)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Digital creator", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dhaka", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("লাউপাডা সাগর সৈকত মাধ্যমিক বিদ্যালয়", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Taltali Degree College .তালতলী ডিগ্রী কলেজ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("niyonislamsohag", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FacebookBlue)
                            }
                        }

                        // Action Buttons: 📊 Dashboard & (+) Add to story & •••
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dashboard Button (Blue)
                            Button(
                                onClick = { showDashboardDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dashboard",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Add to Story Button (Light Gray)
                            FilledTonalButton(
                                onClick = { viewModel.setShowCreatePostDialog(true) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add to story",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // More Options Button (•••)
                            FilledTonalIconButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // ==========================================
                        // SUB-TABS: All | Reels | Photos
                        // User requirement: "টিপ দিলে নিলা হয়ে জাবে"
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileTabItem(
                                title = "All",
                                isSelected = activeSubTab == ProfileSubTab.ALL,
                                onClick = { activeSubTab = ProfileSubTab.ALL }
                            )

                            ProfileTabItem(
                                title = "Reels",
                                isSelected = activeSubTab == ProfileSubTab.REELS,
                                onClick = { activeSubTab = ProfileSubTab.REELS }
                            )

                            ProfileTabItem(
                                title = "Photos",
                                isSelected = activeSubTab == ProfileSubTab.PHOTOS,
                                onClick = { activeSubTab = ProfileSubTab.PHOTOS }
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Tab Content: ALL Tab
            if (activeSubTab == ProfileSubTab.ALL) {
                // Personal Details Section (from screenshot)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Personal Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        DetailRow(icon = Icons.Default.LocationOn, label = user.livesIn)
                        DetailRow(icon = Icons.Default.Home, label = "From ${user.fromCity}")
                        DetailRow(icon = Icons.Default.AccessTime, label = user.joinedDate)
                        DetailRow(icon = Icons.Default.Language, label = user.website)

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // ============================================================
                        // FRIENDS SECTION (Friends এর সামনে See all এবং সব Friends নিচে)
                        // ============================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Friends",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "5,000 friends (65 mutual)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = { showAllFriendsDialog = true }
                            ) {
                                Text(
                                    text = "See all",
                                    color = FacebookBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizontal list of friends
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(SampleData.profileFriends) { friend ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .width(115.dp)
                                        .clickable { selectedFriendDetail = friend }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(contentAlignment = Alignment.BottomEnd) {
                                            AsyncImage(
                                                model = friend.avatarUrl,
                                                contentDescription = friend.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape)
                                            )
                                            if (friend.isOnline) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(OnlineGreen)
                                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = friend.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = friend.mutualFriendsCount,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        // ============================================================
                        // POSTS SECTION (Posts এর নিচে প্রোফাইল আইকন ও ৫টি ফিচার: Reels, Live, Photo, Life Event, Status)
                        // ============================================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Posts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable { viewModel.setShowCreatePostDialog(true) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Filters", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable { }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Manage", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // What's on your mind Card with Avatar & 5 Feature Buttons
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Profile Avatar + What's on your mind? + Photo icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = user.avatarUrl,
                                        contentDescription = user.fullName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setShowCreatePostDialog(true) }
                                    ) {
                                        Text(
                                            text = "What's on your mind?",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { profilePostGalleryPicker.launch("image/*") },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = "Photo Gallery",
                                            tint = Color(0xFF45BD62),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )

                                // 5 Features Row: Reels, Live, Photo, Life Event, Status
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Reels
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { viewModel.openMediaPicker(MediaPickerTarget.SHORT_VIDEO) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Movie, contentDescription = "Reels", tint = Color(0xFFE4405F), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reels", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    // 2. Live
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { viewModel.openLiveStudio() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Videocam, contentDescription = "Live", tint = Color(0xFFF3425F), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Live", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    // 3. Photo
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { profilePostGalleryPicker.launch("image/*") }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = Color(0xFF45BD62), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    // 4. Life Event
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { showLifeEventDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Flag, contentDescription = "Life Event", tint = Color(0xFF9C27B0), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Life Event", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    // 5. Status / New Post
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.clickable { viewModel.setShowCreatePostDialog(true) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = "Status", tint = FacebookBlue, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Render user posts (with pinned post badge if pinned)
                items(userPosts) { post ->
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        if (post.id == "post_pinned") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PushPin,
                                    contentDescription = "Pinned post",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pinned post",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = FacebookBlue
                                )
                            }
                        }

                        PostCard(
                            post = post,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Tab Content: REELS Tab
            if (activeSubTab == ProfileSubTab.REELS) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Reels by ${user.fullName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Reels 3-item grid
                        val reelsList = listOf(
                            Triple("মাটির টানে গ্রামের পথে 🌿", "245K views", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600"),
                            Triple("সন্ধ্যায় সাগরের গর্জন 🌊", "180K views", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600"),
                            Triple("বন্ধুদের সাথে সুন্দর মুহূর্ত ✨", "310K views", "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=600")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reelsList.forEach { (caption, views, img) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    AsyncImage(
                                        model = img,
                                        contentDescription = caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                )
                                            )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(views, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content: PHOTOS Tab
            if (activeSubTab == ProfileSubTab.PHOTOS) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Photos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val photoUrls = listOf(
                            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400",
                            "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=400",
                            "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400",
                            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=400",
                            "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=400",
                            "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=400"
                        )

                        for (i in photoUrls.indices step 3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (j in 0 until 3) {
                                    val idx = i + j
                                    if (idx < photoUrls.size) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.LightGray)
                                        ) {
                                            AsyncImage(
                                                model = photoUrls[idx],
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // ============================================================
    // DIALOG: 50 FOLLOWERS ("তার বিতরে ডুকাইবে")
    // ============================================================
    if (showFollowersDialog) {
        FollowersDialog(
            followers = followers,
            onDismiss = {
                showFollowersDialog = false
                selectedStat = ActiveStatType.NONE
            },
            onRemove = { viewModel.removeFollower(it) },
            onToggleFollowBack = { viewModel.toggleFollowBack(it) }
        )
    }

    // ============================================================
    // DIALOG: 5 FOLLOWING ("তার বিতরে ডুকাইবে")
    // ============================================================
    if (showFollowingDialog) {
        FollowingDialog(
            followingList = following,
            onDismiss = {
                showFollowingDialog = false
                selectedStat = ActiveStatType.NONE
            },
            onToggleFollowing = { viewModel.toggleFollowing(it) }
        )
    }

    // ============================================================
    // DIALOG: PROFESSIONAL DASHBOARD
    // ============================================================
    if (showDashboardDialog) {
        ProfessionalDashboardDialog(
            user = user,
            onDismiss = { showDashboardDialog = false }
        )
    }

    // ============================================================
    // DIALOG: EDIT PROFILE DETAILS
    // ============================================================
    if (showEditProfileDialog) {
        EditProfileDetailsDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, bio, cat, work, lives, from, sch, col, ig ->
                viewModel.updateProfileDetails(name, bio, cat, work, lives, from, sch, col, ig)
                showEditProfileDialog = false
            }
        )
    }

    // ============================================================
    // DIALOG: EDIT NOTE ("Share a note...")
    // ============================================================
    if (showEditNoteDialog) {
        var noteInput by remember { mutableStateOf(user.note) }
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Share a note...", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Share a thought or status update ✨") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNote(noteInput)
                        showEditNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ============================================================
    // DIALOG: ALL FRIENDS ("Friends এর সামনে See all দেখাযায়ে আর সব Friends নিচে যেনো দেখাজায়ে")
    // ============================================================
    if (showAllFriendsDialog) {
        AllFriendsDialog(
            friends = SampleData.profileFriends,
            onDismiss = { showAllFriendsDialog = false },
            onSelectFriend = { friend ->
                showAllFriendsDialog = false
                selectedFriendDetail = friend
            }
        )
    }

    // ============================================================
    // DIALOG: LIFE EVENT (Life Event ক্রিয়েটর)
    // ============================================================
    if (showLifeEventDialog) {
        LifeEventDialog(
            onDismiss = { showLifeEventDialog = false },
            onPublish = { title, category, date, desc ->
                viewModel.createPost(
                    text = "🚩 $title\n$desc\n\n📅 $date",
                    hashtags = listOf("#LifeEvent", "#FaceFeed", "#Milestone", "#Alhamdulillah"),
                    images = listOf("https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=800"),
                    isVideo = false,
                    videoTitle = null,
                    isReel = false
                )
                showLifeEventDialog = false
            }
        )
    }

    // ============================================================
    // DIALOG: FRIEND DETAIL & CHAT ACTION
    // ============================================================
    selectedFriendDetail?.let { friend ->
        FriendDetailDialog(
            friend = friend,
            onDismiss = { selectedFriendDetail = null },
            onMessage = {
                selectedFriendDetail = null
                viewModel.setTab(AppTab.MESSAGES)
            }
        )
    }

    // ============================================================
    // 1. BOTTOM SHEET: PROFILE PICTURE (Screenshot 1)
    // Add to story, Add frame, Take photo, Upload photo, Select photo on FaceFeed, See profile picture
    // ============================================================
    if (showProfileAvatarModal) {
        ProfileAvatarBottomSheet(
            onDismiss = { showProfileAvatarModal = false },
            onAddToStory = {
                viewModel.shareProfileAvatarToStory()
                AppSoundManager.playReactionChime()
                Toast.makeText(context, "প্রোফাইল ছবি স্টোরিতে শেয়ার হয়েছে! 🎉", Toast.LENGTH_SHORT).show()
            },
            onAddFrame = {
                showFramePickerDialog = true
            },
            onTakePhoto = {
                profileAvatarPicker.launch("image/*")
                showProfileAvatarModal = false
            },
            onUploadPhoto = {
                profileAvatarPicker.launch("image/*")
                showProfileAvatarModal = false
            },
            onSelectPhotoOnFaceFeed = {
                isSelectingForCover = false
                showSelectPhotoOnFaceFeedDialog = true
            },
            onSeeProfilePicture = {
                showViewProfilePictureDialog = true
            }
        )
    }

    // ============================================================
    // 2. BOTTOM SHEET: COVER PHOTO (Screenshot 2)
    // Take photo, Upload photo, Select photo on FaceFeed, See cover photo
    // ============================================================
    if (showCoverPhotoModal) {
        CoverPhotoBottomSheet(
            onDismiss = { showCoverPhotoModal = false },
            onTakePhoto = {
                coverPhotoPicker.launch("image/*")
                showCoverPhotoModal = false
            },
            onUploadPhoto = {
                coverPhotoPicker.launch("image/*")
                showCoverPhotoModal = false
            },
            onSelectPhotoOnFaceFeed = {
                isSelectingForCover = true
                showSelectPhotoOnFaceFeedDialog = true
            },
            onSeeCoverPhoto = {
                showViewCoverPhotoDialog = true
            }
        )
    }

    // ============================================================
    // 3. DIALOG: PROFILE FRAME PICKER (Add frame feature)
    // ============================================================
    if (showFramePickerDialog) {
        ProfileFramePickerDialog(
            user = user,
            currentFrameId = user.avatarFrame,
            onFrameSelected = { frameId ->
                viewModel.updateAvatarFrame(frameId)
            },
            onDismiss = { showFramePickerDialog = false }
        )
    }

    // ============================================================
    // 4. DIALOG: SELECT PHOTO ON FACEFEED (Replaced Facebook with FaceFeed)
    // ============================================================
    if (showSelectPhotoOnFaceFeedDialog) {
        SelectPhotoOnFaceFeedDialog(
            title = if (isSelectingForCover) "FaceFeed-এ কভার ছবি নির্বাচন করুন" else "FaceFeed-এ প্রোফাইল ছবি নির্বাচন করুন",
            isForCover = isSelectingForCover,
            onPhotoSelected = { url ->
                if (isSelectingForCover) {
                    viewModel.updateCoverPhoto(url)
                } else {
                    viewModel.updateProfileAvatar(url)
                }
            },
            onDismiss = { showSelectPhotoOnFaceFeedDialog = false }
        )
    }

    // ============================================================
    // 5. FULL SCREEN VIEWER: SEE PROFILE PICTURE
    // ============================================================
    if (showViewProfilePictureDialog) {
        FullScreenPhotoViewerDialog(
            title = "প্রোফাইল ছবি",
            imageUrl = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" },
            authorName = user.fullName,
            authorAvatar = user.avatarUrl,
            frameId = user.avatarFrame,
            isCover = false,
            onDismiss = { showViewProfilePictureDialog = false }
        )
    }

    // ============================================================
    // 6. FULL SCREEN VIEWER: SEE COVER PHOTO
    // ============================================================
    if (showViewCoverPhotoDialog) {
        FullScreenPhotoViewerDialog(
            title = "কভার ফটো",
            imageUrl = user.coverUrl.ifEmpty { "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200" },
            authorName = user.fullName,
            authorAvatar = user.avatarUrl,
            frameId = null,
            isCover = true,
            onDismiss = { showViewCoverPhotoDialog = false }
        )
    }
}

// ============================================================
// COMPONENT: Stat Item Button (Turns blue when clicked)
// User requirement: "প্রোতেক্টা অফসোনে টিপ দিলে নিলা হয়ে জাবে আর তার বিতরে ডুকাইবে"
// ============================================================
@Composable
fun StatTextButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) FacebookBlue.copy(alpha = 0.14f) else Color.Transparent
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

// ============================================================
// COMPONENT: Profile Tab Item (All / Reels / Photos)
// User requirement: "টিপ দিলে নিলা হয়ে জাবে"
// ============================================================
@Composable
fun ProfileTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
            color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FacebookBlue)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

// ============================================================
// COMPONENT: Personal Detail Row
// ============================================================
@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================
// DIALOG: 50 FOLLOWERS LIST DIALOG ("তার বিতরে ডুকাইবে")
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersDialog(
    followers: List<FollowerUser>,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit,
    onToggleFollowBack: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFollowers = remember(followers, searchQuery) {
        if (searchQuery.isBlank()) followers
        else followers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Followers (${followers.size})",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "People following Niyon Islam Sohag",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Followers
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search followers...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Followers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFollowers, key = { it.id }) { follower ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = follower.avatarUrl,
                            contentDescription = follower.name,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = follower.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "@${follower.username} • ${follower.mutualFriendsCount} mutual friends",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Follow Back Button
                        Button(
                            onClick = { onToggleFollowBack(follower.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (follower.isFollowingBack) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (follower.isFollowingBack) "Following" else "Follow back",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (follower.isFollowingBack) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Remove Button
                        IconButton(
                            onClick = { onRemove(follower.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Follower",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// DIALOG: 5 FOLLOWING LIST DIALOG ("তার বিতরে ডুকাইবে")
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingDialog(
    followingList: List<FollowerUser>,
    onDismiss: () -> Unit,
    onToggleFollowing: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Following (${followingList.size})",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Accounts followed by Niyon Islam Sohag",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(followingList, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.avatarUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (item.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FidaVerifiedBadge(size = 15.dp)
                                }
                            }
                            Text(
                                text = "@${item.username}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Unfollow / Follow Toggle Button
                        Button(
                            onClick = { onToggleFollowing(item.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.isFollowingBack) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (item.isFollowingBack) "Following" else "Follow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isFollowingBack) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// DIALOG: PROFESSIONAL DASHBOARD
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalDashboardDialog(
    user: com.example.model.User,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = FacebookBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Professional Dashboard",
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Performance (Last 28 days)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
            )

            // Analytics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Post reach",
                    value = "1.8M",
                    growth = "▲ 42%",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Post engagement",
                    value = "340.5K",
                    growth = "▲ 18%",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Net followers",
                    value = "+50",
                    growth = "▲ 100%",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "3-sec video views",
                    value = "920K",
                    growth = "▲ 35%",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Creator Tools",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ToolRow(icon = Icons.Default.MonetizationOn, title = "Monetization & Stars", subtitle = "Eligible to earn from posts and reels")
            ToolRow(icon = Icons.Default.Stars, title = "Profile recommendation", subtitle = "Your profile is recommended to new people")
            ToolRow(icon = Icons.Default.Insights, title = "Audience insights", subtitle = "Dhaka, Chittagong, Barisal & international fans")

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    growth: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(growth, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isPositive) BangladeshGreen else BangladeshRed)
        }
    }
}

@Composable
fun ToolRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================
// DIALOG: EDIT PROFILE DETAILS
// ============================================================
@Composable
fun EditProfileDetailsDialog(
    user: com.example.model.User,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, cat: String, work: String, lives: String, from: String, sch: String, col: String, ig: String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var bio by remember { mutableStateOf(user.bio) }
    var category by remember { mutableStateOf(user.category) }
    var workplace by remember { mutableStateOf(user.workplace) }
    var livesIn by remember { mutableStateOf(user.livesIn) }
    var fromCity by remember { mutableStateOf(user.fromCity) }
    var school by remember { mutableStateOf(user.school) }
    var college by remember { mutableStateOf(user.college) }
    var instagram by remember { mutableStateOf(user.instagramHandle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = workplace,
                        onValueChange = { workplace = it },
                        label = { Text("Work") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = livesIn,
                        onValueChange = { livesIn = it },
                        label = { Text("Lives in") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = fromCity,
                        onValueChange = { fromCity = it },
                        label = { Text("From") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = school,
                        onValueChange = { school = it },
                        label = { Text("School") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = college,
                        onValueChange = { college = it },
                        label = { Text("College") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = instagram,
                        onValueChange = { instagram = it },
                        label = { Text("ইউজারনেম / সোশ্যাল লিঙ্ক") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, category, workplace, livesIn, fromCity, school, college, instagram) },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================
// DIALOG: ALL FRIENDS (Full List of Friends with Search)
// ============================================================
@Composable
fun AllFriendsDialog(
    friends: List<ProfileFriend>,
    onDismiss: () -> Unit,
    onSelectFriend: (ProfileFriend) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = remember(searchQuery, friends) {
        if (searchQuery.isBlank()) friends
        else friends.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friends (${friends.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search friends...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFriends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFriend(friend) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            if (friend.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = friend.mutualFriendsCount,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onSelectFriend(friend) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = "Message",
                                tint = FacebookBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Done")
            }
        }
    )
}

// ============================================================
// DIALOG: LIFE EVENT (Life Event ক্রিয়েটর)
// ============================================================
@Composable
fun LifeEventDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, category: String, date: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("💼 Work & Career") }
    var date by remember { mutableStateOf("Today • " + java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date())) }
    var desc by remember { mutableStateOf("") }

    val categories = listOf(
        "💼 Work & Career",
        "🎓 Education",
        "❤️ Relationship",
        "🏠 Home & Family",
        "✈️ Travel & Living",
        "🏆 Milestone & Achievement"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Life Event", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Started at New Company)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Story / Description (Optional)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onPublish(title, selectedCategory, date, desc)
                    } else {
                        onPublish("New Life Event in $selectedCategory", selectedCategory, date, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("Share to Feed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================
// DIALOG: FRIEND DETAIL & QUICK ACTIONS
// ============================================================
@Composable
fun FriendDetailDialog(
    friend: ProfileFriend,
    onDismiss: () -> Unit,
    onMessage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = friend.avatarUrl,
                        contentDescription = friend.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                    if (friend.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = friend.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = friend.mutualFriendsCount,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMessage,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message")
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Friends ✓")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
