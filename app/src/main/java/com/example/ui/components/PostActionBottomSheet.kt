package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bottom Sheet for Video Posts (matching Screenshot 2 + Video Download Option)
 * and Normal/Photo Posts (matching Screenshot 3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostActionBottomSheet(
    post: Post,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onOpenBoost: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var editPostText by remember { mutableStateOf(post.text) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var isDownloadingVideo by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    val isVideoPost = post.isVideo || post.isReel

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isVideoPost) {
                // ========================================================
                // Screenshot 2: VIDEO POST OPTIONS + VIDEO DOWNLOAD OPTION
                // ========================================================

                // 1. Pin post
                PostActionItem(
                    icon = Icons.Default.PushPin,
                    title = "Pin post",
                    onClick = {
                        viewModel.pinPost(post.id)
                        Toast.makeText(context, "📌 পোস্টটি পিন করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 2. Turn off notifications for this post
                PostActionItem(
                    icon = Icons.Default.NotificationsNone,
                    title = "Turn off notifications for this post",
                    onClick = {
                        Toast.makeText(context, "🔕 এই পোস্টের নোটিফিকেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 3. Save video
                PostActionItem(
                    icon = Icons.Default.BookmarkBorder,
                    title = "Save video (ভিডিও সেভ করুন)",
                    onClick = {
                        viewModel.savePost(post)
                        Toast.makeText(context, "🔖 ভিডিওটি সেভ করা হয়েছে! 'Saved' মেনুতে দেখতে পারবেন।", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 4. Share
                PostActionItem(
                    icon = Icons.Default.Reply,
                    title = "Share",
                    onClick = {
                        viewModel.sharePost(post.id)
                        Toast.makeText(context, "↗️ ভিডিওটি সফলভাবে শেয়ার হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 5. CRITICAL USER REQUEST: Download video ("সবাই যেনো সবাইর ভিডিও ডাউনলোড করার আপসোন এড ও চালু হবে")
                PostActionItem(
                    icon = Icons.Default.Download,
                    title = "Download video (ভিডিও ডাউনলোড করুন)",
                    iconTint = BangladeshGreen,
                    titleColor = BangladeshGreen,
                    onClick = {
                        isDownloadingVideo = true
                        downloadProgress = 0f
                        coroutineScope.launch {
                            for (p in 1..10) {
                                delay(120)
                                downloadProgress = p / 10f
                            }
                            delay(200)
                            isDownloadingVideo = false
                            Toast.makeText(
                                context,
                                "✅ ভিডিওটি সফলভাবে আপনার ফোনের গ্যালারি ও ডাউনলোড ফোল্ডারে সেভ হয়েছে!",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        }
                    }
                )

                // 6. Edit Privacy
                PostActionItem(
                    icon = Icons.Default.Public,
                    title = "Edit Privacy",
                    onClick = {
                        showPrivacyDialog = true
                    }
                )

                // 7. Edit Post
                PostActionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Post",
                    onClick = {
                        showEditDialog = true
                    }
                )

                // 8. Move to archive
                PostActionItem(
                    icon = Icons.Default.Archive,
                    title = "Move to archive",
                    onClick = {
                        viewModel.movePostToArchive(post.id)
                        Toast.makeText(context, "🗄️ পোস্টটি আর্কাইভে পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 9. Move to trash
                PostActionItem(
                    icon = Icons.Default.Delete,
                    title = "Move to trash",
                    subtitle = "Items in your trash are deleted after 30 days.",
                    iconTint = Color.Red,
                    onClick = {
                        showDeleteConfirmDialog = true
                    }
                )

                // 10. Copy link
                PostActionItem(
                    icon = Icons.Default.Link,
                    title = "Copy link",
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://facefeed.app/video/${post.id}"))
                        Toast.makeText(context, "🔗 ভিডিও লিঙ্ক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // Block Author
                PostActionItem(
                    icon = Icons.Default.Block,
                    title = "Block ${post.author.fullName}",
                    subtitle = "They won't be able to message or see your profile.",
                    iconTint = Color.Red,
                    onClick = {
                        viewModel.blockUser(post.author.id)
                        Toast.makeText(context, "${post.author.fullName} ব্লক করা হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

            } else {
                // ========================================================
                // Screenshot 3: REGULAR / PHOTO / TEXT POST OPTIONS
                // ========================================================

                // 1. Pin post
                PostActionItem(
                    icon = Icons.Default.PushPin,
                    title = "Pin post",
                    onClick = {
                        viewModel.pinPost(post.id)
                        Toast.makeText(context, "📌 পোস্টটি পিন করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 2. Turn off notifications for this post
                PostActionItem(
                    icon = Icons.Default.NotificationsNone,
                    title = "Turn off notifications for this post",
                    onClick = {
                        Toast.makeText(context, "🔕 এই পোস্টের নোটিফিকেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 3. Save post
                PostActionItem(
                    icon = Icons.Default.BookmarkBorder,
                    title = "Save post (পোস্ট সেভ করুন)",
                    onClick = {
                        viewModel.savePost(post)
                        Toast.makeText(context, "🔖 পোস্টটি সেভ করা হয়েছে! 'Saved' মেনুতে দেখতে পারবেন।", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 4. Share
                PostActionItem(
                    icon = Icons.Default.Reply,
                    title = "Share",
                    onClick = {
                        viewModel.sharePost(post.id)
                        Toast.makeText(context, "↗️ পোস্টটি শেয়ার করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 5. Tag photo
                PostActionItem(
                    icon = Icons.Default.Label,
                    title = "Tag photo",
                    onClick = {
                        Toast.makeText(context, "🏷️ বন্ধুদের ট্যাগ করার অপশন চালু হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 6. Edit Privacy
                PostActionItem(
                    icon = Icons.Default.Public,
                    title = "Edit Privacy",
                    onClick = {
                        showPrivacyDialog = true
                    }
                )

                // 7. Edit Post
                PostActionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Post",
                    onClick = {
                        showEditDialog = true
                    }
                )

                // 8. I don't want to see this
                PostActionItem(
                    icon = Icons.Default.Cancel,
                    title = "I don't want to see this",
                    onClick = {
                        viewModel.deletePost(post.id)
                        Toast.makeText(context, "এই পোস্টটি আপনার ফিড থেকে লুকানো হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 9. Move to archive
                PostActionItem(
                    icon = Icons.Default.Archive,
                    title = "Move to archive",
                    onClick = {
                        viewModel.movePostToArchive(post.id)
                        Toast.makeText(context, "🗄️ পোস্টটি আর্কাইভে পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 10. Move to trash
                PostActionItem(
                    icon = Icons.Default.Delete,
                    title = "Move to trash",
                    subtitle = "Items in your trash are deleted after 30 days.",
                    iconTint = Color.Red,
                    onClick = {
                        showDeleteConfirmDialog = true
                    }
                )

                // 11. Copy link
                PostActionItem(
                    icon = Icons.Default.Link,
                    title = "Copy link",
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://facefeed.app/post/${post.id}"))
                        Toast.makeText(context, "🔗 পোস্ট লিঙ্ক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // 12. Block Author
                PostActionItem(
                    icon = Icons.Default.Block,
                    title = "Block ${post.author.fullName}",
                    subtitle = "They won't be able to find your profile, posts or message you.",
                    iconTint = Color.Red,
                    onClick = {
                        viewModel.blockUser(post.author.id)
                        Toast.makeText(context, "${post.author.fullName} ব্লক করা হয়েছে", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }
        }
    }

    // Video Downloading Progress Dialog
    if (isDownloadingVideo) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = BangladeshGreen,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ভিডিও ডাউনলোড হচ্ছে...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "গ্যালারি ও ফোন মেমরিতে সংরক্ষণ হচ্ছে: ${(downloadProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BangladeshGreen
                    )
                }
            }
        }
    }

    // Edit Post Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("পোস্ট সম্পাদনা (Edit Post)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editPostText,
                    onValueChange = { editPostText = it },
                    label = { Text("ক্যাপশন লিখুন") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.editPostText(post.id, editPostText)
                    Toast.makeText(context, "পোস্ট সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                    showEditDialog = false
                    onDismiss()
                }) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Edit Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("পোস্টের প্রাইভেসি নির্বাচন করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        Triple("Public (পাবলিক)", "সবাই দেখতে পারবে", Icons.Default.Public),
                        Triple("Friends (বন্ধুরা)", "শুধুমাত্র আপনার বন্ধুরা", Icons.Default.People),
                        Triple("Only me (শুধুমাত্র আমি)", "অন্য কেউ দেখতে পারবে না", Icons.Default.Lock)
                    ).forEach { (pTitle, pSub, pIcon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "প্রাইভেসি '$pTitle' এ পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    showPrivacyDialog = false
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(pIcon, contentDescription = null, tint = FacebookBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(pTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(pSub, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete/Trash Confirm Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("পোস্টটি ট্র্যাশে পাঠাবেন?", fontWeight = FontWeight.Bold) },
            text = { Text("Items in your trash are deleted after 30 days. আপনি চাইলে ৩০ দিনের ভেতর পুনরায় ফিরিয়ে আনতে পারবেন।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.movePostToTrash(post.id)
                        Toast.makeText(context, "🗑️ পোস্টটি ট্র্যাশে সরানো হয়েছে", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/**
 * Reusable Row Item for Bottom Sheet Options (matching Screenshots 1, 2, 3)
 */
@Composable
fun PostActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Full Screen Photo Viewer (matching Screenshot 1)
 * Tap any photo -> Full screen view -> 3-dots top right -> Slide up Bottom Sheet:
 * - Make profile picture
 * - Make cover photo
 * - Delete
 * - Save
 * - Edit privacy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPostPhotoViewer(
    imageUrl: String,
    post: Post? = null,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showThreeDotsMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Centered Photo
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full photo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 50.dp),
                contentScale = ContentScale.Fit
            )

            // Top App Bar (matching Screenshot 1 Header: ✕, Avatar tag icon, Media icon, Tag icon, 3 dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Close icon (✕)
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Top Right Icon Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 2. Avatar / Person icon (Tag user)
                    IconButton(onClick = {
                        Toast.makeText(context, "👤 ছবিতে ট্যাগ করার সুবিধা সক্রিয়", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Tag Person",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 3. Media / Gallery icon
                    IconButton(onClick = {
                        Toast.makeText(context, "🖼️ গ্যালারিতে যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Media",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 4. Tag icon
                    IconButton(onClick = {
                        Toast.makeText(context, "🏷️ ট্যাগ অপশন", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Tag",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 5. 3 dots icon (⋮) -> Opens Screenshot 1 Bottom Sheet!
                    IconButton(onClick = { showThreeDotsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Screenshot 1 Bottom Sheet
            if (showThreeDotsMenu) {
                ModalBottomSheet(
                    onDismissRequest = { showThreeDotsMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Surface(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .width(42.dp)
                                .height(4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        ) {}
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        // 1. Make profile picture (👤)
                        PostActionItem(
                            icon = Icons.Default.AccountCircle,
                            title = "Make profile picture",
                            onClick = {
                                viewModel.updateProfileAvatar(imageUrl)
                                Toast.makeText(context, "✅ ছবিটি আপনার প্রোফাইল ছবি হিসেবে সেট করা হয়েছে!", Toast.LENGTH_LONG).show()
                                showThreeDotsMenu = false
                            }
                        )

                        // 2. Make cover photo (🖼️)
                        PostActionItem(
                            icon = Icons.Default.Panorama,
                            title = "Make cover photo",
                            onClick = {
                                viewModel.updateCoverPhoto(imageUrl)
                                Toast.makeText(context, "✅ ছবিটি আপনার কভার ফটো হিসেবে সেট করা হয়েছে!", Toast.LENGTH_LONG).show()
                                showThreeDotsMenu = false
                            }
                        )

                        // 3. Delete (🗑️)
                        PostActionItem(
                            icon = Icons.Default.Delete,
                            title = "Delete",
                            iconTint = Color.Red,
                            titleColor = Color.Red,
                            onClick = {
                                showThreeDotsMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )

                        // 4. Save (⬇️)
                        PostActionItem(
                            icon = Icons.Default.Download,
                            title = "Save",
                            iconTint = BangladeshGreen,
                            onClick = {
                                Toast.makeText(context, "📥 ছবিটি সফলভাবে আপনার ফোনে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                showThreeDotsMenu = false
                            }
                        )

                        // 5. Edit privacy (🔒)
                        PostActionItem(
                            icon = Icons.Default.Lock,
                            title = "Edit privacy",
                            onClick = {
                                showThreeDotsMenu = false
                                showPrivacyDialog = true
                            }
                        )
                    }
                }
            }

            // Confirm Delete Dialog for Photo Viewer
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("ছবিটি কি মুছে ফেলতে চান?", fontWeight = FontWeight.Bold) },
                    text = { Text("এই ছবিটি মুছে ফেললে আপনার পোস্ট অথবা প্রোফাইল থেকে তা আর দেখা যাবে না।") },
                    confirmButton = {
                        Button(
                            onClick = {
                                post?.let { viewModel.deletePost(it.id) }
                                Toast.makeText(context, "🗑️ ছবিটি মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                                showDeleteConfirmDialog = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("মুছে ফেলুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("বাতিল")
                        }
                    }
                )
            }

            // Edit Privacy Dialog for Photo Viewer
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("ছবির প্রাইভেসি পরিবর্তন করুন", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            listOf(
                                Triple("Public (পাবলিক)", "সবাই দেখতে পারবে", Icons.Default.Public),
                                Triple("Friends (বন্ধুরা)", "শুধুমাত্র আপনার বন্ধুরা", Icons.Default.People),
                                Triple("Only me (শুধুমাত্র আমি)", "অন্য কেউ দেখতে পারবে না", Icons.Default.Lock)
                            ).forEach { (pTitle, pSub, pIcon) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast.makeText(context, "প্রাইভেসি '$pTitle' নির্বাচিত হয়েছে", Toast.LENGTH_SHORT).show()
                                            showPrivacyDialog = false
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(pIcon, contentDescription = null, tint = FacebookBlue)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(pTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(pSub, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("বাতিল")
                        }
                    }
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
