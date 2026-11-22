package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.ProfileFriend
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen

/**
 * Dialog showing All Friends, Mutual Friends, Followers, and Following
 * Opened when user clicks "See all" next to Friends on Friend Profile Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendAllConnectionsDialog(
    user: User,
    onDismiss: () -> Unit,
    onMessageUser: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: All Friends, 1: Mutual Friends, 2: Followers, 3: Following
    var searchQuery by remember { mutableStateOf("") }

    val allFriendsList = remember { emptyList<ProfileFriend>() }

    val mutualFriendsList = remember { emptyList<ProfileFriend>() }

    val followersList = remember { emptyList<ProfileFriend>() }

    val followingList = remember { emptyList<ProfileFriend>() }

    val tabs = listOf(
        "সব ফ্রেন্ড (${allFriendsList.size})",
        "মিউচুয়াল (${mutualFriendsList.size})",
        "ফলোয়ার্স (${followersList.size})",
        "ফলোয়িং (${followingList.size})"
    )

    val currentDisplayList = when (selectedTab) {
        0 -> allFriendsList
        1 -> mutualFriendsList
        2 -> followersList
        else -> followingList
    }.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Top Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ফ্রেন্ড, ফলোয়ার্স ও ফলোয়িং তালিকা",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ফ্রেন্ড বা ফলোয়ার খুঁজুন...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        // Scrollable Tab Row
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 12.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Friends & Followers List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (currentDisplayList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো প্রোফাইল পাওয়া যায়নি",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    items(currentDisplayList) { friend ->
                        var isFriendStatus by remember { mutableStateOf(true) }
                        var isFollowingStatus by remember { mutableStateOf(true) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar with online indicator
                                Box {
                                    AsyncImage(
                                        model = friend.avatarUrl,
                                        contentDescription = friend.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                    if (friend.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .background(OnlineGreen, CircleShape)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Name & mutual friends
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = friend.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "@${friend.username}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = friend.mutualFriendsCount,
                                        fontSize = 11.sp,
                                        color = FacebookBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Action Buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when (selectedTab) {
                                        2 -> {
                                            // Followers tab -> Follow Back button
                                            Button(
                                                onClick = {
                                                    isFollowingStatus = !isFollowingStatus
                                                    Toast.makeText(context, if (isFollowingStatus) "${friend.name}-কে ফলো করা হয়েছে" else "আনফলো করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(18.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isFollowingStatus) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isFollowingStatus) "Following ✔" else "Follow Back",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isFollowingStatus) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                                )
                                            }
                                        }
                                        3 -> {
                                            // Following tab -> Following toggle button
                                            OutlinedButton(
                                                onClick = {
                                                    isFollowingStatus = !isFollowingStatus
                                                    Toast.makeText(context, if (isFollowingStatus) "ফলোয়িং যুক্ত" else "আনফলো সম্পন্ন", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(18.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isFollowingStatus) "Following ✔" else "Follow",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        else -> {
                                            // All Friends / Mutual tab -> Friends button
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.clickable {
                                                    Toast.makeText(context, "${friend.name} আপনার ফ্রেন্ড লিস্টে আছেন", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = BangladeshGreen)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Message button
                                    IconButton(
                                        onClick = {
                                            onDismiss()
                                            onMessageUser(friend.name)
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(FacebookBlue.copy(alpha = 0.12f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = "Message",
                                            tint = FacebookBlue,
                                            modifier = Modifier.size(17.dp)
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
