package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.model.Story
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun StoryViewerDialog(
    story: Story,
    viewModel: FaceFeedViewModel
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    var replyText by remember { mutableStateOf("") }
    val isPlaying by AppSoundManager.isPlaying.collectAsState()

    DisposableEffect(story.id) {
        if (!story.musicTrack.isNullOrBlank()) {
            val matchingTrack = SampleData.sampleAudioTracks.firstOrNull { it.title == story.musicTrack }
                ?: PostAudioTrack(
                    id = "story_aud_${story.id}",
                    title = story.musicTrack,
                    artist = story.musicArtist ?: "বাংলা সুর",
                    category = if (story.musicTrack.contains("গজল")) "গজল" else if (story.musicTrack.contains("বাঁশি")) "বাঁশির সুর" else "গান",
                    duration = "0:30",
                    audioUrl = story.audioUrl
                )
            AppSoundManager.playAudio(matchingTrack)
        }
        onDispose {
            AppSoundManager.stopAudio()
        }
    }

    Dialog(
        onDismissRequest = {
            AppSoundManager.stopAudio()
            viewModel.closeStoryViewer()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler {
            AppSoundManager.stopAudio()
            viewModel.closeStoryViewer()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Fullscreen story image
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = story.author.fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top Gradient & Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Story Progress Bar
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = story.author.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                            contentDescription = story.author.fullName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = story.author.fullName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = story.timestamp,
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Add to Highlight Button
                        IconButton(onClick = {
                            val currentUser = viewModel.currentUser.value
                            viewModel.addStoryToHighlights(currentUser.id, story, "My Stories 🌟")
                            Toast.makeText(context, "স্টোরিটি আপনার প্রোফাইল হাইলাইটস এ যুক্ত হয়েছে! ✨", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Add to Highlights", tint = Color.White)
                        }

                        IconButton(onClick = { viewModel.closeStoryViewer() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Attached Music Badge in Story Viewer
                if (!story.musicTrack.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = BangladeshGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${story.musicTrack} • ${story.musicArtist ?: "বাংলা গান"}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        AppSoundManager.pauseAudio()
                                    } else {
                                        val matchingTrack = SampleData.sampleAudioTracks.firstOrNull { it.title == story.musicTrack }
                                            ?: PostAudioTrack(
                                                id = "story_aud_${story.id}",
                                                title = story.musicTrack,
                                                artist = story.musicArtist ?: "বাংলা সুর",
                                                category = if (story.musicTrack.contains("গজল")) "গজল" else if (story.musicTrack.contains("বাঁশি")) "বাঁশির সুর" else "গান",
                                                duration = "0:30",
                                                audioUrl = story.audioUrl
                                            )
                                        AppSoundManager.playAudio(matchingTrack)
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Play/Pause Story Music",
                                    tint = BangladeshGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Caption & Quick Reply Box
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                if (story.caption.isNotBlank()) {
                    Text(
                        text = story.caption,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("${strings.reply}...", color = Color.LightGray, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = FacebookBlue,
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.closeStoryViewer()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
