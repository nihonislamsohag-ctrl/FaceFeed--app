package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.User
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessengerAuditDialog(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onBanUser: (User, String) -> Unit
) {
    val allConversations by viewModel.conversations.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()

    // Keep active conversation state updated in real-time
    val currentConv = allConversations.find { it.id == conversation.id } ?: conversation
    val targetUser = allUsers.find { it.id == currentConv.friend.id } ?: currentConv.friend

    var messageToBanSender by remember { mutableStateOf<ChatMessage?>(null) }
    var banReasonInput by remember { mutableStateOf("মেসেঞ্জারে আপত্তিকর কথাবার্তা বা অশ্লীল ছবি/ভিডিও পাঠানো") }
    var selectedMediaPreview by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // User Avatar
                                AsyncImage(
                                    model = targetUser.avatarUrl,
                                    contentDescription = targetUser.fullName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (currentConv.isGroup) currentConv.groupName ?: "গ্রুপ চ্যাট" else targetUser.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                        if (targetUser.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (targetUser.isAccountActive) "আইডি সক্রিয় (অন)" else "আইডি অফ (নিষিদ্ধ)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (targetUser.isAccountActive) OnlineGreen else Color.Red
                                    )
                                }

                                // Quick Ban/Unban Button for this user
                                if (targetUser.isAccountActive) {
                                    Button(
                                        onClick = {
                                            onBanUser(targetUser, "মেসেঞ্জারে অপব্যবহার ও খারাপ আচরণ")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("আইডি অফ করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.toggleUserAccountStatus(targetUser.id, true)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("আইডি অন করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Audit Security Banner
                    Surface(
                        color = Color(0xFFFFF3CD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🕵️ রাইট ম্যানেজার অ্যাডমিন অডিট: এই চ্যাটে প্রেরিত টেক্সট, ছবি ও ভিডিও পর্যবেক্ষণ করছেন। আপত্তিকর কনটেন্ট পেলে নিচের বাটন থেকে আইডি বন্ধ বা মেসেজ মুছতে পারবেন।",
                                fontSize = 11.sp,
                                color = Color(0xFF856404),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (currentConv.messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এই চ্যাটে এখনো কোনো মেসেজ বা মিডিয়া নেই।",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            items(currentConv.messages, key = { it.id }) { msg ->
                                AdminAuditMessageCard(
                                    message = msg,
                                    onViewMedia = { selectedMediaPreview = it },
                                    onDeleteMessage = {
                                        viewModel.adminDeleteChatMessage(currentConv.id, msg.id)
                                    },
                                    onBanSender = {
                                        messageToBanSender = msg
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Media Preview Full Dialog
    selectedMediaPreview?.let { mediaUrl ->
        Dialog(onDismissRequest = { selectedMediaPreview = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মিডিয়া পূর্ণরূপ প্রিভিউ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { selectedMediaPreview = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Full Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }

    // Ban Sender from Message Dialog
    messageToBanSender?.let { msg ->
        val senderUser = allUsers.find { it.id == msg.senderId } ?: User(
            id = msg.senderId,
            username = msg.senderName.lowercase().replace(" ", "_"),
            fullName = msg.senderName,
            email = "",
            avatarUrl = ""
        )

        AlertDialog(
            onDismissRequest = { messageToBanSender = null },
            icon = {
                Icon(
                    Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "${msg.senderName}-এর আইডি বন্ধ (অফ) করবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "ব্যবহারকারীকে মেসেঞ্জারে ক্ষতিকর বা অনুপযুক্ত কনটেন্ট পাঠানোর কারণে নিষিদ্ধ করা হবে:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "অভিযুক্ত বার্তা: \"${msg.text.ifBlank { if (msg.isMediaVideo) "ভিডিও ফাইল" else "ছবি ফাইল" }}\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("আইডি বন্ধের কারণ", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBanUser(senderUser, banReasonInput)
                        messageToBanSender = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("আইডি অফ (ব্যান) করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToBanSender = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun AdminAuditMessageCard(
    message: ChatMessage,
    onViewMedia: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    onBanSender: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (message.mediaUrl != null) FacebookBlue.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Sender info & time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderName.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = FacebookBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = message.senderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Message Type Tag
                Surface(
                    color = when {
                        message.isMediaVideo -> Color(0xFFE91E63).copy(alpha = 0.12f)
                        message.mediaUrl != null -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                        else -> FacebookBlue.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            message.isMediaVideo -> "📹 ভিডিও"
                            message.mediaUrl != null -> "📷 ছবি"
                            else -> "💬 টেক্সট"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            message.isMediaVideo -> Color(0xFFE91E63)
                            message.mediaUrl != null -> Color(0xFF2E7D32)
                            else -> FacebookBlue
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quoted reply if any
            if (message.replyTo != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(FacebookBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "রিপ্লাই: ${message.replyTo.senderName}: ${message.replyTo.text.take(35)}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Attached Media Preview
            if (message.mediaUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onViewMedia(message.mediaUrl) }
                ) {
                    AsyncImage(
                        model = message.mediaUrl,
                        contentDescription = "Attached Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (message.isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.Black,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Zoom indicator icon
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("বড় করে দেখুন", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                if (message.text.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
            }

            // Text message
            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )
            }

            // Reaction badges
            if (message.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.forEach { (emoji, count) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Moderation Actions for this message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ban user button
                Button(
                    onClick = onBanSender,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("খারাপ কনটেন্টের জন্য আইডি অফ", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Delete message button
                OutlinedButton(
                    onClick = onDeleteMessage,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("মেসেজ মুছুন", color = Color.Red, fontSize = 10.sp)
                }
            }
        }
    }
}
