package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.AgoraCallManager
import com.example.data.AgoraRtcState
import com.example.data.FirebaseManager
import com.example.model.ActiveCallState
import com.example.model.CallType
import com.example.model.Translations
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.util.LiveMicHelper
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay

@Composable
fun IncomingCallDialog(
    signal: FirebaseManager.IncomingCallSignal,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(signal.callId) {
        while (true) {
            AppSoundManager.playNotificationChime()
            AppSoundManager.triggerVibration(context)
            delay(2200)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "incoming_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Dialog(
        onDismissRequest = onDecline,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0A1424), Color(0xFF102418), Color(0xFF0A101B))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BangladeshGreen.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, BangladeshGreen)
                    ) {
                        Text(
                            text = if (signal.callType == CallType.VIDEO) "📹 ইনকামিং ভিডিও কল (Agora HD)" else "📞 ইনকামিং অডিও কল (Agora HD)",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = signal.callerName,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "রিং হচ্ছে...",
                        color = Color(0xFFCFD8DC),
                        fontSize = 15.sp
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size((150 * ringScale).dp)
                            .clip(CircleShape)
                            .background(BangladeshGreen.copy(alpha = 0.2f))
                    )
                    FotoFilmAvatar(
                        imageUrl = signal.callerAvatar,
                        name = signal.callerName,
                        modifier = Modifier
                            .size(134.dp)
                            .border(3.dp, BangladeshGreen, CircleShape),
                        iconSize = 54.dp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onDecline,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(BangladeshRed)
                                .testTag("decline_incoming_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "কেটে দিন",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("কেটে দিন", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onAccept,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(BangladeshGreen)
                                .testTag("accept_incoming_call_button")
                        ) {
                            Icon(
                                imageVector = if (signal.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = "গ্রহণ করুন",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("গ্রহণ করুন", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun CallingOverlay(
    activeCall: ActiveCallState,
    viewModel: FaceFeedViewModel
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val agoraState by AgoraCallManager.callState.collectAsState()
    val isSoundEnabled by viewModel.isNotificationSoundEnabled.collectAsState()
    val strings = Translations.get(currentLang)

    LaunchedEffect(Unit) {
        AppSoundManager.init(context)
    }

    val needsCamera = activeCall.callType != CallType.AUDIO
    val channelName = remember(activeCall.channelName, activeCall.callType, activeCall.user?.id, activeCall.groupName, currentUser.id) {
        if (activeCall.channelName.isNotBlank()) {
            activeCall.channelName
        } else if (activeCall.callType == CallType.GROUP) {
            AgoraCallManager.sanitizeChannelName("ff_grp_${activeCall.groupName ?: "chatfilm"}")
        } else {
            AgoraCallManager.buildDirectCallChannel(currentUser.id, activeCall.user?.id ?: "friend")
        }
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by remember(channelName, activeCall.callType) { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasAudioPermission = results[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = results[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(channelName, activeCall.callType, hasAudioPermission, hasCameraPermission) {
        val requiredPermissions = if (needsCamera) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        val missingAny = (!hasAudioPermission) || (needsCamera && !hasCameraPermission)
        if (missingAny && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(requiredPermissions)
        } else if (hasAudioPermission) {
            AgoraCallManager.joinCall(
                context = context,
                channelName = channelName,
                isVideoCall = needsCamera && hasCameraPermission,
                startMuted = activeCall.isMuted
            )
        }
    }

    val micLevel by LiveMicHelper.micLevel.collectAsState()
    val isHardwareMicActive by LiveMicHelper.isMicActive.collectAsState()
    val callMicOwnerTag = remember(channelName, activeCall.callId) { "call_${activeCall.callId.ifBlank { channelName }}" }

    LaunchedEffect(hasAudioPermission, activeCall.isMuted, agoraState.isJoinedChannel, callMicOwnerTag) {
        if (hasAudioPermission && !activeCall.isMuted && !agoraState.isJoinedChannel) {
            LiveMicHelper.startMonitoring(context, callMicOwnerTag)
        } else {
            LiveMicHelper.stopMonitoring(callMicOwnerTag)
        }
    }

    val isOutgoingRinging = when {
        activeCall.isIncoming -> false
        activeCall.callType == CallType.GROUP -> agoraState.remoteUids.isEmpty() && activeCall.durationSeconds < 6
        else -> !activeCall.isAccepted && agoraState.remoteUids.isEmpty() && activeCall.durationSeconds < 45
    }
    val shouldPlayDialTone = isSoundEnabled && isOutgoingRinging

    LaunchedEffect(shouldPlayDialTone, activeCall.callId) {
        if (shouldPlayDialTone) {
            AppSoundManager.startCallDialingTone()
        } else {
            AppSoundManager.stopCallDialingTone()
        }
    }

    DisposableEffect(callMicOwnerTag, activeCall.callId) {
        onDispose {
            AppSoundManager.stopCallDialingTone()
            LiveMicHelper.stopMonitoring(callMicOwnerTag)
            AgoraCallManager.leaveCall()
        }
    }

    val minutes = activeCall.durationSeconds / 60
    val seconds = activeCall.durationSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)
    val needsPermissionFix = !hasAudioPermission || (needsCamera && !hasCameraPermission)

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
                        agoraState = agoraState,
                        hasAudioPermission = hasAudioPermission,
                        isHardwareMicActive = isHardwareMicActive,
                        micLevel = micLevel,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
                CallType.VIDEO -> {
                    VideoCallContent(
                        activeCall = activeCall,
                        agoraState = agoraState,
                        hasAudioPermission = hasAudioPermission,
                        hasCameraPermission = hasCameraPermission,
                        isHardwareMicActive = isHardwareMicActive,
                        micLevel = micLevel,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
                CallType.GROUP -> {
                    GroupCallContent(
                        activeCall = activeCall,
                        agoraState = agoraState,
                        hasAudioPermission = hasAudioPermission,
                        hasCameraPermission = hasCameraPermission,
                        isHardwareMicActive = isHardwareMicActive,
                        micLevel = micLevel,
                        timerFormatted = timerFormatted,
                        strings = strings,
                        viewModel = viewModel
                    )
                }
            }

            if (needsPermissionFix && permissionRequested) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFB71C1C).copy(alpha = 0.94f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (needsCamera) "কল চালুর জন্য মাইক্রোফোন ও ক্যামেরা অনুমতি দিন" else "কল চালুর জন্য মাইক্রোফোন অনুমতি দিন",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    if (needsCamera) arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                                    else arrayOf(Manifest.permission.RECORD_AUDIO)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("অনুমতি দিন", color = BangladeshRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioCallContent(
    activeCall: ActiveCallState,
    agoraState: AgoraRtcState,
    hasAudioPermission: Boolean,
    isHardwareMicActive: Boolean,
    micLevel: Float,
    timerFormatted: String,
    strings: com.example.model.AppTranslations,
    viewModel: FaceFeedViewModel
) {
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

    val isRemoteJoined = agoraState.remoteUids.isNotEmpty() || activeCall.isAccepted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BangladeshGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${strings.audioCall} • Agora HD",
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
                text = if (isRemoteJoined && activeCall.durationSeconds > 0) timerFormatted else strings.calling,
                color = if (isRemoteJoined) Color(0xFF31A24C) else Color.LightGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val micStatusText = when {
                !hasAudioPermission -> "⚠️ মাইক্রোফোন অনুমতি প্রয়োজন"
                activeCall.isMuted -> "🔇 মাইক্রোফোন মিউট করা আছে"
                agoraState.isJoinedChannel || isHardwareMicActive -> "🎙️ আসল মাইক চালু • ভয়েস: ${(micLevel * 100).toInt().coerceAtLeast(12)}%"
                else -> "🎙️ ${agoraState.statusText}"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    !hasAudioPermission || activeCall.isMuted -> BangladeshRed.copy(alpha = 0.25f)
                    else -> BangladeshGreen.copy(alpha = 0.22f)
                }
            ) {
                Text(
                    text = micStatusText,
                    color = if (!hasAudioPermission || activeCall.isMuted) Color(0xFFFFCDD2) else Color(0xFFA5D6A7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

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
                FotoFilmAvatar(
                    imageUrl = activeCall.user?.avatarUrl,
                    name = activeCall.user?.fullName ?: "Friend",
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 52.dp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.15f))
                    .testTag("audio_call_mute_button")
            ) {
                Icon(
                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = strings.mute,
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
                    .testTag("audio_call_end_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = strings.callEnded,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = { viewModel.toggleSpeaker() },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isSpeakerOn) FacebookBlue else Color.White.copy(alpha = 0.15f))
                    .testTag("audio_call_speaker_button")
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
    agoraState: AgoraRtcState,
    hasAudioPermission: Boolean,
    hasCameraPermission: Boolean,
    isHardwareMicActive: Boolean,
    micLevel: Float,
    timerFormatted: String,
    strings: com.example.model.AppTranslations,
    viewModel: FaceFeedViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val remoteUid = agoraState.remoteUids.firstOrNull()
        if (agoraState.isSdkActive && remoteUid != null) {
            key(remoteUid) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).also { sv ->
                            AgoraCallManager.setupRemoteVideo(sv, remoteUid)
                        }
                    },
                    update = { sv ->
                        AgoraCallManager.setupRemoteVideo(sv, remoteUid)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            FotoFilmCoverBackground(
                imageUrl = activeCall.user?.coverUrl?.ifBlank { activeCall.user?.avatarUrl },
                contentDescription = "Video Feed",
                modifier = Modifier.fillMaxSize()
            )
        }

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
            val isRemoteJoined = agoraState.remoteUids.isNotEmpty() || activeCall.isAccepted
            Column {
                Text(
                    text = activeCall.user?.fullName ?: "Friend",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isRemoteJoined) timerFormatted else strings.calling} • ${if (!hasAudioPermission) "মাইক অনুমতি দিন" else if (activeCall.isMuted) "🔇 মিউট" else if (agoraState.isJoinedChannel || isHardwareMicActive) "🎙️ মাইক চালু" else agoraState.statusText}",
                    color = if (!hasAudioPermission || activeCall.isMuted) Color(0xFFFF8A80) else Color(0xFF81C784),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (agoraState.isJoinedChannel || (hasAudioPermission && !activeCall.isMuted)) BangladeshGreen.copy(alpha = 0.85f) else BangladeshRed.copy(alpha = 0.75f)
            ) {
                Text(
                    text = if (agoraState.isSdkActive) "AGORA HD" else if (hasAudioPermission && !activeCall.isMuted) "🎙️ MIC ON" else "LIVE HD",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
                .size(width = 104.dp, height = 156.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (agoraState.isSdkActive && hasCameraPermission) {
                    key(agoraState.isVideoActive) {
                        AndroidView(
                            factory = { ctx ->
                                SurfaceView(ctx).apply {
                                    setZOrderMediaOverlay(true)
                                    AgoraCallManager.setupLocalVideo(this)
                                }
                            },
                            update = { sv ->
                                AgoraCallManager.setupLocalVideo(sv)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    val selfUser = viewModel.currentUser.collectAsState().value
                    FotoFilmAvatar(
                        imageUrl = selfUser.avatarUrl,
                        name = selfUser.fullName,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 36.dp
                    )
                }
                Text(
                    text = "You",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

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
            IconButton(
                onClick = { viewModel.toggleCameraFlip() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .testTag("video_call_flip_button")
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = strings.flipCamera, tint = Color.White)
            }

            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.2f))
                    .testTag("video_call_mute_button")
            ) {
                Icon(
                    imageVector = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = strings.mute,
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.toggleSpeaker() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isSpeakerOn) FacebookBlue else Color.White.copy(alpha = 0.2f))
                    .testTag("video_call_speaker_button")
            ) {
                Icon(
                    imageVector = if (activeCall.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = strings.speaker,
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
                    .testTag("video_call_end_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = strings.callEnded, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun GroupCallContent(
    activeCall: ActiveCallState,
    agoraState: AgoraRtcState,
    hasAudioPermission: Boolean,
    hasCameraPermission: Boolean,
    isHardwareMicActive: Boolean,
    micLevel: Float,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
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
                text = "$timerFormatted • ${activeCall.groupMembers.size} participants • ${if (!hasAudioPermission) "মাইক অনুমতি প্রয়োজন" else if (activeCall.isMuted) "🔇 মাইক মিউট" else if (agoraState.isJoinedChannel || isHardwareMicActive) "🎙️ আসল মাইক চালু" else agoraState.statusText}",
                color = if (!hasAudioPermission || activeCall.isMuted) Color(0xFFFF8A80) else Color(0xFF81C784),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val remoteUids = agoraState.remoteUids
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCall.groupMembers.take(2).forEachIndexed { idx, member ->
                    GroupParticipantTile(
                        member = member,
                        isSpeaking = true,
                        remoteAgoraUid = remoteUids.getOrNull(idx),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCall.groupMembers.drop(2).take(2).forEachIndexed { idx, member ->
                    GroupParticipantTile(
                        member = member,
                        isSpeaking = false,
                        remoteAgoraUid = remoteUids.getOrNull(idx + 2),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleCameraFlip() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .testTag("group_call_flip_button")
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = strings.flipCamera, tint = Color.White)
            }

            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isMuted) BangladeshRed else Color.White.copy(alpha = 0.2f))
                    .testTag("group_call_mute_button")
            ) {
                Icon(if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = strings.mute, tint = Color.White)
            }

            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BangladeshRed)
                    .testTag("group_call_end_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = strings.callEnded, tint = Color.White, modifier = Modifier.size(30.dp))
            }

            IconButton(
                onClick = { viewModel.toggleSpeaker() },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (activeCall.isSpeakerOn) FacebookBlue else Color.White.copy(alpha = 0.2f))
                    .testTag("group_call_speaker_button")
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
fun GroupParticipantTile(
    member: com.example.model.User,
    isSpeaking: Boolean,
    remoteAgoraUid: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2433)),
        border = if (isSpeaking) BorderStroke(2.dp, Color(0xFF31A24C)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (remoteAgoraUid != null) {
                key(remoteAgoraUid) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).also { sv ->
                                AgoraCallManager.setupRemoteVideo(sv, remoteAgoraUid)
                            }
                        },
                        update = { sv ->
                            AgoraCallManager.setupRemoteVideo(sv, remoteAgoraUid)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                FotoFilmAvatar(
                    imageUrl = member.avatarUrl,
                    name = member.fullName,
                    modifier = Modifier.size(70.dp),
                    iconSize = 32.dp
                )
            }

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
                if (isSpeaking || remoteAgoraUid != null) {
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
