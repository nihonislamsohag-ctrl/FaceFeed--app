package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.FakeIdDetector
import com.example.util.PrivacyPolicyHelper
import com.example.viewmodel.AppTab
import com.example.model.AppLanguage
import com.example.model.User
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.UserAccount
import com.example.ui.components.SixSimFreeDataDialog
import com.example.ui.components.ProLevelCutAppDialog
import com.example.ui.components.FlowerRoseAppDialog
import com.example.ui.components.FlowerLotusAppDialog
import com.example.ui.components.FaceAdAdminDialog
import com.example.ui.components.ProfessionalDashboardDialog
import com.example.ui.components.SavedVideosDialog
import com.example.ui.components.FriendsListDialog
import com.example.ui.components.BirthdaysDialog
import com.example.ui.components.MarketplaceDialog
import com.example.ui.components.MemoriesDialog
import com.example.ui.components.EventsDialog
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.components.SupportMessageDialog

@Composable
fun MenuScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isOwnerSession by viewModel.isOwnerSession.collectAsState()
    val accounts by viewModel.userAccounts.collectAsState()
    val isVerified by viewModel.isFidaVerified.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isNiyonOwner = isLoggedIn && isOwnerSession && viewModel.isCurrentUserNiyonIslamSohag()
    val context = LocalContext.current

    var showSwitchAccountDropdown by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showSettingsSection by remember { mutableStateOf(false) }
    var showHelpSection by remember { mutableStateOf(false) }
    var showDashboardSnackbar by remember { mutableStateOf<String?>(null) }
    var showBoostManagerDialog by remember { mutableStateOf(false) }
    var showSixSimDialog by remember { mutableStateOf(false) }
    var showProLevelCutDialog by remember { mutableStateOf(false) }
    var showFlowerRoseDialog by remember { mutableStateOf(false) }
    var showFlowerLotusDialog by remember { mutableStateOf(false) }
    var showFaceAdAdminDialog by remember { mutableStateOf(false) }

    // User requested feature dialog states
    var showProfessionalDashboardDialog by remember { mutableStateOf(false) }
    var showSavedVideosDialog by remember { mutableStateOf(false) }
    var showFriendsListDialog by remember { mutableStateOf(false) }
    var showBirthdaysDialog by remember { mutableStateOf(false) }
    var showMarketplaceDialog by remember { mutableStateOf(false) }
    var showMemoriesDialog by remember { mutableStateOf(false) }
    var showEventsDialog by remember { mutableStateOf(false) }
    var showSupportMessageDialog by remember { mutableStateOf(false) }

    // 6 Settings & Privacy Features Dialog States
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDeviceRequestsDialog by remember { mutableStateOf(false) }
    var showRecentAdsDialog by remember { mutableStateOf(false) }
    var showOrdersPaymentsDialog by remember { mutableStateOf(false) }
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showOwnerQuickUnlockDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top Bar: Back Button, "Menu", Switch Profile Icon, Search Icon
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setTab(AppTab.HOME) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Feed",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        @OptIn(ExperimentalFoundationApi::class)
                        Text(
                            text = "Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        if (isNiyonOwner) viewModel.openRightManagerFida()
                                        else showOwnerQuickUnlockDialog = true
                                    }
                                )
                        )

                        // Switch Profile Shortcut Icon (⇄)
                        IconButton(onClick = { showSwitchAccountDropdown = !showSwitchAccountDropdown }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Account",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Search Icon (🔍)
                        IconButton(onClick = { viewModel.openUniversalSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Profile Card (matching user screenshot top card)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clickable { viewModel.setTab(AppTab.PROFILE) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = currentUser.avatarUrl,
                                contentDescription = currentUser.fullName,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (isVerified) {
                                FidaVerifiedBadge(size = 16.dp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    FidaVerifiedBadge(size = 16.dp)
                                }
                            }
                            Text(
                                text = "View your profile",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "View Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Switch Account Card (matching user screenshot 2nd card)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSwitchAccountDropdown = !showSwitchAccountDropdown }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stacked dual avatars badge
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .offset(x = (-4).dp, y = (-2).dp)
                                        .clip(CircleShape)
                                        .background(FacebookBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .offset(x = 6.dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(BangladeshGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Switch account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "১ ব্যক্তি সর্বোচ্চ ৩টি আইডি খুলতে পারবেন (${accounts.size}/৩টি)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (accounts.size >= FakeIdDetector.MAX_ACCOUNTS_PER_PERSON) Color(0xFFD32F2F) else BangladeshGreen
                                )
                            }

                            Icon(
                                imageVector = if (showSwitchAccountDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Accounts",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Expanded Multi-Account List
                        AnimatedVisibility(
                            visible = showSwitchAccountDropdown,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                accounts.forEach { account ->
                                    AccountItemRow(
                                        account = account,
                                        onSwitch = {
                                            viewModel.switchAccount(account.id)
                                            showDashboardSnackbar = "${account.name} প্রোফাইলে সুইচ করা হয়েছে 🔄"
                                        }
                                    )
                                }

                                val isLimitReached = accounts.size >= FakeIdDetector.MAX_ACCOUNTS_PER_PERSON
                                if (isLimitReached) {
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "⛔ ১ ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (৩/৩ আইডি পূর্ণ হয়েছে)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB71C1C),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                // + Add account option inside the dropdown
                                Surface(
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isLimitReached) {
                                                showDashboardSnackbar = "⛔ ১ জন ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (৩/৩ পূর্ণ হয়েছে)"
                                            } else {
                                                showAddAccountDialog = true
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isLimitReached) Color.Red.copy(alpha = 0.12f)
                                                    else FacebookBlue.copy(alpha = 0.12f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isLimitReached) Icons.Default.Block else Icons.Default.Add,
                                                contentDescription = "Add Account",
                                                tint = if (isLimitReached) Color.Red else FacebookBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = if (isLimitReached) "সর্বোচ্চ ৩টি আইডি পূর্ণ (${accounts.size}/৩)" else "+ আরেকটি আইডি যোগ করুন (${accounts.size}/৩)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isLimitReached) Color.Red else FacebookBlue
                                            )
                                            Text(
                                                text = "অন্যের ছবি বা বিপরীত লিঙ্গের নামে আইডি অটো-ব্লক হবে",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Invite Friends Card (matching user screenshot)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clickable {
                            showDashboardSnackbar = "FotoFilm ইনভাইট লিঙ্ক কপি হয়েছে! বন্ধুদের সাথে শেয়ার করুন ❤️"
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "❤️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Invite friends",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Shortcuts 2-Column Grid
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    @OptIn(ExperimentalFoundationApi::class)
                    Text(
                        text = "সব ফিচারসমূহ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (isNiyonOwner) viewModel.openRightManagerFida()
                                else showOwnerQuickUnlockDialog = true
                            }
                        )
                    )
                    if (isNiyonOwner) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFE4EC),
                            border = BorderStroke(1.dp, Color(0xFFFF4081)),
                            modifier = Modifier.clickable { viewModel.openRightManagerFida() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFlorist,
                                    contentDescription = "Right Manager Fida",
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Right Manager Fida 🔒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2185B)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Film Dashboard, ChatFilm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Dashboard",
                            icon = Icons.Default.BarChart,
                            iconColor = Color(0xFF00B2FE),
                            showVerifiedBadge = true,
                            onClick = { showProfessionalDashboardDialog = true }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "ChatFilm",
                            icon = Icons.Default.ChatBubble,
                            iconColor = Color(0xFF006AFF),
                            showVerifiedBadge = true,
                            onClick = { viewModel.setTab(AppTab.MESSAGES) }
                        )
                    }

                    // Row 2: Film Group, Shopping Mall
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Group",
                            icon = Icons.Default.Groups,
                            iconColor = Color(0xFF00C853),
                            showVerifiedBadge = true,
                            onClick = { viewModel.openGroupsTab() }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Shopping Mall",
                            icon = Icons.Default.Storefront,
                            iconColor = Color(0xFF00B0FF),
                            showVerifiedBadge = true,
                            onClick = { showMarketplaceDialog = true }
                        )
                    }

                    // Row 3: Friends, Short Video
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Friend",
                            icon = Icons.Default.People,
                            iconColor = Color(0xFF0091EA),
                            showVerifiedBadge = true,
                            onClick = { showFriendsListDialog = true }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Short Video",
                            icon = Icons.Default.Movie,
                            iconColor = Color(0xFFFF4081),
                            showVerifiedBadge = true,
                            onClick = { viewModel.setTab(AppTab.REELS) }
                        )
                    }

                    // Row 4: Fida AI, Film Page
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Fida AI",
                            icon = Icons.Default.AutoAwesome,
                            iconColor = Color(0xFF8A2BE2),
                            isGradient = true,
                            badge = "নতুন",
                            showVerifiedBadge = true,
                            onClick = { viewModel.openFidaAI() }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Page",
                            icon = Icons.Default.Flag,
                            iconColor = Color(0xFFFF9800),
                            showVerifiedBadge = true,
                            onClick = { viewModel.openPagesTab() }
                        )
                    }

                    // Row 5: Film Save, Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Save",
                            icon = Icons.Default.Bookmark,
                            iconColor = Color(0xFF9C27B0),
                            showVerifiedBadge = true,
                            onClick = { showSavedVideosDialog = true }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Menu",
                            icon = Icons.Default.History,
                            iconColor = Color(0xFF00ACC1),
                            showVerifiedBadge = true,
                            onClick = { showMemoriesDialog = true }
                        )
                    }

                    // Row 6: Jonmodin, Film Event
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Jonmodin",
                            icon = Icons.Default.CardGiftcard,
                            iconColor = Color(0xFF00B0FF),
                            showVerifiedBadge = true,
                            onClick = { showBirthdaysDialog = true }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Film Event",
                            icon = Icons.Default.Event,
                            iconColor = Color(0xFFE91E63),
                            showVerifiedBadge = true,
                            onClick = { showEventsDialog = true }
                        )
                    }

                    // Row 7: Fida Verified, Hum
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Fida Verified",
                            icon = Icons.Default.Verified,
                            iconColor = BangladeshGreen,
                            badge = if (isVerified) "সক্রিয় ✅" else "পান",
                            showVerifiedBadge = true,
                            onClick = { viewModel.openFidaVerified() }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Hum",
                            icon = Icons.Default.DynamicFeed,
                            iconColor = Color(0xFFFF5722),
                            showVerifiedBadge = true,
                            onClick = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    // Right Manager Fida (Flower Logo 🌸 - Exclusively for Niyon Islam Sohag only!)
                    if (isNiyonOwner) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShortcutGridItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Right Manager Fida",
                                icon = Icons.Default.LocalFlorist,
                                iconColor = Color(0xFFFF4081),
                                badge = "🔒 ২৮টি ফিচার + পিন তালা",
                                showVerifiedBadge = true,
                                onClick = { viewModel.openRightManagerFida() }
                            )
                        }
                    }

                    // =========================================================================
                    // নতুন যুক্ত ফিচারসমূহ:
                    // ১. টাওয়ার লোগো - ৬ টি সিম ধারা ফ্রীতে এমবি না থাকলে বেসিক ডাটা
                    // ২. ProLevelCut অ্যাপ - ঝামেলা ছাড়া ফ্রী ভিডিও এডিটর ও ডাউনলোড লিঙ্ক
                    // ৩. GramFilm অ্যাপ (সবুজ ও লাল রং এবং বিশেষ লোগো)
                    // ৪. অন্য ফুলের লোগো অ্যাপ ২ - Shapla & Lotus 🪷
                    // =========================================================================
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "নতুন বিশেষ অ্যাপ ও ফিচারসমূহ 🌟",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    // Feature 1: টাওয়ার ছবি লোগো (৬ টি সিম ধারা ফ্রীতে এমবি না থাকলে বেসিক মোড ডাটা)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = "৬ সিমে ফ্রী বেসিক ডাটা (এমবি ছাড়া)",
                            icon = Icons.Default.CellTower,
                            iconColor = BangladeshGreen,
                            isGradient = true,
                            badge = "📶 ৬ সিম চালু ⚡",
                            showVerifiedBadge = true,
                            onClick = { showSixSimDialog = true }
                        )
                    }

                    // Feature 2: ProLevelCut অ্যাপ (লোগো, ঝামেলা ছাড়া ফ্রী ভিডিও এডিটর ও ডাউনলোড লিঙ্ক)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = "ProLevelCut ভিডিও এডিটর",
                            icon = Icons.Default.MovieFilter,
                            iconColor = Color(0xFF00C853),
                            isGradient = true,
                            badge = "🎬 ফ্রী এডিট ও APK",
                            showVerifiedBadge = true,
                            onClick = { showProLevelCutDialog = true }
                        )
                    }

                    // Feature 3 & 4: GramFilm (সবুজ ও লাল ব্র্যান্ডিং এবং দ্বিতীয় আপলোডকৃত লোগো) & Shapla & Lotus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GramFilmShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            onClick = { showFlowerRoseDialog = true }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Shapla & Lotus 🪷",
                            icon = Icons.Default.Spa,
                            iconColor = Color(0xFF00ACC1),
                            badge = "ফুল অ্যাপ ২",
                            showVerifiedBadge = true,
                            onClick = { showFlowerLotusDialog = true }
                        )
                    }
                }
            }

            // Bottom Section: Settings & Privacy, Help & Support, Add Account, Log Out
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Column {
                        // 1. Settings & privacy
                        MenuListItem(
                            icon = Icons.Default.Settings,
                            title = "Settings & privacy",
                            hasDropdown = true,
                            isExpanded = showSettingsSection,
                            onClick = { showSettingsSection = !showSettingsSection }
                        )
                        if (showSettingsSection) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                    .padding(vertical = 4.dp)
                            ) {
                                // ১. Settings (সেটিংস)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.Settings,
                                    title = "Settings (সেটিংস)",
                                    subtitle = "অ্যাকাউন্ট সেন্টার, পাসওয়ার্ড ও গোপনীয়তা",
                                    onClick = { showSettingsDialog = true }
                                )

                                // ২. Device requests (ডিভাইস রিকোয়েস্ট)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.PhoneAndroid,
                                    title = "Device requests (ডিভাইস রিকোয়েস্ট)",
                                    subtitle = "স্মার্ট টিভি ও ব্রাউজার লগইন অনুমোদন",
                                    onClick = { showDeviceRequestsDialog = true }
                                )

                                // ৩. Recent ad activity (সাম্প্রতিক অ্যাড অ্যাক্টিভিটি)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.Campaign,
                                    title = "Recent ad activity (সাম্প্রতিক বিজ্ঞাপন)",
                                    subtitle = "সম্প্রতি দেখা বিজ্ঞাপন ও সেভ করা অফার",
                                    onClick = { showRecentAdsDialog = true }
                                )

                                // ৪. Orders and payments (অর্ডার ও পেমেন্ট)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.AccountBalanceWallet,
                                    title = "Orders and payments (অর্ডার ও পেমেন্ট)",
                                    subtitle = "FotoFilm Pay, বিকাশ/নগদ ও ট্রানজ্যাকশন",
                                    onClick = { showOrdersPaymentsDialog = true }
                                )

                                // ৫. Dark mode (ডার্ক মোড)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.DarkMode,
                                    title = "Dark mode (ডার্ক মোড)",
                                    subtitle = if (isDarkMode) "বর্তমানে চালু (On) 🌙" else "বর্তমানে বন্ধ (Off) ☀️",
                                    onClick = { showDarkModeDialog = true }
                                )

                                // ৬. Language (ভাষা)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.Language,
                                    title = "Language (ভাষা)",
                                    subtitle = "${currentLanguage.displayName} (${currentLanguage.flag})",
                                    onClick = { showLanguageDialog = true }
                                )

                                // ৭. Privacy Policy (ব্রাউজারে খুলবে)
                                SettingsPrivacySubItem(
                                    icon = Icons.Default.PrivacyTip,
                                    title = "Privacy Policy",
                                    subtitle = "অফিশিয়াল প্রাইভেসি পলিসি ব্রাউজারে খুলুন",
                                    onClick = { PrivacyPolicyHelper.openInBrowser(context) }
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 2. Help & support
                        MenuListItem(
                            icon = Icons.Default.HelpOutline,
                            title = "Help & support",
                            hasDropdown = true,
                            isExpanded = showHelpSection,
                            onClick = { showHelpSection = !showHelpSection }
                        )
                        if (showHelpSection) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            ) {
                                Text("• FotoFilm হেল্প সেন্টার", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "• 🎧 সাপোর্ট টিমে মেসেজ ও ৫টি ছবি/স্ক্রিনশট পাঠান (ছাপোড)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BangladeshGreen,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clickable { showSupportMessageDialog = true }
                                )
                                Text("• সমস্যা জন্য রিপোর্ট করুন", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("• Fida AI সহায়তা নিন", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp).clickable { viewModel.openFidaAI() })
                                Text("• 🌐 গুগল ও ক্রোমে ব্যবহার / ডাউনলোড হাব", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FacebookBlue, modifier = Modifier.padding(vertical = 4.dp).clickable { viewModel.openAppDownloadWebHub() })
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 3. Add account
                        MenuListItem(
                            icon = Icons.Default.PersonAdd,
                            title = "Add account (${accounts.size}/৩)",
                            onClick = {
                                if (accounts.size >= FakeIdDetector.MAX_ACCOUNTS_PER_PERSON) {
                                    showDashboardSnackbar = "⛔ ১ জন ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (৩/৩ পূর্ণ হয়েছে)"
                                } else {
                                    showAddAccountDialog = true
                                }
                            }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 4. Privacy Policy (Direct Option in Menu)
                        MenuListItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            onClick = { PrivacyPolicyHelper.openInBrowser(context) }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 5. Log out
                        MenuListItem(
                            icon = Icons.Default.ExitToApp,
                            title = "Log out",
                            onClick = { viewModel.logout() }
                        )
                    }
                }
            }

            // Footer branding: FaceFeed (replacing Facebook)
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Foto",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = BangladeshGreen
                        )
                        Text(
                            text = "Film",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = BangladeshRed
                        )
                    }
                    Text(
                        text = "Version 2.4.0 • Made with ❤️ for Bangladesh 🇧🇩",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Feedback Snackbar
        if (showDashboardSnackbar != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { showDashboardSnackbar = null }) {
                        Text("ঠিক আছে", color = Color.White)
                    }
                }
            ) {
                Text(showDashboardSnackbar!!)
            }
        }

        // Add Account Dialog
        if (showAddAccountDialog) {
            AddAccountDialog(
                onDismiss = { showAddAccountDialog = false },
                onCreateAttempt = { name, handle, gender, avatarUrl ->
                    val (success, errorMsg) = viewModel.addNewAccount(
                        name = name,
                        handle = handle,
                        gender = gender,
                        avatarUrl = avatarUrl,
                        context = context
                    )
                    if (success) {
                        showDashboardSnackbar = "নতুন আইডি '$name' সফলভাবে যুক্ত হয়েছে! Switch account-এ দেখতে পাবেন 🌟"
                        showAddAccountDialog = false
                        showSwitchAccountDropdown = true
                        null
                    } else {
                        errorMsg ?: "⛔ আইডি তৈরি করা যায়নি!"
                    }
                }
            )
        }

        // Boost & Ads Manager Dialog
        if (showBoostManagerDialog) {
            com.example.ui.components.BoostManagerDialog(
                viewModel = viewModel,
                onDismiss = { showBoostManagerDialog = false }
            )
        }

        // ৬ সিম ফ্রী ডাটা ডায়ালগ (এমবি ছাড়া বেসিক মোড)
        if (showSixSimDialog) {
            SixSimFreeDataDialog(
                viewModel = viewModel,
                onDismiss = { showSixSimDialog = false }
            )
        }

        // ProLevelCut ভিডিও এডিটর অ্যাপ ডায়ালগ (ডাউনলোড লিঙ্ক ও ফিচার)
        if (showProLevelCutDialog) {
            ProLevelCutAppDialog(
                onDismiss = { showProLevelCutDialog = false }
            )
        }

        // ফুলের অ্যাপ ১ ডায়ালগ (Rose Bloom)
        if (showFlowerRoseDialog) {
            FlowerRoseAppDialog(
                onDismiss = { showFlowerRoseDialog = false }
            )
        }

        // Face AD এডমিন (Ads Manager • AdMob • Audio Music Player)
        if (showFaceAdAdminDialog) {
            FaceAdAdminDialog(
                viewModel = viewModel,
                onDismiss = { showFaceAdAdminDialog = false }
            )
        }

        // ==========================================
        // USER REQUESTED FEATURE MODALS & DIALOGS
        // ==========================================

        // 1. Professional Dashboard (Screenshot 2)
        if (showProfessionalDashboardDialog) {
            ProfessionalDashboardDialog(
                onDismiss = { showProfessionalDashboardDialog = false }
            )
        }

        // 2. Saved Videos & Posts ("Saved জারা যে কোনো ভিডিও ছেইভ করবে সেই ভিডিও এখানে এসে জমা হবে আর দেখা জাবে")
        if (showSavedVideosDialog) {
            SavedVideosDialog(
                viewModel = viewModel,
                onDismiss = { showSavedVideosDialog = false }
            )
        }

        // 3. Friends List ("Friends এর বিতরে জারা ফ্রেন্ড আছে তাদের আইডি প্রোফাইল আইকন সহো এখানে দেখা জাবে")
        if (showFriendsListDialog) {
            FriendsListDialog(
                viewModel = viewModel,
                onDismiss = { showFriendsListDialog = false }
            )
        }

        // 4. Birthdays ("Birthdays এর বিতরে যারা ভাডে উইস করবে এখানে দেখাজাবে")
        if (showBirthdaysDialog) {
            BirthdaysDialog(
                viewModel = viewModel,
                onDismiss = { showBirthdaysDialog = false }
            )
        }

        // 5. Marketplace
        if (showMarketplaceDialog) {
            MarketplaceDialog(
                viewModel = viewModel,
                onDismiss = { showMarketplaceDialog = false }
            )
        }

        // 6. Memories ("On this day" স্মৃতিচারণ)
        if (showMemoriesDialog) {
            MemoriesDialog(
                viewModel = viewModel,
                onDismiss = { showMemoriesDialog = false }
            )
        }

        // 7. Events (আসন্ন ইভেন্টসমূহ)
        if (showEventsDialog) {
            EventsDialog(
                viewModel = viewModel,
                onDismiss = { showEventsDialog = false }
            )
        }

        // ৮. সাপোর্ট টিম মেসেজ ও ৫টি স্ক্রিনশট সাবমিশন ডায়ালগ (ছাপোড)
        if (showSupportMessageDialog) {
            SupportMessageDialog(
                viewModel = viewModel,
                onDismiss = { showSupportMessageDialog = false }
            )
        }

        if (showOwnerQuickUnlockDialog) {
            var secretInput by remember { mutableStateOf("") }
            var secretError by remember { mutableStateOf<String?>(null) }
            AlertDialog(
                onDismissRequest = { showOwnerQuickUnlockDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text("মালিকানা যাচাই (Niyon Islam Sohag)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "আপনার গোপন পিন অথবা মালিকানা পাসওয়ার্ড দিয়ে যাচাই করুন:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = secretInput,
                            onValueChange = {
                                secretInput = it
                                secretError = null
                            },
                            label = { Text("গোপন পিন / পাসওয়ার্ড") },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        secretError?.let { err ->
                            Text(text = err, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (viewModel.verifyAndActivateOwnerSession(secretInput, context)) {
                                showOwnerQuickUnlockDialog = false
                                showDashboardSnackbar = "স্বাগতম Niyon Islam Sohag 🌸 Right Manager Fida সক্রিয় হয়েছে!"
                            } else {
                                secretError = "❌ ভুল পিন বা পাসওয়ার্ড!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("যাচাই ও চালু করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOwnerQuickUnlockDialog = false }) {
                        Text("বাতিল")
                    }
                }
            )
        }

        // ==========================================
        // 6 SETTINGS & PRIVACY SUB-DIALOGS
        // ==========================================

        // ১. সেটিংস ও প্রাইভেসি সেন্টার
        if (showSettingsDialog) {
            SettingsDetailModalDialog(
                user = currentUser,
                viewModel = viewModel,
                onDismiss = { showSettingsDialog = false }
            )
        }

        // ২. ডিভাইস রিকোয়েস্ট
        if (showDeviceRequestsDialog) {
            DeviceRequestsModalDialog(
                onDismiss = { showDeviceRequestsDialog = false }
            )
        }

        // ৩. সাম্প্রতিক বিজ্ঞাপন
        if (showRecentAdsDialog) {
            RecentAdActivityModalDialog(
                onDismiss = { showRecentAdsDialog = false }
            )
        }

        // ৪. অর্ডার ও পেমেন্ট
        if (showOrdersPaymentsDialog) {
            OrdersAndPaymentsModalDialog(
                onDismiss = { showOrdersPaymentsDialog = false }
            )
        }

        // ৫. ডার্ক মোড
        if (showDarkModeDialog) {
            DarkModeSelectorModalDialog(
                isDarkMode = isDarkMode,
                onSetDarkMode = { enabled ->
                    viewModel.setDarkMode(enabled)
                    showDarkModeDialog = false
                    showDashboardSnackbar = if (enabled) "ডার্ক মোড চালু করা হয়েছে! 🌙" else "লাইট মোড চালু করা হয়েছে! ☀️"
                },
                onDismiss = { showDarkModeDialog = false }
            )
        }

        // ৬. ভাষা নির্বাচন
        if (showLanguageDialog) {
            LanguageSelectorModalDialog(
                currentLanguage = currentLanguage,
                onSelectLanguage = { lang ->
                    viewModel.setLanguage(lang)
                    showLanguageDialog = false
                    showDashboardSnackbar = "অ্যাপের ভাষা পরিবর্তিত হয়েছে: ${lang.displayName} ${lang.flag}"
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

@Composable
private fun AccountItemRow(
    account: UserAccount,
    onSwitch: () -> Unit
) {
    Surface(
        color = if (account.isActive) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitch() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = account.avatarUrl,
                contentDescription = account.name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = account.handle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (account.isActive) {
                Surface(
                    color = BangladeshGreen,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "সক্রিয় ✓",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onSwitch,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("সুইচ করুন", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ShortcutGridItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isGradient: Boolean = false,
    badge: String? = null,
    showVerifiedBadge: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (isGradient) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8A2BE2), Color(0xFF00B2FE), Color(0xFF00E676))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (badge != null) {
                    Surface(
                        color = if (badge.contains("সক্রিয়")) BangladeshGreen else Color(0xFFE91E63),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (showVerifiedBadge) {
                    Spacer(modifier = Modifier.width(4.dp))
                    FidaVerifiedBadge(size = 14.dp)
                }
            }
        }
    }
}

@Composable
private fun GramFilmShortcutGridItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // The uploaded GramFilm logo image (R.drawable.ic_gramfilm_logo)
                Image(
                    painter = painterResource(id = R.drawable.ic_gramfilm_logo),
                    contentDescription = "GramFilm Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    color = BangladeshGreen,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "গ্রামবাংলা 🌿",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            // Green & Red colored title "GramFilm" + Blue Verified Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Gram",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = BangladeshGreen
                )
                Text(
                    text = "Film",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = BangladeshRed
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "🎬",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                FidaVerifiedBadge(size = 14.dp)
            }
        }
    }
}

@Composable
private fun MenuListItem(
    icon: ImageVector,
    title: String,
    hasDropdown: Boolean = false,
    isExpanded: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (hasDropdown) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onCreateAttempt: (name: String, handle: String, gender: String, avatarUrl: String) -> String?
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("পুরুষ") }
    var avatarUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUrl = uri.toString()
            errorMessage = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(FacebookBlue.copy(alpha = 0.12f))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "নির্বাচিত প্রোফাইল ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "নিজের ছবি যুক্ত করুন",
                            tint = FacebookBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(
                        text = if (avatarUrl.isBlank()) "নিজের ছবি আপলোড করুন (ঐচ্ছিক)" else "অন্য ছবি পরিবর্তন করুন",
                        fontSize = 12.sp,
                        color = FacebookBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "নতুন FotoFilm আইডি যোগ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "১ ব্যক্তি সর্বোচ্চ ৩টি আইডি • অন্যের ছবি বা বিপরীত লিঙ্গের নামে ফেইক আইডি ব্লক হবে",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("আইডির পুরো নাম *") },
                    placeholder = { Text("আপনার আসল নাম লিখুন") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = handle,
                    onValueChange = {
                        handle = it
                        errorMessage = null
                    },
                    label = { Text("ইউজারনেম / হ্যান্ডেল") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("পুরুষ" to "পুরুষ (ছেলে)", "নারী" to "নারী (মেয়ে)").forEach { (value, label) ->
                        val isSelected = selectedGender == value
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable {
                                    selectedGender = value
                                    errorMessage = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFEF5350)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = Color(0xFFB71C1C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val err = onCreateAttempt(name.trim(), handle.trim(), selectedGender, avatarUrl)
                                if (err != null) {
                                    errorMessage = err
                                }
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                    ) {
                        Text("আইডি তৈরি করুন")
                    }
                }
            }
        }
    }
}

/**
 * Settings & Privacy Sub-Item row
 */
@Composable
private fun SettingsPrivacySubItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FacebookBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = FacebookBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * ১. Settings (সেটিংস ও প্রাইভেসি সেন্টার)
 */
@Composable
private fun SettingsDetailModalDialog(
    user: User,
    viewModel: FaceFeedViewModel? = null,
    onDismiss: () -> Unit
) {
    val blockedUserIds by (viewModel?.blockedUserIds?.collectAsState() ?: remember { mutableStateOf(emptySet()) })
    val conversations by (viewModel?.conversations?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val blockedConversations = remember(conversations, blockedUserIds) {
        conversations.filter { it.isBlocked || blockedUserIds.contains(it.friend.id) }
    }

    var profileLockActive by remember { mutableStateOf(true) }
    var postPrivacyPublic by remember { mutableStateOf(true) }
    var twoFactorActive by remember { mutableStateOf(true) }
    var notificationSound by remember { mutableStateOf(true) }
    var savedSuccess by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteStatusMsg by remember { mutableStateOf<String?>(null) }
    val settingsContext = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Settings & privacy", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("অ্যাকাউন্ট ও নিরাপত্তা সেটিংস", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Meta Account Center Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FacebookBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("FotoFilm", fontWeight = FontWeight.Black, fontSize = 14.sp, color = FacebookBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accounts Center", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("নাম: ${user.fullName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("ইউজারনেম: ${user.username.ifEmpty { "@niyonislamsohag" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ব্যক্তিগত বিবরণ, পাসওয়ার্ড ও নিরাপত্তা নিয়ন্ত্রণ এক ছাদের নিচে।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Security Section
                    Text("নিরাপত্তা ও লগইন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("দ্বি-স্তরীয় যাচাইকরণ (2FA)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("অ্যাকাউন্টে লগইন করতে ওটিপি কোড বাধ্যতামূলক", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = twoFactorActive,
                            onCheckedChange = { twoFactorActive = it }
                        )
                    }

                    // Privacy Section
                    Text("প্রাইভেসি ও প্রোফাইল সুরক্ষা:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("প্রোফাইল লকিং (Lock Profile)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("অপরিচিতদের কাছ থেকে ছবি ও পোস্ট সুরক্ষিত রাখুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profileLockActive,
                            onCheckedChange = { profileLockActive = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ডিফল্ট পোস্ট অডিয়েন্স: Public", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(if (postPrivacyPublic) "সবাই আপনার পোস্ট দেখতে পারবে (Public)" else "শুধুমাত্র ফ্রেন্ডরা দেখতে পারবে (Friends only)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = postPrivacyPublic,
                            onCheckedChange = { postPrivacyPublic = it }
                        )
                    }

                    // Preferences
                    Text("পছন্দ ও অ্যালার্ট:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ইন-অ্যাপ নোটিফিকেশন সাউন্ড", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("লাইক, কমেন্ট ও মেসেজে অডিও টিউন বাজবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notificationSound,
                            onCheckedChange = { notificationSound = it }
                        )
                    }

                    // Blocking Management Section
                    Text("ব্লকিং ও নিরাপত্তা:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ব্লক করা তালিকা (${blockedConversations.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("কাউকে ব্লক করলে সে আপনার পোস্ট দেখতে বা মেসেজ দিতে পারবে না।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (blockedConversations.isEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("বর্তমানে কোনো অ্যাকাউন্ট ব্লক করা নেই", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                blockedConversations.forEach { conv ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(conv.friend.fullName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        TextButton(onClick = { viewModel?.unblockConversation(conv.id) }) {
                                            Text("Unblock", color = FacebookBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Play Store Compliance: Privacy Policy & Account Deletion
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("আইনি সুরক্ষা ও ডাটা নিয়ন্ত্রণ (Play Store):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { PrivacyPolicyHelper.openInBrowser(settingsContext) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_privacy_policy_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📜 Privacy Policy", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🗑️ অ্যাকাউন্ট ডিলিট", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (deleteStatusMsg != null) {
                    Text(
                        text = deleteStatusMsg!!,
                        color = BangladeshGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (savedSuccess) {
                    Text(
                        text = "সেটিংস সফলভাবে সংরক্ষিত হয়েছে! ✅",
                        color = BangladeshGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { savedSuccess = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("📜 FotoFilm প্রাইভেসি পলিসি ও শর্তাবলী", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "১. ডাটা সংগ্রহ ও সুরক্ষা: Google Sign-In, Firebase Auth ও Firestore Zero-Trust রুলসের মাধ্যমে আপনার প্রোফাইল ও মেসেজ সুরক্ষিত থাকে।",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "২. মিডিয়া ও অনুমতি (Permissions): শুধুমাত্র আপনার অনুমতিক্রমেই ক্যামেরা/মাইক্রোফোন (ChatFilm অডিও ও ভিডিও কল এবং আড্ডা রুমের জন্য) এবং Android Photo Picker ব্যবহার করে ছবি/ভিডিও আপলোড করা হয়।",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "৩. অটো কম্প্রেশন ও ক্লাউড: আপলোডের আগে ছবি ও ভিডিও কম্প্রেস করে Cloudinary ও Firebase-এ নিরাপদে সংরক্ষণ করা হয়।",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "৪. বিজ্ঞাপন (AdMob): বিনামূল্যে সেবা চালু রাখতে Google AdMob-এর নীতিমালা অনুযায়ী বিজ্ঞাপন দেখানো হয়।",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "৫. অ্যাকাউন্ট মুছে ফেলা: আপনি যেকোনো সময় সেটিংস থেকে 'অ্যাকাউন্ট ডিলিট' অপশনে গিয়ে আপনার সব ক্লাউড প্রোফাইল ও ডাটা স্থায়ীভাবে মুছে ফেলতে পারেন।",
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { PrivacyPolicyHelper.openInBrowser(settingsContext) }) {
                    Text("ব্রাউজারে খুলুন")
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("⚠️ অ্যাকাউন্ট স্থায়ীভাবে ডিলিট করবেন?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red) },
            text = {
                Text(
                    text = "আপনি নিশ্চিত থাকলে 'ডিলিট নিশ্চিত করুন' চাপুন। এতে Firebase ক্লাউড থেকে আপনার প্রোফাইল ও ব্যক্তিগত তথ্য মুছে যাবে এবং অ্যাকাউন্ট সাইন-আউট হবে।",
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel?.deleteCurrentUserAccount { msg ->
                            deleteStatusMsg = msg
                        } ?: run {
                            deleteStatusMsg = "অ্যাকাউন্ট রিকোয়েস্ট সম্পন্ন হয়েছে ✓"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ডিলিট নিশ্চিত করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * ২. Device requests (ডিভাইস রিকোয়েস্ট)
 */
@Composable
private fun DeviceRequestsModalDialog(
    onDismiss: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var approvalMessage by remember { mutableStateOf<String?>(null) }
    var loggedOutAll by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Device requests", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("স্মার্ট টিভি ও ব্রাউজার লগইন অনুমোদন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "আপনার স্মার্ট টিভি বা কম্পিউটারের স্ক্রিনে প্রদর্শিত ৮ সংখ্যার কোডটি এখানে লিখুন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if (it.length <= 8) codeInput = it.uppercase() },
                        placeholder = { Text("উদা: 7B3X-9K2L") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (codeInput.isNotBlank()) {
                                approvalMessage = "ডিভাইস অনুমোদন সফল হয়েছে! টিভিতে লগইন হয়েছে ✅"
                                codeInput = ""
                            }
                        },
                        enabled = codeInput.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("অনুমোদন")
                    }
                }

                approvalMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, fontSize = 12.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("সক্রিয় লগইন সেশন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (!loggedOutAll) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Samsung Galaxy S24 Ultra • ঢাকা", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("বর্তমান ডিভাইস • সক্রিয় সেশন ✅", fontSize = 10.sp, color = BangladeshGreen)
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Google Chrome (Windows) • বগুড়া", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("গতকাল সক্রিয় ছিল • IP: 103.145.xxx.xx", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { loggedOutAll = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("অন্যান্য সকল ডিভাইস থেকে লগআউট করুন", fontSize = 12.sp)
                    }
                } else {
                    Text("অন্য সকল ডিভাইস সফলভাবে লগআউট করা হয়েছে! 🛡️", color = BangladeshGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * ৩. Recent ad activity (সাম্প্রতিক অ্যাড অ্যাক্টিভিটি)
 */
@Composable
private fun RecentAdActivityModalDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recent ad activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("সাম্প্রতিক দেখা ও ক্লিক করা বিজ্ঞাপন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "FotoFilm-এ সম্প্রতি আপনি যেসব বিজ্ঞাপনের সাথে মিথস্ক্রিয়া করেছেন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "সাম্প্রতিক কোনো বিজ্ঞাপন অ্যাক্টিভিটি নেই।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে")
                }
            }
        }
    }
}

/**
 * ৪. Orders and payments (অর্ডার ও পেমেন্ট)
 */
@Composable
private fun OrdersAndPaymentsModalDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Orders and payments", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("FotoFilm Pay / ওয়ালেট ও পেমেন্ট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A237E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("FotoFilm Pay ওয়ালেট ব্যালেন্স", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("৳ ১,৪৫০.০০", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE91E63),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("bKash লিঙ্কড ✅", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Nagad লিঙ্কড ✅", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("সাম্প্রতিক লেনদেনের বিবরণ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    Triple("Fida Verified ব্লু ব্যাজ", "০৫ অক্টোবর • bKash • ৳ ৭০০", BangladeshGreen),
                    Triple("পোস্ট ফ্রী বুস্ট প্রমোশন ক্যাম্পেইন", "০২ অক্টোবর • Founder Quota • ৳ ০", FacebookBlue),
                    Triple("গিফট স্টার প্যাক (১০০ Stars)", "২৮ সেপ্টেম্বর • Nagad • ৳ ১২০", BangladeshGreen)
                ).forEach { (title, subtitle, statusColor) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("সফল ✅", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সম্পন্ন")
                }
            }
        }
    }
}

/**
 * ৫. Dark mode (ডার্ক মোড)
 */
@Composable
private fun DarkModeSelectorModalDialog(
    isDarkMode: Boolean,
    onSetDarkMode: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableIntStateOf(if (isDarkMode) 1 else 0) } // 0: Off, 1: On, 2: System

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text("Dark mode (ডার্ক মোড)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "অ্যাপের থিম নির্বাচন করুন। ডার্ক মোড রাতে ব্যবহারের সময় চোখের ক্লান্তি কমায় এবং ব্যাটারি বাঁচায়।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: On
                Surface(
                    onClick = { selectedOption = 1 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedOption == 1) FacebookBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (selectedOption == 1) BorderStroke(1.dp, FacebookBlue) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == 1, onClick = { selectedOption = 1 })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("চালু (On) 🌙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("গাঢ় ডার্ক থিম প্রযোজ্য হবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Option 2: Off
                Surface(
                    onClick = { selectedOption = 0 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedOption == 0) FacebookBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (selectedOption == 0) BorderStroke(1.dp, FacebookBlue) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == 0, onClick = { selectedOption = 0 })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("বন্ধ (Off) ☀️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("স্বাভাবিক লাইট থিম প্রযোজ্য হবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Option 3: System
                Surface(
                    onClick = { selectedOption = 2 },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedOption == 2) FacebookBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (selectedOption == 2) BorderStroke(1.dp, FacebookBlue) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == 2, onClick = { selectedOption = 2 })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("সিস্টেম ডিফল্ট (Use system settings) ⚙️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("ডিভাইসের সেটিংস অনুসারে স্বয়ংক্রিয়ভাবে পরিবর্তিত হবে", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSetDarkMode(selectedOption == 1)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("প্রয়োগ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

/**
 * ৬. Language (ভাষা নির্বাচন)
 */
@Composable
private fun LanguageSelectorModalDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLang by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BangladeshGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text("Language (ভাষা নির্বাচন)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FotoFilm অ্যাপের জন্য আপনার পছন্দের ভাষা নির্বাচন করুন:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AppLanguage.entries.forEach { lang ->
                    Surface(
                        onClick = { selectedLang = lang },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedLang == lang) BangladeshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (selectedLang == lang) BorderStroke(1.dp, BangladeshGreen) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLang == lang,
                                onClick = { selectedLang = lang }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang.flag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = lang.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = lang.code.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectLanguage(selectedLang) },
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
            ) {
                Text("ভাষা পরিবর্তন করুন", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
