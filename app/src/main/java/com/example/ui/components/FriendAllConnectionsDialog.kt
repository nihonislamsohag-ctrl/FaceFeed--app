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

    // Sample list of connections for this user
    val allFriendsList = remember {
        listOf(
            ProfileFriend("f_1", "Rahim Aly", "rahim_aly", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "65 mutual friends", isOnline = true),
            ProfileFriend("f_2", "Joy Ahmed", "joy_ahmed", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "135 mutual friends", isOnline = true),
            ProfileFriend("f_3", "Yousuf Farazi", "yousuf_farazi", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", "12 mutual friends", isOnline = false),
            ProfileFriend("f_4", "হাসান মাহমুদ সাইফুল্লাহ", "hasan_mahmud_s", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200", "161 mutual friends", isOnline = true),
            ProfileFriend("f_5", "Mohammad Ali", "mohammad_ali", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200", "42 mutual friends", isOnline = true),
            ProfileFriend("f_6", "Lamia Islam", "lamia_islam", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "18 mutual friends", isOnline = true),
            ProfileFriend("f_7", "Niyon Islam Sohag", "niyon_sohag", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "FotoFilm Founder • 250 mutual friends", isOnline = true),
            ProfileFriend("f_8", "Tanvir Ahmed", "tanvir_ahmed", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "34 mutual friends", isOnline = true),
            ProfileFriend("f_9", "Sadia Afrin", "sadia_afrin", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", "29 mutual friends", isOnline = false),
            ProfileFriend("f_10", "MD Samrat", "md_samrat", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=200", "88 mutual friends", isOnline = true)
        )
    }

    val mutualFriendsList = remember {
        allFriendsList.filter { it.mutualFriendsCount.contains("mutual") }
    }

    val followersList = remember {
        listOf(
            ProfileFriend("fl_1", "Farhana Akter", "farhana_a", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200", "Followed since Jan 2026", isOnline = true),
            ProfileFriend("fl_2", "Kamrul Hassan", "kamrul_h", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200", "Followed since Feb 2026", isOnline = true),
            ProfileFriend("fl_3", "Rifat Hossain", "rifat_h", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200", "Followed recently", isOnline = false),
            ProfileFriend("fl_4", "Mehedi Hasan", "mehedi_hasan", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "Followed since Mar 2026", isOnline = true),
            ProfileFriend("fl_5", "Nusrat Jahan", "nusrat_j", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Followed recently", isOnline = true)
        )
    }

    val followingList = remember {
        listOf(
            ProfileFriend("fg_1", "FotoFilm Official", "fotofilm_corp", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200", "Verified Page • 5.2M Followers", isOnline = true),
            ProfileFriend("fg_2", "Niyon Islam Sohag", "niyon_sohag", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Founder & CEO, FotoFilm", isOnline = true),
            ProfileFriend("fg_3", "Bangladesh Tech Community", "bd_tech", "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200", "Public Group • 45.8K Members", isOnline = true),
            ProfileFriend("fg_4", "Barisal Photographers Club", "barisal_photo", "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=200", "Community Page", isOnline = false)
        )
    }

    val tabs = listOf(
        "সব ফ্রেন্ড (6,420)",
        "মিউচুয়াল (12)",
        "ফলোয়ার্স (12.5K)",
        "ফলোয়িং (340)"
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
                                                    Text("Friends", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
