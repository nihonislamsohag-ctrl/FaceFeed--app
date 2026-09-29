package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.PostAudioTrack
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

@Composable
fun CreatePostDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()

    var postTitle by remember { mutableStateOf("") }
    var postText by remember { mutableStateOf("") }
    var hashtagInput by remember { mutableStateOf("") }
    var hashtagsList by remember { mutableStateOf(mutableListOf<String>()) }
    var selectedImages by remember { mutableStateOf(listOf<String>()) }
    var isVideoPost by remember { mutableStateOf(false) }
    var isReelPost by remember { mutableStateOf(false) }
    var selectedAudioTrack by remember { mutableStateOf<PostAudioTrack?>(null) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var showPhotoAlbumDialog by remember { mutableStateOf(false) }
    var showAddLinkDialog by remember { mutableStateOf(false) }

    // Read any pending images passed into CreatePost (e.g. from HomeScreen gallery launcher)
    val pendingImages by viewModel.pendingPostImages.collectAsState()
    LaunchedEffect(pendingImages) {
        if (pendingImages.isNotEmpty()) {
            selectedImages = (selectedImages + pendingImages).distinct().take(15)
            viewModel.clearPendingPostImages()
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
                                    viewModel.createPost(
                                        text = postText,
                                        hashtags = hashtagsList,
                                        images = if (selectedImages.isNotEmpty()) selectedImages else emptyList(),
                                        isVideo = isVideoPost,
                                        videoTitle = if (isVideoPost) (postTitle.ifBlank { postText }).take(30) else null,
                                        isReel = isReelPost,
                                        title = postTitle.takeIf { it.isNotBlank() },
                                        audioTrack = selectedAudioTrack
                                    )
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // User Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                            contentDescription = currentUser.fullName,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Public", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Post Title Input (Optional)
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        placeholder = { Text("পোস্টের টাইটেল / ক্যাপশন লিখুন (ঐচ্ছিক)", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Post Text Input
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text(strings.whatsOnYourMind, fontSize = 16.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

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
                            placeholder = { Text("#FaceFeed #Bangladesh", fontSize = 12.sp) },
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

                    // Selected Photos Grid Preview (up to 15 photos)
                    if (selectedImages.isNotEmpty()) {
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
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Attached Music Track Card
                    selectedAudioTrack?.let { track ->
                        Spacer(modifier = Modifier.height(8.dp))
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
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
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
                                IconButton(onClick = { selectedAudioTrack = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove music",
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                                text = "আসল মোবাইল ফোনে এই অ্যাপটি (APK) চালালে ফেসবুকের মতোই সাথে সাথে আপনার ফোনের মেমোরি কার্ড ও ক্যামেরার সমস্ত ছবি ওপেন হবে। এখন প্রিভিউতে আপনি ফোন গ্যালারি অথবা নিচের অ্যালবাম কালেকশন থেকেও ছবি বেছে নিতে পারেন।",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Direct Real Phone Gallery Picker Banner Button
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

                    // 2. In-App Photo Album Collection Button
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
                                    contentDescription = "Reel",
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

    // Music Picker Dialog
    if (showMusicPicker) {
        PostMusicPickerDialog(
            onSelectTrack = { track ->
                selectedAudioTrack = track
                showMusicPicker = false
            },
            onDismiss = { showMusicPicker = false }
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
        "সব ছবি" to listOf(
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1200",
            "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=1200",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1200",
            "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1200",
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=1200",
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=1200",
            "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=1200",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200"
        ),
        "ক্যামেরা অ্যালবাম" to listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1200",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=1200",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1200",
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=1200",
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=1200",
            "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=1200"
        ),
        "বাংলাদেশ ও প্রকৃতি" to listOf(
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200",
            "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200"
        )
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
