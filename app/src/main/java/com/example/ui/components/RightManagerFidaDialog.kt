package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import com.example.R
import com.example.model.*
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.IntrusionLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightManagerFidaDialog(
    viewModel: FaceFeedViewModel,
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()
    val freeQuotaRemaining by viewModel.freeVerifiedQuotaRemaining.collectAsState()
    val allConversations by viewModel.conversations.collectAsState()
    val adminSlots by viewModel.adminSlots.collectAsState()
    val languageStatus by viewModel.languageStatus.collectAsState()
    val boostTransactions by viewModel.boostTransactions.collectAsState()
    val adminBkash by viewModel.adminBkashNumber.collectAsState()
    val adminNagad by viewModel.adminNagadNumber.collectAsState()
    val adminRocket by viewModel.adminRocketNumber.collectAsState()
    val isCloudConnected by viewModel.isFirebaseCloudConnected.collectAsState()
    val cloudMessage by viewModel.firebaseCloudStatusMessage.collectAsState()
    val cloudinaryStatus by viewModel.cloudinaryStatus.collectAsState()
    val agoraCallState by viewModel.agoraCallState.collectAsState()
    val isBlazeCardEnabled by viewModel.isBlazeCardModeEnabled.collectAsState()
    val quotaCompressionConfig by viewModel.quotaCompressionConfig.collectAsState()
    val allUsersDailyUsage by viewModel.allUsersDailyUsage.collectAsState()
    val fidaContext = LocalContext.current
    val allPosts by viewModel.posts.collectAsState()
    val allPages by viewModel.pages.collectAsState()
    val allGroups by viewModel.groups.collectAsState()
    val verifiedApplications by viewModel.verifiedApplications.collectAsState()
    val freeVerifiedGrantedUsers by viewModel.freeVerifiedGrantedUsers.collectAsState()
    val cyberOffenders by viewModel.cyberOffenders.collectAsState()
    val appUpdateBroadcasts by viewModel.appUpdateBroadcasts.collectAsState()
    val freeVerifiedGrantRecords by viewModel.freeVerifiedGrantRecords.collectAsState()
    val userVerificationAuditEntries by viewModel.userVerificationAuditEntries.collectAsState()
    val timedSuspensionRecords by viewModel.timedSuspensionRecords.collectAsState()
    val permanentVault by viewModel.permanentDeliveryVault.collectAsState()
    val totalDownloads by viewModel.totalAppDownloadCount.collectAsState()
    val intrusionLogs by viewModel.intrusionLogs.collectAsState()
    val supportTickets by viewModel.supportTickets.collectAsState()
    val userAdAnalytics by viewModel.userAdAnalytics.collectAsState()
    val flaggedMessengerAudits by viewModel.flaggedMessengerAudits.collectAsState()
    val hangoutPresetRooms by viewModel.hangoutPresetRooms.collectAsState()
    val fakeIdReports by viewModel.fakeIdReports.collectAsState()
    val isRightManagerUnlocked by viewModel.isRightManagerUnlocked.collectAsState()
    val lockedOffCount by viewModel.niyonLockedOffCount.collectAsState()

    // সিক্রেট সুরক্ষা: শুধুমাত্র Niyon Islam Sohag ব্যতীত অন্য কোনো ব্যবহারকারী প্রবেশ করতে পারবে না
    if (!viewModel.isCurrentUserNiyonIslamSohag()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "প্রবেশাধিকার সংরক্ষিত 🔒",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Right Manager Fida প্যানেল শুধুমাত্র প্রধান প্রতিষ্ঠাতা Niyon Islam Sohag-এর ব্যক্তিগত একক নিয়ন্ত্রণের জন্য তৈরি। আপনি ব্যতীত অন্য কোনো ব্যবহারকারী এই সিক্রেট প্যানেলে প্রবেশ করতে পারবেন না।",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ফিরে যান", color = Color.White)
                }
            }
        )
        return
    }

    // রাইট ম্যানেজার ফিদা তালা 🔒 (Master PIN Lock Screen)
    if (!isRightManagerUnlocked) {
        RightManagerPinLockDialog(
            viewModel = viewModel,
            onDismiss = onDismiss
        )
        return
    }

    var showEditPaymentDialog by remember { mutableStateOf(false) }
    var editBkashNumber by remember { mutableStateOf(adminBkash) }
    var editNagadNumber by remember { mutableStateOf(adminNagad) }
    var editRocketNumber by remember { mutableStateOf(adminRocket) }

    var selectedMainTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var inspectingConversation by remember { mutableStateOf<Conversation?>(null) }
    var slotToAssign by remember { mutableStateOf<AdminSlot?>(null) }
    var showAssignAdminDialog by remember { mutableStateOf(false) }
    var showFaceAdAdminDialog by remember { mutableStateOf(false) }

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

    val all13Features = remember(
        allUsers.size, allConversations.size, adminSlots, boostTransactions.size,
        verifiedApplications, allPages.size, allGroups.size, cyberOffenders.size,
        freeVerifiedGrantRecords.size, userVerificationAuditEntries.size,
        timedSuspensionRecords.size, permanentVault.size, totalDownloads, intrusionLogs.size,
        supportTickets.size, userAdAnalytics.size, flaggedMessengerAudits.size, hangoutPresetRooms.size,
        fakeIdReports.size
    ) {
        listOf(
            Triple(0, "১. আইডি ও কোটা (${allUsers.size})", Icons.Default.People) to FacebookBlue,
            Triple(1, "২. মেসেঞ্জার (${allConversations.size})", Icons.Default.Visibility) to Color(0xFFD81B60),
            Triple(2, "৩. ১০ অ্যাডমিন (${adminSlots.count { it.user != null }}/10)", Icons.Default.Shield) to Color(0xFF8E24AA),
            Triple(3, "৪. ৪ ভাষা অন/অফ", Icons.Default.Language) to BangladeshGreen,
            Triple(4, "৫. বুস্ট ম্যানেজার (${boostTransactions.size})", Icons.Default.Campaign) to Color(0xFF1B5E20),
            Triple(5, "৬. ক্লাউড DB ও Cloudinary ☁️", Icons.Default.Cloud) to Color(0xFFE65100),
            Triple(6, "৭. ফিদা ভেরিফাইড (${verifiedApplications.count { it.status == "PENDING" }})", Icons.Default.Verified) to BangladeshGreen,
            Triple(7, "৮. পেজ ম্যানেজার (${allPages.size})", Icons.Default.Description) to Color(0xFF00897B),
            Triple(8, "৯. গ্রুপ ম্যানেজার (${allGroups.size})", Icons.Default.Groups) to Color(0xFF5D4037),
            Triple(9, "১০. পেমেন্ট হিস্ট্রি 💰", Icons.Default.AccountBalanceWallet) to Color(0xFFF57F17),
            Triple(10, "১১. ফ্রী মেগা বুস্ট (১M+) 🚀", Icons.Default.RocketLaunch) to Color(0xFFE91E63),
            Triple(11, "১২. সাইবার আইনি অডিট (${cyberOffenders.size}) ⚖️", Icons.Default.Gavel) to Color(0xFFC62828),
            Triple(12, "১৩. আপডেট ব্রডকাস্টার 📢", Icons.Default.NotificationsActive) to Color(0xFF6A1B9A),
            Triple(13, "১৪. ফ্রী ভেরিফাই হিস্ট্রি (${freeVerifiedGrantRecords.size}) 📜", Icons.Default.HistoryEdu) to Color(0xFF00695C),
            Triple(14, "১৫. ভেরিফাই গ্রহণকারী লেজার (${userVerificationAuditEntries.size}) 🎖️", Icons.Default.Badge) to Color(0xFF283593),
            Triple(15, "১৬. সার্চ ও আইডি সাসপেন্ড (${timedSuspensionRecords.count { it.isSuspendedActive }}) 🚫", Icons.Default.Block) to Color(0xFFD32F2F),
            Triple(16, "১৭. যেকোনো পোস্ট ফ্রী বুস্ট ও প্রমোট (${allPosts.count { it.isFreePromotedByNiyon }}) 🚀", Icons.Default.RocketLaunch) to Color(0xFF673AB7),
            Triple(17, "১৮. Face AD এডমিন 📢", Icons.Default.Campaign) to Color(0xFFFF6D00),
            Triple(18, "১৯. গোপন চ্যাট ভল্ট ও খারাপ কনটেন্ট (${permanentVault.size}) 🛡️", Icons.Default.Security) to Color(0xFFB71C1C),
            Triple(19, "২০. লাইভ ইউজার ও ডাউনলোড কাউন্টার (${totalDownloads}+) 📈", Icons.Default.DownloadForOffline) to Color(0xFF00796B),
            Triple(20, "২১. অ্যাপ হ্যাক ও অনুপ্রবেশ মনিটর (${intrusionLogs.size}) 🚨", Icons.Default.Security) to Color(0xFFD50000),
            Triple(21, "২২. যেকোনো ভিডিও ফ্রী বুস্টার (Niyon Special) 🎬🚀", Icons.Default.VideoCameraBack) to Color(0xFFE040FB),
            Triple(22, "২৩. সাপোর্ট টিম ইনবক্স (${supportTickets.size}) 🎧", Icons.Default.SupportAgent) to Color(0xFF00897B),
            Triple(23, "২৪. ইউজার অ্যাড ভিউ ও স্কিপ ট্র্যাকার (${userAdAnalytics.size}) 📊", Icons.Default.Leaderboard) to Color(0xFF6A1B9A),
            Triple(24, "২৫. ১ কোটি ইউজারের মেসেনজার খারাপ ছবি/ভিডিও/ভয়েস ফিল্টার (${flaggedMessengerAudits.size}) 🚨", Icons.Default.Warning) to Color(0xFFD50000),
            Triple(25, "২৬. ৪৩.১ MB কোটা, কম্প্রেশন ও অল-টাইম ফ্রি 🗜️", Icons.Default.Compress) to BangladeshGreen,
            Triple(26, "২৭. বন্ধুদের সাথে আড্ডা গ্রুপ (${allGroups.size + hangoutPresetRooms.size}) 🎙️", Icons.Default.RecordVoiceOver) to Color(0xFF00838F),
            Triple(27, "২৮. ফেইক আইডি ডিটেক্টর ও ব্লক (${fakeIdReports.size}) 🕵️", Icons.Default.GppBad) to Color(0xFFC62828)
        )
    }

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
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                    Text(
                                        text = if (lockedOffCount > 0) {
                                            "🔒 আপনি চালু না করা পর্যন্ত $lockedOffCount টি অপশন লক-বন্ধ থাকবে"
                                        } else {
                                            "🔒 আপনি বন্ধ করলে চালু না করা পর্যন্ত বন্ধ থাকবে"
                                        },
                                        fontSize = 9.5.sp,
                                        color = if (lockedOffCount > 0) Color(0xFFD32F2F) else BangladeshGreen,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { viewModel.lockRightManager() },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "তালা লাগান (Lock Now)",
                                        tint = Color(0xFFD50000),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // 15 Process Features & Options - Pinned Tab Row (সহজেই যেকোনো ফিচারে যান)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                all13Features.forEach { (item, themeColor) ->
                                    val (index, title, icon) = item
                                    val isSelected = selectedMainTab == index
                                    Surface(
                                        onClick = { selectedMainTab = index },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) themeColor else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.2.dp,
                                            if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else themeColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding()
                ) {
                    when (selectedMainTab) {
                        0 -> {
                            // User List with 3000 Free Quota, Stats, Search, and Switches
                            // সম্পূর্ণ স্ক্রিন নিচ থেকে টান দিলে উপরে মসৃণভাবে স্ক্রোল হবে
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
                            ) {
                                // 3000 Free Quota Summary Card & Stats Pills & 15 Features Badge
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
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
                                                        text = "৫০,০০০ (50k) জনকে ফ্রী ভেরিফিকেশন কোটা",
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

                                        // 15 Master Features Header Banner
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFF4081).copy(alpha = 0.08f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🌸 মোট ১৫টি অপশন (১৩ মাস্টার + ২ অডিট)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFC2185B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Surface(
                                                    color = BangladeshGreen,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "১৫ টি অপশনই সক্রিয় ✓",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Search Bar for Users
                                item {
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
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Filter Chips for Users
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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

                                // All Users list
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
                                        onQuickSuspend = {
                                            userToSuspend = user
                                        },
                                        onGrantFreeVerified = {
                                            if (!user.isVerified) {
                                                viewModel.grantFreeVerified(user.id)
                                                snackbarMessage = "৫০,০০০ (50k) কোটা থেকে ${user.fullName}-কে ফ্রী Fida Verified প্রদান করা হয়েছে!"
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
                            // সম্পূর্ণ স্ক্রিন নিচ থেকে টান দিলে উপরে মসৃণভাবে স্ক্রোল হবে
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
                            ) {
                                item {
                                    Surface(
                                        color = Color(0xFFFCE4EC),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Policy, contentDescription = null, tint = Color(0xFFC2185B), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "লাইভ চ্যাট অডিট: মোট $totalMessagesCount টি মেসেজ ও $totalMediaCount টি ছবি/ভিডিও নজরদারিতে রয়েছে",
                                                fontSize = 11.sp,
                                                color = Color(0xFF880E4F),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                item {
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
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                            label = { Text("📷 ছবি ও ভিডিও", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = messengerFilter == "suspended",
                                            onClick = { messengerFilter = "suspended" },
                                            label = { Text("🚨 অফকৃত আইডি", fontSize = 11.sp) }
                                        )
                                    }
                                }

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
                                        snackbarMessage = if (isOn) {
                                            "✅ ${lang.displayName} ভাষাটি অন (সক্রিয়) করা হয়েছে।"
                                        } else {
                                            "🔒 ${lang.displayName} ভাষাটি বন্ধ করা হয়েছে — আপনি (Niyon Islam Sohag) চালু না করা পর্যন্ত চালু হবে না।"
                                        }
                                    } else {
                                        snackbarMessage = "⚠️ শুধুমাত্র Niyon Islam Sohag এই ভাষা অন বা অফ করতে পারবেন!"
                                    }
                                }
                            )
                        }
                        4 -> {
                            // Tab 4: Boost & Ads Manager (Exclusive for Niyon Islam Sohag)
                            AdminBoostManagerView(
                                boostTransactions = boostTransactions,
                                adminBkash = adminBkash,
                                adminNagad = adminNagad,
                                adminRocket = adminRocket,
                                onEditNumbersClick = {
                                    editBkashNumber = adminBkash
                                    editNagadNumber = adminNagad
                                    editRocketNumber = adminRocket
                                    showEditPaymentDialog = true
                                },
                                onUpdateStatus = { txId, newStatus ->
                                    viewModel.updateBoostStatus(txId, newStatus)
                                    snackbarMessage = if (newStatus.equals("ACTIVE", ignoreCase = true)) {
                                        "✅ ক্যাম্পেইন স্ট্যাটাস চালু করা হয়েছে: $newStatus"
                                    } else {
                                        "🔒 ক্যাম্পেইন বন্ধ করা হয়েছে ($newStatus) — আপনি চালু না করা পর্যন্ত চালু হবে না।"
                                    }
                                },
                                onDeleteTransaction = { txId ->
                                    viewModel.deleteBoostTransaction(txId)
                                    snackbarMessage = "ক্যাম্পেইন রেকর্ড তালিকা থেকে মুছে ফেলা হয়েছে।"
                                }
                            )
                        }
                        5 -> {
                            // Tab 5: Firebase Cloud Server, Cloudinary Free Cloud, Agora Live Calls & Card Billing (Exclusive for Niyon Islam Sohag)
                            AdminFirebaseCloudView(
                                isCloudConnected = isCloudConnected,
                                cloudMessage = cloudMessage,
                                cloudinaryStatus = cloudinaryStatus,
                                agoraCallState = agoraCallState,
                                isBlazeCardEnabled = isBlazeCardEnabled,
                                allUsersCount = allUsers.size,
                                allPostsCount = allPosts.size,
                                allConversationsCount = allConversations.size,
                                boostCount = boostTransactions.size,
                                onToggleCloudinary = { enabled ->
                                    viewModel.toggleCloudinaryEnabled(enabled)
                                    snackbarMessage = if (enabled) {
                                        "Cloudinary ফ্রি মিডিয়া আপলোড (২৫ GB/মাস) চালু করা হয়েছে ✓"
                                    } else {
                                        "🔒 Cloudinary আপলোড বন্ধ করা হয়েছে — আপনি চালু না করা পর্যন্ত বন্ধ থাকবে"
                                    }
                                },
                                onToggleBlazeCard = { enabled ->
                                    viewModel.toggleBlazeCardMode(enabled, fidaContext)
                                    snackbarMessage = if (enabled) {
                                        "💳 ব্যাংক কার্ড ও Blaze পে-অ্যাজ-ইউ-গো মোড চালু করা হয়েছে ✓"
                                    } else {
                                        "🔒 ব্যাংক কার্ড ও Blaze মোড বন্ধ রাখা হয়েছে — আপনি চালু না করা পর্যন্ত চালু হবে না ✓"
                                    }
                                },
                                onRefreshConnection = {
                                    viewModel.checkFirebaseCloudConnection()
                                    snackbarMessage = "ফায়ারবেস, Cloudinary ও Agora ক্লাউড স্ট্যাটাস রিফ্রেশ করা হয়েছে।"
                                },
                                onTriggerSync = {
                                    snackbarMessage = "গুগল ফায়ারবেস ক্লাউডে সম্পূর্ণ ডাটা লাইভ সিঙ্ক সম্পন্ন হয়েছে!"
                                }
                            )
                        }
                        6 -> {
                            // Tab 6: Fida Verified Badge Manager & Review
                            AdminFidaVerifiedReviewView(
                                verifiedApplications = verifiedApplications,
                                allUsers = allUsers,
                                freeQuotaRemaining = freeQuotaRemaining,
                                onApproveApplication = { appId ->
                                    viewModel.approveVerifiedApplication(appId)
                                    snackbarMessage = "ফিদা ভেরিফাইড আবেদন রিভিউ করে সফলভাবে অনুমোদন দেওয়া হয়েছে!"
                                },
                                onRejectApplication = { appId ->
                                    viewModel.rejectVerifiedApplication(appId)
                                    snackbarMessage = "ফিদা ভেরিফাইড আবেদনটি বাতিল করা হয়েছে।"
                                },
                                onRevokeFreeVerified = { userId ->
                                    viewModel.revokeVerified(userId)
                                    snackbarMessage = "ব্যবহারকারীর ফ্রী ফিদা ভেরিফাইড ব্যাজ সফলভাবে বাতিল করা হয়েছে।"
                                }
                            )
                        }
                        7 -> {
                            // Tab 7: Pages Manager (All Pages created with ON/OFF switch)
                            AdminPagesManagerView(
                                allPages = allPages,
                                onTogglePage = { pageId, isActive ->
                                    viewModel.togglePageStatus(pageId, isActive)
                                    snackbarMessage = if (isActive) {
                                        "✅ পেজের স্ট্যাটাস 'অন (সক্রিয়)' করা হয়েছে।"
                                    } else {
                                        "🔒 পেজটি বন্ধ করা হয়েছে — আপনি (Niyon Islam Sohag) চালু না করা পর্যন্ত চালু হবে না।"
                                    }
                                }
                            )
                        }
                        8 -> {
                            // Tab 8: Groups Manager (All Groups created with ON/OFF switch)
                            AdminGroupsManagerView(
                                allGroups = allGroups,
                                onToggleGroup = { groupId, isActive ->
                                    viewModel.toggleGroupStatus(groupId, isActive)
                                    snackbarMessage = if (isActive) {
                                        "✅ গ্রুপের স্ট্যাটাস 'অন (সক্রিয়)' করা হয়েছে।"
                                    } else {
                                        "🔒 গ্রুপটি বন্ধ করা হয়েছে — আপনি (Niyon Islam Sohag) চালু না করা পর্যন্ত চালু হবে না।"
                                    }
                                }
                            )
                        }
                        9 -> {
                            // Tab 9: Master Payment & Date Ledger (Boost & Verified Transactions with Exact Date/Month/Year)
                            AdminPaymentLedgerView(
                                boostTransactions = boostTransactions,
                                verifiedApplications = verifiedApplications,
                                onConfirmBoost = { txId ->
                                    viewModel.updateBoostStatus(txId, "ACTIVE")
                                    snackbarMessage = "বুস্ট পেমেন্ট নিশ্চিত করে বিজ্ঞাপনটি সক্রিয় করা হয়েছে!"
                                },
                                onConfirmVerified = { appId ->
                                    viewModel.approveVerifiedApplication(appId)
                                    snackbarMessage = "পেমেন্ট নিশ্চিত করে ভেরিফাইড ব্যাজ অনুমোদন দেওয়া হয়েছে!"
                                }
                            )
                        }
                        10 -> {
                            // Tab 10: Owner Free Mega Boost & 1M+ Reach (Exclusive for Niyon Islam Sohag)
                            AdminOwnerFreeMegaBoostView(
                                posts = allPosts,
                                currentUser = currentUser,
                                onBoostPost = { postId, reach ->
                                    viewModel.ownerFreeMegaBoost(postId, reach)
                                    snackbarMessage = "🎉 ওনার ফ্রী মেগা বুস্ট সক্রিয় হয়েছে! পোস্টটি $reach মানুষের কাছে পৌঁছাবে।"
                                }
                            )
                        }
                        11 -> {
                            // Tab 11: Feature 12: খারাপ মন্তব্যকারী ও সাইবার অপরাধী অডিট (আইনগত প্রোফাইল)
                            AdminCyberCrimeAuditView(
                                offenders = cyberOffenders,
                                onTakeLegalAction = { reportId, action ->
                                    viewModel.takeLegalActionAgainstOffender(reportId, action)
                                    snackbarMessage = when (action) {
                                        "BAN" -> "🛑 অভিযুক্তের আইডি ও ডিভাইস স্থায়ীভাবে ব্যান করা হয়েছে!"
                                        "NOTICE" -> "🚨 সাইবার নিরাপত্তা ও ডিজিটাল আইনগত নোটিশ জারি হয়েছে!"
                                        "PARDON" -> "🕊️ ক্ষমা আবেদন গ্রহণ করে আইডি সক্রিয় করা হয়েছে।"
                                        else -> "আইনগত পদক্ষেপ সম্পন্ন।"
                                    }
                                }
                            )
                        }
                        12 -> {
                            // Tab 12: Feature 13: অ্যাপ আপডেট নোটিশ ব্রডকাস্টার (সবার আইডিতে নোটিফিকেশন)
                            AdminAppUpdateBroadcasterView(
                                currentUser = currentUser,
                                broadcasts = appUpdateBroadcasts,
                                onBroadcast = { version, title, desc ->
                                    viewModel.broadcastAppUpdateNotice(version, title, desc)
                                    snackbarMessage = "📢 সকল ব্যবহারকারীর আইডিতে অ্যাপ আপডেট নোটিফিকেশন পাঠানো হয়েছে!"
                                }
                            )
                        }
                        13 -> {
                            // Tab 13: Option 14: ফ্রী ফিদা ভেরিফাই বিতরণ ইতিহাস (১ জনকে কতো বার দিছি, কোন মাসে দিছি, তারিখ সহ)
                            AdminFreeVerifiedHistoryView(
                                records = freeVerifiedGrantRecords,
                                quotaRemaining = freeQuotaRemaining,
                                onGrantMore = { userId ->
                                    viewModel.grantFreeVerified(userId)
                                    snackbarMessage = "৫০,০০০ (50k) কোটা থেকে পুনরায় ফ্রী ভেরিফিকেশন অনুমোদিত!"
                                }
                            )
                        }
                        14 -> {
                            // Tab 14: Option 15: আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (তারিখ ও প্রোফাইল আইকন সহ অডিট লেজার)
                            AdminUserVerificationAuditView(
                                auditEntries = userVerificationAuditEntries
                            )
                        }
                        15 -> {
                            // Tab 15: Option 16: সার্চ ও নির্দিষ্ট মেয়াদে আইডি সাসপেন্ড (১০টি মেয়াদ, অন/অফ, কেনছেল, প্রোফাইল ও তারিখ)
                            AdminTimedSuspensionManagerView(
                                allUsers = allUsers,
                                suspensionRecords = timedSuspensionRecords,
                                onSuspendUser = { userId, durationIdx, durationTxt, reason ->
                                    viewModel.addTimedSuspension(userId, durationIdx, durationTxt, reason)
                                    snackbarMessage = "আইডি সফলভাবে $durationTxt জন্য সাসপেন্ড করা হয়েছে! 🚫"
                                },
                                onToggleActive = { recordId ->
                                    viewModel.toggleTimedSuspensionActive(recordId)
                                    snackbarMessage = "সাসপেন্ড অবস্থা সফলভাবে পরিবর্তন করা হয়েছে!"
                                },
                                onCancelSuspension = { recordId ->
                                    viewModel.cancelTimedSuspension(recordId)
                                    snackbarMessage = "সাসপেন্ড বাতিল/কেনছেল করা হয়েছে! আইডি পুনরায় সচল ✅"
                                }
                            )
                        }
                        16 -> {
                            // Tab 16: Option 17: যেকোনো ব্যবহারকারীর পোস্ট ফ্রী বুস্ট ও প্রমোট
                            AdminFreePostBoostManagerView(
                                allPosts = allPosts,
                                allUsers = allUsers,
                                onBoostPost = { postId, reach, days, audience ->
                                    viewModel.freeBoostAnyPost(postId, reach, days, audience)
                                    snackbarMessage = "পোস্ট সফলভাবে ফ্রীতে বুস্ট ও প্রমোট করা হয়েছে! 🚀"
                                },
                                onToggleActive = { postId ->
                                    viewModel.togglePostBoostActive(postId)
                                    snackbarMessage = "বুস্ট সক্রিয়তার অবস্থা পরিবর্তন করা হয়েছে!"
                                },
                                onCancelBoost = { postId ->
                                    viewModel.cancelPostBoost(postId)
                                    snackbarMessage = "বুস্ট বাতিল/কেনছেল করা হয়েছে!"
                                }
                            )
                        }
                        17 -> {
                            // Tab 17: Option 18: Face AD এডমিন (Ads Manager • AdMob • Audio Music Player)
                            AdminFaceAdAdminHubView(
                                viewModel = viewModel,
                                onOpenFullFaceAdDialog = {
                                    showFaceAdAdminDialog = true
                                }
                            )
                        }
                        18 -> {
                            // Tab 18: Option 19: গোপন চ্যাট সার্ভিল্যান্স ও পার্মানেন্ট ভল্ট (খারাপ লেখা, ছবি, ভিডিও, ভয়েস, স্ক্রিনশট)
                            AdminSecretSurveillanceVaultView(
                                vaultMessages = permanentVault,
                                allUsers = allUsers,
                                onSuspendUser = { userToSuspend = it }
                            )
                        }
                        19 -> {
                            // Tab 19: Option 20: লাইভ ইউজার ও ডাউনলোড কাউন্টার হিস্ট্রি (১ জন, ৫০ জন... ক্রমবৃদ্ধি)
                            AdminLiveUsersDownloadCounterView(
                                totalDownloads = totalDownloads,
                                allUsers = allUsers,
                                onSimulateDownload = { viewModel.recordNewAppDownload() }
                            )
                        }
                        20 -> {
                            // Tab 20: Option 21: অ্যাপ হ্যাক ও অনুপ্রবেশ মনিটর 🚨 (Anti-Hack Logs)
                            AdminAntiHackMonitorView(
                                intrusionLogs = intrusionLogs,
                                onSimulateTest = { viewModel.simulateIntrusionTest() },
                                onBlockIntruder = {
                                    viewModel.blockIntruderIp(it)
                                    snackbarMessage = "আক্রমণকারীর আইপি ও ডিভাইস পার্মানেন্ট ব্লক করা হয়েছে!"
                                },
                                onClearResolved = {
                                    viewModel.clearResolvedIntrusions()
                                    snackbarMessage = "সমস্ত সমাধানকৃত অনুপ্রবেশ হিস্ট্রি ক্লিয়ার করা হয়েছে!"
                                }
                            )
                        }
                        21 -> {
                            // Tab 21: Option 22: যেকোনো ভিডিও ফ্রী বুস্টার 🎬🚀 (Niyon Special)
                            AdminFreeVideoBoosterView(
                                videoPosts = allPosts.filter { it.isVideo || it.isReel },
                                onFreeBoostVideo = { postId, reach, badge ->
                                    viewModel.freeBoostAnyVideo(postId, reach, badge)
                                    snackbarMessage = "✅ ভিডিওটি সফলভাবে বিনামূল্যে ভিআইপি বুস্ট ও ভাইরাল করা হয়েছে!"
                                }
                            )
                        }
                        22 -> {
                            // Tab 22: Option 23: সাপোর্ট টিম ইনবক্স ও ৫টি স্ক্রিনশট 🎧
                            AdminSupportTeamInboxView(
                                tickets = supportTickets,
                                onReplyTicket = { ticketId, replyText ->
                                    viewModel.replyToSupportTicket(ticketId, replyText)
                                    snackbarMessage = "✅ ব্যবহারকারীর কাছে সাপোর্ট টিমের উত্তর সফলভাবে পাঠানো হয়েছে!"
                                },
                                onDeleteTicket = { ticketId ->
                                    viewModel.deleteSupportTicket(ticketId)
                                    snackbarMessage = "টিকেট মুছে ফেলা হয়েছে!"
                                }
                            )
                        }
                        23 -> {
                            // Tab 23: Option 24: ইউজার অ্যাড ভিউ ও স্কিপ ট্র্যাকার 📊
                            AdminUserAdAnalyticsView(
                                analyticsList = userAdAnalytics
                            )
                        }
                        24 -> {
                            // Tab 24: Option 25: ১ কোটি ইউজারের মেসেনজার খারাপ ছবি, ভিডিও, ভয়েস ও আপত্তিকর চ্যাট অডিট 🚨
                            AdminFlaggedMessengerMonitorView(
                                audits = flaggedMessengerAudits,
                                onDeleteMessage = { id ->
                                    viewModel.deleteFlaggedMessage(id)
                                    snackbarMessage = "মেসেঞ্জার থেকে খারাপ মেসেজটি সম্পূর্ণ মুছে দেওয়া হয়েছে! 🗑️"
                                },
                                onBanSender = { id ->
                                    viewModel.banFlaggedSender(id)
                                    snackbarMessage = "আইডি ব্যান ও অ্যাকাউন্ট সাসপেন্ড করা হয়েছে! 🚫"
                                },
                                onSendWarning = { id ->
                                    viewModel.sendWarningToFlaggedSender(id)
                                    snackbarMessage = "ব্যবহারকারীকে সরাসরি সতর্কবার্তা পাঠানো হয়েছে! ⚠️"
                                }
                            )
                        }
                        25 -> {
                            // Tab 26: অটো কম্প্রেশন (১০০ MB -> ৩০ MB), দৈনিক ৪৩.১ MB খরচ ট্র্যাকার ও অল-টাইম ফ্রি ফিচার
                            AdminDaily43MbQuotaCompressionView(
                                config = quotaCompressionConfig,
                                userUsages = allUsersDailyUsage,
                                onToggleAutoCompression = { enabled ->
                                    viewModel.toggleAutoCompression(enabled)
                                    snackbarMessage = if (enabled) {
                                        "🗜️ অটো কম্প্রেশন চালু: ১০০ MB ভিডিও → ৩০ MB, ছবি → ৩০০ KB ✓"
                                    } else {
                                        "🔒 অটো কম্প্রেশন বন্ধ করা হয়েছে — আপনি চালু না করা পর্যন্ত চালু হবে না"
                                    }
                                },
                                onToggleDailyLimit = { enabled ->
                                    viewModel.toggleDaily43MbLimit(enabled)
                                    snackbarMessage = if (enabled) {
                                        "📊 দৈনিক ৪৩.১ MB ফ্রি কোটা নিয়ম চালু করা হয়েছে ✓"
                                    } else {
                                        "🔒 দৈনিক ৪৩.১ MB কোটা লিমিট বন্ধ করা হয়েছে — আপনি চালু না করা পর্যন্ত চালু হবে না"
                                    }
                                },
                                onResetUserQuota = { userId ->
                                    viewModel.resetUserDailyQuota(userId)
                                    snackbarMessage = "ব্যবহারকারীর আজকের ৪৩.১ MB কোটা রিসেট করা হয়েছে ✓"
                                },
                                onResetAllQuota = {
                                    viewModel.resetAllUsersDailyQuota()
                                    snackbarMessage = "সকল ব্যবহারকারীর আজকের ৪৩.১ MB কোটা রিসেট হয়েছে ↻"
                                }
                            )
                        }
                        26 -> {
                            // Tab 27: বন্ধুদের সাথে আড্ডা গ্রুপ (যারা গ্রুপ খুলবে তাদের গ্রুপের নাম ও প্রোফাইল আইকন সহ অন/অফ সুইচ)
                            AdminHangoutGroupsControlView(
                                allGroups = allGroups,
                                presetRooms = hangoutPresetRooms,
                                onToggleGroup = { groupId, groupName, isActive ->
                                    viewModel.toggleGroupStatus(groupId, isActive)
                                    snackbarMessage = if (isActive) {
                                        "✅ \"$groupName\" বন্ধুদের সাথে আড্ডা গ্রুপ চালু (অন) করা হয়েছে!"
                                    } else {
                                        "🔒 \"$groupName\" আড্ডা গ্রুপ বন্ধ করা হয়েছে — আপনি চালু না করা পর্যন্ত চালু হবে না!"
                                    }
                                },
                                onTogglePresetRoom = { roomKey, roomTitle, isActive ->
                                    viewModel.toggleHangoutPresetRoomStatus(roomKey, isActive)
                                    snackbarMessage = if (isActive) {
                                        "✅ \"$roomTitle\" আড্ডা গ্রুপ চালু (অন) করা হয়েছে!"
                                    } else {
                                        "🔒 \"$roomTitle\" আড্ডা গ্রুপ বন্ধ করা হয়েছে — আপনি চালু না করা পর্যন্ত চালু হবে না!"
                                    }
                                }
                            )
                        }
                        27 -> {
                            // Tab 28: ফেইক আইডি ডিটেক্টর ও ব্লক (১ ব্যক্তি ৩ আইডি, অন্যের ছবি, ছেলে->মেয়ে / মেয়ে->ছেলে)
                            AdminFakeIdDetectorView(
                                reports = fakeIdReports,
                                onPermanentBan = { report ->
                                    viewModel.markFakeIdReportPermanentBanned(report.id)
                                    snackbarMessage = "⛔ \"${report.attemptedName}\" স্থায়ীভাবে নিষিদ্ধ (Permanent Ban) করা হয়েছে!"
                                },
                                onDeleteReport = { report ->
                                    viewModel.deleteFakeIdReport(report.id)
                                    snackbarMessage = "রিপোর্টটি তালিকা থেকে মুছে ফেলা হয়েছে ✓"
                                },
                                onClearAll = {
                                    viewModel.clearAllFakeIdReports()
                                    snackbarMessage = "সকল ফেইক আইডি লগ পরিষ্কার করা হয়েছে ✓"
                                }
                            )
                        }
                    }

                    // Floating Feedback Snackbar
                    snackbarMessage?.let { msg ->
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                }
            }
        }
    }

    // Face AD এডমিন পূর্ণাঙ্গ ডায়ালগ (Right Manager Fida থেকে সরাসরি খোলা)
    if (showFaceAdAdminDialog) {
        FaceAdAdminDialog(
            viewModel = viewModel,
            onDismiss = { showFaceAdAdminDialog = false }
        )
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

    // Edit Admin Payment Gateway Numbers Dialog
    if (showEditPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showEditPaymentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💳", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অ্যাডমিন পেমেন্ট নম্বর সেটিংস", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ব্যবহারকারীরা বুস্ট করার সময় এই নম্বরে টাকা পাঠাবে (Personal / Send Money):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editBkashNumber,
                        onValueChange = { editBkashNumber = it },
                        label = { Text("বিকাশ নম্বর (bKash)") },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.img_bkash_logo),
                                contentDescription = "bKash",
                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNagadNumber,
                        onValueChange = { editNagadNumber = it },
                        label = { Text("নগদ নম্বর (Nagad)") },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.img_nagad_logo),
                                contentDescription = "Nagad",
                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRocketNumber,
                        onValueChange = { editRocketNumber = it },
                        label = { Text("রকেট নম্বর (Rocket)") },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.img_rocket_logo),
                                contentDescription = "Rocket",
                                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAdminPaymentNumbers(editBkashNumber, editNagadNumber, editRocketNumber)
                        snackbarMessage = "বিকাশ, নগদ ও রকেট নম্বর সফলভাবে আপডেট ও সেভ হয়েছে!"
                        showEditPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("নম্বর সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPaymentDialog = false }) {
                    Text("বাতিল")
                }
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
    onQuickSuspend: () -> Unit,
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
                    FotoFilmAvatar(
                        imageUrl = user.avatarUrl,
                        name = user.fullName,
                        contentDescription = user.fullName,
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
                        text = "@${user.username} • আইডি খোলার তারিখ: ${user.accountCreationDate}",
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
                    if (user.isVerified) {
                        FidaVerifiedBadge(size = 15.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isVerified) "Fida Verified সক্রিয় ✅" else "ভেরিফাইড নেই",
                        fontSize = 11.sp,
                        color = if (user.isVerified) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (user.isVerified) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!isMe && user.isAccountActive) {
                        Surface(
                            onClick = onQuickSuspend,
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Red.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Block, contentDescription = "অপরাধে সাসপেন্ড", tint = Color.Red, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("সাসপেন্ড 🚫", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }

                    // Grant/Revoke Free Verified Button
                    Button(
                        onClick = onGrantFreeVerified,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isVerified) MaterialTheme.colorScheme.surfaceVariant else BangladeshGreen
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
                            FidaVerifiedBadge(size = 14.dp)
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
                    Text(
                        text = "চ্যাট শুরুর তারিখ: ${friendUser.accountCreationDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
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
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
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
                                    text = "অ্যাডমিন নিয়োগের তারিখ: ${slot.assignedDate}",
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
                            Text(
                                text = "চালু রাখার তারিখ: ০১/০১/২০২৬ (1/1/2026)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
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

@Composable
fun AdminBoostManagerView(
    boostTransactions: List<BoostTransaction>,
    adminBkash: String,
    adminNagad: String,
    adminRocket: String,
    onEditNumbersClick: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    val totalRevenue = boostTransactions.sumOf { it.amountBdt }
    val activeCount = boostTransactions.count { it.status == "ACTIVE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Revenue & Campaign Stats Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💰", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("মোট বুস্ট ও বিজ্ঞাপন আয়", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳$totalRevenue BDT", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BangladeshGreen)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BangladeshGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "লাইভ পেমেন্ট",
                                color = BangladeshGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট ক্যাম্পেইন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${boostTransactions.size} টি", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সক্রিয় বুস্ট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$activeCount টি", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BangladeshGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মালিকানা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("🌸 Sohag", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                        }
                    }
                }
            }
        }

        // Admin Receiving Payment Numbers Card with Real Logos
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "টাকা গ্রহণের নম্বর (Admin Numbers)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ব্যবহারকারীরা এই নম্বরে টাকা পাঠাবে",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = onEditNumbersClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নম্বর এডিট", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // bKash Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE2136E).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFE2136E).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_bkash_logo),
                            contentDescription = "bKash Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("বিকাশ (Personal / Send Money)", fontSize = 11.sp, color = Color(0xFFE2136E), fontWeight = FontWeight.Bold)
                            Text(adminBkash, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE2136E)) {
                            Text("সক্রিয়", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nagad Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF7941D).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFF7941D).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nagad_logo),
                            contentDescription = "Nagad Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("নগদ (Personal / Send Money)", fontSize = 11.sp, color = Color(0xFFF7941D), fontWeight = FontWeight.Bold)
                            Text(adminNagad, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF7941D)) {
                            Text("সক্রিয়", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rocket Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8C3494).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFF8C3494).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_rocket_logo),
                            contentDescription = "Rocket Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("রকেট (Personal / Send Money)", fontSize = 11.sp, color = Color(0xFF8C3494), fontWeight = FontWeight.Bold)
                            Text(adminRocket, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF8C3494)) {
                            Text("সক্রিয়", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Active Boost Packages Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("বর্তমান বুস্ট প্যাকেজসমূহ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• 👁️ ১০,০০০ ভিউ স্পেশাল: ৳২০০ (২ দিন • দ্রুত ভিডিও ও পোস্ট ভিউ)", fontSize = 12.sp)
                    Text("• 🚀 বেসিক বুস্ট: ৳৩০০ (৩ দিন • ১৫,০০০ - ২০,০০০ মানুষের ফিডে বিজ্ঞাপন)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BangladeshGreen)
                    Text("• 🔥 স্ট্যান্ডার্ড বুস্ট: ৳৫০০ (৭ দিন • ৫০,০০০ - ৮০,০০০ মানুষ)", fontSize = 12.sp)
                    Text("• 👑 প্রিমিয়াম ভিআইপি: ৳১,০০০ (১৫ দিন • ১,২০,০০০+ মানুষ)", fontSize = 12.sp)
                    Text("• 💎 মেগা প্রোমোশন: ৳২,০০০ (৩০ দিন • ৩,০০,০০০+ মানুষ)", fontSize = 12.sp)
                }
            }
        }

        // Header for Campaign List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সকল বুস্ট ও প্রচার অর্ডার (${boostTransactions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Right Manager নিয়ন্ত্রিত 🌸",
                    fontSize = 11.sp,
                    color = FacebookBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (boostTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚀", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("এখনো কোনো বুস্ট অর্ডার নেই", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("ব্যবহারকারীরা পোস্ট বুস্ট করলে এখানে TrxID ও পেমেন্ট দেখা যাবে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(boostTransactions, key = { it.id }) { tx ->
                AdminBoostTransactionCard(
                    tx = tx,
                    onUpdateStatus = { newStatus -> onUpdateStatus(tx.id, newStatus) },
                    onDelete = { onDeleteTransaction(tx.id) }
                )
            }
        }
    }
}

@Composable
fun AdminBoostTransactionCard(
    tx: BoostTransaction,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val methodColor = when (tx.paymentMethod) {
        "bKash" -> Color(0xFFE2136E)
        "Nagad" -> Color(0xFFF7941D)
        "Rocket" -> Color(0xFF8C3494)
        else -> BangladeshGreen
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(
                            id = when (tx.paymentMethod) {
                                "Nagad" -> R.drawable.img_nagad_logo
                                "Rocket" -> R.drawable.img_rocket_logo
                                else -> R.drawable.img_bkash_logo
                            }
                        ),
                        contentDescription = tx.paymentMethod,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tx.paymentMethod,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = methodColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (tx.status == "ACTIVE") BangladeshGreen else if (tx.status == "COMPLETED") FacebookBlue else Color(0xFFE65100)
                ) {
                    Text(
                        text = if (tx.status == "ACTIVE") "সক্রিয় বুস্ট ✓" else if (tx.status == "COMPLETED") "সম্পন্ন" else "পেন্ডিং",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tx.postTitleOrText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ফি: ৳${tx.amountBdt} BDT", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = BangladeshGreen)
                Text("মেয়াদ: ${tx.durationDays} দিন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("রিচ: ${tx.estimatedReach}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("প্রেরক: ${tx.senderNumber}", fontSize = 11.sp)
                    Text("TrxID: ${tx.trxId}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("তারিখ: ${tx.createdAt}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tx.status != "ACTIVE") {
                    TextButton(
                        onClick = { onUpdateStatus("ACTIVE") },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("সক্রিয় করুন", fontSize = 11.sp, color = BangladeshGreen)
                    }
                }
                if (tx.status != "COMPLETED") {
                    TextButton(
                        onClick = { onUpdateStatus("COMPLETED") },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("সম্পন্ন করুন", fontSize = 11.sp, color = FacebookBlue)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AdminFirebaseCloudView(
    isCloudConnected: Boolean,
    cloudMessage: String,
    cloudinaryStatus: com.example.data.CloudinaryStatus,
    agoraCallState: com.example.data.AgoraRtcState,
    isBlazeCardEnabled: Boolean,
    allUsersCount: Int,
    allPostsCount: Int,
    allConversationsCount: Int,
    boostCount: Int,
    onToggleCloudinary: (Boolean) -> Unit,
    onToggleBlazeCard: (Boolean) -> Unit,
    onRefreshConnection: () -> Unit,
    onTriggerSync: () -> Unit
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val serviceQuota by com.example.data.ServiceQuotaTracker.snapshot.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Cloud Connection Status Banner + Firebase Daily Free Read/Write Quota Meter
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isCloudConnected) Color(0xFF1B5E20) else Color(0xFFE65100),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Firebase ক্লাউড ডাটাবেজ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = if (isCloudConnected) "অনলাইন" else "লোকাল",
                                        color = if (isCloudConnected) Color(0xFF1B5E20) else Color(0xFFE65100),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCloudConnected)
                                    "গুগল ফায়ারবেস ক্লাউড সংযুক্ত • ৩০-পোস্ট পেজিনেশন ও রিয়েল-টাইম সিঙ্ক সক্রিয়"
                                else
                                    cloudMessage,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Firebase Daily Read/Write Limit Used vs Remaining Box
                    val readFraction = (serviceQuota.firebaseReadsUsed.toDouble() / serviceQuota.firebaseReadLimit.toDouble())
                        .toFloat().coerceIn(0f, 1f)
                    val writeFraction = (serviceQuota.firebaseWritesUsed.toDouble() / serviceQuota.firebaseWriteLimit.toDouble())
                        .toFloat().coerceIn(0f, 1f)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 আজকের লিমিট (${if (isBlazeCardEnabled) "Blaze" else "ফ্রি ৫০K Read / ২০K Write"})",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "মোট শেষ: ${serviceQuota.firebaseReadsUsed + serviceQuota.firebaseWritesUsed}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Reads Used vs Remaining
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Read শেষ: ${serviceQuota.firebaseReadsUsed} টি",
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    text = "বাকি: ${serviceQuota.firebaseReadsRemaining} টি",
                                    color = Color(0xFFB9F6CA),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { readFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFB9F6CA),
                                trackColor = Color.White.copy(alpha = 0.22f)
                            )

                            // Writes Used vs Remaining
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Write শেষ: ${serviceQuota.firebaseWritesUsed} টি",
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    text = "বাকি: ${serviceQuota.firebaseWritesRemaining} টি",
                                    color = Color(0xFFB9F6CA),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { writeFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFFFE082),
                                trackColor = Color.White.copy(alpha = 0.22f)
                            )
                        }
                    }
                }
            }
        }

        // Cloudinary Free Media Cloud Card (No Credit Card, 25 Credits/GB Free Monthly)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F4FD),
                border = BorderStroke(1.2.dp, Color(0xFF1976D2)),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1976D2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Cloudinary",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Cloudinary ফ্রি মিডিয়া ক্লাউড",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0D47A1)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            !cloudinaryStatus.isEnabled -> Color(0xFF757575)
                                            cloudinaryStatus.isConfigured -> BangladeshGreen
                                            else -> Color(0xFFE65100)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                !cloudinaryStatus.isEnabled -> "বন্ধ"
                                                cloudinaryStatus.isConfigured -> "চালু ✓"
                                                else -> "Secrets বাকি"
                                            },
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "২৫ GB ফ্রি প্রতি মাসে • কার্ড ছাড়াই ছবি/ভিডিও/বয়েজ আপলোড",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Switch(
                            checked = cloudinaryStatus.isEnabled,
                            onCheckedChange = onToggleCloudinary
                        )
                    }

                    Text(
                        text = if (cloudinaryStatus.isConfigured)
                            "✅ সংযুক্ত: ${cloudinaryStatus.cloudName} (Preset: ${cloudinaryStatus.uploadPreset}) • এই সেশনে আপলোড: ${cloudinaryStatus.uploadedCount}টি ফাইল"
                        else
                            "চালু করতে Cloudinary (Unsigned Preset) খুলে AI Studio Secrets এবং GitHub-এ APK বানালে GitHub Repo -> Settings -> Secrets -> Actions-এ CLOUDINARY_CLOUD_NAME ও CLOUDINARY_UPLOAD_PRESET যোগ করুন।",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF0D47A1)
                    )

                    if (!cloudinaryStatus.lastError.isNullOrBlank()) {
                        Text(
                            text = "সর্বশেষ তথ্য: ${cloudinaryStatus.lastError}",
                            fontSize = 10.5.sp,
                            color = BangladeshRed
                        )
                    }

                    // Cloudinary Monthly Limit Used vs Remaining Box (25 GB / Month)
                    val cloudFraction = (serviceQuota.cloudinaryBytesUsed.toDouble() / serviceQuota.cloudinaryMonthlyLimitBytes.toDouble())
                        .toFloat().coerceIn(0f, 1f)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.78f),
                        border = BorderStroke(0.8.dp, Color(0xFF90CAF9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📦 মাসিক লিমিট: ২৫.০০ GB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BangladeshGreen.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "বাকি: ${com.example.data.ServiceQuotaTracker.formatBytesCompact(serviceQuota.cloudinaryBytesRemaining)}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BangladeshGreen,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "শেষ হয়েছে: ${com.example.data.ServiceQuotaTracker.formatBytesCompact(serviceQuota.cloudinaryBytesUsed)} (${String.format(java.util.Locale.US, "%.2f", serviceQuota.cloudinaryUsedPercent)}%)",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF1565C0)
                                )
                                Text(
                                    text = "মোট ফাইল: ${serviceQuota.cloudinaryFilesUploadedMonth}টি",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { cloudFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF1976D2),
                                trackColor = Color(0xFFBBDEFB)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri(com.example.data.CLOUDINARY_FREE_SIGNUP_URL)
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Cloudinary ফ্রি অ্যাকাউন্ট ↗",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        OutlinedButton(
                            onClick = onRefreshConnection,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D47A1)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("চেক", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Agora Live Audio & Video Call Status Card (10,000 Free Minutes/Month, No Card)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEDE7F6),
                border = BorderStroke(1.2.dp, Color(0xFF6A1B9A)),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6A1B9A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = "Agora Calls",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Agora HD অডিও ও ভিডিও কল",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4A148C)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (agoraCallState.isConfigured) BangladeshGreen else Color(0xFFE65100)
                                    ) {
                                        Text(
                                            text = if (agoraCallState.isConfigured) "চালু ✓" else "Secrets বাকি",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "প্রতি মাসে ১০,০০০ মিনিট ফ্রি (কার্ড ছাড়াই) • ${agoraCallState.statusText}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6A1B9A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = "ChatFilm ১-টু-১ অডিও/ভিডিও কল, গ্রুপ কল, বন্ধুদের আড্ডা ভয়েস রুম এবং লুডু কিং লাইভ ভয়েসে Agora SDK যুক্ত করা হয়েছে। ফ্রি ব্যবহার করতে AI Studio Secrets (এবং GitHub-এ APK বানালে GitHub Actions Secrets)-এ AGORA_APP_ID দিন।",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF4A148C)
                    )

                    // Agora Monthly Free Minutes Used vs Remaining Box (10,000 mins / month)
                    val agoraFraction = (serviceQuota.agoraSecondsUsed.toDouble() / serviceQuota.agoraMonthlyLimitSeconds.toDouble())
                        .toFloat().coerceIn(0f, 1f)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.78f),
                        border = BorderStroke(0.8.dp, Color(0xFFCE93D8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏱️ মাসিক লিমিট: ১০,০০০ মিনিট",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF6A1B9A).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "বাকি: ${com.example.data.ServiceQuotaTracker.formatMinutesCompact(serviceQuota.agoraSecondsRemaining)}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A148C),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "শেষ হয়েছে: ${com.example.data.ServiceQuotaTracker.formatMinutesCompact(serviceQuota.agoraSecondsUsed)} (${String.format(java.util.Locale.US, "%.2f", serviceQuota.agoraUsedPercent)}%)",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF6A1B9A)
                                )
                                Text(
                                    text = "মোট কল: ${serviceQuota.agoraCallsCountMonth} বার",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4A148C)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { agoraFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF6A1B9A),
                                trackColor = Color(0xFFE1BEE7)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri(com.example.data.AGORA_FREE_SIGNUP_URL)
                            } catch (_: Exception) { }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Agora ফ্রি অ্যাকাউন্ট (১০,০০০ মিনিট/মাস) খুলুন ↗",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Bank Card & Firebase Blaze Billing Manager Card (Right alongside Cloudinary & Agora)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isBlazeCardEnabled) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                border = BorderStroke(
                    1.2.dp,
                    if (isBlazeCardEnabled) BangladeshGreen else Color(0xFFFFB300)
                ),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isBlazeCardEnabled) BangladeshGreen else Color(0xFFE65100)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = "ব্যাংক কার্ড ও Blaze বিলিং",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ব্যাংক কার্ড ও Blaze বিলিং",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isBlazeCardEnabled) Color(0xFF1B5E20) else Color(0xFF4E342E)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isBlazeCardEnabled) BangladeshGreen else Color(0xFFE65100)
                                    ) {
                                        Text(
                                            text = if (isBlazeCardEnabled) "কার্ড / Blaze চালু ✓" else "ফ্রি (কার্ড ছাড়া)",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isBlazeCardEnabled)
                                        "কার্ড ও Blaze পে-অ্যাজ-ইউ-গো সক্রিয় মোড চিহ্নিত ✓"
                                    else
                                        "কার্ড হাতে এলে এখান থেকেই Firebase, Google, Cloudinary ও Agora-তে কার্ড যুক্ত ও চালু করুন",
                                    fontSize = 11.sp,
                                    color = if (isBlazeCardEnabled) Color(0xFF2E7D32) else Color(0xFF6D4C41),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Switch(
                            checked = isBlazeCardEnabled,
                            onCheckedChange = onToggleBlazeCard,
                            modifier = Modifier.testTag("blaze_card_mode_switch")
                        )
                    }

                    Text(
                        text = "ভবিষ্যতে ডুয়াল কারেন্সি Visa/Mastercard যুক্ত করলে Firebase Spark (৫০K Read/দিন) থেকে Blaze (লক্ষ ব্যবহারকারী), Cloudinary প্লাস এবং Agora প্রো আনলিমিটেড চালু করতে পারবেন। নিরাপত্তার জন্য কার্ড নম্বর সরাসরি অফিশিয়াল গেটওয়েতেই দিতে হয়:",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF4E342E)
                    )

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri(com.example.data.FIREBASE_BLAZE_UPGRADE_URL)
                            } catch (_: Exception) { }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_firebase_blaze_billing_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "💳 Firebase Blaze-এ কার্ড যোগ ও চালু করুন ↗",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri("https://console.cloud.google.com/billing")
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_gcloud_billing_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text("Google বিলিং ↗", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri("https://console.cloudinary.com/settings/billing")
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_cloudinary_billing_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text("Cloudinary কার্ড ↗", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri("https://console.agora.io/billing/overview")
                                } catch (_: Exception) { }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_agora_billing_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text("Agora কার্ড ↗", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Firebase Cloud Services Status Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ক্লাউড সার্ভিসেস ও স্কেল স্ট্যাটাস (Play Store Ready)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    CloudServiceRow(
                        icon = "🔥",
                        name = "৩০-পোস্ট পেজিনেশন + ১০০MB অফলাইন ক্যাশ",
                        detail = "কোটা সাশ্রয়ী কার্সর পেজিনেশন (limit 30 + startAfter)",
                        status = "চালু ✓"
                    )

                    CloudServiceRow(
                        icon = "❤️",
                        name = "রিয়্যাকশন, শেয়ার ও কমেন্ট রিপ্লাই ক্লাউড সিঙ্ক",
                        detail = "সকল ফোনে একই লাইক, ১০টি রিয়্যাকশন ও রিপ্লাই সিঙ্ক",
                        status = "সক্রিয় ✓"
                    )

                    CloudServiceRow(
                        icon = "🔐",
                        name = "Google Sign-In + Zero-Trust রুলস",
                        detail = "CredentialManager Auth + কঠোর request.auth != null রুলস",
                        status = "সুরক্ষিত ✓"
                    )

                    CloudServiceRow(
                        icon = "💬",
                        name = "ChatFilm মেসেজ আইসোলেশন",
                        detail = "participantIds ভিত্তিক প্রাইভেসি ও ৫০-মেসেজ পেজিনেশন",
                        status = "সক্রিয় ✓"
                    )
                }
            }
        }

        // Database Collections Stats Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ডাটাবেজ কালেকশন পরিসংখ্যান",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ডাটাবেজ আইডি: ai-studio-android-facefeed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionStatBox(title = "ইউজার আইডি", count = "$allUsersCount", icon = "👥", modifier = Modifier.weight(1f))
                        CollectionStatBox(title = "পোস্ট ও ভিডিও", count = "$allPostsCount", icon = "📝", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionStatBox(title = "মেসেঞ্জার চ্যাট", count = "$allConversationsCount", icon = "💬", modifier = Modifier.weight(1f))
                        CollectionStatBox(title = "বুস্ট ক্যাম্পেইন", count = "$boostCount", icon = "🚀", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Actions: Refresh & Trigger Sync
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRefreshConnection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কানেকশন চেক")
                }

                FilledTonalButton(
                    onClick = onTriggerSync,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডাটা সিঙ্ক করুন")
                }
            }
        }

        // Security Notice
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Firebase ক্লাউড ডাটাবেজ এককভাবে প্রধান মালিক Niyon Islam Sohag-এর তত্ত্বাবধানে Zero-Trust রুলস দ্বারা সুরক্ষিত।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CloudServiceRow(icon: String, name: String, detail: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(status, color = BangladeshGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
fun CollectionStatBox(title: String, count: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(count, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = FacebookBlue)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==============================================================================
// 7. ফিদা ভেরিফাইড রিভিউ ও ফ্রী কোটা ম্যানেজার (Fida Verified Badge Manager & Review)
// ==============================================================================
@Composable
fun AdminFidaVerifiedReviewView(
    verifiedApplications: List<com.example.model.FidaVerifiedApplication>,
    allUsers: List<User>,
    freeQuotaRemaining: Int,
    onApproveApplication: (String) -> Unit,
    onRejectApplication: (String) -> Unit,
    onRevokeFreeVerified: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: ফ্রী ব্যাজ প্রাপ্তদের তালিকা, 1: পেইড আবেদন ও রিভিউ
    val verifiedUsers = remember(allUsers) { allUsers.filter { it.isVerified } }
    val pendingApplications = remember(verifiedApplications) { verifiedApplications.filter { it.status == "PENDING" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Summary Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0D47A1).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0D47A1).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏅 ফিদা ভেরিফাইড মাস্টার কন্ট্রোল",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0D47A1)
                    )
                    Text(
                        text = "ফ্রী কোটা অবশিষ্ট: $freeQuotaRemaining / ৫০,০০০ (50k) • মোট ভেরিফাইড: ${verifiedUsers.size} জন",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = Color(0xFF0D47A1),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "রিভিউ: ${pendingApplications.size} টি",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-Tab Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { subTab = 0 },
                shape = RoundedCornerShape(10.dp),
                color = if (subTab == 0) FacebookBlue else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (subTab == 0) FacebookBlue else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "১. ফ্রী ব্যাজ প্রাপ্ত (${verifiedUsers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                onClick = { subTab = 1 },
                shape = RoundedCornerShape(10.dp),
                color = if (subTab == 1) Color(0xFFE65100) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (subTab == 1) Color(0xFFE65100) else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "২. পেইড আবেদন ও রিভিউ (${verifiedApplications.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subTab == 1) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (subTab == 0) {
            // ১. ফ্রীতে দেওয়া ব্যাজ প্রাপ্ত ইউজার তালিকা (সামনে বাতিল অপশনসহ)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(verifiedUsers, key = { it.id }) { user ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User profile picture
                            FotoFilmAvatar(
                                imageUrl = user.avatarUrl,
                                name = user.fullName,
                                contentDescription = user.fullName,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name, date, and badge info
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FidaVerifiedBadge(size = 14.dp)
                                }
                                Text(
                                    text = "@${user.username} • ফ্রী ব্যাজ অনুমোদনের তারিখ: ${user.accountCreationDate}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BangladeshGreen.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "৫০,০০০ ফ্রী কোটা থেকে প্রদানকৃত ✓",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BangladeshGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // বাতিল বাটন (Revoke Free Verified Badge)
                            Button(
                                onClick = { onRevokeFreeVerified(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("বাতিল", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // ২. পেইড আবেদন ও রিভিউ তালিকা (টাকা জমা, তারিখ, দিন/মাস/বছর ও কনফার্ম বাটনসহ)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(verifiedApplications, key = { it.id }) { app ->
                    val methodColor = when (app.paymentMethod) {
                        "Nagad" -> Color(0xFFF7941D)
                        "Rocket" -> Color(0xFF8C3494)
                        else -> Color(0xFFE2136E) // bKash
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (app.status == "PENDING") Color(0xFFE65100).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = app.userAvatar,
                                        contentDescription = app.userName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(app.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("ফোন: ${app.userPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (app.status) {
                                        "APPROVED" -> BangladeshGreen
                                        "REJECTED" -> Color.Red
                                        else -> Color(0xFFE65100)
                                    }
                                ) {
                                    Text(
                                        text = when (app.status) {
                                            "APPROVED" -> "অনুমোদিত ✓"
                                            "REJECTED" -> "বাতিলকৃত"
                                            else -> "রিভিউ অপেক্ষমাণ ⏳"
                                        },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Payment & Submission Details with Date, Month, Year
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("জমা দেওয়া ফি: ৳${app.amountBdt} BDT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BangladeshGreen)
                                        Text(app.paymentMethod, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = methodColor)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("প্রেরক নম্বর: ${app.senderNumber} • TrxID: ${app.trxId}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("টাকা জমা ও আবেদনের তারিখ: ${app.submittedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                    Text("যাচাইকরণ নথি: ${app.nidOrDocText}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            // Review Action Buttons
                            if (app.status == "PENDING") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onApproveApplication(app.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("রিভিউ দেখে কনফার্ম করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onRejectApplication(app.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                        modifier = Modifier.weight(0.6f)
                                    ) {
                                        Text("বাতিল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 8. পেজ ম্যানেজার (Pages Manager with ON / OFF Switch & Date)
// ==============================================================================
@Composable
fun AdminPagesManagerView(
    allPages: List<com.example.model.SocialPage>,
    onTogglePage: (String, Boolean) -> Unit
) {
    val activePagesCount = remember(allPages) { allPages.count { it.isActive } }
    val suspendedPagesCount = remember(allPages) { allPages.count { !it.isActive } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Stats Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF00897B).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📄 সকল ফেইসবুক পেজ নিয়ন্ত্রণ ও অডিট",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF00897B)
                    )
                    Text(
                        text = "মোট পেজ: ${allPages.size} টি • সক্রিয় (অন): $activePagesCount টি • অফ: $suspendedPagesCount টি",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = BangladeshGreen,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "অন/অফ কন্ট্রোল",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pages List with ON/OFF switch
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            items(allPages, key = { it.id }) { page ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (page.isActive) MaterialTheme.colorScheme.surface else Color.Red.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (page.isActive) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.4f)
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Page Avatar
                            AsyncImage(
                                model = page.avatarUrl,
                                contentDescription = page.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Page Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = page.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (page.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FidaVerifiedBadge(size = 13.dp)
                                    }
                                }
                                Text(
                                    text = "${page.category} • ${page.followerCount} ফলোয়ার",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "তৈরি করেছেন: ${page.creatorName} • পেইজ খোলার তারিখ: ${page.createdAt}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // ============================================
                            // অন / অফ সুইচ বাটন (Page ON / OFF Switch)
                            // ============================================
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (page.isActive) "অন (সক্রিয়)" else "অফ (বন্ধ)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (page.isActive) BangladeshGreen else Color.Red
                                )
                                Switch(
                                    checked = page.isActive,
                                    onCheckedChange = { isChecked ->
                                        onTogglePage(page.id, isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BangladeshGreen,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.Red
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 9. গ্রুপ ম্যানেজার (Groups Manager with ON / OFF Switch & Date)
// ==============================================================================
@Composable
fun AdminGroupsManagerView(
    allGroups: List<com.example.model.SocialGroup>,
    onToggleGroup: (String, Boolean) -> Unit
) {
    val activeGroupsCount = remember(allGroups) { allGroups.count { it.isActive } }
    val suspendedGroupsCount = remember(allGroups) { allGroups.count { !it.isActive } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Stats Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF5D4037).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5D4037).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👥 সকল FotoFilm গ্রুপ নিয়ন্ত্রণ ও মডারেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = "মোট গ্রুপ: ${allGroups.size} টি • সক্রিয় (অন): $activeGroupsCount টি • অফ: $suspendedGroupsCount টি",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = Color(0xFF5D4037),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "গ্রুপ সুইচ অন/অফ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Groups List with ON/OFF switch
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            items(allGroups, key = { it.id }) { group ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (group.isActive) MaterialTheme.colorScheme.surface else Color.Red.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (group.isActive) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.4f)
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Group Cover/Thumbnail
                            AsyncImage(
                                model = group.coverUrl,
                                contentDescription = group.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Group Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${if (group.isPrivate) "🔒 প্রাইভেট গ্রুপ" else "🌐 পাবলিক গ্রুপ"} • ${group.memberCount} সদস্য",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "অ্যাডমিন: ${group.creatorName} • গ্রুপ খোলার তারিখ: ${group.createdAt}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // ============================================
                            // অন / অফ সুইচ বাটন (Group ON / OFF Switch)
                            // ============================================
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (group.isActive) "অন (সক্রিয়)" else "অফ (বন্ধ)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (group.isActive) BangladeshGreen else Color.Red
                                )
                                Switch(
                                    checked = group.isActive,
                                    onCheckedChange = { isChecked ->
                                        onToggleGroup(group.id, isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BangladeshGreen,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.Red
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 10. পেমেন্ট ও লেনদেন হিস্ট্রি (Master Payment & Date Ledger)
// ==============================================================================
@Composable
fun AdminPaymentLedgerView(
    boostTransactions: List<com.example.model.BoostTransaction>,
    verifiedApplications: List<com.example.model.FidaVerifiedApplication>,
    onConfirmBoost: (String) -> Unit,
    onConfirmVerified: (String) -> Unit
) {
    val totalBoostAmount = boostTransactions.sumOf { it.amountBdt }
    val totalVerifiedAmount = verifiedApplications.sumOf { it.amountBdt }
    val grandTotal = totalBoostAmount + totalVerifiedAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Master Revenue Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF57F17).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57F17).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💰 সর্বমোট জমা ও লেনদেন হিসাব খাতা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFF57F17)
                        )
                        Text(
                            text = "মোট আয়: ৳$grandTotal BDT (বুস্ট: ৳$totalBoostAmount • ভেরিফিকেশন: ৳$totalVerifiedAmount)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = BangladeshGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "১০/১০ মাস্টার হিস্ট্রি ✓",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            // Header for Boost Transactions
            item {
                Text(
                    text = "🚀 বুস্ট ও বিজ্ঞাপন বাবদ জমা লেনদেন:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BangladeshGreen,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            items(boostTransactions, key = { "boost_${it.id}" }) { tx ->
                val methodColor = when (tx.paymentMethod) {
                    "Nagad" -> Color(0xFFF7941D)
                    "Rocket" -> Color(0xFF8C3494)
                    else -> Color(0xFFE2136E)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${tx.authorName} • ফি: ৳${tx.amountBdt} BDT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = methodColor
                            ) {
                                Text(
                                    text = tx.paymentMethod,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "টাকা পাঠানোর তারিখ: ${tx.createdAt} • প্রেরক: ${tx.senderNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "TrxID: ${tx.trxId} • প্যাকেজ: ${tx.goal} (${tx.durationDays} দিন)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Header for Verified Badge Subscriptions
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🏅 ফিদা ভেরিফাইড ব্যাজ বাবদ জমা লেনদেন:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0D47A1),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            items(verifiedApplications, key = { "verified_${it.id}" }) { app ->
                val methodColor = when (app.paymentMethod) {
                    "Nagad" -> Color(0xFFF7941D)
                    "Rocket" -> Color(0xFF8C3494)
                    else -> Color(0xFFE2136E)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${app.userName} • ফি: ৳${app.amountBdt} BDT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = methodColor
                            ) {
                                Text(
                                    text = app.paymentMethod,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "টাকা পাঠানোর তারিখ: ${app.submittedDate} • প্রেরক: ${app.senderNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "TrxID: ${app.trxId} • উদ্দেশ্য: ফিদা ভেরিফাইড সাবস্ক্রিপশন আবেদন",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// ১১তম মাস্টার ফিচার: সুপার ওনার ফ্রী মেগা বুস্ট ও ১ মিলিয়ন+ রিচ প্রোমোটার (Feature 11)
// শুধুমাত্র প্রধান প্রতিষ্ঠাতা ও মালিক Niyon Islam Sohag নিজের যেকোনো পোস্ট ফ্রীতে বুস্ট করতে পারবেন!
// অন্য কোনো ব্যক্তি বা অ্যাডমিন টাকা ছাড়া ফ্রী বুস্ট করতে পারবে না।
// =============================================================================
@Composable
fun AdminOwnerFreeMegaBoostView(
    posts: List<Post>,
    currentUser: User,
    onBoostPost: (postId: String, reach: String) -> Unit
) {
    val isOwner = currentUser.id == "user_me" || currentUser.id == "user_niyon" || currentUser.fullName.contains("Niyon", ignoreCase = true)
    var selectedReachMultiplier by remember { mutableStateOf("১.৫ মিলিয়ন (1.5M Reach) 🚀") }

    val reachOptions = listOf(
        "১.২ মিলিয়ন (1.2M Reach) 🚀",
        "২.৫ মিলিয়ন (2.5M Reach) 🔥",
        "৫.০ মিলিয়ন (5.0M Reach) 🌟",
        "১০.০ মিলিয়ন (10M Mega Viral) 💎"
    )

    val ownerPosts = remember(posts, currentUser) {
        val mine = posts.filter { 
            it.author.id == currentUser.id || it.author.fullName.contains("Niyon", ignoreCase = true) 
        }
        if (mine.isNotEmpty()) mine else posts
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // Feature 11 Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isOwner) Color(0xFFFCE4EC) else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isOwner) Color(0xFFE91E63) else MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isOwner) "👑" else "🔒", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "১১. ওনার ফ্রী মেগা বুস্ট ম্যানেজার",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (isOwner) Color(0xFFAD1457) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isOwner) "Niyon Islam Sohag • শতভাগ ফ্রী মেগা রিচ (৳০ খরচ)" else "লকড: শুধুমাত্র Niyon Islam Sohag-এর জন্য সংরক্ষিত",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isOwner) Color(0xFFC2185B) else Color.Red
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOwner) Color(0xFFE91E63) else Color.Red
                        ) {
                            Text(
                                text = if (isOwner) "১০০% ফ্রী সুবিধা ✓" else "লকড 🚫",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isOwner) {
                            "আপনি আপনার নিজের যেকোনো পোস্ট বা ভিডিও সম্পূর্ণ ফ্রীতে (টাকা ছাড়া) ১ মিলিয়ন থেকে ১০ মিলিয়ন মানুষের মাঝে বুস্ট করতে পারবেন। এই পোস্ট লাখ লাখ মানুষের হোম ফিডে চলে যাবে। আপনি ছাড়া অন্য কোনো ব্যক্তি বা সাধারণ ইউজার টাকা ছাড়া ফ্রীতে বুস্ট করতে পারবে না (তাদের bKash/Nagad পেমেন্ট করতে হবে)।"
                        } else {
                            "⚠️ অ্যাক্সেস সংরক্ষিত: এই মেগা বুস্ট ফিচারটি শুধুমাত্র অ্যাপের প্রধান প্রতিষ্ঠাতা ও মালিক Niyon Islam Sohag-এর ব্যক্তিগত ব্যবহারের জন্য। অন্য কোনো ব্যক্তি বা অ্যাডমিন টাকা ছাড়া ফ্রীতে বুস্ট করতে পারবে না।"
                        },
                        fontSize = 12.sp,
                        color = if (isOwner) Color(0xFF880E4F) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        if (isOwner) {
            // Reach Multiplier Target Selection Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "মিলিয়ন মানুষের রিচ সিলেক্ট করুন (বিনা মূল্যে):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reachOptions.forEach { reach ->
                                val isSelected = selectedReachMultiplier == reach
                                Surface(
                                    onClick = { selectedReachMultiplier = reach },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = reach,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Posts List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আপনার পোস্ট ও ভিডিও তালিকা (${ownerPosts.size}টি)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "যেকোনো পোস্ট ফ্রীতে বুস্ট করুন",
                        fontSize = 11.sp,
                        color = Color(0xFFC2185B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // List of Posts with 1-Click Free Mega Boost
            items(ownerPosts, key = { it.id }) { post ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (post.isBoosted) Color(0xFFE91E63).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (post.images.isNotEmpty()) {
                                    AsyncImage(
                                        model = post.images.first(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(if (post.isVideo) "🎥" else "📝", fontSize = 24.sp)
                                }
                                if (post.isVideo) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = post.title?.takeIf { it.isNotBlank() }
                                        ?: post.videoTitle?.takeIf { it.isNotBlank() }
                                        ?: post.text.take(60).ifBlank { "পোস্ট কন্টেন্ট" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "বর্তমান রিচ: ${post.reachViews}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "তারিখ: ${post.timestamp}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (post.isBoosted) BangladeshGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (post.isBoosted) "🔥 বুস্ট চলছে (${post.boostTargetReach?.ifBlank { "মেগা রিচ" } ?: "মেগা রিচ"})" else "⚪ সাধারণ পোস্ট",
                                    color = if (post.isBoosted) BangladeshGreen else MaterialTheme.colorScheme.outline,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    onBoostPost(post.id, selectedReachMultiplier)
                                    com.example.util.AppSoundManager.playReactionChime()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (post.isBoosted) Color(0xFFC2185B) else Color(0xFFE91E63)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (post.isBoosted) "আবার ফ্রী বুস্ট (১M+)" else "🚀 ফ্রী মেগা বুস্ট (৳০)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Security Policy Note
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛡️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সিকিউরিটি পলিসি: এই মেগা বুস্টের মাধ্যমে পোস্টের রিচ লাখ লাখ মানুষের ডিভাইসে পৌঁছে যায়। শুধুমাত্র প্রধান মালিকের জন্য এটি ফ্রী। অন্য সবার জন্য ৫টি পেইড প্যাকেজ প্রযোজ্য।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// ১২তম ফিচার: খারাপ মন্তব্যকারী ও সাইবার অপরাধী অডিট (আইনগত প্রোফাইল ও অ্যাকশন)
// =========================================================================
@Composable
fun AdminCyberCrimeAuditView(
    offenders: List<CyberOffenderReport>,
    onTakeLegalAction: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val filteredOffenders = remember(offenders, searchQuery, selectedFilter) {
        offenders.filter { offender ->
            val matchesQuery = searchQuery.isBlank() ||
                    offender.userName.contains(searchQuery, ignoreCase = true) ||
                    offender.userUsername.contains(searchQuery, ignoreCase = true) ||
                    offender.nationalId.contains(searchQuery, ignoreCase = true) ||
                    offender.reportedComment.contains(searchQuery, ignoreCase = true) ||
                    offender.ipAddress.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "review" -> offender.status == "UNDER_REVIEW"
                "notice" -> offender.status == "LEGAL_NOTICE_SENT"
                "banned" -> offender.status == "PERMANENT_BANNED"
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Warning Banner
        Surface(
            color = Color(0xFFFFEBEE),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚖️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "১২. সাইবার ক্রাইম ও কুরুচিপূর্ণ মন্তব্যকারী অডিট সেল",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = "মালিক বা অ্যাপ সম্পর্কিত খারাপ মন্তব্যকারীদের আইনগত প্রোফাইল ও আইনি পদক্ষেপ",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${offenders.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC62828))
                            Text("মোট রেকর্ড", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${offenders.count { it.status == "UNDER_REVIEW" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF57F17))
                            Text("তদন্তাধীন", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${offenders.count { it.status == "LEGAL_NOTICE_SENT" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                            Text("নোটিশ জারি", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${offenders.count { it.status == "PERMANENT_BANNED" }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                            Text("স্থায়ী ব্যান", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("নাম, ইউজারনেম, এনআইডি বা কুরুচিপূর্ণ উক্তি খুঁজুন...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("সকল (${offenders.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "review",
                    onClick = { selectedFilter = "review" },
                    label = { Text("তদন্তাধীন (${offenders.count { it.status == "UNDER_REVIEW" }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "notice",
                    onClick = { selectedFilter = "notice" },
                    label = { Text("আইনি নোটিশ জারি (${offenders.count { it.status == "LEGAL_NOTICE_SENT" }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == "banned",
                    onClick = { selectedFilter = "banned" },
                    label = { Text("স্থায়ী ব্যান (${offenders.count { it.status == "PERMANENT_BANNED" }})", fontSize = 11.sp) }
                )
            }
        }

        // Offender Legal Dossier Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredOffenders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛡️", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("কোনো খারাপ মন্তব্যকারী বা অপরাধী রেকর্ড পাওয়া যায়নি", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("সব ব্যবহারকারী নিরাপদ ও নীতি মেনে চলছেন", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            items(filteredOffenders, key = { it.id }) { offender ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        when (offender.status) {
                            "PERMANENT_BANNED" -> Color.Red
                            "LEGAL_NOTICE_SENT" -> Color(0xFFE65100)
                            else -> Color(0xFFC62828).copy(alpha = 0.4f)
                        }
                    ),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User Profile Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AsyncImage(
                                    model = offender.userAvatar,
                                    contentDescription = offender.userName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.Red, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(16.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = offender.userName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color.Red.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "অভিযুক্ত",
                                            color = Color.Red,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "@${offender.userUsername} • আইডি: ${offender.userId}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (offender.status) {
                                    "PERMANENT_BANNED" -> Color.Red
                                    "LEGAL_NOTICE_SENT" -> Color(0xFFE65100)
                                    else -> Color(0xFFF57F17)
                                }
                            ) {
                                Text(
                                    text = when (offender.status) {
                                        "PERMANENT_BANNED" -> "স্থায়ী ব্যান 🛑"
                                        "LEGAL_NOTICE_SENT" -> "আইনি নোটিশ 🚨"
                                        else -> "তদন্তাধীন ⏳"
                                    },
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Offensive Statement Quote Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "💬 কুরুচিপূর্ণ উক্তি / অপরাধের প্রমাণ:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${offender.reportedComment}\"",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📌 পোস্ট: ${offender.targetPostTitle}",
                                        fontSize = 10.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "🕒 ${offender.timestamp}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Legal & Cyber Intelligence Dossier
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📋 সাইবার আইনগত নথি ও আইনি প্রোফাইল (Legal Cyber Dossier)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🆔 জাতীয় পরিচয়পত্র (NID):", fontSize = 11.sp, color = Color.Gray)
                                    Text(offender.nationalId, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("📱 নিবন্ধিত মোবাইল নং:", fontSize = 11.sp, color = Color.Gray)
                                    Text(offender.phoneNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🌐 আইপি এড্রেস:", fontSize = 11.sp, color = Color.Gray)
                                    Text(offender.ipAddress, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("📲 ডিভাইস মডেল:", fontSize = 11.sp, color = Color.Gray)
                                    Text(offender.deviceModel, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("📍 ট্রেসড লোকেশন:", fontSize = 11.sp, color = Color.Gray)
                                    Text(offender.location, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚖️ লঙ্ঘনকৃত ধারা: ${offender.legalViolation}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Send Legal Notice, Ban ID & Device, Export Cyber Report, Pardon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (offender.status != "LEGAL_NOTICE_SENT" && offender.status != "PERMANENT_BANNED") {
                                Button(
                                    onClick = {
                                        onTakeLegalAction(offender.id, "NOTICE")
                                        com.example.util.AppSoundManager.playReactionChime()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Policy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🚨 আইনি নোটিশ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (offender.status != "PERMANENT_BANNED") {
                                Button(
                                    onClick = {
                                        onTakeLegalAction(offender.id, "BAN")
                                        com.example.util.AppSoundManager.playReactionChime()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🛑 স্থায়ী ব্যান", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onTakeLegalAction(offender.id, "PARDON")
                                        com.example.util.AppSoundManager.playReactionChime()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🕊️ ক্ষমা / আনব্যান", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val dossierText = """
                                        সাইবার নিরাপত্তা সেল ডসিয়ার
                                        অভিযুক্তের নাম: ${offender.userName} (@${offender.userUsername})
                                        এনআইডি: ${offender.nationalId}
                                        মোবাইল: ${offender.phoneNumber}
                                        আইপি: ${offender.ipAddress} (${offender.deviceModel})
                                        কুরুচিপূর্ণ উক্তি: ${offender.reportedComment}
                                        আইনগত ধারা: ${offender.legalViolation}
                                    """.trimIndent()
                                    clipboardManager.setText(AnnotatedString(dossierText))
                                    Toast.makeText(context, "📋 সাইবার ডসিয়ার কপি ও পুলিশ রিপোর্ট প্রস্তুত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📋 ডসিয়ার", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// ১৩তম ফিচার: অ্যাপ আপডেট নোটিশ ব্রডকাস্টার (লোগো ও ছবি সহ সবার আইডিতে নোটিফিকেশন)
// =========================================================================
@Composable
fun AdminAppUpdateBroadcasterView(
    currentUser: User,
    broadcasts: List<AppUpdateBroadcast>,
    onBroadcast: (String, String, String) -> Unit
) {
    var versionInput by remember { mutableStateOf("v3.0.0 (Right Manager Fida ১৩ মাস্টার সংস্করণ)") }
    var titleInput by remember { mutableStateOf("FotoFilm নতুন ১৩টি মাস্টার ফিচার ও মেগা বুস্ট আপডেট!") }
    var descInput by remember {
        mutableStateOf(
            "সম্মানিত ব্যবহারকারীগণ, FotoFilm অ্যাপে এখন ওনার ফ্রী মেগা বুস্ট (১M+ রিচ), সাইবার অপরাধী আইনি ট্র্যাকার, নেশা ও জুয়া বিরোধী কড়া সিকিউরিটি এবং অ্যাপ আপডেট ব্রডকাস্টার সিস্টেম সফলভাবে চালু হয়েছে!"
        )
    }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEDE7F6),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1C4E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📢", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "১৩. অ্যাপ আপডেট নোটিশ ব্রডকাস্টার",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF4A148C)
                        )
                        Text(
                            text = "অ্যাপ আপডেট করার পর সবার আইডিতে লোগো ও ছবি সহ নোটিফিকেশন পৌঁছে দিন",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Form Inputs
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "নতুন আপডেট বিবরণ লিখুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FacebookBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = versionInput,
                        onValueChange = { versionInput = it },
                        label = { Text("আপডেট সংস্করণ (Version Number)", fontSize = 11.sp) },
                        placeholder = { Text("যেমন: v3.0.0") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("আপডেটের শিরোনাম (Update Notice Title)", fontSize = 11.sp) },
                        placeholder = { Text("আপডেটের প্রধান বার্তা") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("আপডেটের বিস্তারিত বিবরণ / চেঞ্জলগ", fontSize = 11.sp) },
                        placeholder = { Text("ব্যবহারকারীরা কী কী নতুন সুবিধা পাবেন...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Live Preview of the Notification
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FacebookBlue),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔 নোটিফিকেশন প্রিভিউ (সবার আইডিতে যেভাবে দেখাবে)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = FacebookBlue
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = "লাইভ প্রিভিউ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = FacebookBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // App Logo + Owner Profile Avatar Stack
                        Box(modifier = Modifier.size(50.dp)) {
                            // App Logo as base
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE91E63),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌸", fontSize = 22.sp)
                                }
                            }
                            // Owner Profile Avatar overlay
                            FotoFilmAvatar(
                                imageUrl = currentUser.avatarUrl,
                                name = currentUser.fullName,
                                contentDescription = "Owner Profile Icon",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FotoFilm • ${currentUser.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("👑", fontSize = 10.sp)
                            }
                            Text(
                                text = titleInput.ifBlank { "নতুন অ্যাপ আপডেট প্রকাশিত হয়েছে!" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = FacebookBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = descInput.ifBlank { "নতুন সংস্করণ আপডেট করুন।" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "এইমাত্র • সংস্করণ: ${versionInput.ifBlank { "v3.0.0" }}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Action Broadcast Button
        item {
            Button(
                onClick = {
                    onBroadcast(versionInput, titleInput, descInput)
                    com.example.util.AppSoundManager.playReactionChime()
                    Toast.makeText(context, "📢 সকল ব্যবহারকারীর আইডিতে আপডেট নোটিফিকেশন পৌঁছে গেছে!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📢 সবার আইডিতে আপডেট নোটিফিকেশন পাঠান",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Previous Broadcast History
        item {
            Text(
                text = "ইতিপূর্বে পাঠানো নোটিশের ইতিহাস (${broadcasts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(broadcasts, key = { it.id }) { b ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌸", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = b.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "সংস্করণ: ${b.version} • প্রেরক: ${b.ownerName}", fontSize = 10.sp, color = FacebookBlue)
                        Text(text = "তারিখ: ${b.releaseDate}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Surface(
                        color = BangladeshGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "সক্রিয় ✓",
                            color = BangladeshGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 13 (Option 14): ফ্রী ফিদা ভেরিফাই বিতরণ ইতিহাস
 * "আমি ফ্রীতে কয়েক জনকে ফিদা ভেরিফাই ১ জনকে কতো বার দিছি কোন মাসে দিছি তারিখ সহো থারব"
 */
@Composable
fun AdminFreeVerifiedHistoryView(
    records: List<FreeVerifiedGrantRecord>,
    quotaRemaining: Int,
    onGrantMore: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonthFilter by remember { mutableStateOf("সকল মাস") }

    val monthsList = remember(records) {
        listOf("সকল মাস", "অক্টোবর ২০২৬", "সেপ্টেম্বর ২০২৬", "আগস্ট ২০২৬", "জুলাই ২০২৬", "মে ২০২৬")
    }

    val filteredRecords = remember(records, searchQuery, selectedMonthFilter) {
        records.filter { r ->
            val matchesQuery = searchQuery.isBlank() ||
                    r.recipientName.contains(searchQuery, ignoreCase = true) ||
                    r.recipientUsername.contains(searchQuery, ignoreCase = true)
            val matchesMonth = selectedMonthFilter == "সকল মাস" || r.grantMonth.contains(selectedMonthFilter)
            matchesQuery && matchesMonth
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // Summary & Quota Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00695C).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00695C).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📜", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ফ্রী ফিদা ভেরিফাই বিতরণ ইতিহাস ও লেজার",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF004D40)
                                )
                                Text(
                                    text = "১ জনকে কতো বার ও কোন মাসে দিয়েছেন তারিখ সহ হিসাব",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00695C)
                        ) {
                            Text(
                                text = "কোটা বাকি: $quotaRemaining / ৫০,০০০ (50k)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00695C).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${records.size} জন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00695C))
                                Text(text = "মোট গ্রাহক", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00695C).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${records.sumOf { it.grantCountTimes }} বার", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BangladeshGreen)
                                Text(text = "সর্বমোট বিতরণ সংখ্যা", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00695C).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "১০০% ফ্রী", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FacebookBlue)
                                Text(text = "খরচ/ফি", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Search & Month Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম বা ইউজারনেম দিয়ে খুঁজুন...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Month filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    monthsList.forEach { m ->
                        val isSelected = selectedMonthFilter == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMonthFilter = m },
                            label = { Text(m, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // Records List
        items(filteredRecords, key = { it.id }) { record ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with Verified Badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = record.recipientAvatar,
                                contentDescription = record.recipientName,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            FidaVerifiedBadge(size = 16.dp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = record.recipientName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "@${record.recipientUsername}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // কতো বার দিছি - Distinct Badge
                        Surface(
                            color = Color(0xFF00695C).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00695C))
                        ) {
                            Text(
                                text = "🔥 মোট ${record.grantCountTimes} বার দিছি",
                                color = Color(0xFF004D40),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // কোন মাসে দিছি এবং তারিখ সহ
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "কোন কোন মাসে দেওয়া হয়েছে: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = record.grantMonth,
                            fontSize = 11.sp,
                            color = FacebookBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সর্বশেষ তারিখ ও সময়: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = record.grantDateFormatted,
                            fontSize = 11.sp,
                            color = BangladeshGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Detailed instances breakdown
                    if (record.historyInstances.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "প্রতিটি বারের তারিখ ও মাস হিস্ট্রি:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                record.historyInstances.forEach { detail ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${detail.instanceNumber}ম বার: ${detail.date} (${detail.month}, ${detail.time})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "৫০,০০০ কোটা ✓",
                                            fontSize = 9.sp,
                                            color = BangladeshGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = { onGrantMore(record.recipientUserId) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("পুনরায় ফ্রী ভেরিফাই দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 14 (Option 15): আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (তারিখ ও প্রোফাইল আইকন সহ)
 */
@Composable
fun AdminUserVerificationAuditView(
    auditEntries: List<UserVerificationAuditEntry>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredEntries = remember(auditEntries, searchQuery) {
        auditEntries.filter { e ->
            searchQuery.isBlank() ||
                    e.userName.contains(searchQuery, ignoreCase = true) ||
                    e.userUsername.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF283593).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF283593).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎖️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ভেরিফিকেশন গ্রহণকারী অডিট লেজার",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A237E)
                                )
                                Text(
                                    text = "আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (তারিখ ও প্রোফাইল আইকন সহ)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF283593)
                        ) {
                            Text(
                                text = "মোট রেকর্ড: ${auditEntries.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283593).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${auditEntries.size} জন", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF283593))
                                Text(text = "অনুমোদিত ইউজার", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283593).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${auditEntries.sumOf { it.totalTimesVerifiedTaken }} বার", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE91E63))
                                Text(text = "মোট ভেরিফাই নেওয়া হয়েছে", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("গ্রাহকের নাম বা ইউজারনেম লিখুন...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Entries List
        items(filteredEntries, key = { it.id }) { entry ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with Verified badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = entry.userAvatar,
                                contentDescription = entry.userName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            FidaVerifiedBadge(size = 18.dp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "@${entry.userUsername} • আইডি: ${entry.userId}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.currentBadgeStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FacebookBlue
                            )
                        }

                        // ১ জনে কতো বার নিছে কাউন্টার
                        Surface(
                            color = Color(0xFF283593).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF283593))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${entry.totalTimesVerifiedTaken} বার",
                                    color = Color(0xFF1A237E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "ভেরিফাই নিছে",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF283593)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📅 ভেরিফিকেশন গ্রহণের পূর্ণ তারিখ ও মাস ভিত্তিক ইতিহাস:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // List of all dates when taken
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        entry.historyInstances.forEach { instance ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⭐", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "${instance.instanceNumber}ম বার: ${instance.date} (${instance.month})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "অনুমোদনকারী: ${instance.source} • সময়: ${instance.time}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Surface(
                                        color = BangladeshGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = instance.method,
                                            color = BangladeshGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 15: OPTION 16: সার্চ ও নির্দিষ্ট মেয়াদে আইডি সাসপেন্ড কন্ট্রোল
// (১০টি মেয়াদ অপশন, অন/অফ বাটন, কেনছেল, প্রোফাইল আইকন ও তারিখ সহ)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTimedSuspensionManagerView(
    allUsers: List<User>,
    suspensionRecords: List<UserSuspensionRecord>,
    onSuspendUser: (userId: String, durationIndex: Int, durationText: String, reason: String) -> Unit,
    onToggleActive: (recordId: String) -> Unit,
    onCancelSuspension: (recordId: String) -> Unit
) {
    var userSearchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedDurationIndex by remember { mutableIntStateOf(1) } // 1 to 10
    var customReasonNote by remember { mutableStateOf("") }
    var recordSearchQuery by remember { mutableStateOf("") }
    var recordToCancel by remember { mutableStateOf<UserSuspensionRecord?>(null) }
    var activeFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, INACTIVE

    // ১০টি নির্দিষ্ট মেয়াদ অপশন (ইউজার রিকোয়ারমেন্ট অনুযায়ী নম্বর সহ)
    val durationOptions = remember {
        listOf(
            1 to "1. 3 দিনের",
            2 to "2. 7 দিনের",
            3 to "3. 14 দিনের",
            4 to "4. 21 দিনের",
            5 to "5. 1মাসের",
            6 to "6. 2 মাসের",
            7 to "7. 3 মাসের",
            8 to "8. 1 বছোর",
            9 to "9. 2 বছোর",
            10 to "10. 3 বছোর"
        )
    }

    val searchedUsers = remember(allUsers, userSearchQuery) {
        if (userSearchQuery.isBlank()) {
            emptyList()
        } else {
            allUsers.filter {
                it.fullName.contains(userSearchQuery, ignoreCase = true) ||
                it.username.contains(userSearchQuery, ignoreCase = true)
            }
        }
    }

    val filteredRecords = remember(suspensionRecords, recordSearchQuery, activeFilter) {
        suspensionRecords.filter { record ->
            val matchesSearch = recordSearchQuery.isBlank() ||
                record.userName.contains(recordSearchQuery, ignoreCase = true) ||
                record.userUsername.contains(recordSearchQuery, ignoreCase = true) ||
                record.reasonNote.contains(recordSearchQuery, ignoreCase = true)
            val matchesFilter = when (activeFilter) {
                "ACTIVE" -> record.isSuspendedActive
                "INACTIVE" -> !record.isSuspendedActive
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // =====================================================================
        // ১. হেডার কার্ড ও সারাংশ পরিসংখ্যান
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFD32F2F).copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD32F2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "১৬. সার্চ ও নির্দিষ্ট মেয়াদে আইডি সাসপেন্ড",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = "প্রধান প্রতিষ্ঠাতা Niyon Islam Sohag-এর ব্যক্তিগত একক নিয়ন্ত্রণ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${suspensionRecords.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD32F2F))
                            Text(text = "মোট সাসপেন্ড রেকর্ড", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Red.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${suspensionRecords.count { it.isSuspendedActive }}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red)
                            Text(text = "🔴 সক্রিয় (অন)", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BangladeshGreen.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${suspensionRecords.count { !it.isSuspendedActive }}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BangladeshGreen)
                            Text(text = "🟢 মুক্ত (অফ)", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================================================
        // ২. ব্যবহারকারী সার্চ ও নতুন সাসপেন্ড ফরম (Search & Suspend Panel)
        // =====================================================================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ব্যবহারকারীর আইডিতে সার্চ ও সাসপেন্ড করুন 🔍",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("কারো নাম বা @username লিখে সার্চ করুন...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = FacebookBlue)
                    },
                    trailingIcon = {
                        if (userSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { userSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Search Results Dropdown / Picker List
                if (searchedUsers.isNotEmpty() && selectedUser == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "সার্চ ফলাফলে ${searchedUsers.size} জন পাওয়া গেছে (সাসপেন্ড করতে ক্লিক করুন):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FacebookBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        searchedUsers.take(6).forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUser = user
                                        userSearchQuery = ""
                                    }
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.fullName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.LightGray, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "@${user.username}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Surface(
                                    color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "নির্বাচন করুন ➡️",
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }

                // If a user is selected, show configuration form for suspension
                selectedUser?.let { user ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFD32F2F).copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // User Info Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.fullName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color(0xFFD32F2F), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${user.username} • আইডি: ${user.id}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(
                                    onClick = { selectedUser = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Deselect", tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color(0xFFD32F2F).copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // লেখার অপশন (Custom writing / reason note field)
                            Text(
                                text = "✍️ লেখার অপশন (সাসপেন্ডের কারণ / অপরাধ বিবরণ):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customReasonNote,
                                onValueChange = { customReasonNote = it },
                                placeholder = { Text("যেমন: আপত্তিকর মন্তব্য, স্প্যামিং বা আচরণবিধি লঙ্ঘন...") },
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // ১০টি নির্দিষ্ট মেয়াদ অপশন (Numbered 1 to 10)
                            Text(
                                text = "⏳ সাসপেন্ডের নির্দিষ্ট মেয়াদ নির্বাচন করুন (১০টি অপশন):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // 10 duration choices in 2 columns
                            durationOptions.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { (index, label) ->
                                        val isSelected = selectedDurationIndex == index
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFFD32F2F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedDurationIndex = index }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedDurationIndex = index },
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F)),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Suspend Now (with On button active) & Cancel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedUser = null
                                        customReasonNote = ""
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("বাতিল")
                                }

                                Button(
                                    onClick = {
                                        val chosenDuration = durationOptions.find { it.first == selectedDurationIndex }?.second ?: "1. 3 দিনের"
                                        onSuspendUser(
                                            user.id,
                                            selectedDurationIndex,
                                            chosenDuration,
                                            customReasonNote.ifBlank { "নিয়ম লঙ্ঘন ও আপত্তিকর কার্যকলাপ" }
                                        )
                                        selectedUser = null
                                        customReasonNote = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(2f)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("সাসপেন্ড চালু করুন 🚫 (অন)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================================
        // ৩. সাসপেন্ডেড আইডি তালিকা ও নিয়ন্ত্রণ (অন/অফ, কেনছেল, প্রোফাইল ও তারিখ)
        // =====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "সাসপেন্ডেড আইডি তালিকা ও নিয়ন্ত্রণ",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "অন/অফ বাটন ও কেনছেল করার অপশন সহ (${filteredRecords.size} টি রেকর্ড)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filters: All, Active (On), Inactive (Off)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "সকল (${suspensionRecords.size})",
                "ACTIVE" to "🔴 অন / সক্রিয় (${suspensionRecords.count { it.isSuspendedActive }})",
                "INACTIVE" to "🟢 অফ / নিষ্ক্রিয় (${suspensionRecords.count { !it.isSuspendedActive }})"
            ).forEach { (key, label) ->
                val isSelected = activeFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { activeFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search within records
        OutlinedTextField(
            value = recordSearchQuery,
            onValueChange = { recordSearchQuery = it },
            placeholder = { Text("সাসপেন্ডেড আইডি তালিকায় খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredRecords.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("কোনো সাসপেন্ডেড রেকর্ড নেই", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("উপরে সার্চ করে যেকাউকে নির্দিষ্ট মেয়াদের জন্য সাসপেন্ড করতে পারেন।", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredRecords.forEach { record ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (record.isSuspendedActive) Color(0xFFD32F2F).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Row: Avatar, Name, Username, and On/Off Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile Icon with status ring
                                Box {
                                    AsyncImage(
                                        model = record.userAvatar,
                                        contentDescription = record.userName,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.5.dp,
                                                if (record.isSuspendedActive) Color(0xFFD32F2F) else BangladeshGreen,
                                                CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Status dot
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (record.isSuspendedActive) Color.Red else BangladeshGreen)
                                            .align(Alignment.BottomEnd)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.userName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${record.userUsername}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                // অন / অফ বাটন (On / Off Switch)
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (record.isSuspendedActive) "অন 🔴" else "অফ 🟢",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (record.isSuspendedActive) Color(0xFFD32F2F) else BangladeshGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Switch(
                                            checked = record.isSuspendedActive,
                                            onCheckedChange = { onToggleActive(record.id) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFFD32F2F),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Details: Duration & Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Duration Badge
                                Surface(
                                    color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "মেয়াদ: ${record.durationText}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }

                                // তারিখ (Date)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = record.suspendedDateFormatted,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Writing / Reason note
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("📝", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "কারণ/নোট: ${record.reasonNote}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom actions: Status info & কেনছেল করার অপশন (Cancel Button)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (record.isSuspendedActive)
                                        "অবস্থা: আইডি বন্ধ আছে (অন)"
                                    else
                                        "অবস্থা: সাসপেন্ড স্থগিত (অফ)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (record.isSuspendedActive) Color(0xFFD32F2F) else BangladeshGreen
                                )

                                // কেনছেল করার অপশন
                                Button(
                                    onClick = { recordToCancel = record },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.12f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("কেনছেল করুন ❌", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // কেনছেল করার কনফার্মেশন ডায়লগ (Confirmation Dialog to Cancel Suspension)
    recordToCancel?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordToCancel = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "${rec.userName}-এর সাসপেন্ড কেনছেল করবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "কেনছেল করলে এই ব্যবহারকারীর আইডি পুনরায় পুরোপুরি সচল হবে এবং সাসপেন্ড তালিকা থেকে মুছে যাবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelSuspension(rec.id)
                        recordToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("হ্যাঁ, কেনছেল করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToCancel = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * ১৭. যেকোনো ব্যবহারকারীর যেকোনো পোস্ট ফ্রী বুস্ট ও প্রমোট (Right Manager Fida - Niyon Islam Sohag)
 */
@Composable
fun AdminFreePostBoostManagerView(
    allPosts: List<Post>,
    allUsers: List<User>,
    onBoostPost: (postId: String, reach: String, days: Int, audience: String) -> Unit,
    onToggleActive: (postId: String) -> Unit,
    onCancelBoost: (postId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Promoted, 2: Regular
    var postToBoost by remember { mutableStateOf<Post?>(null) }
    var postToCancel by remember { mutableStateOf<Post?>(null) }

    // Boost config state
    var selectedReach by remember { mutableStateOf("১,০০০,০০০ (1M) রিচ 🚀") }
    var selectedDays by remember { mutableIntStateOf(30) }
    var selectedAudience by remember { mutableStateOf("সমগ্র বাংলাদেশ 🇧🇩") }

    val reachOptions = listOf(
        "৫০০,০০০ (500K) রিচ 🌟",
        "১,০০০,০০০ (1M) রিচ 🚀",
        "২,০০০,০০০ (2M) মেগা রিচ 💥",
        "৫,০০০,০০০ (5M) সুপার রিচ 👑"
    )

    val dayOptions = listOf(
        7 to "৭ দিন",
        14 to "১৪ দিন",
        21 to "২১ দিন",
        30 to "১ মাস (৩০ দিন)",
        60 to "২ মাস (৬০ দিন)",
        90 to "৩ মাস (৯০ দিন)"
    )

    val audienceOptions = listOf(
        "সমগ্র বাংলাদেশ 🇧🇩",
        "ঢাকা ও চট্টগ্রাম বিভাগ 🏙️",
        "তরুণ প্রজন্ম (১৮-৩৫ বছর) 🎯",
        "বিশ্বব্যাপী প্রবাসী বাঙালি 🌍"
    )

    val promotedCount = allPosts.count { it.isFreePromotedByNiyon }
    val activePromotedCount = allPosts.count { it.isFreePromotedByNiyon && it.isBoostActive }

    val filteredPosts = remember(allPosts, searchQuery, selectedFilter) {
        allPosts.filter { post ->
            val matchesSearch = searchQuery.isBlank() ||
                post.author.fullName.contains(searchQuery, ignoreCase = true) ||
                post.author.username.contains(searchQuery, ignoreCase = true) ||
                post.text.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                1 -> post.isFreePromotedByNiyon
                2 -> !post.isFreePromotedByNiyon
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF311B92),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C4DFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "১৭. যেকোনো পোস্ট ফ্রী বুস্ট ও প্রমোট 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Niyon Islam Sohag একক প্রতিষ্ঠাতা বিশেষ অধিকার",
                            fontSize = 12.sp,
                            color = Color(0xFFD1C4E9)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "আপনি FotoFilm-এর যেকোনো ব্যবহারকারীর যেকোনো পোস্ট সম্পূর্ণ বিনামূল্যে (০ টাকা খরচে) আনলিমিটেড রিচ, অডিয়েন্স ও মেয়াদের জন্য বুস্ট ও প্রমোট করতে পারবেন।",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${allPosts.size}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                            Text(text = "মোট পোস্ট", fontSize = 10.sp, color = Color(0xFFD1C4E9))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$promotedCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFFFD54F))
                            Text(text = "ফ্রী প্রমোটেড", fontSize = 10.sp, color = Color(0xFFD1C4E9))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "$activePromotedCount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF81C784))
                            Text(text = "সক্রিয় বুস্ট অন", fontSize = 10.sp, color = Color(0xFFD1C4E9))
                        }
                    }
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ব্যবহারকারীর নাম বা পোস্টের ক্যাপশন খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FacebookBlue) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == 0,
                onClick = { selectedFilter = 0 },
                label = { Text("সকল পোস্ট (${allPosts.size})") }
            )
            FilterChip(
                selected = selectedFilter == 1,
                onClick = { selectedFilter = 1 },
                label = { Text("ফ্রী প্রমোটেড ($promotedCount) 🚀") }
            )
            FilterChip(
                selected = selectedFilter == 2,
                onClick = { selectedFilter = 2 },
                label = { Text("সাধারণ পোস্ট (${allPosts.size - promotedCount})") }
            )
        }

        // Posts List
        Text(
            text = "পোস্ট তালিকা (${filteredPosts.size}):",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        if (filteredPosts.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "কোনো পোস্ট পাওয়া যায়নি",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            filteredPosts.forEach { post ->
                val user = allUsers.find { it.id == post.author.id } ?: post.author
                val isPromoted = post.isFreePromotedByNiyon

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isPromoted) 1.5.dp else 1.dp,
                        color = if (isPromoted) Color(0xFF7C4DFF) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    shadowElevation = if (isPromoted) 2.dp else 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User Info row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FotoFilmAvatar(
                                imageUrl = post.author.avatarUrl.ifEmpty { user.avatarUrl },
                                name = post.author.fullName,
                                contentDescription = post.author.fullName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.author.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (post.author.isVerified || user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FidaVerifiedBadge(size = 14.dp)
                                    }
                                }
                                Text(
                                    text = "${post.author.username} • ${post.timestamp}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isPromoted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RocketLaunch,
                                            contentDescription = null,
                                            tint = Color(0xFF7C4DFF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "প্রমোটেড",
                                            color = Color(0xFF7C4DFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Post Content
                        if (post.text.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = post.text,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )
                        }

                        // Post Image preview
                        post.images.firstOrNull()?.let { imgUrl ->
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Post image preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // If already boosted: show details + switch + cancel
                        if (isPromoted) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEDE7F6),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "👑 Niyon Islam Sohag ফ্রী প্রমোট সক্রিয়",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF4A148C)
                                            )
                                            Text(
                                                text = "রিচ: ${post.boostTargetReach ?: "১M রিচ"} • মেয়াদ: ${post.boostDays ?: 30} দিন",
                                                fontSize = 11.sp,
                                                color = Color(0xFF6A1B9A)
                                            )
                                            Text(
                                                text = "অডিয়েন্স: ${post.boostAudience ?: "সমগ্র বাংলাদেশ"}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF7E57C2)
                                            )
                                        }

                                        // Switch ON/OFF
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Switch(
                                                checked = post.isBoostActive,
                                                onCheckedChange = { onToggleActive(post.id) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Color(0xFF7C4DFF)
                                                )
                                            )
                                            Text(
                                                text = if (post.isBoostActive) "অন (Active)" else "অফ (Paused)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (post.isBoostActive) BangladeshGreen else Color.Red
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Cancel Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { postToCancel = post },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("বুস্ট বাতিল / কেনছেল ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Boost Action Button
                            Button(
                                onClick = {
                                    postToBoost = post
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ফ্রীতে বুস্ট ও প্রমোট করুন 🚀 (০ টাকা)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Modal: Boost Setup Dialog
    postToBoost?.let { post ->
        AlertDialog(
            onDismissRequest = { postToBoost = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF673AB7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ফ্রী পোস্ট বুস্ট ও প্রমোট সেটআপ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${post.author.fullName}-এর পোস্ট",
                        fontSize = 12.sp,
                        color = FacebookBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE7F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Niyon Islam Sohag বিশেষ সুবিধা: কোনো টাকা বা চার্জ লাগবে না, সম্পূর্ণ ফ্রীতে প্রমোট হবে!",
                                fontSize = 11.sp,
                                color = Color(0xFF4A148C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 1. Target Reach
                    Text("১. টার্গেট রিচ নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    reachOptions.forEach { reach ->
                        Surface(
                            onClick = { selectedReach = reach },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedReach == reach) Color(0xFF7C4DFF).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (selectedReach == reach) BorderStroke(1.dp, Color(0xFF7C4DFF)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReach == reach,
                                    onClick = { selectedReach = reach }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = reach, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // 2. Duration Days
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("২. বুস্টের মেয়াদ (দিন):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dayOptions.forEach { (days, label) ->
                            FilterChip(
                                selected = selectedDays == days,
                                onClick = { selectedDays = days },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // 3. Target Audience
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("৩. টার্গেট অডিয়েন্স:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    audienceOptions.forEach { aud ->
                        Surface(
                            onClick = { selectedAudience = aud },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedAudience == aud) Color(0xFF7C4DFF).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (selectedAudience == aud) BorderStroke(1.dp, Color(0xFF7C4DFF)) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedAudience == aud,
                                    onClick = { selectedAudience = aud }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = aud, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBoostPost(post.id, selectedReach, selectedDays, selectedAudience)
                        postToBoost = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Text("ফ্রীতে বুস্ট চালু করুন 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { postToBoost = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Modal: Cancel Confirmation Dialog
    postToCancel?.let { post ->
        AlertDialog(
            onDismissRequest = { postToCancel = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp)) },
            title = { Text("${post.author.fullName}-এর বুস্ট কেনছেল করবেন?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "কেনছেল করলে এই পোস্টের ফ্রী প্রমোটেড স্ট্যাটাস বাতিল হবে এবং পোস্টটি পুনরায় সাধারণ পোস্টে রূপান্তরিত হবে।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelBoost(post.id)
                        postToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("হ্যাঁ, কেনছেল করুন ❌", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { postToCancel = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Option 18: Face AD এডমিন Hub View inside Right Manager Fida
 * Allows Niyon Islam Sohag to manage Ads Manager, AdMob monetization, and Story Audio tools
 */
@Composable
fun AdminFaceAdAdminHubView(
    viewModel: FaceFeedViewModel,
    onOpenFullFaceAdDialog: () -> Unit
) {
    val context = LocalContext.current
    val isAdMobMasterEnabled by viewModel.isAdMobMasterEnabled.collectAsState()
    val isAdMobBannerEnabled by viewModel.isAdMobBannerEnabled.collectAsState()
    val isAdMobInterstitialEnabled by viewModel.isAdMobInterstitialEnabled.collectAsState()
    val isAdMobRewardedEnabled by viewModel.isAdMobRewardedEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        item {
            // Header Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF6D00),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Face AD এডমিন হাব 📢", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("Right Manager Fida-এর সাথে সংযুক্ত বিজ্ঞাপন ও মিউজিক টুলস", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenFullFaceAdDialog,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Face AD এডমিন পূর্ণাঙ্গ প্যানেল খুলুন", fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
                    }
                }
            }
        }

        // Section 1: Ads Manager Tools
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("১. Ads Manager (ক্যাম্পেইন ও বাজেট)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• সকল বাণিজ্যিক স্পনসর্ড পোস্ট পর্যবেক্ষণ ও অনুমোদন\n• টার্গেটেড পৌঁছানো (Reach) ও বাজেট ক্যালকুলেটর\n• FotoFilm ফিডে প্রমোটেড ব্যাজ প্রদর্শন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section 2: AdMob Monetization Controls (Persistent Owner Locks)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF388E3C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("২. Google AdMob মনিটাইজেশন কন্ট্রোল", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("সকল Google AdMob বিজ্ঞাপন (Master)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isAdMobMasterEnabled,
                            onCheckedChange = {
                                if (viewModel.toggleAdMobMaster(it)) {
                                    Toast.makeText(
                                        context,
                                        if (it) "✅ সব AdMob বিজ্ঞাপন চালু হয়েছে" else "🔒 সব AdMob বিজ্ঞাপন বন্ধ — আপনি চালু না করা পর্যন্ত চালু হবে না",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ব্যানার বিজ্ঞাপন (Banner Ads)", fontSize = 13.sp)
                        Switch(
                            checked = isAdMobBannerEnabled,
                            onCheckedChange = {
                                if (viewModel.toggleAdMobBanner(it)) {
                                    Toast.makeText(
                                        context,
                                        if (it) "✅ ব্যানার অ্যাড চালু হয়েছে" else "🔒 ব্যানার অ্যাড বন্ধ — আপনি চালু না করা পর্যন্ত চালু হবে না",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ইন্টারস্টিশিয়াল বিজ্ঞাপন (Full Screen Ads)", fontSize = 13.sp)
                        Switch(
                            checked = isAdMobInterstitialEnabled,
                            onCheckedChange = {
                                if (viewModel.toggleAdMobInterstitial(it)) {
                                    Toast.makeText(
                                        context,
                                        if (it) "✅ ইন্টারস্টিশিয়াল অ্যাড চালু হয়েছে" else "🔒 ইন্টারস্টিশিয়াল অ্যাড বন্ধ — আপনি চালু না করা পর্যন্ত চালু হবে না",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("রিওয়ার্ডেড ভিডিও বিজ্ঞাপন (Rewarded Video Ads)", fontSize = 13.sp)
                        Switch(
                            checked = isAdMobRewardedEnabled,
                            onCheckedChange = {
                                if (viewModel.toggleAdMobRewarded(it)) {
                                    Toast.makeText(
                                        context,
                                        if (it) "✅ রিওয়ার্ডেড অ্যাড চালু হয়েছে" else "🔒 রিওয়ার্ডেড অ্যাড বন্ধ — আপনি চালু না করা পর্যন্ত চালু হবে না",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Section 3: Audio Music Player & Story Audio Library
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color(0xFFD81B60))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("৩. অডিও মিউজিক প্লেয়ার ও লাইব্রেরি হাব", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• স্টোরির জন্য গান, গজল, বাঁশির সুর, ডিজে ও দেশাত্মবোধক ট্র্যাকের সেন্ট্রাল অডিট\n• FotoFilm অডিও প্লেয়ার ও ব্যবহারকারীদের আপলোডকৃত সুর পর্যালোচনা\n• ব্যাকগ্রাউন্ড মিউজিক প্লেব্যাকের সামগ্রিক নিয়ন্ত্রণ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Tab 18: Option 19 - গোপন চ্যাট সার্ভিল্যান্স ও পার্মানেন্ট ভল্ট (খারাপ লেখা, ছবি, ভিডিও, ভয়েস, স্ক্রিনশট)
 * ব্যবহারকারীরা মেসেঞ্জার থেকে ডিলিট করলেও এই ভল্ট থেকে কখনই ডিলিট হবে না।
 */
@Composable
private fun AdminSecretSurveillanceVaultView(
    vaultMessages: List<DeliveredVaultMessage>,
    allUsers: List<User>,
    onSuspendUser: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, bad, media, video, voice
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val badWords = remember {
        listOf("খারাপ", "বাজে", "গালি", "নষ্ট", "spam", "scam", "cheat", "হুমকি", "অশ্লীল")
    }

    val filteredMessages = remember(vaultMessages, searchQuery, selectedFilter) {
        vaultMessages.filter { msg ->
            val matchesSearch = searchQuery.isBlank() ||
                    msg.senderName.contains(searchQuery, ignoreCase = true) ||
                    msg.contactName.contains(searchQuery, ignoreCase = true) ||
                    msg.text.contains(searchQuery, ignoreCase = true)

            val isBad = badWords.any { msg.text.contains(it, ignoreCase = true) } ||
                    msg.text.contains("গোপন") || msg.text.contains("হুমকি") || msg.text.contains("স্ক্রিনশট")

            val matchesFilter = when (selectedFilter) {
                "bad" -> isBad
                "media" -> msg.mediaUrl != null && !msg.isMediaVideo && !msg.isAudioVoice
                "video" -> msg.isMediaVideo
                "voice" -> msg.isAudioVoice
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Top Card: Permanent Surveillance Notice
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFB71C1C),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "গোপন চ্যাট সার্ভিল্যান্স ও পার্মানেন্ট ভল্ট 🛡️🕵️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Right Manager Fida একক স্পাই ও ফরেনসিক নিয়ন্ত্রণ",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🔒 অ্যাপ ব্যবহারকারীরা একে অপরকে পাঠানো যেকোনো খারাপ লেখা টেস্ট, ছবি, ভিডিও, অডিও ভয়েস কিংবা স্ক্রিনশট দিলে তা এখানে স্থায়ীভাবে সংরক্ষিত হচ্ছে। ব্যবহারকারীরা নিজ নিজ ইনবক্স থেকে মেসেজ সম্পূর্ণ ডিলিট করে ফেললেও এই সার্ভার ভল্ট থেকে কখনই ডিলিট হবে না!",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Search & Filter Chips
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ব্যবহারকারী বা মেসেজ দিয়ে সার্চ করুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "all" to "সকল মেসেজ (${vaultMessages.size})",
                    "bad" to "⚠️ খারাপ/অনুপযুক্ত ভাষা",
                    "media" to "📸 ছবি ও স্ক্রিনশট",
                    "video" to "🎥 ভিডিও মিডিয়া",
                    "voice" to "🎤 অডিও ভয়েস"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Message Vault Items
        if (filteredMessages.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোনো অনুপযুক্ত মেসেজ বা মিডিয়া খুঁজে পাওয়া যায়নি", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredMessages) { msg ->
                val isBad = badWords.any { msg.text.contains(it, ignoreCase = true) }
                val senderUser = allUsers.find { it.id == msg.senderId }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBad) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isBad) Color(0xFFE53935) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Sender & Recipient Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FotoFilmAvatar(
                                    imageUrl = senderUser?.avatarUrl ?: msg.contactAvatarUrl,
                                    name = msg.senderName,
                                    contentDescription = msg.senderName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("➡️", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(msg.contactName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("${msg.formattedDate} • ${msg.timestamp}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "🔒 সংরক্ষিত ভল্ট",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (isBad) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = "⚠️ খারাপ/অনুপযুক্ত ভাষা শনাক্ত হয়েছে",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Message Text Content
                        if (msg.text.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isBad) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isBad) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Media / Screenshot / Photo Content
                        msg.mediaUrl?.let { url ->
                            Spacer(modifier = Modifier.height(8.dp))
                            if (msg.isMediaVideo) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Video Thumbnail",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                            }
                                        }
                                        Text(
                                            text = "🎥 ভিডিও মিডিয়া রেকর্ড",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else if (msg.isAudioVoice) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FacebookBlue.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = FacebookBlue, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🎤 অডিও ভয়েস রেকর্ড (${msg.audioDurationSeconds}s)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FacebookBlue)
                                            Text("ব্যবহারকারী ডিলিট করলেও ভল্টে স্থায়ীভাবে সুরক্ষিত", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Screenshot / Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Text(
                                            text = "📸 স্ক্রিনশট / ছবি",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Action Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                    Toast.makeText(context, "মেসেজ কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("কপি", fontSize = 11.sp)
                            }

                            senderUser?.let { sUser ->
                                Button(
                                    onClick = { onSuspendUser(sUser) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("🚫 আইডি সাসপেন্ড", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
 * Tab 19: Option 20 - লাইভ ইউজার ও ডাউনলোড কাউন্টার হিস্ট্রি
 * ১ জন অ্যাপ ডাউনলোড করছে, ৫০ জন ডাউনলোড করছে— এই তালিকায় ক্রমানুসারে প্রত্যেকের প্রোফাইল আইকন ও তথ্য থাকবে।
 */
@Composable
private fun AdminLiveUsersDownloadCounterView(
    totalDownloads: Int,
    allUsers: List<User>,
    onSimulateDownload: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers
        else allUsers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // Milestone Download Card (১ জন, ৫০ জন... ক্রমবৃদ্ধি)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF00796B),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "লাইভ ডাউনলোড কাউন্টার 📈",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "সর্বমোট অ্যাপ ডাউনলোড সংখ্যা",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Big Download Number
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "$totalDownloads+ জন",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🚀 ১ জন, ৫০ জন করে ক্রমবৃদ্ধিমান সম্পূর্ণ তালিকা। অ্যাপ ব্যবহারকারীদের প্রত্যেকের প্রোফাইল ছবি ও আইকন সহ স্বয়ংক্রিয়ভাবে সিরিয়াল নম্বর যুক্ত হয়ে সংরক্ষণ হচ্ছে।",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onSimulateDownload()
                            Toast.makeText(context, "নতুন ১ জন অ্যাপ ডাউনলোড যুক্ত হয়েছে! মোট: ${totalDownloads + 1} জন 🚀", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00796B))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ নতুন ডাউনলোড বৃদ্ধি করুন (+1 Download)",
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Stats Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FacebookBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${allUsers.size} জন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FacebookBlue)
                        Text("মোট প্রোফাইল ইউজার", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BangladeshGreen.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("+১৪ জন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BangladeshGreen)
                        Text("আজকের নতুন ডাউনলোড", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE91E63).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${allUsers.count { it.isVerified }} জন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE91E63))
                        Text("ভেরিফাইড ইউজার", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Search Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ডাউনলোডকারী ব্যবহারকারী খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Sequential Download List (১ জন অ্যাপ ডাউনলোড করছে, ৫০ জন ডাউনলোড করছে...)
        items(filteredUsers.size) { index ->
            val user = filteredUsers[index]
            val downloadRank = index + 1
            val milestoneBadge = when (downloadRank) {
                1 -> "১ম জন অ্যাপ ডাউনলোড করেছে 👑"
                50 -> "৫০তম মাইলস্টোন ডাউনলোড 🎖️"
                100 -> "১০০তম মেগা মাইলস্টোন ডাউনলোড 🚀"
                else -> "#$downloadRank জন অ্যাপ ডাউনলোড করেছে ✅"
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (downloadRank == 1 || downloadRank == 50) Color(0xFFFFD700) else MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with verified badge
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
                                    .size(16.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (downloadRank == 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("👑", fontSize = 12.sp)
                            }
                        }
                        Text("@${user.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Download Sequence Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (downloadRank == 1) Color(0xFFFFF8E1) else if (downloadRank == 50) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = milestoneBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (downloadRank == 1) Color(0xFFF57F17) else if (downloadRank == 50) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "ইনস্টল সম্পন্ন ✅",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Android 14 • Chrome PWA",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 20: অপশন ২১: অ্যাপ হ্যাক ও অনুপ্রবেশ মনিটর (Anti-Hack Intrusion Logs)
 * Niyon Islam Sohag-এর ব্যক্তিগত সাইবার সুরক্ষা প্রাচীর: অ্যাপ হ্যাকিং, রিভার্স ইঞ্জিনিয়ারিং, ডিবি ইনজেকশন ইত্যাদির সকল প্রচেষ্টা এখানে রিয়েল-টাইমে প্রদর্শিত হবে।
 */
@Composable
private fun AdminAntiHackMonitorView(
    intrusionLogs: List<IntrusionLog>,
    onSimulateTest: () -> Unit,
    onBlockIntruder: (String) -> Unit,
    onClearResolved: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Header Hero Card with Shield & Live Pulse
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1E2D),
                border = BorderStroke(1.5.dp, Color(0xFFD50000)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD50000).copy(alpha = 0.2f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFFFF1744),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "অ্যাপ হ্যাক ও অনুপ্রবেশ মনিটর 🚨",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "জিরো-ট্রাস্ট অ্যান্টি-হ্যাক ডিফেন্স সিস্টেম",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ফায়ারওয়াল সক্রিয় 🛡️",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "মালিক Niyon Islam Sohag-এর ব্যক্তিগত সাইবার সেল: কোনো অসাধু ব্যক্তি বা হ্যাকার অ্যাপে অনুপ্রবেশ, রিভার্স ইঞ্জিনিয়ারিং, ডাটাবেস টেম্পারিং বা বাইপাস করার চেষ্টা করলে এখানে তাৎক্ষণিক আইপি, ডিভাইস আইডি, আক্রমণ ভেক্টর ও সময়সহ রেকর্ড হয়ে যায় এবং সিস্টেম তাৎক্ষণিক তাদের অটো-ব্লক করে দেয়।",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats overview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2A2B3D),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${intrusionLogs.size}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFF5252)
                                )
                                Text("আক্রমণ প্রতিহত", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2A2B3D),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${intrusionLogs.count { it.status.contains("BLOCKED") || it.status.contains("ব্লকড") }}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color(0xFF00E676)
                                )
                                Text("আইপি ব্যানড 🚫", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2A2B3D),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "১০০%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color(0xFF40C4FF)
                                )
                                Text("সুরক্ষিত ডাটা 🔒", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSimulateTest,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("অনুপ্রবেশ টেস্ট চালান 🧪", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onClearResolved,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("হিস্ট্রি মুছুন", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. Intrusion Logs List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সনাক্তকৃত ক্ষতিকর অনুপ্রবেশ ও হ্যাকিং চেষ্টা (${intrusionLogs.size} টি)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "লাইভ ট্র্যাকিং 🟢",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        if (intrusionLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛡️", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো অনুপ্রবেশের চেষ্টা নেই",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "অ্যাপের সকল সিস্টেম সম্পূর্ণ সুরক্ষিত রয়েছে।",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(intrusionLogs, key = { it.id }) { log ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color(0xFFD50000).copy(alpha = 0.35f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFEBEE),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🚨", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = log.attackerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "IP: ${log.ipAddress}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = log.severity,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "আক্রমণের ধরন: ${log.attackVector}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "টার্গেট কম্পোনেন্ট: ${log.targetArea} • ডিভাইস: ${log.deviceModel}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "সময়: ${log.timestamp}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Defense & Block status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = log.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Button(
                                onClick = { onBlockIntruder(log.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("আইপি পার্মানেন্ট ব্লক", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ডিফেন্স লগ: ${log.defenseLog}",
                            fontSize = 10.sp,
                            color = Color(0xFF558B2F),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 21: অপশন ২২: যেকোনো ভিডিও ফ্রী বুস্টার (Niyon Special)
 * Niyon Islam Sohag-এর ব্যক্তিগত বিশেষ ক্ষমতা: যেকোনো ভিডিও সম্পূর্ণ বিনামূল্যে (0 ৳) ১ ক্লিকে ১M+ ভাইরাল ভিউ ও ট্রেন্ডিং ব্যাজ দিয়ে বুস্ট করুন।
 */
@Composable
private fun AdminFreeVideoBoosterView(
    videoPosts: List<Post>,
    onFreeBoostVideo: (postId: String, reach: String, badge: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredVideos = remember(videoPosts, searchQuery) {
        if (searchQuery.isBlank()) videoPosts
        else videoPosts.filter {
            it.author.fullName.contains(searchQuery, ignoreCase = true) ||
            (it.videoTitle?.contains(searchQuery, ignoreCase = true) == true) ||
            it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Header Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF311B92),
                border = BorderStroke(1.5.dp, Color(0xFFE040FB)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE040FB).copy(alpha = 0.25f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎬", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "যেকোনো ভিডিও ফ্রী বুস্টার 🎬🚀",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Niyon Islam Sohag এক্সক্লুসিভ ভিআইপি ক্ষমতা (0 ৳)",
                                fontSize = 11.sp,
                                color = Color(0xFFEA80FC),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "আপনি অ্যাপের প্রধান মালিক Niyon Islam Sohag হিসেবে যেকোনো ব্যবহারকারীর আপলোড করা ভিডিও বা শর্ট ভিডিও সম্পূর্ণ বিনামূল্যে (0 ৳) বুস্ট করে ১M+ ভাইরাল ভিউ, ট্রেন্ডিং #১ ব্যাজ এবং আনলিমিটেড লাইক ও শেয়ার প্রদান করতে পারেন!",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ভিডিও বা ক্রিয়েটরের নাম খুঁজুন...", fontSize = 12.sp, color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE040FB),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. Video List Header
        item {
            Text(
                text = "ভিডিও ও শর্ট ভিডিও তালিকা (${filteredVideos.size} টি)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (filteredVideos.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📹", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("কোনো ভিডিও পাওয়া যায়নি", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredVideos, key = { it.id }) { post ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = if (post.isFreePromotedByNiyon) BorderStroke(1.5.dp, Color(0xFFE040FB)) else null,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FotoFilmAvatar(
                                imageUrl = post.author.avatarUrl,
                                name = post.author.fullName,
                                contentDescription = post.author.fullName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.author.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (post.author.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FidaVerifiedBadge(size = 14.dp)
                                    }
                                }
                                Text(
                                    text = post.videoTitle ?: post.title ?: post.text.take(35),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (post.isFreePromotedByNiyon) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF3E5F5),
                                    border = BorderStroke(1.dp, Color(0xFFAB47BC))
                                ) {
                                    Text(
                                        text = "VIP বুস্টেড 🔥",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6A1B9A),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats: Views, Reactions, Shares
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "👁️ ভিউ: ${post.reachViews}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "❤️ রিঅ্যাকশন: ${post.totalReactions}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🔄 শেয়ার: ${post.shareCount}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Click Free Boost Options
                        Text("ফ্রী ভিআইপি বুস্টার অপশন (মালিক সুবিধা - 0 ৳):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onFreeBoostVideo(post.id, "1,000,000+ (১M) মেগা ভাইরাল ভিউ", "🔥 ১M+ ট্রেন্ডিং #1") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("১M+ মেগা বুস্ট 🚀", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onFreeBoostVideo(post.id, "500,000+ ভাইরাল হিট ভিউ", "⚡ ৫০০K ভাইরাল") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("৫০০K ভাইরাল ⚡", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onFreeBoostVideo(post.id, "100,000+ ট্রেন্ডিং ভিউ", "🌟 ১০০K হট") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("১০০K বুস্ট 🌟", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ২৩. সাপোর্ট টিম ইনবক্স ও ৫টি স্ক্রিনশট ম্যানেজমেন্ট (Right Manager Fida)
 * ব্যবহারকারীদের পাঠানো সাপোর্ট টিকেট, প্রোফাইল লিংক, ৫টি স্ক্রিনশট এবং সরাসরি রিপ্লাই
 */
@Composable
fun AdminSupportTeamInboxView(
    tickets: List<SupportTicket>,
    onReplyTicket: (String, String) -> Unit,
    onDeleteTicket: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, RESOLVED
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    val filteredTickets = remember(tickets, searchQuery, selectedFilter) {
        tickets.filter { ticket ->
            val matchesSearch = ticket.userName.contains(searchQuery, ignoreCase = true) ||
                    ticket.message.contains(searchQuery, ignoreCase = true) ||
                    ticket.profileUrl.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "PENDING" -> ticket.status.equals("PENDING", ignoreCase = true) ||
                        ticket.status.equals("Open", ignoreCase = true)
                "RESOLVED" -> ticket.status.equals("RESOLVED", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Header Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF00897B),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সাপোর্ট টিম ইনবক্স (ছাপোড) 🎧",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "ব্যবহারকারীদের বার্তা, ৫টি স্ক্রিনশট ও প্রোফাইল আইডি লিংক",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("মোট মেসেজ", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("${tickets.size} টি", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("অপেক্ষমান ⏳", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("${tickets.count { it.status == "PENDING" }} টি", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("উত্তর সম্পন্ন ✅", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("${tickets.count { it.status == "RESOLVED" }} টি", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                            }
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম, সমস্যা বা আইডি লিংক দিয়ে খুঁজুন...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("সব (${tickets.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("অপেক্ষমান (${tickets.count { it.status == "PENDING" }})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "RESOLVED",
                        onClick = { selectedFilter = "RESOLVED" },
                        label = { Text("উত্তর দেওয়া (${tickets.count { it.status == "RESOLVED" }})", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Tickets List
        if (filteredTickets.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF00897B), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "কোনো মেসেজ মেলেনি" else "কোনো সাপোর্ট মেসেজ নেই! সবকিছু ঠিক আছে 😊",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                var replyText by remember(ticket.id) { mutableStateOf("") }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User Header Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = ticket.userAvatar,
                                contentDescription = ticket.userName,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF00897B), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ticket.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = ticket.timestamp,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ticket.status == "RESOLVED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (ticket.status == "RESOLVED") "উত্তর সম্পন্ন ✅" else "অপেক্ষমান ⏳",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ticket.status == "RESOLVED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Profile ID Link Section
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ইউজারের প্রোফাইল আইডি লিংক:", fontSize = 10.sp, color = Color.Gray)
                                    Text(ticket.profileUrl, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = FacebookBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(ticket.profileUrl))
                                        Toast.makeText(context, "প্রোফাইল লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = FacebookBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Message Text
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ব্যবহারকারীর বার্তা / সমস্যা:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(ticket.message, fontSize = 13.sp, color = Color.Black)
                            }
                        }

                        // Attached Screenshots Preview (up to 5 photos)
                        if (ticket.screenshotUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "সংযুক্ত ছবি ও স্ক্রিনশট (${ticket.screenshotUrls.size}/৫টি - বড় করে দেখতে চাপুন):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00897B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ticket.screenshotUrls.forEachIndexed { index, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFB2DFDB), RoundedCornerShape(8.dp))
                                            .clickable { zoomedImageUrl = url }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Screenshot ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(topStart = 4.dp),
                                            color = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Existing Admin Reply (if any)
                        if (ticket.adminReply != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "সাপোর্ট টিম রিপ্লাই (${ticket.adminReplyTime ?: "সম্প্রতি"}):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ticket.adminReply,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Reply Input and Submit
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("উত্তর লিখুন...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        onReplyTicket(ticket.id, replyText)
                                        replyText = ""
                                    }
                                },
                                enabled = replyText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("রিপ্লাই", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDeleteTicket(ticket.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-screen Zoomed Screenshot Viewer
    if (zoomedImageUrl != null) {
        Dialog(
            onDismissRequest = { zoomedImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { zoomedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = zoomedImageUrl,
                    contentDescription = "Zoomed screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { zoomedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

/**
 * ২৪. ইউজার অ্যাড ভিউ ও স্কিপ ট্র্যাকার (Right Manager Fida Option 24)
 * ট্র্যাক করে:
 * - কোন ব্যক্তি কয়টা অ্যাড দেখলো (totalAdsSeen)
 * - কার সামনে কয়টা দৈনিক অ্যাড গেলো (dailyAdsServed)
 * - সে কয়টা অ্যাড সম্পূর্ণ দেখলো (completedAdsWatched)
 * - কয়টা অ্যাড কেটে স্কিপ করলো (skippedAdsCount)
 * - ইউজারের প্রোফাইল আইকন ও ছবি সহ বিস্তারিত বিশ্লেষণ
 */
@Composable
fun AdminUserAdAnalyticsView(
    analyticsList: List<UserAdAnalytics>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortFilter by remember { mutableStateOf("ALL") } // ALL, MOST_SEEN, MOST_SKIPPED, HIGH_DAILY

    val filteredList = remember(analyticsList, searchQuery, selectedSortFilter) {
        val filtered = analyticsList.filter {
            it.userName.contains(searchQuery, ignoreCase = true) ||
            it.userHandle.contains(searchQuery, ignoreCase = true) ||
            it.userId.contains(searchQuery, ignoreCase = true)
        }
        when (selectedSortFilter) {
            "MOST_SEEN" -> filtered.sortedByDescending { it.totalAdsSeen }
            "MOST_SKIPPED" -> filtered.sortedByDescending { it.skippedAdsCount }
            "HIGH_DAILY" -> filtered.sortedByDescending { it.dailyAdsServed }
            else -> filtered
        }
    }

    val totalImpressions = remember(analyticsList) { analyticsList.sumOf { it.totalAdsSeen } }
    val totalDailyServed = remember(analyticsList) { analyticsList.sumOf { it.dailyAdsServed } }
    val totalCompleted = remember(analyticsList) { analyticsList.sumOf { it.completedAdsWatched } }
    val totalSkipped = remember(analyticsList) { analyticsList.sumOf { it.skippedAdsCount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Top Overview Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF6A1B9A),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ইউজার অ্যাড ভিউ ও স্কিপ ট্র্যাকার 📊",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "কে কয়টা অ্যাড দেখলো ও কেটে স্কিপ করলো তার লাইভ হিসাব",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Overview Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("মোট ভিউ", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("$totalImpressions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("দৈনিক সামনে", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("$totalDailyServed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1BEE7))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("সম্পূর্ণ দেখা", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("$totalCompleted", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("স্কিপ করেছে", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.85f))
                                Text("$totalSkipped", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
                            }
                        }
                    }
                }
            }
        }

        // Search Bar and Filter Sorting Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ব্যবহারকারীর নাম বা ইউজারনেম দিয়ে খুঁজুন...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSortFilter == "ALL",
                        onClick = { selectedSortFilter = "ALL" },
                        label = { Text("সকল ইউজার (${analyticsList.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedSortFilter == "MOST_SEEN",
                        onClick = { selectedSortFilter = "MOST_SEEN" },
                        label = { Text("বেশি অ্যাড দেখা 🔥", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedSortFilter == "MOST_SKIPPED",
                        onClick = { selectedSortFilter = "MOST_SKIPPED" },
                        label = { Text("বেশি স্কিপ করা ⚠️", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedSortFilter == "HIGH_DAILY",
                        onClick = { selectedSortFilter = "HIGH_DAILY" },
                        label = { Text("দৈনিক বেশি ইমপ্রেশন 📈", fontSize = 11.sp) }
                    )
                }
            }
        }

        // User Ad Activity Cards
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কোনো ব্যবহারকারী পাওয়া যায়নি", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(filteredList, key = { it.userId }) { user ->
                val completionRate = if (user.totalAdsSeen > 0) {
                    ((user.completedAdsWatched.toDouble() / user.totalAdsSeen) * 100).toInt()
                } else 0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Profile Avatar and Identity Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = user.userAvatar,
                                contentDescription = user.userName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF6A1B9A), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.userName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FidaVerifiedBadge(size = 14.dp)
                                }
                                Text(
                                    text = "${user.userHandle} • শেষ অ্যাড: ${user.lastAdSeenTime}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            // Completion Rate Chip
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (completionRate >= 70) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = "কমপ্লিট $completionRate%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completionRate >= 70) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4 Metrics Grid for This User (Matching user brief exactly)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // ১. কয়টা এড দেখলো
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FacebookBlue.copy(alpha = 0.09f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("মোট এড দেখলো", fontSize = 9.sp, color = FacebookBlue, fontWeight = FontWeight.Medium)
                                    Text("${user.totalAdsSeen} টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                                }
                            }

                            // ২. কার সামনে কয়টা দৈনিক এড গেলো
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6A1B9A).copy(alpha = 0.09f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("দৈনিক এড গেলো", fontSize = 9.sp, color = Color(0xFF6A1B9A), fontWeight = FontWeight.Medium)
                                    Text("${user.dailyAdsServed} টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                }
                            }

                            // ৩. সে কয়টা এড সম্পূর্ণ দেখলো
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.09f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("সম্পূর্ণ দেখলো", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                                    Text("${user.completedAdsWatched} টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }

                            // ৪. কয়টা এড কেটে স্কিপ করলো
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD32F2F).copy(alpha = 0.09f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("কেটে স্কিপ করলো", fontSize = 9.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                                    Text("${user.skippedAdsCount} টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ad Formats Breakdown (Banner vs Interstitial)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏷️ ব্যানার অ্যাড: ", fontSize = 11.sp, color = Color.Gray)
                                    Text("${user.bannerAdsViewed} টি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎬 ইন্টারস্টিশিয়াল: ", fontSize = 11.sp, color = Color.Gray)
                                    Text("${user.interstitialAdsViewed} টি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Recent Log Events
                        if (user.recentAdLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("রিসেন্ট অ্যাড হিস্ট্রি লগ:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    user.recentAdLogs.take(2).forEach { log ->
                                        Text("• $log", fontSize = 11.sp, color = Color(0xFF424242))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// ২৭. বন্ধুদের সাথে আড্ডা গ্রুপ ম্যানেজার (গ্রুপের নাম + প্রোফাইল আইকন + অন/অফ বাটন + এড)
// ==============================================================================
@Composable
fun AdminHangoutGroupsControlView(
    allGroups: List<SocialGroup>,
    presetRooms: List<HangoutRoomControlEntry>,
    onToggleGroup: (String, String, Boolean) -> Unit,
    onTogglePresetRoom: (String, String, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredGroups = remember(allGroups, searchQuery) {
        allGroups.filter {
            searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.creatorName.contains(searchQuery, ignoreCase = true)
        }
    }
    val filteredPresetRooms = remember(presetRooms, searchQuery) {
        presetRooms.filter {
            searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.creatorName.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalCount = allGroups.size + presetRooms.size
    val activeCount = allGroups.count { it.isActive } + presetRooms.count { it.isActive }
    val pausedCount = totalCount - activeCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header Summary Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF00838F).copy(alpha = 0.09f),
            border = BorderStroke(1.dp, Color(0xFF00838F).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎙️ বন্ধুদের সাথে আড্ডা গ্রুপ নিয়ন্ত্রণ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF006064)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "মোট আড্ডা গ্রুপ: $totalCount টি • 🟢 চালু: $activeCount টি • 🔴 বন্ধ: $pausedCount টি",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    color = Color(0xFF00838F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "অন / অফ প্যানেল",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("গ্রুপের নাম বা যে খুলেছে তার নাম খুঁজুন...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hangout_admin_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            if (filteredGroups.isNotEmpty()) {
                item {
                    Text(
                        text = "🌟 ইউজারদের খোলা বন্ধুদের সাথে আড্ডা গ্রুপ (${filteredGroups.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00838F),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                items(filteredGroups, key = { "hangout_grp_${it.id}" }) { group ->
                    HangoutGroupSwitchCard(
                        avatarUrl = group.coverUrl,
                        groupName = group.name,
                        creatorLabel = "খুলেছেন: ${group.creatorName}",
                        subInfo = "${if (group.isPrivate) "🔒 প্রাইভেট" else "🌐 উন্মুক্ত"} • ${group.memberCount} জন • ${group.createdAt}",
                        isActive = group.isActive,
                        testTagKey = "hangout_switch_${group.id}",
                        onToggle = { enabled ->
                            onToggleGroup(group.id, group.name, enabled)
                        }
                    )
                }
            }

            if (filteredPresetRooms.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎧 লাইভ পার্টি ও আড্ডা রুমসমূহ (${filteredPresetRooms.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                items(filteredPresetRooms, key = { "hangout_room_${it.roomKey}" }) { room ->
                    HangoutGroupSwitchCard(
                        avatarUrl = room.avatarUrl,
                        groupName = room.title,
                        creatorLabel = "খুলেছেন: ${room.creatorName}",
                        subInfo = "${room.category} • ${room.memberCount} জন • ${room.createdAt}",
                        isActive = room.isActive,
                        testTagKey = "hangout_switch_${room.roomKey}",
                        onToggle = { enabled ->
                            onTogglePresetRoom(room.roomKey, room.title, enabled)
                        }
                    )
                }
            }

            if (filteredGroups.isEmpty() && filteredPresetRooms.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Text(
                            text = "কোনো বন্ধুদের সাথে আড্ডা গ্রুপ খুঁজে পাওয়া যায়নি",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            // Google AdMob Banner inside Hangout Groups Manager tab
            item {
                AdBanner(creativeIndex = 2)
            }
        }
    }
}

@Composable
private fun HangoutGroupSwitchCard(
    avatarUrl: String,
    groupName: String,
    creatorLabel: String,
    subInfo: String,
    isActive: Boolean,
    testTagKey: String,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.surface else Color(0xFFFFEBEE).copy(alpha = 0.7f),
        border = BorderStroke(
            1.dp,
            if (isActive) Color(0xFF00838F).copy(alpha = 0.32f) else Color.Red.copy(alpha = 0.45f)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group Profile Icon with Live On/Off Dot
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = groupName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isActive) BangladeshGreen else Color.Red,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(if (isActive) OnlineGreen else Color.Red)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group Name & Creator Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = groupName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = creatorLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF006064),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subInfo,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // On / Off Switch right in front of the group row
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isActive) "অন (চালু)" else "অফ (বন্ধ)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) BangladeshGreen else Color.Red
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag(testTagKey),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BangladeshGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Red
                    )
                )
            }
        }
    }
}

@Composable
private fun AdminFakeIdDetectorView(
    reports: List<FakeIdReport>,
    onPermanentBan: (FakeIdReport) -> Unit,
    onDeleteReport: (FakeIdReport) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val maleToFemaleCount = remember(reports) { reports.count { it.violationType == "MALE_TO_FEMALE_FAKE" } }
    val femaleToMaleCount = remember(reports) { reports.count { it.violationType == "FEMALE_TO_MALE_FAKE" } }
    val stolenPhotoCount = remember(reports) { reports.count { it.violationType == "STOLEN_PHOTO" } }
    val limit3Count = remember(reports) { reports.count { it.violationType == "LIMIT_3_ACCOUNTS" } }

    val filteredReports = remember(reports, selectedCategory) {
        when (selectedCategory) {
            "ALL" -> reports
            else -> reports.filter { it.violationType == selectedCategory }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Active Rules Overview Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFB71C1C),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GppBad,
                            contentDescription = "ফেইক আইডি শিল্ড",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "২৮. ফেইক আইডি ডিটেক্টর ও অটো-ব্লক 🕵️",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "কেউ ফেইক আইডি খোলার চেষ্টা করলে সাথে সাথে ব্লক হয়ে এখানে দেখাবে",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✅ নিয়ম ১: ১ ব্যক্তি সর্বোচ্চ ৩টি আইডি সীমা চালু (৪র্থ আইডি ব্লক)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "✅ নিয়ম ২: অন্যের ছবি (SHA-256 + Perceptual dHash) চুরি করে ফেইক আইডি ব্লক",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "✅ নিয়ম ৩: ছেলে -> মেয়ের নামে/ছবি ও মেয়ে -> ছেলের নামে/ছবি ফেইক আইডি ব্লক",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (reports.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onClearAll,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "সব লগ ক্লিয়ার করুন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "ALL" to "সব (${reports.size})",
                    "MALE_TO_FEMALE_FAKE" to "ছেলে→মেয়ে ($maleToFemaleCount)",
                    "FEMALE_TO_MALE_FAKE" to "মেয়ে→ছেলে ($femaleToMaleCount)",
                    "STOLEN_PHOTO" to "অন্যের ছবি ($stolenPhotoCount)",
                    "LIMIT_3_ACCOUNTS" to "৩+ আইডি ($limit3Count)"
                )
                chips.forEach { (key, label) ->
                    val isSelected = selectedCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        if (filteredReports.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "সুরক্ষিত",
                            tint = BangladeshGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "এখন পর্যন্ত কোনো ফেইক আইডি ধরা পড়েনি",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "কেউ অন্যের ছবি, ছেলে হয়ে মেয়ের নাম/ছবি, মেয়ে হয়ে ছেলের নাম/ছবি বা ৩টির বেশি আইডি খোলার চেষ্টা করলে অটো-ব্লক হয়ে এখানে তালিকায় জমা হবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredReports, key = { it.id }) { report ->
                FakeIdAttemptCard(
                    report = report,
                    onPermanentBan = { onPermanentBan(report) },
                    onDelete = { onDeleteReport(report) }
                )
            }
        }
    }
}

@Composable
private fun FakeIdAttemptCard(
    report: FakeIdReport,
    onPermanentBan: () -> Unit,
    onDelete: () -> Unit
) {
    val violationLabel = when (report.violationType) {
        "MALE_TO_FEMALE_FAKE" -> "ছেলে হয়ে মেয়ের নাম/ছবি (ফেইক)"
        "FEMALE_TO_MALE_FAKE" -> "মেয়ে হয়ে ছেলের নাম/ছবি (ফেইক)"
        "STOLEN_PHOTO" -> "অন্যের ছবি চুরি করে ফেইক আইডি"
        "LIMIT_3_ACCOUNTS" -> "৩+ আইডি খোলার চেষ্টা"
        else -> "ফেইক আইডি নীতি লঙ্ঘন"
    }
    val isPermBanned = report.status == "PERMANENT_BANNED"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.2.dp,
            if (isPermBanned) Color(0xFFB71C1C) else Color(0xFFEF5350).copy(alpha = 0.6f)
        ),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    if (report.attemptedAvatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = report.attemptedAvatarUrl,
                            contentDescription = report.attemptedName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = report.attemptedName,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.attemptedName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${report.attemptedUsername.removePrefix("@")} • ${report.attemptedEmail}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "লিঙ্গ নির্বাচন: ${report.selectedGender} • সময়: ${report.timestamp}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (isPermBanned) Color(0xFF880E4F) else Color(0xFFD32F2F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isPermBanned) "স্থায়ী নিষিদ্ধ ⛔" else "অটো-ব্লকড 🚫",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "শনাক্তকৃত ধরন: $violationLabel",
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (report.detectedSignal.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "সিগন্যাল: ${report.detectedSignal}",
                            color = Color(0xFFC62828),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (report.matchedExistingUser.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "আসল ছবির মালিক: ${report.matchedExistingUser}",
                            color = Color(0xFF1565C0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.violationDetails,
                        color = Color(0xFF3E2723),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ডিভাইস: ${report.deviceModel}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isPermBanned) {
                    Button(
                        onClick = onPermanentBan,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "স্থায়ী নিষিদ্ধ করুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "লগ মুছুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}




