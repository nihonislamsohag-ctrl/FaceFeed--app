package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.components.FidaAILogo
import com.example.ui.components.FidaVerifiedBadge
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.Conversation
import com.example.model.ProfileFriend
import com.example.model.Translations
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel
import com.example.ui.components.StoryTray
import com.example.ui.components.MessengerCircularStoryTray
import com.example.ui.components.BlockConfirmationBottomSheet
import com.example.ui.components.BlockedSuccessBottomSheet
import com.example.ui.components.UnblockConfirmationBottomSheet
import androidx.compose.foundation.BorderStroke
import com.example.ui.components.FriendsHangoutPartyRoomDialog
import com.example.ui.components.GroupPolicyDialog
import com.example.ui.components.LivePartyRoomHubDialog
import com.example.ui.components.LudoKingGroupDialog
import com.example.ui.components.LudoGroupPolicyDialog
import com.example.ui.components.GameMode

enum class MessengerFilterTab {
    ALL, UNREAD, GROUPS, ARCHIVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val conversations by viewModel.conversations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()
    val isActiveStatusEnabled by viewModel.isActiveStatusEnabled.collectAsState()
    val messageRequests by viewModel.messageRequests.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MessengerFilterTab.ALL) }

    // Dialog states
    var showCameraDialog by remember { mutableStateOf(false) }
    var showNewMessageDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showMessengerDrawer by remember { mutableStateOf(false) }
    var showMessageRequestsDialog by remember { mutableStateOf(false) }
    var showMarketplaceChatsDialog by remember { mutableStateOf(false) }
    var showArchivedChatsDialog by remember { mutableStateOf(false) }
    var showMessengerSettingsDialog by remember { mutableStateOf(false) }
    var showTopRightMenu by remember { mutableStateOf(false) }
    var selectedFriendForNoteReply by remember { mutableStateOf<Pair<User, String>?>(null) }
    var selectedConversationForOptions by remember { mutableStateOf<Conversation?>(null) }
    var blockingConversation by remember { mutableStateOf<Conversation?>(null) }
    var unblockingConversation by remember { mutableStateOf<Conversation?>(null) }
    var blockedSuccessUser by remember { mutableStateOf<User?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showFidaAIScreen by remember { mutableStateOf(false) }
    var showFriendsHangoutRoom by remember { mutableStateOf(false) }
    var showGroupPolicyDialog by remember { mutableStateOf(false) }
    var showLivePartyRoomHub by remember { mutableStateOf(false) }
    var showLudoKingGroup by remember { mutableStateOf(false) }
    var showLudoPolicyDialog by remember { mutableStateOf(false) }
    var ludoSelectedMode by remember { mutableStateOf<GameMode?>(null) }

    // Toast auto-clear
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2200)
            toastMessage = null
        }
    }

    // Full Screen: Fida AI (ফিদা এআই) Assistant Screen
    if (showFidaAIScreen) {
        FidaAIScreen(
            onBack = { showFidaAIScreen = false }
        )
        return
    }

    // If an active chat is open, show the ChatDetailScreen
    if (activeChat != null) {
        ChatDetailScreen(
            conversation = activeChat!!,
            viewModel = viewModel,
            onBack = { viewModel.closeChat() }
        )
        return
    }

    // Filter conversations
    // Filter conversations & sort strictly by latest message timestamp (Newest on top at position 1)
    val filteredConversations = remember(conversations, searchQuery, selectedFilter) {
        var list = conversations
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.friend.fullName.contains(searchQuery, ignoreCase = true) ||
                (it.groupName?.contains(searchQuery, ignoreCase = true) == true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
        val tabFiltered = when (selectedFilter) {
            MessengerFilterTab.ALL -> list.filter { !it.isArchived && !it.isMessageRequest }
            MessengerFilterTab.UNREAD -> list.filter { it.unreadCount > 0 && !it.isArchived }
            MessengerFilterTab.GROUPS -> list.filter { it.isGroup && !it.isArchived }
            MessengerFilterTab.ARCHIVED -> list.filter { it.isArchived }
        }
        // Pinned chats stay on top; otherwise strictly sorted by newest message time descending
        tabFiltered.sortedWith(
            compareByDescending<Conversation> { it.isPinned }
                .thenByDescending { it.lastMessageTimestamp }
        )
    }

    val unreadTotalCount = remember(conversations) { conversations.count { it.unreadCount > 0 && !it.isArchived } }
    val archivedTotalCount = remember(conversations) { conversations.count { it.isArchived } }
    val messageRequestsCount = messageRequests.size

    BackHandler {
        viewModel.setTab(com.example.viewmodel.AppTab.HOME)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ChatFilm Header: Back button + Drawer button ☰ + App Logo + "ChatFilm" + Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Back to Facebook/Home Button
                    IconButton(
                        onClick = { viewModel.setTab(com.example.viewmodel.AppTab.HOME) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // Drawer Hamburger Menu Button
                    IconButton(
                        onClick = { showMessengerDrawer = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "ChatFilm Menu",
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // FotoFilm App Icon placed in front of "ChatFilm"
                    Image(
                        painter = painterResource(id = R.drawable.ic_fotofilm_logo),
                        contentDescription = "ChatFilm App Logo",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = BangladeshGreen, fontWeight = FontWeight.Black)) {
                                append("Chat")
                            }
                            withStyle(SpanStyle(color = BangladeshRed, fontWeight = FontWeight.Black)) {
                                append("Film")
                            }
                        },
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Camera Action
                    IconButton(
                        onClick = { showCameraDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Compose New Message
                    IconButton(
                        onClick = { showNewMessageDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New Message",
                            modifier = Modifier.size(19.dp),
                            tint = FacebookBlue
                        )
                    }

                    // Group Call Action
                    IconButton(
                        onClick = {
                            val groupConv = conversations.firstOrNull { it.isGroup }
                            if (groupConv != null) {
                                viewModel.startGroupCall(
                                    groupName = groupConv.groupName ?: strings.groupCall,
                                    members = groupConv.groupMembers
                                )
                            } else {
                                viewModel.startGroupCall(
                                    groupName = "FaceFeed Family Call 📞",
                                    members = listOf(SampleData.friendHasan, SampleData.friendTanvir, SampleData.friendNusrat, SampleData.friendSamrat)
                                )
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = strings.groupCall,
                            modifier = Modifier.size(20.dp),
                            tint = FacebookBlue
                        )
                    }

                    // 3-Dot Line Menu on Right Corner (৩ ডট লাইন ডান পাসে কন্যারে)
                    Box {
                        IconButton(
                            onClick = { showTopRightMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Messenger Options Menu",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showTopRightMenu,
                            onDismissRequest = { showTopRightMenu = false }
                        ) {
                            // Header with Back / Close Button on Right Side (ডান পাসে ব্যাক / ক্লোজ বাটন)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "অপশন মেনু",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { showTopRightMenu = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Back / Close (বন্ধ করুন / ব্যাকে যান)",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = FacebookBlue)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    showMessengerSettingsDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Message requests") },
                                leadingIcon = {
                                    Icon(Icons.Default.MarkChatUnread, contentDescription = null, tint = Color(0xFFFF9800))
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    showMessageRequestsDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Archived chats") },
                                leadingIcon = {
                                    Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    selectedFilter = MessengerFilterTab.ARCHIVED
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("New group chat") },
                                leadingIcon = {
                                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = BangladeshGreen)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    showCreateGroupDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("ChatFilm Camera") },
                                leadingIcon = {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FacebookBlue)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    showCameraDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Share Note (নোট লিখুন 💭)") },
                                leadingIcon = {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF8E24AA))
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    showEditNoteDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Mark all as read") },
                                leadingIcon = {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = FacebookBlue)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    conversations.forEach { conv ->
                                        if (conv.unreadCount > 0) {
                                            viewModel.markConversationAsRead(conv.id)
                                        }
                                    }
                                    toastMessage = "All messages marked as read ✓"
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isActiveStatusEnabled) "Active status: ON 🟢" else "Active status: OFF ⚪"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isActiveStatusEnabled) OnlineGreen else Color.Gray
                                    )
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    viewModel.toggleActiveStatus()
                                    toastMessage = if (!isActiveStatusEnabled) "Active status turned ON 🟢" else "Active status turned OFF ⚪"
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("ব্যাকে যান / ফিরে যান", fontWeight = FontWeight.Bold, color = BangladeshRed) },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BangladeshRed)
                                },
                                onClick = {
                                    showTopRightMenu = false
                                    viewModel.setTab(com.example.viewmodel.AppTab.HOME)
                                }
                            )
                        }
                    }

                    // Back Button on Top-Right Corner (ডান পাসে ব্যাক বাটন - প্রথম স্ক্রিনশট)
                    IconButton(
                        onClick = { viewModel.setTab(com.example.viewmodel.AppTab.HOME) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back (ব্যাকে যান / বের হোন)",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // All-Time Free & Always Active Banner in ChatFilm
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, BangladeshGreen.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ChatFilm অল-টাইম ফ্রি • চ্যাট, ছবি, ভিডিও, ভয়েস ও অডিও/ভিডিও/গ্রুপ কল সবসময় চালু ✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B5E20),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search in ChatFilm...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Messenger Filter Tabs on Left + Fida AI (ফিদা এআই) on Right
            // "ছার্চ বারের নিচে ঐ ৪ টা ফিচার অপসোনের ডান পাসে ঐ ফুলের ছবি নিচে  ফিদা এআই"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 4 Filter Tabs: [ All ] [ Unread ] [ Groups ] [ Archived ]
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MessengerTabChip(
                        label = "All",
                        count = null,
                        isSelected = selectedFilter == MessengerFilterTab.ALL,
                        onClick = { selectedFilter = MessengerFilterTab.ALL }
                    )
                    MessengerTabChip(
                        label = "Unread",
                        count = unreadTotalCount,
                        isSelected = selectedFilter == MessengerFilterTab.UNREAD,
                        onClick = { selectedFilter = MessengerFilterTab.UNREAD }
                    )
                    MessengerTabChip(
                        label = "Groups",
                        count = null,
                        isSelected = selectedFilter == MessengerFilterTab.GROUPS,
                        onClick = { selectedFilter = MessengerFilterTab.GROUPS }
                    )
                    MessengerTabChip(
                        label = "Archived",
                        count = archivedTotalCount,
                        isSelected = selectedFilter == MessengerFilterTab.ARCHIVED,
                        onClick = { selectedFilter = MessengerFilterTab.ARCHIVED }
                    )
                    MessengerTabChip(
                        label = "💭 Note",
                        count = null,
                        isSelected = false,
                        onClick = { showEditNoteDialog = true }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Fida AI Button on Right: ঐ ফুলের ছবি নিচে  ফিদা এআই
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showFidaAIScreen = true }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF3E5F5), Color(0xFFEDE7F6))
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFBA68C8), Color(0xFF7E57C2))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    FidaAILogo(size = 28.dp, animated = true)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ফিদা এআই",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        FidaVerifiedBadge(size = 12.dp)
                    }
                }
            }

            // ২ টা পাশাপাশি অপশন: ১. বন্ধুদের সাথে আড্ডা (🧑🤝🧑), ২. লুডু কিং গ্রুপ (🎲)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ১. বন্ধুদের সাথে আড্ডা (🧑🤝🧑) - লাইভ পার্টি চ্যাট রুম (Screenshot 2)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, Color(0xFFBA68C8).copy(alpha = 0.5f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showGroupPolicyDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF3E5F5),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🧑🤝🧑", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "বন্ধুদের সাথে আড্ডা",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                FidaVerifiedBadge(size = 13.dp)
                            }
                            Text(
                                text = "লাইভ পার্টি রুম 🌿",
                                fontSize = 10.sp,
                                color = Color(0xFF7B1FA2),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ২. লুডু কিং গ্রুপ (🎲) - ৪ জন ভিডিও কলে লাইভ লুডু ম্যাচ
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showLudoPolicyDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF8E1),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎲", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "লুডু কিং গ্রুপ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                FidaVerifiedBadge(size = 13.dp)
                            }
                            Text(
                                text = "২-৪ জন, জোড়া জোড়ায় খেলো 🎲",
                                fontSize = 10.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Facebook Messenger-style Circular Story & Floating Note Tray at the top of ChatFilm
            if (selectedFilter != MessengerFilterTab.ARCHIVED) {
                MessengerCircularStoryTray(
                    viewModel = viewModel,
                    onEditMyNote = { showEditNoteDialog = true },
                    onFriendNoteClick = { friend, note ->
                        selectedFriendForNoteReply = friend to note
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }

            // Conversations List
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedFilter == MessengerFilterTab.ARCHIVED) "No archived chats" else "No messages found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            viewModel = viewModel,
                            onClick = { viewModel.openChat(conv) },
                            onMoreOptions = { selectedConversationForOptions = conv }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
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
                    .padding(bottom = 70.dp, start = 20.dp, end = 20.dp)
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

    // ChatFilm Navigation Drawer
    if (showMessengerDrawer) {
        MessengerDrawerDialog(
            currentUser = currentUser,
            isActiveStatusEnabled = isActiveStatusEnabled,
            unreadCount = unreadTotalCount,
            archivedCount = archivedTotalCount,
            messageRequestsCount = messageRequestsCount,
            onDismiss = { showMessengerDrawer = false },
            onToggleActiveStatus = {
                viewModel.toggleActiveStatus()
            },
            onOpenEditNote = {
                showMessengerDrawer = false
                showEditNoteDialog = true
            },
            onOpenChats = {
                selectedFilter = MessengerFilterTab.ALL
                showMessengerDrawer = false
            },
            onOpenMarketplace = {
                showMessengerDrawer = false
                showMarketplaceChatsDialog = true
            },
            onOpenMessageRequests = {
                showMessengerDrawer = false
                showMessageRequestsDialog = true
            },
            onOpenArchive = {
                showMessengerDrawer = false
                selectedFilter = MessengerFilterTab.ARCHIVED
            },
            onOpenSettings = {
                showMessengerDrawer = false
                showMessengerSettingsDialog = true
            }
        )
    }

    // Dialog: Conversation 3-Dots Options (Pin, Mute, Read/Unread, Archive, Delete, Call)
    selectedConversationForOptions?.let { conv ->
        ConversationOptionsDialog(
            conversation = conv,
            onDismiss = { selectedConversationForOptions = null },
            onTogglePin = {
                viewModel.togglePinConversation(conv.id)
                selectedConversationForOptions = null
                toastMessage = if (conv.isPinned) "Chat unpinned" else "Chat pinned to top 📌"
            },
            onToggleMute = {
                viewModel.toggleMuteConversation(conv.id)
                selectedConversationForOptions = null
                toastMessage = if (conv.isMuted) "Notifications unmuted" else "Notifications muted 🔕"
            },
            onToggleRead = {
                if (conv.unreadCount > 0) viewModel.markConversationAsRead(conv.id)
                else viewModel.markConversationAsUnread(conv.id)
                selectedConversationForOptions = null
                toastMessage = if (conv.unreadCount > 0) "Marked as read" else "Marked as unread"
            },
            onToggleArchive = {
                if (conv.isArchived) {
                    viewModel.unarchiveConversation(conv.id)
                    toastMessage = "Chat unarchived"
                } else {
                    viewModel.archiveConversation(conv.id)
                    toastMessage = "Chat moved to archive 📦"
                }
                selectedConversationForOptions = null
            },
            onAudioCall = {
                selectedConversationForOptions = null
                viewModel.startAudioCall(conv.friend)
            },
            onVideoCall = {
                selectedConversationForOptions = null
                viewModel.startVideoCall(conv.friend)
            },
            onDelete = {
                viewModel.deleteConversation(conv.id)
                selectedConversationForOptions = null
                toastMessage = "Conversation deleted"
            },
            onBlock = {
                val c = conv
                selectedConversationForOptions = null
                if (viewModel.isUserNiyonIslamSohag(c.friend)) {
                    toastMessage = "⛔ আপনি অ্যাপের সম্মানিত মালিক (Niyon Islam Sohag)-কে কোনোভাবেই ব্লক করতে পারবেন না! (App Owner Protected)"
                } else {
                    blockingConversation = c
                }
            },
            onUnblock = {
                val c = conv
                selectedConversationForOptions = null
                unblockingConversation = c
            }
        )
    }

    // Screen 1: Block Confirmation Bottom Sheet
    blockingConversation?.let { conv ->
        BlockConfirmationBottomSheet(
            user = conv.friend,
            onDismiss = { blockingConversation = null },
            onBlock = {
                if (viewModel.isUserNiyonIslamSohag(conv.friend)) {
                    toastMessage = "⛔ অ্যাপের সম্মানিত মালিক (Niyon Islam Sohag)-কে ব্লক করা সম্ভব নয়!"
                    blockingConversation = null
                } else {
                    viewModel.blockConversation(conv.id)
                    blockingConversation = null
                    blockedSuccessUser = conv.friend
                }
            }
        )
    }

    // Screen 2: Blocked Success Bottom Sheet
    blockedSuccessUser?.let { user ->
        BlockedSuccessBottomSheet(
            user = user,
            onDismiss = { blockedSuccessUser = null },
            onDeleteChat = {
                val conv = conversations.find { it.friend.id == user.id }
                if (conv != null) {
                    viewModel.deleteConversation(conv.id)
                }
                blockedSuccessUser = null
                toastMessage = "Chat deleted"
            }
        )
    }

    // Screen 5: Unblock Confirmation Bottom Sheet
    unblockingConversation?.let { conv ->
        UnblockConfirmationBottomSheet(
            user = conv.friend,
            onDismiss = { unblockingConversation = null },
            onUnblock = {
                viewModel.unblockConversation(conv.id)
                unblockingConversation = null
                toastMessage = "${conv.friend.fullName} has been unblocked"
            }
        )
    }

    // Dialog: Compose New Message (Friend Picker + Create Group Chat option)
    if (showNewMessageDialog) {
        NewMessageDialog(
            friends = SampleData.profileFriends,
            onDismiss = { showNewMessageDialog = false },
            onCreateGroupClick = {
                showNewMessageDialog = false
                showCreateGroupDialog = true
            },
            onSelectFriend = { friend ->
                showNewMessageDialog = false
                val existing = conversations.find { it.friend.fullName.contains(friend.name, ignoreCase = true) }
                    ?: Conversation(
                        id = "conv_new_${friend.id}",
                        friend = User(
                            id = friend.id,
                            fullName = friend.name,
                            username = friend.username,
                            avatarUrl = friend.avatarUrl,
                            isOnline = friend.isOnline
                        ),
                        lastMessage = "Say Hi to ${friend.name} 👋",
                        lastTime = "Just now"
                    )
                viewModel.openChat(existing)
            }
        )
    }

    // Dialog: Create New Group Chat
    if (showCreateGroupDialog) {
        CreateGroupChatDialog(
            friends = SampleData.profileFriends,
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { groupName, selectedFriends ->
                showCreateGroupDialog = false
                val users = selectedFriends.map {
                    User(id = it.id, fullName = it.name, username = it.username, avatarUrl = it.avatarUrl, isOnline = it.isOnline)
                }
                viewModel.createGroupChat(groupName, users)
                toastMessage = "Group \"$groupName\" created 🎉"
            }
        )
    }

    // Dialog: Message Requests (with Accept & Decline actions)
    if (showMessageRequestsDialog) {
        MessageRequestsDialog(
            requests = messageRequests,
            onDismiss = { showMessageRequestsDialog = false },
            onAccept = { friend ->
                showMessageRequestsDialog = false
                viewModel.acceptMessageRequest(friend)
                toastMessage = "Message request accepted ✓"
            },
            onDecline = { friendId ->
                viewModel.declineMessageRequest(friendId)
                toastMessage = "Message request declined"
            }
        )
    }

    // Dialog: Marketplace Chats in ChatFilm
    if (showMarketplaceChatsDialog) {
        MarketplaceChatsDialog(
            onDismiss = { showMarketplaceChatsDialog = false },
            onStartProductChat = { seller, productTitle ->
                showMarketplaceChatsDialog = false
                val existing = conversations.find { it.friend.id == seller.id }
                    ?: Conversation(
                        id = "conv_mkt_${seller.id}",
                        friend = seller,
                        lastMessage = "Interested in: $productTitle",
                        lastTime = "Just now",
                        lastMessageTimestamp = System.currentTimeMillis()
                    )
                viewModel.openChat(existing)
                viewModel.sendMessage(
                    conversationId = existing.id,
                    text = "আসসালামু আলাইকুম, \"$productTitle\" পণ্যটি কি এখনো পাওয়া যাবে?"
                )
            }
        )
    }

    // Dialog: Messenger Settings
    if (showMessengerSettingsDialog) {
        com.example.ui.components.MessengerSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showMessengerSettingsDialog = false }
        )
    }

    // Dialog: Camera Viewfinder
    if (showCameraDialog) {
        MessengerCameraDialog(
            onDismiss = { showCameraDialog = false },
            onCapture = { photoUrl ->
                showCameraDialog = false
                val firstConv = conversations.firstOrNull()
                if (firstConv != null) {
                    viewModel.openChat(firstConv)
                    viewModel.sendMessage(conversationId = firstConv.id, text = "📷 Sent a photo", mediaUrl = photoUrl)
                }
            }
        )
    }

    // Dialog: Edit Note (80 chars)
    if (showEditNoteDialog) {
        val initialNote = if (currentUser.note == "Share a note...") "" else currentUser.note
        var noteInput by remember { mutableStateOf(initialNote) }
        var noteError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💭", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নোট পোস্ট করুন (সর্বোচ্চ ৮০ অক্ষর)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "আপনার প্রোফাইল আইকনের উপরে ৮০ অক্ষরে নোট লিখুন। সব বন্ধুদের কাছে এটি ভেসে উঠবে।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = {
                            if (it.length <= 80) {
                                noteInput = it
                                noteError = null
                            }
                        },
                        placeholder = { Text("কী ভাবছেন? (৮০ অক্ষর)") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (noteError != null) noteError!! else "সর্বোচ্চ ৮০ অক্ষর",
                                    color = if (noteError != null) Color.Red else Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${noteInput.length} / ৮০",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (noteInput.length >= 75) Color.Red else FacebookBlue
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("চা খাচ্ছি ☕", "লুডু খেলছি 🎲", "ব্যস্ত আছি 📱", "শুভ দিন ✨").forEach { quickNote ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    noteInput = quickNote
                                    noteError = null
                                }
                            ) {
                                Text(
                                    text = quickNote,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val safety = com.example.util.SafetyPolicyValidator.validateContent(noteInput)
                        if (!safety.isAllowed) {
                            noteError = safety.reasonMessage
                            return@Button
                        }
                        viewModel.updateNote(noteInput)
                        showEditNoteDialog = false
                        toastMessage = "নোট শেয়ার করা হয়েছে 💭"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("নোট পোস্ট করুন", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    if (initialNote.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.updateNote("")
                            showEditNoteDialog = false
                            toastMessage = "নোট মুছে ফেলা হয়েছে"
                        }) {
                            Text("মুছুন", color = BangladeshRed)
                        }
                    }
                    TextButton(onClick = { showEditNoteDialog = false }) {
                        Text("বাতিল")
                    }
                }
            }
        )
    }

    // Dialog: Reply to Friend's Note
    selectedFriendForNoteReply?.let { (friend, note) ->
        NoteReplyDialog(
            friend = friend,
            note = note,
            onDismiss = { selectedFriendForNoteReply = null },
            onReply = { replyText ->
                selectedFriendForNoteReply = null
                val existing = conversations.find { it.friend.id == friend.id }
                    ?: Conversation(
                        id = "conv_${friend.id}",
                        friend = friend,
                        lastMessage = replyText,
                        lastTime = "Just now"
                    )
                viewModel.openChat(existing)
                viewModel.sendMessage(conversationId = existing.id, text = "Replying to note: \"$note\"\n$replyText")
            }
        )
    }

    // Dialog: গ্রুপ পরিচালনার নীতিমালা (Group Management Policy - Screenshot 1)
    if (showGroupPolicyDialog) {
        GroupPolicyDialog(
            viewModel = viewModel,
            onDismiss = { showGroupPolicyDialog = false },
            onOpenLiveHangout = {
                showGroupPolicyDialog = false
                showLivePartyRoomHub = true
            }
        )
    }

    // Dialog: লাইভ পার্টি রুম হাব (Live Party Room Screen - IMG_20261104_212559_59ec2982.jpg)
    if (showLivePartyRoomHub) {
        LivePartyRoomHubDialog(
            viewModel = viewModel,
            onDismiss = { showLivePartyRoomHub = false }
        )
    }

    // Dialog: Friends Hangout Party Room ("বন্ধুদের সাথে আড্ডা 🧑🤝🧑" - Live Voice Room)
    if (showFriendsHangoutRoom) {
        FriendsHangoutPartyRoomDialog(
            viewModel = viewModel,
            onDismiss = { showFriendsHangoutRoom = false }
        )
    }

    // Dialog: Ludo King Policy ("লুডু কিং গ্রুপ এর নীতিমালা 🎲" - Matching Attachment IMG-20261105-WA8665_25711871.jpg)
    if (showLudoPolicyDialog) {
        LudoGroupPolicyDialog(
            viewModel = viewModel,
            onDismiss = { showLudoPolicyDialog = false },
            onLaunchGame = { mode ->
                showLudoPolicyDialog = false
                ludoSelectedMode = mode
                showLudoKingGroup = true
            }
        )
    }

    // Dialog: Ludo King Group ("লুডু কিং গ্রুপ 🎲" - 4-Player Video Call Ludo)
    if (showLudoKingGroup) {
        LudoKingGroupDialog(
            viewModel = viewModel,
            initialMode = ludoSelectedMode,
            onDismiss = {
                showLudoKingGroup = false
                ludoSelectedMode = null
            }
        )
    }
}

@Composable
fun MessengerTabChip(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) FacebookBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FacebookBlue) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurface
            )
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$count",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationRow(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onClick: () -> Unit,
    onMoreOptions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar or Group Avatar (Tapping avatar opens Profile, tapping row opens Chat)
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable {
                if (!conversation.isGroup) {
                    viewModel.openUserProfile(conversation.friend)
                } else {
                    onClick()
                }
            }
        ) {
            if (conversation.isGroup) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BangladeshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            } else {
                AsyncImage(
                    model = conversation.friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = conversation.friend.fullName,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (conversation.friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = if (conversation.isGroup) conversation.groupName ?: "Group" else conversation.friend.fullName,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Black else FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = FacebookBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (conversation.isMuted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Muted",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (conversation.isBlocked || viewModel.isUserBlocked(conversation.friend.id)) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color.Red.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Blocked",
                                color = Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = conversation.lastTime,
                    fontSize = 11.sp,
                    color = if (conversation.unreadCount > 0) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conversation.lastMessage.contains("missed a call", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.PhoneMissed,
                            contentDescription = "Missed Call",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (conversation.lastMessage.contains("attachment", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = FacebookBlue,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (conversation.lastMessage.contains("photo", ignoreCase = true) || conversation.lastMessage.contains("ছবি", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Photo",
                            tint = FacebookBlue,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = conversation.lastMessage,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(FacebookBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onMoreOptions,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Conversation Options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ChatFilm Left Navigation Drawer
@Composable
fun MessengerDrawerDialog(
    currentUser: User,
    isActiveStatusEnabled: Boolean,
    unreadCount: Int,
    archivedCount: Int,
    messageRequestsCount: Int,
    onDismiss: () -> Unit,
    onToggleActiveStatus: () -> Unit,
    onOpenEditNote: () -> Unit,
    onOpenChats: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenMessageRequests: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400" },
                            contentDescription = currentUser.fullName,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isActiveStatusEnabled) OnlineGreen else Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            currentUser.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isActiveStatusEnabled) "🟢 Active status on" else "⚪ Active status off",
                            fontSize = 11.sp,
                            color = if (isActiveStatusEnabled) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onToggleActiveStatus() }
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ListItem(
                    headlineContent = { Text("Chats", fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.Chat, contentDescription = null, tint = FacebookBlue) },
                    trailingContent = {
                        if (unreadCount > 0) {
                            Surface(shape = CircleShape, color = FacebookBlue) {
                                Text("$unreadCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    },
                    modifier = Modifier.clickable { onOpenChats() }
                )

                ListItem(
                    headlineContent = { Text("Share Note (নোট 💭)", fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text(
                            currentUser.note.ifBlank { "৮০ অক্ষরে নোট লিখুন" },
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingContent = { Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF8E24AA)) },
                    modifier = Modifier.clickable { onOpenEditNote() }
                )

                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("FotoFilm মার্কেটপ্লেস", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(5.dp))
                            FidaVerifiedBadge(size = 14.dp)
                        }
                    },
                    leadingContent = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFE91E63)) },
                    modifier = Modifier.clickable { onOpenMarketplace() }
                )

                ListItem(
                    headlineContent = { Text("Message requests", fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.MarkChatUnread, contentDescription = null, tint = Color(0xFFFF9800)) },
                    trailingContent = {
                        if (messageRequestsCount > 0) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("$messageRequestsCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    },
                    modifier = Modifier.clickable { onOpenMessageRequests() }
                )

                ListItem(
                    headlineContent = { Text("Archive", fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingContent = {
                        if (archivedCount > 0) {
                            Text("$archivedCount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.clickable { onOpenArchive() }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                ListItem(
                    headlineContent = { Text("ChatFilm Settings", fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { onOpenSettings() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Conversation Options Dialog (Pin, Mute, Read/Unread, Archive, Call, Delete)
@Composable
fun ConversationOptionsDialog(
    conversation: Conversation,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleArchive: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit = {},
    onUnblock: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = conversation.friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (conversation.isGroup) conversation.groupName ?: "Group" else conversation.friend.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Call Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(onClick = onAudioCall) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audio Call")
                    }
                    FilledTonalButton(onClick = onVideoCall) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Video Call")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                ListItem(
                    headlineContent = { Text(if (conversation.isPinned) "Unpin conversation" else "Pin to top") },
                    leadingContent = { Icon(Icons.Default.PushPin, contentDescription = null, tint = FacebookBlue) },
                    modifier = Modifier.clickable { onTogglePin() }
                )

                ListItem(
                    headlineContent = { Text(if (conversation.isMuted) "Unmute notifications" else "Mute notifications") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.VolumeMute, contentDescription = null) },
                    modifier = Modifier.clickable { onToggleMute() }
                )

                ListItem(
                    headlineContent = { Text(if (conversation.unreadCount > 0) "Mark as read" else "Mark as unread") },
                    leadingContent = { Icon(Icons.Default.MarkEmailRead, contentDescription = null) },
                    modifier = Modifier.clickable { onToggleRead() }
                )

                ListItem(
                    headlineContent = { Text(if (conversation.isArchived) "Unarchive chat" else "Archive chat") },
                    leadingContent = { Icon(Icons.Default.Archive, contentDescription = null) },
                    modifier = Modifier.clickable { onToggleArchive() }
                )

                if (!conversation.isGroup) {
                    if (conversation.isBlocked) {
                        ListItem(
                            headlineContent = { Text("Unblock ${conversation.friend.fullName}") },
                            leadingContent = { Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue) },
                            modifier = Modifier.clickable { onUnblock() }
                        )
                    } else {
                        ListItem(
                            headlineContent = { Text("Block ${conversation.friend.fullName}", color = Color.Red) },
                            leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
                            modifier = Modifier.clickable { onBlock() }
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text("Delete conversation", color = BangladeshRed) },
                    leadingContent = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = BangladeshRed) },
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Compose New Message (Friend Picker + Create Group Chat Button)
@Composable
fun NewMessageDialog(
    friends: List<ProfileFriend>,
    onDismiss: () -> Unit,
    onCreateGroupClick: () -> Unit,
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
                    Text("New Message", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("To: Type a name...", fontSize = 13.sp) },
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
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option: Create a New Group
                item {
                    Surface(
                        onClick = onCreateGroupClick,
                        shape = RoundedCornerShape(12.dp),
                        color = FacebookBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Create a new group", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FacebookBlue)
                                Text("Chat with friends together", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(filteredFriends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFriend(friend) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                            if (friend.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (friend.isOnline) "Active now" else friend.mutualFriendsCount,
                                fontSize = 12.sp,
                                color = if (friend.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onSelectFriend(friend) },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Chat", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Create New Group Chat
@Composable
fun CreateGroupChatDialog(
    friends: List<ProfileFriend>,
    onDismiss: () -> Unit,
    onCreateGroup: (groupName: String, selectedFriends: List<ProfileFriend>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val selectedFriends = remember { mutableStateListOf<ProfileFriend>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Group Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = { Text("Name your group (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select Members (${selectedFriends.size} selected)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(friends) { friend ->
                        val isSelected = selectedFriends.any { it.id == friend.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedFriends.removeAll { it.id == friend.id }
                                    else selectedFriends.add(friend)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = friend.name,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedFriends.add(friend)
                                    else selectedFriends.removeAll { it.id == friend.id }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (groupName.isNotBlank()) groupName else "Family & Friends Group 🚀"
                    onCreateGroup(finalName, selectedFriends.toList())
                },
                enabled = selectedFriends.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Create (${selectedFriends.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Message Requests (with Accept & Decline)
@Composable
fun MessageRequestsDialog(
    requests: List<User>,
    onDismiss: () -> Unit,
    onAccept: (User) -> Unit,
    onDecline: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MarkChatUnread, contentDescription = null, tint = FacebookBlue)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Message Requests (${requests.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Open a request to see who sent it. They won't know you've seen it until you accept.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (requests.isEmpty()) {
                    Text(
                        text = "কোনো পেন্ডিং মেসেজ রিকোয়েস্ট নেই ✓",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    requests.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.fullName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Sent you a message request", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { onDecline(user.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Decline", tint = BangladeshRed, modifier = Modifier.size(18.dp))
                            }
                            Button(
                                onClick = { onAccept(user) },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Accept", fontSize = 12.sp)
                            }
                        }
                    }
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

// Dialog: Marketplace Chats in ChatFilm
@Composable
fun MarketplaceChatsDialog(
    onDismiss: () -> Unit,
    onStartProductChat: (User, String) -> Unit
) {
    val marketItems = remember {
        listOf(
            Triple(
                User(
                    id = "mkt_seller_1",
                    fullName = "Gadget Zone BD",
                    username = "gadget_zone_bd",
                    avatarUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=300"
                ),
                "iPhone 15 Pro Max (256GB)",
                "৳ ১,৩৫,০০০ • ঢাকা"
            ),
            Triple(
                User(
                    id = "mkt_seller_2",
                    fullName = "Camera House",
                    username = "camera_house",
                    avatarUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=300"
                ),
                "Canon EOS R50 Mirrorless Camera",
                "৳ ৭২,৫০০ • চট্টগ্রাম"
            ),
            Triple(
                User(
                    id = "mkt_seller_3",
                    fullName = "Tech Furniture",
                    username = "tech_furniture",
                    avatarUrl = "https://images.unsplash.com/photo-1598550476439-6847785fcea6?w=300"
                ),
                "Ergonomic Gaming & Studio Chair",
                "৳ ১৮,৯০০ • সিলেট"
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFE91E63))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ChatFilm Marketplace", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.width(6.dp))
                FidaVerifiedBadge(size = 16.dp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "সরাসরি সেলারদের সাথে ChatFilm-এ কথা বলুন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                marketItems.forEach { (seller, product, price) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartProductChat(seller, product) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = seller.avatarUrl,
                                contentDescription = product,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(price, fontSize = 11.sp, color = BangladeshGreen, fontWeight = FontWeight.SemiBold)
                                Text(seller.fullName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.Chat, contentDescription = "Message", tint = FacebookBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

// Dialog: Messenger Settings is now modularly provided in MessengerSettingsComponents.kt

// Reproduction of Messenger Note bubble + avatar
@Composable
fun NoteUserItem(
    user: User,
    noteText: String,
    isMe: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        // Floating speech bubble for Note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp,
            modifier = Modifier.heightIn(min = 28.dp)
        ) {
            Text(
                text = noteText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar with Online Green Dot or '+' for me
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                contentDescription = user.fullName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            if (isMe) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            } else if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isMe) "Your note" else user.fullName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Dialog: Camera Studio
@Composable
fun MessengerCameraDialog(
    onDismiss: () -> Unit,
    onCapture: (photoUrl: String) -> Unit
) {
    var flashOn by remember { mutableStateOf(false) }
    var frontCamera by remember { mutableStateOf(false) }

    val sampleSnaps = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800"
    )
    var selectedSnapIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ChatFilm Camera", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Row {
                    IconButton(onClick = { flashOn = !flashOn }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashOn) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { frontCamera = !frontCamera }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = sampleSnaps[selectedSnapIndex],
                        contentDescription = "Camera Viewfinder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (frontCamera) "📷 Front View" else "📷 Rear View",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleSnaps.forEachIndexed { index, snapUrl ->
                        Surface(
                            onClick = { selectedSnapIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            border = if (selectedSnapIndex == index) androidx.compose.foundation.BorderStroke(2.dp, FacebookBlue) else null,
                            modifier = Modifier.size(44.dp)
                        ) {
                            AsyncImage(
                                model = snapUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCapture(sampleSnaps[selectedSnapIndex]) },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Snap & Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog: Reply to Friend's Note
@Composable
fun NoteReplyDialog(
    friend: User,
    note: String,
    onDismiss: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val quickReactions = listOf("🔥", "❤️", "😂", "😮", "😢", "👍", "👏", "🎉")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                        contentDescription = friend.fullName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(friend.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💭 \"$note\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Quick Reactions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    quickReactions.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { onReply(emoji) }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${friend.fullName}...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (replyText.isNotBlank()) onReply(replyText)
                },
                enabled = replyText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
