package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun AppDownloadWebDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()

    // Official Web Application URL
    val webAppUrl = "https://ais-pre-uxxbqekxfrv4arrv2ck4ma-461492489791.asia-southeast1.run.app"
    val shareText = "🔥 FotoFilm - সোশ্যাল মিডিয়া অ্যাপ!\nক্রোম ব্রাউজার ও গুগলে সরাসরি ব্যবহার করুন অথবা অ্যাপ ডাউনলোড করুন:\n$webAppUrl\n\nপ্রতিষ্ঠাতা: Niyon Islam Sohag"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(FacebookBlue, Color(0xFF00C6FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "গুগল, ক্রোম ও অ্যাপ হাব",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ক্রোম ও গুগলে ব্রাউজ ও সরাসরি ডাউনলোড",
                                fontSize = 11.sp,
                                color = FacebookBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Card: Live Web Access & Browser Information
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FacebookBlue.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FacebookBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "লাইভ ওয়েব লিংক (ক্রোম ও গুগল সার্চ):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FacebookBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(webAppUrl))
                                    Toast.makeText(context, "ওয়েব লিংক ক্লিপবোর্ডে কপি হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = webAppUrl,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button 1: Open in Chrome Browser
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webAppUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "ব্রাউজার খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ক্রোম ব্রাউজারে চালু করুন (Chrome Browser)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 2: Download APK directly
                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webAppUrl))
                            context.startActivity(intent)
                            Toast.makeText(context, "সরাসরি অ্যাপ ডাউনলোড পেজ খোলা হচ্ছে... 📥", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "ব্রাউজার লিঙ্ক খোলা যায়নি", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = BangladeshGreen.copy(alpha = 0.15f),
                        contentColor = BangladeshGreen
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = BangladeshGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সরাসরি অ্যাপ ডাউনলোড করুন (Download APK)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BangladeshGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 3: Share with Friends (WhatsApp / Messenger / Facebook)
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "FotoFilm অ্যাপ লিংক শেয়ার করুন")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "বন্ধুদের মাঝে অ্যাপ লিংক শেয়ার করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instruction Box: Google Search & Chrome Usage
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "গুগলে ও ক্রোমে সহজে পাওয়ার নিয়ম:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "১. ক্রোম ব্রাউজার বা গুগলে সার্চবারে এই লিংকটি লিখলেই যে কেউ সরাসরি FotoFilm-এ প্রবেশ করতে পারবে।\n" +
                                    "২. ব্রাউজারে অ্যাপ চলাকালে ৩-ডট মেন্যু থেকে 'Install App' বা 'Add to Home screen' চাপলেই হোমস্ক্রিনে অ্যাপ ইনস্টল হয়ে যাবে।\n" +
                                    "৩. কোনো ডাউনলোড ছাড়াই মোবাইল বা কম্পিউটার থেকে ফুল স্পিডে ব্যবহার করা যাবে!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Founder Credit
                Text(
                    text = "অ্যাপের প্রধান প্রতিষ্ঠাতা ও পরিচালক: Niyon Islam Sohag 👑",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FacebookBlue,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
