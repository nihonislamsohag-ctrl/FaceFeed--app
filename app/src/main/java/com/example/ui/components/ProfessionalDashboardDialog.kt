package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.FacebookBlue

/**
 * Professional Dashboard Modal
 * Exactly matches Screenshot 2 from user:
 * - Header: Dashboard 4-box icon + "Professional Dashboard" + Close 'X'
 * - Performance (Last 28 days) with 4 metric cards:
 *     1. Post reach: 1.8M (▲ 42%)
 *     2. Post engagement: 340.5K (▲ 18%)
 *     3. Net followers: +50 (▲ 100%)
 *     4. 3-sec video views: 920K (▲ 35%)
 * - Creator Tools:
 *     1. Monetization & Stars ($ icon, "Eligible to earn from posts and reels")
 *     2. Profile recommendation (Star icon, "Your profile is recommended to new people")
 *     3. Audience insights (Chart icon, "Dhaka, Chittagong, Barisal & international fans")
 * - Blue "Done" button at bottom
 */
@Composable
fun ProfessionalDashboardDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Gray Drag Handle Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFCBD5E1))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Header Row: Dashboard 4-square icon, "Professional Dashboard", 'X' button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard",
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Professional Dashboard",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Performance (Last 28 days) Title
                    Text(
                        text = "Performance (Last 28 days)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Performance Cards (2x2 Grid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PerformanceMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Post reach",
                            value = "1.8M",
                            growth = "▲ 42%"
                        )
                        PerformanceMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Post engagement",
                            value = "340.5K",
                            growth = "▲ 18%"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PerformanceMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Net followers",
                            value = "+50",
                            growth = "▲ 100%"
                        )
                        PerformanceMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "3-sec video views",
                            value = "920K",
                            growth = "▲ 35%"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Creator Tools Section
                    Text(
                        text = "Creator Tools",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tool 1: Monetization & Stars
                    CreatorToolRow(
                        icon = Icons.Default.AttachMoney,
                        title = "Monetization & Stars",
                        subtitle = "Eligible to earn from posts and short videos"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tool 2: Profile recommendation
                    CreatorToolRow(
                        icon = Icons.Default.Stars,
                        title = "Profile recommendation",
                        subtitle = "Your profile is recommended to new people"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tool 3: Audience insights
                    CreatorToolRow(
                        icon = Icons.Default.ShowChart,
                        title = "Audience insights",
                        subtitle = "Dhaka, Chittagong, Barisal & international fans"
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Green Done Button (Matching Screenshot 2)
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333))
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    growth: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = growth,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )
        }
    }
}

@Composable
private fun CreatorToolRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Active tool click */ }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF137333),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
