package com.example.ui.components

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Facebook-style Offline Blocker Screen.
 * Shown whenever the phone has NO Mobile Data and NO Wi-Fi.
 * Ensures zero photos, videos, reels, stories, live streams, or posts are accessible offline,
 * and unlocks automatically as soon as Data or Wi-Fi is turned back on.
 */
@Composable
fun FacebookStyleOfflineScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var isRetrying by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.retryInternetConnection(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(context) {
        while (isActive) {
            delay(1500L)
            if (viewModel.retryInternetConnection(context)) {
                break
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("facebook_offline_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Facebook-style FotoFilm Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fotofilm_logo),
                            contentDescription = "FotoFilm Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Foto",
                                color = BangladeshGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Film",
                                color = BangladeshRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BangladeshRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, BangladeshRed.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularOff,
                                contentDescription = null,
                                tint = BangladeshRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "অফলাইন",
                                color = BangladeshRed,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Centered Offline Card Layout (matches Facebook's No Internet Connection screen)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 460.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Clean circular Wi-Fi / Data off illustration
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FacebookBlue.copy(alpha = 0.16f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "ইন্টারনেট সংযোগ নেই",
                                tint = FacebookBlue,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Exact requested message
                    Text(
                        text = "ইন্টারনেট সংযোগ নেই - ডাটা বা ওয়াই-ফাই চালু করুন",
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("offline_title_text")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "আপনার ফোনে মোবাইল ডাটা বা ওয়াই-ফাই সংযোগ বন্ধ আছে। ইন্টারনেট সংযোগ ছাড়া কোনো ছবি, ভিডিও, শর্ট ভিডিও, স্টোরি, লাইভ বা পোস্ট দেখা যাবে না।",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6-SIM Free Mode + Cloud info card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BangladeshGreen.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📶 ৬ সিম ফ্রি ও ডাটা মোড প্রস্তুত",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangladeshGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GP • Robi • Airtel • Banglalink • Teletalk • Skitto সিমের ডাটা বা ওয়াই-ফাই অন করলেই আসল পোস্ট, ছবি, ChatFilm, লুডু কিং গ্রুপ, ফিদা এআই ও Marketplace স্বয়ংক্রিয়ভাবে চালু হবে।",
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Retry Button (min 48dp accessible height)
                    Button(
                        onClick = {
                            if (isRetrying) return@Button
                            isRetrying = true
                            coroutineScope.launch {
                                delay(350L)
                                val connected = viewModel.retryInternetConnection(context)
                                isRetrying = false
                                if (!connected) {
                                    Toast.makeText(
                                        context,
                                        "ইন্টারনেট সংযোগ নেই - ডাটা বা ওয়াই-ফাই চালু করুন",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        enabled = !isRetrying,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FacebookBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("offline_retry_button")
                    ) {
                        if (isRetrying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "সংযোগ পরীক্ষা করা হচ্ছে...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পুনরায় চেষ্টা করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Button: Open System Internet/Wi-Fi Settings
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                                } else {
                                    Intent(Settings.ACTION_WIFI_SETTINGS)
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallback)
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        "ফোনের সেটিংস থেকে মোবাইল ডাটা বা ওয়াই-ফাই চালু করুন",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("offline_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = FacebookBlue,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডাটা বা ওয়াই-ফাই চালু করুন (সেটিংস)",
                            color = FacebookBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}
