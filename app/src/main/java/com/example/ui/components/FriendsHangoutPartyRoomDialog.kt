package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PartyVoiceSeat(
    val seatNumber: Int,
    val user: User? = null,
    val customAvatarUrl: String? = null,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val badgeLabel: String? = null,
    val hasCyanRing: Boolean = false,
    val micBadgeColor: Color? = null,
    val isSpeaking: Boolean = false
)

data class PartyRoomMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val message: String,
    val mediaUri: Uri? = null,
    val isVideo: Boolean = false,
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isSystemJoin: Boolean = false,
    val badge: String? = null
)

data class AudienceMember(
    val id: String,
    val name: String,
    val avatarUrl: String
)

/**
 * Friends Hangout Party Room ("বন্ধুদের সাথে আড্ডা 🧑🤝🧑")
 * Authentic recreation of Screenshot 20261026_200055:
 * - Room Header: "__অতিরিক্ত_–" with cyan (+) button, Members: 19, Share & Close
 * - Quote: "☆__শূণ্যতা__দিয়ে__পূর্ণতা__পাক__তবুও__শকের পুরুষ___সুঁখে__থাক্--.
 * - 10 Seats with authentic avatars, labels, cyan rings, lock icons and mic badges
 * - Audience row: পিচ্চি ছেলে, Md Sohag, জামাই
 * - Live floating chat bubbles: "আমার আম্মুর একমাত্র ছে... send 🤣", "◇•*🍀জাঁন্নাঁত্... send 🤣"
 * - Lifted bottom messaging dock ("নিচ থেকে আরো উপরের দিকে হবে যাতে সহজে মেসেজ করা যায়"):
 *   - Voice recording with live timer and waveform (🎙️ বয়েজ)
 *   - Photo/Video gallery picker (ছবি ও ভিডিও গ্যালারি)
 *   - Various Emojis tray (😂, 🤣, ❤️, 😍, 🔥, 🌹, 🤲, 🥳, 💋, ইত্যাদি)
 *   - Full bottom toolbar: 💬 Chat, 😊 Emojis, 📅 Event, ⊞ Tools, 🌹 Rose, 🎁 Gift Box
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FriendsHangoutPartyRoomDialog(
    viewModel: FaceFeedViewModel,
    initialRoomName: String = "__অতিরিক্ত_–",
    initialAvatarUrl: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
    initialMemberCount: Int = 19,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    // Room info matching user screenshot
    var groupNameInput by remember { mutableStateOf(initialRoomName) }
    var groupAvatarUrl by remember { mutableStateOf(initialAvatarUrl) }
    var memberCount by remember { mutableIntStateOf(initialMemberCount) }

    // Live Room Mic & Tools State
    var isMyMicMuted by remember { mutableStateOf(false) }
    var isEchoEnabled by remember { mutableStateOf(false) }
    var isUserOnSeat by remember { mutableStateOf(false) }
    var mySeatIndex by remember { mutableIntStateOf(-1) }
    val agoraState by com.example.data.AgoraCallManager.callState.collectAsState()
    var pendingAudioAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAudioAction?.invoke()
        } else {
            Toast.makeText(context, "ভয়েস সিটে কথা বলতে মাইক্রোফোন অনুমতি দিন", Toast.LENGTH_SHORT).show()
        }
        pendingAudioAction = null
    }
    val hangoutChannel = remember(groupNameInput) {
        com.example.data.AgoraCallManager.sanitizeChannelName("ff_hangout_${groupNameInput}")
    }
    fun runWithMicAndAgora(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            com.example.data.AgoraCallManager.joinCall(
                context = context,
                channelName = hangoutChannel,
                isVideoCall = false,
                startMuted = false
            )
            action()
        } else {
            pendingAudioAction = {
                com.example.data.AgoraCallManager.joinCall(
                    context = context,
                    channelName = hangoutChannel,
                    isVideoCall = false,
                    startMuted = false
                )
                action()
            }
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            com.example.data.AgoraCallManager.leaveCall()
        }
    }
    var chatInput by remember { mutableStateOf("") }
    var showGiftSheet by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }
    var showToolsDialog by remember { mutableStateOf(false) }
    var showInviteFriendsDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf(false) }
    var showLuckySpinDialog by remember { mutableStateOf(false) }
    var showLudoFromHangout by remember { mutableStateOf(false) }
    var showOnlineUsersDialog by remember { mutableStateOf(false) }
    var playingVoiceMsgId by remember { mutableStateOf<String?>(null) }
    var giftCount by remember { mutableIntStateOf(7000) }
    var roomQuote by remember { mutableStateOf("\"☆__শূণ্যতা__দিয়ে__পূর্ণতা__পাক__তবুও__শকের পুরুষ___সুঁখে__থাক্--.") }

    // Bottom Messaging & Media Panels State (lifted from bottom)
    var isChatInputVisible by remember { mutableStateOf(true) }
    var isEmojiTrayVisible by remember { mutableStateOf(false) }
    var isVoiceRecording by remember { mutableStateOf(false) }
    var voiceRecordDuration by remember { mutableIntStateOf(0) }

    // Follow and Follow-Back States in Friends Hangout Room (বন্ধুদের আড্ডায় একে অপরকে ফলো / ফলো ব্যাক)
    var followedUserIds by remember { mutableStateOf(setOf<String>("a2")) }
    var invitedFriendIds by remember { mutableStateOf(setOf<String>()) }
    var reminderSetEvents by remember { mutableStateOf(setOf<String>()) }
    var selectedUserProfile by remember { mutableStateOf<AudienceMember?>(null) }


    // 10 Seats matching exact Screenshot 20261026_200055:
    var seats by remember {
        mutableStateOf(
            listOf(
                PartyVoiceSeat(
                    seatNumber = 1,
                    customAvatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
                    isLocked = false,
                    isMuted = false,
                    badgeLabel = "◇•*🍀জাঁন্নাঁ...",
                    hasCyanRing = true,
                    isSpeaking = true
                ),
                PartyVoiceSeat(
                    seatNumber = 2,
                    customAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200",
                    isLocked = false,
                    isMuted = false,
                    badgeLabel = "sukhi jibon",
                    hasCyanRing = true,
                    isSpeaking = false
                ),
                PartyVoiceSeat(
                    seatNumber = 3,
                    customAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                    isLocked = false,
                    isMuted = false,
                    badgeLabel = "আমার আ...",
                    hasCyanRing = false,
                    isSpeaking = true
                ),
                PartyVoiceSeat(
                    seatNumber = 4,
                    user = null,
                    isLocked = true,
                    isMuted = false,
                    badgeLabel = "4"
                ),
                PartyVoiceSeat(
                    seatNumber = 5,
                    customAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                    isLocked = false,
                    isMuted = true,
                    badgeLabel = "...⚡fac...",
                    micBadgeColor = Color(0xFFE91E63)
                ),
                PartyVoiceSeat(
                    seatNumber = 6,
                    user = null,
                    isLocked = true,
                    isMuted = false,
                    badgeLabel = "6"
                ),
                PartyVoiceSeat(
                    seatNumber = 7,
                    user = null,
                    isLocked = true,
                    isMuted = false,
                    badgeLabel = "7"
                ),
                PartyVoiceSeat(
                    seatNumber = 8,
                    user = null,
                    isLocked = true,
                    isMuted = false,
                    badgeLabel = "8"
                ),
                PartyVoiceSeat(
                    seatNumber = 9,
                    customAvatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200",
                    isLocked = false,
                    isMuted = false,
                    badgeLabel = "জান্নাত"
                ),
                PartyVoiceSeat(
                    seatNumber = 10,
                    customAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    isLocked = false,
                    isMuted = false,
                    badgeLabel = "মো: নয়ন স...",
                    micBadgeColor = Color(0xFF2979FF)
                )
            )
        )
    }

    // Audience List matching Screenshot:
    val audienceMembers = remember {
        listOf(
            AudienceMember("a1", "পিচ্চি ছেলে", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200"),
            AudienceMember("a2", "Md Sohag", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"),
            AudienceMember("a3", "জামাই", "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=200")
        )
    }

    // Live Messages list initialized with exact screenshot bubbles
    val messagesListState = rememberLazyListState()
    var messages by remember {
        mutableStateOf(
            listOf(
                PartyRoomMessage(
                    id = "1",
                    senderName = "আমার আম্মুর একমাত্র ছে...",
                    senderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    message = "send 🤣"
                ),
                PartyRoomMessage(
                    id = "2",
                    senderName = "◇•*🍀জাঁন্নাঁত্—»❦",
                    senderAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
                    message = "send 🤣"
                )
            )
        )
    }

    // Media Picker for Photos & Videos (Zero-permission Android picker)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true
            AppSoundManager.playSocialAdChime()
            messages = messages + PartyRoomMessage(
                id = System.currentTimeMillis().toString(),
                senderName = currentUser.fullName,
                senderAvatar = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
                message = if (isVideo) "ভিডিও পাঠিয়েছেন 🎥" else "ছবি পাঠিয়েছেন 🖼️",
                mediaUri = uri,
                isVideo = isVideo
            )
            Toast.makeText(context, if (isVideo) "ভিডিও শেয়ার হয়েছে! 🎥" else "ছবি শেয়ার হয়েছে! 🖼️", Toast.LENGTH_SHORT).show()
        }
    }

    // Voice record timer effect
    LaunchedEffect(isVoiceRecording) {
        if (isVoiceRecording) {
            voiceRecordDuration = 0
            while (isVoiceRecording) {
                delay(1000)
                voiceRecordDuration++
            }
        }
    }

    // Voice playback audio effect when user taps a voice message bubble
    LaunchedEffect(playingVoiceMsgId) {
        if (playingVoiceMsgId != null) {
            var toneGen: android.media.ToneGenerator? = null
            try {
                toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80)
                repeat(3) {
                    if (playingVoiceMsgId == null) return@LaunchedEffect
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 110)
                    delay(850)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    toneGen?.release()
                } catch (_: Exception) {}
                playingVoiceMsgId = null
            }
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            messagesListState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        AppSoundManager.playReactionSound(com.example.model.ReactionType.LIKE)
        val newMsg = PartyRoomMessage(
            id = System.currentTimeMillis().toString(),
            senderName = currentUser.fullName,
            senderAvatar = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
            message = text.trim()
        )
        messages = messages + newMsg
        chatInput = ""
    }

    fun sendEmojiReaction(emoji: String) {
        AppSoundManager.playReactionSound(com.example.model.ReactionType.HAHA)
        val newMsg = PartyRoomMessage(
            id = System.currentTimeMillis().toString(),
            senderName = currentUser.fullName,
            senderAvatar = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
            message = "send $emoji"
        )
        messages = messages + newMsg
        Toast.makeText(context, "$emoji রিঅ্যাকশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    fun sendVoiceMessage(durationSec: Int) {
        AppSoundManager.playSocialAdChime()
        val newMsg = PartyRoomMessage(
            id = System.currentTimeMillis().toString(),
            senderName = currentUser.fullName,
            senderAvatar = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
            message = "🎙️ ভয়েস মেসেজ (০:${if (durationSec < 10) "0$durationSec" else durationSec}) ▶️",
            isVoice = true,
            voiceDurationSec = durationSec
        )
        messages = messages + newMsg
        Toast.makeText(context, "ভয়েস মেসেজ সফলভাবে পাঠানো হয়েছে! 🎙️", Toast.LENGTH_SHORT).show()
    }

    BackHandler { onDismiss() }

    val seatedOnlineCount = seats.count { it.user != null || it.customAvatarUrl != null }
    val totalLiveOnlineUsers = seatedOnlineCount + audienceMembers.size + 42

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true
        )
    ) {
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.let { win ->
                WindowCompat.setDecorFitsSystemWindows(win, false)
                @Suppress("DEPRECATION")
                win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
        val isKeyboardOpen = WindowInsets.isImeVisible
        LaunchedEffect(isKeyboardOpen, messages.size) {
            if (messages.isNotEmpty()) {
                messagesListState.animateScrollToItem(messages.size - 1)
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFBF8F5)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // ========================================================
                // 1. Top Header with Visible Back Button + Live Online Count
                // ========================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Explicit Visible Back Button to exit "বন্ধুদের সাথে আড্ডা"
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEEEEE))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back (বের হোন)",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Room Avatar with border
                    AsyncImage(
                        model = groupAvatarUrl,
                        contentDescription = "Room Avatar",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = groupNameInput,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FidaVerifiedBadge(size = 14.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            // Cyan Circular + Button -> Opens Friend Invite Sheet
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00B0FF),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showInviteFriendsDialog = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Invite Friends",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Live Online Count Badge (কতো মানুষ লাইনে আছে)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(0.6.dp, BangladeshGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { showOnlineUsersDialog = true }
                            ) {
                                Text(
                                    text = "🟢 লাইনে: ${totalLiveOnlineUsers} জন",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BangladeshGreen,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "Members: $memberCount",
                                fontSize = 10.sp,
                                color = Color(0xFF757575),
                                maxLines = 1
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF8E1),
                                modifier = Modifier.clickable { showGiftSheet = true }
                            ) {
                                Text(
                                    text = "🎁 $giftCount",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Edit Topic, Share and Exit icons (Always stay inside screen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showEditRoomDialog = true }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Room", tint = Color.DarkGray, modifier = Modifier.size(17.dp))
                        }
                        IconButton(onClick = { showInviteFriendsDialog = true }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Invite Friends", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Middle flexible container so Seats/Audience smoothly scroll when keyboard opens,
                // keeping Chat Stream and "মেসেজ লিখুন..." input bar ALWAYS visible above keyboard.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                // ========================================================
                // 2. Room Quote Banner
                // ========================================================
                Text(
                    text = roomQuote,
                    fontSize = 12.sp,
                    color = Color(0xFF212121),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { showEditRoomDialog = true },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ========================================================
                // 3. Ten Audio/Voice Seats matching Screenshot
                // Row 1: Seats 1 - 5
                // ========================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    seats.take(5).forEachIndexed { index, seat ->
                        PartyVoiceSeatItem(
                            seat = seat,
                            onClick = {
                                if (seat.user == null && seat.customAvatarUrl == null) {
                                    runWithMicAndAgora {
                                        seats = seats.mapIndexed { idx, s ->
                                            if (idx == index) s.copy(user = currentUser, badgeLabel = currentUser.fullName.take(6), isLocked = false, isSpeaking = true) else s
                                        }
                                        isUserOnSeat = true
                                        mySeatIndex = index
                                        Toast.makeText(context, "${index + 1} নং সিটে Agora HD ভয়েসে যোগ দিয়েছেন 🎙️", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    selectedUserProfile = AudienceMember(
                                        id = "seat_${seat.seatNumber}",
                                        name = seat.badgeLabel ?: "Seat ${seat.seatNumber}",
                                        avatarUrl = seat.customAvatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200"
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: Seats 6 - 10
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    seats.drop(5).take(5).forEachIndexed { relIndex, seat ->
                        val absIndex = relIndex + 5
                        PartyVoiceSeatItem(
                            seat = seat,
                            onClick = {
                                if (seat.isLocked) {
                                    runWithMicAndAgora {
                                        seats = seats.mapIndexed { idx, s ->
                                            if (idx == absIndex) s.copy(isLocked = false, user = currentUser, badgeLabel = "You", isSpeaking = true) else s
                                        }
                                        isUserOnSeat = true
                                        mySeatIndex = absIndex
                                        Toast.makeText(context, "সিট আনলক করে Agora HD মাইকে উঠেছেন! 🎙️", Toast.LENGTH_SHORT).show()
                                    }
                                } else if (seat.customAvatarUrl != null || seat.user != null) {
                                    selectedUserProfile = AudienceMember(
                                        id = "seat_${seat.seatNumber}",
                                        name = seat.badgeLabel ?: "Seat ${seat.seatNumber}",
                                        avatarUrl = seat.customAvatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
                                    )
                                } else {
                                    Toast.makeText(context, "সিট ${seat.badgeLabel ?: seat.seatNumber}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Seat / Mic Control Pill when User joins a Seat
                AnimatedVisibility(visible = isUserOnSeat && mySeatIndex in seats.indices) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE3F2FD),
                        border = BorderStroke(1.dp, Color(0xFF00B0FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "সিট #${mySeatIndex + 1} সচল",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                                Text(
                                    text = agoraState.statusText,
                                    fontSize = 9.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isMyMicMuted) BangladeshRed else Color(0xFF2E7D32),
                                    modifier = Modifier.clickable {
                                        val nextMuted = !isMyMicMuted
                                        isMyMicMuted = nextMuted
                                        com.example.data.AgoraCallManager.setMicMuted(nextMuted)
                                        seats = seats.mapIndexed { idx, s ->
                                            if (idx == mySeatIndex) s.copy(isMuted = nextMuted, isSpeaking = !nextMuted) else s
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (isMyMicMuted) "🔇 মিউট" else "🎙️ মাইক অন",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isEchoEnabled) Color(0xFF7B1FA2) else Color.White,
                                    modifier = Modifier.clickable {
                                        isEchoEnabled = !isEchoEnabled
                                        Toast.makeText(
                                            context,
                                            if (isEchoEnabled) "🎚️ স্টুডিও ইকো ইফেক্ট চালু!" else "ইকো ইফেক্ট বন্ধ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                ) {
                                    Text(
                                        text = if (isEchoEnabled) "🎚️ ইকো ON" else "🎚️ ইকো",
                                        fontSize = 10.sp,
                                        color = if (isEchoEnabled) Color.White else Color.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEEEEEE),
                                    modifier = Modifier.clickable {
                                        val leftIdx = mySeatIndex
                                        seats = seats.mapIndexed { idx, s ->
                                            if (idx == leftIdx) s.copy(user = null, isMuted = false, isSpeaking = false, badgeLabel = "${s.seatNumber}") else s
                                        }
                                        isUserOnSeat = false
                                        mySeatIndex = -1
                                        isMyMicMuted = false
                                        com.example.data.AgoraCallManager.leaveCall()
                                    }
                                ) {
                                    Text(
                                        text = "🪑 নামুন",
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ========================================================
                // 4. Audience Row matching Screenshot with Follow & Follow Back
                // ========================================================
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Audience (${audienceMembers.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "👉 ক্লিক করে ফলো বা ফলো ব্যাক দিন",
                            fontSize = 10.sp,
                            color = FacebookBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        audienceMembers.forEach { audience ->
                            val isFollowed = followedUserIds.contains(audience.id)
                            val isFollowingMe = audience.id == "a1" || audience.id == "a3" // users who follow me -> show "ফলো ব্যাক"

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    selectedUserProfile = audience
                                }
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    AsyncImage(
                                        model = audience.avatarUrl,
                                        contentDescription = audience.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, if (isFollowed) Color(0xFF2E7D32) else Color(0xFF00B0FF), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isFollowed) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E7D32)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = audience.name,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )

                                // Follow / Follow Back Button
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isFollowed) Color(0xFFEEEEEE) else if (isFollowingMe) FacebookBlue else Color(0xFF00B0FF),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clickable {
                                            if (isFollowed) {
                                                followedUserIds = followedUserIds - audience.id
                                                Toast.makeText(context, "${audience.name}-কে আনফলো করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            } else {
                                                followedUserIds = followedUserIds + audience.id
                                                AppSoundManager.playSocialAdChime()
                                                val msg = if (isFollowingMe) "${audience.name}-কে ফলো ব্যাক দিয়েছেন! 🤝" else "${audience.name}-কে ফলো করেছেন! 🌟"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Text(
                                        text = if (isFollowed) "ফলোয়িং ✓" else if (isFollowingMe) "ফলো ব্যাক" else "ফলো +",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFollowed) Color(0xFF616161) else Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                }

                // ========================================================
                // 5. Floating Live Chat Stream (Matching Screenshot Bubbles)
                // ========================================================
                LazyColumn(
                    state = messagesListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isKeyboardOpen) 96.dp else 160.dp)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages) { msg ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 1.dp,
                            border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                AsyncImage(
                                    model = msg.senderAvatar,
                                    contentDescription = msg.senderName,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF212121),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                if (msg.mediaUri != null) {
                                    if (msg.isVideo) {
                                        Surface(
                                            color = BangladeshRed,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Text("ভিডিও", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        AsyncImage(
                                            model = msg.mediaUri,
                                            contentDescription = "Media",
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    val isPlayingThisVoice = msg.isVoice && playingVoiceMsgId == msg.id
                                    Text(
                                        text = if (isPlayingThisVoice) "🔊 ভয়েস চলছে... ⏸️" else msg.message,
                                        fontSize = 12.sp,
                                        color = if (msg.isVoice) Color(0xFF1976D2) else Color(0xFF616161),
                                        fontWeight = if (msg.isVoice) FontWeight.Bold else FontWeight.Normal,
                                        modifier = if (msg.isVoice) {
                                            Modifier.clickable {
                                                playingVoiceMsgId = if (playingVoiceMsgId == msg.id) null else msg.id
                                            }
                                        } else Modifier
                                    )
                                }
                            }
                        }
                    }
                }

                }

                // ========================================================
                // 6. Lifted Bottom Messaging Bar (Always rises cleanly above Keyboard)
                // With Voice, Photos/Videos from Gallery, Various Emojis
                // ========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = if (isKeyboardOpen) 6.dp else 10.dp)
                ) {
                    // A. Various Emojis Tray (Expandable / Toggleable)
                    AnimatedVisibility(
                        visible = isEmojiTrayVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "✨ বিভিন্ন ধরনের জনপ্রিয় ইমুজি:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        "🤣", "😂", "❤️", "😍", "🔥", "🌹", "🤲",
                                        "🥳", "💋", "👏", "🎉", "👍", "💯", "😜",
                                        "🌸", "⚡", "👑", "🎁", "😇", "🥰"
                                    ).forEach { emoji ->
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFF5F5F5),
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clickable {
                                                    sendEmojiReaction(emoji)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // B. Voice Recording Overlay (Live visual feedback)
                    AnimatedVisibility(
                        visible = isVoiceRecording,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = BangladeshRed,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ভয়েস রেকর্ড হচ্ছে... 00:${if (voiceRecordDuration < 10) "0$voiceRecordDuration" else voiceRecordDuration}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = {
                                        isVoiceRecording = false
                                        voiceRecordDuration = 0
                                        Toast.makeText(context, "ভয়েস রেকর্ড বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("বাতিল", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            val dur = if (voiceRecordDuration == 0) 3 else voiceRecordDuration
                                            isVoiceRecording = false
                                            sendVoiceMessage(dur)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("পাঠান 🚀", color = BangladeshRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // C. Interactive Message Input Bar (Raised & Spacious, sits right above Keyboard)
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("party_room_chat_input_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice (বয়েজ) Record Button
                            IconButton(
                                onClick = {
                                    isVoiceRecording = !isVoiceRecording
                                    if (isVoiceRecording) {
                                        Toast.makeText(context, "🎙️ ভয়েস রেকর্ড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isVoiceRecording) Color(0xFFFFEBEE) else Color(0xFFF5F5F5))
                            ) {
                                Icon(
                                    imageVector = if (isVoiceRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Record",
                                    tint = if (isVoiceRecording) BangladeshRed else Color(0xFF616161),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Photo & Video Gallery Button (ছবি ও ভিডিও গ্যালারি)
                            IconButton(
                                onClick = {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F5))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = Color(0xFF43A047),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Message Input Text Field
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                BasicTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color(0xFF1B1B1B)
                                    ),
                                    maxLines = 3,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (chatInput.isNotBlank()) {
                                                sendTextMessage(chatInput)
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("party_room_chat_input"),
                                    decorationBox = { innerTextField ->
                                        if (chatInput.isEmpty()) {
                                            Text(
                                                text = "মেসেজ লিখুন...",
                                                fontSize = 13.5.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            // Emoji Tray Toggle Button (😊)
                            IconButton(
                                onClick = { isEmojiTrayVisible = !isEmojiTrayVisible },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Text("😊", fontSize = 18.sp)
                            }

                            // Send Button (active when input has text)
                            if (chatInput.isNotBlank()) {
                                Surface(
                                    shape = CircleShape,
                                    color = FacebookBlue,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("party_room_send_btn")
                                        .clickable { sendTextMessage(chatInput) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // D. Bottom Toolbar matching Screenshot (hides smoothly while keyboard is open so input sits flush above keyboard)
                    AnimatedVisibility(visible = !isKeyboardOpen) {
                    Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Chat toggle icon
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEEEEEE),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    isChatInputVisible = true
                                    Toast.makeText(context, "মেসেজ বক্স সচল", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                            }
                        }

                        // 2. Emoji smiley icon
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEEEEEE),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { isEmojiTrayVisible = !isEmojiTrayVisible }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("😊", fontSize = 20.sp)
                            }
                        }

                        // 3. Calendar icon with green notification dot
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { showEventDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Events", tint = Color.DarkGray, modifier = Modifier.size(22.dp))
                            // Green dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                        }

                        // 4. Four-square Grid Tools with green dot
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { showToolsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "Tools", tint = Color.DarkGray, modifier = Modifier.size(24.dp))
                            // Green dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                        }

                        // 5. Rose Gift Icon (🌹)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    giftCount += 10
                                    AppSoundManager.playSocialAdChime()
                                    sendEmojiReaction("🌹")
                                    Toast.makeText(context, "🌹 গোলাপ উপহার পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌹", fontSize = 22.sp)
                            }
                        }

                        // 6. Cyan Circular Gift Box Icon matching Screenshot (🎁)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E5FF),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { showGiftSheet = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Mega Gifts",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    }
                    }
                }
            }

            // Mega Gift Picker Sheet
            if (showGiftSheet) {
                AlertDialog(
                    onDismissRequest = { showGiftSheet = false },
                    title = { Text("🎁 বন্ধুদের উপহার পাঠান (Gifts)", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("রুমে থাকা বন্ধুদের উপহার দিয়ে সম্মান জানান:", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf(
                                    Triple("🌹 Rose", "10", "🌹"),
                                    Triple("💋 Kiss", "50", "💋"),
                                    Triple("👑 Crown", "200", "👑"),
                                    Triple("🚗 Car", "500", "🚗")
                                ).forEach { (name, coins, emoji) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF5F5F5),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clickable {
                                                giftCount += coins.toInt()
                                                showGiftSheet = false
                                                AppSoundManager.playSocialAdChime()
                                                sendEmojiReaction("$emoji $name")
                                                Toast.makeText(context, "$name পাঠানো হয়েছে! 🎉", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(emoji, fontSize = 24.sp)
                                            Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGiftSheet = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            // Room Events Dialog (Interactive with Reminder Bell Toggle)
            if (showEventDialog) {
                val roomEvents = remember {
                    listOf(
                        "ev_1" to "• আড্ডার বিশেষ সময়: প্রতিদিন রাত ৮:০০ - ১২:০০",
                        "ev_2" to "• আজকের প্রতিযোগিতা: সেরা গান ও কবিতা পাঠ",
                        "ev_3" to "• আসন্ন আয়োজন: শুক্রবার স্পেশাল ঈদ আড্ডা"
                    )
                }
                AlertDialog(
                    onDismissRequest = { showEventDialog = false },
                    title = { Text("📅 রুম ইভেন্ট ও শিডিউল", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            roomEvents.forEach { (id, label) ->
                                val isReminded = reminderSetEvents.contains(id)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isReminded) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            reminderSetEvents = if (isReminded) reminderSetEvents - id else reminderSetEvents + id
                                            Toast.makeText(
                                                context,
                                                if (!isReminded) "🔔 ইভেন্ট রিমাইন্ডার সেট হয়েছে!" else "রিমাইন্ডার বন্ধ হয়েছে",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text(
                                            text = if (isReminded) "🔔 সেট" else "রিমাইন্ডার",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isReminded) BangladeshGreen else FacebookBlue
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showEventDialog = false }) {
                            Text("ঠিক আছে")
                        }
                    }
                )
            }

            // Interactive Room Tools & Mini Games Dialog
            if (showToolsDialog) {
                AlertDialog(
                    onDismissRequest = { showToolsDialog = false },
                    title = { Text("⊞ মিনি গেম ও আড্ডা টুলস", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF8E1),
                                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showToolsDialog = false
                                        showLudoFromHangout = true
                                    }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎲", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("লুডু কিং মাল্টিপ্লেয়ার", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("বন্ধুদের সাথে সরাসরি লুডু খেলুন", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFE65100))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3E5F5),
                                border = BorderStroke(1.dp, Color(0xFFBA68C8)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showToolsDialog = false
                                        showLuckySpinDialog = true
                                    }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎡", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("লাকি স্পিন ও কয়েন রিওয়ার্ড", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("স্পিন করে ফ্রি গিফট কয়েন জিতুন", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF7B1FA2))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE3F2FD),
                                border = BorderStroke(1.dp, Color(0xFF64B5F6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showToolsDialog = false
                                        showEditRoomDialog = true
                                    }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("✏️", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("রুমের নাম ও টপিক এডিট", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("আড্ডার নাম ও ঘোষণা আপডেট করুন", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = FacebookBlue)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showToolsDialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            // Invite Online Friends Dialog (+ or Share action)
            if (showInviteFriendsDialog) {
                AlertDialog(
                    onDismissRequest = { showInviteFriendsDialog = false },
                    title = { Text("💌 আড্ডায় বন্ধুদের ইনভাইট করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(SampleData.profileFriends.take(8)) { friend ->
                                val isInvited = invitedFriendIds.contains(friend.id)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = friend.avatarUrl,
                                        contentDescription = friend.name,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        Text(if (friend.isOnline) "🟢 অনলাইনে আছেন" else "অফলাইন", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
                                            if (!isInvited) {
                                                invitedFriendIds = invitedFriendIds + friend.id
                                                memberCount++
                                                messages = messages + PartyRoomMessage(
                                                    id = System.currentTimeMillis().toString(),
                                                    senderName = friend.name,
                                                    senderAvatar = friend.avatarUrl,
                                                    message = "🎉 আড্ডা রুমে যোগ দিয়েছেন!"
                                                )
                                            }
                                        },
                                        enabled = !isInvited,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isInvited) Color.Gray else FacebookBlue
                                        )
                                    ) {
                                        Text(if (isInvited) "ইনভাইটেড ✓" else "ইনভাইট", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInviteFriendsDialog = false }) {
                            Text("সম্পন্ন")
                        }
                    }
                )
            }

            // Edit Room Name & Topic Dialog
            if (showEditRoomDialog) {
                var tempName by remember { mutableStateOf(groupNameInput) }
                var tempQuote by remember { mutableStateOf(roomQuote) }
                AlertDialog(
                    onDismissRequest = { showEditRoomDialog = false },
                    title = { Text("✏️ রুমের নাম ও ঘোষণা এডিট", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                label = { Text("রুমের নাম") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tempQuote,
                                onValueChange = { tempQuote = it },
                                label = { Text("রুমের ঘোষণা / টপিক") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) groupNameInput = tempName
                                if (tempQuote.isNotBlank()) roomQuote = tempQuote
                                showEditRoomDialog = false
                                Toast.makeText(context, "রুমের তথ্য আপডেট হয়েছে ✓", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                        ) {
                            Text("সেভ করুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditRoomDialog = false }) { Text("বাতিল") }
                    }
                )
            }

            // Lucky Spin Mini-Game Dialog
            if (showLuckySpinDialog) {
                var lastSpinResult by remember { mutableStateOf<Int?>(null) }
                AlertDialog(
                    onDismissRequest = { showLuckySpinDialog = false },
                    title = { Text("🎡 লাকি স্পিন ও কয়েন রিওয়ার্ড", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎡", fontSize = 52.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lastSpinResult?.let { "🎉 অভিনন্দন! আপনি +$it গিফট কয়েন জিতেছেন!" }
                                    ?: "স্পিন করে ফ্রি গিফট কয়েন জিতে নিন!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = if (lastSpinResult != null) BangladeshGreen else Color.DarkGray
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val reward = listOf(50, 100, 200, 500).random()
                                lastSpinResult = reward
                                giftCount += reward
                                AppSoundManager.playSocialAdChime()
                                sendEmojiReaction("🎡 +$reward কয়েন জিতেছেন!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                        ) {
                            Text("স্পিন করুন 🎡", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLuckySpinDialog = false }) { Text("বন্ধ করুন") }
                    }
                )
            }

            // Direct Ludo King Launch from Hangout Tools
            if (showLudoFromHangout) {
                LudoKingGroupDialog(
                    viewModel = viewModel,
                    onDismiss = { showLudoFromHangout = false }
                )
            }

            // User Profile & Follow / Follow Back Dialog (মেসেঞ্জারে বন্ধুদের আড্ডায় ফলো এবং ফলো ব্যাক বাটন)
            if (selectedUserProfile != null) {
                val profile = selectedUserProfile!!
                val isFollowed = followedUserIds.contains(profile.id)
                val isFollowingMe = profile.id == "a1" || profile.id == "a3"

                AlertDialog(
                    onDismissRequest = { selectedUserProfile = null },
                    icon = {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = profile.name,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, Color(0xFF00B0FF), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("আইডি লিংক: facefeed.com/u/${profile.id}", fontSize = 11.sp, color = Color.Gray)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isFollowed) "✅ আপনি এই বন্ধুকে ফলো করছেন"
                                       else if (isFollowingMe) "💌 এই বন্ধু আপনাকে ফলো করছে! ফলো ব্যাক দিতে পারেন"
                                       else "🌟 আড্ডার নতুন বন্ধুকে ফলো করুন",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = if (isFollowed) Color(0xFF2E7D32) else FacebookBlue
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isFollowed) {
                                    followedUserIds = followedUserIds - profile.id
                                    Toast.makeText(context, "${profile.name}-কে আনফলো করা হয়েছে", Toast.LENGTH_SHORT).show()
                                } else {
                                    followedUserIds = followedUserIds + profile.id
                                    AppSoundManager.playSocialAdChime()
                                    val msg = if (isFollowingMe) "${profile.name}-কে ফলো ব্যাক দিয়েছেন! 🤝" else "${profile.name}-কে ফলো করেছেন! 🌟"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                selectedUserProfile = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowed) Color(0xFF757575) else if (isFollowingMe) FacebookBlue else Color(0xFF00B0FF)
                            )
                        ) {
                            Text(
                                text = if (isFollowed) "আনফলো করুন" else if (isFollowingMe) "ফলো ব্যাক দিন 🤝" else "ফলো করুন ➕",
                                color = Color.White
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedUserProfile = null }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            // Live Online Users in Hangout Dialog
            if (showOnlineUsersDialog) {
                AlertDialog(
                    onDismissRequest = { showOnlineUsersDialog = false },
                    title = {
                        Text(
                            text = "🟢 এই আড্ডায় লাইনে আছে (${totalLiveOnlineUsers} জন)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BangladeshGreen
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "মঞ্চে ও দর্শক সারিতে সক্রিয় সদস্যরা:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(seats.filter { it.user != null || it.customAvatarUrl != null }) { seat ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF1F8E9))
                                            .padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = seat.customAvatarUrl ?: seat.user?.avatarUrl ?: "",
                                            contentDescription = seat.badgeLabel,
                                            modifier = Modifier.size(32.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = seat.badgeLabel ?: "সিট #${seat.seatNumber}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("🎙️ মঞ্চে (${seat.seatNumber})", fontSize = 10.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                items(audienceMembers) { aud ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF5F5F5))
                                            .padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = aud.avatarUrl,
                                            contentDescription = aud.name,
                                            modifier = Modifier.size(32.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = aud.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("🟢 অনলাইনে", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showOnlineUsersDialog = false }) {
                            Text("ঠিক আছে", color = BangladeshGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Seat Item representing each of the 10 seats in Screenshot
 */
@Composable
fun PartyVoiceSeatItem(
    seat: PartyVoiceSeat,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            val avatarToUse = seat.customAvatarUrl ?: seat.user?.avatarUrl

            if (avatarToUse != null) {
                // Active user avatar with optional cyan ring or speaking green ring
                AsyncImage(
                    model = avatarToUse,
                    contentDescription = seat.badgeLabel,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (seat.hasCyanRing) 2.5.dp else 1.5.dp,
                            color = if (seat.hasCyanRing) Color(0xFF00E5FF) else if (seat.isSpeaking) Color(0xFF00E676) else Color(0xFFB0BEC5),
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                // Mute mic badge or blue mic badge matching Screenshot
                if (seat.isMuted || seat.micBadgeColor != null) {
                    val badgeColor = seat.micBadgeColor ?: Color.Black.copy(alpha = 0.65f)
                    Surface(
                        shape = CircleShape,
                        color = badgeColor,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic State",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty / Locked seat (matching dashed circle with Lock and number from screenshot)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFCFD8DC)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (seat.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFB0BEC5),
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Open Seat",
                                tint = Color(0xFF00B0FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Badge / Name label matching Screenshot
        Text(
            text = seat.badgeLabel ?: "${seat.seatNumber}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
