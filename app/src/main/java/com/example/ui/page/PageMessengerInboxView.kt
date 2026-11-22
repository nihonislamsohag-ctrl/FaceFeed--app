package com.example.ui.page

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SocialPage
import com.example.ui.theme.BangladeshGreen
import com.example.util.VoiceAudioManager

data class PageChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val isFromPage: Boolean,
    val text: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val voiceDuration: String? = null,
    val audioUrl: String? = null,
    val timestamp: String = "Just now"
)

data class PageChatConversation(
    val id: String,
    val visitorName: String,
    val visitorAvatar: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val messages: MutableList<PageChatMessage> = mutableListOf()
)

/**
 * Requirement 6: Separate Messenger Inbox & Chat for Page
 * - Separate inbox for Page visitors
 * - Inside chat: Text message, Photo send, Video send, Voice message, Audio Call button, Video Call button, Emoji/Sticker/File send
 * - When visitor messages Page, Admin gets notification
 * - Page messages show Page name and Page photo
 */
@Composable
fun PageMessengerInboxView(
    page: SocialPage,
    onAdminNotification: (String) -> Unit
) {
    val conversations = remember(page.id) {
        mutableStateListOf<PageChatConversation>()
    }

    var activeConversation by remember { mutableStateOf<PageChatConversation?>(null) }
    var showActiveCallDialog by remember { mutableStateOf<String?>(null) } // "audio" or "video"

    if (activeConversation != null) {
        val conv = activeConversation!!
        val context = LocalContext.current
        var inputText by remember { mutableStateOf("") }
        val isRecordingVoice by VoiceAudioManager.isRecording.collectAsState()
        val recordingSeconds by VoiceAudioManager.recordingSeconds.collectAsState()
        val activeVoiceId by VoiceAudioManager.playingItemId.collectAsState()
        val isVoicePlaying by VoiceAudioManager.isPlaying.collectAsState()
        var pendingPageVoice by remember { mutableStateOf(false) }

        val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && pendingPageVoice) {
                pendingPageVoice = false
                VoiceAudioManager.startRecording(context, "page_${conv.id}")
            } else {
                pendingPageVoice = false
                if (!granted) {
                    Toast.makeText(context, "ভয়েস রেকর্ড করতে মাইক্রোফোন পারমিশন দিন", Toast.LENGTH_SHORT).show()
                }
            }
        }

        DisposableEffect(conv.id) {
            onDispose {
                VoiceAudioManager.stopPlayback()
                if (VoiceAudioManager.isRecording.value) {
                    VoiceAudioManager.cancelRecording()
                }
            }
        }

        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                conv.messages.add(
                    PageChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        senderName = page.name,
                        senderAvatar = page.avatarUrl,
                        isFromPage = true,
                        text = "ছবি পাঠানো হয়েছে 📷",
                        imageUrl = uri.toString()
                    )
                )
            }
        }

        val videoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                conv.messages.add(
                    PageChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        senderName = page.name,
                        senderAvatar = page.avatarUrl,
                        isFromPage = true,
                        text = "ভিডিও ক্লিপ পাঠানো হয়েছে 🎥",
                        videoUrl = uri.toString()
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Chat Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { activeConversation = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        com.example.ui.components.FotoFilmAvatar(
                            imageUrl = conv.visitorAvatar,
                            name = conv.visitorName,
                            contentDescription = conv.visitorName,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(conv.visitorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Active now • Visitor", fontSize = 11.sp, color = Color(0xFF43A047))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Audio Call Button
                        IconButton(onClick = { showActiveCallDialog = "audio" }) {
                            Icon(Icons.Default.Phone, contentDescription = "Audio Call", tint = BangladeshGreen)
                        }
                        // Video Call Button
                        IconButton(onClick = { showActiveCallDialog = "video" }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = BangladeshGreen)
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conv.messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isFromPage) Arrangement.End else Arrangement.Start
                    ) {
                        if (!msg.isFromPage) {
                            com.example.ui.components.FotoFilmAvatar(
                                imageUrl = msg.senderAvatar,
                                name = msg.senderName,
                                contentDescription = msg.senderName,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Bottom)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (msg.isFromPage) BangladeshGreen else Color(0xFFF0F2F5),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (msg.isFromPage) {
                                    Text(
                                        text = page.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                if (msg.imageUrl != null) {
                                    AsyncImage(
                                        model = msg.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                if (msg.voiceDuration != null) {
                                    val isThisPlaying = isVoicePlaying && activeVoiceId == msg.id
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val url = msg.audioUrl?.trim().orEmpty()
                                            if (url.isNotBlank()) {
                                                VoiceAudioManager.togglePlay(context, msg.id, url)
                                            } else {
                                                Toast.makeText(context, "নতুন ভয়েস রেকর্ড করে পাঠান", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Voice message",
                                            tint = if (msg.isFromPage) Color.White else Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "🎙️ ভয়েস বার্তা (${msg.voiceDuration})",
                                            color = if (msg.isFromPage) Color.White else Color.Black,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isFromPage) Color.White else Color.Black,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }

                        if (msg.isFromPage) {
                            Spacer(modifier = Modifier.width(6.dp))
                            com.example.ui.components.FotoFilmAvatar(
                                imageUrl = page.avatarUrl,
                                name = page.name,
                                contentDescription = page.name,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Bottom)
                            )
                        }
                    }
                }
            }

            // Quick Visitor Simulation Button (Test receiving message as visitor)
            Surface(
                color = Color(0xFFFFF9C4),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val replyMsg = PageChatMessage(
                            id = "m_${System.currentTimeMillis()}",
                            senderName = conv.visitorName,
                            senderAvatar = conv.visitorAvatar,
                            isFromPage = false,
                            text = "ধন্যবাদ! আপনাদের পেইজের সার্ভিসটি খুবই দারুণ লেগেছে।"
                        )
                        conv.messages.add(replyMsg)
                        onAdminNotification("নতুন বার্তা: ${conv.visitorName} পেইজে মেসেজ পাঠিয়েছেন! ✉️")
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "⚡ ভিজিটর টেস্ট: '${conv.visitorName}'-এর পক্ষ থেকে বার্তা পাঠান",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.Photo, contentDescription = "Photo", tint = Color(0xFF2E7D32))
                        }
                        IconButton(onClick = { videoPicker.launch("video/*") }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video", tint = Color(0xFFD32F2F))
                        }
                        IconButton(onClick = {
                            if (isRecordingVoice) {
                                val res = VoiceAudioManager.stopRecording()
                                if (res != null) {
                                    val durText = String.format("%d:%02d", res.durationSeconds / 60, res.durationSeconds % 60)
                                    conv.messages.add(
                                        PageChatMessage(
                                            id = "m_${System.currentTimeMillis()}",
                                            senderName = page.name,
                                            senderAvatar = page.avatarUrl,
                                            isFromPage = true,
                                            text = "🎙️ ভয়েস মেসেজ ($durText)",
                                            voiceDuration = durText,
                                            audioUrl = res.fileUri
                                        )
                                    )
                                } else {
                                    Toast.makeText(context, "কমপক্ষে ১ সেকেন্ড কথা বলে ভয়েস দিন", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    VoiceAudioManager.startRecording(context, "page_${conv.id}")
                                } else {
                                    pendingPageVoice = true
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = if (isRecordingVoice) Color.Red else Color(0xFF7B1FA2)
                                )
                                if (isRecordingVoice) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${recordingSeconds}s", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Reply as ${page.name}...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 90.dp),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = false
                        )

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    conv.messages.add(
                                        PageChatMessage(
                                            id = "m_${System.currentTimeMillis()}",
                                            senderName = page.name,
                                            senderAvatar = page.avatarUrl,
                                            isFromPage = true,
                                            text = inputText
                                        )
                                    )
                                    inputText = ""
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = BangladeshGreen
                            )
                        }
                    }
                }
            }
        }

        // Active Audio/Video Call Dialog
        if (showActiveCallDialog != null) {
            AlertDialog(
                onDismissRequest = { showActiveCallDialog = null },
                icon = {
                    Icon(
                        imageVector = if (showActiveCallDialog == "video") Icons.Default.Videocam else Icons.Default.Phone,
                        contentDescription = null,
                        tint = BangladeshGreen,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = {
                    Text(
                        text = if (showActiveCallDialog == "video") "ভিডিও কলিং চলছে..." else "অডিও কলিং চলছে...",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${conv.visitorName}-এর সাথে কল সংযুক্ত হচ্ছে।", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("রিং হচ্ছে...", color = BangladeshGreen, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showActiveCallDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("কল কাটুন")
                    }
                }
            )
        }

        return
    }

    // Inbox List View
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        // Inbox Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.FotoFilmAvatar(
                        imageUrl = page.avatarUrl,
                        name = page.name,
                        contentDescription = page.name,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${page.name} মেসেঞ্জার ইনবক্স",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text("ভিজিটর ও কাস্টমারদের বার্তা", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = BangladeshGreen
                ) {
                    Text(
                        text = "${conversations.count { it.unreadCount > 0 }}টি নতুন",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Mail,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "এখনো কোনো ভিজিটর মেসেজ আসেনি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ভিজিটররা পেইজে মেসেজ দিলে এখানে দেখতে পাবেন।",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(conversations) { conv ->
                    Surface(
                        onClick = { activeConversation = conv },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.ui.components.FotoFilmAvatar(
                                imageUrl = conv.visitorAvatar,
                                name = conv.visitorName,
                                contentDescription = conv.visitorName,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = conv.visitorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                                Text(
                                    text = conv.time,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = conv.lastMessage,
                                fontSize = 12.5.sp,
                                color = if (conv.unreadCount > 0) Color.Black else Color.Gray,
                                fontWeight = if (conv.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }

                        if (conv.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = BangladeshGreen,
                                modifier = Modifier.size(10.dp)
                            ) {}
                        }
                    }
                }
            }
            }
        }
    }
}
