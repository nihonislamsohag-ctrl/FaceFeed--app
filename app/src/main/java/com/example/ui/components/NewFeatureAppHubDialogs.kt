package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.viewmodel.SimOperator
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

/**
 * Dialog for Basic Mode 6-SIM Free Data
 * ৬ টি সিম ধারা ফ্রীতে এমবি না থাকলে বেসিকমোর ডাটা ব্যবহার করা যাবে:
 * গ্রামীণ সিম, রবি সিম, এয়ারটেল সিম, টেলিটক সিম, বাংলালিংক সিম, স্কিটো সিম
 */
@Composable
fun SixSimFreeDataDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentSim by viewModel.currentSimOperator.collectAsState()
    val isFreeMode by viewModel.isFreeMode.collectAsState()

    val simList = listOf(
        SimInfo(SimOperator.GRAMEENPHONE, "গ্রামীণফোন সিম (GP)", "017 / 013 • দেশসেরা দ্রুততম নেটওয়ার্ক", Color(0xFF00A3E0), "📶"),
        SimInfo(SimOperator.ROBI, "রবি সিম (Robi)", "018 • দেশজুড়ে ৪.৫জি সুপার কানেক্টিভিটি", Color(0xFFE30613), "⚡"),
        SimInfo(SimOperator.AIRTEL, "এয়ারটেল সিম (Airtel)", "016 • তরুণদের প্রিয় বন্ধু ফ্রেন্ডস নেটওয়ার্ক", Color(0xFFED1C24), "🎯"),
        SimInfo(SimOperator.TELETALK, "টেলিটক সিম (Teletalk)", "015 • আমাদের ফোন, সরকারি ডিজিটাল সেবা", Color(0xFF00843D), "🇧🇩"),
        SimInfo(SimOperator.BANGLALINK, "বাংলালিংক সিম (Banglalink)", "019 / 014 • ডিজিটাল বাংলাদেশের সঙ্গী", Color(0xFFFF6600), "🔥"),
        SimInfo(SimOperator.SKITTO, "স্কিটো সিম (Skitto)", "017 / 013 • তরুণদের স্মার্ট রিলোড ও কুল ডেটা", Color(0xFFFF007A), "🚀")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF00C853), Color(0xFF0091EA)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CellTower, contentDescription = "Tower", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("৬ সিমের ফ্রী বেসিক ডাটা", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("এমবি না থাকলেও আনলিমিটেড ব্যবহার", fontSize = 11.sp, color = BangladeshGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BangladeshGreen.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "আপনার সিমে টাকা বা এমবি শেষ হলেও FaceFeed বেসিকমোডে টেক্সট ও পোস্ট সম্পূর্ণ বিনামূল্যে ব্রাউজ করতে পারবেন!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "আপনার ব্যবহৃত সিম অপারেটর নির্বাচন করুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6 SIM Operator Cards with Logo / Badge
                simList.forEach { sim ->
                    val isSelected = currentSim == sim.operator
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) sim.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) sim.color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.setSimOperator(sim.operator)
                                if (!isFreeMode) viewModel.toggleFreeMode()
                                Toast.makeText(context, "${sim.title} ফ্রী বেসিক ডাটা সক্রিয় হয়েছে! ⚡", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(sim.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sim.emoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sim.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sim.subtitle,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    color = sim.color,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "সক্রিয় ✓",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("সম্পন্ন ও ফ্রী ডাটা নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class SimInfo(
    val operator: SimOperator,
    val title: String,
    val subtitle: String,
    val color: Color,
    val emoji: String
)

/**
 * ProLevelCut Video Editor App Dialog
 * কোনো ঝামেলা ছাড়া যে কোনো ধরনের ভিডিও ফ্রীতে এডিট করতে পারবেন,
 * নিচে ডাউনলোড বা ইন্সটল করার লিংক ফাইল এড থাকবে।
 */
@Composable
fun ProLevelCutAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadApkUrl = "https://ais-pre-uxxbqekxfrv4arrv2ck4ma-461492489791.asia-southeast1.run.app/FaceFeed.apk"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ভিডিও এডিটর অ্যাপ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // App Icon and Title
                Image(
                    painter = painterResource(id = R.drawable.prolevelcut_app_icon),
                    contentDescription = "ProLevelCut App Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(2.dp, Color(0xFF00E676), RoundedCornerShape(22.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ProLevelCut",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "by Niyon Islam Sohag 👑",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00C853)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Highlight Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF00E676).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎬 কোনো ঝামেলা ছাড়া যে কোনো ধরনের ভিডিও সম্পূর্ণ ফ্রীতে এডিট করতে পারবেন!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• 4K আল্ট্রা এইচডি এক্সপোর্ট • রিলস ও শর্টস অটো-কাট • ওয়াটারমার্ক ছাড়া ফ্রী রেন্ডারিং • স্লোমোশন, ফিল্টার, ট্রানজিশন ও বাংলা ফন্ট যুক্ত করুন",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Download / Install Link Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadApkUrl))
                            context.startActivity(intent)
                            Toast.makeText(context, "ProLevelCut অ্যাপ ফাইল ডাউনলোড শুরু হচ্ছে... 📥", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "ব্রাউজারে লিঙ্ক খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ProLevelCut ডাউনলোড ও ইনস্টল করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "🎬 ProLevelCut - যে কোনো ভিডিও কোনো ঝামেলা ছাড়া ফ্রীতে এডিট করুন!\nপ্রতিষ্ঠাতা: Niyon Islam Sohag\nডাউনলোড লিঙ্ক: $downloadApkUrl")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "ProLevelCut লিঙ্ক শেয়ার করুন"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("বন্ধুদের সাথে অ্যাপ লিঙ্ক শেয়ার করুন", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * GramFilm App Dialog (Replaces Rose Bloom per user request with green and red branding and uploaded logo)
 */
@Composable
fun GramFilmAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = BangladeshGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "গ্রামবাংলা প্ল্যাটফর্ম 🌾",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BangladeshGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // The second uploaded picture (GramFilm Logo)
                Image(
                    painter = painterResource(id = R.drawable.ic_gramfilm_logo),
                    contentDescription = "GramFilm Logo",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(3.dp, Brush.horizontalGradient(listOf(BangladeshGreen, BangladeshRed)), RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Green & Red Title "GramFilm"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Gram",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = BangladeshGreen
                    )
                    Text(
                        text = "Film",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = BangladeshRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "🎬", fontSize = 20.sp)
                }

                Text(
                    text = "গ্রামবাংলার আবহমান প্রকৃতি, নাটক ও ঐতিহ্যবাহী সংস্কৃতির মেলবন্ধন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BangladeshGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✨ GramFilm-এর বিশেষ সুবিধাসমূহ:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BangladeshGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• গ্রামবাংলার নৈসর্গিক সৌন্দর্য, নদী-নালা ও কৃষিজীবী মানুষের জীবনগাথা\n• নাটক, শর্ট ফিল্ম, বাউল গান ও লোকসংস্কৃতির বিশাল ভিডিও লাইব্রেরি\n• Niyon Islam Sohag ভাইয়ের তৈরি ৩টি গর্বের প্ল্যাটফর্ম: FotoFilm, GramFilm ও ProLevelCut-এর প্রত্যক্ষ সংযুক্তি",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "GramFilm প্ল্যাটফর্মে স্বাগতম! 🎬🌿", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("GramFilm চালু করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for FlowerRoseAppDialog -> GramFilmAppDialog
 */
@Composable
fun FlowerRoseAppDialog(
    onDismiss: () -> Unit
) {
    GramFilmAppDialog(onDismiss = onDismiss)
}

/**
 * Flower App 2 Dialog (শাপলা ও পদ্মফুল / Lotus Water Lily Showcase)
 */
@Composable
fun FlowerLotusAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ফুলের অ্যাপ ২", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00ACC1))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.flower_app_lotus),
                    contentDescription = "Lotus Flower App",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color(0xFF00ACC1), RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Shapla & Lotus 🪷",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "বাংলাদেশের জাতীয় ফুল শাপলা ও শান্ত পদ্ম পুকুর",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF00ACC1).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "• লাল, সাদা ও নীল শাপলার লাইভ ওয়ালপেপার ও রিল্যাক্সিং মিউজিক\n• পদ্মবিলের পানির শব্দ ও মেডিটেশন ব্যাকগ্রাউন্ড\n• প্রকৃতি ও পরিবেশ প্রেমীদের জন্য বিশেষ পরিবেশবান্ধব ফিচার",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Shapla & Lotus ফুল অ্যাপ সক্রিয় হয়েছে! 🪷", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))
                ) {
                    Text("অ্যাপটি ব্যবহার ও চালু করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
