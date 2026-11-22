package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.PostAudioTrack
import com.example.model.SocialPage
import com.example.model.Translations
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.VoiceAudioManager
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget
import java.util.UUID

@Composable
fun CreatePostDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    pageAuthor: SocialPage? = null
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()
    val dailyUsage by viewModel.currentUserDailyUsage.collectAsState()
    val quotaConfig by viewModel.quotaCompressionConfig.collectAsState()

    val authorUser = remember(currentUser, pageAuthor) {
        if (pageAuthor != null) {
            User(
                id = "page_${pageAuthor.id}",
                username = pageAuthor.name.lowercase().replace(" ", "_"),
                fullName = pageAuthor.name,
                avatarUrl = pageAuthor.avatarUrl,
                isVerified = true
            )
        } else {
            currentUser
        }
    }

    var postTitle by remember { mutableStateOf("") }
    var postText by remember { mutableStateOf("") }
    var hashtagInput by remember { mutableStateOf("") }
    var hashtagsList by remember { mutableStateOf(mutableListOf<String>()) }
    var selectedImages by remember { mutableStateOf(listOf<String>()) }
    var isVideoPost by remember { mutableStateOf(false) }
    var isReelPost by remember { mutableStateOf(false) }
    var shareToFaceGram by remember { mutableStateOf(false) }
    var shareToFaceClip by remember { mutableStateOf(false) }
    var selectedAudioTrack by remember { mutableStateOf<PostAudioTrack?>(null) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var showPhotoAlbumDialog by remember { mutableStateOf(false) }
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showSafetyWarningDialog by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val isRecordingVoice by VoiceAudioManager.isRecording.collectAsState()
    val isRecordingPaused by VoiceAudioManager.isRecordingPaused.collectAsState()
    val recordingTarget by VoiceAudioManager.activeRecordingTarget.collectAsState()
    val isRecordingPostVoice = isRecordingVoice && recordingTarget == "create_post"
    val recordingSeconds by VoiceAudioManager.recordingSeconds.collectAsState()
    val recordingAmplitude by VoiceAudioManager.recordingAmplitude.collectAsState()
    val playingItemId by VoiceAudioManager.playingItemId.collectAsState()
    val isVoicePlaying by VoiceAudioManager.isPlaying.collectAsState()
    var pendingStartPostVoice by remember { mutableStateOf(false) }

    val recordVoicePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingStartPostVoice) {
            pendingStartPostVoice = false
            VoiceAudioManager.startRecording(context, "create_post")
        } else {
            pendingStartPostVoice = false
            if (!granted) {
                Toast.makeText(context, "ভয়েস পোস্ট করতে মাইক্রোফোন পারমিশন দিন", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val startVoicePostRecording: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            VoiceAudioManager.startRecording(context, "create_post")
        } else {
            pendingStartPostVoice = true
            recordVoicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            VoiceAudioManager.stopPlayback()
            if (VoiceAudioManager.activeRecordingTarget.value == "create_post") {
                VoiceAudioManager.cancelRecording()
            }
        }
    }

    // Read any pending images or videos passed into CreatePost (e.g. from HomeScreen gallery launcher or Profile video launcher)
    val pendingImages by viewModel.pendingPostImages.collectAsState()
    val pendingIsVideo by viewModel.pendingPostIsVideo.collectAsState()
    val pendingAudio by viewModel.pendingPostAudio.collectAsState()

    LaunchedEffect(pendingImages, pendingIsVideo) {
        if (pendingImages.isNotEmpty()) {
            selectedImages = (selectedImages + pendingImages).distinct().take(15)
            if (pendingIsVideo) {
                isVideoPost = true
            }
            viewModel.clearPendingPostImages()
        }
    }

    LaunchedEffect(pendingAudio) {
        if (pendingAudio != null) {
            selectedAudioTrack = pendingAudio
            viewModel.clearPendingPostAudio()
        }
    }

    // Direct Phone Gallery Launcher (Opens the real device Gallery app)
    val phoneGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages = (selectedImages + uris.map { it.toString() }).distinct().take(15)
        }
    }

    // Single photo / Camera image picker
    val singlePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImages = (selectedImages + uri.toString()).distinct().take(15)
        }
    }

    // Direct Video Launcher for uploading video from phone gallery
    val phoneVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImages = listOf(uri.toString())
            isVideoPost = true
        }
    }

    // Phone Storage & File Manager Picker (any files/images/videos from device)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages = (selectedImages + uris.map { it.toString() }).distinct().take(15)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Header with explicit Back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back / বের হয়ে যান",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = strings.createPostTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Button(
                            onClick = {
                                if (currentUser.isAccountActive && (postText.isNotBlank() || postTitle.isNotBlank() || selectedImages.isNotEmpty() || selectedAudioTrack != null)) {
                                    val safetyCheck = com.example.util.SafetyPolicyValidator.validateContent(postText + " " + postTitle + " " + hashtagInput)
                                    if (!safetyCheck.isAllowed) {
                                        showSafetyWarningDialog = safetyCheck.reasonMessage
                                        return@Button
                                    }
                                    viewModel.createPost(
                                        text = postText,
                                        hashtags = hashtagsList,
                                        images = if (selectedImages.isNotEmpty()) selectedImages else emptyList(),
                                        isVideo = isVideoPost || shareToFaceClip,
                                        videoTitle = if (isVideoPost || shareToFaceClip) (postTitle.ifBlank { postText }).take(30) else null,
                                        isReel = isReelPost || shareToFaceClip,
                                        title = postTitle.takeIf { it.isNotBlank() },
                                        audioTrack = selectedAudioTrack,
                                        isFaceGram = shareToFaceGram,
                                        isFaceClip = shareToFaceClip,
                                        customAuthor = if (pageAuthor != null) authorUser else null
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = currentUser.isAccountActive && (postText.isNotBlank() || postTitle.isNotBlank() || selectedImages.isNotEmpty() || selectedAudioTrack != null),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text(strings.postButton, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!currentUser.isAccountActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🚫 অ্যাকাউন্ট অফ: ${currentUser.suspensionReason ?: "অ্যাডমিন Niyon Islam Sohag দ্বারা সাময়িক নিষ্ক্রিয় করা হয়েছে।"}",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Auto-Compression & 43.1 MB Daily Remaining Budget Indicator
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(0.8.dp, BangladeshGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = "অটো কম্প্রেশন",
                                tint = BangladeshGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(
                                    java.util.Locale.US,
                                    "🗜️ অটো কম্প্রেশন %s (১০০MB→৩০MB) • আজকের বাকি: %.1f / ৪৩.১ MB",
                                    if (quotaConfig.isAutoCompressionEnabled) "চালু" else "বন্ধ",
                                    dailyUsage.remainingMb
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    // User / Page Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FotoFilmAvatar(
                            imageUrl = authorUser.avatarUrl,
                            name = authorUser.fullName,
                            modifier = Modifier.size(46.dp),
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = authorUser.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (pageAuthor != null || authorUser.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = FacebookBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (pageAuthor != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (pageAuthor != null) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (pageAuthor != null) "Public • ${pageAuthor.category} (পেইজ পোস্ট)" else "Public",
                                        fontSize = 11.sp,
                                        color = if (pageAuthor != null) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (pageAuthor != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // 1. Post Description / What's on your mind (উপরে লেখার ডিসক্রিপশন বক্স)
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text(strings.whatsOnYourMind, fontSize = 16.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Post Title Input (লেখার টাইটেল)
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        placeholder = { Text("📝 লেখার টাইটেল / পোস্টের শিরোনাম লিখুন (ঐচ্ছিক)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ========================================================
                    // 1. Direct Real Phone Gallery & Media Upload (উপরে উঠানো হয়েছে)
                    // ========================================================
                    Button(
                        onClick = {
                            try {
                                phoneGalleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                filePickerLauncher.launch("*/*")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📱 ফোন গ্যালারি থেকে ছবি যোগ করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Video Upload from Phone Gallery Button
                    Button(
                        onClick = {
                            try {
                                phoneVideoLauncher.launch("video/*")
                            } catch (e: Exception) {
                                filePickerLauncher.launch("video/*")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎥 ফোন গ্যালারি থেকে ভিডিও আপলোড করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone Files & Storage Manager Picker Button
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📁 ফোন ফাইলস ও ডকুমেন্ট (File Manager / Storage)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // In-App Photo Album Collection Button
                    OutlinedButton(
                        onClick = { showPhotoAlbumDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, FacebookBlue)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🖼️ ফটো অ্যালবাম কালেকশন (তাত্ক্ষণিক ছবি নির্বাচন)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FacebookBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                singlePhotoLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF00897B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📷 ক্যামেরা ছবি",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showAddLinkDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF8E24AA), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🔗 ছবির লিংক",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Informative helpful banner explaining how phone gallery works on real phones vs browser preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আসল মোবাইল ফোনে এই অ্যাপটি (APK) চালালে সাথে সাথে আপনার ফোনের মেমোরি কার্ড ও ক্যামেরার সমস্ত ছবি ওপেন হবে। এখন প্রিভিউতে আপনি ফোন গ্যালারি অথবা নিচের অ্যালবাম কালেকশন থেকেও ছবি বেছে নিতে পারেন।",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Selected Photos Grid Preview or Video Preview
                    if (selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (isVideoPost) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("📹 ভিডিও প্রস্তুত (আপলোড হবে)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE53935))
                                        }
                                        TextButton(onClick = { 
                                            selectedImages = emptyList()
                                            isVideoPost = false 
                                        }) {
                                            Text("মুছুন", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = selectedImages.first(),
                                            contentDescription = "Video Thumbnail",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.65f),
                                            modifier = Modifier.size(50.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedImages.size}/15 টি ছবি সিলেক্ট করা হয়েছে",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FacebookBlue
                                )
                                TextButton(onClick = { selectedImages = emptyList() }) {
                                    Text("মুছে ফেলুন", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TenPhotoGrid(images = selectedImages, onImageClick = {})
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Attached Music Track or Recorded Voice Post Card
                    selectedAudioTrack?.let { track ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val isPreviewPlaying = isVoicePlaying && playingItemId == track.id
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BangladeshGreen.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BangladeshGreen,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            val url = track.audioUrl?.trim().orEmpty()
                                            if (url.isNotBlank()) {
                                                VoiceAudioManager.togglePlay(context, track.id, url)
                                            } else {
                                                com.example.util.AppSoundManager.playAudio(track, context)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play/Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BangladeshGreen
                                    )
                                    Text(
                                        text = "${track.artist} • ${track.category} (${track.duration})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = {
                                    VoiceAudioManager.stopPlayback()
                                    selectedAudioTrack = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove audio",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Voice Post Recorder Panel or Record Trigger Button
                    if (isRecordingPostVoice) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.2.dp, BangladeshRed.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = BangladeshRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format(
                                                "🎙️ ভয়েস পোস্ট রেকর্ড হচ্ছে (%02d:%02d)",
                                                recordingSeconds / 60,
                                                recordingSeconds % 60
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BangladeshRed
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val bars = listOf(8f, 16f, 22f, 12f, 20f, 14f, 18f, 10f)
                                        bars.forEachIndexed { idx, baseH ->
                                            val h = if (isRecordingPaused) 5f else (baseH * (0.35f + recordingAmplitude * (0.7f + (idx % 2) * 0.25f))).coerceIn(5f, 24f)
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(h.dp)
                                                    .clip(RoundedCornerShape(1.5.dp))
                                                    .background(BangladeshRed)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { VoiceAudioManager.cancelRecording() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("বাতিল", fontSize = 12.sp, color = BangladeshRed, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { VoiceAudioManager.togglePauseRecording() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isRecordingPaused) "চালু" else "বিরতি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            val rec = VoiceAudioManager.stopRecording()
                                            if (rec != null) {
                                                val mmSs = String.format("%d:%02d", rec.durationSeconds / 60, rec.durationSeconds % 60)
                                                selectedAudioTrack = PostAudioTrack(
                                                    id = "voice_post_${UUID.randomUUID().toString().take(8)}",
                                                    title = "🎙️ ভয়েস পোস্ট ($mmSs)",
                                                    artist = authorUser.fullName,
                                                    category = "ভয়েস পোস্ট",
                                                    duration = mmSs,
                                                    audioUrl = rec.fileUri
                                                )
                                            } else {
                                                Toast.makeText(context, "কমপক্ষে ১ সেকেন্ড কথা বলে যুক্ত করুন", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                                    ) {
                                        Text("✔ যুক্ত করুন", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { startVoicePostRecording() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎙️ সরাসরি ভয়েস রেকর্ড করে পোস্ট করুন (Voice Post)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Music Track Picker Button
                    Button(
                        onClick = { showMusicPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedAudioTrack == null) "🎵 অডিও গান, গজল, ডিজে বা সুর এড করুন" else "🔄 অডিও গান পরিবর্তন করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 10 Hashtags Section
                    Text(
                        text = "${strings.hashtagsHint} (${hashtagsList.size}/10)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hashtagInput,
                            onValueChange = { hashtagInput = it },
                            placeholder = { Text("#FotoFilm #Bangladesh", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val tag = if (hashtagInput.startsWith("#")) hashtagInput.trim() else "#${hashtagInput.trim()}"
                                if (tag.length > 1 && hashtagsList.size < 10 && !hashtagsList.contains(tag)) {
                                    hashtagsList.add(tag)
                                    hashtagInput = ""
                                }
                            },
                            enabled = hashtagInput.isNotBlank() && hashtagsList.size < 10,
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                        ) {
                            Text("+ Add")
                        }
                    }

                    // Display Current Hashtags
                    if (hashtagsList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hashtagsList.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = FacebookBlue.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = tag, color = FacebookBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { hashtagsList.remove(tag) },
                                            tint = FacebookBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ========================================================
                    // FaceGram ও FaceClip অপশন (নিচে এড ও পোস্ট করলে শেয়ার)
                    // ========================================================
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color(0xFFE1306C).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color(0xFFE1306C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌐 ক্রস-পোস্টিং অপশন (GramFilm ও FaceClip)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // GramFilm Option with official GramFilm Logo
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (shareToFaceGram) BangladeshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = if (shareToFaceGram) BorderStroke(1.2.dp, BangladeshGreen) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { shareToFaceGram = !shareToFaceGram }
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
                                        // Official GramFilm Logo Image
                                        Image(
                                            painter = painterResource(id = com.example.R.drawable.ic_gramfilm_logo),
                                            contentDescription = "GramFilm Logo",
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "GramFilm",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = BangladeshGreen.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "অটো-শেয়ার",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BangladeshGreen,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "পোস্ট করলে সাথে GramFilm এ পোস্ট হয়ে যাবে",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = shareToFaceGram,
                                        onCheckedChange = { shareToFaceGram = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = BangladeshGreen
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // FaceClip Option
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (shareToFaceClip) Color(0xFF9C27B0).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = if (shareToFaceClip) BorderStroke(1.dp, Color(0xFF9C27B0)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        shareToFaceClip = !shareToFaceClip
                                        if (shareToFaceClip) {
                                            isVideoPost = true
                                            isReelPost = true
                                        }
                                    }
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
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF9C27B0),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.VideoLibrary,
                                                    contentDescription = "FaceClip",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "FaceClip",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF9C27B0).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "শর্ট ভিডিও",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF9C27B0),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "শর্ট ভিডিও হিসেবে FaceClip এ পোস্ট হবে",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = shareToFaceClip,
                                        onCheckedChange = {
                                            shareToFaceClip = it
                                            if (it) {
                                                isVideoPost = true
                                                isReelPost = true
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF9C27B0)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Attachment Actions
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Phone Gallery (Direct launch of device Gallery app)
                            IconButton(
                                onClick = {
                                    phoneGalleryLauncher.launch("image/*")
                                }
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color(0xFF45BD62))
                            }

                            // Phone Files / Storage (Direct launch)
                            IconButton(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                }
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Files", tint = FacebookBlue)
                            }

                            // Camera
                            IconButton(
                                onClick = {
                                    singlePhotoLauncher.launch("image/*")
                                }
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = FacebookBlue)
                            }

                            // Audio Music Picker Icon
                            IconButton(
                                onClick = { showMusicPicker = true }
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Color(0xFF8E24AA))
                            }

                            // Short Video / Reel Toggle
                            IconButton(
                                onClick = {
                                    isReelPost = !isReelPost
                                    isVideoPost = isReelPost
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Short Video",
                                    tint = if (isReelPost) BangladeshRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Feeling / Emoji
                            IconButton(
                                onClick = { postText += " feeling excited 🤩" }
                            ) {
                                Icon(Icons.Default.Mood, contentDescription = "Feeling", tint = Color(0xFFF7B125))
                            }
                        }
                    }
                }
            }
        }
    }

    // Music Picker Dialog (Facebook-style Music Add Hub)
    if (showMusicPicker) {
        FacebookMusicAddDialog(
            viewModel = viewModel,
            onDismiss = { showMusicPicker = false },
            onTrackSelected = { track ->
                selectedAudioTrack = track
                showMusicPicker = false
            }
        )
    }

    // In-App Photo Album Collection Dialog (Instant selection for preview & testing)
    if (showPhotoAlbumDialog) {
        PostAlbumPickerDialog(
            initialSelected = selectedImages,
            onPhotosSelected = { newPhotos ->
                selectedImages = (selectedImages + newPhotos).distinct().take(15)
                showPhotoAlbumDialog = false
            },
            onDismiss = { showPhotoAlbumDialog = false }
        )
    }

    // Add Image from URL / Link Dialog
    if (showAddLinkDialog) {
        PostImageUrlDialog(
            onAddUrl = { url ->
                if (url.isNotBlank()) {
                    selectedImages = (selectedImages + url.trim()).distinct().take(15)
                }
                showAddLinkDialog = false
            },
            onDismiss = { showAddLinkDialog = false }
        )
    }

    if (showSafetyWarningDialog != null) {
        AlertDialog(
            onDismissRequest = { showSafetyWarningDialog = null },
            icon = {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "⛔ কনটেন্ট নিরাপত্তা নীতি লঙ্ঘন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Red
                )
            },
            text = {
                Column {
                    Text(
                        text = showSafetyWarningDialog!!,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FotoFilm ও Right Manager Fida নিরাপত্তা প্রোটোকল অনুযায়ী কোনো ধরনের নেশা বা মাদকদ্রব্য এবং জুয়া/বাজির গেমস বা লিংক প্রচার আইনত দণ্ডনীয় ও সম্পূর্ণ নিষিদ্ধ।",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafetyWarningDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("সচেতন হলাম (বুঝেছি)")
                }
            }
        )
    }
}

/**
 * Universal In-App Photo Album Picker for fast selection
 */
@Composable
fun PostAlbumPickerDialog(
    initialSelected: List<String> = emptyList(),
    onPhotosSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val albums = listOf(
        "সব ছবি" to emptyList<String>(),
        "ক্যামেরা অ্যালবাম" to emptyList<String>(),
        "বাংলাদেশ ও প্রকৃতি" to emptyList<String>()
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedUrls by remember { mutableStateOf(initialSelected.toSet()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🖼️ ফটো অ্যালবাম কালেকশন",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "যেকোনো ছবি ট্যাপ করে সিলেক্ট করুন",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Album Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    albums.forEachIndexed { index, (name, _) ->
                        FilterChip(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            label = { Text(name, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photos Grid
                val currentPhotos = albums[selectedTabIndex].second
                if (currentPhotos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো ডেমো অ্যালবাম ছবি নেই",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ফোনের গ্যালারি বা ক্যামেরা থেকে ছবি আপলোড করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(currentPhotos) { url ->
                            val isSelected = selectedUrls.contains(url)
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.5.dp,
                                        color = if (isSelected) FacebookBlue else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedUrls = if (isSelected) {
                                            selectedUrls - url
                                        } else {
                                            selectedUrls + url
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0x331877F2))
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = FacebookBlue,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            onPhotosSelected(selectedUrls.toList())
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        enabled = selectedUrls.isNotEmpty()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পোস্টে যোগ করুন (${selectedUrls.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Add Image via URL/Link Dialog
 */
@Composable
fun PostImageUrlDialog(
    onAddUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "🔗 ছবির ওয়েব লিংক দিয়ে যোগ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("https://example.com/image.jpg", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("ছবির লিংক পেস্ট করুন") }
                )

                if (inputUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = inputUrl.trim(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputUrl.isNotBlank()) {
                                onAddUrl(inputUrl.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        enabled = inputUrl.isNotBlank()
                    ) {
                        Text("যোগ করুন")
                    }
                }
            }
        }
    }
}
