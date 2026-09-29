package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Translations
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun FaceFeedBottomNavBar(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val activeTab by viewModel.activeTab.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        // 1. Feed
        NavigationBarItem(
            selected = activeTab == AppTab.HOME,
            onClick = { viewModel.setTab(AppTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Feed"
                )
            },
            label = { Text("Feed", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 2. Chats
        NavigationBarItem(
            selected = activeTab == AppTab.MESSAGES,
            onClick = { viewModel.setTab(AppTab.MESSAGES) },
            icon = {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (activeTab == AppTab.MESSAGES) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chats"
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(BangladeshRed)
                    )
                }
            },
            label = { Text("Chats", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 3. Watch
        NavigationBarItem(
            selected = activeTab == AppTab.VIDEOS,
            onClick = { viewModel.setTab(AppTab.VIDEOS) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppTab.VIDEOS) Icons.Filled.OndemandVideo else Icons.Outlined.OndemandVideo,
                    contentDescription = "Watch"
                )
            },
            label = { Text("Watch", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 4. Reels
        NavigationBarItem(
            selected = activeTab == AppTab.REELS,
            onClick = { viewModel.setTab(AppTab.REELS) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppTab.REELS) Icons.Filled.Movie else Icons.Outlined.Movie,
                    contentDescription = "Reels"
                )
            },
            label = { Text("Reels", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 5. Groups
        NavigationBarItem(
            selected = activeTab == AppTab.GROUPS,
            onClick = { viewModel.setTab(AppTab.GROUPS) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppTab.GROUPS) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Groups"
                )
            },
            label = { Text("Groups", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 6. Friends
        NavigationBarItem(
            selected = activeTab == AppTab.FRIENDS,
            onClick = { viewModel.setTab(AppTab.FRIENDS) },
            icon = {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (activeTab == AppTab.FRIENDS) Icons.Filled.People else Icons.Outlined.People,
                        contentDescription = "Friends"
                    )
                    if (pendingRequests.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-2).dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BangladeshRed)
                        )
                    }
                }
            },
            label = { Text("Friends", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 7. Alerts
        NavigationBarItem(
            selected = activeTab == AppTab.NOTIFICATIONS,
            onClick = { viewModel.setTab(AppTab.NOTIFICATIONS) },
            icon = {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (activeTab == AppTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Alerts"
                    )
                    if (unreadNotifs > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-2).dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BangladeshRed)
                        )
                    }
                }
            },
            label = { Text("Alerts", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )

        // 8. Menu
        NavigationBarItem(
            selected = activeTab == AppTab.MENU,
            onClick = { viewModel.setTab(AppTab.MENU) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppTab.MENU) Icons.Filled.Menu else Icons.Outlined.Menu,
                    contentDescription = "Menu"
                )
            },
            label = { Text("Menu", fontSize = 10.sp, maxLines = 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FacebookBlue,
                selectedTextColor = FacebookBlue,
                indicatorColor = FacebookBlue.copy(alpha = 0.12f)
            )
        )
    }
}
