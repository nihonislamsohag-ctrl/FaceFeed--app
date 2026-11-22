package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidaVerifiedDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val isVerified by viewModel.isFidaVerified.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()
    val paidMonths by viewModel.paidMonths.collectAsState()
    val nidFrontUri by viewModel.nidFrontUri.collectAsState()
    val nidBackUri by viewModel.nidBackUri.collectAsState()
    val isNidSubmitted by viewModel.isNidSubmitted.collectAsState()
    val nidNumber by viewModel.nidNumber.collectAsState()
    val nidApplicantName by viewModel.nidApplicantName.collectAsState()

    val isNiyon = viewModel.isCurrentUserNiyonIslamSohag()

    // Dialog states
    var selectedMonthForPayment by remember { mutableStateOf<String?>(null) }
    var showPaymentSuccessDialog by remember { mutableStateOf<String?>(null) }

    // NID editing state
    var editingNidNumber by remember { mutableStateOf(nidNumber) }
    var editingApplicantName by remember { mutableStateOf(nidApplicantName) }
    var showNidUploadSheet by remember { mutableStateOf(false) }
    var currentNidTarget by remember { mutableStateOf<String?>(null) } // "front" or "back"

    // 12 Months in English
    val monthsList = remember {
        listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header with Back Button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Fida Verified",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Blue Scalloped Verified Icon Emblem
                    FidaVerifiedBadge(size = 84.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Fida Verified",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isVerified) BangladeshGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isVerified) BangladeshGreen else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isVerified) {
                                FidaVerifiedBadge(size = 32.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isVerified) "ভেরিফিকেশন সক্রিয় আছে ✅" else "ভেরিফিকেশন নিষ্ক্রিয়",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isVerified) BangladeshGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isVerified) "${currentUser.fullName}-এর প্রোফাইলে সবুজ-হলুদ ভেরিফাই ব্যাজ দৃশ্যমান" else "ভেরিফাইড ব্যাজ চালু করতে সাবস্ক্রাইব করুন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isVerified,
                                onCheckedChange = { viewModel.toggleFidaVerified() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BangladeshGreen
                                )
                            )
                        }
                    }

                    // =========================================================================
                    // 2. জাতীয় পরিচয় পত্র (NID Card) ডকুমেন্টস যাচাইকরণ সেকশন
                    // =========================================================================
                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = BangladeshGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "জাতীয় পরিচয়পত্র (NID) যাচাইকরণ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Fida Verified পেতে ১ম ও ২য় পিঠের ছবি সাবমিট আবশ্যক",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Side-by-side NID Photos (১ম পিঠ ও ২য় পিঠ)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Front Side Photo Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            currentNidTarget = "front"
                                            showNidUploadSheet = true
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.LightGray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (nidFrontUri != null) {
                                                AsyncImage(
                                                    model = nidFrontUri,
                                                    contentDescription = "NID Front",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                // Hologram badge overlay
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = "১ম পিঠ",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = FacebookBlue)
                                                    Text("ছবি যোগ করুন", fontSize = 10.sp, color = FacebookBlue)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "জাতীয় পরিচয়পত্র (১ম পিঠ)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (nidFrontUri != null) "ছবি সংযুক্ত ✅" else "ছবি তুলুন বা আপলোড",
                                            fontSize = 10.sp,
                                            color = if (nidFrontUri != null) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Back Side Photo Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            currentNidTarget = "back"
                                            showNidUploadSheet = true
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.LightGray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (nidBackUri != null) {
                                                AsyncImage(
                                                    model = nidBackUri,
                                                    contentDescription = "NID Back",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = "২য় পিঠ",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = FacebookBlue)
                                                    Text("ছবি যোগ করুন", fontSize = 10.sp, color = FacebookBlue)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "জাতীয় পরিচয়পত্র (২য় পিঠ)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (nidBackUri != null) "ছবি সংযুক্ত ✅" else "ছবি তুলুন বা আপলোড",
                                            fontSize = 10.sp,
                                            color = if (nidBackUri != null) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // NID Number & Name Inputs
                            OutlinedTextField(
                                value = editingNidNumber,
                                onValueChange = { editingNidNumber = it },
                                label = { Text("জাতীয় পরিচয়পত্র নম্বর (NID Number)", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editingApplicantName,
                                onValueChange = { editingApplicantName = it },
                                label = { Text("আবেদনকারীর পূর্ণ নাম (NID অনুযায়ী)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isNidSubmitted) BangladeshGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isNidSubmitted) Icons.Default.CheckCircle else Icons.Default.Pending,
                                            contentDescription = null,
                                            tint = if (isNidSubmitted) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isNidSubmitted) "NID যাচাই সম্পন্ন ✅" else "অপেক্ষমাণ",
                                            fontSize = 11.sp,
                                            color = if (isNidSubmitted) BangladeshGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.submitNid(editingNidNumber, editingApplicantName)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("ডকুমেন্ট জমা দিন", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    // =========================================================================
                    // 3. ১২ মাসের ইংরেজি সূচি ও মাসিক ৫০০ টাকা ফি (bKash ও Nagad পেমেন্ট)
                    // =========================================================================
                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Fida Verified মাসিক সাবস্ক্রিপশন সূচি",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "মাসিক ৫০০ টাকা Niyon Islam Sohag-এর বিকাশ (01842450605) বা নগদ (01842450605)-এ জমা হবে",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Official Payment Numbers Display Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // bKash Number
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFE2136E),
                                            modifier = Modifier.size(10.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("বিকাশ (bKash)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2136E))
                                            Text("01842450605", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                    VerticalDivider(modifier = Modifier.height(28.dp))
                                    // Nagad Number
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFF7941D),
                                            modifier = Modifier.size(10.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("নগদ (Nagad)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF7941D))
                                            Text("01842450605", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 12 English Months Grid (2 columns or styled rows)
                            monthsList.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { month ->
                                        val isPaid = paidMonths.contains(month)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isPaid) FacebookBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isPaid) FacebookBlue else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = month,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (isPaid) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = "Paid",
                                                            tint = FacebookBlue,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // 500 BDT Fee Display
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "৳ 500",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        color = FacebookBlue
                                                    )

                                                    if (isPaid) {
                                                        Text(
                                                            text = "পরিশোধিত ✅",
                                                            fontSize = 10.sp,
                                                            color = FacebookBlue,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Button(
                                                            onClick = { selectedMonthForPayment = month },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2136E)), // bKash Pink
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("500 দিন", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================================
                    // Fida Verified সুবিধাসমূহ (Benefits)
                    // =========================================================================
                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Fida Verified সুবিধাসমূহ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BenefitItem(
                        icon = Icons.Default.Verified,
                        iconTint = BangladeshGreen,
                        title = "অফিশিয়াল সবুজ-হলুদ ভেরিফাইড ব্যাজ",
                        description = "আপনার প্রোফাইল নামের পাশে অফিশিয়াল সবুজ-হলুদ ভেরিফাইড ব্যাজ থাকবে।"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BenefitItem(
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF00B2FE),
                        title = "অ্যাকাউন্ট সর্বোচ্চ সুরক্ষা",
                        description = "ক্লোনিং বা ফেক আইডির হাত থেকে আপনার আইডি নিরাপদ রাখতে সার্বক্ষণিক প্রটেকশন।"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BenefitItem(
                        icon = Icons.Default.HeadsetMic,
                        iconTint = Color(0xFFFF9800),
                        title = "সরাসরি FotoFilm সাপোর্ট",
                        description = "যেকোনো কারিগরি সাহায্য বা সমস্যার দ্রুত সমাধানের জন্য ডেডিকেটেড সাপোর্ট দল।"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Close button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                    ) {
                        Text("সম্পন্ন", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // =========================================================================
    // DIALOG 1: bKash / Nagad Interactive Payment Gateway Dialog
    // =========================================================================
    selectedMonthForPayment?.let { month ->
        BkashNagadPaymentDialog(
            month = month,
            amount = 500,
            recipientName = "Niyon Islam Sohag",
            onDismiss = { selectedMonthForPayment = null },
            onPaymentComplete = { method, pin ->
                viewModel.payMonthSubscription(month, method, pin)
                selectedMonthForPayment = null
                showPaymentSuccessDialog = "$method-এর মাধ্যমে ৳৫০০ টাকা সফলভাবে Niyon Islam Sohag-এর ফোনে জমা হয়েছে! $month মাসের ভেরিফিকেশন সক্রিয় হয়েছে।"
            }
        )
    }

    // Success dialog after bKash/Nagad payment
    showPaymentSuccessDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BangladeshGreen,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "পেমেন্ট সফল ও ভেরিফাইড সক্রিয়!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(text = msg, fontSize = 13.sp, textAlign = TextAlign.Center)
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentSuccessDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text("ঠিক আছে", color = Color.White)
                }
            }
        )
    }

    // =========================================================================
    // DIALOG 3: NID Photo Upload / Camera Selector Dialog
    // =========================================================================
    if (showNidUploadSheet && currentNidTarget != null) {
        val nidPhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                if (currentNidTarget == "front") {
                    viewModel.setNidFront(uri.toString())
                } else {
                    viewModel.setNidBack(uri.toString())
                }
                showNidUploadSheet = false
            }
        }
        AlertDialog(
            onDismissRequest = { showNidUploadSheet = false },
            title = {
                Text(
                    text = if (currentNidTarget == "front") "জাতীয় পরিচয়পত্র (১ম পিঠের ছবি)" else "জাতীয় পরিচয়পত্র (২য় পিঠের ছবি)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ভেরিফিকেশনের জন্য আপনার ফোনের গ্যালারি থেকে আসল জাতীয় পরিচয়পত্রের ছবি আপলোড করুন:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            nidPhotoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("গ্যালারি থেকে ছবি নির্বাচন করুন", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNidUploadSheet = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

// =============================================================================
// BKASH / NAGAD INTERACTIVE PAYMENT GATEWAY
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BkashNagadPaymentDialog(
    month: String,
    amount: Int,
    recipientName: String,
    onDismiss: () -> Unit,
    onPaymentComplete: (method: String, pin: String) -> Unit
) {
    var selectedGateway by remember { mutableStateOf("bKash") } // "bKash" or "Nagad"
    var mobileNumber by remember { mutableStateOf("") }
    var pinNumber by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Number & OTP, 2: PIN
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gatewayColor = when (selectedGateway) {
        "Nagad" -> Color(0xFFF7941D)
        else -> Color(0xFFE2136E)
    }

    val recipientPhone = when (selectedGateway) {
        "Nagad" -> "01734249971"
        "Rocket" -> "01734249971"
        else -> "01842450605"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Gateway Brand
                Surface(
                    color = gatewayColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (selectedGateway) {
                                "Nagad" -> "নগদ পেমেন্ট (Nagad)"
                                "Rocket" -> "রকেট পেমেন্ট (Rocket)"
                                else -> "বিকাশ পেমেন্ট (bKash)"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "প্রাপক নম্বর: $recipientPhone ($recipientName)",
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "মাস: $month | ফি: ৳$amount",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector: Only bKash and Nagad ("সুদু বিকাশ ও নগোত")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("bKash", "বিকাশ", R.drawable.img_bkash_logo),
                        Triple("Nagad", "নগদ", R.drawable.img_nagad_logo)
                    ).forEach { (id, label, logoRes) ->
                        val isSelected = selectedGateway == id
                        val brandColor = if (id == "Nagad") Color(0xFFF7941D) else Color(0xFFE2136E)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) brandColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) brandColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGateway = id }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = label,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (step == 1) {
                    Text(
                        text = "আপনার $selectedGateway অ্যাকাউন্ট নম্বর লিখুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        placeholder = { Text("01XXXXXXXXX") },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = gatewayColor)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (mobileNumber.length >= 10) {
                                step = 2
                                errorMessage = null
                            } else {
                                errorMessage = "সঠিক মোবাইল নম্বর লিখুন"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gatewayColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("পরবর্তী ধাপ (পিন দিন) ➡️", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // Step 2: Secret PIN Input ("ঐ ব্যাক্তি কাছে পিন নাম্বার দিলে আমার ফোনে চলে আসবে")
                    Text(
                        text = "আপনার $selectedGateway পিন (PIN) নম্বর লিখুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "পিন দেওয়ার সাথে সাথে টাকা সরাসরি Niyon Islam Sohag-এর ফোনে জমা হবে।",
                        fontSize = 10.sp,
                        color = gatewayColor,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pinNumber,
                        onValueChange = {
                            if (it.length <= 5) pinNumber = it
                        },
                        placeholder = { Text("• • • • •") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = gatewayColor)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, color = Color.Red, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("পেছনে")
                        }

                        Button(
                            onClick = {
                                if (pinNumber.length >= 4) {
                                    onPaymentComplete(selectedGateway, pinNumber)
                                } else {
                                    errorMessage = "সঠিক ৪ বা ৫ ডিজিটের পিন নম্বর লিখুন"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gatewayColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(2f)
                        ) {
                            Text("নিশ্চিত করুন (৳$amount)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onDismiss) {
                    Text("বাতিল করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
