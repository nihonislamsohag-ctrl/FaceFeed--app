package com.example.ui.components

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceFeedTopBar(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val strings = Translations.get(currentLang)
    val languageStatus by viewModel.languageStatus.collectAsState()
    var showLangMenu by remember { mutableStateOf(false) }
    var showSearchField by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Logo, Brand "FaceFeed", (+), Search, Menu, Language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.setTab(AppTab.HOME) }
                ) {
                    // FaceFeed Emblem (Squircle matching user's logo)
                    Image(
                        painter = painterResource(id = R.drawable.facefeed_logo_1789791030932),
                        contentDescription = "FaceFeed Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "FaceFeed",
                        color = FacebookBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 23.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Action Icons: (+) Add, (🔍) Search, (☰) Menu, [EN] Language
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Add post button
                    IconButton(
                        onClick = { viewModel.setShowCreatePostDialog(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Post",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Search button (Universal Search for all topics)
                    IconButton(
                        onClick = { viewModel.openUniversalSearch() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Universal Search",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 3-Dots / Menu button (opens full Facebook-style Menu with Switch account, Fida AI, Fida Verified)
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.MENU) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "FaceFeed Menu",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Language Switcher Pill (EN / বাংলা)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.clickable { showLangMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = currentLang.shortName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                                    text = "🌐 ভাষা নির্বাচন ও অন/অফ কন্ট্রোল",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            AppLanguage.values().forEach { lang ->
                                val isLangOn = languageStatus[lang] ?: true
                                val isSelected = lang == currentLang
                                DropdownMenuItem(
                                    enabled = isLangOn,
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = lang.flag, fontSize = 20.sp)
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = lang.displayName,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isLangOn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                                        )
                                                        if (isSelected) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("✓ বর্তমান", fontSize = 10.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Text(
                                                        text = if (isLangOn) "🟢 অন (সক্রিয় ভাষা)" else "🔒 বন্ধ (লকড - শুধুমাত্র Niyon Islam Sohag চালু করতে পারবেন)",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isLangOn) BangladeshGreen else BangladeshRed
                                                    )
                                                }
                                            }
                                            if (!isLangOn) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = BangladeshRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (isLangOn) {
                                            viewModel.setLanguage(lang)
                                            showLangMenu = false
                                        }
                                    }
                                )
                            }
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

            // Row 2: Iconic 6 Top Navigation Tabs with Notification Badges & Blue Underline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home (15+)
                TopNavTabItem(
                    icon = Icons.Filled.Home,
                    isSelected = activeTab == AppTab.HOME,
                    badgeText = "15+",
                    onClick = { viewModel.setTab(AppTab.HOME) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Friends (7)
                TopNavTabItem(
                    icon = Icons.Default.PeopleOutline,
                    isSelected = activeTab == AppTab.FRIENDS,
                    badgeText = "7",
                    onClick = { viewModel.setTab(AppTab.FRIENDS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 3: Messenger (3)
                TopNavTabItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    isSelected = activeTab == AppTab.MESSAGES,
                    badgeText = "3",
                    onClick = { viewModel.setTab(AppTab.MESSAGES) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Watch / Video (15+)
                TopNavTabItem(
                    icon = Icons.Default.OndemandVideo,
                    isSelected = activeTab == AppTab.VIDEOS || activeTab == AppTab.REELS,
                    badgeText = "15+",
                    onClick = { viewModel.setTab(AppTab.VIDEOS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 5: Alerts / Bell (2)
                TopNavTabItem(
                    icon = Icons.Default.NotificationsNone,
                    isSelected = activeTab == AppTab.NOTIFICATIONS,
                    badgeText = "2",
                    onClick = { viewModel.setTab(AppTab.NOTIFICATIONS) },
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

                        // Bottom Indicator
                        if (activeTab == AppTab.PROFILE) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(FacebookBlue)
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
}

@Composable
private fun TopNavTabItem(
    icon: ImageVector,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
                    tint = if (isSelected) FacebookBlue else Color(0xFF65676B),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(2.dp)
                )

                if (badgeText != null) {
                    Surface(
                        color = Color(0xFFE41E3F),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.offset(x = 10.dp, y = (-4).dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                        )
                    }
                }
            }

            // Bottom Blue Indicator Line
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(FacebookBlue)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
