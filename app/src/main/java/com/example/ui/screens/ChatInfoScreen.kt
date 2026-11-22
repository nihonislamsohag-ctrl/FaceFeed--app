package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Conversation
import com.example.model.User
import com.example.ui.components.FotoFilmAvatar
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.util.AppSoundManager
import com.example.util.MediaSaveHelper
import kotlinx.coroutines.launch
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInfoScreen(
    conversation: Conversation,
    viewModel: FaceFeedViewModel,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onStartAudioCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onDeleteConversation: () -> Unit
) {
    var show3DotMenu by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showMediaFilesDialog by remember { mutableStateOf(false) }
    var showPinnedMessagesDialog by remember { mutableStateOf(false) }
    val conversations by viewModel.conversations.collectAsState()
    val currentConv = conversations.find { it.id == conversation.id } ?: conversation
    val isBlocked = currentConv.isBlocked

    var showMuteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showBlockedSuccessDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showRestrictDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showShareContactDialog by remember { mutableStateOf(false) }
    var showProfileLinkDialog by remember { mutableStateOf(false) }
    var showOpenByLinkDialog by remember { mutableStateOf(false) }
    var viewingMediaFromInfo by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allConversations by viewModel.conversations.collectAsState()
    val profileIdLink = remember(currentConv.friend) {
        viewModel.buildProfileLink(currentConv.friend)
    }

    // Settings states
    var readReceiptsOn by remember { mutableStateOf(true) }
    var typingIndicatorOn by remember { mutableStateOf(true) }
    var notifSoundsOn by remember { mutableStateOf(true) }
    var friendNickname by remember { mutableStateOf(currentConv.friend.fullName) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Toast auto-clear
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2200)
            toastMessage = null
        }
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { show3DotMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = show3DotMenu,
                            onDismissRequest = { show3DotMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Profile") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FacebookBlue) },
                                onClick = {
                                    show3DotMenu = false
                                    onOpenProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("আইডির লিংক কপি ও শেয়ার 🔗") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = BangladeshGreen) },
                                onClick = {
                                    show3DotMenu = false
                                    showProfileLinkDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("লিংক দিয়ে আইডিতে ঢুকুন 🔍") },
                                leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = FacebookBlue) },
                                onClick = {
                                    show3DotMenu = false
                                    showOpenByLinkDialog = true
                                }
                            )
                            if (!isBlocked) {
                                DropdownMenuItem(
                                    text = { Text("Audio Call") },
                                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = FacebookBlue) },
                                    onClick = {
                                        show3DotMenu = false
                                        onStartAudioCall()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Video Call") },
                                    leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, tint = FacebookBlue) },
                                    onClick = {
                                        show3DotMenu = false
                                        onStartVideoCall()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mute Notifications") },
                                    leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
                                    onClick = {
                                        show3DotMenu = false
                                        showMuteDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block") },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = BangladeshRed) },
                                    onClick = {
                                        show3DotMenu = false
                                        showBlockDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Unblock") },
                                    leadingIcon = { Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = FacebookBlue) },
                                    onClick = {
                                        show3DotMenu = false
                                        showUnblockDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 40.dp)
            ) {
                // Profile Header (Matching Screenshot 2)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { onOpenProfile() }
                    ) {
                        FotoFilmAvatar(
                            imageUrl = conversation.friend.avatarUrl,
                            name = conversation.friend.fullName,
                            contentDescription = conversation.friend.fullName,
                            modifier = Modifier
                                .size(105.dp)
                                .clip(CircleShape)
                        )
                        if (conversation.friend.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = friendNickname,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "@${conversation.friend.username} • FotoFilm",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FacebookBlue.copy(alpha = 0.10f),
                        modifier = Modifier.clickable { showProfileLinkDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = "Profile Link", tint = FacebookBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = profileIdLink,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FacebookBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!isBlocked) {
                    // 4 Action Buttons Row: [Profile] [Audio] [Video] [Mute] (Matching Messenger)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChatActionCircleButton(
                            icon = Icons.Default.Person,
                            label = "Profile",
                            onClick = onOpenProfile
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.Call,
                            label = "Audio",
                            onClick = onStartAudioCall
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.Videocam,
                            label = "Video",
                            onClick = onStartVideoCall
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.NotificationsOff,
                            label = "Mute",
                            onClick = { showMuteDialog = true }
                        )
                    }

                    // Secondary Quick Actions Row: [Search] [Nicknames] [Theme] [Media]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChatActionCircleButton(
                            icon = Icons.Default.Search,
                            label = "Search",
                            onClick = { showSearchDialog = true }
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.TextFields,
                            label = "Nicknames",
                            onClick = { showNicknameDialog = true }
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.Palette,
                            label = "Theme",
                            onClick = { showCustomizeDialog = true }
                        )
                        ChatActionCircleButton(
                            icon = Icons.Default.PhotoLibrary,
                            label = "Media",
                            onClick = { showMediaFilesDialog = true }
                        )
                    }
                } else {
                    // Blocked State (Matching Screenshot 3): Only Profile button is shown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ChatActionCircleButton(
                            icon = Icons.Default.Person,
                            label = "Profile",
                            onClick = onOpenProfile
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Section: Chat info (Matching Screenshot 2)
                SectionTitle("Chat info")

                // Media Mosaic Gallery Preview (real chat media only)
                val sharedMediaList = remember(currentConv.messages) {
                    currentConv.messages
                        .filter { !it.mediaUrl.isNullOrBlank() }
                        .map { it.mediaUrl!! to it.isMediaVideo }
                }
                if (sharedMediaList.isNotEmpty()) {
                    MediaPreviewMosaic(
                        mediaItems = sharedMediaList,
                        onClick = { showMediaFilesDialog = true }
                    )
                }

                ChatInfoRow(
                    icon = Icons.Default.PhotoLibrary,
                    title = "View all media, files & links",
                    onClick = { showMediaFilesDialog = true }
                )

                ChatInfoRow(
                    icon = Icons.Default.PushPin,
                    title = "Pinned messages",
                    onClick = { showPinnedMessagesDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Actions (Matching Screenshot 3)
                SectionTitle("Actions")

                ChatInfoRow(
                    icon = Icons.Default.NotificationsOff,
                    title = "Mute ${conversation.friend.fullName}",
                    onClick = { showMuteDialog = true }
                )

                ChatInfoRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Notifications & sounds",
                    subtitle = if (notifSoundsOn) "On" else "Off",
                    onClick = {
                        notifSoundsOn = !notifSoundsOn
                        toastMessage = if (notifSoundsOn) "Notifications sound: On" else "Notifications sound: Off"
                    }
                )

                ChatInfoRow(
                    icon = Icons.Default.GroupAdd,
                    title = "Create group chat with ${conversation.friend.fullName}",
                    onClick = { showCreateGroupDialog = true }
                )

                ChatInfoRow(
                    icon = Icons.Default.Share,
                    title = "Share contact",
                    onClick = { showShareContactDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Privacy & support (Matching Screenshot 3)
                SectionTitle("Privacy & support")

                ChatInfoSwitchRow(
                    icon = Icons.Default.Visibility,
                    title = "Read receipts",
                    subtitle = if (readReceiptsOn) "On" else "Off",
                    checked = readReceiptsOn,
                    onCheckedChange = {
                        readReceiptsOn = it
                        toastMessage = "Read receipts updated"
                    }
                )

                ChatInfoSwitchRow(
                    icon = Icons.Default.MoreHoriz,
                    title = "Typing indicator",
                    subtitle = if (typingIndicatorOn) "On" else "Off",
                    checked = typingIndicatorOn,
                    onCheckedChange = {
                        typingIndicatorOn = it
                        toastMessage = "Typing indicator updated"
                    }
                )

                ChatInfoRow(
                    icon = Icons.Default.Security,
                    title = "Message permissions",
                    onClick = { toastMessage = "Message permissions: Standard" }
                )

                if (!isBlocked) {
                    ChatInfoRow(
                        icon = Icons.Default.Block,
                        title = "Restrict",
                        onClick = { showRestrictDialog = true }
                    )

                    ChatInfoRow(
                        icon = Icons.Default.RemoveCircleOutline,
                        title = "Block",
                        onClick = { showBlockDialog = true }
                    )

                    ChatInfoRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Delete conversation",
                        titleColor = BangladeshRed,
                        iconTint = BangladeshRed,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                } else {
                    // Blocked state (Matching Screenshot 3)
                    ChatInfoRow(
                        icon = Icons.Default.RemoveCircle,
                        title = "Unblock",
                        onClick = { showUnblockDialog = true }
                    )

                    ChatInfoRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Delete conversation",
                        titleColor = BangladeshRed,
                        iconTint = BangladeshRed,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }

                SectionTitle("More actions")

                ChatInfoRow(
                    icon = Icons.Default.Share,
                    title = "Share contact",
                    onClick = { showShareContactDialog = true }
                )
            }

            // Floating Toast
            toastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    // Dialog: Edit Nicknames
    if (showNicknameDialog) {
        var tempNickname by remember { mutableStateOf(friendNickname) }
        AlertDialog(
            onDismissRequest = { showNicknameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nicknames", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text("Set a nickname for ${conversation.friend.fullName} in this chat:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempNickname,
                        onValueChange = { tempNickname = it },
                        placeholder = { Text("Enter nickname") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        friendNickname = if (tempNickname.isNotBlank()) tempNickname else conversation.friend.fullName
                        showNicknameDialog = false
                        toastMessage = "Nickname set to \"$friendNickname\""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Search in Conversation
    if (showSearchDialog) {
        var chatSearchText by remember { mutableStateOf("") }
        val foundMessages = remember(chatSearchText, conversation.messages) {
            if (chatSearchText.isBlank()) emptyList()
            else conversation.messages.filter { it.text.contains(chatSearchText, ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = {
                Text("Search in Conversation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = chatSearchText,
                        onValueChange = { chatSearchText = it },
                        placeholder = { Text("Search messages...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (chatSearchText.isNotBlank()) {
                        Text("${foundMessages.size} matching message(s) found", fontSize = 12.sp, color = FacebookBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            foundMessages.take(4).forEach { msg ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(msg.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Customize Chat Theme & Emoji
    if (showCustomizeDialog) {
        val emojis = listOf("👍", "❤️", "🔥", "😂", "🎉", "👏", "😮", "🙏")
        val themeColors = listOf(FacebookBlue, Color(0xFF673AB7), Color(0xFFE91E63), BangladeshGreen, Color(0xFFFF9800), Color(0xFF009688))

        AlertDialog(
            onDismissRequest = { showCustomizeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customize Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme Color", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        themeColors.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable {
                                        showCustomizeDialog = false
                                        toastMessage = "Theme color updated"
                                    }
                            )
                        }
                    }

                    Text("Quick Reaction Emoji", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojis.forEach { em ->
                            Text(
                                text = em,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        showCustomizeDialog = false
                                        toastMessage = "Quick reaction set to $em"
                                    }
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomizeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Dialog: Media, Files & Links (with full View + Save & Download to Gallery)
    if (showMediaFilesDialog) {
        val chatMediaItems = remember(currentConv.messages) {
            currentConv.messages
                .filter { !it.mediaUrl.isNullOrBlank() }
                .map { it.mediaUrl!! to it.isMediaVideo }
                .distinctBy { it.first }
        }

        AlertDialog(
            onDismissRequest = { showMediaFilesDialog = false },
            title = {
                Text("Media, Files & Links", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ছবি/ভিডিও ট্যাপ করে দেখুন অথবা ⬇️ বাটনে চাপ দিয়ে সেইভ ও ডাউনলোড করুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMediaItems.chunked(2)) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (url, isVid) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewingMediaFromInfo = url to isVid
                                            }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = if (isVid) "Video" else "Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (isVid) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .align(Alignment.Center),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Surface(
                                            onClick = {
                                                Toast.makeText(context, "⏳ ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                                                scope.launch {
                                                    val res = MediaSaveHelper.saveMediaToGallery(context, url, isVideo = isVid)
                                                    if (res.isSuccess) {
                                                        AppSoundManager.playReactionChime()
                                                        toastMessage = if (isVid) "✅ ভিডিও গ্যালারিতে সেইভ হয়েছে!" else "✅ ছবি গ্যালারিতে সেইভ হয়েছে!"
                                                    } else {
                                                        toastMessage = "⚠️ ডাউনলোড ব্যর্থ হয়েছে"
                                                    }
                                                }
                                            },
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.72f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(5.dp)
                                                .size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Download, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaFilesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Fullscreen Viewer from ChatInfo media gallery
    viewingMediaFromInfo?.let { (mediaUrl, isVid) ->
        if (isVid) {
            FullScreenVideoViewerDialog(
                videoUrl = mediaUrl,
                onDismiss = { viewingMediaFromInfo = null }
            )
        } else {
            FullScreenImageViewerDialog(
                imageUrl = mediaUrl,
                onDismiss = { viewingMediaFromInfo = null }
            )
        }
    }

    // Dialog: Friend Profile Link (Copy, Send to ChatFilm friends, Share, Open Profile)
    if (showProfileLinkDialog) {
        var showFriendSendList by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showProfileLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${currentConv.friend.fullName}-এর আইডির লিংক", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FacebookBlue.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "এই লিংক যে কাউকে দিলে সে সরাসরি ${currentConv.friend.fullName}-এর আইডিতে ঢুকে সব পোস্টে লাইক, কমেন্ট, শেয়ার ও কমেন্ট রিপ্লাই করতে পারবে:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = profileIdLink,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FacebookBlue
                            )
                        }
                    }

                    // 1. Copy Link Button
                    OutlinedButton(
                        onClick = {
                            MediaSaveHelper.copyToClipboard(context, "Profile Link", profileIdLink)
                            AppSoundManager.playReactionChime()
                            toastMessage = "📋 আইডির লিংক কপি হয়েছে!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("লিংক কপি করুন (Copy Link)", fontWeight = FontWeight.SemiBold)
                    }

                    // 2. Send in ChatFilm toggle
                    OutlinedButton(
                        onClick = { showFriendSendList = !showFriendSendList },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ChatFilm-এ বন্ধুকে লিংক পাঠান", fontWeight = FontWeight.SemiBold, color = BangladeshGreen)
                    }

                    if (showFriendSendList) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyColumn(modifier = Modifier.heightIn(max = 160.dp).padding(6.dp)) {
                                items(allConversations) { targetConv ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.sendMessage(
                                                    conversationId = targetConv.id,
                                                    text = "${currentConv.friend.fullName}-এর প্রোফাইল আইডি লিংক:\n$profileIdLink"
                                                )
                                                AppSoundManager.playReactionChime()
                                                toastMessage = "✅ ${targetConv.groupName ?: targetConv.friend.fullName}-কে লিংক পাঠানো হয়েছে!"
                                                showProfileLinkDialog = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FotoFilmAvatar(
                                            imageUrl = targetConv.friend.avatarUrl,
                                            name = targetConv.groupName ?: targetConv.friend.fullName,
                                            contentDescription = null,
                                            isGroup = targetConv.isGroup,
                                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = targetConv.groupName ?: targetConv.friend.fullName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("পাঠান ➡️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Share Externally
                    OutlinedButton(
                        onClick = {
                            MediaSaveHelper.shareTextOrMediaLink(
                                context,
                                "${currentConv.friend.fullName}-এর FotoFilm আইডি লিংক: $profileIdLink",
                                subject = "${currentConv.friend.fullName} প্রোফাইল"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("অন্য অ্যাপে শেয়ার করুন (Share Link)", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileLinkDialog = false
                        viewModel.openUserProfileByLink(profileIdLink)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("আইডিতে ঢুকুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileLinkDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Dialog: Open Any Profile by Pasting or Typing Profile Link
    if (showOpenByLinkDialog) {
        var inputLink by remember { mutableStateOf("") }
        var linkError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showOpenByLinkDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লিংক দিয়ে আইডিতে ঢুকুন", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "কারো আইডির লিংক (যেমন: https://fotofilm.app/u/username) অথবা ইউজারনেম লিখুন বা পেস্ট করুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputLink,
                        onValueChange = {
                            inputLink = it
                            linkError = null
                        },
                        placeholder = { Text("https://fotofilm.app/u/...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (linkError != null) {
                        Text(linkError!!, color = BangladeshRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.openUserProfileByLink(inputLink)) {
                            showOpenByLinkDialog = false
                        } else {
                            linkError = "দুঃখিত, এই লিংক বা ইউজারনেমের কোনো আইডি পাওয়া যায়নি।"
                        }
                    },
                    enabled = inputLink.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("আইডিতে ঢুকুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenByLinkDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Pinned Messages
    if (showPinnedMessagesDialog) {
        AlertDialog(
            onDismissRequest = { showPinnedMessagesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pinned Messages", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "এখনো কোনো পিন করা মেসেজ নেই।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showPinnedMessagesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Mute Notifications
    if (showMuteDialog) {
        val muteOptions = listOf("For 15 minutes", "For 1 hour", "For 8 hours", "For 24 hours", "Until I turn it back on")
        var selectedOption by remember { mutableStateOf(muteOptions[2]) }

        AlertDialog(
            onDismissRequest = { showMuteDialog = false },
            title = {
                Text("Mute notifications for this chat", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    muteOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = opt }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedOption == opt),
                                onClick = { selectedOption = opt }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleMuteConversation(conversation.id)
                        showMuteDialog = false
                        toastMessage = "Notifications muted $selectedOption 🔕"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMuteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 1. Block Confirmation Bottom Sheet (Matching Screenshot 1)
    if (showBlockDialog) {
        ModalBottomSheet(
            onDismissRequest = { showBlockDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini circular avatar with green border
                FotoFilmAvatar(
                    imageUrl = currentConv.friend.avatarUrl,
                    name = currentConv.friend.fullName,
                    contentDescription = null,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF2E7D32), CircleShape)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Block ${currentConv.friend.fullName}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "If you're FotoFilm friends, blocking ${currentConv.friend.fullName} will unfriend them. They won't be able to message, call or find your FotoFilm profile. We won't tell them you blocked or reported them.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Learn how block works with multiple profiles",
                    color = FacebookBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        toastMessage = "Blocking prevents all messages, calls & profile access"
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        viewModel.blockConversation(currentConv.id)
                        showBlockDialog = false
                        showBlockedSuccessDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Block ${currentConv.friend.fullName}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // 2. Blocked Success Bottom Sheet (Matching Screenshot 2)
    if (showBlockedSuccessDialog) {
        ModalBottomSheet(
            onDismissRequest = { showBlockedSuccessDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Green Checkmark
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, Color(0xFF2E7D32), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "You blocked ${currentConv.friend.fullName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You can also delete this chat.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Other steps you can take",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Delete Step
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBlockedSuccessDialog = false
                            showDeleteConfirmDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Delete",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // 3. Unblock Confirmation Bottom Sheet (Matching Screenshot 4)
    if (showUnblockDialog) {
        ModalBottomSheet(
            onDismissRequest = { showUnblockDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini circular avatar with green border
                FotoFilmAvatar(
                    imageUrl = currentConv.friend.avatarUrl,
                    name = currentConv.friend.fullName,
                    contentDescription = null,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF2E7D32), CircleShape)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Unblock ${currentConv.friend.fullName}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unblocking ${currentConv.friend.fullName} won't add them as a FotoFilm friend. You won't be able to block them again for 48 hours.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Learn more",
                    color = FacebookBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        toastMessage = "Unblocked contacts can call or message you again"
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        viewModel.unblockConversation(currentConv.id)
                        showUnblockDialog = false
                        toastMessage = "${currentConv.friend.fullName} has been unblocked"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Unblock",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Dialog: Restrict
    if (showRestrictDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictDialog = false },
            title = {
                Text("Restrict ${conversation.friend.fullName}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Move this chat out of sight and hide your activity. They won't see when you're online or when you've read their messages.", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestrictDialog = false
                        toastMessage = "Account restricted"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Restrict")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestrictDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Delete Confirm
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("Delete this entire conversation?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Once you delete your copy of this conversation, it cannot be undone.", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteConversation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Create Group Chat
    if (showCreateGroupDialog) {
        var newGroupName by remember { mutableStateOf("${conversation.friend.fullName}'s Circle") }
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = {
                Text("Create Group Chat", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Enter a name for the group with ${conversation.friend.fullName}:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createGroupChat(newGroupName, listOf(conversation.friend))
                        showCreateGroupDialog = false
                        toastMessage = "Group created! 🎉"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Share Contact
    if (showShareContactDialog) {
        AlertDialog(
            onDismissRequest = { showShareContactDialog = false },
            title = {
                Text("Share Contact", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Share ${conversation.friend.fullName}'s profile ID link:", fontSize = 13.sp)
                    Text(profileIdLink, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        MediaSaveHelper.copyToClipboard(context, "Profile Link", profileIdLink)
                        AppSoundManager.playReactionChime()
                        showShareContactDialog = false
                        toastMessage = "Contact link copied: $profileIdLink 📋"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 4 Action Circle Button component (Matching Screenshot 2)
@Composable
fun ChatActionCircleButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Media Preview Mosaic Component (Matching Screenshot 2)
@Composable
fun MediaPreviewMosaic(
    mediaItems: List<Pair<String, Boolean>> = emptyList(),
    onClick: () -> Unit
) {
    if (mediaItems.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        mediaItems.take(3).forEach { (url, isVideo) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Media preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ChatInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ChatInfoSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
