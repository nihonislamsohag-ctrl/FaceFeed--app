package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.*
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

private data class PaymentMethodOption(
    val id: String,
    val label: String,
    val logoRes: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostPostDialog(
    post: Post,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Admin Numbers from ViewModel (User updated: bKash/Nagad 01842450605, Rocket 01734249971)
    val adminBkash by viewModel.adminBkashNumber.collectAsState()
    val adminNagad by viewModel.adminNagadNumber.collectAsState()
    val adminRocket by viewModel.adminRocketNumber.collectAsState()

    val boostPackages = remember {
        listOf(
            BoostPackage(
                id = "pkg_200",
                nameBn = "১০K লাইক স্পেশাল (৳২০০) 🚀",
                nameEn = "10K Likes Special",
                durationDays = 2,
                priceBdt = 200,
                estimatedReach = "৫০,০০০ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 10000,
                followersCount = 3000,
                viewsCount = 50000,
                hasTenReactions = true,
                isPopular = false,
                badge = "১০K লাইক • ৩K ফলোয়ার"
            ),
            BoostPackage(
                id = "pkg_300",
                nameBn = "২০K লাইক মেগা (৳৩০০) 🔥",
                nameEn = "20K Likes Mega",
                durationDays = 3,
                priceBdt = 300,
                estimatedReach = "১,০০,০০০ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 20000,
                followersCount = 5000,
                viewsCount = 100000,
                hasTenReactions = true,
                isPopular = true,
                badge = "২০K লাইক • ৫K ফলোয়ার"
            ),
            BoostPackage(
                id = "pkg_500",
                nameBn = "২৫K লাইক আল্ট্রা (৳৫০০) ⚡",
                nameEn = "25K Likes Ultra",
                durationDays = 7,
                priceBdt = 500,
                estimatedReach = "১,৫০,০০০ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 25000,
                followersCount = 7000,
                viewsCount = 150000,
                hasTenReactions = true,
                isPopular = false,
                badge = "২৫K লাইক • ৭K ফলোয়ার"
            ),
            BoostPackage(
                id = "pkg_1000",
                nameBn = "৩০K লাইক ভিআইপি (৳১০০০) 👑",
                nameEn = "30K Likes VIP",
                durationDays = 15,
                priceBdt = 1000,
                estimatedReach = "২,০০,০০০+ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 30000,
                followersCount = 10000,
                viewsCount = 200000,
                hasTenReactions = true,
                isPopular = false,
                badge = "৩০K লাইক • ১০K ফলোয়ার"
            ),
            BoostPackage(
                id = "pkg_2000",
                nameBn = "৪০K লাইক সুপার ভিআইপি (৳২০০০) 💎",
                nameEn = "40K Likes Super VIP",
                durationDays = 30,
                priceBdt = 2000,
                estimatedReach = "৩,৫০,০০০+ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 40000,
                followersCount = 25000,
                viewsCount = 350000,
                hasTenReactions = true,
                isPopular = false,
                badge = "৪০K লাইক • ২৫K ফলোয়ার"
            ),
            BoostPackage(
                id = "pkg_3000",
                nameBn = "৫০K লাইক গোল্ডেন এম্পায়ার (৳৩০০০) 🏆",
                nameEn = "50K Likes Golden Empire",
                durationDays = 45,
                priceBdt = 3000,
                estimatedReach = "৫,০০,০০০+ মানুষের ফিডে বিজ্ঞাপন",
                likesCount = 50000,
                followersCount = 40000,
                viewsCount = 500000,
                hasTenReactions = true,
                isPopular = false,
                badge = "৫০K লাইক • ৪০K ফলোয়ার"
            )
        )
    }

    var selectedGoal by remember { mutableStateOf(BoostGoal.MORE_REACH) }
    var selectedPackage by remember { mutableStateOf(boostPackages[1]) } // Defaults to ৳300
    var isCustomBudget by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("300") }
    var customDays by remember { mutableStateOf("3") }

    var selectedMethod by remember { mutableStateOf("bKash") } // "bKash", "Nagad", "Rocket"
    var senderNumber by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var ownerFreeBoostSuccessPkg by remember { mutableStateOf<BoostPackage?>(null) }

    val currentUser by viewModel.currentUser.collectAsState()
    val isOwner = currentUser.id == "user_me" || currentUser.id == "user_niyon" || currentUser.fullName.contains("Niyon", ignoreCase = true)
    var selectedFreeReach by remember { mutableStateOf("১.৫ মিলিয়ন (1.5M Reach)") }

    val currentPrice = if (isCustomBudget) (customAmount.toIntOrNull() ?: 300) else selectedPackage.priceBdt
    val currentDays = if (isCustomBudget) (customDays.toIntOrNull() ?: 3) else selectedPackage.durationDays
    val currentReach = if (isCustomBudget) "${currentPrice * 80} - ${currentPrice * 120} মানুষ" else selectedPackage.estimatedReach

    val activeAdminNumber = when (selectedMethod) {
        "Nagad" -> adminNagad
        "Rocket" -> adminRocket
        else -> adminBkash
    }

    val methodColor = when (selectedMethod) {
        "bKash" -> Color(0xFFE2136E)
        "Nagad" -> Color(0xFFF7941D)
        "Rocket" -> Color(0xFF8C3494)
        else -> BangladeshGreen
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = {
            showSuccessDialog = false
            onDismiss()
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BangladeshGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚀", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "বুস্ট সফলভাবে সক্রিয় হয়েছে! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "আপনার ${if (post.isVideo) "ভিডিওটি" else "পোস্টটি"} স্পন্সরড বিজ্ঞাপনে যুক্ত করা হয়েছে। এটি পরবর্তী $currentDays দিন প্রায় $currentReach-এর ফিডে সবার উপরে দেখানো হবে।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পরিশোধিত ফি:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳$currentPrice BDT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BangladeshGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পেমেন্ট মেথড:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(selectedMethod, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TrxID:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(trxId.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                    ) {
                        Text("ঠিক আছে (Done)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Owner Free Boost Celebration Dialog (Niyon Islam Sohag)
    if (ownerFreeBoostSuccessPkg != null) {
        val freePkg = ownerFreeBoostSuccessPkg!!
        Dialog(onDismissRequest = {
            ownerFreeBoostSuccessPkg = null
            onDismiss()
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "মালিক স্পেশাল ফ্রী বুস্ট সক্রিয় হয়েছে! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFAD1457),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "প্রধান মালিক Niyon Islam Sohag এর জন্য '${freePkg.nameBn}' ১০০% ফ্রীতে সফলভাবে সক্রিয় করা হয়েছে! রিয়েল লাইক, ফলোয়ার ও ভিউ অবিলম্বে পোস্ট ও অ্যাকাউন্টে যোগ করা হয়েছে।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFCE4EC),
                        border = BorderStroke(1.dp, Color(0xFFF48FB1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("রিয়েল লাইক (১০টি রিয়্যাকশন):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF880E4F))
                                Text("+${freePkg.likesCount} লাইক 👍", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FacebookBlue)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("প্রোফাইল ফলোয়ার:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF880E4F))
                                Text("+${freePkg.followersCount} ফলোয়ার 👥", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ভিডিও/পোস্ট ভিউ:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF880E4F))
                                Text("+${freePkg.viewsCount} ভিউ 👁️", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BangladeshGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পরিশোধিত ফি:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳০ (১০০% ফ্রী মালিক স্পেশাল)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAD1457))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            ownerFreeBoostSuccessPkg = null
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("ধন্যবাদ (Done)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        return
    }

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
                // Header Bar with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF00897B))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (post.isVideo) "ভিডিও বুস্ট ও প্রোমোট" else "পোস্ট বুস্ট ও প্রোমোট",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "লাখো মানুষের কাছে পৌঁছানোর বিজ্ঞাপন",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview of the Post/Video being boosted
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FacebookBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (post.isVideo) "🎥" else "📝", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = post.title?.takeIf { it.isNotBlank() }
                                        ?: post.videoTitle?.takeIf { it.isNotBlank() }
                                        ?: post.text.take(60),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ক্রিয়েটর: ${post.author.fullName} • বর্তমান রিচ: ${post.reachViews}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FacebookBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "স্পন্সরড হবে",
                                    color = FacebookBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // =========================================================
                    // প্রধান মালিক (Niyon Islam Sohag) এর ১০০% ফ্রী মেগা বুস্ট (১M+ রিচ)
                    // শুধুমাত্র Niyon Islam Sohag ফ্রীতে নিজের যেকোনো পোস্ট বুস্ট করতে পারেন!
                    // অন্য কোনো লোক ফ্রীতে টাকা ছাড়া বুস্ট করতে পারবে না।
                    // =========================================================
                    if (isOwner) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFCE4EC),
                            border = BorderStroke(2.dp, Color(0xFFE91E63)),
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("👑", fontSize = 26.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "প্রধান মালিক ভিআইপি ফ্রী বুস্ট",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFAD1457)
                                            )
                                            Text(
                                                text = "Niyon Islam Sohag (১০০% ফ্রী • ৳০ খরচ)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFC2185B)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFE91E63),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "১০০% ফ্রী ✓",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "আপনি আপনার নিজের এই পোস্টটি সম্পূর্ণ ফ্রীতে ১ মিলিয়ন থেকে ১০ মিলিয়ন মানুষের মাঝে বুস্ট করতে পারেন! আপনার পোস্ট সারা দেশের লাখো কোটি মানুষের ফিডে পৌঁছে যাবে। অন্য কোনো লোক ফ্রীতে বুস্ট করতে পারবে না।",
                                    fontSize = 12.sp,
                                    color = Color(0xFF880E4F),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "মিলিয়ন রিচ টার্গেট নির্বাচন করুন:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFAD1457)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val ownerReachOptions = listOf(
                                    "১.২ মিলিয়ন (1.2M Reach) 🚀",
                                    "২.৫ মিলিয়ন (2.5M Reach) 🔥",
                                    "৫.০ মিলিয়ন (5.0M Reach) 🌟",
                                    "১০.০ মিলিয়ন (10M Mega Viral) 💎"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ownerReachOptions.forEach { reachOpt ->
                                        val isSel = selectedFreeReach == reachOpt
                                        Surface(
                                            onClick = { selectedFreeReach = reachOpt },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) Color(0xFFE91E63) else Color.White,
                                            border = BorderStroke(1.dp, if (isSel) Color(0xFFE91E63) else Color(0xFFF48FB1))
                                        ) {
                                            Text(
                                                text = reachOpt,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else Color(0xFFAD1457),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val safetyCheck = com.example.util.SafetyPolicyValidator.validateContent(post.text + " " + (post.title ?: "") + " " + (post.videoTitle ?: ""))
                                        if (!safetyCheck.isAllowed) {
                                            errorMessage = safetyCheck.reasonMessage
                                            return@Button
                                        }
                                        viewModel.ownerFreeMegaBoost(post.id, selectedFreeReach)
                                        com.example.util.AppSoundManager.playReactionChime()
                                        showSuccessDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🚀 ১ ক্লিকে ফ্রীতে মেগা বুস্ট চালু করুন (৳০)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFE91E63).copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    // Section 1: লক্ষ্য নির্বাচন (Goal)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "১. বুস্টের লক্ষ্য নির্বাচন করুন (Goal)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        BoostGoal.values().forEach { goal ->
                            val isSelected = selectedGoal == goal
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FacebookBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGoal = goal }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedGoal = goal },
                                        colors = RadioButtonDefaults.colors(selectedColor = FacebookBlue)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(goal.icon, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(goal.titleBn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Text(goal.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: বাজেট ও মেয়াদ (Budget & Duration)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "২. বাজেট ও সময়সীমা (প্যাকেজ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            TextButton(onClick = { isCustomBudget = !isCustomBudget }) {
                                Text(
                                    if (isCustomBudget) "প্যাকেজ দেখুন" else "কাস্টম বাজেট ✏️",
                                    fontSize = 12.sp,
                                    color = FacebookBlue
                                )
                            }
                        }

                        if (!isCustomBudget) {
                            boostPackages.forEach { pkg ->
                                val isSelected = selectedPackage.id == pkg.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BangladeshGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) BangladeshGreen else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPackage = pkg }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedPackage = pkg },
                                                    colors = RadioButtonDefaults.colors(selectedColor = BangladeshGreen)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(pkg.nameBn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        if (pkg.badge != null) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFFE65100)
                                                            ) {
                                                                Text(
                                                                    text = pkg.badge,
                                                                    color = Color.White,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "মেয়াদ: ${pkg.durationDays} দিন • আনুমানিক রিচ: ${pkg.estimatedReach}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "৳${pkg.priceBdt}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = BangladeshGreen
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Real Engagement Details Banner (লাইক, ১০টি রিয়্যাকশন, ফলোয়ার, ভিউ)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "👍 ${pkg.likesCount / 1000}K লাইক (১০ রিয়্যাকশন)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FacebookBlue
                                                )
                                                Text("•", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    text = "👥 ${pkg.followersCount / 1000}K ফলোয়ার",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE91E63)
                                                )
                                                Text("•", fontSize = 11.sp, color = Color.Gray)
                                                Text(
                                                    text = "👁️ ${pkg.viewsCount / 1000}K ভিউ",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BangladeshGreen
                                                )
                                            }
                                        }

                                        // Niyon Islam Sohag 1-Click Free Boost Button on Each Package
                                        if (isOwner) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val safetyCheck = com.example.util.SafetyPolicyValidator.validateContent(
                                                        post.text + " " + (post.title ?: "") + " " + (post.videoTitle ?: "")
                                                    )
                                                    if (!safetyCheck.isAllowed) {
                                                        errorMessage = safetyCheck.reasonMessage
                                                        return@Button
                                                    }
                                                    viewModel.ownerFreeBoostPackage(post.id, pkg)
                                                    com.example.util.AppSoundManager.playReactionChime()
                                                    ownerFreeBoostSuccessPkg = pkg
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RocketLaunch,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "👑 Niyon Islam Sohag: এক ক্লিকে ফ্রীতে বুস্ট করুন (৳০)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Custom Budget inputs
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("আপনার বাজেট ও দিন লিখুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = customAmount,
                                            onValueChange = { customAmount = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("বাজেট (টাকা ৳)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = customDays,
                                            onValueChange = { customDays = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("দিন (দিন সংখ্যা)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                    Text(
                                        text = "সম্ভাব্য রিচ: $currentReach",
                                        fontSize = 12.sp,
                                        color = BangladeshGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Section 3: পেমেন্ট মেথড ও অ্যাডমিনের বিকাশ/নগদ নম্বর (Payment Details)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "৩. পেমেন্ট করুন ও TrxID দিন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Selector: Real Authentic Logo Images for bKash, Nagad, Rocket
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                PaymentMethodOption("bKash", "বিকাশ", R.drawable.img_bkash_logo, Color(0xFFE2136E)),
                                PaymentMethodOption("Nagad", "নগদ", R.drawable.img_nagad_logo, Color(0xFFF7941D)),
                                PaymentMethodOption("Rocket", "রকেট", R.drawable.img_rocket_logo, Color(0xFF8C3494))
                            ).forEach { item ->
                                val isSelected = selectedMethod == item.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) item.color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) item.color else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMethod = item.id },
                                    shadowElevation = if (isSelected) 2.dp else 0.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.logoRes),
                                            contentDescription = item.label,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.label,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = item.color
                                            ) {
                                                Text(
                                                    text = "সিলেক্টেড ✓",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Admin Number Card with Copy Button & Selected Logo
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = methodColor.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, methodColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(
                                                id = when (selectedMethod) {
                                                    "Nagad" -> R.drawable.img_nagad_logo
                                                    "Rocket" -> R.drawable.img_rocket_logo
                                                    else -> R.drawable.img_bkash_logo
                                                }
                                            ),
                                            contentDescription = selectedMethod,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "অ্যাডমিনের $selectedMethod নম্বর:",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = activeAdminNumber,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = methodColor
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(activeAdminNumber))
                                            Toast.makeText(context, "$selectedMethod নম্বর কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = methodColor),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("কপি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "💡 নির্দেশনা: আপনার $selectedMethod অ্যাপে যান এবং উপরে দেওয়া নম্বরে ৳$currentPrice টাকা Send Money করুন। টাকা পাঠানোর পর প্রাপ্ত TrxID নিচে লিখুন।",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Inputs: Sender Number & TrxID
                        OutlinedTextField(
                            value = senderNumber,
                            onValueChange = { senderNumber = it },
                            label = { Text("আপনার প্রেরক নম্বর (Sender Mobile)") },
                            placeholder = { Text("01XXXXXXXXX") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = trxId,
                            onValueChange = { trxId = it },
                            label = { Text("TrxID (ট্রানজেকশন আইডি)") },
                            placeholder = { Text("যেমন: BKH8736123 বা NGD78129") },
                            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bottom Action Button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Button(
                            onClick = {
                                if (senderNumber.isBlank() || senderNumber.length < 10) {
                                    errorMessage = "দয়া করে আপনার সঠিক মোবাইল নম্বরটি লিখুন"
                                    return@Button
                                }
                                if (trxId.isBlank() || trxId.length < 4) {
                                    errorMessage = "দয়া করে পেমেন্টের সঠিক TrxID লিখুন"
                                    return@Button
                                }
                                errorMessage = null

                                // Execute Boost in ViewModel
                                viewModel.boostPost(
                                    postId = post.id,
                                    amountBdt = currentPrice,
                                    durationDays = currentDays,
                                    targetReach = currentReach,
                                    paymentMethod = selectedMethod,
                                    senderNumber = senderNumber,
                                    trxId = trxId,
                                    goal = selectedGoal.titleBn,
                                    likesToAdd = selectedPackage.likesCount,
                                    followersToAdd = selectedPackage.followersCount,
                                    viewsToAdd = selectedPackage.viewsCount
                                )

                                showSuccessDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚀", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "৳$currentPrice পরিশোধ করে বুস্ট চালু করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
