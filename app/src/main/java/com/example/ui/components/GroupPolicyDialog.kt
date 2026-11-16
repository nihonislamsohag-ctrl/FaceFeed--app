package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.FirebaseManager
import com.example.data.SampleData
import com.example.model.User
import com.example.viewmodel.FaceFeedViewModel

/**
 * গ্রুপ পরিচালনার নীতিমালা (Group Management Policy) Screen
 * Exactly matches Attachment 1: IMG-20261104-WA6288_33ee7c43.jpg
 *
 * Items 1, 2, 3 are fully interactive and operational:
 *  - 1. নতুন গ্রুপ ক্রিয়েট করুন -> Opens Attachment 2 Dialog (IMG_20261104_201049_2a424c78.jpg)
 *  - 2. গ্রুপে ৩ জন এডমিন -> 3 Admins assignment & permissions panel
 *  - 3. ২ জন মডারেটর -> 2 Moderators assignment & permissions panel
 *  - 4. সবাই এডমিনের আদেশ পালন করুন -> Rules & conduct sheet
 *  - 5. Help & Support টিমের সাথে যোগাযোগ করুন -> 24/7 Support panel
 *  - 6. FotoFilm এর কোম্পানি Niyon Islam Sohag ভাই-কে অসংখ্য ধন্যবাদ -> Founder tribute
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPolicyDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onOpenLiveHangout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    // Sub-dialog states
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showThreeAdminsDialog by remember { mutableStateOf(false) }
    var showTwoModeratorsDialog by remember { mutableStateOf(false) }
    var showAdminRulesDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showFounderDialog by remember { mutableStateOf(false) }

    // Royal dark blue starry gradient background matching Screenshot 1
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060922),
            Color(0xFF0B113B),
            Color(0xFF141952),
            Color(0xFF090D2A)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar with Close & Live Hangout button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    // Button to launch live voice hangout room
                    Surface(
                        onClick = onOpenLiveHangout,
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1B5E20),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🎙️", fontSize = 14.sp)
                            Text(
                                text = "লাইভ পার্টি রুম",
                                color = Color(0xFFFFE082),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FotoFilm Brand Badge & Header (Exact match of Screenshot 1)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Green rounded square logo badge with red circle & flag
                    FotoFilmFlagLogo(size = 64)

                    Spacer(modifier = Modifier.width(14.dp))

                    // "FotoFilm" text with glowing golden gradient
                    Text(
                        text = "FotoFilm",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFE082),
                        letterSpacing = 1.sp
                    )
                }

                // Decorative filigree line with diamond: ◆ ──────── ◆
                PolicyFiligreeDivider()

                Spacer(modifier = Modifier.height(6.dp))

                // Title: গ্রুপ পরিচালনার নীতিমালা (Golden Bengali serif typography)
                Text(
                    text = "গ্রুপ পরিচালনার নীতিমালা",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE082),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ----------------------------------------------------
                // The 6 Numbered Policy Items from Image 1:
                // ----------------------------------------------------

                // Item 1: 1. নতুন গ্রুপ ক্রিয়েট করুন (➕👥)
                PolicyItemCard(
                    itemNumber = 1,
                    text = "1. নতুন গ্রুপ ক্রিয়েট করুন",
                    iconContent = {
                        Text("➕👥", fontSize = 24.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showCreateGroupDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 2: 2. গ্রুপে ৩ জন এডমিন (👥👑)
                PolicyItemCard(
                    itemNumber = 2,
                    text = "2. গ্রুপে ৩ জন এডমিন",
                    iconContent = {
                        Text("👥👑", fontSize = 24.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showThreeAdminsDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 3: 3. ২ জন মডারেটর (👥🛡️)
                PolicyItemCard(
                    itemNumber = 3,
                    text = "3. ২ জন মডারেটর",
                    iconContent = {
                        Text("👥🛡️", fontSize = 24.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showTwoModeratorsDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 4: 4. সবাই এডমিনের আদেশ পালন করুন (👥🛡️)
                PolicyItemCard(
                    itemNumber = 4,
                    text = "4. সবাই এডমিনের আদেশ পালন করুন",
                    iconContent = {
                        Text("👥🛡️", fontSize = 24.sp)
                    },
                    onClick = {
                        showAdminRulesDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 5: 5. কারো কোনো আইডিতে সমস্যা হলে, Help & Support টিমের সাথে যোগাযোগ করুন (📋👆)
                PolicyItemCard(
                    itemNumber = 5,
                    text = "5. কারো কোনো আইডিতে সমস্যা হলে,\nHelp & Support টিমের সাথে\nযোগাযোগ করুন",
                    iconContent = {
                        Text("📋👆", fontSize = 24.sp)
                    },
                    onClick = {
                        showHelpSupportDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 6: 6. FotoFilm এর কোম্পানি Niyon Islam Sohag ভাই-কে অসংখ্য ধন্যবাদ। (FotoFilm Logo badge)
                PolicyItemCard(
                    itemNumber = 6,
                    text = "6. FotoFilm এর কোম্পানি\nNiyon Islam Sohag ভাই-কে\nঅসংখ্য ধন্যবাদ।",
                    iconContent = {
                        FotoFilmFlagLogo(size = 36)
                    },
                    onClick = {
                        showFounderDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Footer: • FotoFilm • সুরক্ষিত • সংগঠিত • সফল • (Exact match of Screenshot 1)
                PolicyFiligreeDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• FotoFilm • সুরক্ষিত • সংগঠিত • সফল •",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ----------------------------------------------------
    // Sub-dialog 1: Exact Image 2 Dialog (গ্রুপ তৈরি করুন)
    // ----------------------------------------------------
    if (showCreateGroupDialog) {
        CreateGroupImage2Dialog(
            viewModel = viewModel,
            onDismiss = { showCreateGroupDialog = false },
            onGroupCreated = { groupName ->
                Toast.makeText(context, "🎉 \"$groupName\" গ্রুপ সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_LONG).show()
                showCreateGroupDialog = false
            }
        )
    }

    // Sub-dialog 2: 3 Admins Management Sheet
    if (showThreeAdminsDialog) {
        ThreeAdminsDialog(
            onDismiss = { showThreeAdminsDialog = false }
        )
    }

    // Sub-dialog 3: 2 Moderators Management Sheet
    if (showTwoModeratorsDialog) {
        TwoModeratorsDialog(
            onDismiss = { showTwoModeratorsDialog = false }
        )
    }

    // Sub-dialog 4: Admin Rules & Etiquette Sheet
    if (showAdminRulesDialog) {
        AdminRulesDialog(
            onDismiss = { showAdminRulesDialog = false }
        )
    }

    // Sub-dialog 5: Help & Support Team Sheet
    if (showHelpSupportDialog) {
        HelpSupportDialog(
            onDismiss = { showHelpSupportDialog = false }
        )
    }

    // Sub-dialog 6: Niyon Islam Sohag Founder Appreciation Sheet
    if (showFounderDialog) {
        FounderAppreciationDialog(
            onDismiss = { showFounderDialog = false }
        )
    }
}

/**
 * Exact replica of Attachment 2: IMG_20261104_201049_2a424c78.jpg
 * "গ্রুপ তৈরি করুন"
 * - OutlinedTextField: গ্রুপের নাম
 * - OutlinedTextField: গ্রুপের বিবরণ...
 * - Checkbox: প্রাইভেট গ্রুপ (Private Group)
 * - বাতিল (Text button, teal/green)
 * - কনফার্ম (Filled dark green button)
 */
@Composable
fun CreateGroupImage2Dialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onGroupCreated: (String) -> Unit
) {
    val context = LocalContext.current
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var isPrivateGroup by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Phone Gallery Photo Picker (Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "গ্যালারি থেকে ছবি সফলভাবে যুক্ত হয়েছে ✅", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                // Header Row: শিরোনাম "গ্রুপ তৈরি করুন" এবং উপরে ডান পাশে ব্যাক বাটন (Close/Back)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গ্রুপ তৈরি করুন",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )

                    // উপরে ডান পাশে ব্যাক বাটন (Back / Dismiss button)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFF1F3F4), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ব্যাক / বন্ধ করুন",
                            tint = Color(0xFF424242),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // গ্যালারি থেকে প্রোফাইল ছবি যুক্ত করার অপশন (Gallery Photo Picker Section)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9))
                                .border(1.5.dp, Color(0xFF0D652D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Group Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Gallery",
                                    tint = Color(0xFF0D652D),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedImageUri != null) "গ্রুপ প্রোফাইল ছবি যুক্ত হয়েছে ✅" else "গ্যালারি থেকে ছবি যুক্ত করুন",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1B)
                            )
                            Text(
                                text = if (selectedImageUri != null) "ছবি পরিবর্তন করতে ট্যাপ করুন" else "ফোনের গ্যালারি থেকে প্রোফাইল ছবি দিন 🖼️",
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        if (selectedImageUri != null) {
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Outlined Text Field 1: গ্রুপের নাম
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = {
                        Text(
                            text = "গ্রুপের নাম",
                            color = Color(0xFF757575),
                            fontSize = 15.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D652D),
                        unfocusedBorderColor = Color(0xFF8E8E93),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Outlined Text Field 2: গ্রুপের বিবরণ...
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    placeholder = {
                        Text(
                            text = "গ্রুপের বিবরণ...",
                            color = Color(0xFF757575),
                            fontSize = 15.sp
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D652D),
                        unfocusedBorderColor = Color(0xFF8E8E93),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Checkbox: প্রাইভেট গ্রুপ (Private Group)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPrivateGroup = !isPrivateGroup },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPrivateGroup,
                        onCheckedChange = { isPrivateGroup = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF0D652D),
                            uncheckedColor = Color(0xFF616161)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "প্রাইভেট গ্রুপ (Private Group)",
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons: বাতিল & কনফার্ম
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // বাতিল (Text button, teal/green text)
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "বাতিল",
                            color = Color(0xFF00796B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // কনফার্ম (Dark green filled pill button)
                    Button(
                        onClick = {
                            val finalName = if (groupName.isBlank()) "নতুন আড্ডা গ্রুপ" else groupName.trim()
                            val finalDesc = if (groupDescription.isBlank()) "FotoFilm বন্ধুদের আড্ডা ও লাইভ চ্যাট গ্রুপ" else groupDescription.trim()
                            val finalCover = selectedImageUri?.toString()
                                ?: "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&auto=format&fit=crop&q=80"

                            // Create Group with 3 Admins & 2 Moderators preset
                            val adminList = listOf(
                                User(
                                    id = "user_niyon",
                                    fullName = "Niyon Islam Sohag",
                                    username = "niyon_sohag",
                                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
                                ),
                                SampleData.friendMdMohiuddin,
                                SampleData.friendRahim
                            )
                            val modList = listOf(
                                SampleData.friendHasanMiaHowlader,
                                SampleData.friendTanvir
                            )

                            viewModel.createGroup(
                                name = finalName,
                                description = finalDesc,
                                isPrivate = isPrivateGroup,
                                coverUrl = finalCover,
                                initialAdmins = adminList,
                                initialModerators = modList
                            )

                            // Save to Firestore
                            FirebaseManager.saveGroup(
                                groupId = "grp_${System.currentTimeMillis()}",
                                groupData = mapOf(
                                    "name" to finalName,
                                    "description" to finalDesc,
                                    "coverUrl" to finalCover,
                                    "isPrivate" to isPrivateGroup,
                                    "creator" to "Niyon Islam Sohag",
                                    "admins" to listOf("Niyon Islam Sohag", "MD Mohiuddin", "Abdur Rahim"),
                                    "moderators" to listOf("Hasan Miya", "Tanvir Ahmed"),
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )

                            onGroupCreated(finalName)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D652D) // Rich dark green from screenshot
                        ),
                        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "কনফার্ম",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. গ্রুপে ৩ জন এডমিন (3 Admins Details & Assignment)
 */
@Composable
fun ThreeAdminsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "গ্রুপে ৩ জন এডমিন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "গ্রুপ পরিচালনা ও সর্বোচ্চ নিয়ন্ত্রণের জন্য ৩ জন সম্মানিত এডমিন:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Admin 1: Niyon Islam Sohag (Main Founder & Admin)
                AdminMemberCard(
                    rank = 1,
                    name = "Niyon Islam Sohag",
                    role = "প্রতিষ্ঠাতা ও প্রধান এডমিন (Owner)",
                    badge = "🌟 চিরস্থায়ী এডমিন",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                    isOwner = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Admin 2: MD Mohiuddin
                AdminMemberCard(
                    rank = 2,
                    name = "MD Mohiuddin",
                    role = "সহ-এডমিন (Co-Admin)",
                    badge = "👑 এডমিন প্যানেল",
                    avatarUrl = SampleData.friendMdMohiuddin.avatarUrl
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Admin 3: Abdur Rahim
                AdminMemberCard(
                    rank = 3,
                    name = "Abdur Rahim",
                    role = "সহ-এডমিন (Co-Admin)",
                    badge = "👑 এডমিন প্যানেল",
                    avatarUrl = SampleData.friendRahim.avatarUrl
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🛡️ এডমিনদের দায়িত্ব ও ক্ষমতা:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• নতুন সদস্য অনুমোদন ও রিমুভ করার অধিকার", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        Text("• গ্রুপের সেটিংস, নাম ও কভার পরিবর্তন", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        Text("• লাইভ আড্ডা পার্টি রুমে মাইক্রোফোন নিয়ন্ত্রণ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে (OK)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 3. ২ জন মডারেটর (2 Moderators Details & Assignment)
 */
@Composable
fun TwoModeratorsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "গ্রুপে ২ জন মডারেটর",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "গ্রুপের শৃঙ্খলা রক্ষা ও সার্বিক সহযোগিতায় ২ জন মডারেটর:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Moderator 1: Hasan Miya
                AdminMemberCard(
                    rank = 1,
                    name = "Hasan Miya",
                    role = "মডারেটর (Moderator)",
                    badge = "🛡️ নিরাপত্তা ও শৃঙ্খলা",
                    avatarUrl = SampleData.friendHasanMiaHowlader.avatarUrl
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Moderator 2: Tanvir Ahmed
                AdminMemberCard(
                    rank = 2,
                    name = "Tanvir Ahmed",
                    role = "মডারেটর (Moderator)",
                    badge = "🛡️ কনটেন্ট ফিল্টার",
                    avatarUrl = SampleData.friendTanvir.avatarUrl
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📋 মডারেটরদের দায়িত্ব:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• গ্রুপে স্প্যাম ও আপত্তিকর পোস্ট তদারকি ও মোছা", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        Text("• সদস্যদের আচরণবিধি মেনে চলার নির্দেশনা দেওয়া", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        Text("• যে কোনো জরুরি বিষয়ে এডমিনকে অবহিত করা", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে (OK)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 4. সবাই এডমিনের আদেশ পালন করুন
 */
@Composable
fun AdminRulesDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 এডমিনের আদেশ ও আচরণবিধি",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val rules = listOf(
                    "১. সকল সদস্যকে পরস্পরের প্রতি শ্রদ্ধা ও সুন্দর ভাষা ব্যবহার করতে হবে।",
                    "২. কোনো ধরনের উসকানিমূলক, রাজনৈতিক বা ধর্মীয় বিতর্কিত পোস্ট কঠোরভাবে নিষিদ্ধ।",
                    "৩. এডমিন ও মডারেটরদের সিদ্ধান্তই গ্রুপের চূড়ান্ত সিদ্ধান্ত হিসেবে বিবেচিত হবে।",
                    "৪. গ্রুপে স্প্যাম লিংক বা অনুমোদনহীন বিজ্ঞাপনী কনটেন্ট পোস্ট করা যাবে না।",
                    "৫. নিয়ম ভঙ্গকারীকে সাময়িক বা স্থায়ীভাবে গ্রুপ থেকে বহিষ্কার করা হবে।"
                )

                rules.forEach { rule ->
                    Text(
                        text = rule,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("মেনে চলব (Agreed)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 5. Help & Support টিমের সাথে যোগাযোগ করুন
 */
@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎧 FotoFilm Help & Support টিম",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "কারো কোনো আইডিতে সমস্যা হলে, পাসওয়ার্ড রিকভারি, একাউন্ট সিকিউরিটি বা যে কোনো টেকনিক্যাল জটিলতায় আমাদের ২৪/৭ সাপোর্ট টিমের সাথে সরাসরি যোগাযোগ করুন:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                SupportOptionRow(
                    icon = "📞",
                    title = "হটলাইন সাপোর্ট",
                    subtitle = "+880 1700-000000 (২৪/৭ ফ্রি কল)",
                    onClick = {
                        Toast.makeText(context, "হটলাইনে কল কানেক্ট করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SupportOptionRow(
                    icon = "💬",
                    title = "WhatsApp লাইভ হেল্পডেস্ক",
                    subtitle = "সরাসরি সাপোর্ট প্রতিনিধির সাথে চ্যাট",
                    onClick = {
                        Toast.makeText(context, "WhatsApp সাপোর্ট চালু হচ্ছে...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SupportOptionRow(
                    icon = "✉️",
                    title = "অফিসিয়াল ইমেইল",
                    subtitle = "support@fotofilm.com",
                    onClick = {
                        Toast.makeText(context, "ইমেইল ড্রাফট খোলা হচ্ছে...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 6. FotoFilm এর কোম্পানি Niyon Islam Sohag ভাই-কে অসংখ্য ধন্যবাদ।
 */
@Composable
fun FounderAppreciationDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Crown and FotoFilm Logo
                Box(contentAlignment = Alignment.Center) {
                    FotoFilmFlagLogo(size = 72)
                    Text("👑", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-14).dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Niyon Islam Sohag",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFE082)
                )

                Text(
                    text = "প্রতিষ্ঠাতা ও সিইও, FotoFilm",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FotoFilm পরিবারের সকল সদস্য ও ব্যবহারকারীদের পক্ষ থেকে কোম্পানি কর্ণধার ও প্রতিষ্ঠাতা Niyon Islam Sohag ভাই-কে জানাই আন্তরিক কৃতজ্ঞতা, ভালোবাসা ও অসংখ্য ধন্যবাদ! ❤️",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ধন্যবাদ Niyon ভাই ❤️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

/**
 * Reusable Policy Item Card matching Screenshot 1:
 * Rounded square badge with gold border on left, bold white text on right.
 */
@Composable
fun PolicyItemCard(
    itemNumber: Int,
    text: String,
    iconContent: @Composable () -> Unit,
    isFeatured: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(
            width = if (isFeatured) 1.5.dp else 1.dp,
            color = if (isFeatured) Color(0xFFFFD54F) else Color(0xFFFFD54F).copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold bordered rounded square icon badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0D1333),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                shadowElevation = 4.dp,
                modifier = Modifier.size(54.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    iconContent()
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Label matching Screenshot 1
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 24.sp,
                modifier = Modifier.weight(1f)
            )

            // Small subtle arrow indicator
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFFD54F).copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * FotoFilm Flag Logo Badge (Green rounded square, red disc, green flag)
 */
@Composable
fun FotoFilmFlagLogo(size: Int) {
    Surface(
        shape = RoundedCornerShape((size / 4).dp),
        color = Color(0xFF0D652D),
        border = BorderStroke(2.dp, Color(0xFFFFD54F)),
        shadowElevation = 6.dp,
        modifier = Modifier.size(size.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Red circular disc
            Surface(
                shape = CircleShape,
                color = Color(0xFFD32F2F),
                modifier = Modifier.size((size * 0.55).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚩", fontSize = (size * 0.32).sp)
                }
            }
        }
    }
}

/**
 * Decorative Filigree Divider: ◆ ──────── ◆
 */
@Composable
fun PolicyFiligreeDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("◆", color = Color(0xFFFFD54F), fontSize = 10.sp)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD54F).copy(alpha = 0.1f),
                            Color(0xFFFFD54F).copy(alpha = 0.8f),
                            Color(0xFFFFD54F).copy(alpha = 0.1f)
                        )
                    )
                )
        )
        Text("◆", color = Color(0xFFFFD54F), fontSize = 10.sp)
    }
}

/**
 * Admin Member Display Row
 */
@Composable
fun AdminMemberCard(
    rank: Int,
    name: String,
    role: String,
    badge: String,
    avatarUrl: String,
    isOwner: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, if (isOwner) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFFFFD54F), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    if (isOwner) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("👑", fontSize = 13.sp)
                    }
                }
                Text(
                    text = role,
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0D652D).copy(alpha = 0.5f),
                border = BorderStroke(0.8.dp, Color(0xFFFFD54F))
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Support option item row
 */
@Composable
fun SupportOptionRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}
