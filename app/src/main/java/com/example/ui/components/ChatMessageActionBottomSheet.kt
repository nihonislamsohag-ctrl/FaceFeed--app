package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.util.MediaSaveHelper
import kotlinx.coroutines.launch

/**
 * Messenger Message Action Bottom Sheet (Matching Screenshot 1 exactly)
 * - Top Reaction Emojis: ❤️, 😆, 😮, 😢, 😡, 👍, (+)
 * - Vertical Action Items:
 *   1. Copy
 *   2. Select text
 *   3. Forward
 *   4. Reply
 *   5. Delete for me (Red Delete Icon)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessageActionBottomSheet(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onDeleteForMe: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExtendedEmojis by remember { mutableStateOf(false) }
    var showSelectTextDialog by remember { mutableStateOf(false) }

    // Device back button press to dismiss sheet
    BackHandler {
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top drag pill
                Surface(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 4.dp)
                        .width(44.dp)
                        .height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                ) {}

                // Top Back and Exit Bar (বের হওয়ার জন্য ব্যাক বাটন)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ব্যাক",
                            tint = FacebookBlue,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "বের হন / ব্যাক",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FacebookBlue
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp)
        ) {
            // =========================================================
            // 1. Top Emojis Bar (Matching Screenshot 1)
            // ❤️, 😆, 😮, 😢, 😡, 👍, (+)
            // =========================================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val mainEmojis = listOf("❤️", "😆", "😮", "😢", "😡", "👍")
                    mainEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable {
                                    AppSoundManager.playReactionChime()
                                    onReact(emoji)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }

                    // Plus (+) button for more emojis
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showExtendedEmojis = !showExtendedEmojis },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (showExtendedEmojis) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "More Emojis",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Extended Emojis Tray
            AnimatedVisibility(visible = showExtendedEmojis) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val extraEmojis = listOf("🥰", "🔥", "👏", "🎉", "💯", "🙏", "🌹", "🥳", "✨", "💔", "🤔", "😴")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        extraEmojis.take(6).forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        AppSoundManager.playReactionChime()
                                        onReact(emoji)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        extraEmojis.drop(6).forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        AppSoundManager.playReactionChime()
                                        onReact(emoji)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // =========================================================
            // 2. Action Items (Matching Screenshot 1 + Save/Download Media)
            // =========================================================

            // 0. Save / Download Photo or Video to Phone Gallery (when media present)
            if (!message.mediaUrl.isNullOrBlank()) {
                val isVid = message.isMediaVideo
                MessageActionRowItem(
                    icon = Icons.Default.Download,
                    title = if (isVid) "ভিডিও সেইভ ও ডাউনলোড করুন ⬇️" else "ছবি সেইভ ও ডাউনলোড করুন ⬇️",
                    iconTint = BangladeshGreen,
                    iconContainerColor = BangladeshGreen.copy(alpha = 0.14f),
                    titleColor = BangladeshGreen,
                    onClick = {
                        val url = message.mediaUrl
                        Toast.makeText(context, if (isVid) "⏳ ভিডিও ডাউনলোড হচ্ছে..." else "⏳ ছবি ডাউনলোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            val result = MediaSaveHelper.saveMediaToGallery(context, url, isVid)
                            if (result.isSuccess) {
                                AppSoundManager.playReactionChime()
                                Toast.makeText(
                                    context,
                                    if (isVid) "✅ ভিডিও গ্যালারি/ডাউনলোডে সেইভ হয়েছে!" else "✅ ছবি গ্যালারি/ডাউনলোডে সেইভ হয়েছে!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "⚠️ ডাউনলোড করা যায়নি: ${result.exceptionOrNull()?.message ?: "ইন্টারনেট/ফাইল চেক করুন"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onDismiss()
                        }
                    }
                )
            }

            // 1. Copy
            MessageActionRowItem(
                icon = Icons.Default.ContentCopy,
                title = "Copy",
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ChatMessage", message.text.ifBlank { message.mediaUrl ?: "" })
                    clipboard.setPrimaryClip(clip)
                    AppSoundManager.playReactionChime()
                    Toast.makeText(context, "মেসেজ কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 2. Select text
            MessageActionRowItem(
                icon = Icons.Default.TextFields,
                title = "Select text",
                onClick = {
                    showSelectTextDialog = true
                }
            )

            // 3. Forward
            MessageActionRowItem(
                icon = Icons.AutoMirrored.Filled.Forward,
                title = "Forward",
                onClick = {
                    onForward()
                    onDismiss()
                }
            )

            // 4. Reply
            MessageActionRowItem(
                icon = Icons.AutoMirrored.Filled.Reply,
                title = "Reply",
                onClick = {
                    AppSoundManager.playReactionChime()
                    onReply()
                    onDismiss()
                }
            )

            // 5. Delete for me (Matching red trash icon and title)
            MessageActionRowItem(
                icon = Icons.Default.Delete,
                title = "Delete for me",
                iconTint = Color(0xFFE53935),
                iconContainerColor = Color(0xFFFFEBEE),
                titleColor = Color(0xFFD32F2F),
                onClick = {
                    AppSoundManager.playReactionChime()
                    onDeleteForMe()
                    Toast.makeText(context, "🗑️ মেসেজটি আপনার জন্য মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(6.dp))

            // 7. ব্যাক বাটন (Exit / Back button per user request)
            MessageActionRowItem(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                title = "ফিরে যান / ব্যাক (Back)",
                iconTint = FacebookBlue,
                iconContainerColor = FacebookBlue.copy(alpha = 0.12f),
                titleColor = FacebookBlue,
                onClick = {
                    onDismiss()
                }
            )
        }
    }

    // Select Text Dialog
    if (showSelectTextDialog) {
        AlertDialog(
            onDismissRequest = { showSelectTextDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = FacebookBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("লেখা সিলেক্ট করুন (Select Text)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                SelectionContainer {
                    Text(
                        text = message.text.ifBlank { "📷 মিডিয়া ফাইল লিঙ্ক: ${message.mediaUrl ?: ""}" },
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SelectedMessage", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "টেক্সট কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        showSelectTextDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("সব কপি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSelectTextDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
fun MessageActionRowItem(
    icon: ImageVector,
    title: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Icon Container matching Screenshot 1
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = iconContainerColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Medium,
            color = titleColor
        )
    }
}

/**
 * Forward Message Dialog allowing user to forward message to any conversation/friend
 */
@Composable
fun ForwardMessageDialog(
    message: ChatMessage,
    conversations: List<Conversation>,
    onForwardTo: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null, tint = FacebookBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("মেসেজ ফরওয়ার্ড করুন", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "ফরওয়ার্ড করা হচ্ছে: \"${message.text.take(40)}${if (message.text.length > 40) "..." else ""}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "কাকে পাঠাতে চান নির্বাচন করুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(conversations) { conv ->
                        val isChecked = selectedIds.contains(conv.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (isChecked) selectedIds - conv.id else selectedIds + conv.id
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                FotoFilmAvatar(
                                    imageUrl = if (conv.isGroup) "" else conv.friend.avatarUrl,
                                    name = conv.groupName ?: conv.friend.fullName,
                                    isGroup = conv.isGroup,
                                    modifier = Modifier.size(36.dp),
                                    iconSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = conv.groupName ?: conv.friend.fullName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = conv.lastMessage,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + conv.id else selectedIds - conv.id
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedIds.isNotEmpty()) {
                        onForwardTo(selectedIds.toList())
                        AppSoundManager.playReactionChime()
                        Toast.makeText(context, "${selectedIds.size}টি ইনবক্সে সফলভাবে ফরওয়ার্ড করা হয়েছে! 🚀", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "অনুগ্রহ করে অন্তত একজন বন্ধু নির্বাচন করুন", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("পাঠান (${selectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
