package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.Post
import com.example.model.User
import com.example.ui.components.BlockConfirmationBottomSheet
import com.example.ui.components.BlockedSuccessBottomSheet
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.components.FriendAllConnectionsDialog
import com.example.ui.components.PostCard
import com.example.ui.components.UnblockConfirmationBottomSheet
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import androidx.compose.ui.platform.LocalContext
import com.example.util.AppSoundManager
import com.example.util.MediaSaveHelper
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    user: User,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    viewModel: FaceFeedViewModel? = null
) {
    val blockedUserIds = viewModel?.blockedUserIds?.collectAsState()?.value ?: emptySet()
    var localBlocked by remember { mutableStateOf(false) }
    val isBlocked = localBlocked || blockedUserIds.contains(user.id) || (viewModel?.isUserBlocked(user.id) == true)

    var isFriend by remember { mutableStateOf(!isBlocked) }
    var isFollowing by remember { mutableStateOf(!isBlocked) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var likedPost by remember { mutableStateOf(false) }

    // Dialog states
    var showBlockDialog by remember { mutableStateOf(false) }
    var showBlockedSuccessDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showAllFriendsDialog by remember { mutableStateOf(false) }
    var selectedPostFilter by remember { mutableStateOf("All") }
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id, viewModel) {
        viewModel?.ensurePostsForUser(user)
    }

    // Fetch author posts from ViewModel so Like, Comment, Reply, and Share mutate real live posts
    val allFeedPosts = viewModel?.posts?.collectAsState()?.value ?: emptyList()
    val authorFeedPosts = allFeedPosts.filter {
        it.author.id == user.id ||
            (user.username.isNotBlank() && it.author.username.equals(user.username, ignoreCase = true)) ||
            it.author.fullName.equals(user.fullName, ignoreCase = true)
    }

    val fallbackDefaultPosts = remember(user) {
        listOf(
            Post(
                id = "post_${user.id}_photo_1",
                author = user,
                timestamp = "২ ঘণ্টা আগে • 🌍",
                text = "আজকের সুন্দর বিকেলটা বন্ধুদের সাথে চমৎকার কাটলো! সবাইকে আন্তরিক ধন্যবাদ ❤️ #FaceFeed #Friendship",
                images = listOf("https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?w=800"),
                isVideo = false,
                isReel = false,
                shareCount = 45
            ),
            Post(
                id = "post_${user.id}_video_2",
                author = user,
                timestamp = "গতকাল বিকাল ৫:৩০ • 🌍",
                text = "🎬 প্রকৃতি ও সূর্যাস্তের অপূর্ব দৃশ্য! ভিডিওটি দেখুন এবং শেয়ার করুন 🌿",
                images = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
                isVideo = true,
                videoTitle = "প্রকৃতি ও সূর্যাস্তের অপূর্ব দৃশ্য",
                videoDuration = "03:45",
                reachViews = "45.2K",
                shareCount = 89
            ),
            Post(
                id = "post_${user.id}_reel_3",
                author = user,
                timestamp = "২ দিন আগে • 🌍",
                text = "🔥 নতুন রিল ভিডিও: নিজের প্রতি বিশ্বাস রাখো, সফলতা আসবেই ইনশাআল্লাহ! 🌟 #Reels #Viral",
                images = listOf("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800"),
                isVideo = true,
                isReel = true,
                videoTitle = "সফলতার অনুপ্রেরণা",
                reachViews = "120K",
                musicInfo = "🎵 Original Audio - ${user.fullName}",
                shareCount = 210
            )
        )
    }

    val combinedUserPosts = if (viewModel != null && authorFeedPosts.isNotEmpty()) {
        authorFeedPosts.distinctBy { it.id }
    } else {
        (authorFeedPosts + fallbackDefaultPosts).distinctBy { it.id }
    }

    val filteredPosts = when (selectedPostFilter) {
        "Photos" -> combinedUserPosts.filter { it.images.isNotEmpty() && !it.isVideo && !it.isReel }
        "Videos" -> combinedUserPosts.filter { it.isVideo && !it.isReel }
        "Reels" -> combinedUserPosts.filter { it.isReel }
        "Stories" -> combinedUserPosts.filter { it.isFaceClip || it.text.contains("স্টোরি") || it.text.contains("Story") }
        "Live" -> combinedUserPosts.filter { it.text.contains("লাইভ") || it.text.contains("Live") || (it.videoDuration != null && it.videoDuration!!.contains(":")) }
        else -> combinedUserPosts
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
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
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
                    IconButton(
                        onClick = {
                            if (isBlocked) {
                                showUnblockDialog = true
                            } else {
                                onAudioCall()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = if (isBlocked) Color.Gray.copy(alpha = 0.4f) else FacebookBlue
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isBlocked) {
                                showUnblockDialog = true
                            } else {
                                onVideoCall()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = if (isBlocked) Color.Gray.copy(alpha = 0.4f) else FacebookBlue
                        )
                    }
                    Box {
                        val context = LocalContext.current
                        val profileLink = remember(user) {
                            viewModel?.buildProfileLink(user)
                                ?: "https://fotofilm.app/u/${user.username.ifBlank { user.id }}"
                        }
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("আইডির লিংক কপি করুন (Copy ID Link)") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = FacebookBlue) },
                                onClick = {
                                    showMoreMenu = false
                                    MediaSaveHelper.copyToClipboard(context, "Profile Link", profileLink)
                                    AppSoundManager.playReactionChime()
                                    toastMessage = "🔗 আইডির লিংক কপি হয়েছে: $profileLink"
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("আইডির লিংক শেয়ার করুন (Share Link)") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = BangladeshGreen) },
                                onClick = {
                                    showMoreMenu = false
                                    MediaSaveHelper.shareTextOrMediaLink(
                                        context,
                                        "${user.fullName}-এর FotoFilm প্রোফাইল লিংক: $profileLink",
                                        subject = user.fullName
                                    )
                                }
                            )
                            if (isBlocked) {
                                DropdownMenuItem(
                                    text = { Text("Unblock ${user.fullName}") },
                                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue) },
                                    onClick = {
                                        showMoreMenu = false
                                        showUnblockDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Block ${user.fullName}") },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
                                    onClick = {
                                        showMoreMenu = false
                                        showBlockDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 60.dp)
            ) {
                // Cover & Profile Avatar Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    // Cover Photo
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000",
                        contentDescription = "Cover photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(FacebookBlue.copy(alpha = 0.5f), FacebookBlue)
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )

                    // Profile Avatar Overlapping
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AsyncImage(
                            model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500" },
                            contentDescription = user.fullName,
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (user.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.5.dp)
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
                }

                // Name and Bio
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FidaVerifiedBadge(size = 20.dp)
                    }

                    val profileCtx = LocalContext.current
                    val userIdLink = remember(user) {
                        viewModel?.buildProfileLink(user)
                            ?: "https://fotofilm.app/u/${user.username.ifBlank { user.id }}"
                    }
                    Text(
                        text = "@${user.username} • Digital Creator",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FacebookBlue.copy(alpha = 0.10f),
                        modifier = Modifier.clickable {
                            MediaSaveHelper.copyToClipboard(profileCtx, "Profile Link", userIdLink)
                            AppSoundManager.playReactionChime()
                            toastMessage = "🔗 আইডির লিংক কপি হয়েছে: $userIdLink"
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = "Profile Link", tint = FacebookBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = userIdLink,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FacebookBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = FacebookBlue, modifier = Modifier.size(13.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Live life with positivity, faith & love ❤️ | FaceFeed Family Member | Barisal, Bangladesh",
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Primary Action Buttons: Blocked State vs Normal State
                if (isBlocked) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "You blocked ${user.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Unblock to message, call, or see posts",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showUnblockDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Unblock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isFriend = !isFriend
                                toastMessage = if (isFriend) "Friendship confirmed" else "Unfriended"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFriend) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isFriend) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (isFriend) MaterialTheme.colorScheme.onSurface else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFriend) "Friends ✓" else "Add Friend",
                                color = if (isFriend) MaterialTheme.colorScheme.onSurface else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onMessage,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Message", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        FilledTonalIconButton(
                            onClick = onAudioCall,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = FacebookBlue)
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Personal Details (9 features matching user requirements)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ProfileDetailItem(
                        icon = Icons.Default.Work,
                        label = "Works at Digital Content & Media Creator"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.School,
                        label = "Studied at লাউপাডা সাগর সৈকত মাধ্যমিক বিদ্যালয়"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.AccountBalance,
                        label = "Went to Taltali Degree College .তালতলী ডিগ্রী কলেজ"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.LocationOn,
                        label = "Lives in Taltali, Barguna"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.Home,
                        label = "From Barisal, Bangladesh"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.Favorite,
                        label = "Married"
                    )
                    ProfileDetailItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Joined May 2020"
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // Friends Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Friends", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("6,420 friends • 12 mutual friends", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { showAllFriendsDialog = true }) {
                            Text("See all", color = FacebookBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(SampleData.profileFriends.take(6)) { friend ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { showAllFriendsDialog = true }
                            ) {
                                AsyncImage(
                                    model = friend.avatarUrl,
                                    contentDescription = friend.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = friend.name,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                // Posts Section with Category Filter Chips (Posts, Photos, Videos, Reels, Stories, Live)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Posts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredPosts.size} টি পোস্ট",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Post Category Filter Chips
                    val postCategories = listOf(
                        "All" to "📝 সকল পোস্ট",
                        "Photos" to "📸 ছবি",
                        "Videos" to "🎥 ভিডিও",
                        "Reels" to "🎬 রিল ভিডিও",
                        "Stories" to "⏱️ স্টোরি",
                        "Live" to "🔴 লাইভ ভিডিও"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(postCategories) { (filterKey, label) ->
                            val isSelected = selectedPostFilter == filterKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPostFilter = filterKey },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BangladeshGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    if (filteredPosts.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "এই ক্যাটাগরিতে কোনো পোস্ট নেই",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        filteredPosts.forEach { postItem ->
                            if (viewModel != null) {
                                PostCard(
                                    post = postItem,
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            } else {
                                // High quality fallback post card
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Header
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            AsyncImage(
                                                model = postItem.author.avatarUrl,
                                                contentDescription = postItem.author.fullName,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(postItem.author.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    if (postItem.author.isVerified) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        FidaVerifiedBadge(size = 14.dp)
                                                    }
                                                }
                                                Text(postItem.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(postItem.text, fontSize = 14.sp, lineHeight = 20.sp)

                                        if (postItem.images.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (postItem.isVideo || postItem.isReel) {
                                                            activeVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                                        }
                                                    }
                                            ) {
                                                AsyncImage(
                                                    model = postItem.images.first(),
                                                    contentDescription = "Post media",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                if (postItem.isVideo || postItem.isReel) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.3f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = BangladeshGreen.copy(alpha = 0.9f),
                                                            modifier = Modifier.size(48.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Play Video",
                                                                tint = Color.White,
                                                                modifier = Modifier
                                                                    .padding(10.dp)
                                                                    .fillMaxSize()
                                                            )
                                                        }
                                                    }

                                                    if (postItem.videoDuration != null) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color.Black.copy(alpha = 0.7f),
                                                            modifier = Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .padding(8.dp)
                                                        ) {
                                                            Text(
                                                                text = postItem.videoDuration!!,
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            TextButton(onClick = { toastMessage = "লাইক দেওয়া হয়েছে" }) {
                                                Icon(Icons.Default.ThumbUpOffAlt, contentDescription = "Like", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Like (${postItem.totalReactions + 1})", fontSize = 12.sp)
                                            }

                                            TextButton(onClick = { toastMessage = "কমেন্ট বক্স খোলা হয়েছে" }) {
                                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Comment (${postItem.comments.size})", fontSize = 12.sp)
                                            }

                                            TextButton(onClick = { toastMessage = "পোস্ট শেয়ার করা হয়েছে" }) {
                                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Share (${postItem.shareCount})", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Toast
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

            // Screen 1: Block Confirmation Bottom Sheet
            if (showBlockDialog) {
                BlockConfirmationBottomSheet(
                    user = user,
                    onDismiss = { showBlockDialog = false },
                    onBlock = {
                        localBlocked = true
                        viewModel?.blockUser(user.id)
                        showBlockDialog = false
                        showBlockedSuccessDialog = true
                    }
                )
            }

            // Screen 2: Blocked Success Bottom Sheet
            if (showBlockedSuccessDialog) {
                BlockedSuccessBottomSheet(
                    user = user,
                    onDismiss = { showBlockedSuccessDialog = false },
                    onDeleteChat = {
                        showBlockedSuccessDialog = false
                        viewModel?.deleteConversation(user.id)
                        onBack()
                    }
                )
            }

            // Screen 5: Unblock Confirmation Bottom Sheet
            if (showUnblockDialog) {
                UnblockConfirmationBottomSheet(
                    user = user,
                    onDismiss = { showUnblockDialog = false },
                    onUnblock = {
                        localBlocked = false
                        viewModel?.unblockUser(user.id)
                        showUnblockDialog = false
                        toastMessage = "${user.fullName} has been unblocked"
                    }
                )
            }

            // All Connections Dialog (See All Friends, Mutual, Followers, Following)
            if (showAllFriendsDialog) {
                FriendAllConnectionsDialog(
                    user = user,
                    onDismiss = { showAllFriendsDialog = false },
                    onMessageUser = {
                        showAllFriendsDialog = false
                        onMessage()
                    }
                )
            }

            // Video & Reel Player Dialog
            if (activeVideoUrl != null) {
                VideoPlayerDialog(
                    videoUrl = activeVideoUrl!!,
                    title = user.fullName,
                    authorName = user.fullName,
                    viewModel = viewModel,
                    onDismiss = { activeVideoUrl = null }
                )
            }
        }
    }
}

@Composable
fun ProfileDetailItem(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
