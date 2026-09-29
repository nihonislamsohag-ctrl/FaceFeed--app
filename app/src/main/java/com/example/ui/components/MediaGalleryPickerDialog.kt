package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.GalleryMediaItem
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

@Composable
fun MediaGalleryPickerDialog(
    viewModel: FaceFeedViewModel,
    initialTarget: MediaPickerTarget,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()

    var activeTarget by remember { mutableStateOf(if (initialTarget != MediaPickerTarget.NONE) initialTarget else MediaPickerTarget.FEED_PHOTO) }
    var selectedMediaUrl by remember { mutableStateOf<String?>(null) }
    var selectedMediaList by remember { mutableStateOf(listOf<String>()) }
    var postCaption by remember { mutableStateOf("") }
    var videoTitle by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableStateOf(0) }
    var showPhoneStudioDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    fun handlePickedMedia(uriStr: String) {
        selectedMediaUrl = uriStr
        when (activeTarget) {
            MediaPickerTarget.PROFILE_PHOTO -> {
                viewModel.updateProfileAvatar(uriStr)
                successMessage = "প্রোফাইল ছবি ফোন থেকে সফলভাবে পরিবর্তন করা হয়েছে!"
                showSuccessToast = true
            }
            MediaPickerTarget.COVER_PHOTO -> {
                viewModel.updateCoverPhoto(uriStr)
                successMessage = "কভার ফটো ফোন থেকে সফলভাবে পরিবর্তন করা হয়েছে!"
                showSuccessToast = true
            }
            MediaPickerTarget.STORY -> {
                viewModel.addStory(uriStr, postCaption.ifBlank { "FaceFeed Story ✨" })
                successMessage = "স্টোরি ফোন থেকে সফলভাবে আপলোড হয়েছে!"
                showSuccessToast = true
            }
            MediaPickerTarget.SHORT_VIDEO -> {
                selectedMediaUrl = uriStr
                successMessage = "শর্ট ভিডিও ফাইল সিলেক্ট হয়েছে! এবার পোস্ট করুন।"
                showSuccessToast = true
            }
            MediaPickerTarget.LONG_VIDEO -> {
                selectedMediaUrl = uriStr
                successMessage = "লোং ভিডিও ফাইল সিলেক্ট হয়েছে! এবার পোস্ট করুন।"
                showSuccessToast = true
            }
            else -> {
                selectedMediaList = (selectedMediaList + uriStr).take(10)
                successMessage = "ফাইল যোগ হয়েছে! মোট ${selectedMediaList.size} টি নির্বাচিত।"
                showSuccessToast = true
            }
        }
    }

    // 2. System File Manager & Storage Picker (Downloads, Files, SD card, Drive)
    val singleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) handlePickedMedia(uri.toString())
    }

    val multipleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedMediaList = (selectedMediaList + uris.map { it.toString() }).take(10)
            successMessage = "ফাইল ম্যানেজার থেকে ${uris.size} টি ফাইল যোগ হয়েছে!"
            showSuccessToast = true
        }
    }

    // Gradient presets for Text Post
    val textGradients = listOf(
        listOf(Color(0xFF1877F2), Color(0xFF00A3FF)), // Facebook Blue
        listOf(Color(0xFF006A4E), Color(0xFFF42A41)), // Bangladesh Flag Colors
        listOf(Color(0xFFFF512F), Color(0xFFDD2476)), // Sunset Red Pink
        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)), // Royal Purple
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // Emerald Green
        listOf(Color(0xFFFF8008), Color(0xFFFFC837)), // Warm Sunshine
        listOf(Color(0xFF232526), Color(0xFF414345)), // Modern Dark Graphite
        listOf(Color(0xFFFF0844), Color(0xFFFFB199))  // Crimson Peach
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Header with Prominent Back Button
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Clear Back Button to Exit
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back / বের হয়ে যান",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = when (activeTarget) {
                                        MediaPickerTarget.PROFILE_PHOTO -> "প্রোফাইল ফটো নির্বাচন"
                                        MediaPickerTarget.COVER_PHOTO -> "কভার ফটো নির্বাচন"
                                        MediaPickerTarget.LONG_VIDEO -> "লং ভিডিও আপলোড"
                                        MediaPickerTarget.SHORT_VIDEO -> "শর্ট ভিডিও / রিলস"
                                        MediaPickerTarget.STORY -> "স্টোরি তৈরি করুন"
                                        MediaPickerTarget.FEED_PHOTO -> "ছবি পোস্ট করুন"
                                        MediaPickerTarget.TEXT_POST -> "রঙিন স্ট্যাটাস / টেক্সট"
                                        MediaPickerTarget.NONE -> "মিডিয়া গ্যালারি"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ফোনের গ্যালারি অথবা অ্যাপ কালেকশন থেকে বেছে নিন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Direct Phone Pickers (Gallery & Files)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Gallery Picker Button
                            FilledTonalButton(
                                onClick = {
                                    showPhoneStudioDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = FacebookBlue.copy(alpha = 0.15f),
                                    contentColor = FacebookBlue
                                )
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("গ্যালারি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Storage / File Manager Picker Button
                            FilledTonalButton(
                                onClick = {
                                    if (activeTarget == MediaPickerTarget.FEED_PHOTO) {
                                        multipleFilePicker.launch("image/*")
                                    } else if (activeTarget == MediaPickerTarget.LONG_VIDEO || activeTarget == MediaPickerTarget.SHORT_VIDEO) {
                                        singleFilePicker.launch("video/*")
                                    } else {
                                        singleFilePicker.launch("*/*")
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BangladeshGreen.copy(alpha = 0.15f),
                                    contentColor = BangladeshGreen
                                )
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ফাইলস", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Feature Switcher Horizontal Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureTabChip(
                        title = "প্রোফাইল",
                        icon = Icons.Default.AccountCircle,
                        isSelected = activeTarget == MediaPickerTarget.PROFILE_PHOTO,
                        onClick = { activeTarget = MediaPickerTarget.PROFILE_PHOTO }
                    )
                    FeatureTabChip(
                        title = "কভার ফটো",
                        icon = Icons.Default.Panorama,
                        isSelected = activeTarget == MediaPickerTarget.COVER_PHOTO,
                        onClick = { activeTarget = MediaPickerTarget.COVER_PHOTO }
                    )
                    FeatureTabChip(
                        title = "ছবি",
                        icon = Icons.Default.PhotoLibrary,
                        isSelected = activeTarget == MediaPickerTarget.FEED_PHOTO,
                        onClick = { activeTarget = MediaPickerTarget.FEED_PHOTO }
                    )
                    FeatureTabChip(
                        title = "ইস্টুরি",
                        icon = Icons.Default.AutoStories,
                        isSelected = activeTarget == MediaPickerTarget.STORY,
                        onClick = { activeTarget = MediaPickerTarget.STORY }
                    )
                    FeatureTabChip(
                        title = "সট ভিডিও",
                        icon = Icons.Default.SlowMotionVideo,
                        isSelected = activeTarget == MediaPickerTarget.SHORT_VIDEO,
                        onClick = { activeTarget = MediaPickerTarget.SHORT_VIDEO }
                    )
                    FeatureTabChip(
                        title = "লোং ভিডিও",
                        icon = Icons.Default.OndemandVideo,
                        isSelected = activeTarget == MediaPickerTarget.LONG_VIDEO,
                        onClick = { activeTarget = MediaPickerTarget.LONG_VIDEO }
                    )
                    FeatureTabChip(
                        title = "টেস্ট লেখা",
                        icon = Icons.Default.TextFields,
                        isSelected = activeTarget == MediaPickerTarget.TEXT_POST,
                        onClick = { activeTarget = MediaPickerTarget.TEXT_POST }
                    )
                    FeatureTabChip(
                        title = "লাইভ ভিডিও",
                        icon = Icons.Default.Videocam,
                        isSelected = false,
                        onClick = {
                            onDismiss()
                            viewModel.openLiveStudio()
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Feedback Banner if updated
                AnimatedVisibility(visible = showSuccessToast) {
                    Surface(
                        color = BangladeshGreen,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(successMessage, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = {
                                showSuccessToast = false
                                onDismiss()
                            }) {
                                Text("ঠিক আছে", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Dynamic Body Content based on active target
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (activeTarget) {
                        MediaPickerTarget.TEXT_POST -> {
                            TextPostComposerContent(
                                textGradients = textGradients,
                                selectedGradientIndex = selectedGradientIndex,
                                onSelectGradient = { selectedGradientIndex = it },
                                postText = postCaption,
                                onTextChange = { postCaption = it },
                                onPublish = {
                                    viewModel.publishTextPost(postCaption, selectedGradientIndex)
                                    onDismiss()
                                },
                                onCancel = onDismiss
                            )
                        }

                        MediaPickerTarget.SHORT_VIDEO -> {
                            VideoUploadContent(
                                isShortVideo = true,
                                items = SampleData.galleryMediaItems.filter { it.category == "ShortVideo" },
                                selectedUrl = selectedMediaUrl,
                                onSelectUrl = { selectedMediaUrl = it },
                                title = videoTitle,
                                onTitleChange = { videoTitle = it },
                                onOpenPhoneGallery = {
                                    showPhoneStudioDialog = true
                                },
                                onOpenFilePicker = {
                                    singleFilePicker.launch("video/*")
                                },
                                onPublish = {
                                    val videoToPost = selectedMediaUrl ?: "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800"
                                    viewModel.publishReel(videoToPost, videoTitle.ifBlank { "FaceFeed Shorts 🇧🇩 #Reels" })
                                    onDismiss()
                                }
                            )
                        }

                        MediaPickerTarget.LONG_VIDEO -> {
                            VideoUploadContent(
                                isShortVideo = false,
                                items = SampleData.galleryMediaItems.filter { it.category == "LongVideo" },
                                selectedUrl = selectedMediaUrl,
                                onSelectUrl = { selectedMediaUrl = it },
                                title = videoTitle,
                                onTitleChange = { videoTitle = it },
                                onOpenPhoneGallery = {
                                    showPhoneStudioDialog = true
                                },
                                onOpenFilePicker = {
                                    singleFilePicker.launch("video/*")
                                },
                                onPublish = {
                                    val videoToPost = selectedMediaUrl ?: "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800"
                                    viewModel.publishLongVideo(
                                        title = videoTitle.ifBlank { "লোং ভিডিও: FaceFeed Watch ডকুমেন্টারি" },
                                        videoUrl = videoToPost,
                                        description = postCaption
                                    )
                                    onDismiss()
                                }
                            )
                        }

                        MediaPickerTarget.STORY -> {
                            StoryCreatorContent(
                                items = SampleData.galleryMediaItems,
                                selectedUrl = selectedMediaUrl,
                                onSelectUrl = { selectedMediaUrl = it },
                                caption = postCaption,
                                onCaptionChange = { postCaption = it },
                                onOpenPhoneGallery = {
                                    singleFilePicker.launch("image/*")
                                },
                                onOpenFilePicker = {
                                    singleFilePicker.launch("image/*")
                                },
                                onPublish = {
                                    val storyMedia = selectedMediaUrl ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
                                    viewModel.addStory(storyMedia, postCaption.ifBlank { "FaceFeed Story ✨" })
                                    onDismiss()
                                }
                            )
                        }

                        MediaPickerTarget.PROFILE_PHOTO -> {
                            GalleryGridSelector(
                                title = "প্রোফাইল ছবি সিলেক্ট করুন (ট্যাপ করলেই সেট হবে)",
                                items = SampleData.galleryMediaItems.filter { it.category == "Profile" || it.category == "Camera" },
                                onOpenPhoneGallery = {
                                    singleFilePicker.launch("image/*")
                                },
                                onOpenFilePicker = {
                                    singleFilePicker.launch("image/*")
                                },
                                onSelectItem = { item ->
                                    viewModel.updateProfileAvatar(item.url)
                                    successMessage = "প্রোফাইল ছবি '${item.title}' সেট করা হয়েছে!"
                                    showSuccessToast = true
                                }
                            )
                        }

                        MediaPickerTarget.COVER_PHOTO -> {
                            GalleryGridSelector(
                                title = "কভার ফটো সিলেক্ট করুন (ট্যাপ করলেই সেট হবে)",
                                items = SampleData.galleryMediaItems.filter { it.category == "Cover" || it.category == "Nature" || it.category == "Bangladesh" },
                                onOpenPhoneGallery = {
                                    singleFilePicker.launch("image/*")
                                },
                                onOpenFilePicker = {
                                    singleFilePicker.launch("image/*")
                                },
                                onSelectItem = { item ->
                                    viewModel.updateCoverPhoto(item.url)
                                    successMessage = "কভার ফটো '${item.title}' সফলভাবে আপডেট করা হয়েছে!"
                                    showSuccessToast = true
                                }
                            )
                        }

                        else -> { // FEED_PHOTO
                            FeedPhotoGalleryContent(
                                items = SampleData.galleryMediaItems.filter { !it.isVideo },
                                selectedList = selectedMediaList,
                                onToggleSelect = { url ->
                                    selectedMediaList = if (selectedMediaList.contains(url)) {
                                        selectedMediaList - url
                                    } else {
                                        (selectedMediaList + url).take(10)
                                    }
                                },
                                caption = postCaption,
                                onCaptionChange = { postCaption = it },
                                onOpenPhoneGallery = {
                                    multipleFilePicker.launch("image/*")
                                },
                                onOpenFilePicker = {
                                    multipleFilePicker.launch("image/*")
                                },
                                onPublish = {
                                    viewModel.createPost(
                                        text = postCaption.ifBlank { "FaceFeed এ নতুন পোস্ট 📸" },
                                        hashtags = listOf("#FaceFeed", "#GalleryPhotos", "#Bangladesh"),
                                        images = selectedMediaList,
                                        isVideo = false
                                    )
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Universal In-App Phone Gallery & Camera Studio
        if (showPhoneStudioDialog) {
            PhoneMediaGalleryDialog(
                title = when (activeTarget) {
                    MediaPickerTarget.PROFILE_PHOTO -> "প্রোফাইল ছবি গ্যালারি কালেকশন"
                    MediaPickerTarget.COVER_PHOTO -> "কভার ফটো গ্যালারি কালেকশন"
                    MediaPickerTarget.SHORT_VIDEO -> "সট ভিডিও ও রিলস কালেকশন"
                    MediaPickerTarget.LONG_VIDEO -> "লোং ভিডিও কালেকশন"
                    MediaPickerTarget.STORY -> "স্টোরি মিডিয়া কালেকশন"
                    else -> "ফোন ক্যামেরা ও গ্যালারি কালেকশন"
                },
                allowMultiple = (activeTarget == MediaPickerTarget.FEED_PHOTO),
                isVideoOnly = (activeTarget == MediaPickerTarget.SHORT_VIDEO || activeTarget == MediaPickerTarget.LONG_VIDEO),
                onMediaSelected = { urls ->
                    urls.forEach { url -> handlePickedMedia(url) }
                },
                onLaunchSystemFilePicker = {
                    if (activeTarget == MediaPickerTarget.FEED_PHOTO) {
                        multipleFilePicker.launch("image/*")
                    } else if (activeTarget == MediaPickerTarget.LONG_VIDEO || activeTarget == MediaPickerTarget.SHORT_VIDEO) {
                        singleFilePicker.launch("video/*")
                    } else {
                        singleFilePicker.launch("*/*")
                    }
                },
                onDismiss = { showPhoneStudioDialog = false }
            )
        }
    }
}

@Composable
private fun FeatureTabChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun TextPostComposerContent(
    textGradients: List<List<Color>>,
    selectedGradientIndex: Int,
    onSelectGradient: (Int) -> Unit,
    postText: String,
    onTextChange: (String) -> Unit,
    onPublish: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "রঙিন ব্যাকগ্রাউন্ডে টেক্সট স্ট্যাটাস লিখুন",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Facebook Style Colored Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(textGradients[selectedGradientIndex])
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = postText,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            "আপনার মনের কথা লিখুন...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("ব্যাকগ্রাউন্ড কালার সিলেক্ট করুন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal color palette picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                textGradients.forEachIndexed { index, gradient ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradient))
                            .clickable { onSelectGradient(index) }
                            .then(
                                if (selectedGradientIndex == index) {
                                    Modifier.border(3.dp, Color.White, CircleShape)
                                } else Modifier
                            )
                    )
                }
            }
        }

        // Bottom Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("বাতিল / বের হন")
            }

            Button(
                onClick = onPublish,
                enabled = postText.isNotBlank(),
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("পোস্ট করুন 🚀", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VideoUploadContent(
    isShortVideo: Boolean,
    items: List<GalleryMediaItem>,
    selectedUrl: String?,
    onSelectUrl: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    onOpenPhoneGallery: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Dual options to open phone storage / gallery directly
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gallery Picker
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenPhoneGallery() },
                colors = CardDefaults.cardColors(containerColor = FacebookBlue.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isShortVideo) "ফোন গ্যালারি (রিলস)" else "ফোন গ্যালারি (ভিডিও)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // File Manager Picker
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenFilePicker() },
                colors = CardDefaults.cardColors(containerColor = BangladeshGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isShortVideo) "ফোন ফাইলস (মেমোরি)" else "ফোন ফাইলস (ডাউনলোড)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isShortVideo) "অথবা অ্যাপ সট ভিডিও গ্যালারি থেকে বেছে নিন:" else "অথবা অ্যাপ লোং ভিডিও কালেকশন থেকে বেছে নিন:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Video selection grid
        items.forEach { videoItem ->
            val isSelected = selectedUrl == videoItem.url
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectUrl(videoItem.url) }
                    .then(
                        if (isSelected) Modifier.border(2.dp, FacebookBlue, RoundedCornerShape(12.dp)) else Modifier
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = videoItem.url,
                            contentDescription = videoItem.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        videoItem.duration?.let { dur ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                            ) {
                                Text(dur, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(videoItem.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (isShortVideo) "রিলস ভিডিও • FaceFeed Shorts" else "এইচডি ভিডিও • FaceFeed Watch", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectUrl(videoItem.url) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text(if (isShortVideo) "ক্যাপশন ও হ্যাশট্যাগ (#Reels)..." else "ভিডিওর টাইটেল ও শিরোনাম...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isShortVideo) BangladeshRed else FacebookBlue)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isShortVideo) "শর্ট ভিডিও / রিলস শেয়ার করুন" else "লোং ভিডিও পাবলিশ করুন", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StoryCreatorContent(
    items: List<GalleryMediaItem>,
    selectedUrl: String?,
    onSelectUrl: (String) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onOpenPhoneGallery: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Dual phone gallery and file storage buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenPhoneGallery,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("গ্যালারি", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = onOpenFilePicker,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = BangladeshGreen.copy(alpha = 0.2f), contentColor = BangladeshGreen)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ফোন ফাইলস", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("অথবা অ্যাপের দৃষ্টিনন্দন ব্যাকগ্রাউন্ড বেছে নিন:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal story preview cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.filter { !it.isVideo }.forEach { item ->
                val isSelected = selectedUrl == item.url
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .height(160.dp)
                        .clickable { onSelectUrl(item.url) }
                        .then(
                            if (isSelected) Modifier.border(3.dp, FacebookBlue, RoundedCornerShape(12.dp)) else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = caption,
            onValueChange = onCaptionChange,
            placeholder = { Text("স্টোরিতে ক্যাপশন বা টেক্সট লিখুন (ঐচ্ছিক)...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("স্টোরিতে আপলোড করুন ✨", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GalleryGridSelector(
    title: String,
    items: List<GalleryMediaItem>,
    onOpenPhoneGallery: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onSelectItem: (GalleryMediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onOpenPhoneGallery, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("গ্যালারি", fontSize = 11.sp)
                }

                TextButton(onClick = onOpenFilePicker, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("ফাইলস", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { mediaItem ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelectItem(mediaItem) },
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = mediaItem.url,
                            contentDescription = mediaItem.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                text = mediaItem.title,
                                color = Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedPhotoGalleryContent(
    items: List<GalleryMediaItem>,
    selectedList: List<String>,
    onToggleSelect: (String) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onOpenPhoneGallery: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${selectedList.size}/10 টি ছবি নির্বাচিত",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = FacebookBlue
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = onOpenPhoneGallery,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("গ্যালারি", fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = onOpenFilePicker,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = BangladeshGreen.copy(alpha = 0.15f), contentColor = BangladeshGreen)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("ফাইলস", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = caption,
            onValueChange = onCaptionChange,
            placeholder = { Text("এই ছবির সাথে কিছু লিখুন...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(items) { item ->
                val isSelected = selectedList.contains(item.url)
                val selectedIndex = selectedList.indexOf(item.url) + 1

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onToggleSelect(item.url) }
                        .then(
                            if (isSelected) Modifier.border(2.5.dp, FacebookBlue, RoundedCornerShape(8.dp)) else Modifier
                        ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$selectedIndex", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
        ) {
            Icon(Icons.Default.PostAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ছবি পোস্ট করুন (${selectedList.size.coerceAtLeast(1)})", fontWeight = FontWeight.Bold)
        }
    }
}
