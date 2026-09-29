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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Story
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

@Composable
fun StoryTray(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.width(4.dp))

        // "Add Story" Card
        Card(
            modifier = Modifier
                .width(105.dp)
                .height(175.dp)
                .clickable {
                    viewModel.openMediaPicker(MediaPickerTarget.STORY)
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top half: user avatar
                    AsyncImage(
                        model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500" },
                        contentDescription = "My Avatar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.3f),
                        contentScale = ContentScale.Crop
                    )
                    // Bottom half: label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.addStory,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            maxLines = 2,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Floating Blue '+' Camera Button
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 12.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
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

        // Friends' Stories
        stories.filter { it.author.id != currentUser.id }.forEach { story ->
            FriendStoryCard(
                story = story,
                onClick = { viewModel.openStoryViewer(story) }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
fun FriendStoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .height(175.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Story background image
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = story.author.fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Author Avatar with gradient story ring
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(
                        2.5.dp,
                        Brush.sweepGradient(listOf(BangladeshGreen, BangladeshRed, FacebookBlue, BangladeshGreen)),
                        CircleShape
                    )
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = story.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                    contentDescription = story.author.fullName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Author Name at bottom
            Text(
                text = story.author.fullName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}
