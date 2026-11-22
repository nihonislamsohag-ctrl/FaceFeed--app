package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.model.Translations
import com.example.model.User
import com.example.ui.components.FotoFilmAvatar
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel

enum class NotificationFilterTab {
    ALL, UNREAD, LIKES, COMMENTS, SHARES, FRIEND_REQUESTS, GROUPS, PAGES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val notifications by viewModel.notifications.collectAsState()

    var selectedFilter by remember { mutableStateOf(NotificationFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTaxDialog by remember { mutableStateOf(false) }
    var selectedNotifForMenu by remember { mutableStateOf<AppNotification?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Toast auto-clear
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2500)
            toastMessage = null
        }
    }

    // Filter notifications
    val filteredList = remember(notifications, selectedFilter, searchQuery) {
        var list = notifications
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.actor.fullName.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedFilter) {
            NotificationFilterTab.ALL -> list
            NotificationFilterTab.UNREAD -> list.filter { !it.isRead }
            NotificationFilterTab.LIKES -> list.filter {
                it.type == NotificationType.LIKE || it.type == NotificationType.STORY_REACTION
            }
            NotificationFilterTab.COMMENTS -> list.filter { it.type == NotificationType.COMMENT }
            NotificationFilterTab.SHARES -> list.filter { it.type == NotificationType.SHARE }
            NotificationFilterTab.FRIEND_REQUESTS -> list.filter { it.type == NotificationType.FRIEND_REQUEST }
            NotificationFilterTab.GROUPS -> list.filter { it.type == NotificationType.GROUP_INVITE }
            NotificationFilterTab.PAGES -> list.filter {
                it.type == NotificationType.PAGE_LIKE || it.type == NotificationType.PAGE_POST
            }
        }
    }

    val newNotifications = remember(filteredList) { filteredList.filter { it.isNewSection } }
    val earlierNotifications = remember(filteredList) { filteredList.filter { !it.isNewSection } }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    BackHandler {
        when {
            selectedNotifForMenu != null -> selectedNotifForMenu = null
            showSettingsDialog -> showSettingsDialog = false
            showTaxDialog -> showTaxDialog = false
            isSearchActive -> {
                searchQuery = ""
                isSearchActive = false
            }
            else -> viewModel.setTab(AppTab.HOME)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header: Facebook style "Notifications" + Search + Mark all as read + Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search notifications...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search", modifier = Modifier.size(18.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.setTab(AppTab.HOME) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("notifications_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "বের হোন / ব্যাকে যান",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.notifications,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = BangladeshRed
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Search Button
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search notifications",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Mark All as Read Button
                        IconButton(
                            onClick = {
                                viewModel.markAllNotificationsAsRead()
                                toastMessage = "All notifications marked as read ✓"
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = FacebookBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Notification Settings",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // Filter Tabs: Pill style chips [ All ] [ Unread ] [ Friend Requests ] [ Groups ] [ Reactions ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationTabChip(
                    label = "All (সব)",
                    count = notifications.size,
                    isSelected = selectedFilter == NotificationFilterTab.ALL,
                    onClick = { selectedFilter = NotificationFilterTab.ALL }
                )
                NotificationTabChip(
                    label = "Unread (অপঠিত)",
                    count = unreadCount,
                    isSelected = selectedFilter == NotificationFilterTab.UNREAD,
                    onClick = { selectedFilter = NotificationFilterTab.UNREAD }
                )
                NotificationTabChip(
                    label = "Likes (লাইক)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.LIKES,
                    onClick = { selectedFilter = NotificationFilterTab.LIKES }
                )
                NotificationTabChip(
                    label = "Comments (কমেন্ট)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.COMMENTS,
                    onClick = { selectedFilter = NotificationFilterTab.COMMENTS }
                )
                NotificationTabChip(
                    label = "Shares (শেয়ার)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.SHARES,
                    onClick = { selectedFilter = NotificationFilterTab.SHARES }
                )
                NotificationTabChip(
                    label = "Friend Requests (ফ্রেন্ড রিকোয়েস্ট)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.FRIEND_REQUESTS,
                    onClick = { selectedFilter = NotificationFilterTab.FRIEND_REQUESTS }
                )
                NotificationTabChip(
                    label = "Groups (গ্রুপ)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.GROUPS,
                    onClick = { selectedFilter = NotificationFilterTab.GROUPS }
                )
                NotificationTabChip(
                    label = "Pages (পেইজ)",
                    count = null,
                    isSelected = selectedFilter == NotificationFilterTab.PAGES,
                    onClick = { selectedFilter = NotificationFilterTab.PAGES }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 6.dp)
            )

            // Notifications List: Grouped into "New" and "Earlier" sections
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notifications found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "When you have new alerts, they will appear here.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // NEW Section
                    if (newNotifications.isNotEmpty()) {
                        item(key = "section_header_new") {
                            SectionHeader(title = "New", count = newNotifications.size)
                        }
                        items(newNotifications, key = { it.id }) { notif ->
                            NotificationCardItem(
                                notification = notif,
                                viewModel = viewModel,
                                onOpenTaxDialog = { showTaxDialog = true },
                                onMenuClick = { selectedNotifForMenu = notif },
                                onShowToast = { toastMessage = it }
                            )
                        }
                    }

                    // EARLIER Section
                    if (earlierNotifications.isNotEmpty()) {
                        item(key = "section_header_earlier") {
                            SectionHeader(title = "Earlier", count = earlierNotifications.size)
                        }
                        items(earlierNotifications, key = { it.id }) { notif ->
                            NotificationCardItem(
                                notification = notif,
                                viewModel = viewModel,
                                onOpenTaxDialog = { showTaxDialog = true },
                                onMenuClick = { selectedNotifForMenu = notif },
                                onShowToast = { toastMessage = it }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Toast Feedback
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

    // 3-Dots Action BottomSheet/Dialog
    selectedNotifForMenu?.let { notif ->
        NotificationOptionsDialog(
            notification = notif,
            onDismiss = { selectedNotifForMenu = null },
            onToggleRead = {
                viewModel.toggleNotificationRead(notif.id)
                selectedNotifForMenu = null
                toastMessage = if (notif.isRead) "Marked as unread" else "Marked as read"
            },
            onDelete = {
                viewModel.deleteNotification(notif.id)
                selectedNotifForMenu = null
                toastMessage = "Notification removed"
            },
            onTurnOff = {
                viewModel.deleteNotification(notif.id)
                selectedNotifForMenu = null
                toastMessage = "Notifications like this turned off"
            }
        )
    }

    // Notification Settings Dialog
    if (showSettingsDialog) {
        NotificationSettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Tax Information Dialog
    if (showTaxDialog) {
        TaxInfoDialog(
            onDismiss = { showTaxDialog = false },
            onSubmit = {
                viewModel.markNotificationAsRead("notif_2")
                showTaxDialog = false
                toastMessage = "Tax profile submitted & verified ✓"
            }
        )
    }
}

@Composable
fun NotificationTabChip(
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotificationCardItem(
    notification: AppNotification,
    viewModel: FaceFeedViewModel,
    onOpenTaxDialog: () -> Unit,
    onMenuClick: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.markNotificationAsRead(notification.id)
                when (notification.type) {
                    NotificationType.TAX_INFO -> onOpenTaxDialog()
                    NotificationType.FRIEND_REQUEST -> viewModel.setTab(AppTab.FRIENDS)
                    NotificationType.GROUP_INVITE -> viewModel.setTab(AppTab.GROUPS)
                    NotificationType.PAGE_LIKE, NotificationType.PAGE_POST -> viewModel.setTab(AppTab.GROUPS)
                    NotificationType.APP_UPDATE -> viewModel.openAppUpdateNoticeDialog()
                    else -> viewModel.setTab(AppTab.HOME)
                }
            }
            .background(
                if (!notification.isRead) FacebookBlue.copy(alpha = 0.08f) else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Actor avatar with small badge overlay
        Box(contentAlignment = Alignment.BottomEnd) {
            FotoFilmAvatar(
                imageUrl = notification.actor.avatarUrl,
                name = notification.actor.fullName,
                contentDescription = notification.actor.fullName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.openUserProfile(notification.actor) }
            )

            // Overlaid badge matching Screenshot 2
            val (badgeIcon, badgeBg) = getNotificationBadgeStyle(notification)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content Column
        Column(modifier = Modifier.weight(1f)) {
            // Title Text
            Text(
                text = notification.title,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Optional App Update Banner
            notification.bannerUrl?.let { banner ->
                if (banner.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AsyncImage(
                        model = banner,
                        contentDescription = "Update Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time Ago
            Text(
                text = notification.timeAgo,
                fontSize = 12.sp,
                color = if (!notification.isRead) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Normal
            )

            // Interactive Action Buttons
            when (notification.type) {
                NotificationType.GROUP_INVITE -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (notification.isGroupJoined) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OnlineGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Joined ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnlineGreen)
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.joinGroupNotification(notification.id)
                                    onShowToast("You joined the group ⭐ Star sender")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Join", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.declineGroupNotification(notification.id)
                                    onShowToast("Invitation declined")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Decline", fontSize = 13.sp)
                            }
                        }
                    }
                }

                NotificationType.FRIEND_REQUEST -> {
                    if (notification.hasActionButtons) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (notification.isFriendAccepted) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = OnlineGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Friends now ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnlineGreen)
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.acceptFriendRequestNotification(notification.id)
                                        onShowToast("Friend request accepted ✓")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("Confirm", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.declineFriendRequestNotification(notification.id)
                                        onShowToast("Friend request deleted")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Delete", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                NotificationType.TAX_INFO -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenTaxDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete Tax Info", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                NotificationType.BIRTHDAY -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.wishHappyBirthdayNotification(notification.id, notification.actor)
                            onShowToast("Happy birthday wish sent to ${notification.actor.fullName}! 🎂🎉")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Wish Happy Birthday 🎂", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                else -> { /* No inline action buttons */ }
            }
        }

        // Right side: Unread blue dot & 3-dots menu button
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FacebookBlue)
                )
            }
        }
    }
}

fun getNotificationBadgeStyle(notification: AppNotification): Pair<ImageVector, Color> {
    return when (notification.subBadgeType) {
        "heart" -> Icons.Default.Favorite to BangladeshRed
        "tax" -> Icons.Default.AccountBalance to Color(0xFF1976D2)
        "group" -> Icons.Default.Group to FacebookBlue
        "comment" -> Icons.AutoMirrored.Filled.Comment to BangladeshGreen
        "friend_accept", "friend_request" -> Icons.Default.PersonAdd to FacebookBlue
        "like" -> Icons.Default.ThumbUp to FacebookBlue
        "share" -> Icons.Default.Share to FacebookBlue
        "page" -> Icons.Default.Flag to Color(0xFFE91E63)
        "birthday" -> Icons.Default.Cake to Color(0xFFF57C00)
        "live" -> Icons.Default.Videocam to BangladeshRed
        else -> when (notification.type) {
            NotificationType.LIKE -> Icons.Default.ThumbUp to FacebookBlue
            NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Comment to BangladeshGreen
            NotificationType.SHARE -> Icons.Default.Share to FacebookBlue
            NotificationType.FRIEND_REQUEST -> Icons.Default.PersonAdd to FacebookBlue
            NotificationType.GROUP_INVITE -> Icons.Default.Group to FacebookBlue
            NotificationType.PAGE_LIKE, NotificationType.PAGE_POST -> Icons.Default.Flag to Color(0xFFE91E63)
            NotificationType.LIVE_STREAM -> Icons.Default.Videocam to BangladeshRed
            NotificationType.APP_UPDATE -> Icons.Default.NotificationsActive to Color(0xFFE91E63)
            NotificationType.TAX_INFO -> Icons.Default.AccountBalance to Color(0xFF1976D2)
            NotificationType.STORY_REACTION -> Icons.Default.Favorite to BangladeshRed
            NotificationType.BIRTHDAY -> Icons.Default.Cake to Color(0xFFF57C00)
        }
    }
}

@Composable
fun NotificationOptionsDialog(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onToggleRead: () -> Unit,
    onDelete: () -> Unit,
    onTurnOff: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("notification_options_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "বের হোন / ব্যাকে যান",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                FotoFilmAvatar(
                    imageUrl = notification.actor.avatarUrl,
                    name = notification.actor.fullName,
                    contentDescription = null,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Notification Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListItem(
                    headlineContent = {
                        Text(if (notification.isRead) "Mark as unread" else "Mark as read")
                    },
                    leadingContent = {
                        Icon(
                            if (notification.isRead) Icons.Default.MarkEmailUnread else Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = FacebookBlue
                        )
                    },
                    modifier = Modifier.clickable { onToggleRead() }
                )

                ListItem(
                    headlineContent = { Text("Remove this notification") },
                    leadingContent = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = BangladeshRed)
                    },
                    modifier = Modifier.clickable { onDelete() }
                )

                ListItem(
                    headlineContent = { Text("Turn off notifications like this") },
                    leadingContent = {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onTurnOff() }
                )

                ListItem(
                    headlineContent = { Text("Report issue to Notifications team") },
                    leadingContent = {
                        Icon(Icons.Default.ReportProblem, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("notification_options_back_text_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ব্যাক / বের হোন", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var friendRequestsAlert by remember { mutableStateOf(true) }
    var commentsAlert by remember { mutableStateOf(true) }
    var birthdaysAlert by remember { mutableStateOf(true) }
    var groupsAlert by remember { mutableStateOf(true) }
    var soundVibration by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = FacebookBlue)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Notification Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                Text(
                    text = "Control what alerts and notifications you receive on FotoFilm.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingToggleRow(
                    title = "Push Notifications",
                    subtitle = "Receive live notifications on device",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )

                SettingToggleRow(
                    title = "Sound & Vibration",
                    subtitle = "Play sound when receiving alerts",
                    checked = soundVibration,
                    onCheckedChange = { soundVibration = it }
                )

                SettingToggleRow(
                    title = "Friend Requests",
                    subtitle = "When someone sends or accepts a request",
                    checked = friendRequestsAlert,
                    onCheckedChange = { friendRequestsAlert = it }
                )

                SettingToggleRow(
                    title = "Comments & Mentions",
                    subtitle = "When friends comment on your posts",
                    checked = commentsAlert,
                    onCheckedChange = { commentsAlert = it }
                )

                SettingToggleRow(
                    title = "Birthdays Reminders",
                    subtitle = "Receive daily birthday reminders for friends",
                    checked = birthdaysAlert,
                    onCheckedChange = { birthdaysAlert = it }
                )

                SettingToggleRow(
                    title = "Groups & Pages Activity",
                    subtitle = "Invites and posts from groups you belong to",
                    checked = groupsAlert,
                    onCheckedChange = { groupsAlert = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Save Preferences")
            }
        }
    )
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun TaxInfoDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var tinNumber by remember { mutableStateOf("TIN-89347291-BD") }
    var bankAccountName by remember { mutableStateOf("Niyon Islam Sohag") }
    var bankName by remember { mutableStateOf("Islami Bank Bangladesh PLC") }
    var accountNumber by remember { mutableStateOf("20502140200874100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = FacebookBlue)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Payout & Tax Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Complete your official tax and payment details to receive monetization earnings on FotoFilm.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = tinNumber,
                    onValueChange = { tinNumber = it },
                    label = { Text("Taxpayer ID (TIN / NID)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bankAccountName,
                    onValueChange = { bankAccountName = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank / Mobile Financial Service") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account / Wallet Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Submit & Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}
