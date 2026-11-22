package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize real-time Wi-Fi & Mobile Data Connectivity Monitor before other services
        com.example.data.NetworkConnectivityManager.initialize(applicationContext)
        // Initialize Niyon Islam Sohag persistent Owner Control Locks before other managers
        com.example.data.NiyonOwnerControlStore.initialize(applicationContext)
        // Initialize Service Quota Tracker (Firebase Read/Write, Cloudinary 25 GB, Agora 10,000 mins)
        com.example.data.ServiceQuotaTracker.initialize(applicationContext)
        // Initialize Firebase Cloud Services with persistent cache & named Firestore database
        com.example.data.FirebaseManager.initialize(applicationContext)
        // Initialize FCM Notification Channels & register push token if signed in
        com.example.data.FotoFilmMessagingService.ensureChannels(applicationContext)
        com.example.data.FirebaseManager.registerFcmToken()
        // Initialize Cloudinary Free Media Cloud (No credit card required)
        com.example.data.CloudinaryManager.initialize(applicationContext)
        // Refresh Agora HD Audio/Video Calling configuration from AI Studio Secrets
        com.example.data.AgoraCallManager.refreshConfigStatus()
        // Initialize Google Mobile Ads (AdMob)
        try {
            com.google.android.gms.ads.MobileAds.initialize(this) {}
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "MobileAds initialization skipped: ${e.message}")
        }
        // Initialize audio playback & voice engine context early
        com.example.util.AppSoundManager.init(applicationContext)
        setContent {
            val viewModel: FaceFeedViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()
            val launchDeepLink = intent?.data?.toString()
            LaunchedEffect(launchDeepLink) {
                if (!launchDeepLink.isNullOrBlank()) {
                    viewModel.openUserProfileByLink(launchDeepLink)
                }
            }

            // Load local Room persisted posts/messages, saved accounts, and restore active auth session
            LaunchedEffect(Unit) {
                viewModel.initializeLocalStorage(applicationContext)
                viewModel.loadSavedEmailAccounts(applicationContext)
                viewModel.restoreAuthSession(applicationContext)
            }

            MyApplicationTheme(darkTheme = isDark) {
                var showSplashScreen by rememberSaveable { mutableStateOf(true) }

                if (showSplashScreen) {
                    SplashScreen(onTimeout = { showSplashScreen = false })
                } else {
                    FaceFeedApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun FaceFeedApp(viewModel: FaceFeedViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val incomingCallSignal by viewModel.incomingCallSignal.collectAsState()
    val showCreatePostDialog by viewModel.showCreatePostDialog.collectAsState()
    val viewingStory by viewModel.viewingStory.collectAsState()
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeMediaPickerTarget by viewModel.activeMediaPickerTarget.collectAsState()
    val showLiveStudio by viewModel.showLiveStudio.collectAsState()
    val showUniversalSearch by viewModel.showUniversalSearch.collectAsState()
    val showFidaAI by viewModel.showFidaAIDialog.collectAsState()
    val showFidaVerified by viewModel.showFidaVerifiedDialog.collectAsState()
    val showRightManagerFida by viewModel.showRightManagerFidaDialog.collectAsState()
    val isOwnerSession by viewModel.isOwnerSession.collectAsState()
    val rightManagerInitialTab by viewModel.rightManagerInitialTab.collectAsState()
    val showAppDownloadWebHub by viewModel.showAppDownloadWebDialog.collectAsState()
    val viewingProfileUser by viewModel.viewingProfileUser.collectAsState()
    val compressionToastMessage by viewModel.compressionToastMessage.collectAsState()
    val isInternetConnected by viewModel.isInternetConnected.collectAsState()

    val notifContext = androidx.compose.ui.platform.LocalContext.current

    // Enforce Facebook-style strict internet gating & immediate reload on reconnect
    LaunchedEffect(isInternetConnected) {
        if (!isInternetConnected) {
            VideoPlaybackCoordinator.pauseActiveVideo()
            com.example.util.AppSoundManager.pauseAudio()
            viewModel.handleNetworkLost()
        } else {
            viewModel.onNetworkConnectivityRestored(notifContext)
        }
    }

    LaunchedEffect(compressionToastMessage) {
        if (compressionToastMessage != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearCompressionToastMessage()
        }
    }
    val notifPrefs = remember(notifContext) {
        notifContext.applicationContext.getSharedPreferences(
            "fotofilm_device_prefs",
            android.content.Context.MODE_PRIVATE
        )
    }
    val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifPrefs.edit().putBoolean("notif_permission_prompted_once", true).apply()
        if (granted) {
            com.example.data.FotoFilmMessagingService.ensureChannels(notifContext.applicationContext)
            com.example.data.FirebaseManager.registerFcmToken()
        }
    }

    LaunchedEffect(isLoggedIn, showAuthDialog) {
        if (isLoggedIn && !showAuthDialog) {
            val alreadyPromptedOnce = notifPrefs.getBoolean("notif_permission_prompted_once", false)
            if (!alreadyPromptedOnce) {
                notifPrefs.edit().putBoolean("notif_permission_prompted_once", true).apply()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        notifContext,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    val isInsideDirectChat = activeChat != null && activeTab == AppTab.MESSAGES
    val hideTopBar = isInsideDirectChat || activeTab == AppTab.MENU

    val homeListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxHeaderCollapsePx = remember(density) { with(density) { 82.dp.toPx() } }
    var topHeaderCollapsePx by remember { mutableFloatStateOf(0f) }
    val topSectionCollapseFraction by remember {
        derivedStateOf {
            when (activeTab) {
                AppTab.HOME -> (topHeaderCollapsePx / maxHeaderCollapsePx).coerceIn(0f, 1f)
                AppTab.MESSAGES -> 1f
                else -> 0f
            }
        }
    }

    LaunchedEffect(activeTab) {
        if (activeTab != AppTab.HOME) {
            topHeaderCollapsePx = 0f
        }
    }

    LaunchedEffect(homeListState.firstVisibleItemIndex, homeListState.firstVisibleItemScrollOffset) {
        if (homeListState.firstVisibleItemIndex == 0 && homeListState.firstVisibleItemScrollOffset == 0) {
            topHeaderCollapsePx = 0f
        }
    }

    val homeNestedScrollConnection = remember(maxHeaderCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                topHeaderCollapsePx = (topHeaderCollapsePx - deltaY).coerceIn(0f, maxHeaderCollapsePx)
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = activeTab != AppTab.HOME || activeChat != null) {
        if (activeChat != null) {
            viewModel.closeChat()
        } else if (activeTab == AppTab.GROUPS) {
            viewModel.setTab(AppTab.MENU)
        } else {
            viewModel.setTab(AppTab.HOME)
        }
    }

    if (!isInternetConnected) {
        Box(modifier = Modifier.fillMaxSize()) {
            FacebookStyleOfflineScreen(viewModel = viewModel)

            if (compressionToastMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFB71C1C),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = compressionToastMessage!!,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearCompressionToastMessage() }) {
                                Text("ঠিক আছে", color = Color(0xFFFFEB3B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Strict Play Store Launch Gate: Users MUST Sign Up or Log In before entering the app
    if (!isLoggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            AuthDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setShowAuthDialog(false) }
            )
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!hideTopBar) {
                FaceFeedTopBar(
                    viewModel = viewModel,
                    topSectionCollapseFraction = topSectionCollapseFraction,
                    onHomeReselect = {
                        topHeaderCollapsePx = 0f
                        viewModel.refreshHomeFeed()
                        coroutineScope.launch {
                            homeListState.animateScrollToItem(0)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isInsideDirectChat) {
                FaceFeedBottomNavBar(viewModel = viewModel)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (activeTab == AppTab.HOME) Modifier.nestedScroll(homeNestedScrollConnection)
                    else Modifier
                )
        ) {
            when (activeTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel, listState = homeListState)
                AppTab.MESSAGES -> MessagesScreen(viewModel = viewModel)
                AppTab.VIDEOS, AppTab.REELS -> VideosScreen(viewModel = viewModel)
                AppTab.FRIENDS -> FriendsScreen(viewModel = viewModel)
                AppTab.GROUPS -> GroupsPagesScreen(viewModel = viewModel)
                AppTab.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppTab.MENU -> MenuScreen(viewModel = viewModel)
            }
        }
    }

    if (activeCall != null) {
        CallingOverlay(
            activeCall = activeCall!!,
            viewModel = viewModel
        )
    } else if (incomingCallSignal != null) {
        IncomingCallDialog(
            signal = incomingCallSignal!!,
            onAccept = { viewModel.acceptIncomingCall() },
            onDecline = { viewModel.declineIncomingCall() }
        )
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowCreatePostDialog(false) }
        )
    }

    if (viewingStory != null) {
        StoryViewerDialog(
            story = viewingStory!!,
            viewModel = viewModel
        )
    }

    if (activeMediaPickerTarget != MediaPickerTarget.NONE) {
        MediaGalleryPickerDialog(
            viewModel = viewModel,
            initialTarget = activeMediaPickerTarget,
            onDismiss = { viewModel.closeMediaPicker() }
        )
    }

    if (showLiveStudio) {
        LiveStreamStudioDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeLiveStudio() }
        )
    }

    if (showAuthDialog || !isLoggedIn) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    if (showUniversalSearch) {
        UniversalSearchDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeUniversalSearch() }
        )
    }

    if (showFidaAI) {
        FidaAIChatDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeFidaAI() }
        )
    }

    if (showFidaVerified) {
        FidaVerifiedDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeFidaVerified() }
        )
    }

    if (showRightManagerFida) {
        if (isOwnerSession && viewModel.isCurrentUserNiyonIslamSohag()) {
            RightManagerFidaDialog(
                viewModel = viewModel,
                initialTab = rightManagerInitialTab,
                onDismiss = { viewModel.closeRightManagerFida() }
            )
        } else {
            viewModel.closeRightManagerFida()
        }
    }

    if (showAppDownloadWebHub) {
        AppDownloadWebDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeAppDownloadWebHub() }
        )
    }

    if (viewingProfileUser != null) {
        FriendProfileScreen(
            user = viewingProfileUser!!,
            viewModel = viewModel,
            onBack = { viewModel.closeUserProfile() },
            onMessage = {
                val target = viewingProfileUser!!
                viewModel.closeUserProfile()
                viewModel.openChatWithUser(target)
            },
            onAudioCall = { viewModel.startAudioCall(viewingProfileUser!!) },
            onVideoCall = { viewModel.startVideoCall(viewingProfileUser!!) }
        )
    }

    if (compressionToastMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 72.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B5E20),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = compressionToastMessage!!,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearCompressionToastMessage() }) {
                        Text("ঠিক আছে", color = Color(0xFFFFEB3B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
