package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ActiveCallState
import com.example.model.CallType
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun CallingOverlay(
    activeCall: ActiveCallState,
    viewModel: FaceFeedViewModel
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)

    val minutes = activeCall.durationSeconds / 60
    val seconds = activeCall.durationSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)

    Dialog(
        onDismissRequest = { viewModel.endCall() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF10141E))
        ) {
            when (activeCall.callType) {
                CallType.AUDIO -> {
                    AudioCallContent(
                        activeCall = activeCall,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
                CallType.VIDEO -> {
                    VideoCallContent(
                        activeCall = activeCall,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
                CallType.GROUP -> {
                    GroupCallContent(
                        activeCall = activeCall,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AudioCallContent(
    activeCall: ActiveCallState,
    timerFormatted: String,
    strings: com.example.model.AppTranslations,
    viewModel: FaceFeedViewModel
) {
    // Pulse animation for audio call
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BangladeshGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = strings.audioCall,
                    color = BangladeshGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = activeCall.user?.fullName ?: "Friend",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (activeCall.durationSeconds > 0) timerFormatted else strings.calling,
                color = if (activeCall.durationSeconds > 0) Color(0xFF31A24C) else Color.LightGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Center Pulsing Avatar
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size((140 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(FacebookBlue.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, FacebookBlue, CircleShape)
            ) {
                AsyncImage(
                    model = activeCall.user?.avatarUrl ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                    contentDescription = activeCall.user?.fullName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Bottom Controls: Mute, End Call, Speaker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = strings.mute,
                    tint = Color.White
                )
            }

            // End Call Button
            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = strings.callEnded,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Speaker Button
            IconButton(
                onClick = { viewModel.toggleSpeaker() },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isSpeakerOn) FacebookBlue else Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (activeCall.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = strings.speaker,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun VideoCallContent(
    activeCall: ActiveCallState,
    timerFormatted: String,
    strings: com.example.model.AppTranslations,
    viewModel: FaceFeedViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Friend's Simulated Fullscreen Video Feed
        AsyncImage(
            model = activeCall.user?.coverUrl?.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200" }
                ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
            contentDescription = "Video Feed",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = activeCall.user?.fullName ?: "Friend",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timerFormatted,
                    color = Color(0xFF31A24C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BangladeshRed.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "LIVE HD",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Picture-in-Picture Self Camera Preview in Top-Right
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
                .size(width = 100.dp, height = 150.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
                    contentDescription = "My Camera",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flip Camera
            IconButton(
                onClick = { viewModel.toggleCameraFlip() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = strings.flipCamera, tint = Color.White)
            }

            // Mute Mic
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = strings.mute,
                    tint = Color.White
                )
            }

            // End Call
            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = strings.callEnded, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun GroupCallContent(
    activeCall: ActiveCallState,
    timerFormatted: String,
    strings: com.example.model.AppTranslations,
    viewModel: FaceFeedViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Group header
        Column(
            modifier = Modifier.padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activeCall.groupName ?: strings.groupCall,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$timerFormatted • ${activeCall.groupMembers.size} participants",
                color = Color(0xFF31A24C),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 4 Participant Video Tiles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCall.groupMembers.take(2).forEach { member ->
                    GroupParticipantTile(member = member, isSpeaking = true, modifier = Modifier.weight(1f))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCall.groupMembers.drop(2).take(2).forEach { member ->
                    GroupParticipantTile(member = member, isSpeaking = false, modifier = Modifier.weight(1f))
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = strings.mute, tint = Color.White)
            }

            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = strings.callEnded, tint = Color.White, modifier = Modifier.size(30.dp))
            }

            IconButton(
                onClick = { viewModel.toggleSpeaker() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isSpeakerOn) FacebookBlue else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = strings.speaker, tint = Color.White)
            }
        }
    }
}

@Composable
fun GroupParticipantTile(
    member: com.example.model.User,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
        border = if (isSpeaking) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF31A24C)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = member.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" },
                contentDescription = member.fullName,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Name & speaking indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = member.fullName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                if (isSpeaking) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Speaking",
                        tint = Color(0xFF31A24C),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
