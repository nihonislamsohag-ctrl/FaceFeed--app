package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.DeliveredVaultMessage
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

enum class MessengerSettingsSubScreen {
    MESSAGE_DELIVERY,
    RESTRICTED_PROFILES,
    BLOCKED_PROFILES
}

@Composable
fun MessengerSettingsDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    var currentSubScreen by remember { mutableStateOf<MessengerSettingsSubScreen?>(null) }

    var activeStatus by remember { mutableStateOf(true) }
    var chatHeads by remember { mutableStateOf(true) }
    var messageSound by remember { mutableStateOf(true) }

    val restrictedList by viewModel.restrictedProfiles.collectAsState()
    val blockedList by viewModel.blockedProfiles.collectAsState()
    val vaultMessages by viewModel.permanentDeliveryVault.collectAsState()

    val context = LocalContext.current

    if (currentSubScreen == null) {
        // Main Messenger Settings Dialog (Matches Screenshots 1 & 2)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = BangladeshGreen, fontWeight = FontWeight.Black)) {
                                    append("Chat")
                                }
                                withStyle(SpanStyle(color = BangladeshRed, fontWeight = FontWeight.Black)) {
                                    append("Film")
                                }
                                append(" Settings")
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Back / Close Button on Right Side (ডান পাসে ব্যাক বাটন - দ্বিতীয় স্ক্রিনশট)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back / Close (বন্ধ করুন / ব্যাকে যান)",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Active Status
                    SettingToggleRow(
                        title = "Active Status",
                        subtitle = "Show when you're active or recently active",
                        checked = activeStatus,
                        onCheckedChange = { activeStatus = it }
                    )

                    // Option 2: Floating Chat Heads
                    SettingToggleRow(
                        title = "Floating Chat Heads",
                        subtitle = "Show chat bubbles over other apps",
                        checked = chatHeads,
                        onCheckedChange = { chatHeads = it }
                    )

                    // Option 3: Notification Sounds
                    SettingToggleRow(
                        title = "Notification Sounds",
                        subtitle = "Play sound for incoming messages and calls",
                        checked = messageSound,
                        onCheckedChange = { messageSound = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Section: Who can reach you (From 2nd Screenshot)
                    Text(
                        text = "Who can reach you",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Feature 1: Message delivery (With Permanent Vault)
                    WhoCanReachYouRow(
                        icon = Icons.Default.MarkEmailRead,
                        iconTint = BangladeshGreen,
                        title = "Message delivery",
                        subtitle = "Choose who can message you",
                        badgeText = "স্থায়ী ভল্ট (${vaultMessages.size})",
                        badgeColor = BangladeshGreen,
                        onClick = {
                            AppSoundManager.playReactionChime()
                            currentSubScreen = MessengerSettingsSubScreen.MESSAGE_DELIVERY
                        }
                    )

                    // Feature 2: Restricted profiles
                    WhoCanReachYouRow(
                        icon = Icons.Default.PersonOff,
                        iconTint = Color(0xFFE65100),
                        title = "Restricted profiles",
                        subtitle = "Limit interaction with someone",
                        badgeText = "${restrictedList.size} জন",
                        badgeColor = Color(0xFFE65100),
                        onClick = {
                            AppSoundManager.playReactionChime()
                            currentSubScreen = MessengerSettingsSubScreen.RESTRICTED_PROFILES
                        }
                    )

                    // Feature 3: Blocked profiles
                    WhoCanReachYouRow(
                        icon = Icons.Default.Block,
                        iconTint = BangladeshRed,
                        title = "Blocked profiles",
                        subtitle = "Stop someone from contacting you",
                        badgeText = "${blockedList.size} জন",
                        badgeColor = BangladeshRed,
                        onClick = {
                            AppSoundManager.playReactionChime()
                            currentSubScreen = MessengerSettingsSubScreen.BLOCKED_PROFILES
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        )
    } else {
        // Dedicated Sub-Screens with Back Navigation
        Dialog(
            onDismissRequest = { currentSubScreen = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentSubScreen) {
                    MessengerSettingsSubScreen.MESSAGE_DELIVERY -> {
                        MessageDeliveryVaultScreen(
                            vaultMessages = vaultMessages,
                            onBack = { currentSubScreen = null }
                        )
                    }
                    MessengerSettingsSubScreen.RESTRICTED_PROFILES -> {
                        RestrictedProfilesScreen(
                            restrictedUsers = restrictedList,
                            onUnrestrict = { user ->
                                viewModel.unrestrictUser(user.id)
                                AppSoundManager.playReactionChime()
                                Toast.makeText(
                                    context,
                                    "${user.fullName} আন-রেস্ট্রিক্ট করা হয়েছে",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onBack = { currentSubScreen = null }
                        )
                    }
                    MessengerSettingsSubScreen.BLOCKED_PROFILES -> {
                        BlockedProfilesScreen(
                            blockedUsers = blockedList,
                            onUnblock = { user ->
                                viewModel.unblockUser(user.id)
                                AppSoundManager.playReactionChime()
                                Toast.makeText(
                                    context,
                                    "${user.fullName} আনব্লক করা হয়েছে",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onBack = { currentSubScreen = null }
                        )
                    }
                    null -> {}
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BangladeshGreen
            )
        )
    }
}

@Composable
private fun WhoCanReachYouRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                color = badgeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ========================================================
// 1. Message Delivery & Permanent Message Vault Screen
// ========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDeliveryVaultScreen(
    vaultMessages: List<DeliveredVaultMessage>,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("ALL") }

    val filteredList = remember(vaultMessages, searchQuery, selectedMediaType) {
        vaultMessages.filter { msg ->
            val matchQuery = searchQuery.isBlank() ||
                    msg.contactName.contains(searchQuery, ignoreCase = true) ||
                    msg.text.contains(searchQuery, ignoreCase = true) ||
                    msg.senderName.contains(searchQuery, ignoreCase = true)

            val matchMedia = when (selectedMediaType) {
                "PHOTO" -> msg.mediaUrl != null && !msg.isMediaVideo
                "VIDEO" -> msg.isMediaVideo
                "VOICE" -> msg.isAudioVoice
                "TEXT" -> msg.mediaUrl == null && !msg.isAudioVoice
                else -> true
            }

            matchQuery && matchMedia
        }
    }

    val photoCount = vaultMessages.count { it.mediaUrl != null && !it.isMediaVideo }
    val videoCount = vaultMessages.count { it.isMediaVideo }
    val voiceCount = vaultMessages.count { it.isAudioVoice }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Message delivery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Choose who can message you & Permanent Vault",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BangladeshGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Protected Vault Banner Card (Explaining Permanent Storage)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BangladeshGreen.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(BangladeshGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "স্থায়ী মেসেজ ডেলিভারি ভল্ট (Permanent Vault)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BangladeshGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "সবাই কে কী মেসেজ দিয়েছে এখানে তার সম্পূর্ণ রেকর্ড রয়েছে। মেসেনজার থেকে কোনো চ্যাট বা মেসেজ ডিলেট করলেও এখান থেকে ঐ মেসেজ, ছবি, ভিডিও বা ভয়েস কখনো ডিলেট হবে না!",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Statistics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট রেকর্ড: ${vaultMessages.size} টি",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "📷 $photoCount | 📹 $videoCount | 🎤 $voiceCount",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ফ্রেন্ড বা মেসেজ খুঁজুন...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "ALL" to "সব (${vaultMessages.size})",
                    "PHOTO" to "📷 ছবি ($photoCount)",
                    "VIDEO" to "📹 ভিডিও ($videoCount)",
                    "VOICE" to "🎤 ভয়েস ($voiceCount)",
                    "TEXT" to "💬 টেক্সট"
                )
                items(chips) { (type, label) ->
                    FilterChip(
                        selected = selectedMediaType == type,
                        onClick = { selectedMediaType = type },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BangladeshGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Vault Messages List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো ডেলিভারি রেকর্ড পাওয়া যায়নি",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { msg ->
                        VaultMessageCard(msg = msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultMessageCard(msg: DeliveredVaultMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Avatar, Contact Name, Timestamp, Protected Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = msg.contactAvatarUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500" },
                    contentDescription = msg.contactName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = msg.contactName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "প্রেরক: ${msg.senderName} • ${msg.timestamp}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = BangladeshGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ডেলিভারড",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BangladeshGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Content (Text, Photo, Video, or Voice)
            if (msg.mediaUrl != null && !msg.isMediaVideo) {
                // Photo Attachment
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(6.dp)
                ) {
                    AsyncImage(
                        model = msg.mediaUrl,
                        contentDescription = "Delivered Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (msg.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            } else if (msg.isMediaVideo) {
                // Video Attachment
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = msg.mediaUrl ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800",
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "📹 ভিডিও ডেলিভারি রেকর্ড",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (msg.isAudioVoice) {
                // Voice Note Attachment
                var isVoicePlaying by remember { mutableStateOf(false) }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BangladeshGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isVoicePlaying = !isVoicePlaying
                                AppSoundManager.playReactionChime()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BangladeshGreen)
                        ) {
                            Icon(
                                imageVector = if (isVoicePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Voice",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎤 ভয়েস মেসেজ সংরক্ষিত (${msg.audioDurationSeconds}s)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = BangladeshGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { if (isVoicePlaying) 0.6f else 0.1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = BangladeshGreen,
                                trackColor = BangladeshGreen.copy(alpha = 0.2f),
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${msg.audioDurationSeconds}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Plain Text Message
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg.text,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Permanent Vault Security Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "পার্মানেন্ট ভল্ট: মূল চ্যাট থেকে ডিলিট করলেও এটি এখানে অক্ষুণ্ণ থাকবে",
                    fontSize = 9.5.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ========================================================
// 2. Restricted Profiles Screen
// ========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestrictedProfilesScreen(
    restrictedUsers: List<User>,
    onUnrestrict: (User) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Restricted profiles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Limit interaction with someone",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFE65100)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Explanatory Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "সীমাবদ্ধ বা রেস্ট্রিক্ট করা প্রোফাইলগুলো আপনি কখন অ্যাক্টিভ আছেন বা তাদের মেসেজ পড়েছেন কিনা তা দেখতে পাবে না। তাদের চ্যাট সরাসরি ইনবক্সে আসবে না। আন-রেস্ট্রিক্ট করতে 'Un Restricted' বাটনে চাপ দিন।",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }

            // Header Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "রেস্ট্রিক্ট করা প্রোফাইল (${restrictedUsers.size} জন)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (restrictedUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "কোনো প্রোফাইল রেস্ট্রিক্ট করা নেই",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আপনার সকল ফ্রেন্ডের সাথে স্বাভাবিক ইন্টারঅ্যাকশন সক্রিয় আছে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(restrictedUsers, key = { it.id }) { user ->
                        RestrictedUserItemCard(
                            user = user,
                            onUnrestrict = { onUnrestrict(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestrictedUserItemCard(
    user: User,
    onUnrestrict: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                model = user.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500" },
                contentDescription = user.fullName,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username.ifBlank { "user" }} • Restricted",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Un Restricted Button
            Button(
                onClick = onUnrestrict,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Un Restricted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ========================================================
// 3. Blocked Profiles Screen
// ========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedProfilesScreen(
    blockedUsers: List<User>,
    onUnblock: (User) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Blocked profiles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Stop someone from contacting you",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BangladeshRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Explanatory Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BangladeshRed.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshRed.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = BangladeshRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ব্লক করা প্রোফাইলগুলো আপনাকে কোনো মেসেজ, অডিও বা ভিডিও কল দিতে পারবে না। তারা আপনার প্রোফাইল বা পোস্ট দেখতে পাবে না। কাউকে আনব্লক করতে তার নামের পাশের 'আনব্লক (Unblock)' বাটনে চাপ দিন।",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Header Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ব্লক করা প্রোফাইল (${blockedUsers.size} জন)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (blockedUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "কোনো ব্লক করা প্রোফাইল নেই",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আপনার অ্যাকাউন্ট সম্পূর্ণ সুরক্ষিত আছে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedUsers, key = { it.id }) { user ->
                        BlockedUserItemCard(
                            user = user,
                            onUnblock = { onUnblock(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUserItemCard(
    user: User,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                model = user.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500" },
                contentDescription = user.fullName,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username.ifBlank { "user" }} • Blocked",
                    fontSize = 11.sp,
                    color = BangladeshRed
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Unblock Button
            Button(
                onClick = onUnblock,
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshRed),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "আনব্লক",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
