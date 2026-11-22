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
    val hangoutPresetRooms by viewModel.hangoutPresetRooms.collectAsState()
    val presetActiveMap = remember(hangoutPresetRooms) {
        hangoutPresetRooms.associate { it.roomKey to it.isActive }
    }

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

    val presetRoomsOnlineBase = 0
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

    fun isRoomEnabled(roomKey: String): Boolean {
        if (roomKey.startsWith("group_")) {
            val groupId = roomKey.removePrefix("group_")
            return allGroups.find { it.id == groupId }?.isActive ?: true
        }
        return presetActiveMap[roomKey] ?: true
    }

    fun openRoom(roomKey: String, data: PartyRoomData) {
        if (!isRoomEnabled(roomKey)) {
            Toast.makeText(
                context,
                "🔴 অ্যাডমিন এই \"${data.title}\" গ্রুপটি সাময়িকভাবে বন্ধ রেখেছেন",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
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
                                    isLive = group.isActive,
                                    isAdminPaused = !group.isActive,
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
                                val ludoEnabled = isRoomEnabled("ludo_king_live")
                                val ludoDelta = if (joinedRoomKeys.contains("ludo_king_live")) 1 else 0
                                MyRoomHorizontalCard(
                                    coverUrl = currentUser.avatarUrl,
                                    title = "🎲 লুডু কিং",
                                    badgeText = "🎲 লুডু লাইভ",
                                    memberCount = ludoDelta,
                                    isVerified = true,
                                    isLive = ludoEnabled,
                                    isAdminPaused = !ludoEnabled,
                                    onClick = {
                                        if (!ludoEnabled) {
                                            Toast.makeText(context, "🔴 অ্যাডমিন লুডু কিং গ্রুপটি সাময়িকভাবে বন্ধ রেখেছেন", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showLudoPolicyDialog = true
                                        }
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

                        // Official Host Ludo Room
                        item {
                            RecommendedRoomCard(
                                title = "🎲 লুডু কিং - লাইভ ভিডিও কল রুম",
                                creatorName = currentUser.fullName,
                                quote = "🎲 বন্ধুদের সাথে লাইভ ভিডিও কলে লুডু কিং খেলুন!",
                                avatarUrl = currentUser.avatarUrl,
                                memberCount = 1,
                                categoryTag = "🎲 লুডু কিং Pro",
                                isGroup = true,
                                isOnline = true,
                                participants = emptyList(),
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
                                isOnline = group.isActive,
                                isAdminPaused = !group.isActive,
                                participants = group.admins.map { it.avatarUrl }.filter { it.isNotBlank() },
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

                    if (allGroups.isEmpty() && selectedFilterCategory != "Ludo") {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "এখনো কোনো লাইভ আড্ডা রুম তৈরি হয়নি",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1B1B1B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "নিচের '+ নতুন গ্রুপ/রুম' বাটনে চাপ দিয়ে আপনার নিজস্ব লাইভ আড্ডা রুম তৈরি করুন।",
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Google AdMob Banner inside Hangout Hub
                    item {
                        AdBanner(creativeIndex = 1)
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
    isAdminPaused: Boolean = false,
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
            FotoFilmCoverBackground(
                imageUrl = coverUrl,
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

            // Green Online Dot or Admin Paused Badge on top left
            if (isAdminPaused) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD32F2F),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                ) {
                    Text(
                        text = "বন্ধ",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            } else if (isLive) {
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
    isAdminPaused: Boolean = false,
    isVerified: Boolean = true,
    participants: List<String> = emptyList(),
    isJoined: Boolean = false,
    onToggleJoin: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isAdminPaused) Color(0xFFFFF5F5) else Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.8.dp, if (isAdminPaused) Color(0xFFE57373) else Color(0xFFE5E5EA)),
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
                    FotoFilmAvatar(
                        imageUrl = avatarUrl,
                        name = creatorName,
                        contentDescription = title,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(if (isAdminPaused) Color(0xFFD32F2F) else if (isOnline) Color(0xFF00E676) else Color.Gray, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
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
                    if (isAdminPaused) {
                        Text(
                            text = "🔴 অ্যাডমিন কর্তৃক সাময়িকভাবে বন্ধ",
                            fontSize = 11.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isGroup) {
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
