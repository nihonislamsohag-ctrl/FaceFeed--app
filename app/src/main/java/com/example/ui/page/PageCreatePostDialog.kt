package com.example.ui.page

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.PostAudioTrack
import com.example.model.SocialPage
import com.example.model.User
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.theme.BangladeshGreen
import com.example.viewmodel.FaceFeedViewModel

/**
 * Dedicated Page Post Creation Dialog
 * Requirement 2:
 * - Shows Page profile photo and Page name
 * - Title writing option on top: "Add Title"
 * - Options: Text, Photo, Video, Music, Hashtag, Location, Feeling
 * - When posting, author = Page name and Page photo (not personal ID)
 * - Post goes to Page Feed and main FotoFilm Feed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageCreatePostDialog(
    page: SocialPage,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    var postTitle by remember { mutableStateOf("") }
    var postText by remember { mutableStateOf("") }
    var hashtagInput by remember { mutableStateOf("") }
    val hashtagsList = remember { mutableStateListOf<String>() }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isVideoPost by remember { mutableStateOf(false) }
    var postLocation by remember { mutableStateOf<String?>(null) }
    var selectedFeeling by remember { mutableStateOf<String?>(null) }
    var selectedAudioTrack by remember { mutableStateOf<PostAudioTrack?>(null) }
    var showMusicDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showFeelingDialog by remember { mutableStateOf(false) }

    // Real Android gallery image picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages = (selectedImages + uris.map { it.toString() }).distinct().take(15)
        }
    }

    // Video picker
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImages = listOf(uri.toString())
            isVideoPost = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
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
                    // Top Bar: Back, "পেইজ থেকে নতুন পোস্ট", "পোস্ট করুন"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Text(
                            text = "পেইজ থেকে পোস্ট তৈরি করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Button(
                            onClick = {
                                if (postText.isNotBlank() || selectedImages.isNotEmpty() || postTitle.isNotBlank()) {
                                    val pageAuthor = User(
                                        id = "page_${page.id}",
                                        username = page.name.lowercase().replace(" ", "_"),
                                        fullName = page.name,
                                        avatarUrl = page.avatarUrl,
                                        isVerified = true
                                    )
                                    viewModel.createPost(
                                        text = postText,
                                        hashtags = hashtagsList.toList(),
                                        images = selectedImages,
                                        isVideo = isVideoPost,
                                        title = postTitle.takeIf { it.isNotBlank() },
                                        audioTrack = selectedAudioTrack,
                                        customAuthor = pageAuthor,
                                        location = postLocation,
                                        musicInfo = selectedFeeling?.let { "$it • ${selectedAudioTrack?.title ?: ""}" }
                                    )
                                    onPostCreated()
                                }
                            },
                            enabled = postText.isNotBlank() || selectedImages.isNotEmpty() || postTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("পোস্ট করুন", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Page Identity Row: Page Profile Photo + Page Name (Requirement 2)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(2.dp, BangladeshGreen),
                            modifier = Modifier.size(46.dp)
                        ) {
                            AsyncImage(
                                model = page.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                                contentDescription = page.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = page.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 15.dp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Public • পেইজ পোস্ট", fontSize = 11.sp)
                                    }
                                }
                                if (postLocation != null) {
                                    Text("📍 $postLocation", fontSize = 11.sp, color = BangladeshGreen)
                                }
                                if (selectedFeeling != null) {
                                    Text(selectedFeeling!!, fontSize = 11.sp, color = Color(0xFFE65100))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Option 1: Title writing option on top of every post: "Add Title"
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("পোস্টের শিরোনাম / Add Title") },
                        placeholder = { Text("✏️ লেখার টাইটেল লিখুন (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BangladeshGreen,
                            focusedLabelColor = BangladeshGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Post Text Area
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text("${page.name} পেইজের পক্ষ থেকে কিছু লিখুন...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BangladeshGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Options Grid: Photo, Video, Music, Location, Feeling (উপরে উঠানো হয়েছে)
                    Text(
                        text = "পেইজ পোস্ট অপশন সমূহ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Photo button (Phone Gallery)
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🖼️ ফোন গ্যালারি থেকে ফটো যোগ করুন", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Video button
                    OutlinedButton(
                        onClick = { videoLauncher.launch("video/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎥 ভিডিও যোগ করুন", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Music button
                    OutlinedButton(
                        onClick = { showMusicDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF7B1FA2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🎵 ব্যাকগ্রাউন্ড মিউজিক / অডিও যুক্ত করুন", fontWeight = FontWeight.Bold, color = Color(0xFF7B1FA2))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Location & Feeling Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLocationDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📍 লোকেশন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showFeelingDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Mood, contentDescription = null, tint = Color(0xFFF57C00))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("😊 অনুভূতি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Selected Images Preview
                    if (selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedImages.forEachIndexed { index, imgUri ->
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = imgUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(22.dp)
                                            .clickable {
                                                selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                                            }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.padding(3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Music Attached Preview
                    if (selectedAudioTrack != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF3E5F5),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF9C27B0))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "${selectedAudioTrack!!.title} - ${selectedAudioTrack!!.artist}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A148C)
                                    )
                                }
                                IconButton(onClick = { selectedAudioTrack = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove music", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hashtag Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hashtagInput,
                            onValueChange = { hashtagInput = it },
                            placeholder = { Text("#Hashtag (যেমন: #${page.name.replace(" ", "")})") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (hashtagInput.isNotBlank()) {
                                    val tag = if (hashtagInput.startsWith("#")) hashtagInput else "#$hashtagInput"
                                    if (!hashtagsList.contains(tag)) hashtagsList.add(tag)
                                    hashtagInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Add")
                        }
                    }

                    if (hashtagsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            hashtagsList.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BangladeshGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tag, fontSize = 12.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { hashtagsList.remove(tag) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Music Picker Dialog
    if (showMusicDialog) {
        val tracks = listOf(
            PostAudioTrack("m1", "Amar Sonar Bangla (Instrumental)", "Folk", "Bangla Traditional"),
            PostAudioTrack("m2", "Relaxing Lofi Beat", "Chill Vibes", "Acoustic"),
            PostAudioTrack("m3", "FotoFilm Viral Groove", "Studio Beats", "Pop"),
            PostAudioTrack("m4", "Islamic Nasheed & Hamd", "Sufi Beats", "Devotional")
        )
        AlertDialog(
            onDismissRequest = { showMusicDialog = false },
            title = { Text("অডিও / মিউজিক নির্বাচন করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tracks.forEach { track ->
                        Surface(
                            onClick = {
                                selectedAudioTrack = track
                                showMusicDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = BangladeshGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(track.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(track.artist, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMusicDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
        var locInput by remember { mutableStateOf(postLocation ?: "Dhaka, Bangladesh") }
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("লোকেশন যোগ করুন") },
            text = {
                OutlinedTextField(
                    value = locInput,
                    onValueChange = { locInput = it },
                    label = { Text("শহর বা স্থান") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    postLocation = locInput.takeIf { it.isNotBlank() }
                    showLocationDialog = false
                }) { Text("যোগ করুন") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Feeling Dialog
    if (showFeelingDialog) {
        val feelings = listOf(
            "😊 Feeling Happy", "❤️ Feeling Loved", "🎉 Feeling Excited",
            "🌟 Feeling Blessed", "😎 Feeling Cool", "☕ Enjoying Tea"
        )
        AlertDialog(
            onDismissRequest = { showFeelingDialog = false },
            title = { Text("অনুভূতি / Feeling নির্বাচন করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    feelings.forEach { f ->
                        Surface(
                            onClick = {
                                selectedFeeling = f
                                showFeelingDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(f, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFeelingDialog = false }) { Text("বাতিল") }
            }
        )
    }
}
