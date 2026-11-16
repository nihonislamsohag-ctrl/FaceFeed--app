package com.example.ui.page

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SocialPage
import com.example.ui.theme.BangladeshGreen

data class PageVideoItem(
    val id: String,
    val title: String,
    val duration: String,
    val views: String,
    val thumbnail: String,
    var likes: Int = 240,
    var isLiked: Boolean = false
)

data class PageReelItem(
    val id: String,
    val title: String,
    val musicName: String,
    val views: String,
    val thumbnail: String,
    var likes: Int = 1420,
    var comments: Int = 118,
    var shares: Int = 42,
    var isLiked: Boolean = false
)

/**
 * Requirement 7: Page Video & Reel Video System
 * - Page Video section: Upload long Video
 * - Page Reels section: Upload short Reel Video
 * - Reel options: Music, Text, Filter, Like, Comment, Share
 */
@Composable
fun PageVideosReelsView(
    page: SocialPage,
    onToast: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Videos, 1 = Reels
    var showUploadVideoDialog by remember { mutableStateOf(false) }
    var showCreateReelDialog by remember { mutableStateOf(false) }
    var viewingVideo by remember { mutableStateOf<PageVideoItem?>(null) }
    var viewingReel by remember { mutableStateOf<PageReelItem?>(null) }

    val longVideos = remember(page.id) {
        mutableStateListOf(
            PageVideoItem("v1", "${page.name}-এর নতুন এক্সক্লুসিভ ফুল ভিডিও", "12:45", "42K views", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=600"),
            PageVideoItem("v2", "কীভাবে সহজেই শুরু করবেন? সম্পূর্ণ গাইডলাইন", "08:20", "18K views", "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=600"),
            PageVideoItem("v3", "কুয়াকাটা ট্যুর ও প্রাকৃতিক সৌন্দর্য ডকুফিল্ম", "15:10", "65K views", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600")
        )
    }

    val shortReels = remember(page.id) {
        mutableStateListOf(
            PageReelItem("r1", "আজকের স্পেশাল মুহূর্ত ✨", "Trending Lofi Beats", "120K", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400"),
            PageReelItem("r2", "প্রাকৃতিক সুন্দর দৃশ্য 🌿", "Acoustic Nature Vibes", "85K", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400"),
            PageReelItem("r3", "FotoFilm স্পেশাল রিলস 🔥", "Studio Instrumental", "210K", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400"),
            PageReelItem("r4", "ভাইরাল টিপস ও ট্রিকস 💡", "Groovy Tech Tune", "45K", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        // Sub-tabs: Videos vs Reels
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { selectedTab = 0 },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedTab == 0) BangladeshGreen else Color(0xFFF0F2F5),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.White else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Videos (লং ভিডিও)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) Color.White else Color.Black
                        )
                    }
                }

                Surface(
                    onClick = { selectedTab = 1 },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedTab == 1) Color(0xFFE91E63) else Color(0xFFF0F2F5),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.White else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reels (রিলস শর্টস)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            // ==========================================
            // Videos Section (Long Video)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${page.name}-এর ভিডিও সমূহ (${longVideos.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Button(
                    onClick = { showUploadVideoDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ভিডিও আপলোড", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(longVideos) { video ->
                    Surface(
                        onClick = { viewingVideo = video },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                AsyncImage(
                                    model = video.thumbnail,
                                    contentDescription = video.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Play icon overlay
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                // Duration Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = video.duration,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${page.name} • ${video.views}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                video.isLiked = !video.isLiked
                                                if (video.isLiked) video.likes++ else video.likes--
                                                onToast(if (video.isLiked) "লাইক দেওয়া হয়েছে 👍" else "লাইক সরানো হয়েছে")
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                if (video.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                                contentDescription = "Like",
                                                tint = if (video.isLiked) BangladeshGreen else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text("${video.likes}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // Reels Section (Short Reels)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পেইজ রিলস ভিডিও (${shortReels.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Button(
                    onClick = { showCreateReelDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রিল তৈরি করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shortReels) { reel ->
                    Surface(
                        onClick = { viewingReel = reel },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = reel.thumbnail,
                                contentDescription = reel.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                            )

                            // Views badge top
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(reel.views, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Reel Info Bottom
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = reel.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(reel.musicName, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload Video Dialog
    if (showUploadVideoDialog) {
        var videoTitleInput by remember { mutableStateOf("") }
        val videoFilePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null && videoTitleInput.isNotBlank()) {
                longVideos.add(
                    0,
                    PageVideoItem(
                        id = "v_${System.currentTimeMillis()}",
                        title = videoTitleInput,
                        duration = "05:00",
                        views = "1 view",
                        thumbnail = uri.toString()
                    )
                )
                showUploadVideoDialog = false
                onToast("ভিডিও সফলভাবে আপলোড হয়েছে! 🎥")
            }
        }

        AlertDialog(
            onDismissRequest = { showUploadVideoDialog = false },
            title = { Text("পেইজে লং ভিডিও আপলোড") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = videoTitleInput,
                        onValueChange = { videoTitleInput = it },
                        label = { Text("ভিডিওর শিরোনাম (Title)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { videoFilePicker.launch("video/*") },
                        enabled = videoTitleInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("গ্যালারি থেকে ভিডিও ফাইল বাছুন")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showUploadVideoDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Create Reel Dialog (with Music, Text, Filter)
    if (showCreateReelDialog) {
        var reelCaption by remember { mutableStateOf("") }
        var selectedFilter by remember { mutableStateOf("Normal") }
        var selectedMusic by remember { mutableStateOf("FotoFilm Viral Beats") }

        val reelMediaPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                shortReels.add(
                    0,
                    PageReelItem(
                        id = "r_${System.currentTimeMillis()}",
                        title = reelCaption.ifBlank { "নতুন শর্ট রিলস" },
                        musicName = selectedMusic,
                        views = "1",
                        thumbnail = uri.toString()
                    )
                )
                showCreateReelDialog = false
                onToast("শর্ট রিলস সফলভাবে পাবলিশ হয়েছে! 🎬")
            }
        }

        AlertDialog(
            onDismissRequest = { showCreateReelDialog = false },
            title = { Text("পেইজে শর্ট রিল তৈরি করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = reelCaption,
                        onValueChange = { reelCaption = it },
                        label = { Text("ক্যাপশন / টেক্সট লিখুন") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("মিউজিক নির্বাচন: $selectedMusic", fontSize = 12.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                    Text("ফিল্টার: $selectedFilter", fontSize = 12.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Normal", "Vibrant", "Vintage", "Warm").forEach { f ->
                            Surface(
                                onClick = { selectedFilter = f },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedFilter == f) Color(0xFFE91E63) else Color(0xFFF0F2F5)
                            ) {
                                Text(
                                    f,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = if (selectedFilter == f) Color.White else Color.Black
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { reelMediaPicker.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ভিডিও বাছুন ও রিল পোস্ট করুন")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateReelDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Video Player Dialog
    if (viewingVideo != null) {
        val vid = viewingVideo!!
        AlertDialog(
            onDismissRequest = { viewingVideo = null },
            title = { Text(vid.title, maxLines = 1) },
            text = {
                Column {
                    AsyncImage(
                        model = vid.thumbnail,
                        contentDescription = vid.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("পেইজ: ${page.name} • ভিউ: ${vid.views} • সময়: ${vid.duration}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { viewingVideo = null }) { Text("বন্ধ করুন") }
            }
        )
    }

    // Reel Player Dialog (Like, Comment, Share)
    if (viewingReel != null) {
        val r = viewingReel!!
        AlertDialog(
            onDismissRequest = { viewingReel = null },
            title = { Text("রিল: ${r.title}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(200.dp, 280.dp)) {
                        AsyncImage(
                            model = r.thumbnail,
                            contentDescription = r.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎵 ${r.musicName}", fontSize = 12.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TextButton(onClick = {
                            r.isLiked = !r.isLiked
                            if (r.isLiked) r.likes++ else r.likes--
                            onToast(if (r.isLiked) "রীলে লাইক দেওয়া হয়েছে ❤️" else "লাইক সরানো হয়েছে")
                        }) {
                            Icon(if (r.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${r.likes}")
                        }
                        TextButton(onClick = { onToast("কমেন্ট অপশন খোলা হয়েছে 💬") }) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${r.comments}")
                        }
                        TextButton(onClick = { onToast("রিলটি শেয়ার করা হয়েছে ↗️") }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${r.shares}")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewingReel = null }) { Text("সম্পন্ন") }
            }
        )
    }
}
