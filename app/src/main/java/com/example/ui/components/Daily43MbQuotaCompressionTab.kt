package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DailyQuotaCompressionStore
import com.example.data.QuotaCompressionConfig
import com.example.data.UserDailyQuotaUsage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.ui.theme.FacebookBlue
import java.util.Locale

@Composable
fun AdminDaily43MbQuotaCompressionView(
    config: QuotaCompressionConfig,
    userUsages: List<UserDailyQuotaUsage>,
    onToggleAutoCompression: (Boolean) -> Unit,
    onToggleDailyLimit: (Boolean) -> Unit,
    onResetUserQuota: (String) -> Unit,
    onResetAllQuota: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsages = remember(userUsages, searchQuery) {
        if (searchQuery.isBlank()) userUsages
        else userUsages.filter {
            it.userName.contains(searchQuery, ignoreCase = true) ||
                it.userId.contains(searchQuery, ignoreCase = true)
        }
    }

    val allTimeFreeList = remember {
        listOf(
            "💬 ChatFilm (অল-টাইম ফ্রি)" to "চ্যাট লেখা, ছবি, ভিডিও, ভয়েস, অডিও কল, ভিডিও কল, গ্রুপ ভিডিও কল — সবসময় চালু",
            "🤖 ফিদা এআই (Fida AI)" to "যেকোনো প্রশ্ন, লেখা ও স্মার্ট সাহায্য — সবসময় ফ্রি ও চালু",
            "🎙️ বন্ধুদের সাথে আড্ডা" to "অডিও কল, ভিডিও কল, ভয়েস, গ্রুপ কল, চ্যাট লেখা ও ছবি — সবসময় ফ্রি",
            "➕ একে অপরকে ফলো দেওয়া" to "আনলিমিটেড ফলো/আনফলো ও ফ্রেন্ড রিকোয়েস্ট — সবসময় চালু",
            "🎲 লুডু কিং (সবাই সবার সাথে)" to "লাইভ লুডু খেলা, ইন-গেম চ্যাট লেখা ও ভয়েস কল — সবসময় ফ্রি",
            "👥 গ্রুপ, বিজনেস ও মার্কেট" to "গ্রুপ, ফটোফিল্ম বিজনেস পেজ ও ক্রয়-বিক্রয় মার্কেট — অল-টাইম চালু"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // 1. Hero Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BangladeshGreen,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = "অটো কম্প্রেশন",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "অটো কম্প্রেশন ও দৈনিক ৪৩.১ MB ট্র্যাকার",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "১০০ MB ভিডিও দিলেও অ্যাপ অটো ৩০ MB বানিয়ে আপলোড করবে • ChatFilm অল-টাইম ফ্রি",
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("মোট দৈনিক লিমিট", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                                Text("৪৩.১ MB / দিন", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("আজ সবার খরচ", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.US, "%.1f MB", config.totalDailySpentMbAllUsers),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("কম্প্রেশনে সাশ্রয়", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.US, "%.1f MB", config.totalCompressionSavedMbToday),
                                    color = Color(0xFFFFEB3B),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Admin Control Switches
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.25f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "অটো কম্প্রেশন ও কোটা নিয়ন্ত্রণ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FacebookBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🗜️ অটো ভিডিও ও ছবি কম্প্রেশন",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "১০০ MB ভিডিও → ৩০ MB, রিল → ৮ MB, স্টোরি → ৩ MB ও ছবি → ৩০০ KB করবে",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.isAutoCompressionEnabled,
                            onCheckedChange = onToggleAutoCompression,
                            modifier = Modifier.testTag("toggle_auto_compression_switch")
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📊 দৈনিক ৪৩.১ MB লিমিট নিয়ম",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "প্রতি ব্যবহারকারী প্রতিদিন ৩০ + ৮ + ১.২ + ৩.৯ = ৪৩.১ MB ফ্রি কোটা পাবেন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.isDaily43MbLimitEnabled,
                            onCheckedChange = onToggleDailyLimit,
                            modifier = Modifier.testTag("toggle_daily_43mb_limit_switch")
                        )
                    }
                }
            }
        }

        // 3. Exact 43.1 MB Quota Rule Table
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📐 দৈনিক ৪৩.১ MB ফ্রি কোটার হিসাব (অটো কম্প্রেসড)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF4E342E)
                    )
                    QuotaRuleRow("১. ১ টি লং ভিডিও (৩ মিনিট)", "৩০.০ MB", "১০০ MB দিলেও ৩০ MB")
                    QuotaRuleRow("২. ১ টি রিল ভিডিও (৩০ সেকেন্ড)", "৮.০ MB", "অটো ছোট করে আপলোড")
                    QuotaRuleRow("৩. ৪ টি ছবি (৪ × ৩০০ KB)", "১.২ MB", "প্রতিটি সর্বোচ্চ ৩০০ KB")
                    QuotaRuleRow("৪. স্টোরি মোট ৪ টি (৩ ছবি ০.৯ + ১ ভিডিও ১৫সে. ৩.০)", "৩.৯ MB", "মোট স্টোরি ৩.৯ MB")
                    HorizontalDivider(color = Color(0xFFFFCC80))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "মোট (৩০ + ৮ + ১.২ + ৩.৯)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "= ৪৩.১ MB / প্রতিদিন ✓",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }

        // 4. All-Time Free & Always Active Features
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, BangladeshGreen.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "অল-টাইম ফ্রি ও সবসময় চালু ফিচার (কোনো লিমিট নেই)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    allTimeFreeList.forEach { (title, subtitle) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                                Text(subtitle, fontSize = 10.5.sp, color = Color(0xFF2E7D32), lineHeight = 14.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BangladeshGreen
                            ) {
                                Text(
                                    text = "চালু ✓",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Daily User Data Usage Header & Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "মানুষের দৈনিক ৪৩.১ MB খরচ (${filteredUsages.size} জন)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = onResetAllQuota,
                        modifier = Modifier.testTag("reset_all_daily_quota_btn")
                    ) {
                        Text("সবার রিসেট ↻", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BangladeshRed)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ব্যবহারকারীর নাম দিয়ে খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quota_user_search_input")
                )
            }
        }

        // 6. Per-User Usage Tracker Cards
        items(filteredUsages, key = { it.userId }) { usage ->
            UserDailyUsageTrackerCard(
                usage = usage,
                onResetClick = { onResetUserQuota(usage.userId) }
            )
        }
    }
}

@Composable
private fun QuotaRuleRow(title: String, mbLabel: String, note: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3E2723))
            Text(note, fontSize = 10.5.sp, color = Color(0xFF6D4C41))
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFFFE082)
        ) {
            Text(
                text = mbLabel,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun UserDailyUsageTrackerCard(
    usage: UserDailyQuotaUsage,
    onResetClick: () -> Unit
) {
    val barColor = when {
        usage.usagePercent >= 0.9f -> BangladeshRed
        usage.usagePercent >= 0.6f -> Color(0xFFE65100)
        else -> BangladeshGreen
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = usage.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" },
                    contentDescription = usage.userName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usage.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "আজকের খরচ: %.1f / %.1f MB (বাকি %.1f MB)",
                            usage.totalUsedMb,
                            DailyQuotaCompressionStore.DAILY_TOTAL_LIMIT_MB,
                            usage.remainingMb
                        ),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = barColor
                    )
                }
                OutlinedButton(
                    onClick = onResetClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reset_user_quota_${usage.userId}")
                ) {
                    Text("রিসেট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = { usage.usagePercent },
                color = barColor,
                trackColor = barColor.copy(alpha = 0.14f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Detailed MB breakdown for 43.1 MB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UsagePill(
                    label = String.format(Locale.US, "🎬 লং: %d/1 (%.1fMB)", usage.longVideoCount, usage.longVideoMb),
                    modifier = Modifier.weight(1f)
                )
                UsagePill(
                    label = String.format(Locale.US, "📱 রিল: %d/1 (%.1fMB)", usage.reelCount, usage.reelMb),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UsagePill(
                    label = String.format(Locale.US, "🖼️ ছবি: %d/4 (%.0fKB)", usage.photoCount, usage.photoKb),
                    modifier = Modifier.weight(1f)
                )
                UsagePill(
                    label = String.format(
                        Locale.US,
                        "🌟 স্টোরি: %dছ+%dভি (%.1fMB)",
                        usage.storyPhotoCount,
                        usage.storyVideoCount,
                        usage.storyTotalMb
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (usage.savedByCompressionMb > 0.0 || usage.chatFilmFreeMediaCount > 0) {
                Text(
                    text = String.format(
                        Locale.US,
                        "🗜️ অটো কম্প্রেশনে সাশ্রয়: %.1f MB • 💬 ChatFilm ফ্রি মিডিয়া: %d টি (কোটা ফ্রি ✓)",
                        usage.savedByCompressionMb,
                        usage.chatFilmFreeMediaCount
                    ),
                    fontSize = 10.5.sp,
                    color = BangladeshGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UsagePill(label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}
