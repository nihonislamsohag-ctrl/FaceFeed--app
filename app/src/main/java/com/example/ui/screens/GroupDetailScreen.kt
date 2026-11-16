package com.example.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.Post
import com.example.model.PostAudioTrack
import com.example.model.SocialGroup
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel
import androidx.compose.ui.window.DialogProperties

/**
 * Group Detail / Group ID Screen ("গ্রুপ আইডি")
 * Exactly matches Screenshot 4 (Screenshot_20261105_152244_578b2e20.jpg)
 *
 * - Cover Photo of "নিদ্রা ডিসি পয়েন্ট (তালতলী উপজেলা বরগুনা)" with EDIT button
 * - Group Title, 1K members, overlapping avatars, "+ Invite" button
 * - Action pills: Manage (with dot), Videos, Photos, Announcements
 * - Post Composer: "Write something...", Feeling, Poll
 * - Feed: "Most relevant", Topic filter chips (All topics, Need advice, Seeking answers)
 * - Group posts with interactive like, comment, and share
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    group: SocialGroup,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var showManageRolesDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var preselectedAudioForPost by remember { mutableStateOf<PostAudioTrack?>(null) }
    var showEditCoverDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var selectedTopicFilter by remember { mutableStateOf("All topics") }
    var customCoverUri by remember { mutableStateOf<Uri?>(null) }

    // Cover photo selection via Android Photo Picker
    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customCoverUri = uri
            toastMessage = "কভার ছবি সফলভাবে আপডেট করা হয়েছে! 📸"
        }
    }

    // Local group posts list
    val groupPosts = remember {
        mutableStateListOf(
            Post(
                id = "grp_post_1",
                author = User(
                    id = "usr_maruf",
                    username = "maruf_maruf",
                    fullName = "Maruf Maruf",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"
                ),
                timestamp = "3d • 🌐",
                text = "নিদ্রা ডিসি পয়েন্টের অপরূপ রাতের দৃশ্য! ক্যাম্পিং এবং বন্ধুদের সাথে চমৎকার স্মৃতি। সবাইকে ভ্রমণের আমন্ত্রণ রইল! 🏕️✨ #NidraDCPoint #Taltali #Barguna",
                images = listOf("https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800"),
                audioTrack = SampleData.sampleAudioTracks.find { it.category == "বাঁশির সুর" },
                musicInfo = "পল্লী সুর - দূর গাঁয়ের মায়াবী বাঁশির তান • ওস্তাদ আলাউদ্দিন খাঁ স্মৃতি বাদক",
                shareCount = 15
            ),
            Post(
                id = "grp_post_2",
                author = User(
                    id = "usr_tasmim",
                    username = "tasmim_tinni",
                    fullName = "Tasmim Tinni",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
                ),
                timestamp = "17 hours ago • 🌐",
                text = "বরগুনার তালতলী নিদ্রার চরের সূর্যাস্ত ও সমুদ্রের গর্জন। অসাধারণ এক অভিজ্ঞতা! 🌊🌴 সবাই কেমন আছেন?",
                images = listOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"),
                audioTrack = SampleData.sampleAudioTracks.find { it.category == "গজল" },
                musicInfo = "হাসবি রাব্বি জাল্লাল্লাহ • সামি ইউসুফ ও বাংলা শিল্পীগোষ্ঠী",
                shareCount = 28
            )
        )
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
            TopAppBar(
                title = {
                    Text(
                        text = group.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Options")
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("গ্রুপ শেয়ার করুন (Share)") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                toastMessage = "গ্রুপ লিংক কপি করা হয়েছে! 🔗"
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("গ্রুপ ম্যানেজ (Manage)") },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                showManageRolesDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("গ্রুপ নোটিফিকেশন") },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            onClick = {
                                showOptionsMenu = false
                                toastMessage = "নোটিফিকেশন চালু করা হয়েছে"
                            }
                        )
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
                // 1. Cover Photo Banner with EDIT Button (Screenshot 4)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        if (customCoverUri != null) {
                            AsyncImage(
                                model = customCoverUri,
                                contentDescription = "Group Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (group.name.contains("নিদ্রা") || group.name.contains("Nidra")) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.img_nidra_dc_point_cover),
                                contentDescription = "Nidra DC Point Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = group.coverUrl.ifEmpty { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800" },
                                contentDescription = group.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // EDIT Button at bottom right of cover photo
                        Surface(
                            onClick = {
                                coverPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit cover",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "EDIT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // 2. Group Info Section (Title, Subtitle, Members, Invite)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = group.name,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Public group • ${if (group.name.contains("নিদ্রা")) "1K" else "${group.memberCount}"} members",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Members Avatar Row & "+ Invite" Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Overlapping circular member avatars
                            val memberAvatars = listOf(
                                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100",
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100",
                                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=100"
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                memberAvatars.forEachIndexed { idx, avatarUrl ->
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .offset(x = (-idx * 10).dp)
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // "+ Invite" Blue Button (Screenshot 4)
                            Button(
                                onClick = { showInviteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Invite",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Invite",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Action Pill Buttons Row (Manage, Videos, Photos, Announcements)
                item {
                    val actionPills = listOf(
                        "Manage" to true,
                        "Videos •" to false,
                        "Photos •" to false,
                        "Announcements •" to false,
                        "Files •" to false,
                        "Events •" to false
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(actionPills) { (label, isManage) ->
                            Surface(
                                onClick = {
                                    if (isManage) {
                                        showManageRolesDialog = true
                                    } else {
                                        toastMessage = "$label সেকশন ফিল্টার করা হয়েছে"
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isManage) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(FacebookBlue)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                }

                // 4. Post Composer Box (Write something..., Feeling, Poll)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" },
                                contentDescription = currentUser.fullName,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                onClick = { showCreatePostDialog = true },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Write something...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

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

                        // Sub-action buttons: Feeling, Poll, Audio/Ghazal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TextButton(onClick = { showCreatePostDialog = true }) {
                                Text("😊", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Feeling",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            TextButton(onClick = {
                                toastMessage = "পোল অপশন চালু করা হয়েছে 📊"
                                showCreatePostDialog = true
                            }) {
                                Text("📊", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Poll",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            TextButton(onClick = {
                                showCreatePostDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF137333),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "গান ও গজল",
                                    fontSize = 12.sp,
                                    color = Color(0xFF137333),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 6.dp
                    )
                }

                // 4.5. Group Audio & Ghazal Hub: অডিও গান, গজল, মিউজিক, বাঁশির সুর, ডিজে গান, হিন্দি গান, জন্মদিন গান
                item {
                    GroupAudioTracksShelf(
                        onSelectTrackForPost = { track ->
                            preselectedAudioForPost = track
                            showCreatePostDialog = true
                        }
                    )
                }

                // 5. Feed Header & Topic Filter Chips (Screenshot 4)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Most relevant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val topicChips = listOf(
                            "All topics",
                            "Need advice",
                            "Seeking answers",
                            "Discussion",
                            "Announcements",
                            "Travel"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            items(topicChips) { topic ->
                                val isSelected = selectedTopicFilter == topic
                                Surface(
                                    onClick = { selectedTopicFilter = topic },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) Color(0xFFE7F3FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (isSelected) BorderStroke(1.dp, FacebookBlue) else null
                                ) {
                                    Text(
                                        text = topic,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Group Posts Feed
                items(groupPosts) { post ->
                    GroupPostCard(
                        post = post,
                        groupName = group.name,
                        onLike = {
                            toastMessage = "লাইক দেওয়া হয়েছে"
                        },
                        onComment = {
                            toastMessage = "কমেন্ট বক্স খোলা হয়েছে"
                        },
                        onShare = {
                            toastMessage = "পোস্ট শেয়ার করা হয়েছে"
                        }
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

    // Sub-dialog 1: Manage Roles & Policies
    if (showManageRolesDialog) {
        GroupRolesManagementDialog(
            group = group,
            viewModel = viewModel,
            onDismiss = { showManageRolesDialog = false }
        )
    }

    // Sub-dialog 2: Invite Members Dialog
    if (showInviteDialog) {
        GroupInviteMembersDialog(
            group = group,
            onDismiss = { showInviteDialog = false },
            onInviteSent = { count ->
                showInviteDialog = false
                toastMessage = "🎉 $count জন বন্ধুকে গ্রুপে ইনভাইট পাঠানো হয়েছে!"
            }
        )
    }

    // Sub-dialog 3: Create Post inside Group Dialog
    if (showCreatePostDialog) {
        GroupCreatePostDialog(
            groupName = group.name,
            currentUser = currentUser,
            initialAudioTrack = preselectedAudioForPost,
            onDismiss = {
                showCreatePostDialog = false
                preselectedAudioForPost = null
            },
            onPostCreated = { text, imageUri, audioTrack ->
                val newPost = Post(
                    id = "post_${System.currentTimeMillis()}",
                    author = currentUser,
                    timestamp = "Just now • 🌐",
                    text = text,
                    images = if (imageUri != null) listOf(imageUri.toString()) else emptyList(),
                    audioTrack = audioTrack,
                    musicInfo = audioTrack?.let { "${it.title} • ${it.artist}" }
                )
                groupPosts.add(0, newPost)
                showCreatePostDialog = false
                preselectedAudioForPost = null
                toastMessage = if (audioTrack != null) {
                    "গ্রুপে অডিও পোস্ট সফলভাবে প্রকাশ করা হয়েছে! 🎶✅"
                } else {
                    "গ্রুপে পোস্ট সফলভাবে প্রকাশ করা হয়েছে! ✅"
                }
            }
        )
    }

    // Sub-dialog 4: Search inside Group
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("গ্রুপে অনুসন্ধান করুন") },
            text = {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("পোস্ট, সদস্য বা বিষয় খুঁজুন...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSearchDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("সার্চ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Group Post Card matching Screenshot 4
 */
@Composable
fun GroupPostCard(
    post: Post,
    groupName: String,
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
            // Post Author Header (Author Name • Follow, + Add topic, 3d • 🌐)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100" },
                    contentDescription = post.author.fullName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.author.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Follow",
                            color = FacebookBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "+ Add topic",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• ${post.timestamp}",
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

            // Post Text
            Text(
                text = post.text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Post Image
            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.images.first(),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Post Attached Audio Track (Audio songs, Gojol, Music, Flute, DJ, Hindi, Birthday)
            if (post.audioTrack != null) {
                Spacer(modifier = Modifier.height(10.dp))
                GroupPostAudioPlayer(audioTrack = post.audioTrack)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reaction Count bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️👍", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLiked) "246" else "245",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "38 comments • ${post.shareCount} shares",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Like, Comment, Share Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextButton(onClick = {
                    isLiked = !isLiked
                    onLike()
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (isLiked) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Like",
                        color = if (isLiked) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = onComment) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comment",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Invite Friends to Group Dialog
 */
@Composable
fun GroupInviteMembersDialog(
    group: SocialGroup,
    onDismiss: () -> Unit,
    onInviteSent: (Int) -> Unit
) {
    val selectedFriends = remember { mutableStateListOf<String>() }

    val friends = listOf(
        SampleData.friendMdMohiuddin,
        SampleData.friendRahim,
        SampleData.friendJoyAhmed,
        SampleData.friendTanvir,
        SampleData.friendSamrat,
        SampleData.friendSadia
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গ্রুপে বন্ধুদের ইনভাইট করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    friends.forEach { friend ->
                        val isSelected = selectedFriends.contains(friend.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedFriends.remove(friend.id)
                                    else selectedFriends.add(friend.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.fullName,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = friend.fullName,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedFriends.add(friend.id)
                                    else selectedFriends.remove(friend.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val count = if (selectedFriends.isEmpty()) 1 else selectedFriends.size
                        onInviteSent(count)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ইনভাইট পাঠান (${if (selectedFriends.isEmpty()) "সবাইকে" else "${selectedFriends.size} জন"})")
                }
            }
        }
    }
}

/**
 * Interactive Audio Player Card for Group Posts
 * Plays real synthesized sound via AppSoundManager
 */
@Composable
fun GroupPostAudioPlayer(
    audioTrack: PostAudioTrack
) {
    val isAudioPlaying by AppSoundManager.isPlaying.collectAsState()
    val playingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()
    val isThisTrackPlaying = isAudioPlaying && playingTrackId == audioTrack.id

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isThisTrackPlaying) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isThisTrackPlaying) Color(0xFF137333) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art or vinyl record icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isThisTrackPlaying) Color(0xFFE8F5E9) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!audioTrack.coverArtUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = audioTrack.coverArtUrl,
                        contentDescription = audioTrack.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF137333),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioTrack.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = audioTrack.artist,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = audioTrack.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF137333),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isThisTrackPlaying) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "🎶 এখন বাজছে... (Playing Audio)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137333)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Button
            IconButton(
                onClick = {
                    if (isThisTrackPlaying) {
                        AppSoundManager.pauseAudio()
                    } else {
                        AppSoundManager.playAudio(audioTrack)
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF137333))
            ) {
                Icon(
                    imageVector = if (isThisTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisTrackPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Dedicated Group Audio & Ghazal Music Hub
 * Allows group members to browse and play the 7 requested categories:
 * 1. অডিও গান
 * 2. গজল
 * 3. মিউজিক
 * 4. বাঁশির সুর
 * 5. ডিজে গান
 * 6. হিন্দি গান
 * 7. জন্মদিন গান
 */
@Composable
fun GroupAudioTracksShelf(
    onSelectTrackForPost: (PostAudioTrack) -> Unit
) {
    val isAudioPlaying by AppSoundManager.isPlaying.collectAsState()
    val playingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()

    var selectedCategory by remember { mutableStateOf("সব (All)") }

    val categories = listOf(
        "সব (All)",
        "অডিও গান",
        "গজল",
        "মিউজিক",
        "বাঁশির সুর",
        "ডিজে গান",
        "হিন্দি গান",
        "জন্মদিন গান"
    )

    val allTracks = SampleData.sampleAudioTracks
    val filteredTracks = remember(selectedCategory) {
        when (selectedCategory) {
            "সব (All)" -> allTracks
            "অডিও গান" -> allTracks.filter { it.category == "অডিও গান" || it.category.contains("গান") }
            "গজল" -> allTracks.filter { it.category == "গজল" || it.category.contains("গজল") }
            "মিউজিক" -> allTracks.filter { it.category == "মিউজিক" || it.category.contains("মিউজিক") }
            "বাঁশির সুর" -> allTracks.filter { it.category == "বাঁশির সুর" || it.category.contains("বাঁশি") }
            "ডিজে গান" -> allTracks.filter { it.category == "ডিজে গান" || it.category.contains("ডিজে") }
            "হিন্দি গান" -> allTracks.filter { it.category == "হিন্দি গান" || it.category.contains("হিন্দি") }
            "জন্মদিন গান" -> allTracks.filter { it.category == "জন্মদিন গান" || it.category == "শুভ জন্মদিন" || it.category.contains("জন্মদিন") }
            else -> allTracks
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "গ্রুপ অডিও গান ও গজল 🎶",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "গান • গজল • মিউজিক • বাঁশির সুর • ডিজে • হিন্দি • জন্মদিন",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "${filteredTracks.size} টি গান",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137333),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7 Categories Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF137333),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Track Cards Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTracks) { track ->
                    val isPlaying = isAudioPlaying && playingTrackId == track.id
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlaying) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPlaying) Color(0xFF137333) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = track.coverArtUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
                                    contentDescription = track.title,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = track.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF137333),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            AppSoundManager.pauseAudio()
                                        } else {
                                            AppSoundManager.playAudio(track)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) Color(0xFF137333) else Color(0xFF137333).copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = if (isPlaying) Color.White else Color(0xFF137333),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Button(
                                    onClick = { onSelectTrackForPost(track) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = "পোস্ট করুন",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 6.dp
            )
        }
    }
}

/**
 * Audio Track Selection Dialog with 7 Categories:
 * 1. অডিও গান
 * 2. গজল
 * 3. মিউজিক
 * 4. বাঁশির সুর
 * 5. ডিজে গান
 * 6. হিন্দি গান
 * 7. জন্মদিন গান
 */
@Composable
fun GroupAudioPickerSheet(
    onDismiss: () -> Unit,
    onTrackSelected: (PostAudioTrack) -> Unit
) {
    val isAudioPlaying by AppSoundManager.isPlaying.collectAsState()
    val playingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()

    var selectedCategory by remember { mutableStateOf("সব (All)") }

    val categories = listOf(
        "সব (All)",
        "অডিও গান",
        "গজল",
        "মিউজিক",
        "বাঁশির সুর",
        "ডিজে গান",
        "হিন্দি গান",
        "জন্মদিন গান"
    )

    val allTracks = SampleData.sampleAudioTracks
    val filteredTracks = remember(selectedCategory) {
        when (selectedCategory) {
            "সব (All)" -> allTracks
            "অডিও গান" -> allTracks.filter { it.category == "অডিও গান" || it.category.contains("গান") }
            "গজল" -> allTracks.filter { it.category == "গজল" || it.category.contains("গজল") }
            "মিউজিক" -> allTracks.filter { it.category == "মিউজিক" || it.category.contains("মিউজিক") }
            "বাঁশির সুর" -> allTracks.filter { it.category == "বাঁশির সুর" || it.category.contains("বাঁশি") }
            "ডিজে গান" -> allTracks.filter { it.category == "ডিজে গান" || it.category.contains("ডিজে") }
            "হিন্দি গান" -> allTracks.filter { it.category == "হিন্দি গান" || it.category.contains("হিন্দি") }
            "জন্মদিন গান" -> allTracks.filter { it.category == "জন্মদিন গান" || it.category == "শুভ জন্মদিন" || it.category.contains("জন্মদিন") }
            else -> allTracks
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অডিও গান ও গজল নির্বাচন করুন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 7 Categories Chips Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF137333),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Tracks List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTracks) { track ->
                        val isPlaying = isAudioPlaying && playingTrackId == track.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPlaying) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (isPlaying) Color(0xFF137333) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = track.coverArtUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
                                    contentDescription = track.title,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = track.artist,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = track.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF137333),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Play Preview
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            AppSoundManager.pauseAudio()
                                        } else {
                                            AppSoundManager.playAudio(track)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) Color(0xFF137333) else Color(0xFF137333).copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = if (isPlaying) Color.White else Color(0xFF137333),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Select Track Button
                                Button(
                                    onClick = { onTrackSelected(track) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("যুক্ত করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Group Create Post Dialog (Matching Screenshot 3)
 * With Photo attachment, 7-category Audio Track & Ghazal attachment, and green Post button
 */
@Composable
fun GroupCreatePostDialog(
    groupName: String,
    currentUser: User,
    initialAudioTrack: PostAudioTrack? = null,
    onDismiss: () -> Unit,
    onPostCreated: (String, Uri?, PostAudioTrack?) -> Unit
) {
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioTrack by remember { mutableStateOf<PostAudioTrack?>(initialAudioTrack) }
    var showAudioPickerSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    if (showAudioPickerSheet) {
        GroupAudioPickerSheet(
            onDismiss = { showAudioPickerSheet = false },
            onTrackSelected = { track ->
                selectedAudioTrack = track
                showAudioPickerSheet = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header (Matching Screenshot 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গ্রুপে পোস্ট করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Author Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = currentUser.fullName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(currentUser.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("পোস্ট হচ্ছে: $groupName", fontSize = 11.sp, color = FacebookBlue)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Field
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("গ্রুপে আপনার মতামত বা আপডেট লিখুন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selected Image Preview
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(26.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Selected Audio Track Preview Card
                if (selectedAudioTrack != null) {
                    val track = selectedAudioTrack!!
                    val isAudioPlaying by AppSoundManager.isPlaying.collectAsState()
                    val playingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()
                    val isPlaying = isAudioPlaying && playingTrackId == track.id

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF137333).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF137333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.category}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF137333),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Play preview
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        AppSoundManager.pauseAudio()
                                    } else {
                                        AppSoundManager.playAudio(track)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = Color(0xFF137333),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Remove audio
                            IconButton(
                                onClick = { selectedAudioTrack = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons (Photo, Audio, Post matching Screenshot 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Picker Button
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ছবি যোগ করুন", fontSize = 11.sp)
                    }

                    // Audio & Gojol Button (The 7 categories)
                    OutlinedButton(
                        onClick = { showAudioPickerSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অডিও/গজল", fontSize = 11.sp, color = Color(0xFF137333), fontWeight = FontWeight.Bold)
                    }

                    // Post Submit Button (Green button matching Screenshot 3)
                    Button(
                        onClick = {
                            if (postText.isNotBlank() || selectedImageUri != null || selectedAudioTrack != null) {
                                onPostCreated(postText, selectedImageUri, selectedAudioTrack)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("পোস্ট করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
