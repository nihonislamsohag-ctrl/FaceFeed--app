package com.example.ui.page

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SocialPage
import com.example.ui.theme.BangladeshGreen

data class PageNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconTint: Color,
    val userAvatar: String? = null,
    val isUnread: Boolean = true
)

/**
 * Requirement 8: Separate Page Notifications System
 * - Separate from personal ID notifications
 * - Notifies when: New Like, New Follow, New Comment, New Message, New Share, New Boost result
 */
@Composable
fun PageNotificationsView(
    page: SocialPage,
    customNotifications: List<String> = emptyList()
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val notifications = remember(page.id, customNotifications) {
        val list = mutableListOf<PageNotificationItem>()

        // Dynamic custom notifications only
        customNotifications.forEachIndexed { i, msg ->
            list.add(
                PageNotificationItem(
                    id = "dyn_$i",
                    title = "নতুন বার্তা / আপডেট",
                    description = msg,
                    time = "Just now",
                    icon = Icons.Default.Mail,
                    iconTint = BangladeshGreen,
                    userAvatar = page.avatarUrl.ifBlank { null }
                )
            )
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        // Notifications Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${page.name} নোটিফিকেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE53935)
                    ) {
                        Text(
                            text = "${notifications.size}টি আপডেট",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Likes", "Comments", "Messages", "Boost").forEach { f ->
                        val isSel = selectedFilter == f
                        Surface(
                            onClick = { selectedFilter = f },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) BangladeshGreen else Color(0xFFF0F2F5)
                        ) {
                            Text(
                                text = f,
                                color = if (isSel) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notifications List
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "এখনো কোনো পেইজ নোটিফিকেশন নেই।",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(notifications) { notif ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with Badge Icon
                        Box(modifier = Modifier.size(46.dp)) {
                            if (notif.userAvatar != null) {
                                AsyncImage(
                                    model = notif.userAvatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = notif.iconTint.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxSize()
                                ) {}
                            }

                            // Notification Type Badge Icon
                            Surface(
                                shape = CircleShape,
                                color = notif.iconTint,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                            ) {
                                Icon(
                                    imageVector = notif.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = notif.time,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = notif.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        if (notif.isUnread) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = BangladeshGreen,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                }
                }
            }
        }
    }
}
