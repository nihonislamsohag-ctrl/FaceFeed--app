package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.FlaggedAuditStatus
import com.example.model.FlaggedMessageType
import com.example.model.FlaggedMessengerAudit
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue

/**
 * Option 25 in Right Manager Fida:
 * ১ কোটি ইউজারের মেসেনজার খারাপ ছবি, ভিডিও, ভয়েস ও আপত্তিকর চ্যাট নজরদারি ও ফিল্টার 🚨
 */
@Composable
fun AdminFlaggedMessengerMonitorView(
    audits: List<FlaggedMessengerAudit>,
    onDeleteMessage: (String) -> Unit,
    onBanSender: (String) -> Unit,
    onSendWarning: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<FlaggedMessageType?>(null) }
    var unblurredMap by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var isVoicePlayingMap by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val filteredList = remember(audits, searchQuery, selectedCategory) {
        audits.filter { item ->
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                item.senderUser.fullName.contains(searchQuery, ignoreCase = true) ||
                item.recipientUser.fullName.contains(searchQuery, ignoreCase = true) ||
                item.contentPreview.contains(searchQuery, ignoreCase = true) ||
                item.aiSafetyDetection.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val badPhotoCount = remember(audits) { audits.count { it.category == FlaggedMessageType.BAD_PHOTO } }
    val badVideoCount = remember(audits) { audits.count { it.category == FlaggedMessageType.BAD_VIDEO } }
    val abusiveVoiceCount = remember(audits) { audits.count { it.category == FlaggedMessageType.ABUSIVE_VOICE } }
    val harmfulTextCount = remember(audits) { audits.count { it.category == FlaggedMessageType.HARMFUL_TEXT } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top 1 Crore User Monitoring Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E1E2C),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD50000),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "১ কোটি+ ইউজার মেসেনজার অডিট 🚨",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "খারাপ ছবি, ভিডিও, বাজে ভয়েস ও ক্ষতিকর চ্যাট মনিটর",
                                fontSize = 11.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD50000).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD50000))
                    ) {
                        Text(
                            text = "LIVE AI ACTIVE",
                            color = Color(0xFFFF5252),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill("🔞 ছবি", "$badPhotoCount", Color(0xFFE91E63))
                    StatPill("🎬 ভিডিও", "$badVideoCount", Color(0xFF9C27B0))
                    StatPill("🎤 ভয়েস", "$abusiveVoiceCount", Color(0xFFFF9800))
                    StatPill("💬 চ্যাট", "$harmfulTextCount", Color(0xFFF44336))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("প্রেরক, প্রাপক বা আপত্তিকর শব্দ দিয়ে খুঁজুন...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("সব (${audits.size})", fontSize = 11.5.sp) }
            )
            FilterChip(
                selected = selectedCategory == FlaggedMessageType.BAD_PHOTO,
                onClick = { selectedCategory = FlaggedMessageType.BAD_PHOTO },
                label = { Text("🔞 খারাপ ছবি ($badPhotoCount)", fontSize = 11.5.sp) }
            )
            FilterChip(
                selected = selectedCategory == FlaggedMessageType.BAD_VIDEO,
                onClick = { selectedCategory = FlaggedMessageType.BAD_VIDEO },
                label = { Text("🎬 খারাপ ভিডিও ($badVideoCount)", fontSize = 11.5.sp) }
            )
            FilterChip(
                selected = selectedCategory == FlaggedMessageType.ABUSIVE_VOICE,
                onClick = { selectedCategory = FlaggedMessageType.ABUSIVE_VOICE },
                label = { Text("🎤 বাজে ভয়েস ($abusiveVoiceCount)", fontSize = 11.5.sp) }
            )
            FilterChip(
                selected = selectedCategory == FlaggedMessageType.HARMFUL_TEXT,
                onClick = { selectedCategory = FlaggedMessageType.HARMFUL_TEXT },
                label = { Text("💬 ক্ষতিকর চ্যাট ($harmfulTextCount)", fontSize = 11.5.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Intercepted Items List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = BangladeshGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোনো আপত্তিকর মেসেজ শনাক্ত হয়নি!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    val isRevealed = unblurredMap[item.id] == true
                    val isVoicePlaying = isVoicePlayingMap[item.id] == true

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = when (item.status) {
                                FlaggedAuditStatus.DELETED -> Color.Gray.copy(alpha = 0.4f)
                                FlaggedAuditStatus.SENDER_BANNED -> Color.Red.copy(alpha = 0.5f)
                                else -> Color(0xFFD50000).copy(alpha = 0.35f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header: Category Badge + Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD50000).copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = item.category.iconEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.category.banglaLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD50000)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (item.status) {
                                        FlaggedAuditStatus.DELETED -> Color.Gray.copy(alpha = 0.15f)
                                        FlaggedAuditStatus.SENDER_BANNED -> Color.Red.copy(alpha = 0.15f)
                                        FlaggedAuditStatus.WARNING_SENT -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                        else -> Color(0xFFD50000).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = item.status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.status) {
                                            FlaggedAuditStatus.DELETED -> Color.Gray
                                            FlaggedAuditStatus.SENDER_BANNED -> Color.Red
                                            FlaggedAuditStatus.WARNING_SENT -> Color(0xFFE65100)
                                            else -> Color(0xFFD50000)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sender -> Recipient Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sender Avatar & Info
                                AsyncImage(
                                    model = item.senderUser.avatarUrl,
                                    contentDescription = item.senderUser.fullName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "প্রেরক: ${item.senderUser.fullName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.senderUser.country} • ${item.timestamp}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Recipient Avatar & Name
                                AsyncImage(
                                    model = item.recipientUser.avatarUrl,
                                    contentDescription = item.recipientUser.fullName,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "প্রাপক:",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.recipientUser.fullName.split(" ").firstOrNull() ?: item.recipientUser.fullName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // AI Detection Alert
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.aiSafetyDetection,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB71C1C),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Content Details / Media Box
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = item.contentPreview,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // If Photo/Video attached:
                                    if (item.mediaUrl != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = item.mediaUrl,
                                                contentDescription = "Intercepted Media",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .then(if (!isRevealed) Modifier.blur(16.dp) else Modifier),
                                                contentScale = ContentScale.Crop
                                            )

                                            // Toggle Reveal / Blur Overlay Button
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color.Black.copy(alpha = 0.75f),
                                                modifier = Modifier.clickable {
                                                    unblurredMap = unblurredMap + (item.id to !isRevealed)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isRevealed) "লুকান (Blur)" else "ছবি প্রকাশ করুন (Reveal)",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // If Audio Voice attached:
                                    if (item.isAudioVoice) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        isVoicePlayingMap = isVoicePlayingMap + (item.id to !isVoicePlaying)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isVoicePlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                        contentDescription = "Play Voice",
                                                        tint = Color(0xFFD50000),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isVoicePlaying) "ভয়েস বাজছে... (০:০৮ / ০:${item.voiceDurationSeconds})" else "বাজে ভয়েস রেকর্ড শুনুন (${item.voiceDurationSeconds}s)",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD50000)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Owner Admin Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Delete Message
                                OutlinedButton(
                                    onClick = { onDeleteMessage(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("ডিলেট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // 2. Ban Sender
                                Button(
                                    onClick = { onBanSender(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("ব্যান করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // 3. Send Warning
                                OutlinedButton(
                                    onClick = { onSendWarning(item.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("ওয়ার্নিং", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(title: String, count: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = count, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}
