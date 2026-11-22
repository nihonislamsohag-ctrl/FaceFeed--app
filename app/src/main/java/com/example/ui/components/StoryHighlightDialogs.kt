package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.model.StoryHighlight
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ফুলস্ক্রিন হাইলাইটস স্টোরি ভিউয়ার (Highlight Story Viewer Dialog)
 * ফেসবুকের আসল স্টোরি হাইলাইটস এর মতো সম্পূর্ণ অভিজ্ঞতা:
 * - অটো প্রগ্রেস বার প্রতি স্টোরির জন্য
 * - বাঁয়ে চাপ দিলে আগের স্টোরি, ডানে চাপ দিলে পরের স্টোরি
 * - চেপে ধরে রাখলে পজ (Pause)
 * - রিঅ্যাকশন ও রিয়্যাক্ট বার
 * - ডাউনলোড ও হাইলাইটস পরিচালনা
 */
@Composable
fun HighlightStoryViewerDialog(
    highlight: StoryHighlight,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stories = remember(highlight) {
        if (highlight.stories.isNotEmpty()) highlight.stories
        else listOf(
            Story(
                id = "${highlight.id}_fallback",
                author = viewModel.currentUser.value,
                mediaUrl = highlight.coverUrl,
                timestamp = highlight.createdAt,
                caption = highlight.title
            )
        )
    }

    var currentStoryIndex by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var storyProgress by remember { mutableStateOf(0f) }
    var showReactionPop by remember { mutableStateOf<String?>(null) }

    val currentStory = stories.getOrElse(currentStoryIndex) { stories.first() }

    // Progress bar auto advance
    LaunchedEffect(currentStoryIndex, isPaused) {
        if (isPaused) return@LaunchedEffect
        storyProgress = 0f
        val stepMs = 50L
        val totalMs = 4500L
        val increment = stepMs.toFloat() / totalMs

        while (storyProgress < 1f && !isPaused) {
            delay(stepMs)
            storyProgress = (storyProgress + increment).coerceAtMost(1f)
        }

        if (storyProgress >= 1f && !isPaused) {
            if (currentStoryIndex < stories.size - 1) {
                currentStoryIndex++
            } else {
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Story Media Image
            AsyncImage(
                model = currentStory.mediaUrl,
                contentDescription = currentStory.caption.ifEmpty { highlight.title },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = { offset ->
                                val screenWidth = size.width
                                if (offset.x < screenWidth * 0.35f) {
                                    // Previous story
                                    if (currentStoryIndex > 0) {
                                        currentStoryIndex--
                                        storyProgress = 0f
                                    }
                                } else {
                                    // Next story
                                    if (currentStoryIndex < stories.size - 1) {
                                        currentStoryIndex++
                                        storyProgress = 0f
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    },
                contentScale = ContentScale.Crop
            )

            // Top gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
            )

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Top Bar: Story Progress Bars + Author Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 12.dp, end = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Segmented Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        val fillFraction = when {
                            index < currentStoryIndex -> 1f
                            index == currentStoryIndex -> storyProgress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fillFraction)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Highlight title & Author info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Highlight Cover Icon
                        FotoFilmAvatar(
                            imageUrl = highlight.coverUrl.ifEmpty { currentStory.author.avatarUrl },
                            name = highlight.title,
                            modifier = Modifier
                                .size(38.dp)
                                .border(1.5.dp, FacebookBlue, CircleShape),
                            iconSize = 18.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = highlight.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FacebookBlue.copy(alpha = 0.8f)
                                ) {
                                    Text(
                                        text = "Highlight",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${currentStory.author.fullName} • ${currentStory.timestamp}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Download Story
                        IconButton(onClick = {
                            coroutineScope.launch {
                                Toast.makeText(context, "স্টোরিটি ডিভাইসে সেভ করা হয়েছে! 📥", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                        }

                        // Close Dialog
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }

            // Caption display if exists
            if (currentStory.caption.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = currentStory.caption,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // Reaction Popup Animation
            showReactionPop?.let { emoji ->
                Text(
                    text = emoji,
                    fontSize = 72.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Bottom Reactions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick emoji reactions
                val reactions = listOf("❤️", "🔥", "😂", "😮", "👏", "🇧🇩")
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reactions.forEach { emoji ->
                        Surface(
                            onClick = {
                                AppSoundManager.playReactionChime()
                                showReactionPop = emoji
                                Toast.makeText(context, "$emoji রিঅ্যাকশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    delay(900)
                                    showReactionPop = null
                                }
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Add to highlight button
                FilledTonalButton(
                    onClick = {
                        Toast.makeText(context, "এই স্টোরিটি আপনার হাইলাইটস এ সংরক্ষিত আছে ✨", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdded, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সংরক্ষিত", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * নতুন হাইলাইটস তৈরির ডায়ালগ (Create New Highlight Dialog)
 * ইউজার যেকোনো নাম ও কাভার দিয়ে নতুন অ্যালবাম তৈরি করতে পারবে
 */
@Composable
fun CreateHighlightDialog(
    user: User,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allAvailableStories by viewModel.stories.collectAsState()
    var highlightTitle by remember { mutableStateOf("") }
    var selectedCoverUrl by remember { mutableStateOf(user.avatarUrl) }
    var selectedStoryIds by remember { mutableStateOf(setOf<String>()) }

    // Cover options strictly from user's own photo & active stories
    val coverOptions = remember(user.avatarUrl, allAvailableStories) {
        (listOfNotNull(user.avatarUrl.takeIf { it.isNotBlank() }) +
            allAvailableStories.mapNotNull { it.mediaUrl.takeIf { url -> url.isNotBlank() } })
            .distinct()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন হাইলাইটস তৈরি করুন ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cover Preview Circle
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(FacebookBlue, Color(0xFF00C6FF), Color(0xFFE91E63), FacebookBlue)
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FotoFilmCoverBackground(
                        imageUrl = selectedCoverUrl,
                        contentDescription = "Cover Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "কাভার ছবি নির্বাচন করুন:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (coverOptions.isEmpty()) {
                    Text(
                        text = "আপনার প্রোফাইল ছবি বা স্টোরি যুক্ত হলে এখানে কভার হিসেবে দেখাবে।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(coverOptions) { url ->
                            val isSelected = selectedCoverUrl == url
                            AsyncImage(
                                model = url,
                                contentDescription = "Cover option",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.5.dp else 1.dp,
                                            if (isSelected) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        CircleShape
                                    )
                                    .clickable { selectedCoverUrl = url }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title input
                OutlinedTextField(
                    value = highlightTitle,
                    onValueChange = { highlightTitle = it },
                    label = { Text("হাইলাইটস এর নাম (Title)") },
                    placeholder = { Text("যেমন: My Stories 🌟, Travel ✈️, Memories ❤️") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selectable Stories Archive
                Text(
                    text = "স্টোরি নির্বাচন করুন (হাইলাইটে জমা হবে এবং পরেও দেখা যাবে):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allAvailableStories) { st ->
                        val isStorySelected = st.id in selectedStoryIds
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(105.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    BorderStroke(
                                        if (isStorySelected) 2.5.dp else 1.dp,
                                        if (isStorySelected) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedStoryIds = if (isStorySelected) {
                                        selectedStoryIds - st.id
                                    } else {
                                        selectedStoryIds + st.id
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = st.mediaUrl,
                                contentDescription = st.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isStorySelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(FacebookBlue.copy(alpha = 0.35f))
                                )
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "💡 এই হাইলাইটে স্টোরিগুলো জমা থাকবে এবং যেকোনো সময় পরেও দেখা যাবে!",
                    fontSize = 11.sp,
                    color = FacebookBlue,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            val finalTitle = highlightTitle.ifBlank { "My Stories 🌟" }
                            val chosenStories = allAvailableStories.filter { it.id in selectedStoryIds }
                            val finalStories = if (chosenStories.isNotEmpty()) {
                                chosenStories
                            } else {
                                listOf(
                                    Story(
                                        id = "st_${System.currentTimeMillis()}",
                                        author = user,
                                        mediaUrl = selectedCoverUrl,
                                        timestamp = "আজকে",
                                        caption = finalTitle
                                    )
                                )
                            }
                            viewModel.createNewHighlight(
                                userId = user.id,
                                title = finalTitle,
                                coverUrl = if (chosenStories.isNotEmpty()) chosenStories.first().mediaUrl else selectedCoverUrl,
                                stories = finalStories
                            )
                            AppSoundManager.playReactionChime()
                            Toast.makeText(context, "'$finalTitle' হাইলাইটস যুক্ত হয়েছে! 🎉", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("যোগ করুন (Save)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
