package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.Reply
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
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.MessageDeliveryStatus
import com.example.model.Translations
import com.example.ui.components.PhoneMediaGalleryDialog
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)

    var messageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showPhoneGalleryDialog by remember { mutableStateOf(false) }
    var viewingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var viewingVideoUrl by remember { mutableStateOf<String?>(null) }

    // Audio Voice Message Recording States
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isRecordingPaused by remember { mutableStateOf(false) }

    // Audio voice recording timer
    LaunchedEffect(isRecordingVoice, isRecordingPaused) {
        if (isRecordingVoice) {
            while (isRecordingVoice) {
                delay(1000)
                if (!isRecordingPaused) {
                    recordingSeconds++
                }
            }
        } else {
            recordingSeconds = 0
            isRecordingPaused = false
        }
    }

    // System File Manager picker (avoids empty photo picker)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.sendMessage(
                conversationId = conversation.id,
                text = "",
                mediaUrl = uri.toString(),
                isVideo = false,
                replyTo = replyingToMessage
            )
            replyingToMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (conversation.isGroup) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(BangladeshGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            } else {
                                AsyncImage(
                                    model = conversation.friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                                    contentDescription = conversation.friend.fullName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                if (conversation.friend.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
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
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (conversation.isGroup) conversation.groupName ?: "Group" else conversation.friend.fullName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (conversation.isGroup) "${conversation.groupMembers.size} members" else if (conversation.friend.isOnline) strings.activeNow else "Offline",
                                fontSize = 11.sp,
                                color = if (!conversation.isGroup && conversation.friend.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (conversation.isGroup) {
                        // Group Call Button
                        IconButton(
                            onClick = {
                                viewModel.startGroupCall(
                                    groupName = conversation.groupName ?: "Group Call",
                                    members = conversation.groupMembers
                                )
                            }
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = strings.groupCall, tint = FacebookBlue)
                        }
                    } else {
                        // Audio Call Button
                        IconButton(
                            onClick = { viewModel.startAudioCall(conversation.friend) }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = strings.audioCall, tint = FacebookBlue)
                        }

                        // Video Call Button
                        IconButton(
                            onClick = { viewModel.startVideoCall(conversation.friend) }
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = strings.videoCall, tint = FacebookBlue)
                        }

                        // Group Call Button (Audio & Video with friend and others)
                        IconButton(
                            onClick = {
                                viewModel.startGroupCall(
                                    groupName = "${conversation.friend.fullName} এবং বন্ধুরা",
                                    members = listOf(conversation.friend)
                                )
                            }
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = strings.groupCall, tint = FacebookBlue)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation.messages) { msg ->
                    ChatBubble(
                        message = msg,
                        isRecipientOnline = if (conversation.isGroup) true else conversation.friend.isOnline,
                        recipientAvatar = conversation.friend.avatarUrl,
                        onReply = { replyingToMessage = msg },
                        onReact = { reaction ->
                            viewModel.reactToChatMessage(conversation.id, msg.id, reaction)
                        },
                        onViewMedia = { url, isVideo ->
                            if (isVideo) {
                                viewingVideoUrl = url
                            } else {
                                viewingPhotoUrl = url
                            }
                        }
                    )
                }
            }

            // Reply Preview Banner
            if (replyingToMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(FacebookBlue, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "রিপ্লাই দিচ্ছেন ${replyingToMessage?.senderName}-কে",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue
                                )
                                Text(
                                    text = replyingToMessage?.text?.ifBlank { "📷 মিডিয়া ফাইল" } ?: "",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { replyingToMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (isRecordingVoice) {
                    // Audio Voice Recording Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel / Delete Voice Recording
                        IconButton(
                            onClick = {
                                isRecordingVoice = false
                                recordingSeconds = 0
                                isRecordingPaused = false
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "বাতিল করুন",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Recording status and live waveform
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FacebookBlue.copy(alpha = 0.10f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Blinking recording red dot + timer
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val pulseTransition = rememberInfiniteTransition(label = "voice_pulse")
                                    val pulseAlpha by pulseTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(500, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "voicePulse"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red.copy(alpha = if (isRecordingPaused) 0.4f else pulseAlpha))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val minutes = recordingSeconds / 60
                                    val seconds = recordingSeconds % 60
                                    Text(
                                        text = String.format("%02d:%02d", minutes, seconds),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Animated audio waveform bars
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    val waveHeights = listOf(8.dp, 16.dp, 24.dp, 12.dp, 22.dp, 14.dp, 20.dp, 10.dp, 18.dp, 24.dp, 16.dp, 8.dp)
                                    waveHeights.forEachIndexed { index, defaultHeight ->
                                        val barHeight = if (isRecordingPaused) 6.dp else defaultHeight
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(barHeight)
                                                .clip(RoundedCornerShape(1.5.dp))
                                                .background(FacebookBlue)
                                        )
                                    }
                                }

                                // Pause / Resume recording toggle
                                IconButton(
                                    onClick = { isRecordingPaused = !isRecordingPaused },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRecordingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = if (isRecordingPaused) "চালু করুন" else "থামুন",
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Voice Message Button
                        IconButton(
                            onClick = {
                                val duration = recordingSeconds.coerceAtLeast(3)
                                viewModel.sendVoiceMessage(
                                    conversationId = conversation.id,
                                    durationSeconds = duration,
                                    replyTo = replyingToMessage
                                )
                                isRecordingVoice = false
                                recordingSeconds = 0
                                isRecordingPaused = false
                                replyingToMessage = null
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(FacebookBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "ভয়েস মেসেজ পাঠান",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Standard Messenger Chat Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery attachment button
                        IconButton(
                            onClick = {
                                showPhoneGalleryDialog = true
                            }
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color(0xFF45BD62))
                        }

                        // Camera button
                        IconButton(
                            onClick = {
                                showPhoneGalleryDialog = true
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = FacebookBlue)
                        }

                        // Audio Voice button (dedicated on left)
                        IconButton(
                            onClick = {
                                isRecordingVoice = true
                                isRecordingPaused = false
                                recordingSeconds = 0
                            }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "ভয়েস রেকর্ড", tint = FacebookBlue)
                        }

                        // Message text field
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text(strings.typeMessage, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Send Button or Voice/Like Icons
                        if (messageText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(
                                        conversationId = conversation.id,
                                        text = messageText,
                                        replyTo = replyingToMessage
                                    )
                                    messageText = ""
                                    replyingToMessage = null
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = strings.sendMessage, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        } else {
                            // Quick Mic trigger button
                            IconButton(
                                onClick = {
                                    isRecordingVoice = true
                                    isRecordingPaused = false
                                    recordingSeconds = 0
                                }
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "ভয়েস মেসেজ", tint = FacebookBlue)
                            }
                            // Thumbs Up like button
                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(
                                        conversationId = conversation.id,
                                        text = "👍",
                                        replyTo = replyingToMessage
                                    )
                                    replyingToMessage = null
                                }
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Thumbs Up", tint = FacebookBlue)
                            }
                        }
                    }
                }
            }
        }

        // Phone Media Gallery & Camera Studio
        if (showPhoneGalleryDialog) {
            PhoneMediaGalleryDialog(
                title = "চ্যাট মিডিয়া গ্যালারি ও ক্যামেরা",
                allowMultiple = false,
                onMediaSelected = { urls ->
                    urls.firstOrNull()?.let { url ->
                        viewModel.sendMessage(
                            conversationId = conversation.id,
                            text = "",
                            mediaUrl = url,
                            isVideo = url.contains(".mp4") || url.contains("video"),
                            replyTo = replyingToMessage
                        )
                        replyingToMessage = null
                    }
                    showPhoneGalleryDialog = false
                },
                onLaunchSystemFilePicker = {
                    filePickerLauncher.launch("*/*")
                    showPhoneGalleryDialog = false
                },
                onDismiss = { showPhoneGalleryDialog = false }
            )
        }

        // Fullscreen Image Viewer Dialog
        if (viewingPhotoUrl != null) {
            FullScreenImageViewerDialog(
                imageUrl = viewingPhotoUrl!!,
                onDismiss = { viewingPhotoUrl = null }
            )
        }

        // Fullscreen Video Viewer Dialog
        if (viewingVideoUrl != null) {
            FullScreenVideoViewerDialog(
                videoUrl = viewingVideoUrl!!,
                onDismiss = { viewingVideoUrl = null }
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isRecipientOnline: Boolean = true,
    recipientAvatar: String = "",
    onReply: () -> Unit,
    onReact: (String) -> Unit,
    onViewMedia: (String, Boolean) -> Unit
) {
    val isMe = message.isFromMe
    var showReactions by remember { mutableStateOf(false) }

    // Audio Voice Message Player States
    var isVoicePlaying by remember { mutableStateOf(false) }
    var voicePlaybackSeconds by remember { mutableIntStateOf(0) }
    val totalVoiceSeconds = if (message.audioDurationSeconds > 0) message.audioDurationSeconds else 5

    // Real audio tone generator sound playback effect
    LaunchedEffect(isVoicePlaying) {
        if (isVoicePlaying) {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (e: Exception) {}

            while (isVoicePlaying && voicePlaybackSeconds < totalVoiceSeconds) {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                } catch (e: Exception) {}
                delay(1000)
                if (isVoicePlaying) {
                    voicePlaybackSeconds++
                }
            }
            isVoicePlaying = false
            voicePlaybackSeconds = 0
            try {
                toneGen?.release()
            } catch (e: Exception) {}
        } else {
            voicePlaybackSeconds = 0
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        // Floating Reaction Bar on tap
        if (showReactions) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val chatEmojis = listOf("❤️", "👍", "😆", "😮", "😢", "😡")
                    chatEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable {
                                    onReact(emoji)
                                    showReactions = false
                                }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // If sender is ME, reply and react triggers on the left
            if (isMe) {
                IconButton(
                    onClick = { showReactions = !showReactions },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddReaction,
                        contentDescription = "React",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onReply,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    ),
                    color = if (isMe) FacebookBlue else MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.widthIn(max = 290.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Quoted reply preview if message is a reply to another message
                        if (message.replyTo != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMe) Color.Black.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
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
                                            .height(24.dp)
                                            .background(if (isMe) Color.White else FacebookBlue, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = message.replyTo.senderName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White else FacebookBlue
                                        )
                                        Text(
                                            text = message.replyTo.text.take(30),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 1. Audio Voice Message Player
                        if (message.isAudioVoice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Pause Button
                                IconButton(
                                    onClick = { isVoicePlaying = !isVoicePlaying },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isMe) Color.White else FacebookBlue)
                                ) {
                                    Icon(
                                        imageVector = if (isVoicePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isVoicePlaying) "Pause Voice" else "Play Voice",
                                        tint = if (isMe) FacebookBlue else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Waveform & Playback Duration
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                    ) {
                                        val waveHeights = listOf(6.dp, 12.dp, 18.dp, 10.dp, 16.dp, 20.dp, 12.dp, 18.dp, 22.dp, 14.dp, 8.dp, 16.dp, 10.dp, 6.dp)
                                        waveHeights.forEachIndexed { i, h ->
                                            val played = (i.toFloat() / waveHeights.size) <= (voicePlaybackSeconds.toFloat() / totalVoiceSeconds.coerceAtLeast(1))
                                            val barColor = if (isMe) {
                                                if (played || isVoicePlaying) Color.White else Color.White.copy(alpha = 0.5f)
                                            } else {
                                                if (played || isVoicePlaying) FacebookBlue else FacebookBlue.copy(alpha = 0.4f)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(if (isVoicePlaying && i % 2 == 0) h * 1.15f else h)
                                                    .clip(RoundedCornerShape(1.dp))
                                                    .background(barColor)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val curSec = if (isVoicePlaying) voicePlaybackSeconds else 0
                                        Text(
                                            text = String.format("%02d:%02d", curSec / 60, curSec % 60),
                                            fontSize = 11.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = String.format("%02d:%02d", totalVoiceSeconds / 60, totalVoiceSeconds % 60),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Video Attachment with play button overlay
                        if (message.mediaUrl != null && message.isMediaVideo) {
                            Box(
                                modifier = Modifier
                                    .size(width = 220.dp, height = 150.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onViewMedia(message.mediaUrl, true) },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "Video Attachment",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "ভিডিও", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (message.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }

                        // 3. Photo Attachment with click to view fullscreen
                        if (message.mediaUrl != null && !message.isMediaVideo) {
                            Box(
                                modifier = Modifier
                                    .size(210.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onViewMedia(message.mediaUrl, false) }
                            ) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "Photo Attachment",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (message.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }

                        // 4. Text Content (if not voice placeholder)
                        if (message.text.isNotBlank() && !message.isAudioVoice) {
                            Text(
                                text = message.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }

                        // 5. Message Timestamp & Facebook Messenger Style Delivery Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.timestamp,
                                color = if (isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )

                            if (isMe) {
                                Spacer(modifier = Modifier.width(6.dp))
                                // Delivery status: Offline -> Sent (ছেন্ড) / Online -> Delivered (ডেলিভারি) / Seen (দেখা হয়েছে)
                                when {
                                    !isRecipientOnline || message.status == MessageDeliveryStatus.SENT -> {
                                        // Recipient Offline: Sent / ছেন্ড
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.20f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✓", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "সেন্ড",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                    message.status == MessageDeliveryStatus.DELIVERED -> {
                                        // Recipient Online: Delivered / ডেলিভারি
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.25f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✓", fontSize = 7.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "ডেলিভারি",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    message.status == MessageDeliveryStatus.SEEN -> {
                                        // Recipient Seen / দেখা হয়েছে
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.22f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            if (recipientAvatar.isNotBlank()) {
                                                AsyncImage(
                                                    model = recipientAvatar,
                                                    contentDescription = "Seen",
                                                    modifier = Modifier
                                                        .size(11.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(11.dp)
                                                        .clip(CircleShape)
                                                    .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "✓✓", fontSize = 6.sp, color = FacebookBlue, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "দেখা হয়েছে",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Reactions Badge Pill
                if (message.reactions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .align(if (isMe) Alignment.BottomStart else Alignment.BottomEnd)
                            .offset(y = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            message.reactions.forEach { (emoji, count) ->
                                Text(text = "$emoji $count", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                        }
                    }
                }
            }

            // If sender is NOT me, reply and react triggers on the right
            if (!isMe) {
                IconButton(
                    onClick = onReply,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { showReactions = !showReactions },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddReaction,
                        contentDescription = "React",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (message.reactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun FullScreenImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Image Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() },
                contentScale = ContentScale.Fit
            )

            // Top Bar with Close and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Text(
                    text = "ছবি ভিউয়ার",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = { /* Save or share */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun FullScreenVideoViewerDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(3) }
    val totalSeconds = 24

    LaunchedEffect(isPlaying) {
        while (isPlaying && currentSeconds < totalSeconds) {
            delay(1000)
            currentSeconds++
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Video Thumbnail as background
            AsyncImage(
                model = videoUrl,
                contentDescription = "Full Video Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Text(
                    text = "ভিডিও প্লেয়ার",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = { /* Share video */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }

            // Center Play / Pause trigger
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            // Bottom Video Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Play",
                            tint = Color.White
                        )
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (currentSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f))
                                .background(FacebookBlue)
                        )
                    }

                    Text(
                        text = String.format("%02d:%02d / %02d:%02d", currentSeconds / 60, currentSeconds % 60, totalSeconds / 60, totalSeconds % 60),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
