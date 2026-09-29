package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveStreamStudioDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var isFrontCamera by remember { mutableStateOf(true) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isMicMuted by remember { mutableStateOf(false) }
    var liveSeconds by remember { mutableStateOf(165) } // Started ~2.5 mins ago
    var viewerCount by remember { mutableStateOf(5840) } // Thousands of active viewers
    var hostMessage by remember { mutableStateOf("") }
    var showEndLiveConfirm by remember { mutableStateOf(false) }
    var showEditTitleHashtagDialog by remember { mutableStateOf(false) }

    // 5 Social Advertisements state for Live Video
    var activeLiveAd by remember { mutableStateOf<SocialAd?>(null) }
    var activeLiveAdSlotTitle by remember { mutableStateOf("") }
    var showAdsSelectorDialog by remember { mutableStateOf(false) }
    var shownLiveAdIndices by remember { mutableStateOf(setOf<Int>()) }

    // Live Title & Hashtags as requested by user
    var liveTitle by remember { mutableStateOf("🔴 সবার সাথে সরাসরি আড্ডা ও গানবাজনা ✨") }
    var liveHashtags by remember { mutableStateOf(listOf("#FaceFeed", "#Live", "#Bangladesh", "#NiyonIslamSohag", "#Viral")) }
    var newHashtagInput by remember { mutableStateOf("") }
    var recentJoinNotice by remember { mutableStateOf("🎉 তানভীর হাসান, রিয়া আক্তার ও আরও ১,২৫০ জন লাইভে যুক্ত হয়েছেন!") }

    val liveComments = remember { mutableStateListOf<LiveCommentItem>() }
    val floatingHearts = remember { mutableStateListOf<Long>() }

    // Initialize initial comments
    LaunchedEffect(Unit) {
        liveComments.addAll(SampleData.sampleLiveComments)
    }

    // Live timer & viewer counter tick (thousands of viewers)
    LaunchedEffect(Unit) {
        val joiners = listOf(
            "🎉 তানভীর হাসান, রিয়া আক্তার ও আরও ১,২৫০ জন লাইভে যুক্ত হয়েছেন!",
            "🔥 সাকিব আল হাসান ও আরও ৩,৪০০ জন দর্শক লাইভ দেখছেন!",
            "✨ ঢাকা ও চট্টগ্রাম থেকে আরও ২,১০০ জন লাইভে জয়েন করেছেন!",
            "👏 নাজমুল, সুমাইয়া ও আরও ৯৮০ জন লাইভে লাইক দিয়েছেন!"
        )
        var noticeIndex = 0
        while (true) {
            delay(1000)
            liveSeconds++
            viewerCount += (-8..35).random()
            if (liveSeconds % 12 == 0) {
                recentJoinNotice = joiners[noticeIndex % joiners.size]
                noticeIndex++
            }

            // 5 Social advertisements scheduling for Live Video (৫ টি সামাজিক এডবেরাইজ)
            if (activeLiveAd == null) {
                if (liveSeconds >= 175 && !shownLiveAdIndices.contains(0)) {
                    shownLiveAdIndices = shownLiveAdIndices + 0
                    activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (১/৫): রক্তদান কর্মসূচি"
                    activeLiveAd = SampleData.sampleSocialAds.getOrNull(0)
                } else if (liveSeconds >= 210 && !shownLiveAdIndices.contains(1)) {
                    shownLiveAdIndices = shownLiveAdIndices + 1
                    activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (২/৫): বৃক্ষরোপণ অভিযান"
                    activeLiveAd = SampleData.sampleSocialAds.getOrNull(1)
                } else if (liveSeconds >= 245 && !shownLiveAdIndices.contains(2)) {
                    shownLiveAdIndices = shownLiveAdIndices + 2
                    activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (৩/৫): পথশিশু শিক্ষা তহবিল"
                    activeLiveAd = SampleData.sampleSocialAds.getOrNull(2)
                } else if (liveSeconds >= 280 && !shownLiveAdIndices.contains(3)) {
                    shownLiveAdIndices = shownLiveAdIndices + 3
                    activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (৪/৫): ক্ষুধামুক্ত বাংলাদেশ"
                    activeLiveAd = SampleData.sampleSocialAds.getOrNull(3)
                } else if (liveSeconds >= 315 && !shownLiveAdIndices.contains(4)) {
                    shownLiveAdIndices = shownLiveAdIndices + 4
                    activeLiveAdSlotTitle = "লাইভ সামাজিক বিজ্ঞাপন (৫/৫): নিরাপদ সড়ক সচেতনতা"
                    activeLiveAd = SampleData.sampleSocialAds.getOrNull(4)
                }
            }
        }
    }

    // Periodic new simulated comments arriving from viewers across Bangladesh
    LaunchedEffect(Unit) {
        val extraComments = listOf(
            LiveCommentItem(UUID.randomUUID().toString(), "Kamrul Hasan", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200", "ভাইয়া সাউন্ড একদম পরিষ্কার ও ক্লিয়ার 👏"),
            LiveCommentItem(UUID.randomUUID().toString(), "Fatema Khatun", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", "মাশাআল্লাহ, অনেক সুন্দর লাইভ ❤️"),
            LiveCommentItem(UUID.randomUUID().toString(), "Biplob Kumar", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200", "লাইভ লিঙ্ক ফেসবুক ও মেসেঞ্জারে শেয়ার করলাম ভাই 🔥"),
            LiveCommentItem(UUID.randomUUID().toString(), "Nusrat Jahan", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "কেমন আছেন ভাইয়া? সিলেট থেকে দেখছি 🇧🇩"),
            LiveCommentItem(UUID.randomUUID().toString(), "Arif Hossain", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", "হাজার হাজার মানুষ লাইভ দেখছে! দারুন 🤩"),
            LiveCommentItem(UUID.randomUUID().toString(), "Sohag Fan Club", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200", "Niyon Islam Sohag ভাই এগিয়ে যান ❤️")
        )
        for (item in extraComments) {
            delay(3500)
            liveComments.add(item)
            floatingHearts.add(System.currentTimeMillis())
        }
    }

    val hours = liveSeconds / 3600
    val minutes = (liveSeconds % 3600) / 60
    val secs = liveSeconds % 60
    val timeFormatted = if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, secs) else String.format("%02d:%02d", minutes, secs)
    val formattedViewers = if (viewerCount >= 1000) String.format("%.1fK", viewerCount / 1000.0) else "$viewerCount"

    Dialog(
        onDismissRequest = { showEndLiveConfirm = true },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Camera Stream Simulator (User Avatar or Live Camera feed background)
            AsyncImage(
                model = if (isFrontCamera) {
                    currentUser.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1000" }
                } else {
                    "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1000"
                },
                contentDescription = "Live Camera Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlays (Top & Bottom) for clear readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Header: Back button, LIVE badge, viewers, camera toggles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Prominent Back Button
                        IconButton(
                            onClick = { showEndLiveConfirm = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back / বের হন",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Pulsing LIVE Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BangladeshRed
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Timer
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = timeFormatted,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Viewers count in thousands
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = BangladeshRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$formattedViewers দর্শক",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Camera Controls (Flip, Flash, Mic)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isFrontCamera = !isFrontCamera },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { isFlashOn = !isFlashOn },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isFlashOn) Color.Yellow.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (isFlashOn) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { isMicMuted = !isMicMuted },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMicMuted) BangladeshRed else Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Seconds & Minutes Active Timer Bar & 5 Social Ads Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BangladeshRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "লাইভ: $timeFormatted",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 5 Social Ads Badge / Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BangladeshGreen.copy(alpha = 0.95f),
                        modifier = Modifier.clickable { showAdsSelectorDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "৫টি সামাজিক বিজ্ঞাপন 📢",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Title & Hashtag Card (as requested: "লেখা টাইটেল ও হেস্টাগ ব্যবহার করতে পারে")
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = liveTitle,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showEditTitleHashtagDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Title & Hashtags",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Hashtags Flow Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            liveHashtags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FacebookBlue.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = tag,
                                        color = Color(0xFF64B5F6),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Real-time Thousands Join Notification
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E88E5).copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recentJoinNotice,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Floating Hearts / Reactions
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 20.dp)
            ) {
                floatingHearts.takeLast(6).forEach { _ ->
                    Text("❤️", fontSize = 28.sp)
                }
            }

            // Bottom Section: Comments Feed & Host Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                // Live Scrolling Comments Feed (max height 220dp)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(liveComments.takeLast(6)) { comment ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = comment.avatarUrl,
                                    contentDescription = comment.authorName,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(comment.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(comment.comment, color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Live Reaction Emojis (👍 ❤️ 🥰 😮 😂 😢 😡 🔥 🎉 🎂)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val liveReactionEmojis = listOf(
                        Pair("❤️", ReactionType.LOVE),
                        Pair("🥰", ReactionType.CARE),
                        Pair("👍", ReactionType.LIKE),
                        Pair("😂", ReactionType.HAHA),
                        Pair("😮", ReactionType.WOW),
                        Pair("😢", ReactionType.SAD),
                        Pair("😡", ReactionType.ANGRY),
                        Pair("🔥", ReactionType.HUNDRED),
                        Pair("🎉", ReactionType.LIKE),
                        Pair("🎂", ReactionType.LOVE)
                    )
                    liveReactionEmojis.forEach { (emoji, reactionType) ->
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    floatingHearts.add(System.currentTimeMillis())
                                    AppSoundManager.playReactionSound(reactionType)
                                    liveComments.add(
                                        LiveCommentItem(
                                            UUID.randomUUID().toString(),
                                            currentUser.fullName,
                                            currentUser.avatarUrl,
                                            "লাইভে রিয়াকশন দিয়েছেন: $emoji"
                                        )
                                    )
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Host Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hostMessage,
                        onValueChange = { hostMessage = it },
                        placeholder = { Text("লাইভে কিছু লিখুন...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.7f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Floating Reaction Buttons (Heart, Fire)
                    IconButton(
                        onClick = {
                            floatingHearts.add(System.currentTimeMillis())
                            if (hostMessage.isNotBlank()) {
                                liveComments.add(LiveCommentItem(UUID.randomUUID().toString(), currentUser.fullName, currentUser.avatarUrl, hostMessage))
                                hostMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // End Live Button
                    Button(
                        onClick = { showEndLiveConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshRed),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("শেষ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Confirmation Dialog for Ending Live
            if (showEndLiveConfirm) {
                AlertDialog(
                    onDismissRequest = { showEndLiveConfirm = false },
                    title = { Text("লাইভ ভিডিও শেষ করবেন?") },
                    text = { Text("আপনি কি নিশ্চিতভাবে এই লাইভ স্ট্রিমিং সেশনটি শেষ করতে চান?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showEndLiveConfirm = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshRed)
                        ) {
                            Text("হ্যাঁ, লাইভ শেষ করুন", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndLiveConfirm = false }) {
                            Text("বাতিল / চালিয়ে যান")
                        }
                    }
                )
            }

            // Edit Live Title & Hashtags Dialog (as requested by user)
            if (showEditTitleHashtagDialog) {
                var tempTitle by remember { mutableStateOf(liveTitle) }
                val popularHashtagPresets = listOf(
                    "#FaceFeed", "#Live", "#Bangladesh", "#Viral", "#NiyonIslamSohag",
                    "#Adda", "#Music", "#Dhaka", "#Chittagong", "#Sylhet", "#BengaliLive"
                )

                AlertDialog(
                    onDismissRequest = { showEditTitleHashtagDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = FacebookBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("লাইভ টাইটেল ও হ্যাশট্যাগ এডিট", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("লাইভের টাইটেল:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = tempTitle,
                                onValueChange = { tempTitle = it },
                                placeholder = { Text("লাইভের আকর্ষণীয় শিরোনাম লিখুন...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("হ্যাশট্যাগসমূহ (ট্যাপ করে অন/অফ করুন):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                popularHashtagPresets.forEach { tag ->
                                    val isSelected = liveHashtags.contains(tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            liveHashtags = if (isSelected) {
                                                liveHashtags - tag
                                            } else {
                                                liveHashtags + tag
                                            }
                                        },
                                        label = { Text(tag, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }

                            // Custom hashtag input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newHashtagInput,
                                    onValueChange = { newHashtagInput = it },
                                    placeholder = { Text("#কাস্টম_হ্যাশট্যাগ", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilledTonalButton(
                                    onClick = {
                                        if (newHashtagInput.isNotBlank()) {
                                            val tagFormatted = if (newHashtagInput.startsWith("#")) newHashtagInput.trim() else "#${newHashtagInput.trim()}"
                                            if (!liveHashtags.contains(tagFormatted)) {
                                                liveHashtags = liveHashtags + tagFormatted
                                            }
                                            newHashtagInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("যোগ")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempTitle.isNotBlank()) {
                                    liveTitle = tempTitle
                                }
                                showEditTitleHashtagDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditTitleHashtagDialog = false }) {
                            Text("বাতিল")
                        }
                    }
                )
            }

            // 5 Social Advertisements Selector Dialog for Live Streaming
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
                                text = "লাইভ ভিডিওতে মোট ৫টি সামাজিক সচেতনতামূলক বিজ্ঞাপন নির্ধারিত রয়েছে। আপনি যেকোনো সময় এগুলো প্লে করতে পারেন:",
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

            // Active Social Advertisement Overlay for Live Video (৫টি বিজ্ঞাপন)
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
