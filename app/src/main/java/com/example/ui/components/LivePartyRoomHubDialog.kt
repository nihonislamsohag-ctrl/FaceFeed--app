package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.SocialGroup
import com.example.model.User
import com.example.viewmodel.FaceFeedViewModel

/**
 * Live Party Room Hub Screen
 * Exactly matches Attachment: IMG_20261104_212559_59ec2982.jpg
 *
 * Features:
 *  - Header Tabs: "Room" (active with blue underline indicator) & "Explore"
 *  - Search, Notification, and Profile actions
 *  - "My Room" section with horizontal scrolling party room cards & live member count
 *  - "Recommended Room" category filter chips: 🔥 Trending, 📍 Nearby, 🎲 লুডু, 🎮 গেম, 🎤 ট্যালেন্ট, 💖 ডেটিং
 *  - Recommended room cards (Rafi Islam's Room, 💖–ālįşħā–😄, Tumi sudhu Amar's Room)
 *  - "NEWBIE GROWTH TASK" cartoon promotional banner
 *  - DYNAMIC GROUPS INTEGRATION: Every group created by any user automatically appears in
 *    "My Room" and the live room feed with its name, profile image, online status indicator (🟢 অনলাইনে আছে),
 *    and member count. Clicking any room enters the live voice party room!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePartyRoomHubDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allGroups by viewModel.groups.collectAsState()

    var selectedTab by remember { mutableStateOf("Room") } // "Room" or "Explore"
    var selectedFilterCategory by remember { mutableStateOf("Trending") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // State for opening a specific live voice party room & live online entry/exit tracking
    var activePartyRoom by remember { mutableStateOf<PartyRoomData?>(null) }
    var activeEnteredRoomKey by remember { mutableStateOf<String?>(null) }
    var joinedRoomKeys by remember { mutableStateOf(setOf<String>()) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showLudoKingGame by remember { mutableStateOf(false) }
    var showLudoPolicyDialog by remember { mutableStateOf(false) }
    var ludoSelectedMode by remember { mutableStateOf<GameMode?>(null) }

    val presetRoomsOnlineBase = 52
    val totalGroupsOnlineBase = remember(allGroups) {
        allGroups.sumOf { it.memberCount } + presetRoomsOnlineBase
    }
    val extraEnteredFromActive = if (activeEnteredRoomKey != null && !joinedRoomKeys.contains(activeEnteredRoomKey)) 1 else 0
    val ludoEntryDelta = if (showLudoKingGame && !joinedRoomKeys.contains("ludo_king_live")) 1 else 0
    val exploreLiveOnlineCount = totalGroupsOnlineBase + joinedRoomKeys.size + extraEnteredFromActive + ludoEntryDelta

    fun toggleGroupMembership(roomKey: String, roomTitle: String) {
        val alreadyJoined = joinedRoomKeys.contains(roomKey)
        joinedRoomKeys = if (alreadyJoined) {
            joinedRoomKeys - roomKey
        } else {
            joinedRoomKeys + roomKey
        }
        val updatedTotal = totalGroupsOnlineBase + joinedRoomKeys.size + extraEnteredFromActive + ludoEntryDelta
        Toast.makeText(
            context,
            if (alreadyJoined) "🔴 \"$roomTitle\" থেকে বের হয়েছেন • লাইনে: $updatedTotal জন"
            else "🟢 \"$roomTitle\" গ্রুপে প্রবেশ করেছেন • লাইনে: $updatedTotal জন",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun openRoom(roomKey: String, data: PartyRoomData) {
        activeEnteredRoomKey = roomKey
        val effectiveCount = data.memberCount + 1
        activePartyRoom = data.copy(memberCount = effectiveCount)
    }

    // If Ludo Policy Dialog is opened:
    if (showLudoPolicyDialog) {
        LudoGroupPolicyDialog(
            viewModel = viewModel,
            onDismiss = { showLudoPolicyDialog = false },
            onLaunchGame = { mode ->
                showLudoPolicyDialog = false
                ludoSelectedMode = mode
                showLudoKingGame = true
            }
        )
    }

    // If Ludo King is opened, launch the full LudoKingGroupDialog
    if (showLudoKingGame) {
        LudoKingGroupDialog(
            viewModel = viewModel,
            initialMode = ludoSelectedMode,
            onDismiss = {
                showLudoKingGame = false
                ludoSelectedMode = null
            }
        )
        return
    }

    // If an active party room is chosen, show the full 10-seat live party voice room
    if (activePartyRoom != null) {
        FriendsHangoutPartyRoomDialog(
            viewModel = viewModel,
            initialRoomName = activePartyRoom!!.title,
            initialAvatarUrl = activePartyRoom!!.coverUrl,
            initialMemberCount = activePartyRoom!!.memberCount,
            onDismiss = {
                activePartyRoom = null
                activeEnteredRoomKey = null
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ----------------------------------------------------
                // 1. TOP HEADER: Room | Explore & Action Icons
                // ----------------------------------------------------
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back / Close Icon
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF1C1B1F)
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            // Room Tab
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedTab = "Room" }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Room",
                                    fontSize = 19.sp,
                                    fontWeight = if (selectedTab == "Room") FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == "Room") Color(0xFF1C1B1F) else Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (selectedTab == "Room") {
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(3.dp)
                                            .background(Color(0xFF00B0FF), RoundedCornerShape(2.dp))
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Explore Tab + Live Online Count Badge right beside Explore
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { selectedTab = "Explore" }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Explore",
                                        fontSize = 17.sp,
                                        fontWeight = if (selectedTab == "Explore") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == "Explore") Color(0xFF1C1B1F) else Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (selectedTab == "Explore") {
                                        Box(
                                            modifier = Modifier
                                                .width(26.dp)
                                                .height(3.dp)
                                                .background(Color(0xFF00B0FF), RoundedCornerShape(2.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    border = BorderStroke(0.8.dp, Color(0xFF2E7D32).copy(alpha = 0.45f)),
                                    modifier = Modifier.testTag("explore_online_count_badge")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(Color(0xFF00C853), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$exploreLiveOnlineCount জন",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Action Icons: 🔍 Search, 🔔 Notification, 👤 Profile
                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF2C2C2C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "লাইভ রুম নোটিফিকেশন চালু আছে ($exploreLiveOnlineCount জন লাইনে)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF2C2C2C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "প্রোফাইল: ${currentUser.fullName}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF2C2C2C),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Search Bar if active
                        AnimatedVisibility(visible = isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("রুম বা গ্রুপের নাম সার্চ করুন...", fontSize = 14.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // ----------------------------------------------------
                // 2. MAIN SCROLLABLE CONTENT
                // ----------------------------------------------------
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                ) {

                    // ----------------------------------------------------
                    // Section A: "My Room" (হরিজন্টাল স্ক্রল তালিকা)
                    // ----------------------------------------------------
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "My Room",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1B)
                            )

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "More",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    item {
                        // Horizontal row of My Rooms (Fixed presets + User-created groups)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // User-created groups from ViewModel (Dynamic Groups)
                            items(allGroups) { group ->
                                val rKey = "group_${group.id}"
                                val delta = if (joinedRoomKeys.contains(rKey)) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = group.coverUrl,
                                    title = group.name,
                                    memberCount = group.memberCount + delta,
                                    isVerified = true,
                                    isLive = true,
                                    onClick = {
                                        openRoom(
                                            rKey,
                                            PartyRoomData(
                                                title = group.name,
                                                creatorName = group.creatorName,
                                                coverUrl = group.coverUrl,
                                                memberCount = group.memberCount,
                                                tag = "🌿 বন্ধুদের গ্রুপ"
                                            )
                                        )
                                    }
                                )
                            }

                            // Ludo King Group Card in My Room
                            item {
                                val ludoDelta = if (joinedRoomKeys.contains("ludo_king_live")) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
                                    title = "🎲 লুডু কিং",
                                    badgeText = "🎲 লুডু লাইভ",
                                    memberCount = 4 + ludoDelta,
                                    isVerified = true,
                                    isLive = true,
                                    onClick = {
                                        showLudoPolicyDialog = true
                                    }
                                )
                            }

                            // Preset Rooms from User Screenshot
                            item {
                                val rKey = "preset_fm"
                                val delta = if (joinedRoomKeys.contains(rKey)) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400",
                                    title = "FM 🎙️ ইসলাঁমিঁক ...",
                                    badgeText = "FM ইসলামিক রেডিও শো",
                                    memberCount = 3 + delta,
                                    isLive = true,
                                    onClick = {
                                        openRoom(
                                            rKey,
                                            PartyRoomData(
                                                title = "FM 🎙️ ইসলামিক রেডিও শো",
                                                creatorName = "হাফেজ মাওলানা রহীম",
                                                coverUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400",
                                                memberCount = 3,
                                                tag = "🕌 ইসলামিক আলোচনা"
                                            )
                                        )
                                    }
                                )
                            }

                            item {
                                val rKey = "preset_162"
                                val delta = if (joinedRoomKeys.contains(rKey)) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                                    title = "✔ ①62💖Cha...",
                                    memberCount = 19 + delta,
                                    isVerified = true,
                                    isLive = true,
                                    onClick = {
                                        openRoom(
                                            rKey,
                                            PartyRoomData(
                                                title = "①62💖 Chat & Fun Party",
                                                creatorName = "Sweet Angel",
                                                coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                                                memberCount = 19,
                                                tag = "💖 মিষ্টি আড্ডা"
                                            )
                                        )
                                    }
                                )
                            }

                            item {
                                val rKey = "preset_rk"
                                val delta = if (joinedRoomKeys.contains(rKey)) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400",
                                    title = "✔ ⓇⓀ... বিনোদন",
                                    badgeText = "মেঘ-বাড়ী বিনোদন ক্লাব",
                                    memberCount = 15 + delta,
                                    isVerified = true,
                                    isLive = true,
                                    onClick = {
                                        openRoom(
                                            rKey,
                                            PartyRoomData(
                                                title = "মেঘ-বাড়ী বিনোদন ক্লাব",
                                                creatorName = "রাজকুমার তানভীর",
                                                coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400",
                                                memberCount = 15,
                                                tag = "🎸 গানের আসর"
                                            )
                                        )
                                    }
                                )
                            }

                            item {
                                val rKey = "preset_rainbow"
                                val delta = if (joinedRoomKeys.contains(rKey)) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
                                    title = "রংধনু",
                                    memberCount = delta,
                                    isLive = delta > 0,
                                    onClick = {
                                        openRoom(
                                            rKey,
                                            PartyRoomData(
                                                title = "রংধনু স্পেশাল আড্ডা",
                                                creatorName = "রংধনু আপু",
                                                coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
                                                memberCount = 1,
                                                tag = "🌈 রঙের আড্ডা"
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // Section B: "Recommended Room" & Category Chips
                    // ----------------------------------------------------
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Recommended Room",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1B)
                            )

                            // Translation icon (🅰️)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEEEEEE),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("A", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                                }
                            }
                        }
                    }

                    item {
                        // Category Filter Chips matching Screenshot: Trending, Nearby, Ludo, Game, etc.
                        val categories = listOf(
                            CategoryChipItem("Trending", "🔥 Trending"),
                            CategoryChipItem("Nearby", "📍 Nearby"),
                            CategoryChipItem("Ludo", "🎲 লুডু"),
                            CategoryChipItem("Game", "🎮 গেম"),
                            CategoryChipItem("Talent", "🎤 ট্যালেন্ট"),
                            CategoryChipItem("Dating", "💖 ডেটিং"),
                            CategoryChipItem("Fun", "💡 অজানা তথ্য")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedFilterCategory == cat.id
                                Surface(
                                    onClick = { selectedFilterCategory = cat.id },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFF1E1E1E) else Color(0xFFF1F3F4),
                                    border = if (isSelected) null else BorderStroke(0.5.dp, Color(0xFFE0E0E0))
                                ) {
                                    Text(
                                        text = cat.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF424242),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // Section C: LUDO KING GROUP & ROOMS (when "Ludo" is selected or at top of feed)
                    // ----------------------------------------------------
                    if (selectedFilterCategory == "Ludo") {
                        // 1. Featured Messenger Ludo King Group Banner
                        item {
                            LudoKingFeaturedBanner(
                                onPlay = { showLudoKingGame = true }
                            )
                        }

                        // 2. Ludo Room 1: ৪ জন লাইভ ভিডিও কলে লুডু ম্যাচ
                        item {
                            RecommendedRoomCard(
                                title = "🎲 লুডু কিং - ৪ জন লাইভ ভিডিও কল রুম",
                                creatorName = "Niyon Islam Sohag",
                                quote = "🎲 বন্ধুদের সাথে লাইভ ভিডিও কলে লুডু কিং খেলুন! ৬ পড়লে গুটি ঘুরবে ও কাটলে ফস সাউন্ড!",
                                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
                                memberCount = 4,
                                categoryTag = "🎲 লুডু কিং Pro",
                                isGroup = true,
                                isOnline = true,
                                participants = listOf(
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                                    SampleData.friendMdMohiuddin.avatarUrl,
                                    SampleData.friendRahim.avatarUrl,
                                    SampleData.friendHasanMiaHowlader.avatarUrl
                                ),
                                 onClick = { showLudoPolicyDialog = true }
                            )
                        }

                        // 3. Ludo Room 2: ২/৪ জন জোড়া টিম লুডু টুর্নামেন্ট
                        item {
                            RecommendedRoomCard(
                                title = "🏆 ২/৪ জন জোড়া টিম লুডু টুর্নামেন্ট",
                                creatorName = "FotoFilm Ludo League",
                                quote = "🏆 লাইভ টিম লুডু চ্যালেঞ্জ! পার্টনারের সাথে এক হয়ে অন্য টিমকে হারানোর রোমাঞ্চকর খেলা!",
                                avatarUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=200",
                                memberCount = 8,
                                categoryTag = "🏆 লুডু টুর্নামেন্ট",
                                isGroup = true,
                                isOnline = true,
                                participants = listOf(
                                    SampleData.friendTanvir.avatarUrl,
                                    SampleData.friendSamrat.avatarUrl,
                                    SampleData.friendSadia.avatarUrl
                                ),
                                onClick = { showLudoPolicyDialog = true }
                            )
                        }

                        // 4. Ludo Room 3: ক্লাসিক ফ্রেন্ডস লুডু আড্ডা
                        item {
                            RecommendedRoomCard(
                                title = "🎲 ক্লাসিক ফ্রেন্ডস লুডু আড্ডা (Friends Match)",
                                creatorName = "MD Mohiuddin",
                                quote = "🔥 বড় ছক্কা ডাইস, সাউন্ড ইফেক্ট, ভয়েস কল ও ৫টি ইমোজি সহ আসল লুডু কিং!",
                                avatarUrl = SampleData.friendMdMohiuddin.avatarUrl,
                                memberCount = 4,
                                categoryTag = "🎲 ফ্রেন্ডস লুডু",
                                isGroup = true,
                                isOnline = true,
                                participants = listOf(
                                    SampleData.friendMdMohiuddin.avatarUrl,
                                    SampleData.friendRahim.avatarUrl
                                ),
                                onClick = { showLudoPolicyDialog = true }
                            )
                        }

                        // 5. Ludo Room 4: কুইক ৫ মিনিট লুডু চ্যালেঞ্জ
                        item {
                            RecommendedRoomCard(
                                title = "⚡ কুইক ৫ মিনিট লুডু চ্যালেঞ্জ রুম",
                                creatorName = "Abdur Rahim",
                                quote = "⚡ দ্রুত গতির ৫ মিনিটের স্পেশাল লুডু ম্যাচ! কে নেবে প্রথম স্থান?",
                                avatarUrl = SampleData.friendRahim.avatarUrl,
                                memberCount = 2,
                                categoryTag = "⚡ কুইক লুডু",
                                isGroup = true,
                                isOnline = true,
                                participants = listOf(
                                    SampleData.friendRahim.avatarUrl,
                                    SampleData.friendTanvir.avatarUrl
                                ),
                                onClick = { showLudoPolicyDialog = true }
                            )
                        }
                    } else {
                        // In Trending or other categories, also display the Featured Ludo Banner at top of feed!
                        item {
                            LudoKingFeaturedBanner(
                                onPlay = { showLudoPolicyDialog = true }
                            )
                        }
                    }

                    // ----------------------------------------------------
                    // Section D: User-Created Groups Live Room Cards (DYNAMIC)
                    // ----------------------------------------------------
                    // Shows any newly created group immediately with online status & members
                    items(allGroups) { group ->
                        if (searchQuery.isBlank() || group.name.contains(searchQuery, ignoreCase = true) || group.description.contains(searchQuery, ignoreCase = true)) {
                            val rKey = "group_${group.id}"
                            val isJoined = joinedRoomKeys.contains(rKey)
                            RecommendedRoomCard(
                                title = group.name,
                                creatorName = "${group.creatorName}'s Group",
                                quote = if (group.description.isNotBlank()) group.description else "গ্রুপে সবাই মিলে আড্ডা ও ভয়েস কল চলছে...",
                                avatarUrl = group.coverUrl,
                                memberCount = group.memberCount + if (isJoined) 1 else 0,
                                categoryTag = "🌿 গ্রুপ লাইভ আড্ডা",
                                isGroup = true,
                                isOnline = true,
                                participants = listOf(
                                    group.coverUrl,
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                                    SampleData.friendMdMohiuddin.avatarUrl
                                ),
                                isJoined = isJoined,
                                onToggleJoin = { toggleGroupMembership(rKey, group.name) },
                                onClick = {
                                    openRoom(
                                        rKey,
                                        PartyRoomData(
                                            title = group.name,
                                            creatorName = group.creatorName,
                                            coverUrl = group.coverUrl,
                                            memberCount = group.memberCount,
                                            tag = "🌿 ${group.name}"
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // ----------------------------------------------------
                    // Section D: Static Recommended Rooms from Screenshot
                    // ----------------------------------------------------

                    // Card 1: Rafi Islam's Room
                    item {
                        if (searchQuery.isBlank() || "Rafi Islam".contains(searchQuery, ignoreCase = true)) {
                            val rKey = "rec_rafi"
                            val isJoined = joinedRoomKeys.contains(rKey)
                            RecommendedRoomCard(
                                title = "Rafi Islam's Room",
                                creatorName = "Rafi Islam",
                                quote = "🥰 পাগল খানায় সিঙ্গেল ছেলে মেয়ে পাওয়া যায় 🥰",
                                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                                memberCount = 4 + if (isJoined) 1 else 0,
                                categoryTag = "💡 অজানা তথ্য",
                                isOnline = true,
                                participants = listOf(
                                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                                    "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
                                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
                                ),
                                isJoined = isJoined,
                                onToggleJoin = { toggleGroupMembership(rKey, "Rafi Islam's Room") },
                                onClick = {
                                    openRoom(
                                        rKey,
                                        PartyRoomData(
                                            title = "Rafi Islam's Room",
                                            creatorName = "Rafi Islam",
                                            coverUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                                            memberCount = 4,
                                            tag = "💡 অজানা তথ্য"
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Promotional Banner matching Screenshot: NEWBIE GROWTH TASK
                    item {
                        NewbieGrowthTaskBanner()
                    }

                    // Card 2: 💖–ālįşħā–😄 Room
                    item {
                        if (searchQuery.isBlank() || "alisha".contains(searchQuery, ignoreCase = true)) {
                            val rKey = "rec_alisha"
                            val isJoined = joinedRoomKeys.contains(rKey)
                            RecommendedRoomCard(
                                title = "💖–ālįşħā–😄",
                                creatorName = "Alisha",
                                quote = "$, 🕊️ সিঙ্গেল ছেলে মেয়ে রা আসো মিঙ্গেল করে দিবো,...",
                                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                                memberCount = 2 + if (isJoined) 1 else 0,
                                categoryTag = "👩‍❤️‍👨 ডেটিং",
                                isOnline = true,
                                participants = listOf(
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200"
                                ),
                                isJoined = isJoined,
                                onToggleJoin = { toggleGroupMembership(rKey, "💖–ālįşħā–😄") },
                                onClick = {
                                    openRoom(
                                        rKey,
                                        PartyRoomData(
                                            title = "💖–ālįşħā–😄 এর আড্ডা",
                                            creatorName = "Alisha",
                                            coverUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                                            memberCount = 2,
                                            tag = "👩‍❤️‍👨 ডেটিং"
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Card 3: Tumi sudhu Amar's Room
                    item {
                        if (searchQuery.isBlank() || "Tumi sudhu Amar".contains(searchQuery, ignoreCase = true)) {
                            val rKey = "rec_tumi"
                            val isJoined = joinedRoomKeys.contains(rKey)
                            RecommendedRoomCard(
                                title = "Tumi sudhu Amar's Room",
                                creatorName = "Tumi sudhu Amar",
                                quote = "🎶 মন ভালো করার সুর ও কবিতা পাঠ... সবাই স্বাগতম 🌹",
                                avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200",
                                memberCount = 5 + if (isJoined) 1 else 0,
                                categoryTag = "🎵 গান ও কবিতা",
                                isOnline = true,
                                participants = listOf(
                                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200",
                                    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                                    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200"
                                ),
                                isJoined = isJoined,
                                onToggleJoin = { toggleGroupMembership(rKey, "Tumi sudhu Amar's Room") },
                                onClick = {
                                    openRoom(
                                        rKey,
                                        PartyRoomData(
                                            title = "Tumi sudhu Amar's Room",
                                            creatorName = "Tumi sudhu Amar",
                                            coverUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200",
                                            memberCount = 5,
                                            tag = "🎵 গান ও কবিতা"
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Floating Action Button at bottom right to create a new group/room
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showCreateGroupDialog = true },
                    containerColor = Color(0xFF0D652D),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Group")
                        Text(
                            text = "নতুন গ্রুপ/রুম",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Sub-dialog: Create Group dialog from Attachment 2
    if (showCreateGroupDialog) {
        CreateGroupImage2Dialog(
            viewModel = viewModel,
            onDismiss = { showCreateGroupDialog = false },
            onGroupCreated = { newName ->
                Toast.makeText(context, "🎉 \"$newName\" লাইভ রুম তৈরি হয়েছে!", Toast.LENGTH_LONG).show()
                showCreateGroupDialog = false
            }
        )
    }
}

/**
 * Data holder for room parameters
 */
data class PartyRoomData(
    val title: String,
    val creatorName: String,
    val coverUrl: String,
    val memberCount: Int,
    val tag: String
)

data class CategoryChipItem(
    val id: String,
    val label: String
)

/**
 * Horizontal "My Room" Card matching Screenshot
 */
@Composable
fun MyRoomHorizontalCard(
    coverUrl: String,
    title: String,
    badgeText: String? = null,
    memberCount: Int,
    isVerified: Boolean = false,
    isLive: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(88.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.Start
    ) {
        // Image box
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Text on top of image if badgeText present
            if (badgeText != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Bottom pill badge with user icon & member count (e.g. 👤 3)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "$memberCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Green Online Dot on top left
            if (isLive) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(8.dp)
                        .background(Color(0xFF00E676), CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .align(Alignment.TopStart)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title text below
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isVerified) {
                FidaVerifiedBadge(size = 12.dp)
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Recommended Room Card matching Screenshot:
 * Avatar, Title, Right member badge, Room status/quote, Participant avatars, Category tag, Online indicator
 */
@Composable
fun RecommendedRoomCard(
    title: String,
    creatorName: String,
    quote: String,
    avatarUrl: String,
    memberCount: Int,
    categoryTag: String,
    isGroup: Boolean = false,
    isOnline: Boolean = true,
    isVerified: Boolean = true,
    participants: List<String> = emptyList(),
    isJoined: Boolean = false,
    onToggleJoin: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.6.dp, Color(0xFFE5E5EA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Avatar, Room Name, Right Member Count Badge (e.g. 👤 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    )
                    if (isOnline) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(Color(0xFF00E676), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            FidaVerifiedBadge(size = 14.dp)
                        }
                    }
                    if (isGroup) {
                        Text(
                            text = "🟢 অনলাইনে আছে • $memberCount জন সক্রিয়",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Member count pill badge matching Screenshot (👤 4)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F3F4),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF616161),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$memberCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room status / quote (e.g. 🥰 পাগল খানায় সিঙ্গেল ছেলে মেয়ে পাওয়া যায় 🥰)
            Text(
                text = quote,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Participant Avatars on Left, Join/Leave + Category Tag on Right/Bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Overlapping or spaced participant avatars
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    participants.take(4).forEach { participantUrl ->
                        AsyncImage(
                            model = participantUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onToggleJoin != null) {
                        Surface(
                            onClick = onToggleJoin,
                            shape = RoundedCornerShape(14.dp),
                            color = if (isJoined) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                            border = BorderStroke(
                                0.8.dp,
                                if (isJoined) Color(0xFFC62828) else Color(0xFF2E7D32)
                            ),
                            modifier = Modifier.testTag("room_join_leave_btn_${title.hashCode()}")
                        ) {
                            Text(
                                text = if (isJoined) "বের হোন (-1)" else "প্রবেশ (+1)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isJoined) Color(0xFFC62828) else Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Category Tag matching Screenshot (💡 অজানা তথ্য / 👩‍❤️‍👨 ডেটিং)
                    Text(
                        text = categoryTag,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Cartoon Promotional Banner: NEWBIE GROWTH TASK
 * Exactly matches Screenshot 3
 */
@Composable
fun NewbieGrowthTaskBanner() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF2979FF),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2979FF),
                            Color(0xFF448AFF),
                            Color(0xFF82B1FF)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NEWBIE GROWTH TASK",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                // Cute cartoon emojis representing the characters in screenshot
                Text(
                    text = "🚌 🐣 🥳 🎁",
                    fontSize = 24.sp
                )
            }
        }
    }
}

/**
 * Messenger Ludo King Group Featured Banner
 * Clicking opens full Ludo King Pro 4-Player match
 */
@Composable
fun LudoKingFeaturedBanner(onPlay: () -> Unit) {
    Surface(
        onClick = onPlay,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A103C),
        border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
        shadowElevation = 5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2E0854),
                            Color(0xFF1B1B4F),
                            Color(0xFF0F3460)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ludo animated dice icon
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFD54F),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎲", fontSize = 30.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "মেসেঞ্জার লুডু কিং গ্রুপ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        FidaVerifiedBadge(size = 15.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("👑", fontSize = 12.sp)
                    }
                    Text(
                        text = "৪ জন ভিডিও কলে খেলুন • ঘূর্ণায়মান গুটি ও সাউন্ড সহ",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.25f),
                            border = BorderStroke(0.8.dp, Color(0xFF00E676))
                        ) {
                            Text(
                                text = "🟢 ৪ জন লাইভ",
                                fontSize = 10.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                            border = BorderStroke(0.8.dp, Color(0xFFFFD54F))
                        ) {
                            Text(
                                text = "🏆 টিম লুডু",
                                fontSize = 10.sp,
                                color = Color(0xFFFFE082),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("খেলুন", color = Color(0xFF1A103C), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}
