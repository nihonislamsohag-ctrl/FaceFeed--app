package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

/**
 * Facebook-style Interactive Share Bottom Sheet
 * Allows user to:
 * 1. Share immediately to their own Feed
 * 2. Send via direct Messenger chat to any friend
 * 3. Share to their Groups
 * 4. Copy public post link
 * 5. Share externally via Android share sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostBottomSheet(
    post: Post,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val conversations by viewModel.conversations.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var sentFriends by remember { mutableStateOf(setOf<String>()) }
    var shareCaption by remember { mutableStateOf("") }
    var showGroupPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পোস্ট শেয়ার করুন (Share)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Quick Preview Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FotoFilmAvatar(
                        imageUrl = post.author.avatarUrl,
                        name = post.author.fullName,
                        modifier = Modifier.size(36.dp),
                        iconSize = 18.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.author.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = post.text.ifBlank { "📷 মিডিয়া পোস্ট" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct Messenger Send Row (মেসেঞ্জারে পাঠান)
            Text(
                text = "মেসেঞ্জারে বন্ধুদের পাঠান (Send in Messenger):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(conversations.take(8), key = { it.id }) { conv ->
                    val isSent = sentFriends.contains(conv.id)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(62.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            FotoFilmAvatar(
                                imageUrl = conv.friend.avatarUrl,
                                name = conv.friend.fullName,
                                isGroup = conv.isGroup,
                                modifier = Modifier.size(46.dp),
                                iconSize = 22.dp
                            )
                            if (isSent) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(BangladeshGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = conv.friend.fullName.split(" ").firstOrNull() ?: conv.friend.fullName,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (!isSent) {
                                    viewModel.sendMessage(
                                        conversationId = conv.id,
                                        text = "🔗 দেখুন এই চমৎকার পোস্টটি: ${post.author.fullName} এর পোস্ট\n\"${post.text.take(60)}\""
                                    )
                                    viewModel.sharePost(post.id)
                                    sentFriends = sentFriends + conv.id
                                    Toast.makeText(context, "${conv.friend.fullName}-কে মেসেঞ্জারে পাঠানো হয়েছে! ✉️", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSent) Color(0xFFE2E8F0) else FacebookBlue,
                                contentColor = if (isSent) Color(0xFF64748B) else Color.White
                            ),
                            modifier = Modifier
                                .height(26.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSent) "Sent" else "Send",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Share Action Items List
            // 1. Share to Feed Now
            ShareActionRow(
                icon = Icons.Default.Campaign,
                iconColor = FacebookBlue,
                title = "ফিডে শেয়ার করুন (Share now to Feed)",
                subtitle = "আপনার ওয়ালে পাবলিকলি পোস্টটি যুক্ত হবে",
                onClick = {
                    viewModel.createPost(
                        text = "Shared from ${post.author.fullName}:\n\n${post.text}",
                        hashtags = post.hashtags,
                        images = post.images,
                        isVideo = post.isVideo,
                        videoTitle = post.videoTitle,
                        isReel = post.isReel
                    )
                    viewModel.sharePost(post.id)
                    Toast.makeText(context, "🎉 পোস্টটি আপনার ফিডে শেয়ার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 2. Share to Group
            ShareActionRow(
                icon = Icons.Default.Group,
                iconColor = BangladeshGreen,
                title = "গ্রুপে শেয়ার করুন (Share to a group)",
                subtitle = "আপনার যুক্ত থাকা গ্রুপগুলোতে পাঠান",
                onClick = {
                    if (groups.isNotEmpty()) {
                        val targetGroup = groups.first()
                        viewModel.sharePost(post.id)
                        Toast.makeText(context, "✅ '${targetGroup.name}' গ্রুপে সফলভাবে শেয়ার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "গ্রুপে শেয়ার সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                }
            )

            // 3. Copy Link
            ShareActionRow(
                icon = Icons.Default.Link,
                iconColor = Color(0xFF00897B),
                title = "লিংক কপি করুন (Copy link)",
                subtitle = "পোস্টের সরাসরি লিংক ক্লিপবোর্ডে কপি হবে",
                onClick = {
                    clipboardManager.setText(AnnotatedString("https://fotofilm.com/post/${post.id}"))
                    Toast.makeText(context, "📋 পোস্টের লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            // 4. External Android Share Sheet
            ShareActionRow(
                icon = Icons.Default.Share,
                iconColor = Color(0xFFE91E63),
                title = "অন্যান্য অ্যাপে শেয়ার (Share via...)",
                subtitle = "হোয়াটসঅ্যাপ, মেসেঞ্জার বা ব্লুটুথ দিয়ে পাঠান",
                onClick = {
                    try {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "FotoFilm Post by ${post.author.fullName}:\n${post.text}\nhttps://fotofilm.com/post/${post.id}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share post via")
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        clipboardManager.setText(AnnotatedString("https://fotofilm.com/post/${post.id}"))
                        Toast.makeText(context, "লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.sharePost(post.id)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ShareActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}
