package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.model.Translations
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun FriendsScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val suggestedUsers by viewModel.suggestedUsers.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Title & Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.friends,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                IconButton(onClick = { /* Search */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search Friends")
                }
            }

            // 5000 Limit Info Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (currentUser.isMaxFriendsReached) BangladeshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentUser.isMaxFriendsReached) Icons.Default.Verified else Icons.Default.People,
                            contentDescription = null,
                            tint = if (currentUser.isMaxFriendsReached) BangladeshGreen else FacebookBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currentUser.friendCount} / 5,000 ${strings.friends}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentUser.isMaxFriendsReached)
                            "${strings.friendLimitNotice}: ${strings.autoFollowActive}"
                        else
                            "৫,০০০ বন্ধু সম্পন্ন হলে স্বয়ংক্রিয়ভাবে ফলো বাটন চালু হবে।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Friend Requests Section
            if (pendingRequests.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.friendRequests} (${pendingRequests.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }

        // Friend Requests List
        items(pendingRequests) { reqUser ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = reqUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                        contentDescription = reqUser.fullName,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reqUser.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${reqUser.friendCount} ${strings.friends}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Confirm & Delete Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.acceptFriendRequest(reqUser) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                            ) {
                                Text(strings.confirm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.declineFriendRequest(reqUser) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(strings.delete, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // People You May Know / Suggested Friends Section
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = strings.peopleYouMayKnow,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(suggestedUsers) { user ->
            SuggestedFriendRow(
                user = user,
                strings = strings,
                onAction = { viewModel.sendFriendRequestOrFollow(user) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SuggestedFriendRow(
    user: User,
    strings: com.example.model.AppTranslations,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = user.fullName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (user.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = FacebookBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Show friend count or 5000+ notice
                Text(
                    text = if (user.isMaxFriendsReached)
                        "5,000+ ${strings.friends} • ${user.followerCount} ${strings.followers}"
                    else
                        "${user.friendCount} ${strings.friends}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Auto-switch between "Add Friend" vs "Follow / Following" based on 5000 friend limit!
                if (user.isMaxFriendsReached) {
                    // Over 5000: Displays Follow / Following button!
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else BangladeshGreen
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.isFollowing) strings.following else strings.follow,
                                color = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Under 5000: Displays Add Friend / Request Sent button!
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.hasSentFriendRequest) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.hasSentFriendRequest) Icons.Default.HourglassTop else Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (user.hasSentFriendRequest) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.hasSentFriendRequest) "রিকোয়েস্ট পাঠানো হয়েছে" else strings.addFriend,
                                color = if (user.hasSentFriendRequest) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
