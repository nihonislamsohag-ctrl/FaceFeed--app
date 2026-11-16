package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.model.Translations
import com.example.model.User
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

/**
 * Facebook-style Story Tray (৪ কোনাইকা স্টোরি কার্ড + গোল প্রোফাইল আইকন)
 * 1. Story cards are 4-cornered rounded rectangles (৪ কোনাইকা কার্ড) exactly like Facebook.
 * 2. When someone uploads a story, their profile icon at top-left is perfectly round (গোল প্রোফাইল আইকন).
 * 3. Colorful ring (Facebook Blue/Cyan gradient) around the round profile icon for unviewed stories.
 * 4. Grey ring around the round profile icon after viewing.
 * 5. Position 1: 4-cornered "Create story" card with user's photo and round '+' button.
 * 6. Tapping opens full screen story view.
 */
@Composable
fun StoryTray(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Find if current user has an active story
    val myStory = stories.firstOrNull { it.author.id == currentUser.id }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(6.dp))

            // 1. Position 1: 4-cornered "Create Story" Card (৪ কোনাইকা কার্ড)
            CreateStoryCard(
                avatarUrl = currentUser.avatarUrl,
                hasActiveStory = myStory != null,
                label = strings.addStory,
                onAddStory = {
                    viewModel.openMediaPicker(MediaPickerTarget.STORY)
                },
                onViewMyStory = {
                    if (myStory != null) {
                        viewModel.openStoryViewer(myStory)
                    } else {
                        viewModel.openMediaPicker(MediaPickerTarget.STORY)
                    }
                }
            )

            // 2. Friends' 4-cornered Story Cards with Round Profile Icon at Top-Left
            val friendStories = stories.filter { it.author.id != currentUser.id }
            friendStories.forEach { story ->
                FriendStoryCard(
                    story = story,
                    onClick = {
                        viewModel.openStoryViewer(story)
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

/**
 * Position 1: Facebook-Style 4-Cornered "Create Story" Card (৪ কোনাইকা কার্ড)
 * - Top 68%: User's profile photo
 * - Overlap Center: Round '+' badge button (গোল প্লাস বাটন)
 * - Bottom 32%: "Create story" text
 */
@Composable
private fun CreateStoryCard(
    avatarUrl: String,
    hasActiveStory: Boolean,
    label: String,
    onAddStory: () -> Unit,
    onViewMyStory: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .height(172.dp)
            .clickable {
                if (hasActiveStory) onViewMyStory() else onAddStory()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top part: User's photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    AsyncImage(
                        model = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                        contentDescription = "My Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Bottom part: White / Surface background with label
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = if (label.isNotBlank()) label else "স্টোরি তৈরি করুন",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }
            }

            // Round '+' Button centered directly on the seam between photo and bottom card
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 28.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.5.dp)
                    .clickable { onAddStory() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(FacebookBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Story",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Friends' 4-Cornered Story Card (৪ কোনাইকা কার্ড):
 * - 4-cornered card with rounded corners (105dp x 172dp)
 * - Background: Story photo / video full bleed
 * - Top-Left: Round circular profile icon (গোল প্রোফাইল আইকন) with colorful / grey ring
 * - Bottom: Friend's name in white text with gradient shadow
 */
@Composable
private fun FriendStoryCard(
    story: Story,
    onClick: () -> Unit
) {
    val isUnseen = !story.isViewed

    Card(
        modifier = Modifier
            .width(105.dp)
            .height(172.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Full 4-cornered Story Background Media
            AsyncImage(
                model = story.mediaUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800" },
                contentDescription = story.caption.ifBlank { story.author.fullName },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2. Subtle Dark Gradient Overlays (Top for profile icon & bottom for user name)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // 3. Top-Left: Round Profile Icon (গোল প্রোফাইল আইকন)
            // Unseen stories get Facebook Blue/Cyan gradient ring, seen get Grey ring
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                val ringBrush = if (isUnseen) {
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF0866FF),
                            Color(0xFF00C6FF),
                            Color(0xFF0072FF),
                            Color(0xFF0866FF)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFB0B3B8),
                            Color(0xFFB0B3B8)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .border(
                            width = 2.5.dp,
                            brush = ringBrush,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = story.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300" },
                        contentDescription = story.author.fullName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 4. Top-Right: Music Note Badge if attached
            if (!story.musicTrack.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Attached",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // 5. Bottom: Author's Name in clean white text
            Text(
                text = story.author.fullName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}


/**
 * Facebook Messenger-Style Circular Stories Tray (মেসেঞ্জার বিতরে গোল ইস্টুরি)
 * Used specifically inside MessagesScreen.kt per user request:
 * 1. Stories are displayed in a clean horizontal row in perfect CIRCLE shape (মেসেঞ্জারে গোল ইস্টুরি).
 * 2. Position 1: My Story circle with circular avatar and blue '+' badge.
 * 3. Friends' stories: Profile photo inside perfect circle.
 * 4. Colorful ring (Facebook blue/cyan gradient) around unseen stories.
 * 5. Grey ring after seeing the story.
 * 6. Tapping opens full screen story view.
 */
@Composable
fun MessengerCircularStoryTray(
    viewModel: FaceFeedViewModel,
    onEditMyNote: () -> Unit = {},
    onFriendNoteClick: (User, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    val myStory = stories.firstOrNull { it.author.id == currentUser.id }
    val myNote = currentUser.note.takeIf { it.isNotBlank() && it != "Share a note..." }

    val friendStories = stories.filter { it.author.id != currentUser.id }
    val storyAuthorIds = friendStories.map { it.author.id }.toSet()
    // Additional non-group active friends with notes so every friend profile icon shows its Note text above
    val extraNotedFriends = conversations
        .asSequence()
        .filter { !it.isGroup && it.friend.id != currentUser.id && it.friend.id !in storyAuthorIds }
        .map { it.friend }
        .distinctBy { it.id }
        .take(5)
        .toList()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.width(6.dp))

            // 1. Position 1: My Profile Circle with Floating Note Bubble Above + '+' Story Badge
            MessengerMyStoryCircle(
                avatarUrl = currentUser.avatarUrl,
                hasActiveStory = myStory != null,
                isStoryViewed = myStory?.isViewed ?: false,
                myNoteText = myNote,
                label = if (strings.addStory.isNotBlank()) "Your note" else "আমার নোট",
                onEditNote = onEditMyNote,
                onAddStory = { viewModel.openMediaPicker(MediaPickerTarget.STORY) },
                onViewMyStory = {
                    if (myStory != null) viewModel.openStoryViewer(myStory)
                    else viewModel.openMediaPicker(MediaPickerTarget.STORY)
                }
            )

            // 2. Friends' Circular Stories with Floating Note Bubble Above Each Circle
            friendStories.forEach { story ->
                val noteText = story.author.note
                    .takeIf { it.isNotBlank() && it != "Share a note..." }
                    ?: story.caption.ifBlank { "হ্যালো বন্ধু! 😊" }
                MessengerFriendStoryCircle(
                    story = story,
                    noteText = noteText,
                    onStoryClick = { viewModel.openStoryViewer(story) },
                    onNoteClick = { onFriendNoteClick(story.author, noteText) }
                )
            }

            // 3. Extra Friends from ChatFilm with Floating Note Bubbles Above Their Circular Profile Icons
            extraNotedFriends.forEach { friend ->
                val noteText = friend.note
                    .takeIf { it.isNotBlank() && it != "Share a note..." }
                    ?: "অনলাইনে আছি 💬"
                MessengerNotedFriendCircle(
                    friend = friend,
                    noteText = noteText,
                    onAvatarClick = {
                        val conv = conversations.firstOrNull { !it.isGroup && it.friend.id == friend.id }
                        if (conv != null) viewModel.openChat(conv)
                    },
                    onNoteClick = { onFriendNoteClick(friend, noteText) }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

/**
 * Facebook Messenger Style Thought/Speech Note Bubble positioned directly over the top edge of a profile circle
 */
@Composable
private fun MessengerNoteBubble(
    text: String,
    isPlaceholder: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .widthIn(min = 56.dp, max = 82.dp)
            .clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ),
            shadowElevation = 3.dp
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium,
                color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
        // Signature two tiny speech-bubble dots connecting Note to Profile Circle
        Box(
            modifier = Modifier
                .offset(x = (-10).dp, y = (-2).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = (-6).dp, y = (-1).dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
        )
    }
}

@Composable
private fun MessengerMyStoryCircle(
    avatarUrl: String,
    hasActiveStory: Boolean,
    isStoryViewed: Boolean,
    myNoteText: String?,
    label: String,
    onEditNote: () -> Unit,
    onAddStory: () -> Unit,
    onViewMyStory: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Profile Circle pushed down so the Note bubble floats above it like Facebook Messenger
            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(62.dp)
                    .clickable { onViewMyStory() },
                contentAlignment = Alignment.Center
            ) {
                val ringModifier = if (hasActiveStory) {
                    if (!isStoryViewed) {
                        Modifier.border(
                            width = 2.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF0866FF),
                                    Color(0xFF00C6FF),
                                    Color(0xFF0072FF),
                                    Color(0xFF0866FF)
                                )
                            ),
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = 2.dp,
                            color = Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                    }
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color(0xFFE2E8F0),
                        shape = CircleShape
                    )
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .then(ringModifier)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400" },
                        contentDescription = "My Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Blue '+' Badge at bottom-right corner of circle
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = 1.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clickable { onAddStory() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Floating Note Bubble right above Profile Icon (tap to write/edit Note)
            MessengerNoteBubble(
                text = myNoteText ?: "নোট লিখুন...",
                isPlaceholder = myNoteText == null,
                onClick = onEditNote,
                modifier = Modifier.zIndex(2f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessengerFriendStoryCircle(
    story: Story,
    noteText: String,
    onStoryClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    val isUnseen = !story.isViewed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Circular Story Avatar below the Note Bubble
            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(62.dp)
                    .clickable { onStoryClick() },
                contentAlignment = Alignment.Center
            ) {
                val ringModifier = if (isUnseen) {
                    Modifier.border(
                        width = 2.8.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF0866FF),
                                Color(0xFF00C6FF),
                                Color(0xFF0072FF),
                                Color(0xFF0866FF)
                            )
                        ),
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFFCBD5E1),
                        shape = CircleShape
                    )
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .then(ringModifier)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = story.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400" },
                        contentDescription = story.author.fullName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                if (!story.musicTrack.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF137333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else if (story.author.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                    }
                }
            }

            // Floating Note Bubble right above Friend's Circular Avatar
            MessengerNoteBubble(
                text = noteText,
                isPlaceholder = false,
                onClick = onNoteClick,
                modifier = Modifier.zIndex(2f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val shortName = story.author.fullName.split(" ").firstOrNull() ?: story.author.fullName
        Text(
            text = shortName,
            fontSize = 11.sp,
            fontWeight = if (isUnseen) FontWeight.Bold else FontWeight.Normal,
            color = if (isUnseen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessengerNotedFriendCircle(
    friend: User,
    noteText: String,
    onAvatarClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(62.dp)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    AsyncImage(
                        model = friend.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400" },
                        contentDescription = friend.fullName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                    }
                }
            }

            MessengerNoteBubble(
                text = noteText,
                isPlaceholder = false,
                onClick = onNoteClick,
                modifier = Modifier.zIndex(2f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val shortName = friend.fullName.split(" ").firstOrNull() ?: friend.fullName
        Text(
            text = shortName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
