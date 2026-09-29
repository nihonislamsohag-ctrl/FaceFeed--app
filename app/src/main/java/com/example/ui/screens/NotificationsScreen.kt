package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun NotificationsScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.notifications,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Notification Settings")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications) { notif ->
                NotificationRow(
                    notification = notif,
                    onClick = { viewModel.markNotificationAsRead(notif.id) }
                )
            }
        }
    }
}

@Composable
fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (!notification.isRead) FacebookBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Actor avatar with type badge overlay
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = notification.actor.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                contentDescription = notification.actor.fullName,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Small badge icon
            val (badgeIcon, badgeColor) = when (notification.type) {
                NotificationType.LIKE -> Icons.Default.Favorite to BangladeshRed
                NotificationType.COMMENT -> Icons.Default.ChatBubble to BangladeshGreen
                NotificationType.FRIEND_REQUEST -> Icons.Default.PersonAdd to FacebookBlue
                NotificationType.GROUP_INVITE -> Icons.Default.Group to FacebookBlue
                NotificationType.PAGE_LIKE -> Icons.Default.ThumbUp to FacebookBlue
                NotificationType.LIVE_STREAM -> Icons.Default.Videocam to BangladeshRed
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.timeAgo,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
