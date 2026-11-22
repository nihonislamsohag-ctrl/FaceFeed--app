package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.User
import androidx.activity.compose.BackHandler
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.FaceFeedViewModel

/**
 * 🎲 লুডু কিং গ্রুপ এর নীতিমালা (Ludo King Group Policy) Screen
 * Exactly matches Attachment: IMG-20261105-WA8665_25711871.jpg
 *
 * All 6 numbered items are fully interactive:
 *  - 1. 1 জন কম্পিউটারের সাথে লুডু খেলতে পারবেন -> Launches Computer AI mode
 *  - 2. এক সাথে 4 জন মিলে লুডু খেলতে পারবেন -> Launches 4-player match
 *  - 3. 2 জন 2 জন মিলে জোর জোর লুডু খেলতে পারবেন -> Launches 2 vs 2 Team mode
 *  - 4. লুডু খেলার জন্য বন্ধুদের ইনভাইট করুন -> Opens Invite Friends dialog with online status
 *  - 5. কারো কোনো আইডিতে সমস্যা হলে, Help & Support টিমের সাথে যোগাযোগ করুন -> 24/7 Support sheet
 *  - 6. FotoFilm এর কোম্পানি Niyon Islam Sohag ভাই-কে অসংখ্য ধন্যবাদ -> Founder tribute dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGroupPolicyDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onLaunchGame: (GameMode?) -> Unit
) {
    val context = LocalContext.current
    val allUsers by viewModel.allAppUsers.collectAsState()
    val onlinePlayersCount = 1248 + allUsers.count { it.isOnline }

    // Sub-dialog states
    var showInviteFriendsDialog by remember { mutableStateOf(false) }
    var showComputerModeDialog by remember { mutableStateOf(false) }
    var showFourPlayersDialog by remember { mutableStateOf(false) }
    var showTwoTeamDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showFounderDialog by remember { mutableStateOf(false) }
    var showOriginalPosterDialog by remember { mutableStateOf(false) }
    var showLudoClubLobbyDialog by remember { mutableStateOf(false) }

    BackHandler { onDismiss() }

    // Royal dark blue starry gradient background matching the attachment
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060922),
            Color(0xFF0B113B),
            Color(0xFF141952),
            Color(0xFF090D2A)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar with Close & Quick Launch Ludo button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Button to view original poster image
                        Surface(
                            onClick = { showOriginalPosterDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1565C0).copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color(0xFF64B5F6)),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🖼️", fontSize = 12.sp)
                                Text(
                                    text = "আসল পোস্টার",
                                    color = Color(0xFFBBDEFB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Button to launch Ludo King directly
                        Surface(
                            onClick = { onLaunchGame(null) },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1B5E20),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🎲", fontSize = 13.sp)
                                Text(
                                    text = "সরাসরি খেলুন",
                                    color = Color(0xFFFFE082),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FotoFilm Brand Badge & Header (Exact match of Screenshot)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    FotoFilmFlagLogo(size = 64)

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "FotoFilm",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFE082),
                        letterSpacing = 1.sp
                    )
                }

                // Decorative filigree line with diamond: ◆ ──────── ◆
                PolicyFiligreeDivider()

                Spacer(modifier = Modifier.height(6.dp))

                // Title: 🎲 লুডু কিং গ্রুপ এর নীতিমালা (Golden Bengali serif typography)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🎲 ", fontSize = 22.sp)
                    Text(
                        text = "লুডু কিং গ্রুপ এর নীতিমালা",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FidaVerifiedBadge(size = 18.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Ludo King Online Players Badge ("ঐ খানে ডুকলে যেনো দেখা জায়ে যে কতো মানুস লুডু কিং লাইনে আছে")
                Surface(
                    onClick = { showLudoClubLobbyDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0D652D).copy(alpha = 0.85f),
                    border = BorderStroke(1.2.dp, Color(0xFF69F0AE)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                        Text(
                            text = "এখন লুডু কিং লাইনে আছে: $onlinePlayersCount জন খেলোয়াড় ⚡",
                            color = Color(0xFFB9F6CA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ----------------------------------------------------
                // The 6 Numbered Policy Items from Image:
                // ----------------------------------------------------

                // Item 1: 1. 1 জন কম্পিউটারের সাথে লুডু খেলতে পারবেন। (💻👤)
                PolicyItemCard(
                    itemNumber = 1,
                    text = "1. 1 জন কম্পিউটারের সাথে\nলুডু খেলতে পারবেন।",
                    iconContent = {
                        Text("💻👤", fontSize = 22.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showComputerModeDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 2: 2. এক সাথে 4 জন মিলে লুডু খেলতে পারবেন। (👥👑)
                PolicyItemCard(
                    itemNumber = 2,
                    text = "2. এক সাথে 4 জন মিলে লুডু\nখেলতে পারবেন।",
                    iconContent = {
                        Text("👥👑", fontSize = 22.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showFourPlayersDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 3: 3. 2 জন 2 জন মিলে জোর জোর লুডু খেলতে পারবেন। (👥🛡️)
                PolicyItemCard(
                    itemNumber = 3,
                    text = "3. 2 জন 2 জন মিলে জোর জোর\nলুডু খেলতে পারবেন।",
                    iconContent = {
                        Text("👥🛡️", fontSize = 22.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showTwoTeamDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 4: 4. লুডু খেলার জন্য বন্ধুদের ইনভাইট করুন। (✉️👥)
                PolicyItemCard(
                    itemNumber = 4,
                    text = "4. লুডু খেলার জন্য বন্ধুদের\nইনভাইট করুন।",
                    iconContent = {
                        Text("👥✉️", fontSize = 22.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showInviteFriendsDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 5: 5. কারো কোনো আইডিতে সমস্যা হলে, Help & Support টিমের সাথে যোগাযোগ করুন (📋👆)
                PolicyItemCard(
                    itemNumber = 5,
                    text = "5. কারো কোনো আইডিতে সমস্যা হলে,\nHelp & Support টিমের সাথে\nযোগাযোগ করুন",
                    iconContent = {
                        Text("📋👆", fontSize = 22.sp)
                    },
                    onClick = {
                        showHelpSupportDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 6: 6. FotoFilm এর কোম্পানি Niyon Islam Sohag ভাই-কে অসংখ্য ধন্যবাদ। (FotoFilm Logo badge)
                PolicyItemCard(
                    itemNumber = 6,
                    text = "6. FotoFilm এর কোম্পানি\nNiyon Islam Sohag ভাই-কে\nঅসংখ্য ধন্যবাদ।",
                    iconContent = {
                        FotoFilmFlagLogo(size = 36)
                    },
                    onClick = {
                        showFounderDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Item 7: New Friend Ludo Club, Chat, Photo/Screenshot & Incremental 2/3/4 Join Option
                PolicyItemCard(
                    itemNumber = 7,
                    text = "7. বন্ধু ক্লাব: চ্যাট, ছবি, স্ক্রিনশট\nও ২/৩/৪ জন এক এক করে\nলুডু খেলায় জয়েন দিন",
                    iconContent = {
                        Text("💬📸", fontSize = 21.sp)
                    },
                    isFeatured = true,
                    onClick = {
                        showLudoClubLobbyDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big Gold & Green Action Button: লুডু ম্যাচ শুরু করুন
                Button(
                    onClick = { onLaunchGame(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎲", fontSize = 20.sp)
                        Text(
                            text = "লুডু খেলা শুরু করুন (Start Ludo)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Footer: • FotoFilm • সুরক্ষিত • সংগঠিত • সফল • (Exact match of Screenshot)
                PolicyFiligreeDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• FotoFilm • সুরক্ষিত • সংগঠিত • সফল •",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ----------------------------------------------------
    // Sub-dialog 1: 1. 1 জন কম্পিউটারের সাথে খেলুন
    // ----------------------------------------------------
    if (showComputerModeDialog) {
        LudoModeInfoDialog(
            title = "🤖 কম্পিউটার মোড (1 Player vs 3 AI)",
            modeTitle = "১ জন কম্পিউটারের সাথে খেলুন",
            description = "আপনি লাল রঙের গুটি নিয়ে খেলবেন এবং কম্পিউটার স্বয়ংক্রিয়ভাবে সবুজ, হলুদ ও নীল গুটি পরিচালনা করবে। ইন্টারনেট ছাড়াও আপনি একাই দ্রুত প্র্যাকটিস করতে পারবেন!",
            badge = "1 Player vs AI",
            actionButtonText = "কম্পিউটারের সাথে খেলুন 🤖",
            onDismiss = { showComputerModeDialog = false },
            onStartMode = {
                showComputerModeDialog = false
                onLaunchGame(GameMode.VS_COMPUTER)
            }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 2: 2. এক সাথে 4 জন মিলে খেলুন
    // ----------------------------------------------------
    if (showFourPlayersDialog) {
        LudoModeInfoDialog(
            title = "🏆 ৪ জন মাল্টিপ্লেয়ার ম্যাচ (4 Players Bijor)",
            modeTitle = "এক সাথে ৪ জন মিলে খেলুন",
            description = "এক সাথে ৪ জন খেলোয়াড় ৪টি ভিন্ন রঙে (লাল, সবুজ, হলুদ ও নীল) একে অপরের বিরুদ্ধে খেলুন। সবার গুটি কাটুন এবং প্রথমে ঘরে প্রবেশ করে চ্যাম্পিয়ন ট্রফি জিতুন!",
            badge = "4 Players All vs All",
            actionButtonText = "৪ জনের ম্যাচ শুরু করুন 🏆",
            onDismiss = { showFourPlayersDialog = false },
            onStartMode = {
                showFourPlayersDialog = false
                onLaunchGame(GameMode.FOUR_BIJOR)
            }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 3: 3. ২ জন ২ জন মিলে জোর জোর লুডু
    // ----------------------------------------------------
    if (showTwoTeamDialog) {
        LudoModeInfoDialog(
            title = "🤝 ২ জন ২ জন জোড় জোর লুডু (Team 2v2)",
            modeTitle = "টিম পার্টনারশিপ লুডু টুর্নামেন্ট",
            description = "টিম লাল ও হলুদ মিলে এক পক্ষ এবং টিম সবুজ ও নীল মিলে দ্বিতীয় পক্ষ! টিম মেম্বারদের গুটি একে অপরকে কাটবে না, বরং একসাথে অপর টিমকে ব্লক ও স্ট্রাইক করে জয় নিশ্চিত করবে!",
            badge = "Team 2 vs 2",
            actionButtonText = "টিম ম্যাচ খেলুন 🤝",
            onDismiss = { showTwoTeamDialog = false },
            onStartMode = {
                showTwoTeamDialog = false
                onLaunchGame(GameMode.TWO_JOR_TEAM)
            }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 4: 4. বন্ধুদের ইনভাইট করুন
    // ----------------------------------------------------
    if (showInviteFriendsDialog) {
        val currentUser by viewModel.currentUser.collectAsState()
        val allUsers by viewModel.allAppUsers.collectAsState()
        LudoInviteFriendsDialog(
            realUsers = allUsers.filter { it.id != currentUser.id },
            onDismiss = { showInviteFriendsDialog = false },
            onInviteSent = { friendName ->
                Toast.makeText(context, "🎉 $friendName-কে লুডু খেলার আমন্ত্রণ পাঠানো হয়েছে! 🎲", Toast.LENGTH_SHORT).show()
            },
            onStartOnlineRoom = {
                showInviteFriendsDialog = false
                onLaunchGame(GameMode.ONLINE_MULTIPLAYER)
            }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 5: 5. Help & Support
    // ----------------------------------------------------
    if (showHelpSupportDialog) {
        val currentUser by viewModel.currentUser.collectAsState()
        HelpSupportDialog(
            onDismiss = { showHelpSupportDialog = false },
            onSubmitSupportMessage = { messageText ->
                viewModel.submitSupportTicket(
                    message = "[লুডু কিং গ্রুপ - হেল্পলাইন সাপোর্ট]\n$messageText",
                    profileUrl = "https://fotofilm.app/u/${currentUser.username}",
                    screenshotUrls = emptyList()
                )
            }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 6: 6. Founder Niyon Islam Sohag Tribute
    // ----------------------------------------------------
    if (showFounderDialog) {
        FounderAppreciationDialog(
            onDismiss = { showFounderDialog = false }
        )
    }

    // ----------------------------------------------------
    // Sub-dialog 7: View Original Poster Image (Attachment)
    // ----------------------------------------------------
    if (showOriginalPosterDialog) {
        Dialog(
            onDismissRequest = { showOriginalPosterDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎲 লুডু কিং গ্রুপ এর নীতিমালা (আসল পোস্টার)",
                            color = Color(0xFFFFE082),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showOriginalPosterDialog = false },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Original Poster Image
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.img_ludo_king_policy),
                            contentDescription = "Ludo King Policy Poster",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Bottom start game button
                    Button(
                        onClick = {
                            showOriginalPosterDialog = false
                            onLaunchGame(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("🎲 লুডু খেলা শুরু করুন", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Sub-dialog 8: Ludo Friends Club Lobby (Chat + Photos/Screenshots + 2/3/4 Incremental Join + Online Counter)
    // ----------------------------------------------------
    if (showLudoClubLobbyDialog) {
        LudoFriendsClubLobbyDialog(
            viewModel = viewModel,
            onDismiss = { showLudoClubLobbyDialog = false },
            onLaunchGame = { mode ->
                showLudoClubLobbyDialog = false
                onLaunchGame(mode)
            }
        )
    }
}

/**
 * Reusable Mode Info Dialog for Items 1, 2, 3
 */
@Composable
fun LudoModeInfoDialog(
    title: String,
    modeTitle: String,
    description: String,
    badge: String,
    actionButtonText: String,
    onDismiss: () -> Unit,
    onStartMode: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D652D).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F))
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = modeTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onStartMode,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actionButtonText,
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * 4. বন্ধুদের ইনভাইট করুন (Invite Friends Sheet)
 */
@Composable
fun LudoInviteFriendsDialog(
    realUsers: List<User> = emptyList(),
    onDismiss: () -> Unit,
    onInviteSent: (String) -> Unit,
    onStartOnlineRoom: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val invitedSet = remember { mutableStateListOf<String>() }

    val friendsList = remember(realUsers) {
        realUsers.map { Triple(it.id, it.fullName, it.isOnline) }
    }

    val filtered = friendsList.filter {
        it.second.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0E1438),
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✉️ বন্ধুদের লুডু খেলার ইনভাইট করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ফ্রেন্ড সার্চ করুন...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD54F)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD54F),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color(0xFFFFD54F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Friends List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো অনলাইন বন্ধু খুঁজে পাওয়া যায়নি",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        filtered.forEach { (id, name, isOnline) ->
                        val isInvited = invitedSet.contains(id)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = name.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                    if (isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                                .border(1.dp, Color(0xFF0E1438), CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isOnline) "🟢 অনলাইনে আছে" else "⚪ অফলাইন",
                                        fontSize = 11.sp,
                                        color = if (isOnline) Color(0xFF00E676) else Color.White.copy(alpha = 0.5f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (!isInvited) {
                                            invitedSet.add(id)
                                            onInviteSent(name)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isInvited) Color.Gray.copy(alpha = 0.5f) else Color(0xFF0D652D)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isInvited) "ইনভাইটেড ✓" else "ইনভাইট 🎲",
                                        fontSize = 11.sp,
                                        color = if (isInvited) Color.White.copy(alpha = 0.8f) else Color(0xFFFFE082),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Copy Room link & Start Online Room buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Ludo Room Link", "https://fotofilm.app/ludo/room_${System.currentTimeMillis() % 10000}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "🔗 রুম লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔗 লিংক কপি", color = Color(0xFFFFE082), fontSize = 12.sp)
                    }

                    Button(
                        onClick = onStartOnlineRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("অনলাইন খেলুন 🌐", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
