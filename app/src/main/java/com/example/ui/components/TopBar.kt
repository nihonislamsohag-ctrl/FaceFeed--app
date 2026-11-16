package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppLanguage
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.SimOperator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceFeedTopBar(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isFreeMode by viewModel.isFreeMode.collectAsState()
    val currentSimOperator by viewModel.currentSimOperator.collectAsState()
    val strings = Translations.get(currentLang)
    val languageStatus by viewModel.languageStatus.collectAsState()
    var showLangMenu by remember { mutableStateOf(false) }
    var showSearchField by remember { mutableStateOf(false) }
    var showSimSelectorDialog by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    // Real-time Dynamic Badges State from ViewModel
    val homeBadgeCount by viewModel.homeBadgeCount.collectAsState()
    val friendsBadgeCount by viewModel.friendsBadgeCount.collectAsState()
    val messagesBadgeCount by viewModel.messagesBadgeCount.collectAsState()
    val hasNewMessageDot by viewModel.hasNewMessageDot.collectAsState()
    val watchBadgeCount by viewModel.watchBadgeCount.collectAsState()
    val notificationsBadgeCount by viewModel.notificationsBadgeCount.collectAsState()
    val showNotificationDropdown by viewModel.showNotificationDropdown.collectAsState()
    val isNotificationSoundEnabled by viewModel.isNotificationSoundEnabled.collectAsState()
    val isNotificationVibrationEnabled by viewModel.isNotificationVibrationEnabled.collectAsState()

    // Format badge counts dynamically according to Facebook rules
    val homeBadgeText = remember(homeBadgeCount) {
        if (homeBadgeCount <= 0) null else if (homeBadgeCount >= 15) "15+" else homeBadgeCount.toString()
    }
    val friendsBadgeText = remember(friendsBadgeCount) {
        if (friendsBadgeCount <= 0) null else if (friendsBadgeCount >= 15) "15+" else if (friendsBadgeCount > 9) "9+" else friendsBadgeCount.toString()
    }
    val messagesBadgeText = remember(messagesBadgeCount) {
        if (messagesBadgeCount <= 0) null else if (messagesBadgeCount >= 15) "15+" else if (messagesBadgeCount > 9) "9+" else messagesBadgeCount.toString()
    }
    val watchBadgeText = remember(watchBadgeCount) {
        if (watchBadgeCount <= 0) null else if (watchBadgeCount >= 15) "15+" else watchBadgeCount.toString()
    }
    val notificationsBadgeText = remember(notificationsBadgeCount) {
        if (notificationsBadgeCount <= 0) null else if (notificationsBadgeCount >= 15) "15+" else if (notificationsBadgeCount > 9) "9+" else notificationsBadgeCount.toString()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Free Data / Basic Mode Bar for BD Operators (GP, Robi, Airtel, Banglalink, Teletalk, Skitto)
            if (isFreeMode) {
                Surface(
                    color = Color(currentSimOperator.colorHex),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showSimSelectorDialog = true }
                        ) {
                            Text(text = "📶", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "০ এমবি ফ্রি ডাটা (${currentSimOperator.banglaName})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change SIM",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ছবি, চ্যাট ও পোস্ট ফ্রি ⚡",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.clickable { showSimSelectorDialog = true }
                            ) {
                                Text(
                                    text = "সিম পরিবর্তন",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Row 1: Logo, Brand "FotoFilm", (+), Search, Menu, Language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.setTab(AppTab.HOME) }
                ) {
                    // FotoFilm Emblem (Squircle matching user's Bangladesh Flag logo)
                    Image(
                        painter = painterResource(id = R.drawable.ic_fotofilm_logo),
                        contentDescription = "FotoFilm Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Foto",
                            color = BangladeshGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Film",
                            color = BangladeshRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // Action Icons: (+) Add, (🔍) Search, (⋮) 3-Dot, [EN] Language, (📶) Data Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. + আইকন (Add / Create Post)
                    IconButton(
                        onClick = { viewModel.setShowCreatePostDialog(true) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Post",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 2. সার্চ বার / সার্চ আইকন (Universal Search)
                    IconButton(
                        onClick = { viewModel.openUniversalSearch() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Universal Search",
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // 3. ৪ দেশের ভাষা বাটন (4 Countries Language Switcher - In place of download)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .height(32.dp)
                                .clickable { showLangMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = currentLang.flag, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = currentLang.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Language",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🌐 ৪ দেশের ভাষা নির্বাচন (Language)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            AppLanguage.values().forEach { lang ->
                                val isSelected = lang == currentLang
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(text = lang.flag, fontSize = 22.sp)
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = lang.displayName,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (isSelected) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("✓ সক্রিয়", fontSize = 10.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(
                                                        text = when (lang) {
                                                            AppLanguage.BENGALI -> "বাংলাদেশ (বাংলা)"
                                                            AppLanguage.ENGLISH -> "United States (English)"
                                                            AppLanguage.ARABIC -> "المملكة العربية السعودية (العربية)"
                                                            AppLanguage.HINDI -> "भारत (हिन्दी)"
                                                        },
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 4. ৩ ডট (3-Dots Menu)
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.MENU) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "FaceFeed Menu",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 5. ডাটা মোড (এমবি ডাটা / ফ্রি ডাটা) - Never wraps vertically
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isFreeMode) Color(currentSimOperator.colorHex) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { viewModel.toggleFreeMode() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isFreeMode) "⚡" else "📶", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isFreeMode) "ফ্রি" else "ডাটা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = if (isFreeMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Search Bar if toggled
            if (showSearchField) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(strings.searchHint, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Row 2: Iconic 6 Top Navigation Tabs with Notification Badges & Green Underline (matching screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home (15+ new posts)
                TopNavTabItem(
                    icon = Icons.Filled.Home,
                    isSelected = activeTab == AppTab.HOME,
                    badgeText = homeBadgeText,
                    onClick = { viewModel.setTab(AppTab.HOME) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Friends (7 pending requests)
                TopNavTabItem(
                    icon = Icons.Default.PeopleOutline,
                    isSelected = activeTab == AppTab.FRIENDS,
                    badgeText = friendsBadgeText,
                    onClick = { viewModel.setTab(AppTab.FRIENDS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 3: Messenger (3 unread chats + pulse animation)
                TopNavTabItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    isSelected = activeTab == AppTab.MESSAGES,
                    badgeText = messagesBadgeText,
                    hasPulsingDot = hasNewMessageDot && messagesBadgeText == null,
                    onClick = { viewModel.setTab(AppTab.MESSAGES) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Watch / Video (15+ unwatched videos)
                TopNavTabItem(
                    icon = Icons.Default.OndemandVideo,
                    isSelected = activeTab == AppTab.VIDEOS || activeTab == AppTab.REELS,
                    badgeText = watchBadgeText,
                    onClick = { viewModel.setTab(AppTab.VIDEOS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 5: Alerts / Bell (2 unread alerts + interactive dropdown)
                TopNavTabItem(
                    icon = Icons.Default.NotificationsNone,
                    isSelected = activeTab == AppTab.NOTIFICATIONS,
                    badgeText = notificationsBadgeText,
                    onClick = {
                        viewModel.triggerNotificationFeedback(context)
                        viewModel.toggleNotificationDropdown()
                    },
                    modifier = Modifier.weight(1f)
                )

                // Tab 6: Profile / Avatar with mini-menu badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setTab(AppTab.PROFILE) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            AsyncImage(
                                model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            // Mini 3-line hamburger icon overlay
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF242526)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }

                        // Bottom Green Indicator
                        if (activeTab == AppTab.PROFILE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(BangladeshGreen)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }

    // Bangladesh SIM Operator Selection Dialog (GP, Robi, Airtel, Banglalink, Teletalk, Skitto)
    if (showSimSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showSimSelectorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📶", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "বাংলাদেশি ফ্রি ডাটা সিম নির্বাচন করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ফোনে এমবি না থাকলেও নিচের যেকোনো সিমে আনলিমিটেড ছবি, চ্যাট লেখা, রিপ্লাই ও পোস্ট সম্পূর্ণ ফ্রিতে ব্যবহার করতে পারবেন:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SimOperator.ALL.forEach { op ->
                        val isSelected = op == currentSimOperator
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(op.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Color(op.colorHex) else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSimOperator(op)
                                    showSimSelectorDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(op.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = op.banglaName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "০ এমবি ফ্রি মোড সক্রিয় • ছবি ও চ্যাট ফ্রি",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(op.colorHex),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimSelectorDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Facebook-style Notifications Dropdown Dialog
    if (showNotificationDropdown) {
        val notifications by viewModel.notifications.collectAsState()
        val recentNotifs = remember(notifications) {
            val unread = notifications.filter { !it.isRead }
            if (unread.isNotEmpty()) unread else notifications.take(5)
        }

        AlertDialog(
            onDismissRequest = { viewModel.closeNotificationDropdown() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নোটিফিকেশনসমূহ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    TextButton(
                        onClick = { viewModel.markAllNotificationsAsRead() }
                    ) {
                        Text("সব পড়া হয়েছে", fontSize = 12.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sound & Vibration Toggles
                    Surface(
                        shape = RoundedCornerShape(10.dp),
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
                                Text(
                                    text = if (isNotificationSoundEnabled) "🔊 সাউন্ড চালু" else "🔇 সাউন্ড বন্ধ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Switch(
                                    checked = isNotificationSoundEnabled,
                                    onCheckedChange = { viewModel.toggleNotificationSound() },
                                    modifier = Modifier.scale(0.75f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isNotificationVibrationEnabled) "📳 ভাইব্রেশন" else "📳 বন্ধ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Switch(
                                    checked = isNotificationVibrationEnabled,
                                    onCheckedChange = { viewModel.toggleNotificationVibration() },
                                    modifier = Modifier.scale(0.75f)
                                )
                            }
                        }
                    }

                    Text(
                        text = "নতুন ও সাম্প্রতিক অ্যালার্ট:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Recent Notifications Preview
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentNotifs, key = { it.id }) { notif ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!notif.isRead) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.toggleNotificationRead(notif.id)
                                        viewModel.closeNotificationDropdown()
                                        viewModel.setTab(AppTab.NOTIFICATIONS)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = notif.actor.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = notif.timeAgo,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!notif.isRead) {
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeNotificationDropdown()
                        viewModel.setTab(AppTab.NOTIFICATIONS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("সম্পূর্ণ নোটিফিকেশন ট্যাব দেখুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeNotificationDropdown() }) {
                    Text("বন্ধ করুন", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun TopNavTabItem(
    icon: ImageVector,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasPulsingDot: Boolean = false
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(top = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) BangladeshGreen else Color(0xFF65676B),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(2.dp)
                )

                // Facebook-Style Red Notification Badge (#e41e3f, white text, 50% border-radius, min-width 20dp, height 20dp)
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFE41E3F),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-6).dp)
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (hasPulsingDot) {
                    // Pulsing Dot Animation for new incoming message
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotPulse"
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-3).dp)
                            .size(9.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFFE41E3F))
                    )
                }
            }

            // Bottom Green Indicator Line (matching screenshot)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(BangladeshGreen)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
