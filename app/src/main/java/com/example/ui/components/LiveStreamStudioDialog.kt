package com.example.ui.components

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.LiveCommentItem
import com.example.model.ReactionType
import com.example.model.SocialAd
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import java.util.UUID

enum class LiveStudioStage {
    PRE_LIVE,        // Screenshot 1: Prepare live, Go Live button, description, vertical toolbar
    ACTIVE_BROADCAST,// Screenshot 2: LIVE badge with timer, live comments, reaction bar, Finish button
    ENDED_SUMMARY    // Screenshot 3: Broadcast ended, Monetization card, 30 days warning, Boost, Reel, Done
}

enum class LivePrivacy {
    PUBLIC, FRIENDS, ONLY_ME
}

/**
 * Facebook Live Studio with:
 * 1. Pre-Live Setup (Screenshot 1)
 * 2. Active Live Broadcast (Screenshot 2)
 * 3. Broadcast Ended Summary (Screenshot 3)
 * 4. Front & Rear camera switching (সামনের ক্যামেরা ও পিছনের ক্যামেরা)
 * 5. Full support for Portrait (লাম্বা) and Landscape (দিগলা) modes ("যেকোনো ফোন লাম্বা ও দিগলা করে দুনোটা দিয়ে লাইভ চালানো যাবে")
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveStreamStudioDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val configuration = LocalConfiguration.current

    // Studio Stage
    var currentStage by remember { mutableStateOf(LiveStudioStage.PRE_LIVE) }

    // Orientation state: Portrait (লাম্বা) vs Landscape (দিগলা)
    var isManualLandscape by remember { mutableStateOf(false) }
    val isLandscape = isManualLandscape || (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    // Camera states (Front vs Rear Camera / সামনের ও পিছনের ক্যামেরা)
    var isFrontCamera by remember { mutableStateOf(false) } // Default rear camera matching screenshot 1
    var isFlashOn by remember { mutableStateOf(false) }
    var isMicMuted by remember { mutableStateOf(false) }
    var isEnhanceOn by remember { mutableStateOf(false) }
    var isToolbarExpanded by remember { mutableStateOf(true) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showStickersDialog by remember { mutableStateOf(false) }

    // Privacy & Audience
    var livePrivacy by remember { mutableStateOf(LivePrivacy.PUBLIC) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var isShareToStoryOn by remember { mutableStateOf(true) }

    // Title & Description
    var liveDescription by remember { mutableStateOf("") }
    var liveTitle by remember { mutableStateOf("🔴 সবার সাথে সরাসরি আড্ডা ও গানবাজনা ✨") }
    var liveHashtags by remember { mutableStateOf(listOf("#FaceFeed", "#Live", "#Bangladesh", "#NiyonIslamSohag", "#Viral")) }
    var showEditDescriptionDialog by remember { mutableStateOf(false) }

    // Timer & Viewers
    var liveSeconds by remember { mutableIntStateOf(18) } // Matching Screenshot 2: 0:18
    var isLiveTimerRunning by remember { mutableStateOf(false) }
    var viewerCount by remember { mutableIntStateOf(5840) }
    var recentJoinNotice by remember { mutableStateOf("🎉 তানভীর হাসান, রিয়া আক্তার ও আরও ১,২৫০ জন লাইভে যুক্ত হয়েছেন!") }

    // 5 Social Advertisements
    var activeLiveAd by remember { mutableStateOf<SocialAd?>(null) }
    var activeLiveAdSlotTitle by remember { mutableStateOf("") }
    var showAdsSelectorDialog by remember { mutableStateOf(false) }
    var shownLiveAdIndices by remember { mutableStateOf(setOf<Int>()) }

    // Google AdMob Live Mid-Roll Ads (at 3 min / 180s and 8 min / 480s)
    var showGoogleLiveMidAd by remember { mutableStateOf(false) }
    var triggeredLiveAdSlots by remember { mutableStateOf(setOf<Int>()) }

    // Live Comments & Reactions
    val liveComments = remember { mutableStateListOf<LiveCommentItem>() }
    val floatingHearts = remember { mutableStateListOf<Long>() }
    var hostCommentInput by remember { mutableStateOf("") }

    // Ended screen states (Screenshot 3)
    var showReminderSentToast by remember { mutableStateOf(false) }
    var isDismissedPaymentCard by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEndedMenuOptions by remember { mutableStateOf(false) }

    // Initialize initial comments
    LaunchedEffect(Unit) {
        liveComments.addAll(SampleData.sampleLiveComments)
    }

    // Active Live timer & viewer counter
    LaunchedEffect(isLiveTimerRunning) {
        if (isLiveTimerRunning) {
            val joiners = listOf(
                "🎉 তানভীর হাসান, রিয়া আক্তার ও আরও ১,২৫০ জন লাইভে যুক্ত হয়েছেন!",
                "🔥 সাকিব আল হাসান ও আরও ৩,৪০০ জন দর্শক লাইভ দেখছেন!",
                "✨ ঢাকা ও চট্টগ্রাম থেকে আরও ২,১০০ জন লাইভে জয়েন করেছেন!",
                "👏 নাজমুল, সুমাইয়া ও আরও ৯৮০ জন লাইভে লাইক দিয়েছেন!"
            )
            var noticeIndex = 0
            while (isLiveTimerRunning) {
                delay(1000)
                liveSeconds++
                viewerCount += (-5..25).random()
                if (liveSeconds % 12 == 0) {
                    recentJoinNotice = joiners[noticeIndex % joiners.size]
                    noticeIndex++
                }

                // Google AdMob Ads at 3 min (180s) and 8 min (480s)
                if (liveSeconds >= 180 && !triggeredLiveAdSlots.contains(180)) {
                    triggeredLiveAdSlots = triggeredLiveAdSlots + 180
                    showGoogleLiveMidAd = true
                } else if (liveSeconds >= 480 && !triggeredLiveAdSlots.contains(480)) {
                    triggeredLiveAdSlots = triggeredLiveAdSlots + 480
                    showGoogleLiveMidAd = true
                }

                // Social advertisements scheduling
                if (activeLiveAd == null && !showGoogleLiveMidAd) {
                    if (liveSeconds >= 35 && !shownLiveAdIndices.contains(0)) {
                        shownLiveAdIndices = shownLiveAdIndices + 0
                        activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (১/৫): রক্তদান কর্মসূচি"
                        activeLiveAd = SampleData.sampleSocialAds.getOrNull(0)
                    } else if (liveSeconds >= 70 && !shownLiveAdIndices.contains(1)) {
                        shownLiveAdIndices = shownLiveAdIndices + 1
                        activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (২/৫): বৃক্ষরোপণ অভিযান"
                        activeLiveAd = SampleData.sampleSocialAds.getOrNull(1)
                    } else if (liveSeconds >= 105 && !shownLiveAdIndices.contains(2)) {
                        shownLiveAdIndices = shownLiveAdIndices + 2
                        activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (৩/৫): পথশিশু শিক্ষা তহবিল"
                        activeLiveAd = SampleData.sampleSocialAds.getOrNull(2)
                    }
                }
            }
        }
    }

    // Extra incoming comments during live
    LaunchedEffect(isLiveTimerRunning) {
        if (isLiveTimerRunning) {
            val extraComments = listOf(
                LiveCommentItem(UUID.randomUUID().toString(), "Kamrul Hasan", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "ভাইয়া সাউন্ড একদম পরিষ্কার ও ক্লিয়ার 👏"),
                LiveCommentItem(UUID.randomUUID().toString(), "Fatema Khatun", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "মাশাআল্লাহ, অনেক সুন্দর লাইভ ❤️"),
                LiveCommentItem(UUID.randomUUID().toString(), "Biplob Kumar", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200", "লাইভ লিঙ্ক ফেসবুক ও মেসেঞ্জারে শেয়ার করলাম ভাই 🔥"),
                LiveCommentItem(UUID.randomUUID().toString(), "Nusrat Jahan", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "কেমন আছেন ভাইয়া? সিলেট থেকে দেখছি 🇧🇩"),
                LiveCommentItem(UUID.randomUUID().toString(), "Arif Hossain", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "হাজার হাজার মানুষ লাইভ দেখছে! দারুন 🤩"),
                LiveCommentItem(UUID.randomUUID().toString(), "Sohag Fan Club", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200", "Niyon Islam Sohag ভাই এগিয়ে যান ❤️")
            )
            for (item in extraComments) {
                delay(3000)
                if (isLiveTimerRunning) {
                    liveComments.add(item)
                    floatingHearts.add(System.currentTimeMillis())
                }
            }
        }
    }

    val hours = liveSeconds / 3600
    val minutes = (liveSeconds % 3600) / 60
    val secs = liveSeconds % 60
    val timeFormatted = if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, secs) else String.format("%02d:%02d", minutes, secs)

    // Camera feed image simulation
    val cameraBackgroundModel = remember(isFrontCamera) {
        if (isFrontCamera) {
            currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1000" }
        } else {
            // Rear camera feed: lush green trees, backyard outdoor scene from user screenshot
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200"
        }
    }

    Dialog(
        onDismissRequest = {
            if (currentStage == LiveStudioStage.ACTIVE_BROADCAST) {
                isLiveTimerRunning = false
                currentStage = LiveStudioStage.ENDED_SUMMARY
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 1. Camera Stream Background (Supports Portrait & Landscape seamlessly)
            AsyncImage(
                model = cameraBackgroundModel,
                contentDescription = "Camera Feed",
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isFlashOn) Modifier.background(Color.White.copy(alpha = 0.25f)) else Modifier
                    ),
                contentScale = ContentScale.Crop
            )

            // Enhancement overlay if active
            if (isEnhanceOn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFE0B2).copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
            }

            // Dark gradient overlay for top & bottom controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // =========================================================================
            // SWITCH BETWEEN STAGES
            // =========================================================================
            when (currentStage) {
                LiveStudioStage.PRE_LIVE -> {
                    // =================================================================
                    // SCREENSHOT 1: PRE-LIVE PREPARATION SCREEN
                    // =================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        // Top Bar matching Screenshot 1: Back, Avatar, Name, Public dropdown, Help, Orientation toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Back arrow
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Profile Avatar
                                AsyncImage(
                                    model = currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
                                    contentDescription = currentUser.fullName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = currentUser.fullName.ifBlank { "Niyon Islam Sohag" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )

                                    // Privacy Dropdown Pill: 🌐 Public ▼
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { showPrivacyDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (livePrivacy) {
                                                    LivePrivacy.PUBLIC -> "🌐 Public"
                                                    LivePrivacy.FRIENDS -> "👥 Friends"
                                                    LivePrivacy.ONLY_ME -> "🔒 Only Me"
                                                },
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Top: Orientation Toggle (লাম্বা / দিগলা) + Help icon '?'
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Manual Orientation Switcher (যেকোনো ফোন লাম্বা ও দিগলা করে দুনোটা দিয়ে লাইভ চালানো যাবে)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isLandscape) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable {
                                        isManualLandscape = !isManualLandscape
                                        val modeName = if (!isManualLandscape) "লাম্বা (Portrait)" else "দিগলা (Landscape)"
                                        Toast.makeText(context, "লাইভ ভিউ: $modeName মোড", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isLandscape) Icons.Default.ScreenRotation else Icons.Default.StayCurrentPortrait,
                                            contentDescription = "Orientation",
                                            tint = if (isLandscape) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isLandscape) "দিগলা" else "লাম্বা",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLandscape) Color.Black else Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Help '?' icon
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Facebook Live: সরাসরি দর্শকদের সাথে সংযোগ স্থাপন করুন", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Help",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Middle Body: Camera View with Right Vertical Toolbar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Right Vertical Control Toolbar matching Screenshot 1
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 14.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AnimatedVisibility(visible = isToolbarExpanded) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // 1. Flash off / on
                                        LiveToolButton(
                                            label = if (isFlashOn) "Flash on" else "Flash off",
                                            icon = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                            isActive = isFlashOn,
                                            onClick = {
                                                isFlashOn = !isFlashOn
                                                Toast.makeText(context, if (isFlashOn) "ফ্ল্যাশ অন" else "ফ্ল্যাশ অফ", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        // 2. Rotate (Camera switch front / back)
                                        LiveToolButton(
                                            label = if (isFrontCamera) "Front cam" else "Rotate",
                                            icon = Icons.Default.Cameraswitch,
                                            isActive = false,
                                            onClick = {
                                                isFrontCamera = !isFrontCamera
                                                Toast.makeText(
                                                    context,
                                                    if (isFrontCamera) "সামনের ক্যামেরা চালু হয়েছে 🤳" else "পিছনের ক্যামেরা চালু হয়েছে 📷",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )

                                        // 3. Stickers
                                        LiveToolButton(
                                            label = "Stickers",
                                            icon = Icons.Default.Mood,
                                            isActive = selectedFilterIndex > 0,
                                            onClick = { showStickersDialog = true }
                                        )

                                        // 4. Mute mic
                                        LiveToolButton(
                                            label = if (isMicMuted) "Muted" else "Mute mic",
                                            icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                            isActive = isMicMuted,
                                            onClick = {
                                                isMicMuted = !isMicMuted
                                                Toast.makeText(context, if (isMicMuted) "মাইক মিউট করা হয়েছে" else "মাইক চালু", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        // 5. Enhance off / on
                                        LiveToolButton(
                                            label = if (isEnhanceOn) "Enhance on" else "Enhance off",
                                            icon = Icons.Default.AutoFixHigh,
                                            isActive = isEnhanceOn,
                                            onClick = {
                                                isEnhanceOn = !isEnhanceOn
                                                Toast.makeText(context, if (isEnhanceOn) "বিউটি ফিল্টার চালু" else "ফিল্টার বন্ধ", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }

                                // Collapse / Expand Arrow Button
                                IconButton(
                                    onClick = { isToolbarExpanded = !isToolbarExpanded },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = if (isToolbarExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Toolbar",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Bottom Section matching Screenshot 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            // "Tap to add a description..."
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEditDescriptionDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (liveDescription.isNotBlank()) liveDescription else "Tap to add a description...",
                                        color = if (liveDescription.isNotBlank()) Color.White else Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp,
                                        fontWeight = if (liveDescription.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Share to Story toggle: Share to > Story: On >
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isShareToStoryOn = !isShareToStoryOn
                                        Toast.makeText(context, if (isShareToStoryOn) "স্টোরিতে শেয়ার চালু" else "স্টোরিতে শেয়ার বন্ধ", Toast.LENGTH_SHORT).show()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Reply, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Share to", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(if (isShareToStoryOn) "Story: On" else "Story: Off", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Blue Big "Go live" Button
                            Button(
                                onClick = {
                                    liveSeconds = 1
                                    isLiveTimerRunning = true
                                    currentStage = LiveStudioStage.ACTIVE_BROADCAST
                                    Toast.makeText(context, "লাইভ ব্রডকাস্ট শুরু হয়েছে! 🔴", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Go live",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom 5 Icons: Megaphone, Coins, Add Guest, Handshake, Menu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showAdsSelectorDialog = true }) {
                                    Icon(Icons.Default.Campaign, contentDescription = "Promote / Ad", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    Toast.makeText(context, "স্টার ও মনিটাইজেশন সক্রিয় আছে ⭐", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = "Stars / Coins", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    Toast.makeText(context, "লাইভে কো-হোস্ট বা বন্ধুদের যুক্ত করার ইনভাইট পাঠান", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.PersonAddAlt1, contentDescription = "Add Guest", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    Toast.makeText(context, "ব্র্যান্ড পার্টনারশিপ ও কলাবোরেশন", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Handshake, contentDescription = "Collab", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    Toast.makeText(context, "লাইভ সেটিংস মেনু", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                LiveStudioStage.ACTIVE_BROADCAST -> {
                    // =================================================================
                    // SCREENSHOT 2: ACTIVE LIVE BROADCAST SCREEN
                    // =================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        // Top Bar matching Screenshot 2: Red LIVE 0:18 badge, Public indicator, Orientation toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Red LIVE badge with timer (LIVE 0:18)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE53935)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = timeFormatted,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Privacy Indicator: 🌐 Public
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🌐 Public", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Viewers
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (viewerCount >= 1000) String.format("%.1fK", viewerCount / 1000.0) else "$viewerCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Orientation toggle (লাম্বা / দিগলা)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLandscape) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable {
                                    isManualLandscape = !isManualLandscape
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isLandscape) Icons.Default.ScreenRotation else Icons.Default.StayCurrentPortrait,
                                        contentDescription = "Orientation",
                                        tint = if (isLandscape) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isLandscape) "দিগলা" else "লাম্বা",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLandscape) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        // Middle Viewport: Live feed with right overlay controls
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Right side buttons matching Screenshot 2: Flash, Rotate, Stickers, Collapse
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IconButton(
                                    onClick = {
                                        isFlashOn = !isFlashOn
                                        Toast.makeText(context, if (isFlashOn) "ফ্ল্যাশ অন" else "ফ্ল্যাশ অফ", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isFlashOn) Color.Yellow.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "Flash",
                                        tint = if (isFlashOn) Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Rotate Camera: Front ↔ Rear (সামনের ও পিছনের ক্যামেরা)
                                IconButton(
                                    onClick = {
                                        isFrontCamera = !isFrontCamera
                                        Toast.makeText(
                                            context,
                                            if (isFrontCamera) "সামনের ক্যামেরা চালু হয়েছে 🤳" else "পিছনের ক্যামেরা চালু হয়েছে 📷",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Rotate",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { showStickersDialog = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Mood, contentDescription = "Stickers", tint = Color.White, modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "কন্ট্রোল মিনিমাইজ", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse", tint = Color.White)
                                }
                            }

                            // Floating hearts animation
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 60.dp, end = 20.dp)
                            ) {
                                floatingHearts.takeLast(5).forEach { _ ->
                                    Text("❤️", fontSize = 26.sp)
                                }
                            }
                        }

                        // Bottom Section matching Screenshot 2: Live Comments + Action icons + Finish button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            // Live incoming comments scrolling
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(if (isLandscape) 0.6f else 0.85f)
                                    .heightIn(max = 140.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(liveComments.takeLast(4)) { comment ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Black.copy(alpha = 0.55f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = comment.avatarUrl,
                                                contentDescription = comment.authorName,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${comment.authorName}: ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = comment.comment,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // "Comments will appear here" placeholder / text input
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextField(
                                        value = hostCommentInput,
                                        onValueChange = { hostCommentInput = it },
                                        placeholder = { Text("Comments will appear here", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = Color.White
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (hostCommentInput.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                liveComments.add(
                                                    LiveCommentItem(UUID.randomUUID().toString(), currentUser.fullName, currentUser.avatarUrl, hostCommentInput)
                                                )
                                                hostCommentInput = ""
                                                floatingHearts.add(System.currentTimeMillis())
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = FacebookBlue, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Action Icons matching Screenshot 2: Link, Equalizer, Q&A, Stars, Co-host, Share, Capture, FINISH
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Group of toolbar icons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // 1. Link icon
                                    IconButton(
                                        onClick = { Toast.makeText(context, "লাইভ লিংক কপি হয়েছে! 🔗", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = "Link", tint = Color.White, modifier = Modifier.size(19.dp))
                                    }

                                    // 2. Sound Equalizer
                                    IconButton(
                                        onClick = {
                                            AppSoundManager.playReactionSound(ReactionType.LOVE)
                                            Toast.makeText(context, "সাউন্ড ইফেক্ট চালু 🎵", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Equalizer, contentDescription = "Sound", tint = Color.White, modifier = Modifier.size(19.dp))
                                    }

                                    // 3. Q&A
                                    IconButton(
                                        onClick = { Toast.makeText(context, "দর্শকদের প্রশ্নোত্তর সেশন চালু ❓", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.QuestionAnswer, contentDescription = "Q&A", tint = Color.White, modifier = Modifier.size(19.dp))
                                    }

                                    // 4. Star
                                    IconButton(
                                        onClick = {
                                            floatingHearts.add(System.currentTimeMillis())
                                            Toast.makeText(context, "স্টার ও শুভেচ্ছা গৃহীত হয়েছে ⭐", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.StarBorder, contentDescription = "Star", tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                                    }

                                    // 5. Co-host / Guests
                                    IconButton(
                                        onClick = { Toast.makeText(context, "লাইভে দর্শক ও বন্ধুদের যুক্ত করুন", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.GroupAdd, contentDescription = "Co-host", tint = Color.White, modifier = Modifier.size(19.dp))
                                    }

                                    // 6. Share
                                    IconButton(
                                        onClick = { Toast.makeText(context, "লাইভ শেয়ার মেনু ওপেন হয়েছে", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }

                                    // 7. Snapshot Capture Circle
                                    IconButton(
                                        onClick = { Toast.makeText(context, "লাইভ থেকে ছবি ক্যাপচার হয়েছে 📸", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Finish Button (Red / White Button matching Screenshot 2)
                                Button(
                                    onClick = {
                                        isLiveTimerRunning = false
                                        currentStage = LiveStudioStage.ENDED_SUMMARY
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Finish",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                LiveStudioStage.ENDED_SUMMARY -> {
                    // =================================================================
                    // SCREENSHOT 3: BROADCAST ENDED SUMMARY SCREEN
                    // =================================================================
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            color = Color(0xFF1E1E24),
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Top Drag Handle
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.Gray)
                                        .align(Alignment.CenterHorizontally)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Header: "Your live broadcast has ended" + 3-dots menu
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Your live broadcast has ended",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Box {
                                        IconButton(
                                            onClick = { showEndedMenuOptions = true },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = Color.White)
                                        }

                                        DropdownMenu(
                                            expanded = showEndedMenuOptions,
                                            onDismissRequest = { showEndedMenuOptions = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Download video to device") },
                                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                                onClick = {
                                                    showEndedMenuOptions = false
                                                    Toast.makeText(context, "ভিডিও ডিভাইসে ডাউনলোড শুরু হয়েছে 📥", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Copy link") },
                                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                                onClick = {
                                                    showEndedMenuOptions = false
                                                    Toast.makeText(context, "লাইভ ভিডিওর লিংক কপি হয়েছে", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete broadcast", color = Color.Red) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                                onClick = {
                                                    showEndedMenuOptions = false
                                                    showDeleteConfirmDialog = true
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // CARD 1: Star Monetization Card matching Screenshot 3
                                if (!isDismissedPaymentCard) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF2C2C34),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    // Yellow Star Icon
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFFFFB300),
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column {
                                                        Text(
                                                            text = "Enter payment info to start earning",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Complete payment info on a computer to get paid for Stars you receive from fans. Send a reminder to your Page inbox.",
                                                            fontSize = 12.sp,
                                                            color = Color.LightGray,
                                                            lineHeight = 16.sp
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { isDismissedPaymentCard = true },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // "Send reminder" button
                                            Button(
                                                onClick = {
                                                    showReminderSentToast = true
                                                    Toast.makeText(context, "রিমাইন্ডার পাঠানো হয়েছে আপনার ইনবক্সে!", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D4263)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (showReminderSentToast) "✓ Reminder sent" else "Send reminder",
                                                    color = Color(0xFF90CAF9),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // CARD 2: 30-day deletion warning card matching Screenshot 3
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2C2C34),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFE53935).copy(alpha = 0.2f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = "This video will be deleted after 30 days",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "You can download it to your device in the video's ... menu. Learn more",
                                                fontSize = 12.sp,
                                                color = Color.LightGray,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Menu Action 1: Boost post >
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2C2C34),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast.makeText(context, "Boost Post: পোস্ট প্রমোট করার অপশন খোলা হয়েছে", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Boost post", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Menu Action 2: Turn this live into a reel >
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF2C2C34),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast.makeText(context, "রিলসে রূপান্তর করার প্রসেসিং শুরু হয়েছে 🎬", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFFAB47BC), modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Turn this live into a reel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Bottom Action Buttons: Done Button + Delete/Trash Button matching Screenshot 3
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            // Save as published post in FaceFeed
                                            viewModel.createPost(
                                                text = if (liveDescription.isNotBlank()) liveDescription else "🔴 লাইভ সম্পন্ন: $liveTitle",
                                                hashtags = liveHashtags,
                                                images = listOf(if (isFrontCamera) currentUser.avatarUrl else "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000"),
                                                isVideo = true,
                                                videoTitle = liveTitle
                                            )
                                            Toast.makeText(context, "লাইভ ভিডিও টাইমলাইনে ও স্টোরিতে সফলভাবে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text("Done", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Trash Icon
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF2C2C34),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable { showDeleteConfirmDialog = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Footer notice matching Screenshot 3
                                Text(
                                    text = "Your video will also be added to your story. Closing the app or switching accounts will stop the HD upload.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // DIALOGS & OVERLAYS
            // =========================================================================

            // Delete Confirm Dialog
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("লাইভ ভিডিও মুছে ফেলবেন?") },
                    text = { Text("আপনি কি নিশ্চিতভাবে এই লাইভ রেকর্ডিংটি ডিসকার্ড করতে চান? এটি টাইমলাইনে সেভ হবে না।") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                Toast.makeText(context, "লাইভ ভিডিও মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("হ্যাঁ, ডিলিট করুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("বাতিল")
                        }
                    }
                )
            }

            // Privacy Dialog
            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("লাইভ দর্শক নির্বাচন (Audience)", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple(LivePrivacy.PUBLIC, "🌐 Public", "যে কেউ এই লাইভ ভিডিও দেখতে পারবে"),
                                Triple(LivePrivacy.FRIENDS, "👥 Friends", "শুধুমাত্র আপনার বন্ধুরা দেখতে পারবে"),
                                Triple(LivePrivacy.ONLY_ME, "🔒 Only Me", "শুধুমাত্র আপনি নিজে দেখতে পারবেন")
                            ).forEach { (privacy, title, desc) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (livePrivacy == privacy) FacebookBlue.copy(alpha = 0.15f) else Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            livePrivacy = privacy
                                            showPrivacyDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = livePrivacy == privacy,
                                            onClick = {
                                                livePrivacy = privacy
                                                showPrivacyDialog = false
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            // Description Edit Dialog
            if (showEditDescriptionDialog) {
                var tempDesc by remember { mutableStateOf(liveDescription) }
                AlertDialog(
                    onDismissRequest = { showEditDescriptionDialog = false },
                    title = { Text("লাইভের বিবরণ লিখুন") },
                    text = {
                        OutlinedTextField(
                            value = tempDesc,
                            onValueChange = { tempDesc = it },
                            placeholder = { Text("Tap to add a description...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                liveDescription = tempDesc
                                if (liveDescription.isNotBlank()) {
                                    liveTitle = liveDescription
                                }
                                showEditDescriptionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDescriptionDialog = false }) {
                            Text("বাতিল")
                        }
                    }
                )
            }

            // Stickers & Filters Dialog
            if (showStickersDialog) {
                AlertDialog(
                    onDismissRequest = { showStickersDialog = false },
                    title = { Text("ফিল্টার ও স্টিকার নির্বাচন (Stickers)", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val stickers = listOf("✨ নরমাল", "🕶️ কুল সানগ্লাস", "👑 গোল্ডেন ক্রাউন", "🌸 ফুল মালা", "🐱 কিউট ক্যাট ফেস", "🔥 ফায়ার ইফেক্ট")
                            stickers.forEachIndexed { idx, item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedFilterIndex == idx) FacebookBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedFilterIndex = idx
                                            showStickersDialog = false
                                            Toast.makeText(context, "$item প্রয়োগ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Text(
                                        text = item,
                                        modifier = Modifier.padding(12.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showStickersDialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                )
            }

            // 5 Social Advertisements Selector Dialog
            if (showAdsSelectorDialog) {
                AlertDialog(
                    onDismissRequest = { showAdsSelectorDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = BangladeshGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("লাইভ সামাজিক বিজ্ঞাপন (৫টি)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "লাইভ ভিডিও চলাকালীন মোট ৫টি সামাজিক সচেতনতামূলক বিজ্ঞাপন প্রচার করতে পারবেন:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            SampleData.sampleSocialAds.forEachIndexed { index, ad ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeLiveAd = ad
                                            activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (${index + 1}/৫): ${ad.category}"
                                            showAdsSelectorDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = BangladeshGreen,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ad.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            Text(ad.slogan, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = BangladeshGreen, modifier = Modifier.size(22.dp))
                                    }
                                }
                            }

                            // Google AdMob Live Mid-Roll Ad Test trigger
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BangladeshRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, BangladeshRed.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showGoogleLiveMidAd = true
                                        showAdsSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = BangladeshRed,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("Ad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Google AdMob লাইভ অ্যাড (৩ মিনিট ও ৮ মিনিট)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BangladeshRed)
                                        Text("এখনই পরীক্ষা করতে ট্যাপ করুন (৫ সেকেন্ড কাউন্টডাউন)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = BangladeshRed, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showAdsSelectorDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                        ) {
                            Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Google AdMob Live Mid-Roll Ad (at 3 min / 180s and 8 min / 480s)
            if (showGoogleLiveMidAd) {
                VideoMidAd(
                    onAdFinished = {
                        showGoogleLiveMidAd = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            // Active Social Advertisement Overlay
            if (activeLiveAd != null) {
                SocialAdOverlay(
                    ad = activeLiveAd!!,
                    adSlotTitle = activeLiveAdSlotTitle.ifBlank { "লাইভ সামাজিক বিজ্ঞাপন (৫টি চালু)" },
                    onAdFinished = {
                        activeLiveAd = null
                    }
                )
            }
        }
    }
}

@Composable
private fun LiveToolButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) Color(0xFFFFD54F) else Color.Black.copy(alpha = 0.45f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
