package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.MessageDeliveryStatus
import com.example.model.Translations
import com.example.ui.components.ChatMessageActionBottomSheet
import com.example.ui.components.ForwardMessageDialog
import com.example.ui.components.PhoneMediaGalleryDialog
import com.example.ui.components.BlockConfirmationBottomSheet
import com.example.ui.components.BlockedSuccessBottomSheet
import com.example.ui.components.UnblockConfirmationBottomSheet
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import com.example.ui.components.BlockedChatFooter
import com.example.ui.components.FotoFilmAvatar
import com.example.ui.components.GoogleAdConfig
import com.example.ui.components.LudoKingGroupDialog
import com.example.ui.components.StickyVideoBottomBanner
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel
import com.example.util.AppSoundManager
import com.example.util.MediaSaveHelper
import com.example.util.VoiceAudioManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val typingConversationId by viewModel.typingConversationId.collectAsState()
    val isPartnerTyping = typingConversationId == conversation.id
    val isBlocked = conversation.isBlocked || blockedUserIds.contains(conversation.friend.id)
    val chatListState = rememberLazyListState()

    var messageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showPhoneGalleryDialog by remember { mutableStateOf(false) }
    var viewingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var viewingVideoUrl by remember { mutableStateOf<String?>(null) }
    var showChatInfoScreen by remember { mutableStateOf(false) }
    var showFriendProfileScreen by remember { mutableStateOf(false) }

    // Block & Unblock Dialog States
    var showBlockDialog by remember { mutableStateOf(false) }
    var showBlockedSuccessDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showLudoGameDialog by remember { mutableStateOf(false) }

    // Message Action Bottom Sheet & Forward States (Matching Screenshot 20261109_211144)
    var selectedMessageForActionSheet by remember { mutableStateOf<ChatMessage?>(null) }
    var forwardingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val allConversations by viewModel.conversations.collectAsState()

    val context = LocalContext.current

    // Real Voice Recording Engine States (MediaRecorder + AAC .m4a)
    val isRecordingVoice by VoiceAudioManager.isRecording.collectAsState()
    val isRecordingPaused by VoiceAudioManager.isRecordingPaused.collectAsState()
    val recordingSeconds by VoiceAudioManager.recordingSeconds.collectAsState()
    val recordingAmplitude by VoiceAudioManager.recordingAmplitude.collectAsState()
    val voiceError by VoiceAudioManager.lastError.collectAsState()
    var pendingStartVoiceRecord by remember { mutableStateOf(false) }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingStartVoiceRecord) {
            pendingStartVoiceRecord = false
            VoiceAudioManager.startRecording(context, "chat_${conversation.id}")
        } else {
            pendingStartVoiceRecord = false
            if (!granted) {
                Toast.makeText(context, "ভয়েস রেকর্ড করতে মাইক্রোফোন পারমিশন দিন", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val startVoiceRecord: () -> Unit = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            VoiceAudioManager.startRecording(context, "chat_${conversation.id}")
        } else {
            pendingStartVoiceRecord = true
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(voiceError) {
        voiceError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            VoiceAudioManager.clearError()
        }
    }

    DisposableEffect(conversation.id) {
        onDispose {
            VoiceAudioManager.stopPlayback()
            if (VoiceAudioManager.isRecording.value) {
                VoiceAudioManager.cancelRecording()
            }
        }
    }

    // System File Manager picker (avoids empty photo picker)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: ""
            val isPickedVideo = mime.startsWith("video/", ignoreCase = true) ||
                uri.toString().contains(".mp4", ignoreCase = true)
            viewModel.sendMessage(
                conversationId = conversation.id,
                text = "",
                mediaUrl = uri.toString(),
                isVideo = isPickedVideo,
                replyTo = replyingToMessage
            )
            replyingToMessage = null
        }
    }

    // Full Screen: Friend Profile View (when clicking Profile button or profile avatar)
    if (showFriendProfileScreen) {
        FriendProfileScreen(
            user = conversation.friend,
            viewModel = viewModel,
            onBack = { showFriendProfileScreen = false },
            onMessage = {
                showFriendProfileScreen = false
                showChatInfoScreen = false
            },
            onAudioCall = { viewModel.startAudioCall(conversation.friend) },
            onVideoCall = { viewModel.startVideoCall(conversation.friend) }
        )
        return
    }

    // Full Screen: Chat Info Screen (Matching Screenshot 2 & 3 when clicking [ⓘ] or name)
    if (showChatInfoScreen) {
        ChatInfoScreen(
            conversation = conversation,
            viewModel = viewModel,
            onBack = { showChatInfoScreen = false },
            onOpenProfile = { showFriendProfileScreen = true },
            onStartAudioCall = { viewModel.startAudioCall(conversation.friend) },
            onStartVideoCall = { viewModel.startVideoCall(conversation.friend) },
            onDeleteConversation = {
                viewModel.deleteConversation(conversation.id)
                onBack()
            }
        )
        return
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showFriendProfileScreen = true }
                    ) {
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
                                FotoFilmAvatar(
                                    imageUrl = conversation.friend.avatarUrl,
                                    name = conversation.friend.fullName,
                                    contentDescription = conversation.friend.fullName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
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
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (conversation.isGroup) "${conversation.groupMembers.size} members" else "Active 16 hours ago",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    // Audio Call Button (Disabled when blocked)
                    IconButton(
                        onClick = {
                            if (isBlocked) {
                                showUnblockDialog = true
                            } else {
                                viewModel.startAudioCall(conversation.friend)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = strings.audioCall,
                            tint = if (isBlocked) Color.Gray.copy(alpha = 0.4f) else Color(0xFF673AB7),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Video Call Button (Disabled when blocked)
                    IconButton(
                        onClick = {
                            if (isBlocked) {
                                showUnblockDialog = true
                            } else {
                                viewModel.startVideoCall(conversation.friend)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = strings.videoCall,
                            tint = if (isBlocked) Color.Gray.copy(alpha = 0.4f) else Color(0xFF673AB7),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Small Ludo Dice Icon (🎲) next to camera icon in Messenger chat header
                    IconButton(
                        onClick = {
                            val inviteText = "🎲 ${currentUser.fullName} invited you to play Ludo King - Play Now"
                            viewModel.sendMessage(conversationId = conversation.id, text = inviteText)
                            showLudoGameDialog = true
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎲", fontSize = 16.sp)
                            }
                        }
                    }

                    // Info Button: [ ⓘ ] (Blue circle with white 'i' / exclamation mark, matching Screenshot 1!)
                    IconButton(onClick = { showChatInfoScreen = true }) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(FacebookBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Chat Info",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
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
                .imePadding()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            val isFreeMode by viewModel.isFreeMode.collectAsState()
            val currentSim by viewModel.currentSimOperator.collectAsState()

            if (isFreeMode) {
                Surface(
                    color = Color(currentSim.colorHex),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📶 ${currentSim.banglaName} ফ্রি ChatFilm: টেক্সট মেসেজ, রিপ্লাই ও ছবি দেখা-দেওয়া ফ্রি ⚡",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LaunchedEffect(conversation.messages.size, isPartnerTyping) {
                val totalCount = conversation.messages.size + if (isPartnerTyping) 1 else 0
                if (totalCount > 0) {
                    chatListState.animateScrollToItem(totalCount - 1)
                }
            }

            // Messages List
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation.messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isRecipientOnline = if (conversation.isGroup) true else conversation.friend.isOnline,
                        recipientAvatar = conversation.friend.avatarUrl,
                        onReply = { replyingToMessage = msg },
                        onReact = { reaction ->
                            viewModel.reactToChatMessage(conversation.id, msg.id, reaction)
                        },
                        onLongPress = {
                            selectedMessageForActionSheet = msg
                        },
                        onViewMedia = { url, isVideo ->
                            if (isVideo) {
                                viewingVideoUrl = url
                            } else {
                                viewingPhotoUrl = url
                            }
                        },
                        onPlayLudo = { showLudoGameDialog = true },
                        onOpenProfileLink = { link ->
                            val opened = viewModel.openUserProfileByLink(link)
                            if (!opened) {
                                Toast.makeText(context, "এই আইডির প্রোফাইল লিংক খুঁজে পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCallBack = { isVideoCall ->
                            if (isBlocked) {
                                showUnblockDialog = true
                            } else if (isVideoCall) {
                                viewModel.startVideoCall(conversation.friend)
                            } else {
                                viewModel.startAudioCall(conversation.friend)
                            }
                        }
                    )
                }
                if (isPartnerTyping) {
                    item(key = "typing_indicator") {
                        MessengerTypingBubbleRow(
                            friendName = conversation.friend.fullName,
                            avatarUrl = conversation.friend.avatarUrl
                        )
                    }
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
                if (isBlocked) {
                    // Blocked State Footer (Screen 4)
                    BlockedChatFooter(
                        user = conversation.friend,
                        onUnblockClick = { showUnblockDialog = true }
                    )
                } else if (isRecordingVoice) {
                    // Real Audio Voice Recording Mode (MediaRecorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel / Delete Voice Recording
                        IconButton(
                            onClick = {
                                VoiceAudioManager.cancelRecording()
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

                        // Recording status and live amplitude waveform
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

                                // Live audio amplitude waveform bars
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    val waveHeights = listOf(8f, 16f, 24f, 12f, 22f, 14f, 20f, 10f, 18f, 24f, 16f, 8f)
                                    waveHeights.forEachIndexed { idx, defaultHeight ->
                                        val ampFactor = if (isRecordingPaused) 0.25f else (0.35f + recordingAmplitude * (0.65f + (idx % 3) * 0.18f))
                                        val barHeight = (defaultHeight * ampFactor).coerceIn(5f, 26f).dp
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
                                    onClick = { VoiceAudioManager.togglePauseRecording() },
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

                        // Send Real Recorded Voice Message Button
                        IconButton(
                            onClick = {
                                val recorded = VoiceAudioManager.stopRecording()
                                if (recorded != null) {
                                    viewModel.sendVoiceMessage(
                                        conversationId = conversation.id,
                                        durationSeconds = recorded.durationSeconds,
                                        audioUrl = recorded.fileUri,
                                        replyTo = replyingToMessage
                                    )
                                    replyingToMessage = null
                                } else {
                                    Toast.makeText(context, "কমপক্ষে ১ সেকেন্ড কথা বলে ভয়েস পাঠান", Toast.LENGTH_SHORT).show()
                                }
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
                            onClick = { startVoiceRecord() }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "ভয়েস রেকর্ড", tint = FacebookBlue)
                        }

                        // Message text field (plays warm Messenger typing sound as user writes)
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { newText ->
                                val hasChanged = newText != messageText
                                messageText = newText
                                if (hasChanged && newText.isNotEmpty()) {
                                    viewModel.onUserTypingInChat(true)
                                }
                            },
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
                                onClick = { startVoiceRecord() }
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

        // Screen 1: Block Confirmation Bottom Sheet
        if (showBlockDialog) {
            BlockConfirmationBottomSheet(
                user = conversation.friend,
                onDismiss = { showBlockDialog = false },
                onBlock = {
                    viewModel.blockConversation(conversation.id)
                    showBlockDialog = false
                    showBlockedSuccessDialog = true
                }
            )
        }

        // Screen 2: Blocked Success Bottom Sheet
        if (showBlockedSuccessDialog) {
            BlockedSuccessBottomSheet(
                user = conversation.friend,
                onDismiss = { showBlockedSuccessDialog = false },
                onDeleteChat = {
                    showBlockedSuccessDialog = false
                    viewModel.deleteConversation(conversation.id)
                    onBack()
                }
            )
        }

        // Screen 5: Unblock Confirmation Bottom Sheet
        if (showUnblockDialog) {
            UnblockConfirmationBottomSheet(
                user = conversation.friend,
                onDismiss = { showUnblockDialog = false },
                onUnblock = {
                    viewModel.unblockConversation(conversation.id)
                    showUnblockDialog = false
                }
            )
        }

        // Ludo King Realtime Multiplayer Dialog
        if (showLudoGameDialog) {
            LudoKingGroupDialog(
                viewModel = viewModel,
                onDismiss = { showLudoGameDialog = false }
            )
        }

        // Messenger Long-Press Message Actions & Reactions Bottom Sheet (Screenshot 20261109_211144)
        if (selectedMessageForActionSheet != null) {
            ChatMessageActionBottomSheet(
                message = selectedMessageForActionSheet!!,
                onDismiss = { selectedMessageForActionSheet = null },
                onReact = { reaction ->
                    viewModel.reactToChatMessage(conversation.id, selectedMessageForActionSheet!!.id, reaction)
                    selectedMessageForActionSheet = null
                },
                onReply = {
                    replyingToMessage = selectedMessageForActionSheet
                    selectedMessageForActionSheet = null
                },
                onForward = {
                    forwardingMessage = selectedMessageForActionSheet
                    selectedMessageForActionSheet = null
                },
                onDeleteForMe = {
                    viewModel.deleteChatMessageForMe(conversation.id, selectedMessageForActionSheet!!.id)
                    selectedMessageForActionSheet = null
                }
            )
        }

        // Forward Message to Friends Dialog
        if (forwardingMessage != null) {
            ForwardMessageDialog(
                message = forwardingMessage!!,
                conversations = allConversations,
                onDismiss = { forwardingMessage = null },
                onForwardTo = { targetConversationIds ->
                    viewModel.forwardChatMessage(forwardingMessage!!, targetConversationIds)
                    forwardingMessage = null
                }
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
    onLongPress: () -> Unit = {},
    onViewMedia: (String, Boolean) -> Unit,
    onPlayLudo: () -> Unit = {},
    onOpenProfileLink: (String) -> Unit = {},
    onCallBack: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val bubbleScope = rememberCoroutineScope()
    val isMe = message.isFromMe
    var showReactions by remember { mutableStateOf(false) }

    // Swipe-to-reply horizontal gesture state (মেসেজ চেপে টেনে রিপ্লাই)
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "swipeToReply"
    )

    // Real Continuous MediaPlayer Voice Playback States (No ToneGenerator beeps!)
    val activeVoiceId by VoiceAudioManager.playingItemId.collectAsState()
    val isAudioEnginePlaying by VoiceAudioManager.isPlaying.collectAsState()
    val playbackPosMs by VoiceAudioManager.playbackPositionMs.collectAsState()
    val playbackDurMs by VoiceAudioManager.playbackDurationMs.collectAsState()
    val isCurrentTrackLoaded = activeVoiceId == message.id
    val isVoicePlaying = isAudioEnginePlaying && isCurrentTrackLoaded
    val totalVoiceSeconds = if (isCurrentTrackLoaded && playbackDurMs > 0) {
        (playbackDurMs / 1000).coerceAtLeast(1)
    } else if (message.audioDurationSeconds > 0) {
        message.audioDurationSeconds
    } else {
        5
    }
    val voicePlaybackSeconds = if (isCurrentTrackLoaded) {
        (playbackPosMs / 1000).coerceIn(0, totalVoiceSeconds)
    } else {
        0
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

        // Reply Header (Matching Facebook Messenger Screenshot 3: You replied to MD)
        if (message.replyTo != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 3.dp, start = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMe) "You replied to ${message.replyTo.senderName.split(" ").firstOrNull() ?: message.replyTo.senderName}"
                           else "${message.senderName.split(" ").firstOrNull() ?: message.senderName} replied to you",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(animatedSwipeOffset.roundToInt(), 0) }
                .pointerInput(message.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (kotlin.math.abs(swipeOffset) > 80f) {
                                onReply()
                            }
                            swipeOffset = 0f
                        },
                        onDragCancel = {
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(-130f, 130f)
                        }
                    )
                }
        ) {
            // Recipient Small Avatar for friend's messages (Matching Screenshot 3)
            if (!isMe) {
                FotoFilmAvatar(
                    imageUrl = recipientAvatar,
                    name = message.senderName,
                    contentDescription = message.senderName,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                )
            }

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
                    color = if (isMe) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .widthIn(max = 290.dp)
                        .pointerInput(message.id) {
                            detectTapGestures(
                                onLongPress = {
                                    onLongPress()
                                },
                                onTap = {
                                    // Tap can also open reaction options
                                }
                            )
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Quoted reply preview if message is a reply to another message
                        if (message.replyTo != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isMe) Color.Black.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
                                            text = message.replyTo.text.ifBlank { "📷 মিডিয়া ফাইল" },
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 1. Real Audio Voice Message Player (MediaPlayer)
                        if (message.isAudioVoice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Pause Button
                                IconButton(
                                    onClick = {
                                        val mediaSource = message.mediaUrl?.trim().orEmpty()
                                        if (mediaSource.isNotBlank()) {
                                            VoiceAudioManager.togglePlay(context, message.id, mediaSource)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "ভয়েস অডিও ফাইল পাওয়া যায়নি — নতুন ভয়েস রেকর্ড করুন",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
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

                                // Waveform & Smooth Playback Duration
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                    ) {
                                        val waveHeights = listOf(6.dp, 12.dp, 18.dp, 10.dp, 16.dp, 20.dp, 12.dp, 18.dp, 22.dp, 14.dp, 8.dp, 16.dp, 10.dp, 6.dp)
                                        val progressFrac = if (isCurrentTrackLoaded && playbackDurMs > 0) {
                                            (playbackPosMs.toFloat() / playbackDurMs.toFloat()).coerceIn(0f, 1f)
                                        } else {
                                            0f
                                        }
                                        waveHeights.forEachIndexed { i, h ->
                                            val played = (i.toFloat() / waveHeights.size) <= progressFrac
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
                                        Text(
                                            text = String.format("%02d:%02d", voicePlaybackSeconds / 60, voicePlaybackSeconds % 60),
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

                        // 2. Video Attachment with play button overlay + Quick Save/Download button
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
                                // Quick Save / Download Video Button
                                Surface(
                                    onClick = {
                                        Toast.makeText(context, "⏳ ভিডিও ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                                        bubbleScope.launch {
                                            val res = MediaSaveHelper.saveMediaToGallery(context, message.mediaUrl, isVideo = true)
                                            if (res.isSuccess) {
                                                AppSoundManager.playReactionChime()
                                                Toast.makeText(context, "✅ ভিডিও গ্যালারি/ডাউনলোডে সেইভ হয়েছে!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "⚠️ ভিডিও ডাউনলোড ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Black.copy(alpha = 0.72f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Save Video", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("সেইভ", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
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

                        // 3. Photo Attachment with click to view fullscreen + Quick Save/Download button
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
                                Surface(
                                    onClick = {
                                        Toast.makeText(context, "⏳ ছবি ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                                        bubbleScope.launch {
                                            val res = MediaSaveHelper.saveMediaToGallery(context, message.mediaUrl, isVideo = false)
                                            if (res.isSuccess) {
                                                AppSoundManager.playReactionChime()
                                                Toast.makeText(context, "✅ ছবি গ্যালারি/ডাউনলোডে সেইভ হয়েছে!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "⚠️ ছবি ডাউনলোড ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Black.copy(alpha = 0.68f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Save Photo", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("সেইভ", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (message.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                        }

                        // 4. Messenger Audio/Video Call Bubble, Ludo Challenge Invite Card, Profile Link Card, or Standard Text Content
                        val profileLinkInMsg = MediaSaveHelper.extractProfileLink(message.text)
                        val isLegacyMissedOrCall = message.text.contains("Missed audio call", ignoreCase = true) ||
                            message.text.contains("Missed video call", ignoreCase = true) ||
                            message.text.contains("মিসড অডিও কল") ||
                            message.text.contains("মিসড ভিডিও কল")
                        val effectiveCallType = message.callLogType ?: when {
                            isLegacyMissedOrCall && (message.text.contains("video", ignoreCase = true) || message.text.contains("ভিডিও")) -> com.example.model.CallType.VIDEO
                            isLegacyMissedOrCall -> com.example.model.CallType.AUDIO
                            else -> null
                        }
                        if (effectiveCallType != null) {
                            val isVideoLog = effectiveCallType != com.example.model.CallType.AUDIO
                            val isMissedLog = message.isMissedCall ||
                                message.text.contains("মিসড") ||
                                message.text.contains("উত্তর নেই") ||
                                message.text.contains("missed", ignoreCase = true)
                            val callCardBg = if (isMe) {
                                Color.White.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                            val callBadgeColor = when {
                                isMissedLog -> Color(0xFFFA3E3E)
                                isMe -> Color.White.copy(alpha = 0.25f)
                                else -> FacebookBlue.copy(alpha = 0.14f)
                            }
                            val callBadgeIconTint = when {
                                isMissedLog -> Color.White
                                isMe -> Color.White
                                else -> FacebookBlue
                            }
                            val callTitle = when {
                                isMissedLog && isVideoLog && !isMe -> "মিসড ভিডিও কল"
                                isMissedLog && !isMe -> "মিসড অডিও কল"
                                isVideoLog -> "ভিডিও কল"
                                effectiveCallType == com.example.model.CallType.GROUP -> "গ্রুপ ভিডিও কল"
                                else -> "অডিও কল"
                            }
                            val callSubtitle = when {
                                message.text.contains("রিং হচ্ছে") -> "রিং হচ্ছে..."
                                message.text.contains("চলছে") -> "কল চলছে..."
                                isMissedLog && isMe -> "উত্তর পাওয়া যায়নি"
                                isMissedLog && !isMe -> "কল ব্যাক করতে ট্যাপ করুন"
                                message.callDurationSeconds > 0 -> {
                                    val m = message.callDurationSeconds / 60
                                    val s = message.callDurationSeconds % 60
                                    String.format(java.util.Locale.US, "কথা বলার সময় %02d:%02d", m, s)
                                }
                                else -> message.text
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = callCardBg,
                                border = if (!isMe) BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(callBadgeColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isMissedLog -> Icons.Default.PhoneMissed
                                                    isVideoLog -> Icons.Default.Videocam
                                                    else -> Icons.Default.Call
                                                },
                                                contentDescription = callTitle,
                                                tint = callBadgeIconTint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = callTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isMe) Color.White else if (isMissedLog) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = callSubtitle,
                                                fontSize = 12.sp,
                                                color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        onClick = { onCallBack(isVideoLog) },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isMe) Color.White else FacebookBlue.copy(alpha = 0.14f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 7.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isVideoLog) Icons.Default.Videocam else Icons.Default.Call,
                                                contentDescription = null,
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isVideoLog) "আবার ভিডিও কল দিন" else "আবার অডিও কল দিন",
                                                color = FacebookBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (message.text.contains("invited you to play Ludo King") || message.text.contains("লুডু খেলার আমন্ত্রণ") || message.text.contains("🎲")) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMe) Color(0xFF1565C0) else Color(0xFF1B5E20),
                                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                shadowElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "🎲", fontSize = 28.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "লুডু কিং চ্যালেঞ্জ! 👑",
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFFD700),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = message.text,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onPlayLudo,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFD700),
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "🎮 Play Now / এখনই খেলুন 🎲",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        } else if (message.text.isNotBlank() && !message.isAudioVoice) {
                            Text(
                                text = message.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            if (profileLinkInMsg != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    onClick = { onOpenProfileLink(profileLinkInMsg) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMe) Color.White.copy(alpha = 0.18f) else FacebookBlue.copy(alpha = 0.12f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isMe) Color.White.copy(alpha = 0.55f) else FacebookBlue.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.PersonPin,
                                                contentDescription = "Open Profile",
                                                tint = if (isMe) Color.White else FacebookBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "আইডির প্রোফাইল লিংক 🔗",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMe) Color.White else FacebookBlue
                                                )
                                                Text(
                                                    text = profileLinkInMsg,
                                                    fontSize = 10.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isMe) Color.White else FacebookBlue
                                        ) {
                                            Text(
                                                text = "আইডিতে ঢুকুন",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) FacebookBlue else Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
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
                                // Facebook Messenger style live progression: Sent ✓ -> Delivered ✓✓ -> Seen (avatar)
                                when (message.status) {
                                    MessageDeliveryStatus.SENT -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.18f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
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
                                                color = Color.White.copy(alpha = 0.92f)
                                            )
                                        }
                                    }
                                    MessageDeliveryStatus.DELIVERED -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.26f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✓✓", fontSize = 6.5.sp, color = FacebookBlue, fontWeight = FontWeight.ExtraBold)
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
                                    MessageDeliveryStatus.SEEN -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.24f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            if (recipientAvatar.isNotBlank()) {
                                                AsyncImage(
                                                    model = recipientAvatar,
                                                    contentDescription = "Seen",
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .border(0.8.dp, Color.White, CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "✓✓", fontSize = 6.5.sp, color = FacebookBlue, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "দেখা হয়েছে",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

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

            // Top Bar with Close, Title, Save/Download & Share
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                scope.launch {
                                    val res = MediaSaveHelper.saveMediaToGallery(context, imageUrl, isVideo = false)
                                    isSaving = false
                                    if (res.isSuccess) {
                                        isSaved = true
                                        AppSoundManager.playReactionChime()
                                        Toast.makeText(context, "✅ ছবি ফোনের গ্যালারিতে সেইভ ও ডাউনলোড হয়েছে!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "⚠️ ডাউনলোড ব্যর্থ: ${res.exceptionOrNull()?.message ?: ""}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) BangladeshGreen else Color.Black.copy(alpha = 0.65f))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Download,
                                contentDescription = "Save Photo",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            MediaSaveHelper.shareTextOrMediaLink(
                                context,
                                "FotoFilm ChatFilm ছবি: $imageUrl",
                                subject = "FotoFilm ছবি"
                            )
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            }

            // Floating Save & Download Pill Button right above banner
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        scope.launch {
                            val res = MediaSaveHelper.saveMediaToGallery(context, imageUrl, isVideo = false)
                            isSaving = false
                            if (res.isSuccess) {
                                isSaved = true
                                AppSoundManager.playReactionChime()
                                Toast.makeText(context, "✅ ছবি গ্যালারিতে সেইভ ও ডাউনলোড হয়েছে!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "⚠️ ডাউনলোড করা যায়নি", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) BangladeshGreen else FacebookBlue
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSaving) "ডাউনলোড হচ্ছে..." else if (isSaved) "সেইভ হয়েছে ✓" else "সেইভ ও ডাউনলোড করুন",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // User requested: যে কোনো ছবির নিচে Banner এই এড চালু করে দেন
            StickyVideoBottomBanner(
                modifier = Modifier.align(Alignment.BottomCenter),
                adUnitId = GoogleAdConfig.BANNER_ID
            )
        }
    }
}

@Composable
fun FullScreenVideoViewerDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
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

            // Top Bar with Close, Title, Save/Download & Share
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                scope.launch {
                                    val res = MediaSaveHelper.saveMediaToGallery(context, videoUrl, isVideo = true)
                                    isSaving = false
                                    if (res.isSuccess) {
                                        isSaved = true
                                        AppSoundManager.playReactionChime()
                                        Toast.makeText(context, "✅ ভিডিও ফোনের গ্যালারিতে সেইভ ও ডাউনলোড হয়েছে!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "⚠️ ভিডিও ডাউনলোড ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) BangladeshGreen else Color.Black.copy(alpha = 0.65f))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Download,
                                contentDescription = "Save Video",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            MediaSaveHelper.shareTextOrMediaLink(
                                context,
                                "FotoFilm ChatFilm ভিডিও: $videoUrl",
                                subject = "FotoFilm ভিডিও"
                            )
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
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

            // Bottom Video Controls + Save & Download Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                scope.launch {
                                    val res = MediaSaveHelper.saveMediaToGallery(context, videoUrl, isVideo = true)
                                    isSaving = false
                                    if (res.isSuccess) {
                                        isSaved = true
                                        AppSoundManager.playReactionChime()
                                        Toast.makeText(context, "✅ ভিডিও গ্যালারিতে সেইভ ও ডাউনলোড হয়েছে!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "⚠️ ভিডিও ডাউনলোড করা যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) BangladeshGreen else FacebookBlue
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = "Save Video",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaving) "ডাউনলোড হচ্ছে..." else if (isSaved) "ভিডিও সেইভ হয়েছে ✓" else "ভিডিও সেইভ ও ডাউনলোড করুন",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
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

@Composable
fun MessengerTypingBubbleRow(
    friendName: String,
    avatarUrl: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "messenger_typing")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        FotoFilmAvatar(
            imageUrl = avatarUrl,
            name = friendName,
            contentDescription = friendName,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue.copy(alpha = alpha))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "লিখছে...",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

