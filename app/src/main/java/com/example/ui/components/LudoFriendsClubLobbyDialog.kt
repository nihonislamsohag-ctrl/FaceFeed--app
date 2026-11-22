package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

data class LudoLobbyChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val time: String = "Just now",
    val photoUri: Uri? = null,
    val samplePhotoUrl: String? = null,
    val isScreenshotCard: Boolean = false,
    val screenshotSummary: String? = null,
    val isFromMe: Boolean = false,
    val isSystem: Boolean = false
)

/**
 * 🎲 Ludo Friends Club Lobby & Live Chat Dialog
 * - Shows total players currently online in Ludo King (লাইনে আছে)
 * - Supports 2, 3, or 4 player incremental joining ("২, ৩, ৪ এই ভাবে এক হলে একজনে জয়েন দিবে")
 * - Full group chat with text, photos from gallery, and screenshots sharing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoFriendsClubLobbyDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onLaunchGame: (GameMode) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Chat + Photos/Screenshots, 1 = Online Friends & Join 2/3/4
    var targetPlayerCount by remember { mutableIntStateOf(4) } // 2, 3, or 4

    // Active online friends list from SampleData + allAppUsers
    val onlineFriends = remember(allUsers, currentUser.id) {
        val sampleAsUsers = SampleData.profileFriends.map {
            User(
                id = it.id,
                fullName = it.name,
                username = it.username,
                avatarUrl = it.avatarUrl,
                isOnline = it.isOnline
            )
        }
        (allUsers.filter { it.id != currentUser.id } + sampleAsUsers)
            .distinctBy { it.id }
            .take(12)
    }

    // Joined seats list (Host + friends joining one-by-one up to targetPlayerCount)
    val joinedFriends = remember { mutableStateListOf<User>() }
    val invitedIds = remember { mutableStateListOf<String>() }
    val totalOnlineInLudo = onlineFriends.count { it.isOnline } + joinedFriends.size + 1

    var chatInput by remember { mutableStateOf("") }
    var fullScreenPhotoPreview by remember { mutableStateOf<Pair<Uri?, String?>?>(null) }
    var showScreenshotPickerDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val chatMessages = remember { mutableStateListOf<LudoLobbyChatMessage>() }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Zero-permission Android Photo Picker for sending Photos or Gallery Screenshots
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            AppSoundManager.playSocialAdChime()
            chatMessages.add(
                LudoLobbyChatMessage(
                    id = "lm_img_${System.currentTimeMillis()}",
                    senderName = currentUser.fullName,
                    senderAvatar = currentUser.avatarUrl,
                    text = "ছবি / স্ক্রিনশট শেয়ার করেছেন 📸",
                    photoUri = uri,
                    isFromMe = true
                )
            )
            Toast.makeText(context, "ছবি / স্ক্রিনশট গ্রুপ চ্যাটে পাঠানো হয়েছে! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    fun joinFriendStepByStep(friend: User) {
        if (joinedFriends.any { it.id == friend.id }) {
            Toast.makeText(context, "${friend.fullName} আগেই টেবিলে জয়েন দিয়েছেন", Toast.LENGTH_SHORT).show()
            return
        }
        val maxFriendsAllowed = (targetPlayerCount - 1).coerceAtLeast(1)
        if (joinedFriends.size >= maxFriendsAllowed) {
            Toast.makeText(context, "$targetPlayerCount জনের টেবিল পূর্ণ হয়েছে! নিচে 'খেলা শুরু করুন' চাপুন 🎲", Toast.LENGTH_SHORT).show()
            return
        }
        joinedFriends.add(friend)
        val totalSeated = 1 + joinedFriends.size
        AppSoundManager.playSocialAdChime()
        chatMessages.add(
            LudoLobbyChatMessage(
                id = "lm_join_${System.currentTimeMillis()}",
                senderName = "Ludo Lobby",
                senderAvatar = friend.avatarUrl,
                text = "🎉 ${friend.fullName} লুডু টেবিলে জয়েন দিয়েছেন! ($totalSeated/$targetPlayerCount জন প্রস্তুত)",
                isSystem = true
            )
        )
    }

    BackHandler { onDismiss() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070C28)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 1. Top Header with Back Button + Title + Live Online Counter
                Surface(
                    color = Color(0xFF0E1542),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎲 লুডু বন্ধু ক্লাব ও ইনভাইট লবি",
                                        color = Color(0xFFFFE082),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    FidaVerifiedBadge(size = 15.dp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                    )
                                    Text(
                                        text = "লুডু কিং লাইনে আছে: $totalOnlineInLudo জন",
                                        color = Color(0xFF69F0AE),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Player Count Mode Selector (২ জন, ৩ জন, ৪ জন - এক এক করে জয়েন)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ম্যাচ সিট:",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            listOf(2 to "২ জন", 3 to "৩ জন", 4 to "৪ জন").forEach { (count, label) ->
                                val selected = targetPlayerCount == count
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) BangladeshGreen else Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.clickable {
                                        targetPlayerCount = count
                                        while (joinedFriends.size > count - 1) {
                                            joinedFriends.removeAt(joinedFriends.lastIndex)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color(0xFFFFE082) else Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Seat Row showing Host + Joined Players 2, 3, 4
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Seat 1 (Host / You)
                            LudoLobbySeatChip(
                                seatIndex = 1,
                                name = "আপনি (হোস্ট)",
                                avatarUrl = currentUser.avatarUrl,
                                isOccupied = true,
                                modifier = Modifier.weight(1f),
                                onClick = {}
                            )
                            for (slotIdx in 2..targetPlayerCount) {
                                val joinedUser = joinedFriends.getOrNull(slotIdx - 2)
                                LudoLobbySeatChip(
                                    seatIndex = slotIdx,
                                    name = joinedUser?.fullName?.split(" ")?.firstOrNull() ?: "খালি ($slotIdx)",
                                    avatarUrl = joinedUser?.avatarUrl,
                                    isOccupied = joinedUser != null,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (joinedUser != null) {
                                            joinedFriends.remove(joinedUser)
                                        } else {
                                            selectedTab = 1
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Two Tab Switcher: [💬 গ্রুপ চ্যাট, ছবি ও স্ক্রিনশট] | [👥 বন্ধু ইনভাইট ও জয়েন]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A1033))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTab == 0) Color(0xFF1565C0) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "💬 চ্যাট, ছবি ও স্ক্রিনশট (${chatMessages.size})",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTab == 1) BangladeshGreen else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "👥 বন্ধু এড / জয়েন (${onlineFriends.size})",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // 4. Main Content Area
                if (selectedTab == 0) {
                    // CHAT + PHOTOS + SCREENSHOTS
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            if (msg.isSystem) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1B5E20).copy(alpha = 0.45f),
                                    border = BorderStroke(0.8.dp, Color(0xFF69F0AE).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color(0xFFB9F6CA),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (!msg.isFromMe) {
                                        AsyncImage(
                                            model = msg.senderAvatar,
                                            contentDescription = msg.senderName,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (msg.isFromMe) Color(0xFF1565C0) else Color(0xFF161E52),
                                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.15f)),
                                        modifier = Modifier.widthIn(max = 265.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            if (!msg.isFromMe) {
                                                Text(
                                                    text = msg.senderName,
                                                    color = Color(0xFFFFD54F),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }
                                            if (msg.text.isNotBlank()) {
                                                Text(
                                                    text = msg.text,
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            if (msg.photoUri != null || msg.samplePhotoUrl != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                AsyncImage(
                                                    model = msg.photoUri ?: msg.samplePhotoUrl,
                                                    contentDescription = "Shared Photo or Screenshot",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(150.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            fullScreenPhotoPreview = msg.photoUri to msg.samplePhotoUrl
                                                        },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (msg.isScreenshotCard) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(0xFF0A1033),
                                                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.7f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.img_ludo_king_policy),
                                                            contentDescription = "Ludo Screenshot Preview",
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(100.dp)
                                                                .clip(RoundedCornerShape(8.dp)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = msg.screenshotSummary ?: "🎲 লুডু লাইভ স্ক্রিনশট",
                                                            color = Color(0xFFFFE082),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = msg.time,
                                                color = Color.White.copy(alpha = 0.55f),
                                                fontSize = 9.5.sp,
                                                modifier = Modifier.align(Alignment.End)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick invite & reply chips above input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("কে ২/৩/৪ জনে খেলবে আসো! 🎲", "আমি রেডি 👍", "সবাই জয়েন দাও 🔥").forEach { quickMsg ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.clickable {
                                    chatMessages.add(
                                        LudoLobbyChatMessage(
                                            id = "lm_q_${System.currentTimeMillis()}",
                                            senderName = currentUser.fullName,
                                            senderAvatar = currentUser.avatarUrl,
                                            text = quickMsg,
                                            isFromMe = true
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = quickMsg,
                                    color = Color(0xFFFFE082),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Chat input + Photo + Screenshot buttons
                    Surface(
                        color = Color(0xFF0E1542),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Photo Picker Button
                            IconButton(
                                onClick = {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1565C0))
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Send Photo", tint = Color.White, modifier = Modifier.size(19.dp))
                            }

                            // Screenshot Share Button
                            IconButton(
                                onClick = { showScreenshotPickerDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6A1B9A))
                            ) {
                                Icon(Icons.Default.Screenshot, contentDescription = "Share Screenshot", tint = Color.White, modifier = Modifier.size(19.dp))
                            }

                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("চ্যাট ও ইনভাইট লিখুন...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD54F),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                                ),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        AppSoundManager.playSocialAdChime()
                                        chatMessages.add(
                                            LudoLobbyChatMessage(
                                                id = "lm_t_${System.currentTimeMillis()}",
                                                senderName = currentUser.fullName,
                                                senderAvatar = currentUser.avatarUrl,
                                                text = chatInput.trim(),
                                                isFromMe = true
                                            )
                                        )
                                        chatInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BangladeshGreen)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                } else {
                    // ONLINE FRIENDS LIST + ADD / INVITE / 2-3-4 INCREMENTAL JOIN
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF16215C),
                                border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 এক বন্ধু আরেক বন্ধুকে এড/ইনভাইট করুন — ২, ৩ বা ৪ জন এইভাবে এক এক করে জয়েন হলেই সরাসরি ম্যাচ শুরু হবে!",
                                    color = Color(0xFFFFE082),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        items(onlineFriends, key = { it.id }) { friend ->
                            val isJoined = joinedFriends.any { it.id == friend.id }
                            val isInvited = invitedIds.contains(friend.id)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF101847),
                                border = BorderStroke(
                                    1.dp,
                                    if (isJoined) Color(0xFF00E676) else Color.White.copy(alpha = 0.14f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        FotoFilmAvatar(
                                            imageUrl = friend.avatarUrl,
                                            name = friend.fullName,
                                            contentDescription = friend.fullName,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                        )
                                        if (friend.isOnline) {
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .clip(CircleShape)
                                                    .background(OnlineGreen)
                                                    .border(1.5.dp, Color(0xFF101847), CircleShape)
                                                    .align(Alignment.BottomEnd)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = friend.fullName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isJoined) "✅ টেবিলে জয়েন করেছেন"
                                            else if (friend.isOnline) "🟢 লুডু লাইনে আছে"
                                            else "⚪ অফলাইন",
                                            color = if (isJoined || friend.isOnline) Color(0xFF69F0AE) else Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                if (!isInvited) {
                                                    invitedIds.add(friend.id)
                                                    chatMessages.add(
                                                        LudoLobbyChatMessage(
                                                            id = "lm_inv_${System.currentTimeMillis()}",
                                                            senderName = "Invite",
                                                            senderAvatar = friend.avatarUrl,
                                                            text = "📩 ${friend.fullName}-কে লুডু খেলার ইনভাইট পাঠানো হয়েছে!",
                                                            isSystem = true
                                                        )
                                                    )
                                                    Toast.makeText(context, "${friend.fullName}-কে ইনভাইট পাঠানো হয়েছে 📩", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isInvited) "ইনভাইটেড" else "এড ➕",
                                                color = Color(0xFFFFE082),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (isJoined) {
                                                    joinedFriends.removeAll { it.id == friend.id }
                                                } else {
                                                    joinFriendStepByStep(friend)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isJoined) BangladeshRed else BangladeshGreen
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isJoined) "বাদ দিন" else "জয়েন 🎲",
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Bottom Start Match CTA reflecting 2, 3, 4 Players Join Progress
                val totalJoinedNow = 1 + joinedFriends.size
                val isTableFull = totalJoinedNow >= targetPlayerCount
                val launchMode = when (targetPlayerCount) {
                    2 -> GameMode.TWO_BIJOR
                    3 -> GameMode.THREE_BIJOR
                    else -> GameMode.FOUR_BIJOR
                }

                Surface(
                    color = Color(0xFF0A1033),
                    border = BorderStroke(1.dp, if (isTableFull) Color(0xFF00E676) else Color(0xFFFFD54F).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTableFull) "🎉 $totalJoinedNow/$targetPlayerCount জন জয়েন পূর্ণ!" else "জয়েন প্রগ্রেস: $totalJoinedNow / $targetPlayerCount জন",
                                color = if (isTableFull) Color(0xFF69F0AE) else Color(0xFFFFE082),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isTableFull) "সবাই প্রস্তুত, এখনই লুডু শুরু করুন!" else "আরও ${targetPlayerCount - totalJoinedNow} জন জয়েন করতে 'বন্ধু এড / জয়েন' চাপুন",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onLaunchGame(launchMode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTableFull) Color(0xFF00C853) else FacebookBlue
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isTableFull) "🎲 খেলুন" else "দ্রুত শুরু",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to share Screenshot from gallery OR instant Ludo Live Screenshot card
    if (showScreenshotPickerDialog) {
        AlertDialog(
            onDismissRequest = { showScreenshotPickerDialog = false },
            containerColor = Color(0xFF0E1542),
            title = {
                Text("📸 স্ক্রিনশট শেয়ার করুন", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "আপনার গ্যালারি থেকে ফোনের স্ক্রিনশট বা সরাসরি এই লুডু রুমের স্ক্রিনশট কার্ড বন্ধুদের চ্যাটে পাঠান:",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.5.sp
                    )
                    Button(
                        onClick = {
                            showScreenshotPickerDialog = false
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("গ্যালারি থেকে স্ক্রিনশট / ছবি বাছুন")
                    }
                    Button(
                        onClick = {
                            showScreenshotPickerDialog = false
                            AppSoundManager.playSocialAdChime()
                            chatMessages.add(
                                LudoLobbyChatMessage(
                                    id = "lm_ss_${System.currentTimeMillis()}",
                                    senderName = currentUser.fullName,
                                    senderAvatar = currentUser.avatarUrl,
                                    text = "লাইভ লুডু লবি স্ক্রিনশট শেয়ার করলাম 📸🎲",
                                    isScreenshotCard = true,
                                    screenshotSummary = "🎲 লাইভ রুম (${1 + joinedFriends.size}/$targetPlayerCount প্লেয়ার) • লাইনে: $totalOnlineInLudo জন",
                                    isFromMe = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Screenshot, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ইনস্ট্যান্ট লুডু স্ক্রিনশট কার্ড পাঠান")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScreenshotPickerDialog = false }) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }

    // Full-screen photo/screenshot viewer
    fullScreenPhotoPreview?.let { (uri, url) ->
        Dialog(
            onDismissRequest = { fullScreenPhotoPreview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenPhotoPreview = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uri ?: url,
                    contentDescription = "Full Preview",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenPhotoPreview = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LudoLobbySeatChip(
    seatIndex: Int,
    name: String,
    avatarUrl: String?,
    isOccupied: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isOccupied) Color(0xFF1B5E20).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isOccupied) Color(0xFF00E676) else Color.White.copy(alpha = 0.25f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isOccupied && !avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF00E676), CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = if (isOccupied) BangladeshGreen else Color.White.copy(alpha = 0.14f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isOccupied) name.take(1) else "+",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = name,
                color = if (isOccupied) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
