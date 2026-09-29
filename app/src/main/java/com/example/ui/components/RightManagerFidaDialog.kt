package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.AdminSlot
import com.example.model.AppLanguage
import com.example.model.Conversation
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightManagerFidaDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()
    val freeQuotaRemaining by viewModel.freeVerifiedQuotaRemaining.collectAsState()
    val allConversations by viewModel.conversations.collectAsState()
    val adminSlots by viewModel.adminSlots.collectAsState()
    val languageStatus by viewModel.languageStatus.collectAsState()

    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Users, 1: Messenger Audit, 2: 10 Admins, 3: 4 Languages
    var inspectingConversation by remember { mutableStateOf<Conversation?>(null) }
    var slotToAssign by remember { mutableStateOf<AdminSlot?>(null) }
    var showAssignAdminDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, active, suspended, verified
    var messengerSearchQuery by remember { mutableStateOf("") }
    var messengerFilter by remember { mutableStateOf("all") } // all, media, suspended

    var userToSuspend by remember { mutableStateOf<User?>(null) }
    var suspensionReasonInput by remember { mutableStateOf("নিয়ম লঙ্ঘন ও অনুপযুক্ত কার্যকলাপ") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredUsers = remember(allUsers, searchQuery, selectedFilter) {
        allUsers.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                    user.fullName.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "active" -> user.isAccountActive
                "suspended" -> !user.isAccountActive
                "verified" -> user.isVerified
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    val filteredConversations = remember(allConversations, messengerSearchQuery, messengerFilter, allUsers) {
        allConversations.filter { conv ->
            val friendUser = allUsers.find { it.id == conv.friend.id } ?: conv.friend
            val matchesQuery = messengerSearchQuery.isBlank() ||
                    conv.friend.fullName.contains(messengerSearchQuery, ignoreCase = true) ||
                    (conv.groupName?.contains(messengerSearchQuery, ignoreCase = true) == true) ||
                    conv.lastMessage.contains(messengerSearchQuery, ignoreCase = true) ||
                    conv.messages.any { it.text.contains(messengerSearchQuery, ignoreCase = true) }

            val matchesFilter = when (messengerFilter) {
                "media" -> conv.messages.any { it.mediaUrl != null }
                "suspended" -> !friendUser.isAccountActive
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    val totalMediaCount = remember(allConversations) {
        allConversations.sumOf { conv -> conv.messages.count { it.mediaUrl != null } }
    }
    val totalMessagesCount = remember(allConversations) {
        allConversations.sumOf { it.messages.size }
    }

    val activeCount = allUsers.count { it.isAccountActive }
    val suspendedCount = allUsers.count { !it.isAccountActive }
    val verifiedCount = allUsers.count { it.isVerified }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Flower Logo (🌸)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFF4081), Color(0xFFE91E63), Color(0xFFFF80AB))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFlorist,
                                        contentDescription = "Flower Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Right Manager Fida",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "🌸", fontSize = 16.sp)
                                    }
                                    Text(
                                        text = "অ্যাডমিন: ${currentUser.fullName} (একক নিয়ন্ত্রণ)",
                                        fontSize = 11.sp,
                                        color = FacebookBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = FacebookBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = FacebookBlue,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "অ্যাডমিন প্যানেল",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FacebookBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Admin Banner & 3000 Free Quota Summary
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            // 3000 Free Quota Stat Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FacebookBlue,
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "৩,০০০ জনকে ফ্রী ভেরিফিকেশন কোটা",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Niyon Islam Sohag-এর নিজস্ব স্পেশাল অধিকার",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "$freeQuotaRemaining",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = FacebookBlue
                                            )
                                            Text(
                                                text = "অবশিষ্ট",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats overview: Total, Active, Suspended
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(
                                    title = "মোট আইডি",
                                    count = "${allUsers.size}",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "অন (সক্রিয়)",
                                    count = "$activeCount",
                                    color = OnlineGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    title = "অফ (নিষিদ্ধ)",
                                    count = "$suspendedCount",
                                    color = Color.Red,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Main Tab Switcher: 4 Tabs (Users, Messenger Audit, 10 Admins, 4 Languages)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { selectedMainTab = 0 },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedMainTab == 0) FacebookBlue else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selectedMainTab == 0) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.People,
                                                contentDescription = null,
                                                tint = if (selectedMainTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "আইডি (${allUsers.size})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedMainTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { selectedMainTab = 1 },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedMainTab == 1) Color(0xFFD81B60) else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selectedMainTab == 1) Color(0xFFD81B60) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = if (selectedMainTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🕵️ মেসেঞ্জার (${allConversations.size})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedMainTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { selectedMainTab = 2 },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedMainTab == 2) Color(0xFF8E24AA) else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selectedMainTab == 2) Color(0xFF8E24AA) else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = if (selectedMainTab == 2) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "👑 ১০ অ্যাডমিন (${adminSlots.count { it.user != null }}/10)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedMainTab == 2) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { selectedMainTab = 3 },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedMainTab == 3) BangladeshGreen else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (selectedMainTab == 3) BangladeshGreen else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = null,
                                                tint = if (selectedMainTab == 3) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🌐 ৪ ভাষা (অন/অফ)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedMainTab == 3) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    when (selectedMainTab) {
                        0 -> {
                            // Tab 0: User Accounts
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("ব্যবহারকারী বা আইডি খুঁজুন...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // Filter Chips for Users
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedFilter == "all",
                                    onClick = { selectedFilter = "all" },
                                    label = { Text("সকল (${allUsers.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = selectedFilter == "active",
                                    onClick = { selectedFilter = "active" },
                                    label = { Text("অন ($activeCount)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = selectedFilter == "suspended",
                                    onClick = { selectedFilter = "suspended" },
                                    label = { Text("অফ ($suspendedCount)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = selectedFilter == "verified",
                                    onClick = { selectedFilter = "verified" },
                                    label = { Text("ভেরিফাইড ($verifiedCount)", fontSize = 11.sp) }
                                )
                            }
                        }
                        1 -> {
                            // Tab 1: Messenger Audit Controls
                            Surface(
                                color = Color(0xFFFCE4EC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Policy, contentDescription = null, tint = Color(0xFFC2185B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "লাইভ চ্যাট অডিট: মোট $totalMessagesCount টি মেসেজ ও $totalMediaCount টি ছবি/ভিডিও নজরদারিতে রয়েছে",
                                        fontSize = 11.sp,
                                        color = Color(0xFF880E4F),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Search Bar for Messenger Audit
                            OutlinedTextField(
                                value = messengerSearchQuery,
                                onValueChange = { messengerSearchQuery = it },
                                placeholder = { Text("মেসেঞ্জারের কথোপকথন, টেক্সট বা ইউজার খুঁজুন...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (messengerSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { messengerSearchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            // Filter Chips for Messenger Audit
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = messengerFilter == "all",
                                    onClick = { messengerFilter = "all" },
                                    label = { Text("সকল চ্যাট (${allConversations.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = messengerFilter == "media",
                                    onClick = { messengerFilter = "media" },
                                    label = { Text("📷 ছবি ও ভিডিও চ্যাট", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = messengerFilter == "suspended",
                                    onClick = { messengerFilter = "suspended" },
                                    label = { Text("🚨 অফকৃত আইডি চ্যাট", fontSize = 11.sp) }
                                )
                            }
                        }
                        else -> { /* No secondary controls for tab 2 and 3 */ }
                    }

                    // Feedback Snackbar
                    snackbarMessage?.let { msg ->
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    when (selectedMainTab) {
                        0 -> {
                            // User List with ON/OFF Switches & Free Verified Granting
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                items(filteredUsers, key = { it.id }) { user ->
                                    UserAdminRow(
                                        user = user,
                                        isMe = user.id == currentUser.id,
                                        onToggleActive = { shouldBeActive ->
                                            if (shouldBeActive) {
                                                viewModel.toggleUserAccountStatus(user.id, true)
                                                snackbarMessage = "${user.fullName}-এর আইডি সফলভাবে 'অন' (চালু) করা হয়েছে।"
                                            } else {
                                                userToSuspend = user
                                            }
                                        },
                                        onGrantFreeVerified = {
                                            if (!user.isVerified) {
                                                viewModel.grantFreeVerified(user.id)
                                                snackbarMessage = "৩,০০০ কোটা থেকে ${user.fullName}-কে ফ্রী Fida Verified প্রদান করা হয়েছে!"
                                            } else {
                                                viewModel.revokeVerified(user.id)
                                                snackbarMessage = "${user.fullName}-এর Fida Verified প্রত্যাহার করা হয়েছে।"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Tab 1: Live Messenger Audit Conversations List
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                items(filteredConversations, key = { it.id }) { conv ->
                                    val friendUser = allUsers.find { it.id == conv.friend.id } ?: conv.friend
                                    AdminMessengerConversationCard(
                                        conversation = conv,
                                        friendUser = friendUser,
                                        onInspect = {
                                            inspectingConversation = conv
                                        },
                                        onToggleActive = { shouldBeActive ->
                                            if (shouldBeActive) {
                                                viewModel.toggleUserAccountStatus(friendUser.id, true)
                                                snackbarMessage = "${friendUser.fullName}-এর আইডি সফলভাবে 'অন' (চালু) করা হয়েছে।"
                                            } else {
                                                userToSuspend = friendUser
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Tab 2: 10 Admin Slots Management
                            AdminSlotsManagementView(
                                adminSlots = adminSlots,
                                onAssignClick = { slot ->
                                    slotToAssign = slot
                                    showAssignAdminDialog = true
                                },
                                onRemoveClick = { slotNum ->
                                    viewModel.removeAdmin(slotNum)
                                    snackbarMessage = "স্লট $slotNum থেকে অ্যাডমিন পদ বাতিল করা হয়েছে।"
                                }
                            )
                        }
                        3 -> {
                            // Tab 3: 4-Language Master ON/OFF Switch (Niyon Islam Sohag exclusive)
                            LanguageMasterControlView(
                                languageStatus = languageStatus,
                                currentUser = currentUser,
                                onToggle = { lang, isOn ->
                                    val success = viewModel.toggleLanguageStatus(lang, isOn)
                                    if (success) {
                                        snackbarMessage = "${lang.displayName} ভাষাটি ${if (isOn) "অন (সক্রিয়)" else "অফ (নিষ্ক্রিয়)"} করা হয়েছে।"
                                    } else {
                                        snackbarMessage = "⚠️ শুধুমাত্র Niyon Islam Sohag এই ভাষা অন বা অফ করতে পারবেন!"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog when Administrator is turning an account OFF
    userToSuspend?.let { user ->
        AlertDialog(
            onDismissRequest = { userToSuspend = null },
            icon = {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "${user.fullName}-এর আইডি বন্ধ (অফ) করবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "অফ করে দিলে এই ব্যবহারকারী অ্যাপে আর কোনো কার্যকলাপ বা পোস্ট করতে পারবে না।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = suspensionReasonInput,
                        onValueChange = { suspensionReasonInput = it },
                        label = { Text("বন্ধ করার কারণ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserAccountStatus(user.id, false, suspensionReasonInput)
                        snackbarMessage = "${user.fullName}-এর আইডি 'অফ' (নিষ্ক্রিয়) করা হয়েছে।"
                        userToSuspend = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("আইডি অফ করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToSuspend = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Live Messenger Audit & Content Inspector Dialog
    inspectingConversation?.let { conv ->
        AdminMessengerAuditDialog(
            conversation = conv,
            viewModel = viewModel,
            onDismiss = { inspectingConversation = null },
            onBanUser = { targetUser, reason ->
                viewModel.toggleUserAccountStatus(targetUser.id, false, reason)
                snackbarMessage = "${targetUser.fullName}-এর আইডি ক্ষতিকর কনটেন্টের জন্য 'অফ' (ব্যান) করা হয়েছে।"
            }
        )
    }

    // Assign Admin Dialog to choose one of the users for any of the 10 admin slots
    if (showAssignAdminDialog && slotToAssign != null) {
        AssignAdminDialog(
            slot = slotToAssign!!,
            allUsers = allUsers,
            onAssign = { user, roleTitle ->
                viewModel.assignAdmin(slotToAssign!!.slotNumber, user, roleTitle)
                snackbarMessage = "স্লট ${slotToAssign!!.slotNumber}-এ ${user.fullName}-কে '$roleTitle' হিসেবে অ্যাডমিন নিয়োগ দেওয়া হয়েছে!"
                showAssignAdminDialog = false
                slotToAssign = null
            },
            onDismiss = {
                showAssignAdminDialog = false
                slotToAssign = null
            }
        )
    }
}

@Composable
private fun StatPill(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UserAdminRow(
    user: User,
    isMe: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onGrantFreeVerified: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (!user.isAccountActive) Color.Red.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!user.isAccountActive) Color.Red.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with online/active indicator
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                        contentDescription = user.fullName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (user.isAccountActive) OnlineGreen else Color.Red)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            FidaVerifiedBadge(size = 14.dp)
                        }
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = FacebookBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "প্রশাসক",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "@${user.username} • ${user.joinedDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Active/Suspended status text
                    if (!user.isAccountActive) {
                        Text(
                            text = "🚫 অ্যাকাউন্ট অফ (নিষিদ্ধ): ${user.suspensionReason ?: "অ্যাডমিন দ্বারা অফ"}",
                            fontSize = 10.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ============================================================
                // ON / OFF SWITCH (ঐ ব্যাক্তি কোনো খারাপ কাজ করলে বন্ধ করার বাটন)
                // ============================================================
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (user.isAccountActive) "অন" else "অফ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isAccountActive) OnlineGreen else Color.Red
                    )
                    Switch(
                        checked = user.isAccountActive,
                        onCheckedChange = { onToggleActive(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OnlineGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Red
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action row: 3,000 Quota Free Verified Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = if (user.isVerified) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isVerified) "Fida Verified সক্রিয় 🔵" else "ভেরিফাইড নেই",
                        fontSize = 11.sp,
                        color = if (user.isVerified) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (user.isVerified) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Grant/Revoke Free Verified Button
                Button(
                    onClick = onGrantFreeVerified,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isVerified) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    if (user.isVerified) {
                        Text(
                            text = "বাতিল করুন",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ফ্রী ভেরিফাইড দিন 🎁",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMessengerConversationCard(
    conversation: Conversation,
    friendUser: User,
    onInspect: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    val mediaCount = conversation.messages.count { it.mediaUrl != null }
    val videoCount = conversation.messages.count { it.isMediaVideo }
    val photoCount = mediaCount - videoCount

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (!friendUser.isAccountActive) Color.Red.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Friend Avatar, Name, Verified, and Status Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(
                        model = friendUser.avatarUrl,
                        contentDescription = friendUser.fullName,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (friendUser.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (conversation.isGroup) conversation.groupName ?: "গ্রুপ চ্যাট" else friendUser.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (friendUser.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = FacebookBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (friendUser.isAccountActive) "আইডি অন (সক্রিয়)" else "আইডি অফ (নিষিদ্ধ)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (friendUser.isAccountActive) OnlineGreen else Color.Red
                        )
                        Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "${conversation.messages.size} টি বার্তা",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Account Toggle
                Switch(
                    checked = friendUser.isAccountActive,
                    onCheckedChange = { onToggleActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OnlineGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Red
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last Message Snippet
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = conversation.lastMessage.ifBlank { "কথোপকথন শুরু হয়েছে" },
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = conversation.lastTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Media badges if any
            if (mediaCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (photoCount > 0) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📷 $photoCount টি ছবি",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (videoCount > 0) {
                        Surface(
                            color = Color(0xFFE91E63).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📹 $videoCount টি ভিডিও",
                                fontSize = 10.sp,
                                color = Color(0xFFC2185B),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("চ্যাট ও মিডিয়া দেখুন (অডিট)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (friendUser.isAccountActive) {
                    OutlinedButton(
                        onClick = { onToggleActive(false) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("আইডি অফ", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onToggleActive(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("আইডি অন", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSlotsManagementView(
    adminSlots: List<AdminSlot>,
    onAssignClick: (AdminSlot) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4A148C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "১০ জন অ্যাডমিন নিয়ন্ত্রণ প্যানেল",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Right Manager Fida • Niyon Islam Sohag",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "অ্যাপ পরিচালনায় সর্বোচ্চ ১০ জন দায়িত্বশীল অ্যাডমিন নিয়োগ ও পরিচালনা করার ক্ষমতা। ১ নম্বর স্লট প্রধান প্রতিষ্ঠাতা ও সুপার অ্যাডমিনের জন্য স্থায়ী।",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        items(adminSlots, key = { it.slotNumber }) { slot ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (slot.user != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (slot.isSuperAdmin) Color(0xFFFFD700) else if (slot.user != null) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (slot.isSuperAdmin) Color(0xFFFFD700) else if (slot.user != null) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                        ) {
                            Text(
                                text = if (slot.isSuperAdmin) "👑 স্লট ${slot.slotNumber} (সুপার অ্যাডমিন)" else "স্লট ${slot.slotNumber}",
                                color = if (slot.isSuperAdmin) Color.Black else if (slot.user != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (slot.isSuperAdmin) {
                            Text(
                                text = "স্থায়ী প্রধান অ্যাডমিন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangladeshGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val user = slot.user
                    if (user != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.fullName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                if (user.isVerified) {
                                    Surface(
                                        shape = CircleShape,
                                        color = FacebookBlue,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "@${user.username} • ${slot.roleTitle}",
                                    fontSize = 12.sp,
                                    color = FacebookBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "যুক্ত হয়েছেন: ${slot.assignedDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (!slot.isSuperAdmin) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onRemoveClick(slot.slotNumber) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("অ্যাডমিন পদ বাতিল করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Empty slot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ফাঁকা অ্যাডমিন স্লট (${slot.slotNumber})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "এই স্লটে একজন অ্যাডমিন নিয়োগ দিন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Button(
                                onClick = { onAssignClick(slot) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("নিয়োগ দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageMasterControlView(
    languageStatus: Map<AppLanguage, Boolean>,
    currentUser: User,
    onToggle: (AppLanguage, Boolean) -> Unit
) {
    val isNiyon = currentUser.id == "user_niyon" || currentUser.fullName.contains("Niyon", ignoreCase = true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNiyon) BangladeshGreen else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isNiyon) "👑" else "🔒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "৪ টি ভাষার মাস্টার অন/অফ কন্ট্রোল",
                                color = if (isNiyon) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isNiyon) "Right Manager Fida • Niyon Islam Sohag (যাচাইকৃত প্রধান মালিক)" else "শুধুমাত্র প্রধান মালিক Niyon Islam Sohag-এর জন্য সংরক্ষিত",
                                color = if (isNiyon) Color.White.copy(alpha = 0.85f) else BangladeshRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isNiyon) {
                            "কোনো ভাষায় সমস্যা বা ভুল দেখা দিলে আপনি (Niyon Islam Sohag) সাথে সাথে সেই ভাষাটি এখান থেকে 'অফ' করে দিতে পারবেন। অফ করা ভাষাটি সাধারণ ইউজারদের স্ক্রিনে লকড থাকবে এবং আপনি ছাড়া অন্য কোনো ব্যক্তি এটি চালু করতে পারবে না।"
                        } else {
                            "⚠️ সতর্কবার্তা: আপনি প্রধান মালিক Niyon Islam Sohag নন। ৪ টি ভাষা অন বা অফ করার একক ক্ষমতা শুধুমাত্র Niyon Islam Sohag-এর হাতে। অন্য কোনো অ্যাডমিন বা ব্যক্তি এটি পরিবর্তন করতে পারবে না।"
                        },
                        color = if (isNiyon) Color.White.copy(alpha = 0.95f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        val languages = listOf(
            Triple(AppLanguage.BENGALI, "বাংলা (Bengali)", "বাংলাদেশ ও বাংলাভাষী অঞ্চলের জন্য প্রধান মাতৃভাষা"),
            Triple(AppLanguage.ENGLISH, "English (Global)", "আন্তর্জাতিক ও বৈশ্বিক ব্যবহারের জন্য অফিশিয়াল ভাষা"),
            Triple(AppLanguage.ARABIC, "العربية (Arabic)", "মধ্যপ্রাচ্য ও আরবিভাষী মুসলিম উম্মাহর জন্য"),
            Triple(AppLanguage.HINDI, "हिन्दी (Hindi)", "দক্ষিণ এশিয়ার হিন্দিভাষী বন্ধুদের জন্য")
        )

        items(languages, key = { it.first.name }) { (lang, title, desc) ->
            val isOn = languageStatus[lang] ?: true
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOn) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOn) BangladeshGreen else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flag, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isOn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isOn) BangladeshGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isOn) "অন (সক্রিয়)" else "অফ (বন্ধ)",
                                        color = if (isOn) BangladeshGreen else Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Master ON/OFF Switch - ONLY active for Niyon Islam Sohag
                    Switch(
                        checked = isOn,
                        enabled = isNiyon,
                        onCheckedChange = { if (isNiyon) onToggle(lang, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignAdminDialog(
    slot: AdminSlot,
    allUsers: List<User>,
    onAssign: (User, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var roleTitle by remember { mutableStateOf("সহকারী অ্যাডমিন") }
    var userSearch by remember { mutableStateOf("") }

    val presetRoles = listOf(
        "সহকারী অ্যাডমিন",
        "সিনিয়র টেকনিক্যাল অ্যাডমিন",
        "কন্টেন্ট ও সেফটি মডারেটর",
        "কমিউনিটি ম্যানেজার"
    )

    val candidateUsers = remember(allUsers, userSearch) {
        allUsers.filter { user ->
            user.id != "user_niyon" && (userSearch.isBlank() || user.fullName.contains(userSearch, ignoreCase = true) || user.username.contains(userSearch, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("স্লট ${slot.slotNumber}-এ অ্যাডমিন নিয়োগ দিন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "১০ জন অ্যাডমিন প্যানেলে দায়িত্ব দেওয়ার জন্য অ্যাপের একজন ব্যবহারকারী সিলেক্ট করুন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userSearch,
                    onValueChange = { userSearch = it },
                    placeholder = { Text("ব্যবহারকারী খুঁজুন...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(130.dp)) {
                    items(candidateUsers, key = { it.id }) { user ->
                        val isSelected = selectedUser?.id == user.id
                        Surface(
                            onClick = { selectedUser = user },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FacebookBlue.copy(alpha = 0.15f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.fullName,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "@${user.username}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("দায়িত্ব / পদবী:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roleTitle,
                    onValueChange = { roleTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetRoles.take(3).forEach { role ->
                        SuggestionChip(
                            onClick = { roleTitle = role },
                            label = { Text(role, fontSize = 9.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedUser?.let { user ->
                        onAssign(user, roleTitle)
                    }
                },
                enabled = selectedUser != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
            ) {
                Text("নিয়োগ নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
