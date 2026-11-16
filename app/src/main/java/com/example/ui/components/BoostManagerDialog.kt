package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.BoostTransaction
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostManagerDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val boostTransactions by viewModel.boostTransactions.collectAsState()
    val adminBkash by viewModel.adminBkashNumber.collectAsState()
    val adminNagad by viewModel.adminNagadNumber.collectAsState()
    val adminRocket by viewModel.adminRocketNumber.collectAsState()

    var showEditNumbers by remember { mutableStateOf(false) }
    var editBkash by remember { mutableStateOf(adminBkash) }
    var editNagad by remember { mutableStateOf(adminNagad) }
    var editRocket by remember { mutableStateOf(adminRocket) }

    val totalRevenue = boostTransactions.sumOf { it.amountBdt }
    val activeCampaigns = boostTransactions.count { it.status == "ACTIVE" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF00695C))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💼", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "বুস্ট ও বিজ্ঞাপন ম্যানেজার",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "আপনার অ্যাপের বুস্ট আয় ও বিজ্ঞাপন নিয়ন্ত্রণ",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Stats Dashboard Banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট বুস্ট আয় 💰", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "৳$totalRevenue BDT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BangladeshGreen
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সক্রিয় বিজ্ঞাপন 🚀", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$activeCampaigns টি",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FacebookBlue
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Admin Payment Numbers Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💳", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "আপনার পেমেন্ট গ্রহণ নম্বর (বিকাশ/নগদ)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    TextButton(onClick = { showEditNumbers = !showEditNumbers }) {
                                        Text(if (showEditNumbers) "বন্ধ করুন" else "পরিবর্তন ✏️", fontSize = 12.sp, color = FacebookBlue)
                                    }
                                }

                                if (!showEditNumbers) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE2136E).copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, Color(0xFFE2136E).copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_bkash_logo),
                                                    contentDescription = "bKash",
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("বিকাশ", fontSize = 11.sp, color = Color(0xFFE2136E), fontWeight = FontWeight.Bold)
                                                Text(adminBkash, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF7941D).copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, Color(0xFFF7941D).copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_nagad_logo),
                                                    contentDescription = "Nagad",
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("নগদ", fontSize = 11.sp, color = Color(0xFFF7941D), fontWeight = FontWeight.Bold)
                                                Text(adminNagad, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF8C3494).copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, Color(0xFF8C3494).copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_rocket_logo),
                                                    contentDescription = "Rocket",
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("রকেট", fontSize = 11.sp, color = Color(0xFF8C3494), fontWeight = FontWeight.Bold)
                                                Text(adminRocket, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            }
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = editBkash,
                                            onValueChange = { editBkash = it },
                                            label = { Text("বিকাশ নম্বর (bKash)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = editNagad,
                                            onValueChange = { editNagad = it },
                                            label = { Text("নগদ নম্বর (Nagad)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = editRocket,
                                            onValueChange = { editRocket = it },
                                            label = { Text("রকেট নম্বর (Rocket)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.updateAdminPaymentNumbers(editBkash, editNagad, editRocket)
                                                showEditNumbers = false
                                                Toast.makeText(context, "নম্বর সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                                        ) {
                                            Text("সংরক্ষণ করুন (Save Numbers)", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Boost Transactions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📋", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বুস্ট ট্রানজেকশন ও বিজ্ঞাপন তালিকা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (boostTransactions.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🚀", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "এখনো কোনো বুস্ট রিকোয়েস্ট নেই",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "যেকোনো পোস্টের নিচে 'বুস্ট করুন' চাপলে এখানে প্রদর্শিত হবে।",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(boostTransactions) { tx ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (tx.isVideo) "🎥" else "📝", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tx.postTitleOrText.take(24) + "...",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (tx.status == "ACTIVE") BangladeshGreen else Color.Gray
                                        ) {
                                            Text(
                                                text = if (tx.status == "ACTIVE") "সক্রিয় 🟢" else "সম্পন্ন",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("গ্রাহক: ${tx.authorName} (${tx.senderNumber})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("৳${tx.amountBdt}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BangladeshGreen)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("পেমেন্ট: ${tx.paymentMethod} • TrxID: ${tx.trxId}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = FacebookBlue)
                                        Text("${tx.durationDays} দিন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("টার্গেট রিচ: ${tx.estimatedReach} • লক্ষ্য: ${tx.goal}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (tx.status == "PENDING") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.updateBoostStatus(tx.id, "ACTIVE")
                                                Toast.makeText(context, "বুস্ট অনুমোদন ও রিয়েল এনগেজমেন্ট যুক্ত হয়েছে! 🚀", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("অনুমোদন ও এনগেজমেন্ট চালু করুন (Approve) ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Close
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Text("বন্ধ করুন (Close)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
