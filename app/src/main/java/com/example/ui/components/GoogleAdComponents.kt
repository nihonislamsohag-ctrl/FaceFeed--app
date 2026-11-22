package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import kotlinx.coroutines.delay

/**
 * Google AdMob & AdSense Monetization Configuration
 * User Production IDs:
 * - App ID: ca-app-pub-9310751224653667~3328217860
 * - Banner ID: ca-app-pub-9310751224653667/8654752079
 * - Interstitial ID: ca-app-pub-9310751224653667/7038418079
 */
object GoogleAdConfig {
    const val APP_ID = "ca-app-pub-9310751224653667~3328217860"
    const val BANNER_ID = "ca-app-pub-9310751224653667/8654752079"
    const val INTERSTITIAL_ID = "ca-app-pub-9310751224653667/7038418079"
    const val BANNER_TEST_ID = BANNER_ID
    const val INTERSTITIAL_TEST_ID = INTERSTITIAL_ID
}

data class GoogleAdCreative(
    val id: String,
    val title: String,
    val sponsorName: String,
    val description: String,
    val iconUrl: String,
    val bannerUrl: String,
    val ctaText: String,
    val rating: String = "4.9 ★"
)

object GoogleAdInventory {
    val creatives = listOf(
        GoogleAdCreative(
            id = "ad_1",
            title = "Google Cloud & AI Studio Build",
            sponsorName = "Google Cloud",
            description = "Build and monetize modern Android apps faster with high performance cloud infrastructure.",
            iconUrl = "",
            bannerUrl = "",
            ctaText = "Learn More"
        ),
        GoogleAdCreative(
            id = "ad_2",
            title = "ProLevelCut - 4K Video Editor",
            sponsorName = "FotoFilm Studio",
            description = "অসাধারণ ৪কে শর্ট ভিডিও ও ভিডিও এডিটিং অ্যাপ। আজই ব্যবহার করুন সম্পূর্ণ ফ্রিতে!",
            iconUrl = "",
            bannerUrl = "",
            ctaText = "Install Now"
        ),
        GoogleAdCreative(
            id = "ad_3",
            title = "FotoFilm Marketplace",
            sponsorName = "FotoFilm Commerce",
            description = "সেরা পণ্যে আকর্ষণীয় ছাড় ও নিরাপদ কেনাকাটার সুবিধা।",
            iconUrl = "",
            bannerUrl = "",
            ctaText = "Shop Now"
        )
    )
}

/**
 * AdBanner Component (Matching exact requested specs):
 * - Margin 15px 0 (vertical padding)
 * - Min-height 120px
 * - Background #f5f5f5
 * - Text "Advertisement" / "Google AdMob"
 * - Responsive
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = GoogleAdConfig.BANNER_ID,
    creativeIndex: Int = 0
) {
    val context = LocalContext.current
    val isMasterEnabled by com.example.data.NiyonOwnerControlStore.isAdMobMasterEnabled.collectAsState()
    val isBannerEnabled by com.example.data.NiyonOwnerControlStore.isAdMobBannerEnabled.collectAsState()
    if (!isMasterEnabled || !isBannerEnabled) return

    val creative = GoogleAdInventory.creatives[creativeIndex % GoogleAdInventory.creatives.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp) // Exact specification: margin 15px 0
            .defaultMinSize(minHeight = 120.dp), // Exact specification: min-height 120px
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // Exact specification: background #f5f5f5
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Advertisement Label & Test Ad ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFB300)
                    ) {
                        Text(
                            text = "Ad",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Advertisement", // Exact specification: text "Advertisement"
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
                ) {
                    Text(
                        text = "Google AdMob • Test",
                        fontSize = 9.sp,
                        color = FacebookBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Creative Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (creative.iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = creative.iconUrl,
                        contentDescription = creative.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FacebookBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = creative.title,
                            tint = FacebookBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creative.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${creative.sponsorName} • ${creative.rating}",
                        fontSize = 11.sp,
                        color = Color(0xFF616161),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = creative.description,
                        fontSize = 10.5.sp,
                        color = Color(0xFF757575),
                        lineHeight = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // CTA Button
                Button(
                    onClick = {
                        AppSoundManager.playReactionChime()
                        Toast.makeText(context, "Google AdMob: ${creative.ctaText} ক্লিক সফল!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = creative.ctaText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Unit ID info
            Text(
                text = "Unit ID: $adUnitId",
                fontSize = 8.5.sp,
                color = Color(0xFF9E9E9E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * VideoMidAd Component (Matching exact requested specs):
 * - Mid-roll video ad
 * - 5-second countdown timer
 * - Skip button (enabled after 5 seconds)
 * - Pause main video, show ad, then resume video
 * - Test Ad ID: ca-app-pub-3940256099942544/1033173712
 */
@Composable
fun VideoMidAd(
    onAdFinished: () -> Unit,
    modifier: Modifier = Modifier,
    adTitle: String = "Google AdMob Interstitial Ad",
    adUnitId: String = GoogleAdConfig.INTERSTITIAL_ID,
    creativeIndex: Int = 0
) {
    var countdown by remember { mutableIntStateOf(5) }
    var canSkip by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isMasterEnabled by com.example.data.NiyonOwnerControlStore.isAdMobMasterEnabled.collectAsState()
    val isInterstitialEnabled by com.example.data.NiyonOwnerControlStore.isAdMobInterstitialEnabled.collectAsState()
    if (!isMasterEnabled || !isInterstitialEnabled) {
        LaunchedEffect(Unit) { onAdFinished() }
        return
    }
    val creative = GoogleAdInventory.creatives[creativeIndex % GoogleAdInventory.creatives.size]

    LaunchedEffect(Unit) {
        AppSoundManager.playSocialAdChime()
        while (countdown > 0) {
            delay(1000L)
            countdown--
            if (countdown <= 0) {
                canSkip = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Ad Background Media Image or Clean Gradient
        if (creative.bannerUrl.isNotBlank()) {
            AsyncImage(
                model = creative.bannerUrl,
                contentDescription = creative.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.82f
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                        )
                    )
            )
        }

        // Gradient Protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.88f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Top Status Header (Google AdMob Info & 5-Second Countdown / Skip Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E88E5).copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = adTitle,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Countdown / Skip Button (User can skip after 5 seconds!)
            if (canSkip) {
                Button(
                    onClick = {
                        AppSoundManager.playReactionChime()
                        onAdFinished()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "স্কিপ করুন ⏭️",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "বিজ্ঞাপন $countdown সে.",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Advertiser Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (creative.iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = creative.iconUrl,
                        contentDescription = creative.title,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = creative.title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creative.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${creative.sponsorName} • ${creative.rating}",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = creative.description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CTA Button
            Button(
                onClick = {
                    AppSoundManager.playReactionChime()
                    Toast.makeText(context, "Google Ad: ${creative.ctaText} সম্পন্ন!", Toast.LENGTH_SHORT).show()
                    onAdFinished()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(creative.ctaText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Google AdMob Interstitial ID: $adUnitId",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Sticky Small Banner Ad (Rule 4):
 * আর যেকোনো ভিডিওর নিচে সবসময় ১টা ছোট Banner এড লেগে থাকবে। স্ক্রল করলেও যাবে না।
 * Unit ID: ca-app-pub-9310751224653667/8654752079
 */
@Composable
fun StickyVideoBottomBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = GoogleAdConfig.BANNER_ID,
    creativeIndex: Int = 0
) {
    val context = LocalContext.current
    val isMasterEnabled by com.example.data.NiyonOwnerControlStore.isAdMobMasterEnabled.collectAsState()
    val isBannerEnabled by com.example.data.NiyonOwnerControlStore.isAdMobBannerEnabled.collectAsState()
    if (!isMasterEnabled || !isBannerEnabled) return

    val creative = GoogleAdInventory.creatives[creativeIndex % GoogleAdInventory.creatives.size]

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        color = Color(0xFF1B1B1E),
        shadowElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Ad" badge
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFFFB300)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                if (creative.iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = creative.iconUrl,
                        contentDescription = creative.title,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(FacebookBlue.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = creative.title,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creative.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${creative.sponsorName} • Google Banner",
                        fontSize = 9.5.sp,
                        color = Color(0xFFB0BEC5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    AppSoundManager.playReactionChime()
                    Toast.makeText(context, "Google Ad: ${creative.ctaText} সফল!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = creative.ctaText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
