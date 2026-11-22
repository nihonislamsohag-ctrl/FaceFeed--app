package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

/**
 * 11 Features Profile Settings Modal (Exact match to Screenshot 1: IMG_20261012_092741.jpg)
 * Triggered by 3-dots icon on Profile Screen:
 * 1. Advertise
 * 2. Profile status
 * 3. Archive
 * 4. Activity log
 * 5. Review posts and tags
 * 6. Add highlights
 * 7. View as
 * 8. Search
 * 9. Turn off pro mode
 * 10. Invite people to connect
 * 11. Copy link to profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMoreOptionsDialog(
    user: User,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onActivateViewAs: () -> Unit,
    onOpenCreateHighlight: () -> Unit
) {
    val context = LocalContext.current
    val isProMode by viewModel.isProModeActive.collectAsState()

    // Sub-dialog states for the 11 features
    var showAdvertiseDialog by remember { mutableStateOf(false) }
    var showProfileStatusDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var showActivityLogDialog by remember { mutableStateOf(false) }
    var showReviewTagsDialog by remember { mutableStateOf(false) }
    var showProModeDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showProfileSearchDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar matching Screenshot 1: <  Niyon Islam So... ✏️ 🔍 •••
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = {
                            onDismiss()
                            onOpenEditProfile()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(onClick = { showProfileSearchDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = { /* Already in 3-dot sheet */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Header Snippet with Cover Preview matching Screenshot 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    FotoFilmCoverBackground(
                        imageUrl = user.coverUrl,
                        contentDescription = "Cover preview",
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                                )
                            )
                    )
                }

                // All 11 Feature Options List (matching Screenshot 1)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // 1. Advertise
                    ProfileOptionRow(
                        icon = Icons.Default.Campaign,
                        title = "Advertise",
                        onClick = { showAdvertiseDialog = true }
                    )

                    // 2. Profile status
                    ProfileOptionRow(
                        icon = Icons.Default.Shield,
                        title = "Profile status",
                        onClick = { showProfileStatusDialog = true }
                    )

                    // 3. Archive
                    ProfileOptionRow(
                        icon = Icons.Default.Inventory2,
                        title = "Archive",
                        onClick = { showArchiveDialog = true }
                    )

                    // 4. Activity log
                    ProfileOptionRow(
                        icon = Icons.Default.FormatListBulleted,
                        title = "Activity log",
                        onClick = { showActivityLogDialog = true }
                    )

                    // 5. Review posts and tags
                    ProfileOptionRow(
                        icon = Icons.Default.MarkChatRead,
                        title = "Review posts and tags",
                        onClick = { showReviewTagsDialog = true }
                    )

                    // 6. Add highlights
                    ProfileOptionRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Add highlights",
                        onClick = {
                            onDismiss()
                            onOpenCreateHighlight()
                        }
                    )

                    // 7. View as
                    ProfileOptionRow(
                        icon = Icons.Default.Visibility,
                        title = "View as",
                        onClick = {
                            onDismiss()
                            onActivateViewAs()
                        }
                    )

                    // 8. Search
                    ProfileOptionRow(
                        icon = Icons.Default.Search,
                        title = "Search",
                        onClick = { showProfileSearchDialog = true }
                    )

                    // 9. Turn off pro mode (Professional mode)
                    ProfileOptionRow(
                        icon = Icons.Default.Undo,
                        title = if (isProMode) "Turn off pro mode" else "Turn on pro mode",
                        onClick = { showProModeDialog = true }
                    )

                    // 10. Invite people to connect
                    ProfileOptionRow(
                        icon = Icons.Default.PersonAddAlt1,
                        title = "Invite people to connect",
                        onClick = { showInviteDialog = true }
                    )

                    // 11. Copy link to profile
                    ProfileOptionRow(
                        icon = Icons.Default.Link,
                        title = "Copy link to profile",
                        onClick = {
                            val profileLink = "https://facefeed.app/${user.username.ifEmpty { "niyonislamsohag" }}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Profile Link", profileLink)
                            clipboard.setPrimaryClip(clip)
                            AppSoundManager.playReactionChime()
                            Toast.makeText(context, "প্রোফাইল লিংক কপি করা হয়েছে! 📋\n$profileLink", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // =========================================================================
    // 1. ADVERTISE DIALOG
    // =========================================================================
    if (showAdvertiseDialog) {
        var selectedBudget by remember { mutableIntStateOf(500) }
        var selectedDuration by remember { mutableIntStateOf(7) }
        AlertDialog(
            onDismissRequest = { showAdvertiseDialog = false },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("প্রোফাইল ও পোস্ট Advertise (বিজ্ঞাপন)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("আপনার প্রোফাইল ও পোস্ট বেশি মানুষের কাছে পৌঁছাতে FotoFilm স্টাইল ক্যাম্পেইন চালু করুন।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("বাজেট নির্বাচন করুন:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(500, 1000, 2000).forEach { b ->
                            FilterChip(
                                selected = selectedBudget == b,
                                onClick = { selectedBudget = b },
                                label = { Text("৳ $b", fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("আনুমানিক রিচ: ${(selectedBudget * 120)} - ${(selectedBudget * 280)} মানুষ 👥", fontSize = 12.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "অ্যাড ক্যাম্পেইন সফলভাবে তৈরি হয়েছে! 📢", Toast.LENGTH_SHORT).show()
                        showAdvertiseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("অ্যাড চালু করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdvertiseDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // =========================================================================
    // 2. PROFILE STATUS DIALOG
    // =========================================================================
    if (showProfileStatusDialog) {
        AlertDialog(
            onDismissRequest = { showProfileStatusDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(38.dp)) },
            title = { Text("Profile status (প্রোফাইল স্ট্যাটাস)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = BangladeshGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Good standing (চমৎকার স্ট্যাটাস)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BangladeshGreen)
                                Text("আপনার প্রোফাইলে কোনো ভায়োলেশন বা স্ট্রাইক নেই।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text("• Profile is recommendable ✅ (সুপারিশযোগ্য)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Fida Verified সুরক্ষাপ্রাপ্ত ✅", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("• কমিউনিটি স্ট্যান্ডার্ড ১০০% মেনে চলা হয়েছে 🛡️", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(onClick = { showProfileStatusDialog = false }) { Text("ঠিক আছে") }
            }
        )
    }

    // =========================================================================
    // 3. ARCHIVE DIALOG
    // =========================================================================
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("Archive (আর্কাইভ ও রিসাইকেল বিন)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার পুরনো স্টোরি ও পোস্ট নিরাপদে সংরক্ষিত আছে:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().clickable {
                            Toast.makeText(context, "স্টোরি আর্কাইভ খোলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = FacebookBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Story archive (স্টোরি আর্কাইভ)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().clickable {
                            Toast.makeText(context, "পোস্ট আর্কাইভ খোলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = FacebookBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Posts archive (পোস্ট আর্কাইভ)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().clickable {
                            Toast.makeText(context, "রিসাইকেল বিন খোলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Trash / Recycle bin (রিসাইকেল বিন)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showArchiveDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }

    // =========================================================================
    // 4. ACTIVITY LOG DIALOG
    // =========================================================================
    if (showActivityLogDialog) {
        AlertDialog(
            onDismissRequest = { showActivityLogDialog = false },
            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("Activity log (অ্যাক্টিভিটি লগ)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার প্রোফাইলের সাম্প্রতিক কার্যকলাপের তালিকা:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(
                        "পোস্ট করেছেন: 'শুভ সকাল বন্ধুরা' • আজ সকাল ০৯:৩০ AM",
                        "প্রোফাইল ছবি পরিবর্তন করেছেন • গতকাল রাত ১০:১৫ PM",
                        "লাইক দিয়েছেন Tareq Mahmud-এর পোস্টে • গতকাল বিকাল ০৫:০০ PM",
                        "নোট আপডেট করেছেন • ২ দিন আগে"
                    ).forEach { act ->
                        Text("• $act", fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showActivityLogDialog = false }) { Text("ঠিক আছে") }
            }
        )
    }

    // =========================================================================
    // 5. REVIEW POSTS AND TAGS DIALOG
    // =========================================================================
    if (showReviewTagsDialog) {
        AlertDialog(
            onDismissRequest = { showReviewTagsDialog = false },
            icon = { Icon(Icons.Default.MarkChatRead, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("Review posts and tags", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("টাইমলাইন রিভিউ: বন্ধুরা আপনাকে কোনো পোস্টে ট্যাগ করলে তা অনুমোদন বা হাইড করার সুবিধা।", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Tareq Mahmud আপনাকে একটি ছবিতে ট্যাগ করেছেন", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { Toast.makeText(context, "প্রোফাইলে যুক্ত হয়েছে ✅", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("যুক্ত করুন", fontSize = 11.sp, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { Toast.makeText(context, "ট্যাগ লুকানো হয়েছে ❌", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("লুকান", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReviewTagsDialog = false }) { Text("সম্পন্ন") }
            }
        )
    }

    // =========================================================================
    // 8. PROFILE SEARCH DIALOG
    // =========================================================================
    if (showProfileSearchDialog) {
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showProfileSearchDialog = false },
            title = { Text("প্রোফাইলে সার্চ করুন 🔍", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("পোস্ট, ছবি বা স্মৃতি খুঁজুন...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (query.isNotEmpty()) {
                        Text("'$query' দিয়ে ৩টি পোস্ট ও ২টি ছবি পাওয়া গেছে", fontSize = 12.sp, color = FacebookBlue)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showProfileSearchDialog = false }) { Text("খুঁজুন") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileSearchDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // =========================================================================
    // 9. PRO MODE DIALOG
    // =========================================================================
    if (showProModeDialog) {
        AlertDialog(
            onDismissRequest = { showProModeDialog = false },
            icon = { Icon(Icons.Default.WorkHistory, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text(if (isProMode) "Turn off pro mode?" else "Turn on pro mode?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (isProMode)
                        "প্রফেশনাল মোড বন্ধ করলে ক্রিয়েটর টুলস ও ইনসাইটস ড্যাশবোর্ড সাময়িকভাবে নিষ্ক্রিয় হবে। আপনি যেকোনো সময় আবার চালু করতে পারবেন।"
                    else
                        "প্রফেশনাল মোড চালু করলে আপনি মনিটাইজেশন, প্রোফাইল ফলোয়ার কাউন্ট ও অ্যাডভান্সড অ্যানালিটিক্স সুবিধা পাবেন।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleProMode()
                        Toast.makeText(context, if (!isProMode) "প্রফেশনাল মোড চালু হয়েছে! 💼" else "প্রফেশনাল মোড বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        showProModeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isProMode) Color.Red else FacebookBlue)
                ) {
                    Text(if (isProMode) "Turn off" else "Turn on", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProModeDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // =========================================================================
    // 10. INVITE PEOPLE TO CONNECT DIALOG
    // =========================================================================
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            icon = { Icon(Icons.Default.PersonAddAlt1, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(36.dp)) },
            title = { Text("Invite people to connect", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার বন্ধু ও পরিচিতদের FotoFilm-এ যুক্ত হতে আমন্ত্রণ জানান:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = {
                            val profileLink = "https://facefeed.app/${user.username.ifEmpty { "niyonislamsohag" }}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Invite Link", profileLink)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "ইনভাইট লিঙ্ক কপি হয়েছে! 🔗", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আমন্ত্রণ লিঙ্ক শেয়ার করুন", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

/**
 * Standard clickable row matching Screenshot 1:
 * Circular light gray icon + Bold title text
 */
@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
