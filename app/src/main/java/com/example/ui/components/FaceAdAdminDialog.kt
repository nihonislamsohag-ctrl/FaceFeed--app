package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.MusicTrack
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

/**
 * Face AD এডমিন Dialog:
 * Replaces the old Google & Chrome Hub shortcut.
 * Contains 3 core tools:
 * 1. Ads Manager (ক্যাম্পেইন বাজেট, টার্গেটিং, পারফরম্যান্স)
 * 2. AdMob (গুগল অ্যাডমব ব্যানার, ইন্টারস্টিশিয়াল, আর্নিং)
 * 3. Audio Music Player (ফেসবুক মিউজিক প্লেয়ার ও গ্যালারি থেকে অডিও আপলোড হাব)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceAdAdminDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSectionIndex by remember { mutableStateOf(0) } // 0: Ads Manager, 1: AdMob, 2: Audio Music Player
    var showFullMusicDialog by remember { mutableStateOf(false) }
    var showUploadAudioDialog by remember { mutableStateOf(false) }

    // AdMob state toggles
    var isAdMobEnabled by remember { mutableStateOf(true) }
    var isBannerAdsEnabled by remember { mutableStateOf(true) }
    var isInterstitialAdsEnabled by remember { mutableStateOf(true) }
    var isRewardedAdsEnabled by remember { mutableStateOf(true) }

    // Ads Manager mock campaigns state
    var showCreateCampaignDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = FacebookBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Face AD এডমিন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Ads Manager • AdMob • Audio Music Player",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Navigation Tabs
                        TabRow(
                            selectedTabIndex = selectedSectionIndex,
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedSectionIndex == 0,
                                onClick = { selectedSectionIndex = 0 },
                                text = {
                                    Text(
                                        text = "Ads Manager",
                                        fontWeight = if (selectedSectionIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                            Tab(
                                selected = selectedSectionIndex == 1,
                                onClick = { selectedSectionIndex = 1 },
                                text = {
                                    Text(
                                        text = "AdMob",
                                        fontWeight = if (selectedSectionIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                            Tab(
                                selected = selectedSectionIndex == 2,
                                onClick = { selectedSectionIndex = 2 },
                                text = {
                                    Text(
                                        text = "Audio Music",
                                        fontWeight = if (selectedSectionIndex == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }

                // Body Content based on selected tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedSectionIndex) {
                        0 -> AdsManagerSection(
                            onCreateCampaignClick = { showCreateCampaignDialog = true }
                        )
                        1 -> AdMobSection(
                            isAdMobEnabled = isAdMobEnabled,
                            onToggleAdMob = { isAdMobEnabled = it },
                            isBannerEnabled = isBannerAdsEnabled,
                            onToggleBanner = { isBannerAdsEnabled = it },
                            isInterstitialEnabled = isInterstitialAdsEnabled,
                            onToggleInterstitial = { isInterstitialAdsEnabled = it },
                            isRewardedEnabled = isRewardedAdsEnabled,
                            onToggleRewarded = { isRewardedAdsEnabled = it }
                        )
                        2 -> AudioMusicSection(
                            viewModel = viewModel,
                            onOpenFullMusicHub = { showFullMusicDialog = true },
                            onUploadAudioClick = { showUploadAudioDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Full Facebook Music Dialog
    if (showFullMusicDialog) {
        FacebookMusicAddDialog(
            viewModel = viewModel,
            onDismiss = { showFullMusicDialog = false }
        )
    }

    // Upload Audio Dialog
    if (showUploadAudioDialog) {
        UploadAudioModalDialog(
            viewModel = viewModel,
            onDismiss = { showUploadAudioDialog = false }
        )
    }

    // Create Ad Campaign Dialog
    if (showCreateCampaignDialog) {
        CreateAdCampaignDialog(
            viewModel = viewModel,
            onDismiss = { showCreateCampaignDialog = false }
        )
    }
}

/**
 * 1. Ads Manager Section
 */
@Composable
fun AdsManagerSection(
    onCreateCampaignClick: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Spend",
                    value = "৳১২,৪৫০",
                    icon = Icons.Default.Payments,
                    color = BangladeshGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Impressions",
                    value = "২৪৫.৮K",
                    icon = Icons.Default.Visibility,
                    color = FacebookBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Clicks",
                    value = "১৮,২৩০",
                    icon = Icons.Default.TouchApp,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg. CTR",
                    value = "৭.৪১%",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Action button: Create Ad Campaign
            Button(
                onClick = onCreateCampaignClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("নতুন অ্যাড ক্যাম্পেইন তৈরি করুন (Create Ad)", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "সক্রিয় বিজ্ঞাপন ক্যাম্পেইনসমূহ (Active Campaigns):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        item {
            CampaignItemCard(
                title = "FaceFeed ঈদ ধামাকা ও ফলোয়ার গ্রোথ অ্যাড",
                budget = "৳৩,০০০ / দিন",
                reach = "৮৫K ভিউ",
                status = "Active",
                platform = "Feed & Reels"
            )
        }

        item {
            CampaignItemCard(
                title = "ডিজিটাল ক্রিয়েটর প্রমোশন ও ভেরিফাইড ক্যাম্পেইন",
                budget = "৳১,৫০০ / দিন",
                reach = "৪২K ভিউ",
                status = "Active",
                platform = "Stories & Video"
            )
        }

        item {
            CampaignItemCard(
                title = "Palli Gramer Bashir Sur মিউজিক প্রোমো",
                budget = "৳২,০০০ / দিন",
                reach = "১২০K ভিউ",
                status = "Active",
                platform = "Audio & Reels"
            )
        }
    }
}

@Composable
fun CampaignItemCard(
    title: String,
    budget: String,
    reach: String,
    status: String,
    platform: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = BangladeshGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BangladeshGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "বাজেট: $budget",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "পৌঁছেছে: $reach",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "প্লেসমেন্ট: $platform",
                fontSize = 11.sp,
                color = FacebookBlue
            )
        }
    }
}

/**
 * 2. AdMob Section
 */
@Composable
fun AdMobSection(
    isAdMobEnabled: Boolean,
    onToggleAdMob: (Boolean) -> Unit,
    isBannerEnabled: Boolean,
    onToggleBanner: (Boolean) -> Unit,
    isInterstitialEnabled: Boolean,
    onToggleInterstitial: (Boolean) -> Unit,
    isRewardedEnabled: Boolean,
    onToggleRewarded: (Boolean) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Master AdMob Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdMobEnabled) Color(0xFFF9AB00).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAdMobEnabled) Color(0xFFF9AB00) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFF9AB00),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "গুগল অ্যাডমব (Google AdMob)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isAdMobEnabled) "সব অ্যাড বর্তমানে সক্রিয়" else "সব অ্যাড বর্তমানে স্থগিত",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAdMobEnabled,
                        onCheckedChange = onToggleAdMob,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF9AB00))
                    )
                }
            }
        }

        item {
            // Earnings summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Today's Revenue",
                    value = "$24.60",
                    icon = Icons.Default.AttachMoney,
                    color = BangladeshGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "eCPM",
                    value = "$1.85",
                    icon = Icons.Default.Analytics,
                    color = FacebookBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "অ্যাড ইউনিট কনফিগারেশন ও কন্ট্রোল:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        item {
            AdMobUnitRow(
                title = "ব্যানার বিজ্ঞাপন (Banner Ads)",
                adUnitId = GoogleAdConfig.BANNER_ID,
                isEnabled = isBannerEnabled && isAdMobEnabled,
                onToggle = onToggleBanner
            )
        }

        item {
            AdMobUnitRow(
                title = "ইন্টারস্টিশিয়াল বিজ্ঞাপন (Interstitial Ads)",
                adUnitId = GoogleAdConfig.INTERSTITIAL_ID,
                isEnabled = isInterstitialEnabled && isAdMobEnabled,
                onToggle = onToggleInterstitial
            )
        }

        item {
            AdMobUnitRow(
                title = "রিওয়ার্ডেড / অ্যাপ আইডি (AdMob App ID)",
                adUnitId = GoogleAdConfig.APP_ID,
                isEnabled = isRewardedEnabled && isAdMobEnabled,
                onToggle = onToggleRewarded
            )
        }

        item {
            Button(
                onClick = {
                    Toast.makeText(context, "Google AdMob লাইভ ইউনিট (${GoogleAdConfig.BANNER_ID}) সক্রিয় রয়েছে!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9AB00))
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("টেস্ট অ্যাড প্রিভিউ দেখুন (Test Ad Preview)", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun AdMobUnitRow(
    title: String,
    adUnitId: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                    text = adUnitId,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * 3. Audio Music Player Section
 */
@Composable
fun AudioMusicSection(
    viewModel: FaceFeedViewModel,
    onOpenFullMusicHub: () -> Unit,
    onUploadAudioClick: () -> Unit
) {
    val musicTracks by viewModel.musicTracks.collectAsState()
    val isPlaying by AppSoundManager.isPlaying.collectAsState()
    val currentPlayingTrackId by AppSoundManager.currentPlayingTrackId.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Hero Banner for Music Feature
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2638),
                border = androidx.compose.foundation.BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF45BD62),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফেসবুক অডিও মিউজিক হাব",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "গানের তালিকা Firebase Firestore থেকে আসছে। সার্চ করুন, শুনুন এবং পোস্ট, স্টোরি ও রিলসে মিউজিক যুক্ত করুন!",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenFullMusicHub,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("মিউজিক হাব খুলুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onUploadAudioClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = BangladeshGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অডিও আপলোড", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Firestore থেকে লাইভ গানসমূহ (${musicTracks.size} টি গান সক্রিয়):",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Preview top 4 tracks
        items(musicTracks.take(5), key = { it.id }) { track ->
            val isThisTrackPlaying = isPlaying && currentPlayingTrackId == track.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFullMusicHub() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.thumbnailUrl.ifEmpty { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300" },
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${track.artist} • ${track.views} views",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (track.lyrics.isNotBlank()) {
                            Text(
                                text = "🎤 ${track.lyrics.take(30)}...",
                                fontSize = 10.sp,
                                color = BangladeshGreen,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isThisTrackPlaying) {
                                AppSoundManager.pauseAudio()
                            } else {
                                AppSoundManager.playAudio(track.toPostAudioTrack())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isThisTrackPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play/Pause",
                            tint = FacebookBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

/**
 * Dialog to create a new ad campaign that saves to Firebase Firestore and displays across user feeds
 */
@Composable
fun CreateAdCampaignDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var headline by remember { mutableStateOf("") }
    var targetReach by remember { mutableStateOf("৫০,০০০ - ১,০০,০০০ মানুষ") }
    var budget by remember { mutableStateOf("৳২,০০০") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন বিজ্ঞাপন তৈরি করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("বিজ্ঞাপনের শিরোনাম") },
                    placeholder = { Text("e.g. নতুন প্রোডাক্ট অফার...") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("প্রতিদিনের বাজেট") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetReach,
                    onValueChange = { targetReach = it },
                    label = { Text("লক্ষ্যমাত্রা (Audience Reach)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (headline.isBlank()) {
                        Toast.makeText(context, "দয়া করে বিজ্ঞাপনের শিরোনাম লিখুন!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createSponsoredAdPost(headline.trim(), budget.trim(), targetReach.trim())
                    Toast.makeText(context, "বিজ্ঞাপন সফলভাবে তৈরি ও Firebase Firestore এ সক্রিয় হয়েছে! সকল ব্যবহারকারীদের ফিডে এটি দেখা যাবে। 🚀", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
            ) {
                Text("ক্যাম্পেইন চালু করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
