package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.MediaPickerTarget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FaceFeedViewModel = viewModel()
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

    // When viewing a direct chat conversation or the Menu screen, hide the main top bar
    val isInsideDirectChat = activeChat != null && activeTab == AppTab.MESSAGES
    val hideTopBar = isInsideDirectChat || activeTab == AppTab.MENU

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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

    // Interactive Audio, Video, and Group Calling Overlay
    if (activeCall != null) {
        CallingOverlay(
            activeCall = activeCall!!,
            viewModel = viewModel
        )
    }

    // Create Post Dialog (10 photos, 10 hashtags, text, videos/reels)
    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowCreatePostDialog(false) }
        )
    }

    // Fullscreen Story Viewer
    if (viewingStory != null) {
        StoryViewerDialog(
            story = viewingStory!!,
            viewModel = viewModel
        )
    }

    // Full-featured Media Gallery Picker (Profile, Cover, Long Video, Short Video, Story, Photo, Text Status)
    if (activeMediaPickerTarget != MediaPickerTarget.NONE) {
        MediaGalleryPickerDialog(
            viewModel = viewModel,
            initialTarget = activeMediaPickerTarget,
            onDismiss = { viewModel.closeMediaPicker() }
        )
    }

    // Interactive Facebook Live Studio (Camera, LIVE badge, timer, viewers, floating reactions, live chat)
    if (showLiveStudio) {
        LiveStreamStudioDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeLiveStudio() }
        )
    }

    // Authentication Modal (Sign Up, Log In, Apple ID, password eye icon)
    if (showAuthDialog || !isLoggedIn) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    // Universal Global Search Dialog (Any topic, people, posts, videos, groups, ask Fida AI)
    if (showUniversalSearch) {
        UniversalSearchDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeUniversalSearch() }
        )
    }

    // Fida AI Assistant Chat Modal
    if (showFidaAI) {
        FidaAIChatDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeFidaAI() }
        )
    }

    // Fida Verified (Green Badge) Modal
    if (showFidaVerified) {
        FidaVerifiedDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeFidaVerified() }
        )
    }

    // Right Manager Fida (Flower Logo 🌸 - Exclusively for Niyon Islam Sohag)
    if (showRightManagerFida) {
        RightManagerFidaDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeRightManagerFida() }
        )
    }
}
