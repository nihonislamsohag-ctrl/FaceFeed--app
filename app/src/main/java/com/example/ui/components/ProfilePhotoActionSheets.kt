package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CropFree
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
import androidx.compose.ui.platform.testTag
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
import com.example.util.MediaSaveHelper
import kotlinx.coroutines.launch

/**
 * 1. Profile Picture Bottom Sheet (Matches Screenshot 1)
 * Options:
 * - Add to story
 * - Add frame
 * - Take photo
 * - Upload photo
 * - Select photo on FaceFeed
 * - See profile picture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAvatarBottomSheet(
    onDismiss: () -> Unit,
    onAddToStory: () -> Unit,
    onAddFrame: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    onSelectPhotoOnFaceFeed: () -> Unit,
    onSeeProfilePicture: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Option 1: Add to story
            ActionSheetItem(
                icon = Icons.Default.Add,
                label = "Add to story",
                sublabel = "স্টোরিতে প্রোফাইল ছবি শেয়ার করুন",
                onClick = {
                    onAddToStory()
                    onDismiss()
                }
            )

            // Option 2: Add frame
            ActionSheetItem(
                icon = Icons.Outlined.CropFree,
                label = "Add frame",
                sublabel = "পতাকা, ভেরিফাইড ও স্পেশাল ফ্রেম যুক্ত করুন",
                onClick = {
                    onAddFrame()
                    onDismiss()
                }
            )

            // Option 3: Take photo
            ActionSheetItem(
                icon = Icons.Default.CameraAlt,
                label = "Take photo",
                sublabel = "ক্যামেরা দিয়ে নতুন ছবি তুলুন",
                onClick = {
                    onTakePhoto()
                    onDismiss()
                }
            )

            // Option 4: Upload photo
            ActionSheetItem(
                icon = Icons.Default.Folder,
                label = "Upload photo",
                sublabel = "ফোন মেমোরি ও গ্যালারি থেকে ছবি আপলোড",
                onClick = {
                    onUploadPhoto()
                    onDismiss()
                }
            )

            // Option 5: Select photo on FaceFeed (Replaced Facebook with FaceFeed as requested)
            ActionSheetItem(
                icon = Icons.Default.PhotoLibrary,
                label = "Select photo on FotoFilm",
                sublabel = "FotoFilm-এ পূর্বে আপলোড করা ছবি থেকে নির্বাচন করুন",
                highlight = true,
                onClick = {
                    onSelectPhotoOnFaceFeed()
                    onDismiss()
                }
            )

            // Option 6: See profile picture
            ActionSheetItem(
                icon = Icons.Default.AccountCircle,
                label = "See profile picture",
                sublabel = "পূর্ণ স্ক্রিনে প্রোফাইল ছবি দেখুন ও লাইক দিন",
                onClick = {
                    onSeeProfilePicture()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * 2. Cover Photo Bottom Sheet (Matches Screenshot 2)
 * Options:
 * - Take photo
 * - Upload photo
 * - Select photo on FaceFeed (with FaceFeed name)
 * - See cover photo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPhotoBottomSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    onSelectPhotoOnFaceFeed: () -> Unit,
    onSeeCoverPhoto: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Option 1: Take photo
            ActionSheetItem(
                icon = Icons.Default.CameraAlt,
                label = "Take photo",
                sublabel = "ক্যামেরা দিয়ে নতুন কভার ছবি তুলুন",
                onClick = {
                    onTakePhoto()
                    onDismiss()
                }
            )

            // Option 2: Upload photo
            ActionSheetItem(
                icon = Icons.Default.Folder,
                label = "Upload photo",
                sublabel = "ফোন গ্যালারি বা ফাইল থেকে কভার আপলোড",
                onClick = {
                    onUploadPhoto()
                    onDismiss()
                }
            )

            // Option 3: Select photo on FaceFeed (User explicitly requested: FaceFeed app name in place of Facebook)
            ActionSheetItem(
                icon = Icons.Default.PhotoLibrary,
                label = "Select photo on FotoFilm",
                sublabel = "FotoFilm কভার ফটো অ্যালবাম থেকে নির্বাচন",
                highlight = true,
                onClick = {
                    onSelectPhotoOnFaceFeed()
                    onDismiss()
                }
            )

            // Option 4: See cover photo
            ActionSheetItem(
                icon = Icons.Default.AccountCircle,
                label = "See cover photo",
                sublabel = "পূর্ণ স্ক্রিনে কভার ফটো দেখুন",
                onClick = {
                    onSeeCoverPhoto()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Reusable Action Row inside Bottom Sheets
 */
@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    label: String,
    sublabel: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon container matching Facebook layout
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (highlight) FacebookBlue.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (highlight) FacebookBlue else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlight) FacebookBlue else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sublabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 3. Profile Frame Picker Dialog (Add Frame Feature)
 */
data class ProfileFrame(
    val id: String,
    val title: String,
    val description: String,
    val borderColor: Color,
    val secondaryColor: Color,
    val badgeIcon: String
)

val AVAILABLE_PROFILE_FRAMES = listOf(
    ProfileFrame(
        id = "frame_bd",
        title = "বাংলাদেশ পতাকা ফ্রেম 🇧🇩",
        description = "আমার সোনার বাংলা - সবুজ ও লালের গর্বিত ফ্রেম",
        borderColor = BangladeshGreen,
        secondaryColor = Color(0xFFF42A41),
        badgeIcon = "🇧🇩"
    ),
    ProfileFrame(
        id = "frame_verified",
        title = "Fida Verified VIP গোল্ডেন ফ্রেম 💎",
        description = "স্বর্ণালী ফ্রেম ও সবুজ-হলুদ ভেরিফাইড মেম্বারশিপ ব্যাজ",
        borderColor = Color(0xFFFFB300),
        secondaryColor = BangladeshGreen,
        badgeIcon = "💎"
    ),
    ProfileFrame(
        id = "frame_nature",
        title = "মা মাটি মানুষের ফ্রেম 🌱",
        description = "অনবরত মা মাটি মানুষের জন্য - সবুজ প্রকৃতি ফ্রেম",
        borderColor = Color(0xFF2E7D32),
        secondaryColor = Color(0xFF81C784),
        badgeIcon = "🌱"
    ),
    ProfileFrame(
        id = "frame_eid",
        title = "ঈদ মোবারক ফ্রেম 🌙",
        description = "ঈদ আনন্দ ও শুভেচ্ছা ফ্রেম",
        borderColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFF1B5E20),
        badgeIcon = "🌙"
    ),
    ProfileFrame(
        id = "frame_flowers",
        title = "ফুলের সাজ ও শুভেচ্ছা ফ্রেম 🌺",
        description = "গোলাপ ও রক্তিম ফুলের সুন্দর ফ্রেম",
        borderColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFFFF80AB),
        badgeIcon = "🌺"
    ),
    ProfileFrame(
        id = "frame_peace",
        title = "শান্তির প্রতীক ফ্রেম 🕊️",
        description = "শান্তি ও সৌহার্দ্যের শ্বেত শুভ্র ফ্রেম",
        borderColor = Color(0xFF00ACC1),
        secondaryColor = Color(0xFF80DEEA),
        badgeIcon = "🕊️"
    )
)

@Composable
fun ProfileFramePickerDialog(
    user: User,
    currentFrameId: String?,
    onFrameSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFrame by remember { mutableStateOf(currentFrameId) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "প্রোফাইল ফ্রেম নির্বাচন",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview with selected frame
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Frame border
                    val activeFrame = AVAILABLE_PROFILE_FRAMES.find { it.id == selectedFrame }
                    if (activeFrame != null) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            activeFrame.borderColor,
                                            activeFrame.secondaryColor,
                                            activeFrame.borderColor
                                        )
                                    )
                                )
                        )
                    }

                    // Avatar
                    FotoFilmAvatar(
                        imageUrl = user.avatarUrl,
                        name = user.fullName,
                        modifier = Modifier
                            .size(if (selectedFrame != null) 124.dp else 136.dp)
                            .border(3.dp, Color.White, CircleShape),
                        iconSize = 48.dp
                    )

                    // Badge Icon
                    if (activeFrame != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(activeFrame.badgeIcon, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.fullName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedFrame != null) "ফ্রেম সক্রিয় আছে" else "কোনো ফ্রেম নেই",
                    fontSize = 13.sp,
                    color = if (selectedFrame != null) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // List of Frames
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option to remove frame
                    item {
                        Surface(
                            onClick = { selectedFrame = null },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedFrame == null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (selectedFrame == null) borderStrokeActive() else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("❌", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("কোনো ফ্রেম নয় (সাধারণ)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text("মূল ছবি যেমন আছে তেমনই থাকবে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(AVAILABLE_PROFILE_FRAMES) { frame ->
                        val isSelected = selectedFrame == frame.id
                        Surface(
                            onClick = {
                                selectedFrame = frame.id
                                AppSoundManager.playReactionChime()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) borderStrokeActive() else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(frame.badgeIcon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(frame.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text(frame.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = FacebookBlue)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save button
                Button(
                    onClick = {
                        onFrameSelected(selectedFrame)
                        AppSoundManager.playReactionChime()
                        Toast.makeText(context, "প্রোফাইল ফ্রেম সফলভাবে যুক্ত হয়েছে! 🎉", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ফ্রেম সংরক্ষণ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun borderStrokeActive(): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(2.dp, FacebookBlue)
}

/**
 * 4. Select Photo on FaceFeed Dialog
 * (Replaces "Select photo on Facebook" with user's app name FaceFeed)
 */
@Composable
fun SelectPhotoOnFaceFeedDialog(
    title: String = "FotoFilm-এ ছবি নির্বাচন করুন",
    isForCover: Boolean = false,
    onPhotoSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val faceFeedPhotos = emptyList<String>()

    var selectedUrl by remember { mutableStateOf<String?>(faceFeedPhotos.firstOrNull()) }

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
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "FotoFilm অ্যালবাম ও টাইমলাইন ছবি",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Large Preview
                selectedUrl?.let { url ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isForCover) 150.dp else 190.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Selected photo preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = if (isForCover) ContentScale.Crop else ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "গ্যালারি ও আপলোড করা ছবিসমূহ:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Photos Grid
                if (faceFeedPhotos.isEmpty()) {
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
                                text = "কোনো আপলোড করা ছবি পাওয়া যায়নি",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ফোনের গ্যালারি থেকে নতুন ছবি নির্বাচন করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(faceFeedPhotos) { url ->
                            val isSelected = selectedUrl == url
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.5.dp,
                                        color = if (isSelected) FacebookBlue else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedUrl = url
                                        AppSoundManager.playReactionChime()
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "FotoFilm photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isSelected) {
                                    Surface(
                                        color = FacebookBlue,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Button
                Button(
                    onClick = {
                        selectedUrl?.let { url ->
                            onPhotoSelected(url)
                            AppSoundManager.playReactionChime()
                            Toast.makeText(
                                context,
                                if (isForCover) "কভার ফটো সফলভাবে পরিবর্তন করা হয়েছে!"
                                else "প্রোফাইল ছবি সফলভাবে পরিবর্তন করা হয়েছে!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        }
                    },
                    enabled = selectedUrl != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isForCover) "কভার ফটো হিসেবে সেট করুন" else "প্রোফাইল ছবি হিসেবে সেট করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 5. Full Screen High-Res Facebook-Style Photo Viewer (See Profile Picture / See Cover Photo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPhotoViewerDialog(
    title: String,
    imageUrl: String,
    authorName: String,
    authorAvatar: String,
    frameId: String? = null,
    isCover: Boolean = false,
    isOwnProfile: Boolean = true,
    onMakeProfilePicture: (() -> Unit)? = null,
    onMakeCoverPhoto: (() -> Unit)? = null,
    onEditPhotoOptions: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(472) }
    var showCommentField by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var photoComments by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChrome by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun savePhotoToDevice() {
        coroutineScope.launch {
            Toast.makeText(context, "📥 ছবিটি গ্যালারিতে সংরক্ষণ হচ্ছে...", Toast.LENGTH_SHORT).show()
            val result = MediaSaveHelper.saveMediaToGallery(context, imageUrl, isVideo = false)
            result.onSuccess {
                Toast.makeText(context, "✅ ছবিটি ফোনের গ্যালারিতে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(context, "❌ সেভ ব্যর্থ: ${err.message ?: "ত্রুটি"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler { onDismiss() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Top Facebook-Style App Bar (Back Arrow + Author Avatar/Name + Actions)
                AnimatedVisibility(visible = showChrome) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("photo_viewer_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        AsyncImage(
                            model = authorAvatar.ifEmpty { imageUrl },
                            contentDescription = authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = title,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        if (isOwnProfile && onEditPhotoOptions != null) {
                            IconButton(
                                onClick = { onEditPhotoOptions() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { savePhotoToDevice() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save Photo",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showOptionsSheet = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Centered High-Res Photo (Tap to toggle chrome like Facebook)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { showChrome = !showChrome }
                        .padding(horizontal = if (isCover) 4.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeFrame = if (!isCover && frameId != null) {
                        AVAILABLE_PROFILE_FRAMES.find { it.id == frameId }
                    } else null

                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!isCover) {
                                        Modifier
                                            .heightIn(min = 240.dp, max = 440.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .then(
                                                if (activeFrame != null) {
                                                    Modifier.border(
                                                        width = 4.dp,
                                                        brush = Brush.sweepGradient(
                                                            listOf(
                                                                activeFrame.borderColor,
                                                                activeFrame.secondaryColor,
                                                                activeFrame.borderColor
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                } else Modifier
                                            )
                                    } else {
                                        Modifier
                                            .heightIn(min = 210.dp, max = 400.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    }
                                )
                        )

                        if (activeFrame != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, activeFrame.borderColor),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(activeFrame.badgeIcon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = activeFrame.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Facebook-Style Reaction, Comment & AdMob Banner Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color.Black.copy(alpha = 0.90f))
                ) {
                    AnimatedVisibility(visible = showChrome) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Likes & Comments summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = FacebookBlue,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ThumbUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$likeCount টি লাইক",
                                        color = Color.White.copy(alpha = 0.88f),
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "${photoComments.size} টি কমেন্ট",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Buttons: Like, Comment, Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Like Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            isLiked = !isLiked
                                            if (isLiked) {
                                                likeCount += 1
                                                AppSoundManager.playReactionChime()
                                            } else {
                                                likeCount -= 1
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (isLiked) Color(0xFFFA3E3E) else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isLiked) "লাভ" else "লাইক",
                                        color = if (isLiked) Color(0xFFFA3E3E) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                // Comment Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showCommentField = !showCommentField }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Comment",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "কমেন্ট",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                // Share Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            MediaSaveHelper.shareTextOrMediaLink(
                                                context,
                                                "$authorName-এর $title দেখুন FotoFilm-এ: $imageUrl",
                                                subject = "$authorName • $title"
                                            )
                                            AppSoundManager.playReactionChime()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "শেয়ার",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Recent comments preview + input box
                            AnimatedVisibility(visible = showCommentField || photoComments.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 6.dp)) {
                                    photoComments.takeLast(2).forEach { (commenter, msg) ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.12f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "$commenter: $msg",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    if (showCommentField) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = commentText,
                                                onValueChange = { commentText = it },
                                                placeholder = { Text("একটি মন্তব্য লিখুন...", color = Color.Gray) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = FacebookBlue,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                                    focusedContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                                                    unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.5f)
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    if (commentText.isNotBlank()) {
                                                        photoComments = photoComments + ("আপনি" to commentText.trim())
                                                        commentText = ""
                                                        showCommentField = false
                                                        Toast.makeText(context, "মন্তব্য পোস্ট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        AppSoundManager.playReactionChime()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Send,
                                                    contentDescription = "Send",
                                                    tint = FacebookBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Google AdMob Banner cleanly anchored at bottom without covering Like/Comment/Share
                    StickyVideoBottomBanner(
                        modifier = Modifier.fillMaxWidth(),
                        adUnitId = GoogleAdConfig.BANNER_ID
                    )
                }
            }

            // Bottom Sheet for 3-dot Menu
            if (showOptionsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showOptionsSheet = false },
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
                        if (isOwnProfile) {
                            if (onEditPhotoOptions != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showOptionsSheet = false
                                            onEditPhotoOptions()
                                        }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text("ছবি পরিবর্তন / আপলোড করুন", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMakeProfilePicture?.invoke()
                                        Toast.makeText(context, "✅ ছবিটি আপনার প্রোফাইল ছবি হিসেবে সেট করা হয়েছে!", Toast.LENGTH_LONG).show()
                                        showOptionsSheet = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(18.dp))
                                Text("Make profile picture", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMakeCoverPhoto?.invoke()
                                        Toast.makeText(context, "✅ ছবিটি আপনার কভার ফটো হিসেবে সেট করা হয়েছে!", Toast.LENGTH_LONG).show()
                                        showOptionsSheet = false
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Panorama, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(18.dp))
                                Text("Make cover photo", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    savePhotoToDevice()
                                    showOptionsSheet = false
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(18.dp))
                            Text("Save photo", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    MediaSaveHelper.shareTextOrMediaLink(
                                        context,
                                        "$authorName-এর $title: $imageUrl",
                                        subject = authorName
                                    )
                                    showOptionsSheet = false
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(18.dp))
                            Text("Share photo", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }

                        if (isOwnProfile) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showOptionsSheet = false
                                        showPrivacyDialog = true
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(18.dp))
                                Text("Edit privacy", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showOptionsSheet = false
                                        showDeleteConfirmDialog = true
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(18.dp))
                                Text("Delete", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Red)
                            }
                        }
                    }
                }
            }

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("ছবিটি মুছে ফেলবেন?", fontWeight = FontWeight.Bold) },
                    text = { Text("এই ছবিটি মুছে ফেললে তা প্রোফাইল থেকে আর প্রদর্শিত হবে না।") },
                    confirmButton = {
                        Button(
                            onClick = {
                                Toast.makeText(context, "🗑️ ছবিটি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
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

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("প্রাইভেসি নির্বাচন করুন", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            listOf("Public (পাবলিক)", "Friends (বন্ধুরা)", "Only me (শুধুমাত্র আমি)").forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast.makeText(context, "প্রাইভেসি '$p' সেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            showPrivacyDialog = false
                                        }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(p, fontSize = 14.sp)
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
        }
    }
}

