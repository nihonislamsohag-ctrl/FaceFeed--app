package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.UserAccount

@Composable
fun MenuScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val accounts by viewModel.userAccounts.collectAsState()
    val isVerified by viewModel.isFidaVerified.collectAsState()

    var showSwitchAccountDropdown by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showSettingsSection by remember { mutableStateOf(false) }
    var showHelpSection by remember { mutableStateOf(false) }
    var showDashboardSnackbar by remember { mutableStateOf<String?>(null) }

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

                        Text(
                            text = "Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
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
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(BangladeshGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Green Verified",
                                        tint = BangladeshGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
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

                            Text(
                                text = "Switch account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

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

                                // + Add account option inside the dropdown
                                Surface(
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAddAccountDialog = true }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(FacebookBlue.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Account",
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = "+ আরেকটি আইডি যোগ করুন",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = FacebookBlue
                                        )
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
                            showDashboardSnackbar = "FaceFeed ইনভাইট লিঙ্ক কপি হয়েছে! বন্ধুদের সাথে শেয়ার করুন ❤️"
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
                Text(
                    text = "সব ফিচারসমূহ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Dashboard, Messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Dashboard",
                            icon = Icons.Default.BarChart,
                            iconColor = Color(0xFF00B2FE),
                            onClick = { showDashboardSnackbar = "FaceFeed ক্রিয়েটর ড্যাশবোর্ড: ৪৭৯ পোস্ট • ৫K ফলোয়ার্স" }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Messages",
                            icon = Icons.Default.ChatBubble,
                            iconColor = Color(0xFF006AFF),
                            onClick = { viewModel.setTab(AppTab.MESSAGES) }
                        )
                    }

                    // Row 2: Groups, Marketplace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Groups",
                            icon = Icons.Default.Groups,
                            iconColor = Color(0xFF00C853),
                            onClick = { viewModel.setTab(AppTab.GROUPS) }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Marketplace",
                            icon = Icons.Default.Storefront,
                            iconColor = Color(0xFF00B0FF),
                            onClick = { showDashboardSnackbar = "FaceFeed মার্কেটপ্লেসে নতুন পণ্য ক্রয়-বিক্রয় চালু হয়েছে" }
                        )
                    }

                    // Row 3: Friends, Reels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Friends",
                            icon = Icons.Default.People,
                            iconColor = Color(0xFF0091EA),
                            onClick = { viewModel.setTab(AppTab.FRIENDS) }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Reels",
                            icon = Icons.Default.Movie,
                            iconColor = Color(0xFFFF4081),
                            onClick = { viewModel.setTab(AppTab.REELS) }
                        )
                    }

                    // Row 4: Fida AI (Replaces Meta AI!), Pages
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
                            onClick = { viewModel.openFidaAI() }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Pages",
                            icon = Icons.Default.Flag,
                            iconColor = Color(0xFFFF9800),
                            onClick = { viewModel.setTab(AppTab.GROUPS) }
                        )
                    }

                    // Row 5: Saved, Memories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Saved",
                            icon = Icons.Default.Bookmark,
                            iconColor = Color(0xFF9C27B0),
                            onClick = { showDashboardSnackbar = "সংরক্ষিত পোস্ট ও ভিডিও তালিকা সংরক্ষিত আছে" }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Memories",
                            icon = Icons.Default.History,
                            iconColor = Color(0xFF00ACC1),
                            onClick = { showDashboardSnackbar = "আজকের দিনে আপনার পূর্ববর্তী স্মরণীয় পোস্টসমূহ" }
                        )
                    }

                    // Row 6: Birthdays, Events
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Birthdays",
                            icon = Icons.Default.CardGiftcard,
                            iconColor = Color(0xFF00B0FF),
                            onClick = { showDashboardSnackbar = "আজকের বন্ধুদের জন্মদিন ও শুভেচ্ছা বার্তা" }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Events",
                            icon = Icons.Default.Event,
                            iconColor = Color(0xFFE91E63),
                            onClick = { showDashboardSnackbar = "আপনার এলাকার আসন্ন ইভেন্টসমূহ" }
                        )
                    }

                    // Row 7: Fida Verified (Green Badge! Replaces Meta Verified), Feeds
                    // (Note: Instagram Lite is completely removed!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Fida Verified",
                            icon = Icons.Default.Verified,
                            iconColor = FacebookBlue,
                            badge = if (isVerified) "সক্রিয় 🔵" else "পান",
                            onClick = { viewModel.openFidaVerified() }
                        )
                        ShortcutGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Feeds",
                            icon = Icons.Default.DynamicFeed,
                            iconColor = Color(0xFFFF5722),
                            onClick = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    // Row 8: Right Manager Fida (Flower Logo 🌸 - Exclusively for Niyon Islam Sohag only!)
                    if (viewModel.isCurrentUserNiyonIslamSohag()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShortcutGridItem(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Right Manager Fida",
                                icon = Icons.Default.LocalFlorist,
                                iconColor = Color(0xFFFF4081),
                                badge = "🌸 Admin",
                                onClick = { viewModel.openRightManagerFida() }
                            )
                        }
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                            ) {
                                Text("• সাধারণ সেটিংস ও নিরাপত্তা", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("• ডার্ক মোড ও ডিসপ্লে", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("• প্রাইভেসি শর্টকাটস", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
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
                                Text("• FaceFeed হেল্প সেন্টার", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("• সমস্যা রিপোর্ট করুন", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("• Fida AI সহায়তা নিন", fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp).clickable { viewModel.openFidaAI() })
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 3. Add account
                        MenuListItem(
                            icon = Icons.Default.PersonAdd,
                            title = "Add account",
                            onClick = { showAddAccountDialog = true }
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // 4. Log out
                        MenuListItem(
                            icon = Icons.Default.ExitToApp,
                            title = "Log out",
                            onClick = { viewModel.setShowAuthDialog(true) }
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
                    Text(
                        text = "FaceFeed",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = FacebookBlue
                    )
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
                onAccountCreated = { name, handle ->
                    viewModel.addNewAccount(name, handle)
                    showDashboardSnackbar = "নতুন আইডি '$name' সফলভাবে যুক্ত হয়েছে! Switch account-এ দেখতে পাবেন 🌟"
                    showAddAccountDialog = false
                    showSwitchAccountDropdown = true
                }
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

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
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
    onAccountCreated: (name: String, handle: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(FacebookBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = FacebookBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "নতুন FaceFeed আইডি যোগ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "একই অ্যাপে একাধিক আইডি খুলে সহজেই সুইচ করতে পারবেন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আইডির পুরো নাম") },
                    placeholder = { Text("উদা: Sohag Official") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("ইউজারনেম / হ্যান্ডেল") },
                    placeholder = { Text("উদা: @sohagofficial") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                                onAccountCreated(name.trim(), handle.trim())
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
