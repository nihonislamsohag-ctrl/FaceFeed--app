package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.Conversation
import com.example.model.ProfileFriend
import com.example.model.Translations
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun MessagesScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val conversations by viewModel.conversations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCameraDialog by remember { mutableStateOf(false) }
    var showNewMessageDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var selectedFriendForNoteReply by remember { mutableStateOf<Pair<User, String>?>(null) }

    // If an active chat is open, show the ChatDetailScreen
    if (activeChat != null) {
        ChatDetailScreen(
            conversation = activeChat!!,
            viewModel = viewModel,
            onBack = { viewModel.closeChat() }
        )
        return
    }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.friend.fullName.contains(searchQuery, ignoreCase = true) ||
            (it.groupName?.contains(searchQuery, ignoreCase = true) == true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Messenger Header: App Icon in front of "Messenger" as requested by user
        // "Messenger লেখা সামনে ফেসবুক এর ঐ ছবিটা সরিয়ে ঐ জাগায়ে আমার অ্যাপ ছবি বসিয়ে দেন, আর ঐ ফিচার গুলো ঐ ভাবে এড করে দেন আর চালু করে দেন,,"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Toggle profile or branding */ }
            ) {
                // FaceFeed App Icon placed in front of "Messenger"
                Image(
                    painter = painterResource(id = R.drawable.ic_facefeed_logo),
                    contentDescription = "FaceFeed App Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Messenger",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Camera Action
                IconButton(
                    onClick = { showCameraDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Compose New Message
                IconButton(
                    onClick = { showNewMessageDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Message",
                        modifier = Modifier.size(19.dp),
                        tint = FacebookBlue
                    )
                }

                // New Group Call / Instant Video Call
                IconButton(
                    onClick = {
                        val groupConv = conversations.firstOrNull { it.isGroup }
                        if (groupConv != null) {
                            viewModel.startGroupCall(
                                groupName = groupConv.groupName ?: strings.groupCall,
                                members = groupConv.groupMembers
                            )
                        } else {
                            viewModel.startGroupCall(
                                groupName = "FaceFeed Family Call 📞",
                                members = listOf(SampleData.friendHasan, SampleData.friendTanvir, SampleData.friendNusrat, SampleData.friendSamrat)
                            )
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = strings.groupCall,
                        modifier = Modifier.size(20.dp),
                        tint = FacebookBlue
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search in Messenger...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        // Notes Horizontal Row (exact reproduction of Messenger Notes!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            // User's own Note item ("Share a note...")
            NoteUserItem(
                user = currentUser,
                noteText = if (currentUser.note.isNotBlank()) currentUser.note else strings.shareNote,
                isMe = true,
                onClick = { showEditNoteDialog = true }
            )

            // Friends from user's screenshot with notes
            val friendsWithNotes = listOf(
                SampleData.friendHasan to "আজকে ফ্রি আছি 🔥",
                SampleData.friendSamrat to "FaceFeed rocks 🚀",
                SampleData.friendArifur to "Busy at work 💻",
                SampleData.friendTanvir to "Coding FaceFeed 🔥",
                SampleData.friendNusrat to "New photo session ✨"
            )

            friendsWithNotes.forEach { (friend, note) ->
                NoteUserItem(
                    user = friend,
                    noteText = note,
                    isMe = false,
                    onClick = { selectedFriendForNoteReply = friend to note }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Conversations List
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredConversations) { conv ->
                ConversationRow(
                    conversation = conv,
                    viewModel = viewModel,
                    onClick = { viewModel.openChat(conv) }
                )
            }
        }
    }

    // Dialog: Compose New Message
    if (showNewMessageDialog) {
        NewMessageDialog(
            friends = SampleData.profileFriends,
            onDismiss = { showNewMessageDialog = false },
            onSelectFriend = { friend ->
                showNewMessageDialog = false
                val existing = conversations.find { it.friend.fullName.contains(friend.name, ignoreCase = true) }
                    ?: Conversation(
                        id = "conv_new_${friend.id}",
                        friend = User(
                            id = friend.id,
                            fullName = friend.name,
                            username = friend.username,
                            avatarUrl = friend.avatarUrl,
                            isOnline = friend.isOnline
                        ),
                        lastMessage = "Say Hi to ${friend.name} 👋",
                        lastTime = "Just now"
                    )
                viewModel.openChat(existing)
            }
        )
    }

    // Dialog: Camera Viewfinder
    if (showCameraDialog) {
        MessengerCameraDialog(
            onDismiss = { showCameraDialog = false },
            onCapture = { photoUrl ->
                showCameraDialog = false
                val firstConv = conversations.firstOrNull()
                if (firstConv != null) {
                    viewModel.openChat(firstConv)
                    viewModel.sendMessage(conversationId = firstConv.id, text = "📷 Sent a photo", mediaUrl = photoUrl)
                }
            }
        )
    }

    // Dialog: Edit Note
    if (showEditNoteDialog) {
        var noteInput by remember { mutableStateOf(currentUser.note) }
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Share a note...", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Share a thought with friends ✨") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNote(noteInput)
                        showEditNoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Reply to Friend's Note
    selectedFriendForNoteReply?.let { (friend, note) ->
        NoteReplyDialog(
            friend = friend,
            note = note,
            onDismiss = { selectedFriendForNoteReply = null },
            onReply = { replyText ->
                selectedFriendForNoteReply = null
                val existing = conversations.find { it.friend.id == friend.id }
                    ?: Conversation(
                        id = "conv_${friend.id}",
                        friend = friend,
                        lastMessage = replyText,
                        lastTime = "Just now"
                    )
                viewModel.openChat(existing)
                viewModel.sendMessage(conversationId = existing.id, text = "Replying to note: \"$note\"\n$replyText")
            }
        )
    }
}

// Reproduction of Messenger Note bubble + avatar from user screenshot
@Composable
fun NoteUserItem(
    user: User,
    noteText: String,
    isMe: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        // Floating speech bubble for Note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp,
            modifier = Modifier.heightIn(min = 28.dp)
        ) {
            Text(
                text = noteText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar with Online Green Dot or '+' for me
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = user.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                contentDescription = user.fullName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            if (isMe) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            } else if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isMe) "Your note" else user.fullName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ConversationRow(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar or Group Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            if (conversation.isGroup) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BangladeshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            } else {
                AsyncImage(
                    model = conversation.friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = conversation.friend.fullName,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (conversation.friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
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
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (conversation.isGroup) conversation.groupName ?: "Group" else conversation.friend.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = conversation.lastTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (conversation.lastMessage.contains("missed a call", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.PhoneMissed,
                            contentDescription = "Missed Call",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (conversation.lastMessage.contains("attachment", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = FacebookBlue,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (conversation.lastMessage.contains("photo", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Photo",
                            tint = FacebookBlue,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = conversation.lastMessage,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${conversation.unreadCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// DIALOG: COMPOSE NEW MESSAGE (Friend Picker)
// ============================================================
@Composable
fun NewMessageDialog(
    friends: List<ProfileFriend>,
    onDismiss: () -> Unit,
    onSelectFriend: (ProfileFriend) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = remember(searchQuery, friends) {
        if (searchQuery.isBlank()) friends
        else friends.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Message", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("To: Type a name...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFriends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFriend(friend) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                            if (friend.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (friend.isOnline) "Active now" else friend.mutualFriendsCount,
                                fontSize = 12.sp,
                                color = if (friend.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onSelectFriend(friend) },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Chat", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================
// DIALOG: MESSENGER CAMERA STUDIO (Photo Capture & Send)
// ============================================================
@Composable
fun MessengerCameraDialog(
    onDismiss: () -> Unit,
    onCapture: (photoUrl: String) -> Unit
) {
    var flashOn by remember { mutableStateOf(false) }
    var frontCamera by remember { mutableStateOf(false) }

    val sampleSnaps = listOf(
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800"
    )
    var selectedSnapIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Messenger Camera", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Row {
                    IconButton(onClick = { flashOn = !flashOn }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashOn) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { frontCamera = !frontCamera }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Viewfinder display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = sampleSnaps[selectedSnapIndex],
                        contentDescription = "Camera Viewfinder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Live camera overlay watermark
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (frontCamera) "📷 Front View" else "📷 Rear View",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Switch filter/scenes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleSnaps.forEachIndexed { index, snapUrl ->
                        Surface(
                            onClick = { selectedSnapIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            border = if (selectedSnapIndex == index) androidx.compose.foundation.BorderStroke(2.dp, FacebookBlue) else null,
                            modifier = Modifier.size(44.dp)
                        ) {
                            AsyncImage(
                                model = snapUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCapture(sampleSnaps[selectedSnapIndex]) },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Snap & Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================
// DIALOG: REPLY TO FRIEND'S NOTE
// ============================================================
@Composable
fun NoteReplyDialog(
    friend: User,
    note: String,
    onDismiss: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val quickReactions = listOf("🔥", "❤️", "😂", "😮", "😢", "👍", "👏", "🎉")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                        contentDescription = friend.fullName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(friend.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Note bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💭 \"$note\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Quick Reactions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    quickReactions.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { onReply(emoji) }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${friend.fullName}...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (replyText.isNotBlank()) onReply(replyText)
                },
                enabled = replyText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
