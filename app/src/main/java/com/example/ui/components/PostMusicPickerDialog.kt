package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.PostAudioTrack
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMusicPickerDialog(
    onSelectTrack: (PostAudioTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব গান") }
    var playingTrackId by remember { mutableStateOf<String?>(null) }

    val allCategories = listOf(
        "সব গান",
        "🎂 শুভ জন্মদিন",
        "গজল",
        "বাঁশির সুর",
        "জেমস ও আইয়ুব বাচ্চু",
        "ডিজে গান",
        "হিন্দি গান",
        "দেশাত্মবোধক",
        "লালনগীতি",
        "রবীন্দ্র সংগীত",
        "দুঃখের গান"
    )

    val filteredTracks = remember(searchQuery, selectedCategory) {
        SampleData.sampleAudioTracks.filter { track ->
            val matchesCategory = selectedCategory == "সব গান" || 
                    (selectedCategory == "🎂 শুভ জন্মদিন" && (track.category.contains("জন্মদিন") || track.category.contains("Birthday"))) ||
                    track.category.contains(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true) ||
                    track.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = {
            playingTrackId = null
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = FacebookBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "পোস্টে অডিও গান / সুর যুক্ত করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "গজল • বাঁশি • জেমস-বাচ্চু • ডিজে • রবীন্দ্র • লালন",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                playingTrackId = null
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("গান, গজল বা শিল্পীর নাম দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = FacebookBlue)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Horizontal Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BangladeshGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Birthday Emojis and wishes banner
                if (selectedCategory == "🎂 শুভ জন্মদিন" || selectedCategory == "সব গান") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎂", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "জন্মদিনের স্পেশাল ইমুজি কালেকশন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Text(
                                    text = "শুভ জন্মদিন 🎉",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SampleData.birthdayEmojis.forEach { emoji ->
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier.clickable {
                                            AppSoundManager.playReactionChime()
                                        }
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Track Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পাওয়া গেছে ${filteredTracks.size} টি গান ও সুর",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "100% কপিরাইট ফ্রি ও অনুমোদিত 🎵",
                        fontSize = 11.sp,
                        color = BangladeshGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Music Tracks List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(filteredTracks, key = { it.id }) { track ->
                        val isPlaying = playingTrackId == track.id
                        MusicTrackCard(
                            track = track,
                            isPlaying = isPlaying,
                            onTogglePlay = {
                                if (isPlaying) {
                                    playingTrackId = null
                                    com.example.util.AppSoundManager.pauseAudio()
                                } else {
                                    playingTrackId = track.id
                                    com.example.util.AppSoundManager.playAudio(track)
                                }
                            },
                            onSelect = {
                                playingTrackId = null
                                com.example.util.AppSoundManager.pauseAudio()
                                onSelectTrack(track)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTrackCard(
    track: PostAudioTrack,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) FacebookBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art / Category Icon with Play Button Overlay
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                if (!track.coverArtUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.coverArtUrl,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FacebookBlue)
                    )
                }

                // Play / Pause Icon badge
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) BangladeshGreen else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Track Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ${track.duration}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (track.category) {
                            "গজল" -> BangladeshGreen.copy(alpha = 0.2f)
                            "ডিজে গান" -> BangladeshRed.copy(alpha = 0.2f)
                            "বাঁশির সুর" -> Color(0xFF009688).copy(alpha = 0.2f)
                            "জেমস ও আইয়ুব বাচ্চু" -> FacebookBlue.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = track.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (track.category) {
                                "গজল" -> BangladeshGreen
                                "ডিজে গান" -> BangladeshRed
                                "বাঁশির সুর" -> Color(0xFF00796B)
                                "জেমস ও আইয়ুব বাচ্চু" -> FacebookBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(8.dp))
                        EqualizerAnimation()
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Select button
            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("+ যুক্ত করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EqualizerAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val height1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(height1.dp).background(BangladeshGreen, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(3.dp).height(height2.dp).background(FacebookBlue, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(3.dp).height(height3.dp).background(BangladeshGreen, RoundedCornerShape(1.dp)))
    }
}
