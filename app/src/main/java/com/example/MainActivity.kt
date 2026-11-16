package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        setContent {
            val viewModel: FaceFeedViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()
            val launchDeepLink = intent?.data?.toString()
            LaunchedEffect(launchDeepLink) {
                if (!launchDeepLink.isNullOrBlank()) {
                    viewModel.openUserProfileByLink(launchDeepLink)
                }
            }

            // Load saved device accounts and resume any existing Firebase cloud sync quietly without showing any Google prompt
            LaunchedEffect(Unit) {
                viewModel.loadSavedEmailAccounts(applicationContext)
                try {
                    Firebase.auth.currentUser?.let { user ->
                        viewModel.startCloudSyncForUser(user, applicationContext)
                    }
                } catch (_: Exception) {
                }
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
    val rightManagerInitialTab by viewModel.rightManagerInitialTab.collectAsState()
    val showAppDownloadWebHub by viewModel.showAppDownloadWebDialog.collectAsState()
    val viewingProfileUser by viewModel.viewingProfileUser.collectAsState()
    val compressionToastMessage by viewModel.compressionToastMessage.collectAsState()

    LaunchedEffect(compressionToastMessage) {
        if (compressionToastMessage != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearCompressionToastMessage()
        }
    }

    val notifContext = androidx.compose.ui.platform.LocalContext.current
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

    BackHandler(enabled = activeTab != AppTab.HOME || activeChat != null) {
        if (activeChat != null) {
            viewModel.closeChat()
        } else if (activeTab == AppTab.GROUPS) {
            viewModel.setTab(AppTab.MENU)
        } else {
            viewModel.setTab(AppTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!hideTopBar) {
                FaceFeedTopBar(viewModel = viewModel)
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
        ) {
            when (activeTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
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
        if (viewModel.isCurrentUserNiyonIslamSohag()) {
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
