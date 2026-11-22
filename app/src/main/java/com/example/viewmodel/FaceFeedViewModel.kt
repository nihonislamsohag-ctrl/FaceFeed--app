package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab {
    HOME, MESSAGES, VIDEOS, REELS, FRIENDS, GROUPS, NOTIFICATIONS, PROFILE, MENU
}

enum class MediaPickerTarget {
    NONE,
    PROFILE_PHOTO,
    COVER_PHOTO,
    FEED_PHOTO,
    STORY,
    SHORT_VIDEO,
    LONG_VIDEO,
    TEXT_POST
}

data class UserAccount(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val isActive: Boolean = false,
    val notificationCount: Int = 0
)

data class FidaChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "FIDA_AI"
    val text: String,
    val timestamp: String = "এখন"
)

data class IntrusionLog(
    val id: String = UUID.randomUUID().toString(),
    val ipAddress: String,
    val deviceModel: String,
    val attackerName: String = "অজানা অনুপ্রবেশকারী (Anonymous)",
    val attackVector: String,
    val timestamp: String,
    val targetArea: String,
    val severity: String, // "CRITICAL 🔴", "HIGH 🟠", "MEDIUM 🟡"
    val status: String = "AUTO-BLOCKED 🛡️",
    val defenseLog: String = "আইপি এবং ডিভাইস পার্মানেন্ট ব্ল্যাকলিস্ট করা হয়েছে।"
)

data class PostUploadProgress(
    val postId: String,
    val postPreview: String,
    val mediaTypeLabel: String,
    val progressPercent: Int = 0,
    val statusLog: String = "আপলোড প্রস্তুত হচ্ছে...",
    val isCompleted: Boolean = false
)

enum class UniversalSearchCategory(val labelBn: String, val labelEn: String) {
    ALL("সব", "All"),
    PEOPLE("মানুষ", "People"),
    POSTS("পোস্ট", "Posts"),
    VIDEOS("ভিডিও", "Videos"),
    GROUPS("গ্রুপ", "Groups"),
    PAGES("পেজ", "Pages")
}

enum class SimOperator(val banglaName: String, val englishName: String, val colorHex: Long) {
    GRAMEENPHONE("গ্রামীণফোন (GP)", "Grameenphone", 0xFF00A3E0),
    ROBI("রবি (Robi)", "Robi", 0xFFE31B23),
    AIRTEL("এয়ারটেল (Airtel)", "Airtel", 0xFFE40000),
    BANGLALINK("বাংলালিংক (Banglalink)", "Banglalink", 0xFFFF6600),
    TELETALK("টেলিটক (Teletalk)", "Teletalk", 0xFF00853F),
    SKITTO("স্কিটো (Skitto)", "Skitto", 0xFFFFD100);

    companion object {
        val ALL = values().toList()
    }
}

class FaceFeedViewModel : ViewModel() {

    // Free Mode (0 MB Data) & BD SIM operators
    private val _isFreeMode = MutableStateFlow(false)
    val isFreeMode: StateFlow<Boolean> = _isFreeMode.asStateFlow()

    private val _currentSimOperator = MutableStateFlow(SimOperator.GRAMEENPHONE)
    val currentSimOperator: StateFlow<SimOperator> = _currentSimOperator.asStateFlow()

    private val _showFreeModeVideoWarning = MutableStateFlow(false)
    val showFreeModeVideoWarning: StateFlow<Boolean> = _showFreeModeVideoWarning.asStateFlow()

    // Music Tracks from Firebase Firestore
    private val _musicTracks = MutableStateFlow<List<MusicTrack>>(com.example.data.FirebaseManager.getDefaultMusicTracks())
    val musicTracks: StateFlow<List<MusicTrack>> = _musicTracks.asStateFlow()

    private val _pendingPostAudio = MutableStateFlow<PostAudioTrack?>(null)
    val pendingPostAudio: StateFlow<PostAudioTrack?> = _pendingPostAudio.asStateFlow()

    // Real-time Wi-Fi & Mobile Data Connectivity State (Facebook-style Internet Gating)
    val isInternetConnected: StateFlow<Boolean> = com.example.data.NetworkConnectivityManager.isConnected
    val networkTransportType: StateFlow<com.example.data.NetworkTransportType> = com.example.data.NetworkConnectivityManager.transportType

    private var activeChatListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var postsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var storiesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var musicListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var pagesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var groupsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val commentListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
    private var listenersStartedForUid: String? = null
    private var isCurrentOwnerAuthenticated: Boolean = false
    private val _isOwnerSession = MutableStateFlow(false)
    val isOwnerSession: StateFlow<Boolean> = _isOwnerSession.asStateFlow()

    private fun setOwnerAuthenticated(authenticated: Boolean) {
        isCurrentOwnerAuthenticated = authenticated
        _isOwnerSession.value = authenticated
    }

    private fun createGuestUser(): User = User(
        id = "guest_pending",
        fullName = "",
        username = "",
        email = "",
        avatarUrl = "",
        coverUrl = "",
        bio = "",
        isVerified = false,
        isOnline = false
    )

    private fun syncUserAccountsForCurrentUser(user: User) {
        if (user.id == "guest_pending" || (user.fullName.isBlank() && user.email.isBlank())) {
            _userAccounts.value = emptyList()
            return
        }
        val cleanHandle = user.username.ifBlank {
            user.email.substringBefore("@").ifBlank { "user" }
        }.let { if (it.startsWith("@")) it else "@$it" }
        val entry = UserAccount(
            id = user.id,
            name = user.fullName.ifBlank { "FotoFilm User" },
            handle = cleanHandle,
            avatarUrl = user.avatarUrl,
            isActive = true,
            notificationCount = 0
        )
        val mergedOthers = (_userAccounts.value + _savedEmailAccounts.value.map { saved ->
            val h = saved.username.ifBlank { saved.email.substringBefore("@") }
                .let { if (it.startsWith("@")) it else "@$it" }
            UserAccount(
                id = saved.id,
                name = saved.fullName,
                handle = h,
                avatarUrl = saved.avatarUrl,
                isActive = false,
                notificationCount = 0
            )
        })
            .distinctBy { it.handle.lowercase() }
            .filterNot { it.id == entry.id || it.handle.equals(entry.handle, ignoreCase = true) }
            .map { it.copy(isActive = false) }
        _userAccounts.value = (listOf(entry) + mergedOthers).take(com.example.util.FakeIdDetector.MAX_ACCOUNTS_PER_PERSON)
    }

    private fun countDistinctAccountsOnDevice(
        context: android.content.Context? = appContextRef,
        excludingEmail: String? = null
    ): Int {
        val excl = excludingEmail?.trim()?.lowercase().orEmpty()
        val stored = context?.let { com.example.data.SavedAccountsStore.loadAccounts(it) }
            ?: _savedEmailAccounts.value
        val distinctKeys = mutableSetOf<String>()
        stored.forEach { acc ->
            val key = acc.email.trim().lowercase().ifBlank { acc.username.trim().lowercase() }
            if (key.isNotBlank() && key != excl) {
                distinctKeys.add(key)
            }
        }
        _userAccounts.value.forEach { ua ->
            val h = ua.handle.removePrefix("@").trim().lowercase()
            val expectedEmail = "$h@fotofilm.app"
            if (h.isNotBlank() && expectedEmail != excl && !distinctKeys.any { it.startsWith("$h@") || it == h }) {
                distinctKeys.add(h)
            }
        }
        return distinctKeys.size
    }

    private fun recordFakeIdViolationReport(
        context: android.content.Context?,
        attemptedName: String,
        attemptedUsername: String,
        attemptedEmail: String,
        selectedGender: String,
        detectedSignal: String,
        violationType: String,
        violationDetails: String,
        attemptedAvatarUrl: String = "",
        matchedExistingUser: String = ""
    ) {
        val report = com.example.util.FakeIdDetector.createReport(
            attemptedName = attemptedName,
            attemptedUsername = attemptedUsername,
            attemptedEmail = attemptedEmail,
            selectedGender = selectedGender,
            detectedSignal = detectedSignal,
            violationType = violationType,
            violationDetails = violationDetails,
            attemptedAvatarUrl = attemptedAvatarUrl,
            matchedExistingUser = matchedExistingUser
        )
        val updated = com.example.util.FakeIdDetector.recordReport(context ?: appContextRef, report)
        _fakeIdReports.value = updated
    }

    companion object {
        private val DEMO_POST_IDS = setOf(
            "post_sponsored_demo",
            "post_sahid",
            "post_pinned",
            "post_1",
            "post_user_2",
            "post_2",
            "post_3",
            "post_4"
        )
        private val DEMO_STORY_IDS = setOf(
            "story_me",
            "story_1",
            "story_2",
            "story_3"
        )
    }

    private fun isAuthenticPost(post: Post): Boolean {
        if (post.id in DEMO_POST_IDS) return false
        val isHidden = synchronized(hiddenPostIdsCache) { post.id in hiddenPostIdsCache }
        if (isHidden) return false
        if (post.images.isNotEmpty()) {
            val allUnsplashStock = post.images.all {
                it.contains("images.unsplash.com", ignoreCase = true)
            }
            if (allUnsplashStock) return false
        }
        return true
    }

    private fun isAuthenticStory(story: Story): Boolean {
        if (story.id in DEMO_STORY_IDS) return false
        if (story.mediaUrl.isBlank()) return false
        if (story.mediaUrl.contains("images.unsplash.com", ignoreCase = true)) return false
        return true
    }

    fun hasActiveInternetConnection(context: android.content.Context? = appContextRef): Boolean {
        val ctx = context ?: appContextRef
        return if (ctx != null) {
            com.example.data.NetworkConnectivityManager.refreshNow(ctx)
        } else {
            com.example.data.NetworkConnectivityManager.isConnected.value
        }
    }

    private fun requireInternetConnection(): Boolean {
        val connected = hasActiveInternetConnection()
        if (!connected) {
            _compressionToastMessage.value = "ইন্টারনেট সংযোগ নেই - ডাটা বা ওয়াই-ফাই চালু করুন"
        }
        return connected
    }

    fun handleNetworkLost() {
        _posts.value = emptyList()
        _stories.value = emptyList()
        _savedPosts.value = emptyList()
        _viewingStory.value = null
        _showLiveStudio.value = false
        _showCreatePostDialog.value = false
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
        _homeBadgeCount.value = 0
        _watchBadgeCount.value = 0
        _isHomeRefreshing.value = false
    }

    fun onNetworkConnectivityRestored(context: android.content.Context) {
        val ctx = context.applicationContext
        appContextRef = ctx
        com.example.data.NetworkConnectivityManager.initialize(ctx)
        if (!com.example.data.NetworkConnectivityManager.isConnected.value) return

        val repo = localRepo ?: com.example.data.local.LocalPersistenceRepository(ctx).also { localRepo = it }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = repo.loadPostsSnapshot()
                synchronized(hiddenPostIdsCache) {
                    hiddenPostIdsCache.addAll(snapshot.hiddenPostIds)
                }
                val authenticVisible = snapshot.visiblePosts
                    .filter { isAuthenticPost(it) }
                    .sortedByDescending { it.createdAt }
                    .map { p ->
                        if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(p.id)) {
                            p.copy(isBoostActive = false, isBoosted = false)
                        } else p
                    }
                if (authenticVisible.isNotEmpty() && _posts.value.isEmpty()) {
                    _posts.value = authenticVisible
                    _savedPosts.value = authenticVisible.filter { it.isSaved }
                }
            } catch (_: Exception) {
            }
        }

        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { user ->
                startCloudSyncForUser(user, ctx)
            }
        } catch (_: Exception) {
        }
        refreshHomeFeed()
    }

    fun retryInternetConnection(context: android.content.Context): Boolean {
        val connected = hasActiveInternetConnection(context)
        if (connected) {
            onNetworkConnectivityRestored(context)
        } else {
            handleNetworkLost()
        }
        return connected
    }

    private var appContextRef: android.content.Context? = null
    private var localRepo: com.example.data.local.LocalPersistenceRepository? = null
    private var isLocalStorageLoaded: Boolean = false
    private val hiddenPostIdsCache = mutableSetOf<String>()

    fun initializeLocalStorage(context: android.content.Context) {
        val ctx = context.applicationContext
        appContextRef = ctx
        com.example.data.NetworkConnectivityManager.initialize(ctx)
        com.example.data.NiyonOwnerControlStore.initialize(ctx)
        com.example.util.AppSoundManager.init(ctx)
        _fakeIdReports.value = com.example.util.FakeIdDetector.loadReports(ctx)
        if (_savedEmailAccounts.value.isEmpty()) {
            _savedEmailAccounts.value = com.example.data.SavedAccountsStore.loadAccounts(ctx)
        }
        if (localRepo == null) {
            localRepo = com.example.data.local.LocalPersistenceRepository(ctx)
        }
        applyNiyonOwnerLocksToState()
        if (isLocalStorageLoaded) return
        isLocalStorageLoaded = true

        val repo = localRepo ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val postsSnapshot = repo.loadPostsSnapshot()
            val chatsSnapshot = repo.loadChatsSnapshot()

            synchronized(hiddenPostIdsCache) {
                hiddenPostIdsCache.addAll(postsSnapshot.hiddenPostIds)
            }

            if (com.example.data.NetworkConnectivityManager.isConnected.value && postsSnapshot.visiblePosts.isNotEmpty()) {
                val authenticRoomPosts = postsSnapshot.visiblePosts
                    .filter { isAuthenticPost(it) }
                val roomById = authenticRoomPosts.associateBy { it.id }
                val hiddenIds = postsSnapshot.hiddenPostIds
                val currentMerged = _posts.value
                    .filter { it.id !in hiddenIds && isAuthenticPost(it) }
                    .map { existing -> roomById[existing.id] ?: existing }
                val existingIds = currentMerged.map { it.id }.toSet()
                val extraFromRoom = authenticRoomPosts.filter { it.id !in existingIds && it.id !in hiddenIds }
                val mergedPosts = (extraFromRoom + currentMerged)
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }
                    .map { p ->
                        if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(p.id)) {
                            p.copy(isBoostActive = false, isBoosted = false)
                        } else p
                    }
                _posts.value = mergedPosts
                _savedPosts.value = mergedPosts.filter { it.isSaved }
            } else if (!com.example.data.NetworkConnectivityManager.isConnected.value) {
                _posts.value = emptyList()
                _savedPosts.value = emptyList()
                _stories.value = emptyList()
            }

            if (chatsSnapshot.messagesByConversation.isNotEmpty() || chatsSnapshot.deletedMessageIds.isNotEmpty()) {
                val deletedMsgIds = chatsSnapshot.deletedMessageIds
                val byConv = chatsSnapshot.messagesByConversation
                val updatedConvs = _conversations.value.map { conv ->
                    val roomMsgs = byConv[conv.id].orEmpty()
                    val existingMsgs = conv.messages.filter { it.id !in deletedMsgIds }
                    val existingIds = existingMsgs.map { it.id }.toSet()
                    val combinedMsgs = existingMsgs + roomMsgs.filter { it.id !in existingIds && it.id !in deletedMsgIds }
                    val lastMsg = combinedMsgs.lastOrNull()
                    if (lastMsg != null && roomMsgs.isNotEmpty()) {
                        conv.copy(
                            messages = combinedMsgs,
                            lastMessage = lastMsg.text.ifBlank { if (lastMsg.isAudioVoice) "🎤 অডিও ভয়েস" else "📷 মিডিয়া" },
                            lastTime = lastMsg.timestamp
                        )
                    } else if (existingMsgs.size != conv.messages.size) {
                        conv.copy(messages = combinedMsgs)
                    } else {
                        conv
                    }
                }
                _conversations.value = updatedConvs
                _activeChat.value?.let { active ->
                    updatedConvs.firstOrNull { it.id == active.id }?.let { refreshed ->
                        _activeChat.value = refreshed
                    }
                }
            }
        }
    }

    private fun persistPostToRoom(post: Post, isPinned: Boolean = false) {
        val repo = localRepo ?: appContextRef?.let {
            com.example.data.local.LocalPersistenceRepository(it).also { r -> localRepo = r }
        } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repo.upsertPost(post, isPinned = isPinned)
        }
    }

    private fun persistChatMessageToRoom(conversationId: String, message: ChatMessage) {
        val repo = localRepo ?: appContextRef?.let {
            com.example.data.local.LocalPersistenceRepository(it).also { r -> localRepo = r }
        } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repo.upsertChatMessage(conversationId, message)
        }
    }

    val hasMorePosts: StateFlow<Boolean> = com.example.data.FirebaseManager.hasMorePosts
    val isLoadingMorePosts: StateFlow<Boolean> = com.example.data.FirebaseManager.isLoadingMorePosts
    val cloudQuotaNotice: StateFlow<String?> = com.example.data.FirebaseManager.cloudQuotaNotice

    fun dismissQuotaNotice() {
        com.example.data.FirebaseManager.dismissQuotaNotice()
    }

    private val _savedEmailAccounts = MutableStateFlow<List<com.example.data.SavedEmailAccount>>(emptyList())
    val savedEmailAccounts: StateFlow<List<com.example.data.SavedEmailAccount>> = _savedEmailAccounts.asStateFlow()

    fun loadSavedEmailAccounts(context: android.content.Context) {
        appContextRef = context.applicationContext
        val loaded = com.example.data.SavedAccountsStore.loadAccounts(context)
        val merged = linkedMapOf<String, com.example.data.SavedEmailAccount>()
        loaded.forEach { acc -> merged[acc.email.trim().lowercase()] = acc }

        // Also include Firebase currentUser if signed in
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { fbUser ->
                val fbEmail = fbUser.email?.trim().orEmpty()
                if (fbEmail.isNotBlank()) {
                    val key = fbEmail.lowercase()
                    val isOwner = com.example.data.AuthSessionStore.isReservedOwnerEmail(key)
                    merged[key] = com.example.data.SavedEmailAccount(
                        id = fbUser.uid,
                        fullName = fbUser.displayName?.takeIf { it.isNotBlank() } ?: fbEmail.substringBefore("@"),
                        username = fbEmail.substringBefore("@"),
                        email = fbEmail,
                        avatarUrl = fbUser.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: "",
                        isFirebaseLinked = true,
                        isOwner = isOwner
                    )
                }
            }
        } catch (_: Exception) {
        }

        _savedEmailAccounts.value = merged.values.sortedByDescending { it.lastUsedAt }

        try {
            com.example.data.NiyonOwnerControlStore.initialize(context)
            val billingPrefs = context.applicationContext.getSharedPreferences(
                "fotofilm_billing_prefs",
                android.content.Context.MODE_PRIVATE
            )
            val legacyBlaze = billingPrefs.getBoolean("blaze_card_mode_enabled", false)
            _isBlazeCardModeEnabled.value =
                com.example.data.NiyonOwnerControlStore.isBlazeCardEnabled.value && legacyBlaze ||
                com.example.data.NiyonOwnerControlStore.isBlazeCardEnabled.value
        } catch (_: Exception) {
        }

        try {
            val cu = _currentUser.value
            com.example.data.DailyQuotaCompressionStore.initialize(
                context = context,
                currentUserId = cu.id,
                currentUserName = cu.fullName,
                currentAvatar = cu.avatarUrl,
                allUsers = _allAppUsers.value
            )
        } catch (_: Exception) {
        }

        applyNiyonOwnerLocksToState()
    }

    private fun recordSavedAccount(
        context: android.content.Context?,
        user: User,
        isFirebaseLinked: Boolean,
        isOwner: Boolean
    ) {
        val email = user.email.trim()
        if (email.isBlank()) return
        val entry = com.example.data.SavedEmailAccount(
            id = user.id,
            fullName = user.fullName,
            username = user.username,
            email = email,
            avatarUrl = user.avatarUrl,
            isFirebaseLinked = isFirebaseLinked || (com.example.data.FirebaseManager.activeAuthUid() != null),
            isOwner = isOwner
        )
        if (context != null) {
            _savedEmailAccounts.value = com.example.data.SavedAccountsStore.saveAccount(context, entry)
        } else {
            val existing = _savedEmailAccounts.value.filterNot { it.email.trim().equals(email, ignoreCase = true) }
            _savedEmailAccounts.value = listOf(entry) + existing
        }
    }

    fun restoreAuthSession(context: android.content.Context) {
        val ctx = context.applicationContext
        appContextRef = ctx
        try {
            val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (fbUser != null) {
                startCloudSyncForUser(fbUser, ctx)
                return
            }
        } catch (_: Exception) {
        }

        val savedSession = com.example.data.AuthSessionStore.loadActiveSession(ctx)
        if (savedSession != null) {
            val isReservedEmail = com.example.data.AuthSessionStore.isReservedOwnerEmail(savedSession.user.email)
            val isOwner = isReservedEmail
            setOwnerAuthenticated(isOwner)
            _currentUser.value = if (isOwner) {
                SampleData.currentUser.copy(
                    id = savedSession.user.id,
                    fullName = savedSession.user.fullName.ifBlank { SampleData.currentUser.fullName },
                    email = savedSession.user.email.ifBlank { SampleData.currentUser.email },
                    avatarUrl = savedSession.user.avatarUrl.ifBlank { SampleData.currentUser.avatarUrl },
                    isVerified = true,
                    isOnline = true
                )
            } else {
                savedSession.user.copy(isOnline = true, isVerified = savedSession.user.isVerified)
            }
            _isFidaVerified.value = isOwner || _currentUser.value.isVerified
            if (!isOwner) {
                closeRightManagerFida()
            }
            syncUserAccountsForCurrentUser(_currentUser.value)
            _authErrorMessage.value = null
            _isLoggedIn.value = true
            _showAuthDialog.value = false
        } else {
            setOwnerAuthenticated(false)
            closeRightManagerFida()
            _isFidaVerified.value = false
            _currentUser.value = createGuestUser()
            _userAccounts.value = emptyList()
            _isLoggedIn.value = false
        }
    }

    fun startCloudSyncForUser(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        context: android.content.Context? = null
    ) {
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx
        val emailLower = firebaseUser.email?.trim()?.lowercase().orEmpty()
        val isOwner = com.example.data.AuthSessionStore.isReservedOwnerEmail(emailLower)
        setOwnerAuthenticated(isOwner)

        if (isOwner) {
            _currentUser.value = SampleData.currentUser.copy(
                id = firebaseUser.uid,
                fullName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.fullName,
                email = firebaseUser.email?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.email,
                avatarUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.avatarUrl,
                isVerified = true,
                isOnline = true
            )
            _isFidaVerified.value = true
        } else {
            val resolvedName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: emailLower.substringBefore("@").ifBlank { "FotoFilm User" }
            val resolvedHandle = resolvedName.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user" }
            _currentUser.value = User(
                id = firebaseUser.uid,
                fullName = resolvedName,
                username = resolvedHandle,
                email = firebaseUser.email ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: "",
                bio = "FotoFilm সদস্য ✨",
                isVerified = false,
                isOnline = true
            )
            _isFidaVerified.value = false
            closeRightManagerFida()
        }

        recordSavedAccount(
            context = ctx,
            user = _currentUser.value,
            isFirebaseLinked = true,
            isOwner = isOwner
        )
        if (ctx != null) {
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = _currentUser.value,
                isOwnerVerified = isOwner,
                authMethod = "google"
            )
        }
        syncUserAccountsForCurrentUser(_currentUser.value)
        _authErrorMessage.value = null
        _isLoggedIn.value = true
        _showAuthDialog.value = false

        if (listenersStartedForUid == firebaseUser.uid) return

        stopCloudSync(keepLoggedIn = true)
        listenersStartedForUid = firebaseUser.uid

        com.example.data.FirebaseManager.syncSignedInUserProfile(_currentUser.value)

        postsListener = com.example.data.FirebaseManager.listenToPosts { cloudPosts ->
            if (!isInternetConnected.value) return@listenToPosts
            val hiddenIds = synchronized(hiddenPostIdsCache) { hiddenPostIdsCache.toSet() }
            val existingById = _posts.value.associateBy { it.id }
            val mergedCloud = cloudPosts
                .filter { it.id !in hiddenIds && isAuthenticPost(it) }
                .map { cp ->
                    val old = existingById[cp.id]
                    if (old != null) {
                        val preservedImages = if (old.images.any { it.startsWith("file://") } && cp.images.all { !it.startsWith("http") }) {
                            old.images
                        } else if (cp.images.isEmpty()) {
                            old.images
                        } else {
                            cp.images
                        }
                        val preservedAudio = when {
                            cp.audioTrack?.audioUrl?.startsWith("http") == true -> cp.audioTrack
                            old.audioTrack != null -> old.audioTrack
                            else -> cp.audioTrack
                        }
                        val basePost = cp.copy(
                            images = preservedImages,
                            audioTrack = preservedAudio,
                            comments = old.comments.ifEmpty { cp.comments },
                            userReaction = old.userReaction ?: cp.userReaction,
                            reactors = old.reactors.ifEmpty { cp.reactors },
                            isSaved = old.isSaved || cp.isSaved
                        )
                        if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(cp.id)) {
                            basePost.copy(isBoostActive = false, isBoosted = false)
                        } else basePost
                    } else if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(cp.id)) {
                        cp.copy(isBoostActive = false, isBoosted = false)
                    } else cp
                }
            val cloudIds = mergedCloud.map { it.id }.toSet()
            val localOnly = _posts.value.filter { it.id !in cloudIds && it.id !in hiddenIds && isAuthenticPost(it) }
            val finalMerged = (mergedCloud + localOnly)
                .map { p ->
                    if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(p.id)) {
                        p.copy(isBoostActive = false, isBoosted = false)
                    } else p
                }
                .sortedByDescending { it.createdAt }
            _posts.value = finalMerged
            _watchBadgeCount.value = finalMerged.count { it.isVideo || it.isReel }
            val repo = localRepo
            if (repo != null && mergedCloud.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    repo.upsertPosts(mergedCloud)
                }
            }
        }

        storiesListener = com.example.data.FirebaseManager.listenToStories { cloudStories ->
            if (!isInternetConnected.value) return@listenToStories
            val authenticCloud = cloudStories.filter { isAuthenticStory(it) }
            val cloudIds = authenticCloud.map { it.id }.toSet()
            val existingById = _stories.value.associateBy { it.id }
            val mergedCloud = authenticCloud.map { cs ->
                val old = existingById[cs.id]
                if (old != null) cs.copy(isViewed = old.isViewed) else cs
            }
            val localOnly = _stories.value.filter { it.id !in cloudIds && isAuthenticStory(it) }
            _stories.value = mergedCloud + localOnly
        }

        musicListener = com.example.data.FirebaseManager.listenToMusicTracks { cloudTracks ->
            val existingIds = cloudTracks.map { it.id }.toSet()
            val localTracks = _musicTracks.value.filter { it.id !in existingIds }
            _musicTracks.value = cloudTracks + localTracks
        }

        pagesListener = com.example.data.FirebaseManager.listenToPages { cloudPages ->
            val enforcedCloud = cloudPages.map { page ->
                if (com.example.data.NiyonOwnerControlStore.isPageLockedOff(page.id)) {
                    page.copy(isActive = false)
                } else page
            }
            val existingIds = enforcedCloud.map { it.id }.toSet()
            val localPages = _pages.value.filter { it.id !in existingIds }.map { page ->
                if (com.example.data.NiyonOwnerControlStore.isPageLockedOff(page.id)) {
                    page.copy(isActive = false)
                } else page
            }
            _pages.value = enforcedCloud + localPages
        }

        groupsListener = com.example.data.FirebaseManager.listenToGroups { cloudGroups ->
            val enforcedCloud = cloudGroups.map { group ->
                if (com.example.data.NiyonOwnerControlStore.isGroupLockedOff(group.id)) {
                    group.copy(isActive = false)
                } else group
            }
            val existingIds = enforcedCloud.map { it.id }.toSet()
            val localGroups = _groups.value.filter { it.id !in existingIds }.map { group ->
                if (com.example.data.NiyonOwnerControlStore.isGroupLockedOff(group.id)) {
                    group.copy(isActive = false)
                } else group
            }
            _groups.value = enforcedCloud + localGroups
        }

        incomingCallListener = com.example.data.FirebaseManager.listenToIncomingCalls { signal ->
            if (signal != null && _activeCall.value == null) {
                _incomingCallSignal.value = signal
                com.example.data.CloudinaryManager.getAppContext()?.let { ctx ->
                    val typeLabel = when (signal.callType) {
                        CallType.VIDEO -> "ভিডিও কল"
                        CallType.GROUP -> "গ্রুপ কল"
                        else -> "অডিও কল"
                    }
                    com.example.data.FotoFilmMessagingService.showSystemNotification(
                        context = ctx,
                        title = "📞 ইনকামিং $typeLabel • ${signal.callerName}",
                        body = "${signal.callerName} আপনাকে ChatFilm-এ কল করছেন...",
                        isCall = true
                    )
                }
            } else if (signal == null) {
                _incomingCallSignal.value = null
            }
        }
    }

    fun loadMorePosts() {
        com.example.data.FirebaseManager.loadMorePosts { olderPosts ->
            val existingIds = _posts.value.map { it.id }.toSet()
            val newUnique = olderPosts.filter { it.id !in existingIds }
            if (newUnique.isNotEmpty()) {
                _posts.value = _posts.value + newUnique
            }
        }
    }

    fun stopCloudSync(keepLoggedIn: Boolean = false) {
        postsListener?.remove()
        postsListener = null
        storiesListener?.remove()
        storiesListener = null
        musicListener?.remove()
        musicListener = null
        pagesListener?.remove()
        pagesListener = null
        groupsListener?.remove()
        groupsListener = null
        activeChatListener?.remove()
        activeChatListener = null
        incomingCallListener?.remove()
        incomingCallListener = null
        outgoingCallListener?.remove()
        outgoingCallListener = null
        commentListeners.values.forEach { it.remove() }
        commentListeners.clear()
        listenersStartedForUid = null
        _incomingCallSignal.value = null
        if (!keepLoggedIn) {
            setOwnerAuthenticated(false)
            closeRightManagerFida()
            _isLoggedIn.value = false
        }
    }

    fun deleteCurrentUserAccount(onComplete: (String) -> Unit) {
        com.example.data.FirebaseManager.deleteCurrentUserAccountData { _, msg ->
            stopCloudSync(keepLoggedIn = false)
            _showAuthDialog.value = true
            onComplete(msg)
        }
    }

    fun openCreatePostWithAudio(audioTrack: PostAudioTrack) {
        _pendingPostAudio.value = audioTrack
        _showCreatePostDialog.value = true
    }

    fun clearPendingPostAudio() {
        _pendingPostAudio.value = null
    }

    fun uploadMusicTrack(track: MusicTrack, onComplete: ((Boolean) -> Unit)? = null) {
        _musicTracks.value = listOf(track) + _musicTracks.value
        com.example.data.FirebaseManager.saveMusicTrack(track, onComplete)
    }

    // Firebase Cloud Database Status & Cloudinary + Agora Cloud Status
    val isFirebaseCloudConnected: StateFlow<Boolean> = com.example.data.FirebaseManager.isCloudConnected
    val firebaseCloudStatusMessage: StateFlow<String> = com.example.data.FirebaseManager.databaseStatusMessage
    val cloudinaryStatus: StateFlow<com.example.data.CloudinaryStatus> = com.example.data.CloudinaryManager.status
    val agoraCallState: StateFlow<com.example.data.AgoraRtcState> = com.example.data.AgoraCallManager.callState

    private val _isBlazeCardModeEnabled = MutableStateFlow(false)
    val isBlazeCardModeEnabled: StateFlow<Boolean> = _isBlazeCardModeEnabled.asStateFlow()

    val quotaCompressionConfig: StateFlow<com.example.data.QuotaCompressionConfig> =
        com.example.data.DailyQuotaCompressionStore.config
    val currentUserDailyUsage: StateFlow<com.example.data.UserDailyQuotaUsage> =
        com.example.data.DailyQuotaCompressionStore.currentUserUsage
    val allUsersDailyUsage: StateFlow<List<com.example.data.UserDailyQuotaUsage>> =
        com.example.data.DailyQuotaCompressionStore.userUsages

    private val _compressionToastMessage = MutableStateFlow<String?>(null)
    val compressionToastMessage: StateFlow<String?> = _compressionToastMessage.asStateFlow()

    fun clearCompressionToastMessage() {
        _compressionToastMessage.value = null
    }

    val isAdMobMasterEnabled: StateFlow<Boolean> = com.example.data.NiyonOwnerControlStore.isAdMobMasterEnabled
    val isAdMobBannerEnabled: StateFlow<Boolean> = com.example.data.NiyonOwnerControlStore.isAdMobBannerEnabled
    val isAdMobInterstitialEnabled: StateFlow<Boolean> = com.example.data.NiyonOwnerControlStore.isAdMobInterstitialEnabled
    val isAdMobRewardedEnabled: StateFlow<Boolean> = com.example.data.NiyonOwnerControlStore.isAdMobRewardedEnabled
    val niyonLockedOffCount: StateFlow<Int> = com.example.data.NiyonOwnerControlStore.lockedOffCount

    fun toggleAdMobMaster(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.NiyonOwnerControlStore.setAdMobMasterEnabled(enabled, isOwner = true)
    }

    fun toggleAdMobBanner(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.NiyonOwnerControlStore.setAdMobBannerEnabled(enabled, isOwner = true)
    }

    fun toggleAdMobInterstitial(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.NiyonOwnerControlStore.setAdMobInterstitialEnabled(enabled, isOwner = true)
    }

    fun toggleAdMobRewarded(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.NiyonOwnerControlStore.setAdMobRewardedEnabled(enabled, isOwner = true)
    }

    fun toggleAutoCompression(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.DailyQuotaCompressionStore.setAutoCompressionEnabled(enabled, isOwner = true)
    }

    fun toggleDaily43MbLimit(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.DailyQuotaCompressionStore.setDaily43MbLimitEnabled(enabled, isOwner = true)
    }

    fun resetUserDailyQuota(userId: String) {
        com.example.data.DailyQuotaCompressionStore.resetUserDailyUsage(userId, _currentUser.value.id)
    }

    fun resetAllUsersDailyQuota() {
        com.example.data.DailyQuotaCompressionStore.resetAllDailyUsage(_currentUser.value.id)
    }

    fun toggleBlazeCardMode(enabled: Boolean, context: android.content.Context): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        com.example.data.NiyonOwnerControlStore.setBlazeCardEnabled(enabled, isOwner = true)
        _isBlazeCardModeEnabled.value = enabled
        try {
            context.applicationContext
                .getSharedPreferences("fotofilm_billing_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("blaze_card_mode_enabled", enabled)
                .apply()
        } catch (_: Exception) {
        }
        return true
    }

    fun toggleCloudinaryEnabled(enabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) return false
        return com.example.data.CloudinaryManager.setEnabled(enabled, isOwner = true)
    }

    fun checkFirebaseCloudConnection() {
        com.example.data.CloudinaryManager.refreshStatus()
        com.example.data.AgoraCallManager.refreshConfigStatus()
    }

    fun toggleFreeMode() {
        _isFreeMode.value = !_isFreeMode.value
    }

    fun setFreeMode(enabled: Boolean) {
        _isFreeMode.value = enabled
    }

    fun setSimOperator(operator: SimOperator) {
        _currentSimOperator.value = operator
    }

    fun showFreeModeVideoWarning() {
        _showFreeModeVideoWarning.value = true
    }

    fun dismissFreeModeVideoWarning() {
        _showFreeModeVideoWarning.value = false
    }

    // Current language (defaults to Bengali)
    private val _currentLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // 4-Language ON/OFF status controlled by Niyon Islam Sohag
    private val _languageStatus = MutableStateFlow(
        mapOf(
            AppLanguage.BENGALI to true,
            AppLanguage.ENGLISH to true,
            AppLanguage.ARABIC to true,
            AppLanguage.HINDI to true
        )
    )
    val languageStatus: StateFlow<Map<AppLanguage, Boolean>> = _languageStatus.asStateFlow()

    // Current logged-in user (empty guest until Sign Up / Log In succeeds)
    private val _currentUser = MutableStateFlow(createGuestUser())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Auth status (strictly requires Sign Up or Log In on fresh install)
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    // Navigation state
    private val _activeTab = MutableStateFlow(AppTab.HOME)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _groupsPagesSelectedTab = MutableStateFlow(0) // 0 = Groups, 1 = Pages
    val groupsPagesSelectedTab: StateFlow<Int> = _groupsPagesSelectedTab.asStateFlow()

    fun openGroupsTab() {
        _groupsPagesSelectedTab.value = 0
        _activeTab.value = AppTab.GROUPS
    }

    fun openPagesTab() {
        _groupsPagesSelectedTab.value = 1
        _activeTab.value = AppTab.GROUPS
    }

    fun setGroupsPagesSubTab(index: Int) {
        _groupsPagesSelectedTab.value = index
    }

    // Global User Profile Navigation
    private val _viewingProfileUser = MutableStateFlow<User?>(null)
    val viewingProfileUser: StateFlow<User?> = _viewingProfileUser.asStateFlow()

    // Blocked Users State Flow & Profiles (real runtime blocks only)
    private val _blockedProfiles = MutableStateFlow<List<User>>(emptyList())
    val blockedProfiles: StateFlow<List<User>> = _blockedProfiles.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    // Restricted Profiles State Flow (real runtime restrictions only)
    private val _restrictedProfiles = MutableStateFlow<List<User>>(emptyList())
    val restrictedProfiles: StateFlow<List<User>> = _restrictedProfiles.asStateFlow()

    // Permanent Message Delivery Vault (real delivered messages only)
    private val _permanentDeliveryVault = MutableStateFlow<List<DeliveredVaultMessage>>(emptyList())
    val permanentDeliveryVault: StateFlow<List<DeliveredVaultMessage>> = _permanentDeliveryVault.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Posts (strictly real users & Cloudinary media from Firestore/Room — never fake demo posts)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Admin Payment Receiving Numbers for Boost & Promotions (strictly 01842450605 for bKash & Nagad)
    private val _adminBkashNumber = MutableStateFlow("01842450605")
    val adminBkashNumber: StateFlow<String> = _adminBkashNumber.asStateFlow()

    private val _adminNagadNumber = MutableStateFlow("01842450605")
    val adminNagadNumber: StateFlow<String> = _adminNagadNumber.asStateFlow()

    private val _adminRocketNumber = MutableStateFlow("01842450605")
    val adminRocketNumber: StateFlow<String> = _adminRocketNumber.asStateFlow()

    // Saved Videos & Posts (strictly real user saved items)
    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    fun savePost(post: Post) {
        val savedCopy = post.copy(isSaved = true)
        _posts.value = _posts.value.map { if (it.id == post.id) it.copy(isSaved = true) else it }
        if (_savedPosts.value.none { it.id == post.id }) {
            _savedPosts.value = listOf(savedCopy) + _savedPosts.value
        } else {
            _savedPosts.value = _savedPosts.value.map { if (it.id == post.id) savedCopy else it }
        }
        persistPostToRoom(savedCopy)
    }

    fun unsavePost(postId: String) {
        _savedPosts.value = _savedPosts.value.filter { it.id != postId }
        var updatedRef: Post? = null
        _posts.value = _posts.value.map {
            if (it.id == postId) {
                val u = it.copy(isSaved = false)
                updatedRef = u
                u
            } else it
        }
        updatedRef?.let { persistPostToRoom(it) }
    }

    fun isPostSaved(postId: String): Boolean {
        return _savedPosts.value.any { it.id == postId }
    }

    // Boost & Ads Transactions (real user boost transactions only)
    private val _boostTransactions = MutableStateFlow<List<com.example.model.BoostTransaction>>(emptyList())
    val boostTransactions: StateFlow<List<com.example.model.BoostTransaction>> = _boostTransactions.asStateFlow()

    fun updateAdminPaymentNumbers(bkash: String, nagad: String, rocket: String) {
        if (bkash.isNotBlank()) _adminBkashNumber.value = bkash
        if (nagad.isNotBlank()) _adminNagadNumber.value = nagad
        if (rocket.isNotBlank()) _adminRocketNumber.value = rocket
    }

    fun boostPost(
        postId: String,
        amountBdt: Int,
        durationDays: Int,
        targetReach: String,
        paymentMethod: String,
        senderNumber: String,
        trxId: String,
        goal: String,
        likesToAdd: Int = 0,
        followersToAdd: Int = 0,
        viewsToAdd: Int = 0
    ) {
        val checkTarget = _posts.value.firstOrNull { it.id == postId }
        if (checkTarget != null) {
            val safety = com.example.util.SafetyPolicyValidator.validateContent(
                checkTarget.text + " " + (checkTarget.title ?: "") + " " + (checkTarget.videoTitle ?: "")
            )
            if (!safety.isAllowed) {
                _safetyPolicyAlert.value = "⛔ এই পোস্টটি বুস্ট করা যাবে না: ${safety.reasonMessage}"
                return
            }
        }
        val isFreeOrOwner = amountBdt == 0 || paymentMethod.contains("ফ্রী") || trxId.startsWith("VIP-")
        val status = if (isFreeOrOwner) "ACTIVE" else "PENDING"

        if (status == "ACTIVE" && (likesToAdd > 0 || followersToAdd > 0)) {
            applyRealBoostEngagement(postId, likesToAdd, followersToAdd, viewsToAdd)
        }

        var boostedPost: Post? = null
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) {
                val updated = p.copy(
                    isBoosted = true,
                    boostStatus = status,
                    boostBudget = amountBdt,
                    boostDays = durationDays,
                    boostTargetReach = targetReach,
                    boostTrxId = trxId,
                    reachViews = "$targetReach 🚀"
                )
                boostedPost = updated
                updated
            } else p
        }

        val target = boostedPost ?: _posts.value.firstOrNull { it.id == postId }
        val newTx = com.example.model.BoostTransaction(
            id = "boost_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            postTitleOrText = target?.title?.takeIf { it.isNotBlank() } ?: target?.videoTitle?.takeIf { it.isNotBlank() } ?: target?.text?.take(60) ?: "Boosted Content",
            isVideo = target?.isVideo ?: false,
            authorName = target?.author?.fullName ?: _currentUser.value.fullName,
            paymentMethod = paymentMethod,
            senderNumber = senderNumber,
            trxId = trxId,
            amountBdt = amountBdt,
            durationDays = durationDays,
            estimatedReach = targetReach,
            goal = goal,
            status = status,
            createdAt = "০৭ অক্টোবর ২০২৬"
        )
        _boostTransactions.value = listOf(newTx) + _boostTransactions.value

        boostedPost?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    // =========================================================================
    // ১১তম ফিচার: প্রধান মালিক (Niyon Islam Sohag) এর ১০০% ফ্রী মেগা বুস্ট (১M+ রিচ)
    // শুধুমাত্র Niyon Islam Sohag ফ্রীতে নিজের যেকোনো পোস্ট বুস্ট করতে পারবেন!
    // অন্য কোনো সাধারণ ব্যক্তি বা অ্যাডমিন টাকা ছাড়া ফ্রী বুস্ট করতে পারবে না।
    // =========================================================================
    fun ownerFreeMegaBoost(postId: String, reachMultiplier: String = "১.৫ মিলিয়ন (1.5M Reach)") {
        val user = _currentUser.value
        val isOwner = user.id == "user_me" || user.id == "user_niyon" || user.fullName.contains("Niyon", ignoreCase = true)
        if (!isOwner) return // কঠোরভাবে যাচাই: শুধুমাত্র প্রধান মালিকের জন্য প্রযোজ্য!

        val checkTarget = _posts.value.firstOrNull { it.id == postId }
        if (checkTarget != null) {
            val safety = com.example.util.SafetyPolicyValidator.validateContent(
                checkTarget.text + " " + (checkTarget.title ?: "") + " " + (checkTarget.videoTitle ?: "")
            )
            if (!safety.isAllowed) {
                _safetyPolicyAlert.value = "⛔ এই পোস্টটি বুস্ট করা যাবে না: ${safety.reasonMessage}"
                return
            }
        }

        var boostedPost: Post? = null
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) {
                val updated = p.copy(
                    isBoosted = true,
                    boostStatus = "ACTIVE",
                    boostBudget = 0,
                    boostDays = 365,
                    boostTargetReach = reachMultiplier,
                    boostTrxId = "VIP-OWNER-FREE-0BDT",
                    reachViews = "$reachMultiplier 🚀"
                )
                boostedPost = updated
                updated
            } else p
        }

        val target = boostedPost ?: _posts.value.firstOrNull { it.id == postId }
        val newTx = com.example.model.BoostTransaction(
            id = "boost_owner_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            postTitleOrText = target?.title?.takeIf { it.isNotBlank() } ?: target?.videoTitle?.takeIf { it.isNotBlank() } ?: target?.text?.take(60) ?: "Owner VIP Post",
            isVideo = target?.isVideo ?: false,
            authorName = "${_currentUser.value.fullName} (প্রধান মালিক)",
            paymentMethod = "👑 ওনার স্পেশাল ফ্রী (৳০)",
            senderNumber = "Owner VIP",
            trxId = "VIP-MILLION-VIRAL-FREE",
            amountBdt = 0,
            durationDays = 365,
            estimatedReach = reachMultiplier,
            goal = "মিলিয়ন মানুষের কাছে পৌঁছে দেওয়া",
            status = "ACTIVE",
            createdAt = "০১/০১/২০২৬ (1/1/2026)"
        )
        _boostTransactions.value = listOf(newTx) + _boostTransactions.value
        boostedPost?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    // =========================================================================
    // রিয়েল লাইক (১০টি রিয়্যাকশন), ফলোয়ার ও ভিউ বৃদ্ধি করার ইঞ্জিন
    // =========================================================================
    fun applyRealBoostEngagement(
        postId: String,
        likesToAdd: Int,
        followersToAdd: Int,
        viewsToAdd: Int
    ) {
        val rLike = (likesToAdd * 0.32).toInt()
        val rLove = (likesToAdd * 0.34).toInt()
        val rCare = (likesToAdd * 0.12).toInt()
        val rHaha = (likesToAdd * 0.08).toInt()
        val rHundred = (likesToAdd * 0.06).toInt()
        val rWow = (likesToAdd * 0.04).toInt()
        val rOk = (likesToAdd * 0.02).toInt()
        val rSad = (likesToAdd * 0.01).toInt()
        val rDislike = (likesToAdd * 0.005).toInt()
        val rAngry = likesToAdd - (rLike + rLove + rCare + rHaha + rHundred + rWow + rOk + rSad + rDislike)

        var targetAuthorId: String? = null
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) {
                targetAuthorId = p.author.id
                val updatedMap = p.reactionCounts.toMutableMap()
                updatedMap[ReactionType.LIKE] = (updatedMap[ReactionType.LIKE] ?: 0) + rLike
                updatedMap[ReactionType.LOVE] = (updatedMap[ReactionType.LOVE] ?: 0) + rLove
                updatedMap[ReactionType.CARE] = (updatedMap[ReactionType.CARE] ?: 0) + rCare
                updatedMap[ReactionType.HAHA] = (updatedMap[ReactionType.HAHA] ?: 0) + rHaha
                updatedMap[ReactionType.HUNDRED] = (updatedMap[ReactionType.HUNDRED] ?: 0) + rHundred
                updatedMap[ReactionType.WOW] = (updatedMap[ReactionType.WOW] ?: 0) + rWow
                updatedMap[ReactionType.OK] = (updatedMap[ReactionType.OK] ?: 0) + rOk
                updatedMap[ReactionType.SAD] = (updatedMap[ReactionType.SAD] ?: 0) + rSad
                updatedMap[ReactionType.DISLIKE] = (updatedMap[ReactionType.DISLIKE] ?: 0) + rDislike
                updatedMap[ReactionType.ANGRY] = (updatedMap[ReactionType.ANGRY] ?: 0) + rAngry

                val currentViewsNum = p.reachViews.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1200
                val totalViews = currentViewsNum + (viewsToAdd / 1000)

                p.copy(
                    isBoosted = true,
                    boostStatus = "ACTIVE",
                    reactionCounts = updatedMap,
                    reachViews = "${totalViews}K+ ভিউ 🚀"
                )
            } else p
        }

        // Add real followers to author/user
        val targetUser = targetAuthorId ?: _currentUser.value.id
        addFollowersToUser(targetUser, followersToAdd)
    }

    fun addFollowersToUser(userId: String, count: Int) {
        if (_currentUser.value.id == userId || userId == "user_me") {
            _currentUser.value = _currentUser.value.copy(
                followerCount = _currentUser.value.followerCount + count
            )
        }
        _allAppUsers.value = _allAppUsers.value.map { u ->
            if (u.id == userId) u.copy(followerCount = u.followerCount + count) else u
        }
    }

    /**
     * Niyon Islam Sohag এর প্রতিটি প্যাকেজে ১০০% ফ্রী বুস্ট ও প্রোমোট করার স্পেশাল পাওয়ার
     */
    fun ownerFreeBoostPackage(postId: String, pkg: BoostPackage) {
        val user = _currentUser.value
        val isOwner = user.id == "user_me" || user.id == "user_niyon" || user.fullName.contains("Niyon", ignoreCase = true)
        if (!isOwner) return

        val checkTarget = _posts.value.firstOrNull { it.id == postId }
        if (checkTarget != null) {
            val safety = com.example.util.SafetyPolicyValidator.validateContent(
                checkTarget.text + " " + (checkTarget.title ?: "") + " " + (checkTarget.videoTitle ?: "")
            )
            if (!safety.isAllowed) {
                _safetyPolicyAlert.value = "⛔ এই পোস্টটি বুস্ট করা যাবে না: ${safety.reasonMessage}"
                return
            }
        }

        // Apply real likes, 10 reactions, followers, and views immediately!
        applyRealBoostEngagement(
            postId = postId,
            likesToAdd = pkg.likesCount,
            followersToAdd = pkg.followersCount,
            viewsToAdd = pkg.viewsCount
        )

        val target = _posts.value.firstOrNull { it.id == postId }
        val newTx = com.example.model.BoostTransaction(
            id = "boost_owner_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            postTitleOrText = target?.title?.takeIf { it.isNotBlank() } ?: target?.videoTitle?.takeIf { it.isNotBlank() } ?: target?.text?.take(60) ?: "Owner Content",
            isVideo = target?.isVideo ?: false,
            authorName = "${_currentUser.value.fullName} (প্রধান মালিক)",
            paymentMethod = "👑 ওনার ফ্রী বুস্ট (৳০)",
            senderNumber = "01842450605 (Owner VIP)",
            trxId = "VIP-FREE-BOOST-${pkg.priceBdt}BDT",
            amountBdt = 0,
            durationDays = pkg.durationDays,
            estimatedReach = pkg.estimatedReach,
            goal = "${pkg.likesCount} রিয়েল লাইক + ${pkg.followersCount} ফলোয়ার",
            status = "ACTIVE",
            createdAt = "০৭ অক্টোবর ২০২৬"
        )
        _boostTransactions.value = listOf(newTx) + _boostTransactions.value
    }

    fun updateBoostStatus(transactionId: String, newStatus: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setBoostTxStatus(transactionId, newStatus, isOwner = true)
        _boostTransactions.value = _boostTransactions.value.map { tx ->
            if (tx.id == transactionId) {
                if (newStatus == "ACTIVE" && tx.status != "ACTIVE") {
                    // Apply real engagement based on amount
                    val (likes, followers, views) = when (tx.amountBdt) {
                        200 -> Triple(10000, 3000, 50000)
                        300 -> Triple(20000, 5000, 100000)
                        500 -> Triple(25000, 7000, 150000)
                        1000 -> Triple(30000, 10000, 200000)
                        2000 -> Triple(40000, 25000, 350000)
                        3000 -> Triple(50000, 40000, 500000)
                        else -> Triple(10000, 3000, 50000)
                    }
                    applyRealBoostEngagement(tx.postId, likes, followers, views)
                }
                tx.copy(status = newStatus)
            } else tx
        }
    }

    fun approveBoostTransaction(transactionId: String) {
        updateBoostStatus(transactionId, "ACTIVE")
    }

    fun deleteBoostTransaction(transactionId: String) {
        _boostTransactions.value = _boostTransactions.value.filter { it.id != transactionId }
    }

    // Stories (strictly real users' uploaded Cloudinary/Firestore stories)
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _viewingStory = MutableStateFlow<Story?>(null)
    val viewingStory: StateFlow<Story?> = _viewingStory.asStateFlow()

    // Highlights (Friends এর নিচে হাইলাইটস — user-created stories only)
    private val _userHighlights = MutableStateFlow<Map<String, List<StoryHighlight>>>(emptyMap())
    val userHighlights: StateFlow<Map<String, List<StoryHighlight>>> = _userHighlights.asStateFlow()

    fun addStoryToHighlights(userId: String, story: Story, highlightTitle: String = "My Stories 🌟") {
        val currentList = _userHighlights.value[userId] ?: emptyList()
        val targetHighlight = currentList.find { it.title == highlightTitle }
        val updatedList = if (targetHighlight != null) {
            currentList.map {
                if (it.id == targetHighlight.id) {
                    it.copy(
                        stories = listOf(story) + it.stories,
                        coverUrl = story.mediaUrl
                    )
                } else it
            }
        } else {
            val newHl = StoryHighlight(
                id = "hl_${UUID.randomUUID().toString().take(6)}",
                userId = userId,
                title = highlightTitle,
                coverUrl = story.mediaUrl,
                stories = listOf(story),
                createdAt = "আজকে"
            )
            listOf(newHl) + currentList
        }
        _userHighlights.value = _userHighlights.value + (userId to updatedList)
    }

    fun createNewHighlight(userId: String, title: String, coverUrl: String, stories: List<Story> = emptyList()) {
        val newHl = StoryHighlight(
            id = "hl_${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            title = title.ifBlank { "Highlight" },
            coverUrl = coverUrl.ifBlank { _currentUser.value.avatarUrl },
            stories = stories,
            createdAt = "আজকে"
        )
        val currentList = _userHighlights.value[userId] ?: emptyList()
        _userHighlights.value = _userHighlights.value + (userId to (listOf(newHl) + currentList))
    }

    // Conversations
    private val _conversations = MutableStateFlow(SampleData.initialConversations)
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeChat = MutableStateFlow<Conversation?>(null)
    val activeChat: StateFlow<Conversation?> = _activeChat.asStateFlow()

    private val _typingConversationId = MutableStateFlow<String?>(null)
    val typingConversationId: StateFlow<String?> = _typingConversationId.asStateFlow()

    // Calling state
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()
    private val _incomingCallSignal = MutableStateFlow<com.example.data.FirebaseManager.IncomingCallSignal?>(null)
    val incomingCallSignal: StateFlow<com.example.data.FirebaseManager.IncomingCallSignal?> = _incomingCallSignal.asStateFlow()
    private var incomingCallListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var outgoingCallListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var callTimerJob: Job? = null

    // Support Team User Messages & Tickets (Screenshot 2 and Right Manager Fida Option)
    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(SampleData.sampleSupportTickets)
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    // Friends
    private val _pendingRequests = MutableStateFlow(SampleData.pendingRequestUsers)
    val pendingRequests: StateFlow<List<User>> = _pendingRequests.asStateFlow()

    private val _suggestedUsers = MutableStateFlow(SampleData.suggestedUsers)
    val suggestedUsers: StateFlow<List<User>> = _suggestedUsers.asStateFlow()

    // Groups & Pages
    private val _groups = MutableStateFlow(SampleData.initialGroups)
    val groups: StateFlow<List<SocialGroup>> = _groups.asStateFlow()

    private val _pages = MutableStateFlow(SampleData.initialPages)
    val pages: StateFlow<List<SocialPage>> = _pages.asStateFlow()

    fun togglePageStatus(pageId: String, isActive: Boolean) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setPageActive(pageId, isActive, isOwner = true)
        var updatedPage: SocialPage? = null
        _pages.value = _pages.value.map { page ->
            if (page.id == pageId) {
                val updated = page.copy(isActive = isActive)
                updatedPage = updated
                updated
            } else page
        }
        updatedPage?.let { com.example.data.FirebaseManager.saveSocialPage(it) }
    }

    fun toggleGroupStatus(groupId: String, isActive: Boolean) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setGroupActive(groupId, isActive, isOwner = true)
        var updatedGroup: SocialGroup? = null
        _groups.value = _groups.value.map { group ->
            if (group.id == groupId) {
                val updated = group.copy(isActive = isActive)
                updatedGroup = updated
                updated
            } else group
        }
        updatedGroup?.let { com.example.data.FirebaseManager.saveSocialGroup(it) }
    }

    private val _hangoutPresetRooms = MutableStateFlow<List<HangoutRoomControlEntry>>(emptyList())
    val hangoutPresetRooms: StateFlow<List<HangoutRoomControlEntry>> = _hangoutPresetRooms.asStateFlow()

    fun toggleHangoutPresetRoomStatus(roomKey: String, isActive: Boolean) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setHangoutRoomActive(roomKey, isActive, isOwner = true)
        _hangoutPresetRooms.value = _hangoutPresetRooms.value.map { room ->
            if (room.roomKey == roomKey) room.copy(isActive = isActive) else room
        }
    }

    fun isHangoutRoomActive(roomKey: String): Boolean {
        if (roomKey.startsWith("group_")) {
            val gId = roomKey.removePrefix("group_")
            return _groups.value.find { it.id == gId }?.isActive ?: true
        }
        return _hangoutPresetRooms.value.find { it.roomKey == roomKey }?.isActive ?: true
    }

    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Dynamic Header Notification Badges (zero on fresh clean launch until real activity arrives)
    private val _homeBadgeCount = MutableStateFlow(0)
    val homeBadgeCount: StateFlow<Int> = _homeBadgeCount.asStateFlow()

    private val _friendsBadgeCount = MutableStateFlow(0)
    val friendsBadgeCount: StateFlow<Int> = _friendsBadgeCount.asStateFlow()

    private val _messagesBadgeCount = MutableStateFlow(0)
    val messagesBadgeCount: StateFlow<Int> = _messagesBadgeCount.asStateFlow()

    private val _hasNewMessageDot = MutableStateFlow(false)
    val hasNewMessageDot: StateFlow<Boolean> = _hasNewMessageDot.asStateFlow()

    private val _watchBadgeCount = MutableStateFlow(0)
    val watchBadgeCount: StateFlow<Int> = _watchBadgeCount.asStateFlow()

    private val _notificationsBadgeCount = MutableStateFlow(0)
    val notificationsBadgeCount: StateFlow<Int> = _notificationsBadgeCount.asStateFlow()

    // Notification Settings: Sound & Vibration
    private val _isNotificationSoundEnabled = MutableStateFlow(true)
    val isNotificationSoundEnabled: StateFlow<Boolean> = _isNotificationSoundEnabled.asStateFlow()

    private val _isNotificationVibrationEnabled = MutableStateFlow(true)
    val isNotificationVibrationEnabled: StateFlow<Boolean> = _isNotificationVibrationEnabled.asStateFlow()

    private val _showNotificationDropdown = MutableStateFlow(false)
    val showNotificationDropdown: StateFlow<Boolean> = _showNotificationDropdown.asStateFlow()

    private val _isHomeRefreshing = MutableStateFlow(false)
    val isHomeRefreshing: StateFlow<Boolean> = _isHomeRefreshing.asStateFlow()

    private val _postUploadProgress = MutableStateFlow<PostUploadProgress?>(null)
    val postUploadProgress: StateFlow<PostUploadProgress?> = _postUploadProgress.asStateFlow()

    fun dismissPostUploadProgress() {
        _postUploadProgress.value = null
    }

    fun clearHomeBadge() {
        _homeBadgeCount.value = 0
    }

    fun refreshHomeFeed() {
        if (!requireInternetConnection()) {
            handleNetworkLost()
            return
        }
        if (_isHomeRefreshing.value) return
        _isHomeRefreshing.value = true
        _homeBadgeCount.value = 0

        viewModelScope.launch {
            try {
                val startedAt = System.currentTimeMillis()
                val hiddenIds = synchronized(hiddenPostIdsCache) { hiddenPostIdsCache.toSet() }
                val existingById = _posts.value.associateBy { it.id }

                val cloudResult = com.example.data.FirebaseManager.fetchPostsPageAwait()
                if (cloudResult.isSuccess) {
                    val cloudPosts = cloudResult.getOrNull()
                        ?.filter { it.id !in hiddenIds && isAuthenticPost(it) }
                        .orEmpty()
                    val mergedCloud = cloudPosts.map { cp ->
                        val old = existingById[cp.id]
                        if (old != null) {
                            val preservedImages = if (old.images.any { it.startsWith("file://") } && cp.images.all { !it.startsWith("http") }) {
                                old.images
                            } else if (cp.images.isEmpty()) {
                                old.images
                            } else {
                                cp.images
                            }
                            val preservedAudio = when {
                                cp.audioTrack?.audioUrl?.startsWith("http") == true -> cp.audioTrack
                                old.audioTrack != null -> old.audioTrack
                                else -> cp.audioTrack
                            }
                            val basePost = cp.copy(
                                images = preservedImages,
                                audioTrack = preservedAudio,
                                comments = old.comments.ifEmpty { cp.comments },
                                userReaction = old.userReaction ?: cp.userReaction,
                                reactors = old.reactors.ifEmpty { cp.reactors },
                                isSaved = old.isSaved || cp.isSaved
                            )
                            if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(cp.id)) {
                                basePost.copy(isBoostActive = false, isBoosted = false)
                            } else basePost
                        } else if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(cp.id)) {
                            cp.copy(isBoostActive = false, isBoosted = false)
                        } else cp
                    }
                    val cloudIds = mergedCloud.map { it.id }.toSet()
                    val localOnly = _posts.value.filter { it.id !in cloudIds && it.id !in hiddenIds && isAuthenticPost(it) }
                    val finalMerged = (mergedCloud + localOnly)
                        .map { p ->
                            if (com.example.data.NiyonOwnerControlStore.isPostBoostLockedOff(p.id)) {
                                p.copy(isBoostActive = false, isBoosted = false)
                            } else p
                        }
                        .sortedByDescending { it.createdAt }
                    _posts.value = finalMerged
                    _watchBadgeCount.value = finalMerged.count { it.isVideo || it.isReel }
                    if (mergedCloud.isNotEmpty()) {
                        localRepo?.let { repo ->
                            launch(Dispatchers.IO) { repo.upsertPosts(mergedCloud) }
                        }
                    }
                } else {
                    _posts.value = _posts.value.filter { isAuthenticPost(it) }
                }

                val storiesResult = com.example.data.FirebaseManager.fetchStoriesAwait()
                if (storiesResult.isSuccess) {
                    val cloudStories = storiesResult.getOrNull()?.filter { isAuthenticStory(it) }.orEmpty()
                    val cloudStoryIds = cloudStories.map { it.id }.toSet()
                    val existingStoryById = _stories.value.associateBy { it.id }
                    val mergedCloudStories = cloudStories.map { cs ->
                        val old = existingStoryById[cs.id]
                        if (old != null) cs.copy(isViewed = old.isViewed) else cs
                    }
                    val localStories = _stories.value.filter { it.id !in cloudStoryIds && isAuthenticStory(it) }
                    _stories.value = mergedCloudStories + localStories
                }

                val elapsed = System.currentTimeMillis() - startedAt
                if (elapsed < 650L) {
                    delay(650L - elapsed)
                }
            } catch (_: Exception) {
                // Keep UI stable if Firestore/network call fails before Google Sign-In
            } finally {
                _isHomeRefreshing.value = false
            }
        }
    }

    fun incrementHomeBadge() {
        _homeBadgeCount.value += 1
    }

    fun clearFriendsBadge() {
        _friendsBadgeCount.value = 0
    }

    fun clearMessagesBadge() {
        _messagesBadgeCount.value = 0
        _hasNewMessageDot.value = false
        _conversations.value = _conversations.value.map { it.copy(unreadCount = 0) }
    }

    fun clearWatchBadge() {
        _watchBadgeCount.value = 0
    }

    fun clearNotificationsBadge() {
        _notificationsBadgeCount.value = 0
    }

    fun toggleNotificationDropdown() {
        val next = !_showNotificationDropdown.value
        _showNotificationDropdown.value = next
        if (next) {
            clearNotificationsBadge()
        }
    }

    fun closeNotificationDropdown() {
        _showNotificationDropdown.value = false
    }

    fun toggleNotificationSound() {
        _isNotificationSoundEnabled.value = !_isNotificationSoundEnabled.value
    }

    fun toggleNotificationVibration() {
        _isNotificationVibrationEnabled.value = !_isNotificationVibrationEnabled.value
    }

    fun triggerNotificationFeedback(context: android.content.Context? = null) {
        if (_isNotificationSoundEnabled.value) {
            com.example.util.AppSoundManager.playNotificationChime()
        }
        if (_isNotificationVibrationEnabled.value && context != null) {
            com.example.util.AppSoundManager.triggerVibration(context)
        }
    }

    // Feature 12: খারাপ মন্তব্যকারী ও সাইবার অপরাধী অডিট (আইনগত প্রোফাইল)
    private val _cyberOffenders = MutableStateFlow(SampleData.initialCyberOffenders)
    val cyberOffenders: StateFlow<List<CyberOffenderReport>> = _cyberOffenders.asStateFlow()

    // Feature 13: অ্যাপ আপডেট নোটিশ ব্রডকাস্টার
    private val _appUpdateBroadcasts = MutableStateFlow(SampleData.initialAppUpdateBroadcasts)
    val appUpdateBroadcasts: StateFlow<List<AppUpdateBroadcast>> = _appUpdateBroadcasts.asStateFlow()

    private val _latestAppUpdate = MutableStateFlow<AppUpdateBroadcast?>(SampleData.initialAppUpdateBroadcasts.firstOrNull())
    val latestAppUpdate: StateFlow<AppUpdateBroadcast?> = _latestAppUpdate.asStateFlow()

    private val _showAppUpdateNoticeDialog = MutableStateFlow(false)
    val showAppUpdateNoticeDialog: StateFlow<Boolean> = _showAppUpdateNoticeDialog.asStateFlow()

    fun dismissAppUpdateNoticeDialog() {
        _showAppUpdateNoticeDialog.value = false
    }

    fun openAppUpdateNoticeDialog() {
        _showAppUpdateNoticeDialog.value = true
    }

    // Safety policy warning message (Drugs & Gambling content blocker)
    private val _safetyPolicyAlert = MutableStateFlow<String?>(null)
    val safetyPolicyAlert: StateFlow<String?> = _safetyPolicyAlert.asStateFlow()

    fun clearSafetyPolicyAlert() {
        _safetyPolicyAlert.value = null
    }

    fun setSafetyPolicyAlert(msg: String) {
        _safetyPolicyAlert.value = msg
    }

    fun takeLegalActionAgainstOffender(reportId: String, actionType: String) {
        _cyberOffenders.value = _cyberOffenders.value.map { r ->
            if (r.id == reportId) {
                when (actionType) {
                    "BAN" -> {
                        toggleUserAccountStatus(r.userId, active = false, reason = "সাইবার নিরাপত্তা আইন ২০২৩ লঙ্ঘনের কারণে অ্যাকাউন্ট ও ডিভাইস স্থায়ীভাবে ব্যান করা হয়েছে")
                        r.copy(status = "PERMANENT_BANNED")
                    }
                    "NOTICE" -> r.copy(status = "LEGAL_NOTICE_SENT")
                    "PARDON" -> {
                        toggleUserAccountStatus(r.userId, active = true)
                        r.copy(status = "UNDER_REVIEW")
                    }
                    else -> r
                }
            } else r
        }
    }

    fun reportCyberHarassment(userId: String, userName: String, comment: String, postTitle: String) {
        val targetAvatar = _allAppUsers.value.find { it.id == userId }?.avatarUrl.orEmpty()
        val newReport = CyberOffenderReport(
            id = "offender_${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            userName = userName,
            userUsername = userName.lowercase().replace(" ", "_"),
            userAvatar = targetAvatar,
            reportedComment = comment,
            targetPostTitle = postTitle,
            timestamp = "এইমাত্র",
            nationalId = "",
            phoneNumber = "",
            ipAddress = "",
            deviceModel = "Android Device",
            location = "বাংলাদেশ",
            legalViolation = "সাইবার নিরাপত্তা আইন ২০২৩ ধারা ২৫ ও ২৯ (অনলাইন অবমাননা ও কুরুচিপূর্ণ উক্তি)",
            status = "UNDER_REVIEW"
        )
        _cyberOffenders.value = listOf(newReport) + _cyberOffenders.value
    }

    fun broadcastAppUpdateNotice(version: String, title: String, description: String) {
        val owner = _currentUser.value
        val update = AppUpdateBroadcast(
            id = "update_${UUID.randomUUID().toString().take(6)}",
            version = version.ifBlank { "v3.0.0" },
            title = title.ifBlank { "FotoFilm নতুন আপডেট প্রকাশিত হয়েছে!" },
            description = description.ifBlank { "সকল ফিচারের চমৎকার পারফরম্যান্স ও নতুন সুবিধা যুক্ত করা হয়েছে।" },
            appLogoUrl = "",
            ownerAvatar = owner.avatarUrl,
            ownerName = owner.fullName,
            releaseDate = "এইমাত্র",
            isActive = true
        )
        _appUpdateBroadcasts.value = listOf(update) + _appUpdateBroadcasts.value
        _latestAppUpdate.value = update
        _showAppUpdateNoticeDialog.value = true

        // Broadcast notification to ALL users
        val newNotification = AppNotification(
            id = "notif_update_${UUID.randomUUID().toString().take(6)}",
            actor = owner,
            title = "📢 নতুন অ্যাপ আপডেট: ${update.title} ($version)",
            timeAgo = "এইমাত্র",
            type = NotificationType.APP_UPDATE,
            isRead = false,
            targetPreview = update.description.take(80),
            appUpdateVersion = update.version
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    // Create post dialog state
    private val _showCreatePostDialog = MutableStateFlow(false)
    val showCreatePostDialog: StateFlow<Boolean> = _showCreatePostDialog.asStateFlow()

    private val _pendingPostImages = MutableStateFlow<List<String>>(emptyList())
    val pendingPostImages: StateFlow<List<String>> = _pendingPostImages.asStateFlow()

    private val _pendingPostIsVideo = MutableStateFlow(false)
    val pendingPostIsVideo: StateFlow<Boolean> = _pendingPostIsVideo.asStateFlow()

    fun openCreatePostWithImages(images: List<String>) {
        _pendingPostImages.value = images
        _pendingPostIsVideo.value = false
        _showCreatePostDialog.value = true
    }

    fun openCreatePostWithVideo(videoUrl: String) {
        _pendingPostImages.value = listOf(videoUrl)
        _pendingPostIsVideo.value = true
        _showCreatePostDialog.value = true
    }

    fun setPendingPostMedia(url: String, isVideo: Boolean = false) {
        _pendingPostImages.value = listOf(url)
        _pendingPostIsVideo.value = isVideo
        _showCreatePostDialog.value = true
    }

    fun clearPendingPostImages() {
        _pendingPostImages.value = emptyList()
        _pendingPostIsVideo.value = false
    }

    // Universal Media & Gallery Picker Target
    private val _activeMediaPickerTarget = MutableStateFlow(MediaPickerTarget.NONE)
    val activeMediaPickerTarget: StateFlow<MediaPickerTarget> = _activeMediaPickerTarget.asStateFlow()

    // Live Streaming Studio Simulator
    private val _showLiveStudio = MutableStateFlow(false)
    val showLiveStudio: StateFlow<Boolean> = _showLiveStudio.asStateFlow()

    // Language switching with Admin ON/OFF enforcement
    fun setLanguage(language: AppLanguage) {
        if (_languageStatus.value[language] == true) {
            _currentLanguage.value = language
        }
    }

    // Toggle 4-Language ON/OFF (Right Manager Fida exclusive - Only Niyon Islam Sohag can toggle)
    fun toggleLanguageStatus(language: AppLanguage, isEnabled: Boolean): Boolean {
        if (!isCurrentUserNiyonIslamSohag()) {
            return false // Unauthorized: Only Niyon Islam Sohag can toggle languages
        }
        com.example.data.NiyonOwnerControlStore.setLanguageEnabled(language.name, isEnabled, isOwner = true)
        val updated = _languageStatus.value.toMutableMap()
        updated[language] = isEnabled
        _languageStatus.value = updated
        // If disabled language is currently selected, fall back to an active language (preferring Bengali)
        if (!isEnabled && _currentLanguage.value == language) {
            val fallback = updated.entries.firstOrNull { it.value }?.key ?: AppLanguage.BENGALI
            _currentLanguage.value = fallback
        }
        return true
    }

    fun setTab(tab: AppTab) {
        _activeTab.value = tab
        when (tab) {
            AppTab.HOME -> clearHomeBadge()
            AppTab.FRIENDS -> clearFriendsBadge()
            AppTab.MESSAGES -> clearMessagesBadge()
            AppTab.VIDEOS, AppTab.REELS -> clearWatchBadge()
            AppTab.NOTIFICATIONS -> clearNotificationsBadge()
            else -> {}
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowCreatePostDialog(show: Boolean) {
        _showCreatePostDialog.value = show
    }

    fun openMediaPicker(target: MediaPickerTarget) {
        _activeMediaPickerTarget.value = target
    }

    fun closeMediaPicker() {
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
    }

    fun openLiveStudio() {
        if (!requireInternetConnection()) return
        _showLiveStudio.value = true
    }

    fun closeLiveStudio() {
        _showLiveStudio.value = false
    }

    fun publishLongVideo(title: String, videoUrl: String, description: String) {
        createPost(
            text = if (description.isNotBlank()) "$title\n\n$description" else title,
            hashtags = listOf("#LongVideo", "#FotoFilmWatch", "#ViralVideo", "#Bangladesh"),
            images = listOf(videoUrl),
            isVideo = true,
            videoTitle = title,
            isReel = false
        )
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
        _activeTab.value = AppTab.VIDEOS
    }

    fun publishReel(videoUrl: String, caption: String) {
        createPost(
            text = if (caption.isNotBlank()) caption else "New Short Video on FotoFilm 🎬✨",
            hashtags = listOf("#ShortVideo", "#FotoFilmShortVideo", "#Trending"),
            images = listOf(videoUrl),
            isVideo = true,
            videoTitle = caption.take(25),
            isReel = true
        )
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
        _activeTab.value = AppTab.REELS
    }

    fun publishTextPost(text: String, backgroundGradientIndex: Int) {
        if (text.isBlank()) return
        createPost(
            text = text,
            hashtags = listOf("#FotoFilm", "#Status", "#Thoughts"),
            images = emptyList(),
            isVideo = false,
            videoTitle = null,
            isReel = false
        )
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
        _activeTab.value = AppTab.HOME
    }

    fun setShowAuthDialog(show: Boolean) {
        if (!show && !_isLoggedIn.value) {
            return
        }
        _showAuthDialog.value = show
    }

    fun login(
        emailOrUser: String,
        pass: String,
        context: android.content.Context? = null
    ): Boolean {
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx
        _authErrorMessage.value = null

        val input = emailOrUser.trim()
        if (input.isBlank()) {
            _authErrorMessage.value = "অনুগ্রহ করে ইমেইল অ্যাড্রেস অথবা ইউজারনেম লিখুন।"
            return false
        }
        if (pass.length < 6) {
            _authErrorMessage.value = "অনুগ্রহ করে কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড লিখুন।"
            return false
        }

        val lower = input.lowercase()
        val isOwnerIdentifier = com.example.data.AuthSessionStore.isReservedOwnerEmail(lower) ||
            lower == "niyon_sohag" ||
            lower == "niyonislamsohag" ||
            lower == "niyon islam sohag"

        if (isOwnerIdentifier) {
            val activeFbEmail = try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
            } catch (_: Exception) {
                null
            }
            val firebaseOwnerSignedIn = activeFbEmail != null &&
                com.example.data.AuthSessionStore.isReservedOwnerEmail(activeFbEmail) &&
                (!input.contains("@") || activeFbEmail == lower)
            val ownerSecretMatched = com.example.data.AuthSessionStore.verifyOwnerSecret(pass, _rightManagerPin.value)

            if (!firebaseOwnerSignedIn && !ownerSecretMatched) {
                setOwnerAuthenticated(false)
                closeRightManagerFida()
                _authErrorMessage.value = "ভুল পাসওয়ার্ড! এই অ্যাকাউন্টটি শুধুমাত্র প্রতিষ্ঠাতা Niyon Islam Sohag-এর জন্য সংরক্ষিত। সঠিক পাসওয়ার্ড অথবা Google / Gmail দিয়ে লগইন করুন।"
                return false
            }

            val resolvedOwnerEmail = if (com.example.data.AuthSessionStore.isReservedOwnerEmail(lower)) {
                input
            } else {
                activeFbEmail ?: SampleData.currentUser.email
            }
            _currentUser.value = SampleData.currentUser.copy(
                email = resolvedOwnerEmail,
                isVerified = true,
                isOnline = true
            )
            setOwnerAuthenticated(true)
            _isFidaVerified.value = true
            recordSavedAccount(
                context = ctx,
                user = _currentUser.value,
                isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
                isOwner = true
            )
            if (ctx != null) {
                com.example.data.AuthSessionStore.saveActiveSession(
                    context = ctx,
                    user = _currentUser.value,
                    isOwnerVerified = true,
                    authMethod = "owner_pin"
                )
            }
            syncUserAccountsForCurrentUser(_currentUser.value)
            com.example.data.FirebaseManager.syncSignedInUserProfile(_currentUser.value)
            _isLoggedIn.value = true
            _showAuthDialog.value = false
            return true
        }

        // Non-owner user login: verify local password if account was registered on this device
        if (ctx != null && com.example.data.AuthSessionStore.hasLocalCredential(ctx, input)) {
            if (!com.example.data.AuthSessionStore.verifyLocalCredential(ctx, input, pass)) {
                _authErrorMessage.value = "পাসওয়ার্ড সঠিক নয়! অনুগ্রহ করে সঠিক পাসওয়ার্ড দিয়ে আবার চেষ্টা করুন।"
                return false
            }
        }

        val matchedSaved = _savedEmailAccounts.value.firstOrNull {
            !it.isOwner && (it.email.trim().equals(input, ignoreCase = true) ||
                it.username.trim().equals(input, ignoreCase = true))
        }

        setOwnerAuthenticated(false)
        closeRightManagerFida()
        _isFidaVerified.value = false

        if (matchedSaved != null) {
            _currentUser.value = User(
                id = matchedSaved.id,
                fullName = matchedSaved.fullName,
                username = matchedSaved.username,
                email = matchedSaved.email,
                avatarUrl = matchedSaved.avatarUrl,
                bio = "FotoFilm সদস্য ✨",
                isVerified = false,
                isOnline = true
            )
        } else {
            val resolvedName = input.substringBefore("@").ifBlank { "FotoFilm User" }
            val resolvedHandle = resolvedName.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user" }
            _currentUser.value = User(
                id = "user_${resolvedHandle}_${UUID.randomUUID().toString().take(6)}",
                fullName = resolvedName,
                username = resolvedHandle,
                email = if (input.contains("@")) input else "$resolvedHandle@fotofilm.app",
                bio = "FotoFilm সদস্য ✨",
                isVerified = false,
                isOnline = true
            )
        }

        recordSavedAccount(
            context = ctx,
            user = _currentUser.value,
            isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
            isOwner = false
        )
        if (ctx != null) {
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = _currentUser.value,
                isOwnerVerified = false,
                authMethod = "email"
            )
        }
        syncUserAccountsForCurrentUser(_currentUser.value)
        com.example.data.FirebaseManager.syncSignedInUserProfile(_currentUser.value)
        _isLoggedIn.value = true
        _showAuthDialog.value = false
        return true
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        gender: String,
        country: String,
        birthDate: String,
        pass: String,
        avatarUrl: String?,
        context: android.content.Context? = null
    ): Boolean {
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx
        _authErrorMessage.value = null

        if (fullName.isBlank()) {
            _authErrorMessage.value = "অনুগ্রহ করে আপনার পুরো নাম লিখুন।"
            return false
        }
        val cleanEmail = email.trim()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _authErrorMessage.value = "অনুগ্রহ করে সঠিক ইমেইল অ্যাড্রেস লিখুন।"
            return false
        }
        if (birthDate.isBlank()) {
            _authErrorMessage.value = "অনুগ্রহ করে আপনার জন্মতারিখ লিখুন।"
            return false
        }
        if (pass.length < 6) {
            _authErrorMessage.value = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
            return false
        }

        val emailLower = cleanEmail.lowercase()
        val activeFbEmail = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
        } catch (_: Exception) {
            null
        }
        val isReservedOwner = com.example.data.AuthSessionStore.isReservedOwnerEmail(emailLower)
        val matchesOwnerSecret = com.example.data.AuthSessionStore.verifyOwnerSecret(pass, _rightManagerPin.value)
        val isOwnerNameOrHandle = fullName.trim().equals("Niyon Islam Sohag", ignoreCase = true) ||
            username.trim().lowercase() in listOf("niyon_sohag", "niyonislamsohag", "niyon islam sohag")
        val isVerifiedOwner = (isReservedOwner && (activeFbEmail == emailLower || matchesOwnerSecret)) ||
            (isOwnerNameOrHandle && matchesOwnerSecret)

        // Block anyone from registering manually with owner's reserved email without verified Google Auth or owner secret
        if (isReservedOwner && !isVerifiedOwner) {
            _authErrorMessage.value = "এই ইমেইলটি সংরক্ষিত! মালিক হলে সঠিক পাসওয়ার্ড বা ফোনের Google / Gmail দিয়ে প্রবেশ করুন অথবা আপনার নিজের ইমেইল দিন।"
            return false
        }

        val resolvedUsername = if (username.isNotBlank()) {
            username.trim()
        } else {
            fullName.trim().lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user" }
        }

        // ১ ব্যক্তি সর্বোচ্চ ৩টি আইডি সীমা যাচাই (নতুন ইমেইল দিয়ে ৪র্থ অ্যাকাউন্ট খুললে ব্লক হবে)
        val savedOnDevice = ctx?.let { com.example.data.SavedAccountsStore.loadAccounts(it) } ?: _savedEmailAccounts.value
        val isAlreadySavedEmail = savedOnDevice.any { it.email.trim().equals(cleanEmail, ignoreCase = true) }
        val existingCount = countDistinctAccountsOnDevice(ctx, excludingEmail = cleanEmail)
        if (!isVerifiedOwner && !isAlreadySavedEmail && existingCount >= com.example.util.FakeIdDetector.MAX_ACCOUNTS_PER_PERSON) {
            val limitMsg = "⛔ ১ জন ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (এই ডিভাইসে ইতিমধ্যে ৩টি আইডি খোলা আছে)"
            _authErrorMessage.value = limitMsg
            recordFakeIdViolationReport(
                context = ctx,
                attemptedName = fullName.trim(),
                attemptedUsername = resolvedUsername,
                attemptedEmail = cleanEmail,
                selectedGender = gender.trim(),
                detectedSignal = "১ ব্যক্তি সর্বোচ্চ ৩ আইডি সীমা অতিক্রম ($existingCount/৩ পূর্ণ)",
                violationType = "LIMIT_3_ACCOUNTS",
                violationDetails = limitMsg,
                attemptedAvatarUrl = avatarUrl.orEmpty()
            )
            return false
        }

        val activeFirebaseUid = com.example.data.FirebaseManager.activeAuthUid()
        val candidateUserId = if (isVerifiedOwner) {
            activeFirebaseUid ?: SampleData.currentUser.id
        } else {
            activeFirebaseUid ?: "user_${UUID.randomUUID().toString().take(8)}"
        }

        // অ্যান্টি-ফেক-আইডি যাচাই:
        // ১. অন্যের ছবি দিয়ে ফেইক আইডি ব্লক
        // ২. ছেলে হয়ে মেয়ের নামে/ছবিতে ফেইক আইডি ব্লক
        // ৩. মেয়ে হয়ে ছেলের নামে/ছবিতে ফেইক আইডি ব্লক
        val fakeIdCheck = com.example.util.FakeIdDetector.validateRegistrationOrUpdate(
            context = ctx,
            candidateUserId = candidateUserId,
            candidateEmail = cleanEmail,
            fullName = fullName.trim(),
            username = resolvedUsername,
            selectedGender = gender.trim(),
            avatarUrl = avatarUrl,
            existingUsers = _allAppUsers.value,
            existingPosts = _posts.value,
            savedEmailAccounts = savedOnDevice,
            isOwnerVerified = isVerifiedOwner
        )
        if (!fakeIdCheck.isAllowed) {
            val msg = fakeIdCheck.reasonMessage ?: "⛔ ফেইক আইডি শনাক্ত হওয়ায় অ্যাকাউন্ট খোলা ব্লক করা হয়েছে!"
            _authErrorMessage.value = msg
            recordFakeIdViolationReport(
                context = ctx,
                attemptedName = fullName.trim(),
                attemptedUsername = resolvedUsername,
                attemptedEmail = cleanEmail,
                selectedGender = gender.trim(),
                detectedSignal = fakeIdCheck.detectedSignal,
                violationType = fakeIdCheck.violationType ?: "FAKE_ID_BLOCKED",
                violationDetails = msg,
                attemptedAvatarUrl = avatarUrl.orEmpty(),
                matchedExistingUser = fakeIdCheck.matchedOwnerName
            )
            return false
        }

        val newUser = if (isVerifiedOwner) {
            SampleData.currentUser.copy(
                id = candidateUserId,
                fullName = fullName.trim(),
                username = resolvedUsername,
                email = cleanEmail,
                gender = gender.trim(),
                country = country.trim(),
                birthDate = birthDate.trim(),
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.avatarUrl,
                isVerified = true,
                isOnline = true
            )
        } else {
            User(
                id = candidateUserId,
                fullName = fullName.trim(),
                username = resolvedUsername,
                email = cleanEmail,
                gender = gender.trim(),
                country = country.trim(),
                birthDate = birthDate.trim(),
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: "",
                bio = "FotoFilm সদস্য ✨",
                isVerified = false,
                isOnline = true
            )
        }

        if (newUser.avatarUrl.isNotBlank()) {
            com.example.util.FakeIdDetector.registerPhotoFingerprint(
                context = ctx,
                ownerId = newUser.id,
                ownerName = newUser.fullName,
                ownerEmail = newUser.email,
                ownerGender = newUser.gender,
                avatarUrl = newUser.avatarUrl,
                precomputed = fakeIdCheck.fingerprint
            )
        }

        setOwnerAuthenticated(isVerifiedOwner)
        _isFidaVerified.value = isVerifiedOwner
        if (!isVerifiedOwner) {
            closeRightManagerFida()
        }
        _currentUser.value = newUser
        if (ctx != null) {
            com.example.data.AuthSessionStore.saveLocalCredential(
                context = ctx,
                email = cleanEmail,
                username = resolvedUsername,
                password = pass
            )
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = newUser,
                isOwnerVerified = isVerifiedOwner,
                authMethod = if (isVerifiedOwner) "owner_verified" else "email"
            )
        }
        recordSavedAccount(
            context = ctx,
            user = newUser,
            isFirebaseLinked = activeFirebaseUid != null,
            isOwner = isVerifiedOwner
        )
        syncUserAccountsForCurrentUser(newUser)
        com.example.data.FirebaseManager.syncSignedInUserProfile(newUser)
        _isLoggedIn.value = true
        _showAuthDialog.value = false
        return true
    }

    fun loginWithAppleId(context: android.content.Context? = null) {
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx
        _authErrorMessage.value = null
        val existingCount = countDistinctAccountsOnDevice(ctx, excludingEmail = "apple.user@icloud.com")
        if (existingCount >= com.example.util.FakeIdDetector.MAX_ACCOUNTS_PER_PERSON) {
            val limitMsg = "⛔ ১ জন ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (এই ডিভাইসে ইতিমধ্যে ৩টি আইডি খোলা আছে)"
            _authErrorMessage.value = limitMsg
            recordFakeIdViolationReport(
                context = ctx,
                attemptedName = "Apple User",
                attemptedUsername = "apple_user",
                attemptedEmail = "apple.user@icloud.com",
                selectedGender = "অনির্ধারিত",
                detectedSignal = "১ ব্যক্তি সর্বোচ্চ ৩ আইডি সীমা অতিক্রম ($existingCount/৩ পূর্ণ)",
                violationType = "LIMIT_3_ACCOUNTS",
                violationDetails = limitMsg
            )
            return
        }
        val appleUser = User(
            id = "apple_${UUID.randomUUID().toString().take(8)}",
            fullName = "Apple User",
            username = "apple_user",
            email = "apple.user@icloud.com",
            bio = "FotoFilm সদস্য ✨",
            isVerified = false,
            isOnline = true
        )
        setOwnerAuthenticated(false)
        _isFidaVerified.value = false
        closeRightManagerFida()
        _currentUser.value = appleUser
        if (ctx != null) {
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = appleUser,
                isOwnerVerified = false,
                authMethod = "apple"
            )
        }
        recordSavedAccount(
            context = ctx,
            user = appleUser,
            isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
            isOwner = false
        )
        syncUserAccountsForCurrentUser(appleUser)
        com.example.data.FirebaseManager.syncSignedInUserProfile(appleUser)
        _isLoggedIn.value = true
        _showAuthDialog.value = false
    }

    // Profile customization & session teardown
    fun updateAuthenticatedUserProfile(
        fullName: String,
        username: String,
        gender: String,
        country: String,
        birthDate: String,
        avatarUrl: String?
    ): Boolean {
        if (fullName.isBlank()) return false
        val cu = _currentUser.value
        val candidateAvatar = avatarUrl?.takeIf { it.isNotBlank() } ?: cu.avatarUrl
        val resolvedHandle = if (username.isNotBlank()) username.trim() else fullName.trim().lowercase().replace(" ", "_")
        val check = com.example.util.FakeIdDetector.validateRegistrationOrUpdate(
            context = appContextRef,
            candidateUserId = cu.id,
            candidateEmail = cu.email,
            fullName = fullName.trim(),
            username = resolvedHandle,
            selectedGender = gender.trim(),
            avatarUrl = candidateAvatar,
            existingUsers = _allAppUsers.value,
            existingPosts = _posts.value,
            savedEmailAccounts = _savedEmailAccounts.value,
            isOwnerVerified = isCurrentUserNiyonIslamSohag()
        )
        if (!check.isAllowed) {
            val msg = check.reasonMessage ?: "⛔ ফেইক আইডি সুরক্ষা নীতির কারণে প্রোফাইল আপডেট ব্লক করা হয়েছে!"
            _authErrorMessage.value = msg
            _safetyPolicyAlert.value = msg
            recordFakeIdViolationReport(
                context = appContextRef,
                attemptedName = fullName.trim(),
                attemptedUsername = resolvedHandle,
                attemptedEmail = cu.email,
                selectedGender = gender.trim(),
                detectedSignal = check.detectedSignal,
                violationType = check.violationType ?: "FAKE_ID_BLOCKED",
                violationDetails = msg,
                attemptedAvatarUrl = candidateAvatar,
                matchedExistingUser = check.matchedOwnerName
            )
            return false
        }
        val updatedUser = cu.copy(
            fullName = fullName.trim(),
            username = resolvedHandle,
            gender = gender.trim(),
            country = country.trim(),
            birthDate = birthDate.trim(),
            avatarUrl = candidateAvatar
        )
        if (updatedUser.avatarUrl.isNotBlank()) {
            com.example.util.FakeIdDetector.registerPhotoFingerprint(
                context = appContextRef,
                ownerId = updatedUser.id,
                ownerName = updatedUser.fullName,
                ownerEmail = updatedUser.email,
                ownerGender = updatedUser.gender,
                avatarUrl = updatedUser.avatarUrl,
                precomputed = check.fingerprint
            )
        }
        _currentUser.value = updatedUser
        appContextRef?.let { ctx ->
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = updatedUser,
                isOwnerVerified = isCurrentOwnerAuthenticated,
                authMethod = "profile_update"
            )
        }
        syncUserAccountsForCurrentUser(updatedUser)
        com.example.data.FirebaseManager.syncSignedInUserProfile(updatedUser)
        _showAuthDialog.value = false
        return true
    }

    fun logout() {
        appContextRef?.let { ctx ->
            com.example.data.AuthSessionStore.clearActiveSession(ctx)
        }
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
        }
        setOwnerAuthenticated(false)
        closeRightManagerFida()
        _isRightManagerUnlocked.value = false
        _isFidaVerified.value = false
        _currentUser.value = createGuestUser()
        _userAccounts.value = emptyList()
        _authErrorMessage.value = null
        stopCloudSync(keepLoggedIn = false)
        _isLoggedIn.value = false
        _showAuthDialog.value = true
    }

    // Profile updates
    fun updateProfileAvatar(uri: String) {
        val cu = _currentUser.value
        val check = com.example.util.FakeIdDetector.validateRegistrationOrUpdate(
            context = appContextRef,
            candidateUserId = cu.id,
            candidateEmail = cu.email,
            fullName = cu.fullName,
            username = cu.username,
            selectedGender = cu.gender,
            avatarUrl = uri,
            existingUsers = _allAppUsers.value,
            existingPosts = _posts.value,
            savedEmailAccounts = _savedEmailAccounts.value,
            isOwnerVerified = isCurrentUserNiyonIslamSohag()
        )
        if (!check.isAllowed) {
            val msg = check.reasonMessage ?: "⛔ অন্যের ছবি বা বিপরীত লিঙ্গের ছবি ব্যবহার নিষিদ্ধ!"
            _safetyPolicyAlert.value = msg
            _compressionToastMessage.value = msg
            recordFakeIdViolationReport(
                context = appContextRef,
                attemptedName = cu.fullName,
                attemptedUsername = cu.username,
                attemptedEmail = cu.email,
                selectedGender = cu.gender,
                detectedSignal = check.detectedSignal,
                violationType = check.violationType ?: "STOLEN_PHOTO",
                violationDetails = msg,
                attemptedAvatarUrl = uri,
                matchedExistingUser = check.matchedOwnerName
            )
            return
        }
        com.example.util.FakeIdDetector.registerPhotoFingerprint(
            context = appContextRef,
            ownerId = cu.id,
            ownerName = cu.fullName,
            ownerEmail = cu.email,
            ownerGender = cu.gender,
            avatarUrl = uri,
            precomputed = check.fingerprint
        )
        _currentUser.value = cu.copy(avatarUrl = uri)
        appContextRef?.let { ctx ->
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = _currentUser.value,
                isOwnerVerified = isCurrentOwnerAuthenticated,
                authMethod = "avatar_update"
            )
        }
        syncUserAccountsForCurrentUser(_currentUser.value)
    }

    fun updateCoverPhoto(uri: String) {
        _currentUser.value = _currentUser.value.copy(coverUrl = uri)
    }

    fun updateAvatarFrame(frameId: String?) {
        _currentUser.value = _currentUser.value.copy(avatarFrame = frameId)
    }

    fun shareProfileAvatarToStory(caption: String = "নতুন প্রোফাইল ছবি ✨") {
        val avatar = _currentUser.value.avatarUrl
        if (avatar.isNotBlank()) {
            addStory(avatar, caption)
        }
    }

    fun updateNote(newNote: String) {
        val check = com.example.util.SafetyPolicyValidator.validateContent(newNote)
        if (!check.isAllowed) {
            _safetyPolicyAlert.value = check.reasonMessage
            return
        }
        _currentUser.value = _currentUser.value.copy(note = newNote.take(80))
    }

    // Posts & Reactions
    fun createPost(
        text: String,
        hashtags: List<String>,
        images: List<String>,
        isVideo: Boolean = false,
        videoTitle: String? = null,
        isReel: Boolean = false,
        title: String? = null,
        audioTrack: PostAudioTrack? = null,
        isFaceGram: Boolean = false,
        isFaceClip: Boolean = false,
        customAuthor: User? = null,
        location: String? = null,
        musicInfo: String? = null
    ) {
        if (!requireInternetConnection()) return
        val check = com.example.util.SafetyPolicyValidator.validateContent(text + " " + (title ?: "") + " " + (videoTitle ?: ""))
        if (!check.isAllowed) {
            _safetyPolicyAlert.value = check.reasonMessage
            return
        }

        val activeUser = customAuthor ?: _currentUser.value
        val effectiveIsVideo = isVideo || isFaceClip
        val effectiveIsReel = isReel || isFaceClip
        val hasMedia = images.isNotEmpty()
        val quotaCategory = when {
            effectiveIsReel -> com.example.data.DailyQuotaCategory.REEL_VIDEO
            effectiveIsVideo -> com.example.data.DailyQuotaCategory.LONG_VIDEO
            hasMedia -> com.example.data.DailyQuotaCategory.FEED_PHOTO
            else -> null
        }

        if (quotaCategory != null) {
            val itemCount = if (quotaCategory == com.example.data.DailyQuotaCategory.FEED_PHOTO) images.size else 1
            val verdict = com.example.data.DailyQuotaCompressionStore.checkQuotaBeforeUpload(
                userId = activeUser.id,
                category = quotaCategory,
                itemCount = itemCount
            )
            if (!verdict.allowed) {
                _compressionToastMessage.value = verdict.reasonBn
                _safetyPolicyAlert.value = verdict.reasonBn
                return
            }
        }

        val compressionKind = when {
            effectiveIsReel -> com.example.data.CompressionMediaKind.REEL_VIDEO
            effectiveIsVideo -> com.example.data.CompressionMediaKind.LONG_VIDEO
            else -> com.example.data.CompressionMediaKind.PHOTO
        }

        val postHashtags = (hashtags + listOfNotNull(
            if (isFaceGram && !hashtags.contains("#GramFilm")) "#GramFilm" else null,
            if (isFaceGram && !hashtags.contains("#FaceGram")) "#FaceGram" else null,
            if (isFaceClip && !hashtags.contains("#FaceClip")) "#FaceClip" else null
        )).take(10)

        val cappedImages = if (quotaCategory == com.example.data.DailyQuotaCategory.FEED_PHOTO &&
            com.example.data.DailyQuotaCompressionStore.config.value.isDaily43MbLimitEnabled
        ) {
            images.take(com.example.data.DailyQuotaCompressionStore.MAX_FEED_PHOTO_COUNT)
        } else {
            images.take(15)
        }

        val newPost = Post(
            id = "post_${UUID.randomUUID()}",
            author = activeUser,
            timestamp = "Just now • 🌐 Public",
            createdAt = System.currentTimeMillis(),
            title = title?.takeIf { it.isNotBlank() },
            text = text,
            hashtags = postHashtags,
            images = cappedImages,
            isVideo = effectiveIsVideo,
            videoTitle = videoTitle,
            isReel = effectiveIsReel,
            isFaceGram = isFaceGram,
            isFaceClip = isFaceClip,
            reachViews = "1.5K",
            reactionCounts = mapOf(ReactionType.LIKE to 1),
            audioTrack = audioTrack,
            musicInfo = musicInfo ?: audioTrack?.let { "${it.title} • ${it.artist}" },
            location = location
        )
        _posts.value = listOf(newPost) + _posts.value
        _showCreatePostDialog.value = false

        val mediaTypeLabel = when {
            effectiveIsReel -> "শর্ট ভিডিও"
            effectiveIsVideo -> "ভিডিও পোস্ট"
            cappedImages.isNotEmpty() -> "ছবি পোস্ট"
            audioTrack != null -> "ভয়েস/অডিও পোস্ট"
            else -> "টেক্সট পোস্ট"
        }
        val postPreviewText = (title ?: videoTitle ?: text).trim()
            .ifBlank { mediaTypeLabel }
            .take(48)

        fun updateUploadProgress(percent: Int, logText: String, completed: Boolean = false) {
            _postUploadProgress.value = PostUploadProgress(
                postId = newPost.id,
                postPreview = postPreviewText,
                mediaTypeLabel = mediaTypeLabel,
                progressPercent = percent.coerceIn(0, 100),
                statusLog = logText,
                isCompleted = completed
            )
            if (completed) {
                viewModelScope.launch {
                    delay(2400L)
                    if (_postUploadProgress.value?.postId == newPost.id) {
                        _postUploadProgress.value = null
                    }
                }
            }
        }

        updateUploadProgress(12, "পোস্ট প্রস্তুত ও লোকাল সেভ হচ্ছে...")

        // Persist immediately to Room DB so post stays after app restart
        persistPostToRoom(newPost)
        // Sync to cloud Firebase so all users see it in real-time
        com.example.data.FirebaseManager.savePost(newPost)

        val ctx = appContextRef ?: com.example.data.CloudinaryManager.getAppContext()
        val hasLocalMedia = cappedImages.any {
            it.startsWith("content://") || it.startsWith("file://") || it.startsWith("/")
        }
        val hasLocalAudio = audioTrack?.audioUrl?.let {
            it.startsWith("content://") || it.startsWith("file://") || it.startsWith("/")
        } == true

        if (ctx != null && (hasLocalMedia || hasLocalAudio || quotaCategory != null)) {
            viewModelScope.launch(Dispatchers.IO) {
                updateUploadProgress(18, "ফাইল স্থায়ী মেমোরিতে কপি হচ্ছে...")

                // Step 1: Copy any local picked media & voice recordings into permanent internal storage
                val persistedImageMap = mutableMapOf<String, String>()
                cappedImages.forEach { raw ->
                    if (raw.startsWith("content://") || raw.startsWith("file://")) {
                        val sub = if (effectiveIsVideo || effectiveIsReel) "videos" else "photos"
                        val ext = if (effectiveIsVideo || effectiveIsReel) "mp4" else "jpg"
                        val durable = com.example.data.local.MediaPersistenceHelper.persistUriIfLocal(
                            ctx, raw, subDir = sub, defaultExtension = ext
                        )
                        persistedImageMap[raw] = durable
                    }
                }
                val persistedAudioUrl = audioTrack?.audioUrl?.let { rawAudio ->
                    com.example.data.local.MediaPersistenceHelper.persistUriIfLocal(
                        ctx, rawAudio, subDir = "voice_posts", defaultExtension = "m4a"
                    )
                }

                var updatedPostForDisk: Post? = null
                _posts.value = _posts.value.map { cur ->
                    if (cur.id == newPost.id) {
                        val nextImages = cur.images.map { persistedImageMap[it] ?: it }
                        val nextAudio = cur.audioTrack?.let { a ->
                            if (persistedAudioUrl != null) a.copy(audioUrl = persistedAudioUrl) else a
                        }
                        val upd = cur.copy(images = nextImages, audioTrack = nextAudio)
                        updatedPostForDisk = upd
                        upd
                    } else cur
                }
                updatedPostForDisk?.let { localRepo?.upsertPost(it) }

                val activeImages = _posts.value.firstOrNull { it.id == newPost.id }?.images ?: cappedImages
                val localMediaItems = activeImages.filter { it.startsWith("content://") || it.startsWith("file://") }
                val hasUploadableVoice = persistedAudioUrl != null &&
                    (persistedAudioUrl.startsWith("file://") || persistedAudioUrl.startsWith("content://") || persistedAudioUrl.startsWith("/"))
                val totalUploadTasks = localMediaItems.size + (if (hasUploadableVoice) 1 else 0)

                if (totalUploadTasks == 0) {
                    updateUploadProgress(60, "Firestore ক্লাউডে সিঙ্ক হচ্ছে...")
                    delay(280L)
                    updateUploadProgress(100, "আপলোড সম্পন্ন! সবাই এখন দেখতে পাবে ✓", completed = true)
                    return@launch
                }

                val taskProgressMap = java.util.concurrent.ConcurrentHashMap<String, Int>()
                val remainingTasks = java.util.concurrent.atomic.AtomicInteger(totalUploadTasks)

                fun reportCombinedProgress(taskKey: String, pct: Int, stageLabel: String) {
                    taskProgressMap[taskKey] = pct.coerceIn(0, 100)
                    val avg = if (taskProgressMap.isEmpty()) 25 else {
                        (taskProgressMap.values.sum() / totalUploadTasks)
                    }
                    val mapped = (22 + (avg * 0.74)).toInt().coerceIn(22, 96)
                    updateUploadProgress(mapped, stageLabel)
                }

                fun onTaskFinished(taskKey: String, successLabel: String) {
                    taskProgressMap[taskKey] = 100
                    if (remainingTasks.decrementAndGet() <= 0) {
                        updateUploadProgress(100, successLabel, completed = true)
                    } else {
                        reportCombinedProgress(taskKey, 100, "পরবর্তী অংশ আপলোড হচ্ছে...")
                    }
                }

                // Step 2: Upload local recorded Voice Post (.m4a) cleanly to Cloudinary (isAudioOnly = true)
                if (hasUploadableVoice && persistedAudioUrl != null) {
                    val voiceTaskKey = "voice_audio"
                    reportCombinedProgress(voiceTaskKey, 5, "ভয়েস অডিও Cloudinary-তে পাঠানো হচ্ছে...")
                    try {
                        val audioUri = if (persistedAudioUrl.startsWith("/")) {
                            android.net.Uri.fromFile(java.io.File(persistedAudioUrl))
                        } else {
                            android.net.Uri.parse(persistedAudioUrl)
                        }
                        val onVoiceCloudReady: (String) -> Unit = { downloadUrl ->
                            var syncedVoicePost: Post? = null
                            _posts.value = _posts.value.map { cur ->
                                if (cur.id == newPost.id) {
                                    val upd = cur.copy(audioTrack = cur.audioTrack?.copy(audioUrl = downloadUrl))
                                    syncedVoicePost = upd
                                    com.example.data.FirebaseManager.savePost(upd)
                                    upd
                                } else cur
                            }
                            syncedVoicePost?.let { p -> persistPostToRoom(p) }
                            _compressionToastMessage.value = "☁️ ভয়েস পোস্ট Cloudinary-তে সেভ হয়েছে ✓"
                            onTaskFinished(voiceTaskKey, "ভয়েস ও পোস্ট আপলোড সম্পন্ন! ✓")
                        }
                        if (com.example.data.CloudinaryManager.isEnabled() && com.example.data.CloudinaryManager.isConfigured()) {
                            com.example.data.CloudinaryManager.uploadMedia(
                                uri = audioUri,
                                isVideoOrAudio = true,
                                folder = "fotofilm/voice_posts",
                                isAudioOnly = true,
                                onProgress = { pct ->
                                    reportCombinedProgress(voiceTaskKey, pct, "ভয়েস অডিও আপলোড হচ্ছে...")
                                },
                                onSuccess = onVoiceCloudReady,
                                onFailure = {
                                    com.example.data.FirebaseManager.uploadFileToStorage(
                                        uri = audioUri,
                                        folder = "voice_posts",
                                        onSuccess = onVoiceCloudReady,
                                        onFailure = {
                                            onTaskFinished(voiceTaskKey, "ডিভাইসে সেভ সম্পন্ন (অফলাইন সিঙ্ক) ✓")
                                        }
                                    )
                                }
                            )
                        } else {
                            com.example.data.FirebaseManager.uploadFileToStorage(
                                uri = audioUri,
                                folder = "voice_posts",
                                onSuccess = onVoiceCloudReady,
                                onFailure = {
                                    onTaskFinished(voiceTaskKey, "ডিভাইসে সেভ সম্পন্ন ✓")
                                }
                            )
                        }
                    } catch (_: Exception) {
                        onTaskFinished(voiceTaskKey, "ডিভাইসে সেভ সম্পন্ন ✓")
                    }
                }

                // Step 3: Auto-compress photos/videos, track daily quota, and upload to Cloudinary
                if (quotaCategory != null) {
                    updateUploadProgress(24, "মিডিয়া স্মার্ট কম্প্রেস করা হচ্ছে...")
                    val isAutoCompress = com.example.data.DailyQuotaCompressionStore.config.value.isAutoCompressionEnabled
                    var totalUsedUnit = 0.0
                    var totalSavedMb = 0.0
                    var latestBanner: String? = null
                    val compressedLocalMap = mutableMapOf<String, android.net.Uri>()

                    activeImages.forEach { mediaUriStr ->
                        if (mediaUriStr.startsWith("content://") || mediaUriStr.startsWith("file://")) {
                            try {
                                val rawUri = android.net.Uri.parse(mediaUriStr)
                                if (isAutoCompress) {
                                    val outcome = if (effectiveIsVideo || effectiveIsReel) {
                                        com.example.data.MediaCompressionManager.compressAndTrimVideo(ctx, rawUri, compressionKind)
                                    } else {
                                        com.example.data.MediaCompressionManager.compressImageToMaxKb(ctx, rawUri, maxKb = 300)
                                    }
                                    compressedLocalMap[mediaUriStr] = outcome.outputUri
                                    latestBanner = outcome.summaryBn
                                    val savedMb = ((outcome.originalBytes - outcome.compressedBytes).coerceAtLeast(0L)) / (1024.0 * 1024.0)
                                    totalSavedMb += savedMb
                                    totalUsedUnit += if (quotaCategory == com.example.data.DailyQuotaCategory.FEED_PHOTO) {
                                        (outcome.compressedBytes / 1024.0).coerceIn(80.0, 300.0)
                                    } else {
                                        outcome.effectiveQuotaMb.coerceAtMost(
                                            if (effectiveIsReel) com.example.data.DailyQuotaCompressionStore.MAX_REEL_MB
                                            else com.example.data.DailyQuotaCompressionStore.MAX_LONG_VIDEO_MB
                                        )
                                    }
                                } else {
                                    totalUsedUnit += when (quotaCategory) {
                                        com.example.data.DailyQuotaCategory.LONG_VIDEO -> com.example.data.DailyQuotaCompressionStore.MAX_LONG_VIDEO_MB
                                        com.example.data.DailyQuotaCategory.REEL_VIDEO -> com.example.data.DailyQuotaCompressionStore.MAX_REEL_MB
                                        com.example.data.DailyQuotaCategory.FEED_PHOTO -> com.example.data.DailyQuotaCompressionStore.MAX_PHOTO_UNIT_KB
                                        else -> 0.0
                                    }
                                }
                            } catch (_: Exception) {
                            }
                        } else {
                            totalUsedUnit += when (quotaCategory) {
                                com.example.data.DailyQuotaCategory.LONG_VIDEO -> 24.0
                                com.example.data.DailyQuotaCategory.REEL_VIDEO -> 6.5
                                com.example.data.DailyQuotaCategory.FEED_PHOTO -> 280.0
                                else -> 0.0
                            }
                        }
                    }

                    com.example.data.DailyQuotaCompressionStore.recordUsage(
                        userId = activeUser.id,
                        userName = activeUser.fullName,
                        avatarUrl = activeUser.avatarUrl,
                        category = quotaCategory,
                        itemCount = if (quotaCategory == com.example.data.DailyQuotaCategory.FEED_PHOTO) activeImages.size else 1,
                        usedMbOrKb = totalUsedUnit,
                        savedMb = totalSavedMb
                    )

                    if (latestBanner != null) {
                        _compressionToastMessage.value = latestBanner
                    }

                    localMediaItems.forEachIndexed { idx, mediaUriStr ->
                        val taskKey = "media_$idx"
                        reportCombinedProgress(taskKey, 8, "Cloudinary-তে আপলোড শুরু হচ্ছে...")
                        try {
                            val uploadUri = compressedLocalMap[mediaUriStr] ?: android.net.Uri.parse(mediaUriStr)
                            val onCloudReady: (String) -> Unit = { downloadUrl ->
                                var syncedPost: Post? = null
                                _posts.value = _posts.value.map { cur ->
                                    if (cur.id == newPost.id) {
                                        val updated = cur.copy(
                                            images = cur.images.map { if (it == mediaUriStr) downloadUrl else it }
                                        )
                                        syncedPost = updated
                                        com.example.data.FirebaseManager.savePost(updated)
                                        updated
                                    } else cur
                                }
                                syncedPost?.let { p -> persistPostToRoom(p) }
                                _compressionToastMessage.value = "☁️ Cloudinary-তে সেভ হয়েছে — সবাই দেখতে পাবে ✓"
                                onTaskFinished(taskKey, "Cloudinary ও Firestore আপলোড সম্পন্ন! ✓")
                            }
                            if (com.example.data.CloudinaryManager.isEnabled() && com.example.data.CloudinaryManager.isConfigured()) {
                                com.example.data.CloudinaryManager.uploadMedia(
                                    uri = uploadUri,
                                    isVideoOrAudio = effectiveIsVideo || effectiveIsReel,
                                    folder = if (effectiveIsVideo || effectiveIsReel) "fotofilm/videos" else "fotofilm/photos",
                                    compressionKind = compressionKind,
                                    onProgress = { pct ->
                                        reportCombinedProgress(taskKey, pct, "Cloudinary-তে আপলোড হচ্ছে...")
                                    },
                                    onSuccess = onCloudReady,
                                    onFailure = {
                                        com.example.data.FirebaseManager.uploadFileToStorage(
                                            uri = uploadUri,
                                            folder = if (effectiveIsVideo || effectiveIsReel) "videos" else "photos",
                                            onSuccess = onCloudReady,
                                            onFailure = {
                                                onTaskFinished(taskKey, "ডিভাইসে সেভ সম্পন্ন (অফলাইন সিঙ্ক) ✓")
                                            }
                                        )
                                    }
                                )
                            } else {
                                com.example.data.FirebaseManager.uploadFileToStorage(
                                    uri = uploadUri,
                                    folder = if (effectiveIsVideo || effectiveIsReel) "videos" else "photos",
                                    onSuccess = onCloudReady,
                                    onFailure = {
                                        onTaskFinished(taskKey, "ডিভাইসে সেভ সম্পন্ন ✓")
                                    }
                                )
                            }
                        } catch (_: Exception) {
                            onTaskFinished(taskKey, "ডিভাইসে সেভ সম্পন্ন ✓")
                        }
                    }
                }
            }
        } else {
            // Text-only post: show smooth quick upload percentage log and complete cleanly
            viewModelScope.launch {
                delay(220L)
                updateUploadProgress(50, "পোস্ট ক্লাউডে পাঠানো হচ্ছে...")
                delay(280L)
                updateUploadProgress(85, "সবার ফিডে পাবলিশ হচ্ছে...")
                delay(220L)
                updateUploadProgress(100, "পোস্ট সফলভাবে পাবলিশ হয়েছে ✓", completed = true)
            }
        }

        // Update user post count
        _currentUser.value = _currentUser.value.copy(postCount = _currentUser.value.postCount + 1)
    }

    /**
     * Create an official Sponsored Ad Campaign post that syncs to Firebase Firestore
     * and appears across all users' news feeds.
     */
    fun createSponsoredAdPost(headline: String, budget: String, reach: String) {
        val newAdPost = Post(
            id = "ad_post_${UUID.randomUUID()}",
            author = _currentUser.value,
            timestamp = "Sponsored • 🌐 Public",
            createdAt = System.currentTimeMillis(),
            title = "📢 $headline",
            text = "$headline\n\n🎯 টার্গেটেড বিজ্ঞাপন • বাজেট: $budget • লক্ষ্যমাত্রা: $reach\nFace AD এডমিন দ্বারা অনুমোদিত স্পনসর্ড বিজ্ঞাপন।",
            hashtags = listOf("#Sponsored", "#Ad", "#FotoFilmPromo", "#Business"),
            images = emptyList(),
            reachViews = reach.ifBlank { "0" },
            isBoosted = true,
            boostStatus = "Active",
            boostTargetReach = reach,
            reactionCounts = emptyMap()
        )
        _posts.value = listOf(newAdPost) + _posts.value
        persistPostToRoom(newAdPost)
        com.example.data.FirebaseManager.savePost(newAdPost)
    }

    fun reactToPost(postId: String, reaction: ReactionType) {
        if (!requireInternetConnection()) return
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val currentReaction = post.userReaction
                val updatedCounts = post.reactionCounts.toMutableMap()
                val currentReactors = post.reactors.toMutableList()
                currentReactors.removeAll { it.user.id == _currentUser.value.id }

                val updated = if (currentReaction == reaction) {
                    val count = updatedCounts[reaction] ?: 1
                    if (count <= 1) updatedCounts.remove(reaction) else updatedCounts[reaction] = count - 1
                    post.copy(userReaction = null, reactionCounts = updatedCounts, reactors = currentReactors)
                } else {
                    if (currentReaction != null) {
                        val oldCount = updatedCounts[currentReaction] ?: 1
                        if (oldCount <= 1) updatedCounts.remove(currentReaction) else updatedCounts[currentReaction] = oldCount - 1
                    }
                    val newCount = (updatedCounts[reaction] ?: 0) + 1
                    updatedCounts[reaction] = newCount
                    currentReactors.add(0, ReactionUser(_currentUser.value, reaction, "Just now"))
                    post.copy(userReaction = reaction, reactionCounts = updatedCounts, reactors = currentReactors)
                }
                updatedPostRef = updated
                updated
            } else post
        }
        updatedPostRef?.let { post ->
            persistPostToRoom(post)
            com.example.data.FirebaseManager.reactToPostCloud(
                postId = postId,
                user = _currentUser.value,
                newReaction = post.userReaction,
                updatedCounts = post.reactionCounts
            )
        }
    }

    fun addComment(postId: String, text: String, customAuthor: User? = null) {
        if (text.isBlank()) return
        if (!requireInternetConnection()) return
        val newComment = Comment(
            id = "c_${UUID.randomUUID()}",
            postId = postId,
            author = customAuthor ?: _currentUser.value,
            text = text,
            timestamp = "Just now"
        )
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val nextComments = post.comments + newComment
                val upd = post.copy(
                    comments = nextComments,
                    cloudCommentCount = maxOf(post.cloudCommentCount + 1, nextComments.size)
                )
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
        // Sync to cloud Firestore
        com.example.data.FirebaseManager.saveComment(postId, newComment)
    }

    fun replyToComment(postId: String, commentId: String, text: String, customAuthor: User? = null) {
        if (text.isBlank()) return
        if (!requireInternetConnection()) return
        val newReply = CommentReply(
            id = "cr_${UUID.randomUUID()}",
            author = customAuthor ?: _currentUser.value,
            text = text,
            timestamp = "Just now"
        )
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        comment.copy(replies = comment.replies + newReply)
                    } else comment
                }
                val upd = post.copy(comments = updatedComments)
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
        com.example.data.FirebaseManager.saveCommentReply(postId, commentId, newReply)
    }

    fun observeCommentsForPost(postId: String) {
        if (commentListeners.containsKey(postId)) return
        val reg = com.example.data.FirebaseManager.listenToPostCommentsAndReplies(postId) { cloudComments, myPostReaction ->
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    val cloudIds = cloudComments.map { it.id }.toSet()
                    val localOnly = post.comments.filter { it.id !in cloudIds }
                    val merged = cloudComments + localOnly
                    post.copy(
                        comments = merged,
                        userReaction = myPostReaction ?: post.userReaction,
                        cloudCommentCount = maxOf(post.cloudCommentCount, merged.size)
                    )
                } else post
            }
        }
        if (reg != null) {
            commentListeners[postId] = reg
        }
    }

    fun stopObservingCommentsForPost(postId: String) {
        commentListeners.remove(postId)?.remove()
    }

    fun deleteComment(postId: String, commentId: String) {
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val upd = post.copy(comments = post.comments.filter { it.id != commentId })
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
    }

    fun deleteReply(postId: String, commentId: String, replyId: String) {
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        comment.copy(replies = comment.replies.filter { it.id != replyId })
                    } else comment
                }
                val upd = post.copy(comments = updatedComments)
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
    }

    fun boostPost(postId: String, budget: Int, days: Int) {
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val upd = post.copy(
                    isBoosted = true,
                    boostStatus = "Sponsored",
                    boostBudget = budget,
                    boostDays = days,
                    isBoostActive = true
                )
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    fun reactToComment(postId: String, commentId: String, reaction: ReactionType) {
        var updatedCommentRef: Comment? = null
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        val currentReaction = comment.userReaction
                        val updatedCounts = comment.reactionCounts.toMutableMap()
                        val currentReactors = comment.reactors.toMutableList()
                        currentReactors.removeAll { it.user.id == _currentUser.value.id }

                        val updated = if (currentReaction == reaction) {
                            val count = updatedCounts[reaction] ?: 1
                            if (count <= 1) updatedCounts.remove(reaction) else updatedCounts[reaction] = count - 1
                            comment.copy(userReaction = null, reactionCounts = updatedCounts, reactors = currentReactors, isLiked = false)
                        } else {
                            if (currentReaction != null) {
                                val oldCount = updatedCounts[currentReaction] ?: 1
                                if (oldCount <= 1) updatedCounts.remove(currentReaction) else updatedCounts[currentReaction] = oldCount - 1
                            }
                            val newCount = (updatedCounts[reaction] ?: 0) + 1
                            updatedCounts[reaction] = newCount
                            currentReactors.add(0, ReactionUser(_currentUser.value, reaction, "Just now"))
                            comment.copy(userReaction = reaction, reactionCounts = updatedCounts, reactors = currentReactors, isLiked = true)
                        }
                        updatedCommentRef = updated
                        updated
                    } else comment
                }
                val upd = post.copy(comments = updatedComments)
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
        updatedCommentRef?.let { c ->
            com.example.data.FirebaseManager.reactToCommentCloud(
                postId = postId,
                commentId = commentId,
                user = _currentUser.value,
                newReaction = c.userReaction,
                updatedCounts = c.reactionCounts
            )
        }
    }

    fun reactToCommentReply(postId: String, commentId: String, replyId: String, reaction: ReactionType) {
        var updatedReplyRef: CommentReply? = null
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        val updatedReplies = comment.replies.map { reply ->
                            if (reply.id == replyId) {
                                val currentReaction = reply.userReaction
                                val updatedCounts = reply.reactionCounts.toMutableMap()
                                val currentReactors = reply.reactors.toMutableList()
                                currentReactors.removeAll { it.user.id == _currentUser.value.id }

                                val updated = if (currentReaction == reaction) {
                                    val count = updatedCounts[reaction] ?: 1
                                    if (count <= 1) updatedCounts.remove(reaction) else updatedCounts[reaction] = count - 1
                                    reply.copy(userReaction = null, reactionCounts = updatedCounts, reactors = currentReactors, isLiked = false)
                                } else {
                                    if (currentReaction != null) {
                                        val oldCount = updatedCounts[currentReaction] ?: 1
                                        if (oldCount <= 1) updatedCounts.remove(currentReaction) else updatedCounts[currentReaction] = oldCount - 1
                                    }
                                    val newCount = (updatedCounts[reaction] ?: 0) + 1
                                    updatedCounts[reaction] = newCount
                                    currentReactors.add(0, ReactionUser(_currentUser.value, reaction, "Just now"))
                                    reply.copy(userReaction = reaction, reactionCounts = updatedCounts, reactors = currentReactors, isLiked = true)
                                }
                                updatedReplyRef = updated
                                updated
                            } else reply
                        }
                        comment.copy(replies = updatedReplies)
                    } else comment
                }
                val upd = post.copy(comments = updatedComments)
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
        updatedReplyRef?.let { r ->
            com.example.data.FirebaseManager.reactToReplyCloud(
                postId = postId,
                commentId = commentId,
                replyId = replyId,
                user = _currentUser.value,
                newReaction = r.userReaction,
                updatedCounts = r.reactionCounts
            )
        }
    }

    fun sharePost(postId: String) {
        if (!requireInternetConnection()) return
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val upd = post.copy(shareCount = post.shareCount + 1)
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let { persistPostToRoom(it) }
        com.example.data.FirebaseManager.incrementShareCountCloud(postId)
    }

    // Story (3 Photo Stories <= 300 KB = 0.9 MB + 1 Video Story <= 3 MB / 15s = 3.9 MB daily)
    fun addStory(
        mediaUrl: String,
        caption: String = "",
        musicTrack: String? = null,
        musicArtist: String? = null,
        audioUrl: String? = null
    ) {
        if (!requireInternetConnection()) return
        val isVid = mediaUrl.contains("video", ignoreCase = true) ||
            mediaUrl.endsWith(".mp4", ignoreCase = true) ||
            mediaUrl.endsWith(".mov", ignoreCase = true) ||
            mediaUrl.endsWith(".mkv", ignoreCase = true)
        val storyCategory = if (isVid) {
            com.example.data.DailyQuotaCategory.STORY_VIDEO
        } else {
            com.example.data.DailyQuotaCategory.STORY_PHOTO
        }
        val activeUser = _currentUser.value
        val verdict = com.example.data.DailyQuotaCompressionStore.checkQuotaBeforeUpload(
            userId = activeUser.id,
            category = storyCategory,
            itemCount = 1
        )
        if (!verdict.allowed) {
            _compressionToastMessage.value = verdict.reasonBn
            _safetyPolicyAlert.value = verdict.reasonBn
            return
        }

        val newStory = Story(
            id = "story_${UUID.randomUUID()}",
            author = activeUser,
            mediaUrl = mediaUrl,
            timestamp = "Just now",
            caption = caption,
            musicTrack = musicTrack,
            musicArtist = musicArtist,
            audioUrl = audioUrl
        )
        _stories.value = listOf(newStory) + _stories.value
        // Automatically add story to User Highlights (ইস্টুরি দিলে ঐ জায়গায় জমা হবে)
        addStoryToHighlights(activeUser.id, newStory, "My Stories 🌟")
        // Sync story to cloud Firestore
        com.example.data.FirebaseManager.saveStory(newStory)

        val ctx = com.example.data.CloudinaryManager.getAppContext()
        val isAutoCompress = com.example.data.DailyQuotaCompressionStore.config.value.isAutoCompressionEnabled
        val kind = if (isVid) com.example.data.CompressionMediaKind.STORY_VIDEO else com.example.data.CompressionMediaKind.PHOTO

        viewModelScope.launch(Dispatchers.IO) {
            var finalUsed = if (isVid) 2.8 else 285.0
            var savedMb = 0.0
            var uploadUri = if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://")) {
                android.net.Uri.parse(mediaUrl)
            } else null

            if (uploadUri != null && ctx != null && isAutoCompress) {
                try {
                    val outcome = if (isVid) {
                        com.example.data.MediaCompressionManager.compressAndTrimVideo(ctx, uploadUri, kind)
                    } else {
                        com.example.data.MediaCompressionManager.compressImageToMaxKb(ctx, uploadUri, maxKb = 300)
                    }
                    uploadUri = outcome.outputUri
                    _compressionToastMessage.value = outcome.summaryBn
                    savedMb = ((outcome.originalBytes - outcome.compressedBytes).coerceAtLeast(0L)) / (1024.0 * 1024.0)
                    finalUsed = if (isVid) {
                        outcome.effectiveQuotaMb.coerceAtMost(com.example.data.DailyQuotaCompressionStore.MAX_STORY_VIDEO_MB)
                    } else {
                        (outcome.compressedBytes / 1024.0).coerceIn(80.0, 300.0)
                    }
                } catch (_: Exception) {
                }
            }

            com.example.data.DailyQuotaCompressionStore.recordUsage(
                userId = activeUser.id,
                userName = activeUser.fullName,
                avatarUrl = activeUser.avatarUrl,
                category = storyCategory,
                itemCount = 1,
                usedMbOrKb = finalUsed,
                savedMb = savedMb
            )

            if (uploadUri != null &&
                com.example.data.CloudinaryManager.isEnabled() &&
                com.example.data.CloudinaryManager.isConfigured()
            ) {
                try {
                    com.example.data.CloudinaryManager.uploadMedia(
                        uri = uploadUri,
                        isVideoOrAudio = isVid,
                        folder = "fotofilm/stories",
                        compressionKind = kind,
                        onSuccess = { cloudUrl ->
                            val updatedStory = newStory.copy(mediaUrl = cloudUrl)
                            _stories.value = _stories.value.map { if (it.id == newStory.id) updatedStory else it }
                            com.example.data.FirebaseManager.saveStory(updatedStory)
                        },
                        onFailure = { }
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    fun createStoryWithMusic(audioTrack: PostAudioTrack) {
        val cover = audioTrack.coverArtUrl.orEmpty()
        val caption = "🎵 ${audioTrack.title} • ${audioTrack.artist}"
        addStory(cover, caption)
    }

    fun openStoryViewer(story: Story) {
        _viewingStory.value = story
        // Mark story as viewed so the ring turns grey like Facebook
        _stories.value = _stories.value.map {
            if (it.id == story.id) it.copy(isViewed = true) else it
        }
    }

    fun closeStoryViewer() {
        _viewingStory.value = null
    }

    // Chat & Messaging
    fun openChat(conversation: Conversation) {
        val readConv = conversation.copy(unreadCount = 0)
        _activeChat.value = readConv
        _conversations.value = _conversations.value.map { if (it.id == conversation.id) readConv else it }
        activeChatListener?.remove()
        activeChatListener = com.example.data.FirebaseManager.listenToChatMessages(conversation.id) { cloudMsgs ->
            val existingIds = cloudMsgs.map { it.id }.toSet()
            val currentMsgs = _activeChat.value?.messages ?: emptyList()
            val localMsgs = currentMsgs.filter { it.id !in existingIds }
            val merged = (localMsgs + cloudMsgs)
            val updated = readConv.copy(messages = merged)
            _activeChat.value = updated
            _conversations.value = listOf(updated) + _conversations.value.filter { it.id != conversation.id }
        }
    }

    fun closeChat() {
        activeChatListener?.remove()
        activeChatListener = null
        _activeChat.value = null
    }

    // Profile Navigation & Deep Profile Links
    fun buildProfileLink(user: User): String {
        val handle = user.username.ifBlank { user.id }
            .trim()
            .removePrefix("@")
            .replace(" ", "_")
        return "https://fotofilm.app/u/$handle"
    }

    fun resolveUserFromProfileLink(linkOrHandle: String): User? {
        val cleaned = linkOrHandle.trim()
            .removePrefix("https://fotofilm.app/u/")
            .removePrefix("http://fotofilm.app/u/")
            .removePrefix("https://facefeed.com/u/")
            .removePrefix("http://facefeed.com/u/")
            .removePrefix("facefeed://u/")
            .removePrefix("fotofilm://u/")
            .substringBefore("?")
            .substringBefore("#")
            .trim('/')
            .removePrefix("@")
            .trim()

        if (cleaned.isBlank()) return null

        val allCandidates = buildList {
            add(_currentUser.value)
            addAll(_allAppUsers.value)
            addAll(_conversations.value.map { it.friend })
            addAll(_posts.value.map { it.author })
        }.distinctBy { it.id }

        val directMatch = allCandidates.firstOrNull { u ->
            u.username.equals(cleaned, ignoreCase = true) ||
                u.id.equals(cleaned, ignoreCase = true) ||
                u.fullName.equals(cleaned, ignoreCase = true) ||
                u.fullName.replace(" ", "_").equals(cleaned, ignoreCase = true)
        }
        if (directMatch != null) return directMatch

        val profileFriend = SampleData.profileFriends.firstOrNull { pf ->
            pf.username.equals(cleaned, ignoreCase = true) ||
                pf.id.equals(cleaned, ignoreCase = true) ||
                pf.name.equals(cleaned, ignoreCase = true) ||
                pf.name.replace(" ", "_").equals(cleaned, ignoreCase = true)
        }
        if (profileFriend != null) {
            return User(
                id = profileFriend.id,
                fullName = profileFriend.name,
                username = profileFriend.username.ifBlank { profileFriend.id },
                avatarUrl = profileFriend.avatarUrl,
                isOnline = profileFriend.isOnline,
                isFriend = true
            )
        }
        return null
    }

    fun ensurePostsForUser(user: User) {
        // Strictly display real posts published by this user (no fake demo posts)
    }

    fun openUserProfile(user: User) {
        ensurePostsForUser(user)
        _viewingProfileUser.value = user
    }

    fun openUserProfileByLink(linkOrHandle: String): Boolean {
        val resolved = resolveUserFromProfileLink(linkOrHandle) ?: return false
        openUserProfile(resolved)
        return true
    }

    fun closeUserProfile() {
        _viewingProfileUser.value = null
    }

    fun openChatWithUser(user: User) {
        val existing = _conversations.value.firstOrNull { it.friend.id == user.id || it.friend.fullName.equals(user.fullName, ignoreCase = true) }
        if (existing != null) {
            _activeChat.value = existing
        } else {
            val newConv = Conversation(
                id = "conv_${user.id}_${System.currentTimeMillis()}",
                friend = user,
                lastMessage = "Say hello to ${user.fullName} 👋",
                lastTime = "এখন",
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = user.id,
                        senderName = user.fullName,
                        text = "Hello! 👋",
                        timestamp = "এখন",
                        isFromMe = false
                    )
                )
            )
            _conversations.value = listOf(newConv) + _conversations.value
            _activeChat.value = newConv
        }
        _activeTab.value = AppTab.MESSAGES
    }

    /**
     * Send Marketplace inquiry and auto-open chat with reply capability
     * Per user request:
     * "যেমন: আমি গরি বিক্রি করার জন্য পোস্ট করছি রহিম গরি কেনার জন্য আমারে মেসেজদিছে আর প্রোতেকের মেসেজের জেনো রিপলাই দেওয়া জায়ে এড ও চালু করে দেন"
     */
    fun sendMarketplaceInquiry(
        productTitle: String,
        productPrice: String,
        targetUser: User,
        customMessage: String? = null,
        isIncomingFromBuyer: Boolean = false
    ) {
        val existing = _conversations.value.firstOrNull { it.friend.id == targetUser.id || it.friend.fullName.equals(targetUser.fullName, ignoreCase = true) }
        val convId = existing?.id ?: "conv_${targetUser.id}_${System.currentTimeMillis()}"
        val initialText = customMessage ?: if (isIncomingFromBuyer) {
            "আসসালামু আলাইকুম ভাই! আমি আপনার এই ঘড়ি/পণ্যটি কিনতে চাই: $productTitle ($productPrice)। এটি কি এখনও আছে?"
        } else {
            "আসসালামু আলাইকুম, আমি আপনার এই পণ্যটি কিনতে আগ্রহী: $productTitle ($productPrice)। এটি কি এখনও পাওয়া যাবে?"
        }

        val firstMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            senderId = if (isIncomingFromBuyer) targetUser.id else _currentUser.value.id,
            senderName = if (isIncomingFromBuyer) targetUser.fullName else _currentUser.value.fullName,
            text = initialText,
            timestamp = "Just now",
            isFromMe = !isIncomingFromBuyer,
            status = MessageDeliveryStatus.DELIVERED
        )

        val now = System.currentTimeMillis()
        val conv = if (existing != null) {
            existing.copy(
                lastMessage = initialText,
                lastTime = "Just now",
                lastMessageTimestamp = now,
                messages = existing.messages + firstMsg
            )
        } else {
            Conversation(
                id = convId,
                friend = targetUser,
                lastMessage = initialText,
                lastTime = "Just now",
                lastMessageTimestamp = now,
                messages = listOf(firstMsg)
            )
        }

        _conversations.value = listOf(conv) + _conversations.value.filter { it.id != conv.id }
        _activeChat.value = conv
        _activeTab.value = AppTab.MESSAGES

        // Realistic interactive reply within 1.5 seconds so user can reply back!
        viewModelScope.launch {
            delay(1400)
            val replyText = if (isIncomingFromBuyer) {
                "ভাই, আমি ক্যাশ অন ডেলিভারিতে নিতে চাচ্ছিলাম। লোকেশন দিলে আজকেই নিয়ে নেব ইনশাআল্লাহ।"
            } else {
                "ওয়ালাইকুম আসসালাম! হ্যাঁ ভাই, $productTitle সম্পূর্ণ ফ্রেশ ও এভেইলেবল আছে। আপনি নিতে চাইলে আপনার ডেলিভারি ঠিকানা দিন।"
            }
            val replyMsg = ChatMessage(
                id = "msg_${UUID.randomUUID()}",
                senderId = targetUser.id,
                senderName = targetUser.fullName,
                text = replyText,
                timestamp = "Just now",
                isFromMe = false,
                replyTo = firstMsg,
                status = MessageDeliveryStatus.DELIVERED
            )
            val replyNow = System.currentTimeMillis()
            val convToUpdate = _conversations.value.find { it.id == conv.id }
            if (convToUpdate != null) {
                val isChatCurrentlyOpen = _activeChat.value?.id == conv.id
                val updated = convToUpdate.copy(
                    lastMessage = replyText,
                    lastTime = "Just now",
                    lastMessageTimestamp = replyNow,
                    unreadCount = if (isChatCurrentlyOpen) 0 else convToUpdate.unreadCount + 1,
                    messages = convToUpdate.messages + replyMsg
                )
                if (isChatCurrentlyOpen) {
                    _activeChat.value = updated
                }
                // Instantly jump to top (position 1) when reply arrives!
                _conversations.value = listOf(updated) + _conversations.value.filter { it.id != conv.id }
            }
        }
    }

    fun onUserTypingInChat(hasInputChanged: Boolean) {
        if (hasInputChanged && _isNotificationSoundEnabled.value) {
            com.example.util.AppSoundManager.playChatTypingSound()
        }
    }

    private fun updateChatMessageStatus(
        conversationId: String,
        messageId: String,
        newStatus: MessageDeliveryStatus
    ) {
        var updatedMsg: ChatMessage? = null
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val nextMessages = conv.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(status = newStatus).also { updatedMsg = it }
                    } else msg
                }
                val nextConv = conv.copy(messages = nextMessages)
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = nextConv
                }
                nextConv
            } else conv
        }
        updatedMsg?.let { persistChatMessageToRoom(conversationId, it) }
    }

    private fun scheduleMessengerDeliveryLifecycle(
        conversationId: String,
        sentMessage: ChatMessage,
        triggerPartnerReply: Boolean
    ) {
        viewModelScope.launch {
            // 1. Sent ✓ (immediate) -> Delivered ✓✓ (~520ms) with Messenger delivery chime
            delay(520)
            updateChatMessageStatus(conversationId, sentMessage.id, MessageDeliveryStatus.DELIVERED)
            if (_isNotificationSoundEnabled.value) {
                com.example.util.AppSoundManager.playChatMessageDeliveredSound()
            }

            // 2. Friend opens inbox -> Seen (~650ms later)
            delay(650)
            updateChatMessageStatus(conversationId, sentMessage.id, MessageDeliveryStatus.SEEN)

            if (!triggerPartnerReply) return@launch
            val currentConv = _conversations.value.find { it.id == conversationId } ?: return@launch

            // 3. Friend starts typing in inbox ("গর গর" typing sound while typing indicator is visible)
            delay(250)
            _typingConversationId.value = conversationId
            repeat(3) {
                if (_activeChat.value?.id == conversationId && _isNotificationSoundEnabled.value) {
                    com.example.util.AppSoundManager.playChatTypingSound()
                }
                delay(280)
            }
            _typingConversationId.value = null

            // 4. Friend sends reply + Messenger incoming sound
            val replyReplies = listOf(
                "জি ভাই, আপনার মেসেজ পেয়েছি। ধন্যবাদ!",
                "ঠিক আছে ভাই, কথা বলে খুব ভালো লাগলো।",
                "ইনশাআল্লাহ, কথা অনুযায়ী কাজ হবে।",
                "আপনার সাথে ডিল কনফার্ম রইলো ভাই।",
                "জি ভাই, আমি প্রস্তুত আছি।"
            )
            val autoText = replyReplies.random()
            val partnerReply = ChatMessage(
                id = "msg_${UUID.randomUUID()}",
                senderId = currentConv.friend.id,
                senderName = currentConv.friend.fullName,
                text = autoText,
                timestamp = "Just now",
                isFromMe = false,
                replyTo = sentMessage,
                status = MessageDeliveryStatus.DELIVERED
            )
            val replyNow = System.currentTimeMillis()
            val convToUpdate = _conversations.value.find { it.id == conversationId }
            if (convToUpdate != null) {
                val isChatCurrentlyOpen = _activeChat.value?.id == conversationId
                val upd = convToUpdate.copy(
                    lastMessage = autoText,
                    lastTime = "Just now",
                    lastMessageTimestamp = replyNow,
                    unreadCount = if (isChatCurrentlyOpen) 0 else convToUpdate.unreadCount + 1,
                    messages = convToUpdate.messages + partnerReply
                )
                if (isChatCurrentlyOpen) {
                    _activeChat.value = upd
                }
                _conversations.value = listOf(upd) + _conversations.value.filter { it.id != conversationId }
                persistChatMessageToRoom(conversationId, partnerReply)
                if (_isNotificationSoundEnabled.value) {
                    com.example.util.AppSoundManager.playChatIncomingSound()
                }
            }
        }
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        mediaUrl: String? = null,
        isVideo: Boolean = false,
        replyTo: ChatMessage? = null
    ) {
        if (text.isBlank() && mediaUrl == null) return
        if (!requireInternetConnection()) return
        val currentConv = _conversations.value.find { it.id == conversationId }

        if (_isNotificationSoundEnabled.value) {
            com.example.util.AppSoundManager.playChatMessageSentSound()
        }

        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.fullName,
            text = text,
            timestamp = "Just now",
            mediaUrl = mediaUrl,
            isMediaVideo = isVideo,
            isFromMe = true,
            replyTo = replyTo,
            status = MessageDeliveryStatus.SENT
        )
        val now = System.currentTimeMillis()
        val targetConv = currentConv ?: _conversations.value.find { it.id == conversationId }
        if (targetConv != null) {
            val updated = targetConv.copy(
                lastMessage = if (text.isNotBlank()) text else if (isVideo) "📹 Video" else "📷 Photo",
                lastTime = "Just now",
                lastMessageTimestamp = now,
                messages = targetConv.messages + newMsg
            )
            if (_activeChat.value?.id == conversationId) {
                _activeChat.value = updated
            }
            // Instantly move this updated chat to position 1 on top of the list!
            _conversations.value = listOf(updated) + _conversations.value.filter { it.id != conversationId }
        }

        // Always log to Permanent Delivery Vault (Never deleted even if chat is cleared)
        val vaultItem = DeliveredVaultMessage(
            id = "vault_${newMsg.id}",
            conversationId = conversationId,
            contactId = currentConv?.friend?.id ?: "unknown",
            contactName = currentConv?.friend?.fullName ?: "Friend",
            contactAvatarUrl = currentConv?.friend?.avatarUrl ?: "",
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.fullName,
            isFromMe = true,
            text = text,
            mediaUrl = mediaUrl,
            isMediaVideo = isVideo,
            isAudioVoice = false,
            audioDurationSeconds = 0,
            timestamp = "Just now",
            formattedDate = "আজ"
        )
        _permanentDeliveryVault.value = listOf(vaultItem) + _permanentDeliveryVault.value
        persistChatMessageToRoom(conversationId, newMsg)

        com.example.data.FirebaseManager.sendMessage(
            conversationId = conversationId,
            message = newMsg,
            recipientId = currentConv?.friend?.id ?: "user_tanvir"
        )

        if (!mediaUrl.isNullOrBlank() &&
            (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://"))
        ) {
            val chatKind = if (isVideo) com.example.data.CompressionMediaKind.CHAT_VIDEO else com.example.data.CompressionMediaKind.PHOTO
            com.example.data.DailyQuotaCompressionStore.recordChatFilmFreeMedia(
                userId = _currentUser.value.id,
                userName = _currentUser.value.fullName,
                avatarUrl = _currentUser.value.avatarUrl
            )
            val ctx = appContextRef ?: com.example.data.CloudinaryManager.getAppContext()
            if (ctx != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    val persistedUrl = com.example.data.local.MediaPersistenceHelper.persistUriIfLocal(
                        ctx,
                        mediaUrl,
                        subDir = if (isVideo) "chat_videos" else "chat_photos",
                        defaultExtension = if (isVideo) "mp4" else "jpg"
                    )
                    if (persistedUrl != mediaUrl) {
                        val persistedMsg = newMsg.copy(mediaUrl = persistedUrl)
                        _conversations.value = _conversations.value.map { c ->
                            if (c.id == conversationId) {
                                val nextMsgs = c.messages.map { if (it.id == newMsg.id) persistedMsg else it }
                                val upd = c.copy(messages = nextMsgs)
                                if (_activeChat.value?.id == conversationId) _activeChat.value = upd
                                upd
                            } else c
                        }
                        localRepo?.upsertChatMessage(conversationId, persistedMsg)
                    }
                }
            }
            if (com.example.data.CloudinaryManager.isEnabled() && com.example.data.CloudinaryManager.isConfigured()) {
                try {
                    val uri = android.net.Uri.parse(mediaUrl)
                    com.example.data.CloudinaryManager.uploadMedia(
                        uri = uri,
                        isVideoOrAudio = isVideo,
                        folder = "fotofilm/chatfilm",
                        compressionKind = chatKind,
                        onSuccess = { cloudUrl ->
                            val syncedMsg = newMsg.copy(mediaUrl = cloudUrl)
                            persistChatMessageToRoom(conversationId, syncedMsg)
                            com.example.data.FirebaseManager.sendMessage(
                                conversationId = conversationId,
                                message = syncedMsg,
                                recipientId = currentConv?.friend?.id ?: "user_tanvir"
                            )
                        },
                        onFailure = { }
                    )
                } catch (_: Exception) {
                }
            }
        }

        // Messenger Sent -> Delivered -> Seen lifecycle + Friend typing & reply sound
        val shouldReply = currentConv != null && !currentConv.friend.id.equals(_currentUser.value.id)
        scheduleMessengerDeliveryLifecycle(
            conversationId = conversationId,
            sentMessage = newMsg,
            triggerPartnerReply = shouldReply
        )
    }

    fun sendVoiceMessage(
        conversationId: String,
        durationSeconds: Int,
        audioUrl: String? = null,
        replyTo: ChatMessage? = null
    ) {
        val safeDuration = durationSeconds.coerceAtLeast(1)
        val currentConv = _conversations.value.find { it.id == conversationId }

        if (_isNotificationSoundEnabled.value) {
            com.example.util.AppSoundManager.playChatMessageSentSound()
        }

        val newMsg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.fullName,
            text = "🎤 অডিও ভয়েস (${safeDuration}s)",
            timestamp = "Just now",
            mediaUrl = audioUrl,
            isMediaVideo = false,
            isFromMe = true,
            replyTo = replyTo,
            isAudioVoice = true,
            audioDurationSeconds = safeDuration,
            status = MessageDeliveryStatus.SENT
        )
        val now = System.currentTimeMillis()
        val targetConv = currentConv ?: _conversations.value.find { it.id == conversationId }
        if (targetConv != null) {
            val updated = targetConv.copy(
                lastMessage = "🎤 অডিও ভয়েস (${safeDuration}s)",
                lastTime = "Just now",
                lastMessageTimestamp = now,
                messages = targetConv.messages + newMsg
            )
            if (_activeChat.value?.id == conversationId) {
                _activeChat.value = updated
            }
            // Instantly move this updated chat to position 1 on top of the list!
            _conversations.value = listOf(updated) + _conversations.value.filter { it.id != conversationId }
        }

        // Persist immediately in Room + Delivery Vault + Cloud Firestore
        persistChatMessageToRoom(conversationId, newMsg)
        com.example.data.FirebaseManager.sendMessage(
            conversationId = conversationId,
            message = newMsg,
            recipientId = currentConv?.friend?.id ?: "user_tanvir"
        )

        val vaultVoiceItem = DeliveredVaultMessage(
            id = "vault_${newMsg.id}",
            conversationId = conversationId,
            contactId = currentConv?.friend?.id ?: "unknown",
            contactName = currentConv?.friend?.fullName ?: "Friend",
            contactAvatarUrl = currentConv?.friend?.avatarUrl ?: "",
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.fullName,
            isFromMe = true,
            text = "🎤 অডিও ভয়েস (${safeDuration}s)",
            mediaUrl = audioUrl,
            isMediaVideo = false,
            isAudioVoice = true,
            audioDurationSeconds = safeDuration,
            timestamp = "Just now",
            formattedDate = "আজ"
        )
        _permanentDeliveryVault.value = listOf(vaultVoiceItem) + _permanentDeliveryVault.value

        scheduleMessengerDeliveryLifecycle(
            conversationId = conversationId,
            sentMessage = newMsg,
            triggerPartnerReply = false
        )

        if (!audioUrl.isNullOrBlank() && (audioUrl.startsWith("file://") || audioUrl.startsWith("content://") || audioUrl.startsWith("/"))) {
            val ctx = appContextRef ?: com.example.data.CloudinaryManager.getAppContext()
            viewModelScope.launch(Dispatchers.IO) {
                val durableUrl = if (ctx != null) {
                    com.example.data.local.MediaPersistenceHelper.persistUriIfLocal(ctx, audioUrl, subDir = "voice_notes", defaultExtension = "m4a")
                } else audioUrl
                val localSyncedMsg = newMsg.copy(mediaUrl = durableUrl)
                localRepo?.upsertChatMessage(conversationId, localSyncedMsg)

                val uploadUri = android.net.Uri.parse(
                    if (durableUrl.startsWith("/")) android.net.Uri.fromFile(java.io.File(durableUrl)).toString() else durableUrl
                )
                val onVoiceUploaded: (String) -> Unit = { cloudUrl ->
                    val cloudMsg = localSyncedMsg.copy(mediaUrl = cloudUrl)
                    com.example.data.FirebaseManager.sendMessage(
                        conversationId = conversationId,
                        message = cloudMsg,
                        recipientId = currentConv?.friend?.id ?: "user_tanvir"
                    )
                }
                if (com.example.data.CloudinaryManager.isEnabled() && com.example.data.CloudinaryManager.isConfigured()) {
                    com.example.data.CloudinaryManager.uploadMedia(
                        uri = uploadUri,
                        isVideoOrAudio = true,
                        folder = "fotofilm/chatfilm_voice",
                        onSuccess = onVoiceUploaded,
                        onFailure = {
                            com.example.data.FirebaseManager.uploadFileToStorage(
                                uri = uploadUri,
                                folder = "chat_voice",
                                onSuccess = onVoiceUploaded,
                                onFailure = { }
                            )
                        }
                    )
                } else {
                    com.example.data.FirebaseManager.uploadFileToStorage(
                        uri = uploadUri,
                        folder = "chat_voice",
                        onSuccess = onVoiceUploaded,
                        onFailure = { }
                    )
                }
            }
        }
    }

    fun reactToChatMessage(conversationId: String, messageId: String, reaction: String) {
        var reactedMsgRef: ChatMessage? = null
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val updatedMessages = conv.messages.map { msg ->
                    if (msg.id == messageId) {
                        val currentReactions = msg.reactions.toMutableMap()
                        val prevReaction = msg.userReaction
                        if (prevReaction != null) {
                            val count = currentReactions[prevReaction] ?: 1
                            if (count <= 1) currentReactions.remove(prevReaction)
                            else currentReactions[prevReaction] = count - 1
                        }
                        val nextMsg = if (prevReaction != reaction) {
                            currentReactions[reaction] = (currentReactions[reaction] ?: 0) + 1
                            msg.copy(reactions = currentReactions, userReaction = reaction)
                        } else {
                            msg.copy(reactions = currentReactions, userReaction = null)
                        }
                        reactedMsgRef = nextMsg
                        nextMsg
                    } else msg
                }
                val updated = conv.copy(messages = updatedMessages)
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
        reactedMsgRef?.let { persistChatMessageToRoom(conversationId, it) }
    }

    // Messenger Conversation Management Features
    fun togglePinConversation(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(isPinned = !conv.isPinned) else conv
        }
    }

    fun toggleMuteConversation(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(isMuted = !conv.isMuted) else conv
        }
    }

    fun archiveConversation(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(isArchived = true) else conv
        }
        if (_activeChat.value?.id == id) {
            _activeChat.value = null
        }
    }

    fun unarchiveConversation(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(isArchived = false) else conv
        }
    }

    // ChatFilm Active Status & Message Requests Management
    private val _isActiveStatusEnabled = MutableStateFlow(true)
    val isActiveStatusEnabled: StateFlow<Boolean> = _isActiveStatusEnabled.asStateFlow()

    fun toggleActiveStatus() {
        val newValue = !_isActiveStatusEnabled.value
        _isActiveStatusEnabled.value = newValue
        _currentUser.value = _currentUser.value.copy(isOnline = newValue)
    }

    private val _messageRequests = MutableStateFlow<List<User>>(emptyList())
    val messageRequests: StateFlow<List<User>> = _messageRequests.asStateFlow()

    fun acceptMessageRequest(friend: User) {
        _messageRequests.value = _messageRequests.value.filter { it.id != friend.id }
        val existing = _conversations.value.find { it.friend.id == friend.id }
        val targetConv = existing ?: Conversation(
            id = "conv_req_${friend.id}",
            friend = friend,
            lastMessage = "Request accepted! You can now chat 🎉",
            lastTime = "Just now",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        if (existing == null) {
            _conversations.value = listOf(targetConv) + _conversations.value
        }
        openChat(targetConv)
    }

    fun deleteConversation(id: String) {
        _conversations.value = _conversations.value.filter { it.id != id }
        if (_activeChat.value?.id == id) {
            _activeChat.value = null
        }
        localRepo?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) { repo.markConversationDeleted(id) }
        }
    }

    // Delete a specific message for ME (Screenshot 1: "Delete for me")
    fun deleteChatMessageForMe(conversationId: String, messageId: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val updatedMessages = conv.messages.filter { it.id != messageId }
                val updated = conv.copy(
                    messages = updatedMessages,
                    lastMessage = updatedMessages.lastOrNull()?.text ?: conv.lastMessage
                )
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
        localRepo?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) { repo.markChatMessageDeleted(conversationId, messageId) }
        }
    }

    // Forward message to selected conversations (Screenshot 1: "Forward")
    fun forwardChatMessage(message: ChatMessage, targetConversationIds: List<String>) {
        val me = _currentUser.value
        val toPersist = mutableListOf<Pair<String, ChatMessage>>()
        _conversations.value = _conversations.value.map { conv ->
            if (targetConversationIds.contains(conv.id)) {
                val forwardedMsg = ChatMessage(
                    id = "msg_fwd_${System.currentTimeMillis()}_${conv.id}",
                    senderId = me.id,
                    senderName = me.fullName,
                    text = message.text,
                    timestamp = "এইমাত্র",
                    mediaUrl = message.mediaUrl,
                    isMediaVideo = message.isMediaVideo,
                    isAudioVoice = message.isAudioVoice,
                    audioDurationSeconds = message.audioDurationSeconds,
                    isFromMe = true,
                    status = MessageDeliveryStatus.SENT
                )
                toPersist.add(conv.id to forwardedMsg)
                val updated = conv.copy(
                    messages = conv.messages + forwardedMsg,
                    lastMessage = forwardedMsg.text.ifBlank { "ফরওয়ার্ড করা মিডিয়া" },
                    lastTime = "Just now"
                )
                if (_activeChat.value?.id == conv.id) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
        toPersist.forEach { (convId, msg) -> persistChatMessageToRoom(convId, msg) }
    }

    // Support Team User Messages & Tickets (Screenshot 2: Help & Support, and Right Manager Fida Option)
    fun submitSupportTicket(
        message: String,
        profileUrl: String,
        screenshotUrls: List<String>
    ) {
        val user = _currentUser.value
        val newTicket = SupportTicket(
            id = "ticket_${System.currentTimeMillis()}",
            userId = user.id,
            userName = user.fullName,
            userAvatar = user.avatarUrl,
            profileUrl = profileUrl.ifBlank { "https://facefeed.com/user/${user.username}" },
            message = message,
            screenshotUrls = screenshotUrls,
            timestamp = "এইমাত্র",
            status = "Open"
        )
        _supportTickets.value = listOf(newTicket) + _supportTickets.value
    }

    fun replyToSupportTicket(ticketId: String, replyText: String) {
        _supportTickets.value = _supportTickets.value.map { ticket ->
            if (ticket.id == ticketId) {
                ticket.copy(
                    adminReply = replyText,
                    adminReplyTime = "এইমাত্র",
                    status = "Resolved"
                )
            } else ticket
        }
    }

    fun deleteSupportTicket(ticketId: String) {
        _supportTickets.value = _supportTickets.value.filter { it.id != ticketId }
    }

    fun markConversationAsRead(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(unreadCount = 0) else conv
        }
    }

    fun markConversationAsUnread(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(unreadCount = 1) else conv
        }
    }

    fun createGroupChat(groupName: String, members: List<User>) {
        val newGroupChat = Conversation(
            id = "group_chat_${System.currentTimeMillis()}",
            friend = members.firstOrNull() ?: _currentUser.value,
            isGroup = true,
            groupName = groupName,
            groupMembers = listOf(_currentUser.value) + members,
            lastMessage = "গ্রুপ তৈরি করা হয়েছে। সবাই যোগ দিয়েছেন 🎉",
            lastTime = "Just now",
            unreadCount = 0,
            messages = listOf(
                ChatMessage(
                    id = "msg_init_${System.currentTimeMillis()}",
                    senderId = _currentUser.value.id,
                    senderName = _currentUser.value.fullName,
                    text = "Welcome to $groupName! 🚀",
                    timestamp = "Just now",
                    isFromMe = true
                )
            )
        )
        _conversations.value = listOf(newGroupChat) + _conversations.value
        _activeChat.value = newGroupChat
    }

    fun acceptMessageRequest(id: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) conv.copy(isMessageRequest = false) else conv
        }
    }

    fun declineMessageRequest(id: String) {
        _messageRequests.value = _messageRequests.value.filter { it.id != id }
        deleteConversation(id)
    }

    fun isUserBlocked(userId: String): Boolean {
        return _blockedUserIds.value.contains(userId) ||
                _conversations.value.any { it.friend.id == userId && it.isBlocked }
    }

    fun blockUser(userId: String) {
        val foundUser = _allAppUsers.value.find { it.id == userId }
            ?: _conversations.value.find { it.friend.id == userId }?.friend
        if (foundUser != null && isUserNiyonIslamSohag(foundUser)) {
            // সম্মানিত অ্যাপের মালিক Niyon Islam Sohag-কে কোনোভাবেই ব্লক করা সম্ভব নয়
            return
        }
        _blockedUserIds.value = _blockedUserIds.value + userId
        _conversations.value = _conversations.value.map { conv ->
            if (conv.friend.id == userId || conv.id == userId) conv.copy(isBlocked = true) else conv
        }
        if (_activeChat.value?.friend?.id == userId || _activeChat.value?.id == userId) {
            _activeChat.value = _activeChat.value?.copy(isBlocked = true)
        }
        if (foundUser != null && _blockedProfiles.value.none { it.id == userId }) {
            _blockedProfiles.value = _blockedProfiles.value + foundUser
        }
    }

    fun blockUser(user: User) {
        if (isUserNiyonIslamSohag(user)) {
            // সম্মানিত অ্যাপের মালিক Niyon Islam Sohag-কে কোনোভাবেই ব্লক করা সম্ভব নয়
            return
        }
        _blockedUserIds.value = _blockedUserIds.value + user.id
        if (_blockedProfiles.value.none { it.id == user.id }) {
            _blockedProfiles.value = _blockedProfiles.value + user
        }
        _conversations.value = _conversations.value.map { conv ->
            if (conv.friend.id == user.id || conv.id == user.id) conv.copy(isBlocked = true) else conv
        }
        if (_activeChat.value?.friend?.id == user.id || _activeChat.value?.id == user.id) {
            _activeChat.value = _activeChat.value?.copy(isBlocked = true)
        }
    }

    fun unblockUser(userId: String) {
        _blockedUserIds.value = _blockedUserIds.value - userId
        _blockedProfiles.value = _blockedProfiles.value.filter { it.id != userId }
        _conversations.value = _conversations.value.map { conv ->
            if (conv.friend.id == userId || conv.id == userId) conv.copy(isBlocked = false) else conv
        }
        if (_activeChat.value?.friend?.id == userId || _activeChat.value?.id == userId) {
            _activeChat.value = _activeChat.value?.copy(isBlocked = false)
        }
    }

    // Restricted Profiles System
    fun unrestrictUser(userId: String) {
        _restrictedProfiles.value = _restrictedProfiles.value.filter { it.id != userId }
    }

    fun restrictUser(user: User) {
        if (_restrictedProfiles.value.none { it.id == user.id }) {
            _restrictedProfiles.value = _restrictedProfiles.value + user
        }
    }

    fun blockConversation(id: String) {
        val conv = _conversations.value.find { it.id == id }
        if (conv != null && isUserNiyonIslamSohag(conv.friend)) {
            // সম্মানিত অ্যাপের মালিক Niyon Islam Sohag-কে কোনোভাবেই ব্লক করা সম্ভব নয়
            return
        }
        if (conv != null) {
            _blockedUserIds.value = _blockedUserIds.value + conv.friend.id
        }
        _conversations.value = _conversations.value.map { c ->
            if (c.id == id) c.copy(isBlocked = true) else c
        }
        if (_activeChat.value?.id == id) {
            _activeChat.value = _activeChat.value?.copy(isBlocked = true)
        }
    }

    fun unblockConversation(id: String) {
        val conv = _conversations.value.find { it.id == id }
        if (conv != null) {
            _blockedUserIds.value = _blockedUserIds.value - conv.friend.id
        }
        _conversations.value = _conversations.value.map { c ->
            if (c.id == id) c.copy(isBlocked = false) else c
        }
        if (_activeChat.value?.id == id) {
            _activeChat.value = _activeChat.value?.copy(isBlocked = false)
        }
    }

    // Calling System (Agora HD + Real-Time Firestore Signaling + Messenger Inbox Call Log)
    private fun findOrCreateDirectConversationForCall(user: User): Conversation {
        val currentActive = _activeChat.value
        if (currentActive != null && !currentActive.isGroup &&
            (currentActive.friend.id == user.id || currentActive.friend.fullName.equals(user.fullName, ignoreCase = true))
        ) {
            return currentActive
        }
        val existing = _conversations.value.firstOrNull {
            !it.isGroup && (it.friend.id == user.id || it.friend.fullName.equals(user.fullName, ignoreCase = true))
        }
        if (existing != null) return existing

        val created = Conversation(
            id = "conv_${user.id}",
            friend = user,
            lastMessage = "",
            lastTime = "এখন",
            messages = emptyList(),
            lastMessageTimestamp = System.currentTimeMillis()
        )
        _conversations.value = listOf(created) + _conversations.value
        return created
    }

    private fun formatCallLogDuration(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val mins = s / 60
        val secs = s % 60
        return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }

    private fun upsertCallLogInConversation(
        conversation: Conversation,
        callMsg: ChatMessage,
        syncCloud: Boolean
    ) {
        val now = System.currentTimeMillis()
        val latestConv = _conversations.value.find { it.id == conversation.id } ?: conversation
        val hasExisting = latestConv.messages.any { it.id == callMsg.id }
        val nextMsgs = if (hasExisting) {
            latestConv.messages.map { if (it.id == callMsg.id) callMsg else it }
        } else {
            latestConv.messages + callMsg
        }
        val isChatOpen = _activeChat.value?.id == latestConv.id
        val updated = latestConv.copy(
            lastMessage = callMsg.text,
            lastTime = "এখন",
            lastMessageTimestamp = now,
            unreadCount = if (!callMsg.isFromMe && !isChatOpen && !hasExisting) latestConv.unreadCount + 1 else latestConv.unreadCount,
            messages = nextMsgs
        )
        if (isChatOpen) {
            _activeChat.value = updated
        }
        _conversations.value = listOf(updated) + _conversations.value.filter { it.id != updated.id }
        persistChatMessageToRoom(updated.id, callMsg)
        if (syncCloud) {
            com.example.data.FirebaseManager.sendMessage(
                conversationId = updated.id,
                message = callMsg,
                recipientId = updated.friend.id
            )
        }
    }

    fun startAudioCall(user: User) {
        launchOutgoingDirectCall(user, CallType.AUDIO)
    }

    fun startVideoCall(user: User) {
        launchOutgoingDirectCall(user, CallType.VIDEO)
    }

    private fun launchOutgoingDirectCall(user: User, callType: CallType) {
        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val channelName = com.example.data.AgoraCallManager.buildDirectCallChannel(_currentUser.value.id, user.id)
        _incomingCallSignal.value = null
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = callType,
            user = user,
            durationSeconds = 0,
            isSpeakerOn = (callType == CallType.VIDEO),
            callId = callId,
            channelName = channelName,
            isIncoming = false,
            isAccepted = false
        )

        val targetConv = findOrCreateDirectConversationForCall(user)
        val initialCallMsg = ChatMessage(
            id = "call_msg_$callId",
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.fullName,
            text = if (callType == CallType.VIDEO) "ভিডিও কল করা হয়েছে (রিং হচ্ছে...)" else "অডিও কল করা হয়েছে (রিং হচ্ছে...)",
            timestamp = "এখন",
            isFromMe = true,
            status = MessageDeliveryStatus.SENT,
            callLogType = callType,
            callDurationSeconds = 0,
            isMissedCall = false
        )
        upsertCallLogInConversation(targetConv, initialCallMsg, syncCloud = false)

        outgoingCallListener?.remove()
        com.example.data.FirebaseManager.sendCallSignal(
            callId = callId,
            caller = _currentUser.value,
            callee = user,
            channelName = channelName,
            callType = callType
        )
        outgoingCallListener = com.example.data.FirebaseManager.listenToOutgoingCallStatus(callId) { status ->
            when (status) {
                "ACCEPTED" -> {
                    _activeCall.value = _activeCall.value?.copy(isAccepted = true)
                    val conv = findOrCreateDirectConversationForCall(user)
                    upsertCallLogInConversation(
                        conv,
                        initialCallMsg.copy(
                            text = if (callType == CallType.VIDEO) "ভিডিও কল চলছে..." else "অডিও কল চলছে...",
                            status = MessageDeliveryStatus.DELIVERED
                        ),
                        syncCloud = false
                    )
                }
                "DECLINED", "ENDED" -> {
                    endCall(updateRemote = false)
                }
            }
        }
        startCallTimer()
    }

    fun startGroupCall(groupName: String, members: List<User>) {
        val callId = "grp_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val channelName = com.example.data.AgoraCallManager.sanitizeChannelName("ff_grp_$groupName")
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = CallType.GROUP,
            groupName = groupName,
            groupMembers = members,
            durationSeconds = 0,
            isSpeakerOn = true,
            callId = callId,
            channelName = channelName,
            isIncoming = false,
            isAccepted = true
        )
        startCallTimer()
    }

    fun acceptIncomingCall() {
        com.example.util.AppSoundManager.stopCallDialingTone()
        val signal = _incomingCallSignal.value ?: return
        _incomingCallSignal.value = null
        com.example.data.FirebaseManager.updateCallSignalStatus(signal.callId, "ACCEPTED")
        val callerUser = allAppUsers.value.find { it.id == signal.callerId } ?: User(
            id = signal.callerId,
            fullName = signal.callerName,
            username = signal.callerName.lowercase().replace(" ", "_"),
            avatarUrl = signal.callerAvatar
        )
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = signal.callType,
            user = callerUser,
            groupName = signal.groupName,
            durationSeconds = 0,
            isSpeakerOn = (signal.callType != CallType.AUDIO),
            callId = signal.callId,
            channelName = signal.channelName,
            isIncoming = true,
            isAccepted = true
        )

        if (signal.callType != CallType.GROUP) {
            val conv = findOrCreateDirectConversationForCall(callerUser)
            val acceptedCallMsg = ChatMessage(
                id = "call_msg_${signal.callId}",
                senderId = callerUser.id,
                senderName = callerUser.fullName,
                text = if (signal.callType == CallType.VIDEO) "ইনকামিং ভিডিও কল চলছে..." else "ইনকামিং অডিও কল চলছে...",
                timestamp = "এখন",
                isFromMe = false,
                status = MessageDeliveryStatus.SEEN,
                callLogType = signal.callType,
                callDurationSeconds = 0,
                isMissedCall = false
            )
            upsertCallLogInConversation(conv, acceptedCallMsg, syncCloud = false)
        }

        outgoingCallListener?.remove()
        outgoingCallListener = com.example.data.FirebaseManager.listenToOutgoingCallStatus(signal.callId) { status ->
            if (status == "ENDED" || status == "DECLINED") {
                endCall(updateRemote = false)
            }
        }
        startCallTimer()
    }

    fun declineIncomingCall() {
        val signal = _incomingCallSignal.value ?: return
        _incomingCallSignal.value = null
        com.example.data.FirebaseManager.updateCallSignalStatus(signal.callId, "DECLINED")

        if (signal.callType != CallType.GROUP) {
            val callerUser = allAppUsers.value.find { it.id == signal.callerId } ?: User(
                id = signal.callerId,
                fullName = signal.callerName,
                username = signal.callerName.lowercase().replace(" ", "_"),
                avatarUrl = signal.callerAvatar
            )
            val conv = findOrCreateDirectConversationForCall(callerUser)
            val missedCallMsg = ChatMessage(
                id = "call_msg_${signal.callId}",
                senderId = callerUser.id,
                senderName = callerUser.fullName,
                text = if (signal.callType == CallType.VIDEO) "মিসড ভিডিও কল" else "মিসড অডিও কল",
                timestamp = "এখন",
                isFromMe = false,
                status = MessageDeliveryStatus.DELIVERED,
                callLogType = signal.callType,
                callDurationSeconds = 0,
                isMissedCall = true
            )
            upsertCallLogInConversation(conv, missedCallMsg, syncCloud = true)
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { current ->
                    current.copy(durationSeconds = current.durationSeconds + 1)
                } ?: break
            }
        }
    }

    fun toggleMute() {
        val nextMuted = !(_activeCall.value?.isMuted ?: false)
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = nextMuted) }
        com.example.data.AgoraCallManager.setMicMuted(nextMuted)
    }

    fun toggleSpeaker() {
        val nextSpeaker = !(_activeCall.value?.isSpeakerOn ?: true)
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = nextSpeaker) }
        com.example.data.AgoraCallManager.setSpeakerEnabled(nextSpeaker)
    }

    fun toggleCameraFlip() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoFrontCamera = !it.isVideoFrontCamera) }
        com.example.data.AgoraCallManager.switchCamera()
    }

    fun endCall(updateRemote: Boolean = true) {
        com.example.util.AppSoundManager.stopCallDialingTone()
        val endedCall = _activeCall.value
        val closingCallId = endedCall?.callId.orEmpty()
        val wasAgoraRemoteJoined = com.example.data.AgoraCallManager.callState.value.remoteUids.isNotEmpty()
        callTimerJob?.cancel()
        outgoingCallListener?.remove()
        outgoingCallListener = null
        if (updateRemote && closingCallId.isNotBlank()) {
            com.example.data.FirebaseManager.updateCallSignalStatus(closingCallId, "ENDED")
        }
        com.example.data.AgoraCallManager.leaveCall()
        _activeCall.value = null

        if (endedCall != null && closingCallId.isNotBlank()) {
            val wasAnswered = endedCall.isAccepted || wasAgoraRemoteJoined
            val dur = if (wasAnswered) endedCall.durationSeconds.coerceAtLeast(1) else 0
            val durText = formatCallLogDuration(dur)

            if (endedCall.callType == CallType.GROUP) {
                val grpConv = _conversations.value.firstOrNull {
                    it.isGroup && it.groupName.equals(endedCall.groupName, ignoreCase = true)
                } ?: _conversations.value.firstOrNull { it.isGroup }
                if (grpConv != null) {
                    val grpMsg = ChatMessage(
                        id = "call_msg_$closingCallId",
                        senderId = _currentUser.value.id,
                        senderName = _currentUser.value.fullName,
                        text = "গ্রুপ ভিডিও কল • $durText",
                        timestamp = "এখন",
                        isFromMe = true,
                        status = MessageDeliveryStatus.SEEN,
                        callLogType = CallType.GROUP,
                        callDurationSeconds = dur,
                        isMissedCall = false
                    )
                    upsertCallLogInConversation(grpConv, grpMsg, syncCloud = true)
                }
            } else if (endedCall.user != null) {
                val targetUser = endedCall.user
                val conv = findOrCreateDirectConversationForCall(targetUser)
                val finalText = when {
                    wasAnswered && endedCall.callType == CallType.VIDEO -> "ভিডিও কল • $durText"
                    wasAnswered -> "অডিও কল • $durText"
                    endedCall.isIncoming && endedCall.callType == CallType.VIDEO -> "মিসড ভিডিও কল"
                    endedCall.isIncoming -> "মিসড অডিও কল"
                    endedCall.callType == CallType.VIDEO -> "ভিডিও কল • উত্তর নেই"
                    else -> "অডিও কল • উত্তর নেই"
                }
                val callLogMsg = ChatMessage(
                    id = "call_msg_$closingCallId",
                    senderId = if (endedCall.isIncoming) targetUser.id else _currentUser.value.id,
                    senderName = if (endedCall.isIncoming) targetUser.fullName else _currentUser.value.fullName,
                    text = finalText,
                    timestamp = "এখন",
                    isFromMe = !endedCall.isIncoming,
                    status = if (wasAnswered) MessageDeliveryStatus.SEEN else MessageDeliveryStatus.DELIVERED,
                    callLogType = endedCall.callType,
                    callDurationSeconds = dur,
                    isMissedCall = !wasAnswered
                )
                upsertCallLogInConversation(conv, callLogMsg, syncCloud = true)
            }
        }
    }

    // Friend requests & 5000 friend auto-follow
    fun acceptFriendRequest(user: User) {
        _pendingRequests.value = _pendingRequests.value.filter { it.id != user.id }
        _friendsBadgeCount.value = (_friendsBadgeCount.value - 1).coerceAtLeast(0)
        _currentUser.value = _currentUser.value.copy(
            friendCount = _currentUser.value.friendCount + 1
        )
    }

    fun declineFriendRequest(user: User) {
        _pendingRequests.value = _pendingRequests.value.filter { it.id != user.id }
        _friendsBadgeCount.value = (_friendsBadgeCount.value - 1).coerceAtLeast(0)
    }

    fun sendFriendRequestOrFollow(user: User) {
        _suggestedUsers.value = _suggestedUsers.value.map { u ->
            if (u.id == user.id) {
                if (u.isMaxFriendsReached) {
                    // Over 5000: Auto-Follow mode!
                    u.copy(isFollowing = !u.isFollowing)
                } else {
                    // Under 5000: Friend request mode
                    u.copy(hasSentFriendRequest = !u.hasSentFriendRequest)
                }
            } else u
        }
    }

    // Groups & Pages
    fun createGroup(
        name: String,
        description: String,
        isPrivate: Boolean,
        coverUrl: String = "",
        initialAdmins: List<User> = emptyList(),
        initialModerators: List<User> = emptyList()
    ): SocialGroup {
        val defaultAdmins = if (initialAdmins.isEmpty()) {
            listOf(_currentUser.value)
        } else {
            (listOf(_currentUser.value) + initialAdmins).distinctBy { it.id }.take(5)
        }
        val defaultMods = initialModerators.filter { mod -> defaultAdmins.none { it.id == mod.id } }.take(15)

        val newGroup = SocialGroup(
            id = "grp_${UUID.randomUUID()}",
            name = name,
            description = description,
            coverUrl = coverUrl,
            memberCount = 1 + defaultAdmins.size + defaultMods.size,
            isPrivate = isPrivate,
            isJoined = true,
            creatorName = _currentUser.value.fullName,
            admins = defaultAdmins,
            moderators = defaultMods
        )
        _groups.value = listOf(newGroup) + _groups.value
        com.example.data.FirebaseManager.saveSocialGroup(newGroup)
        return newGroup
    }

    fun addAdminToGroup(groupId: String, user: User) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId && grp.admins.size < 5 && grp.admins.none { it.id == user.id }) {
                grp.copy(
                    admins = grp.admins + user,
                    moderators = grp.moderators.filter { it.id != user.id }
                )
            } else grp
        }
    }

    fun removeAdminFromGroup(groupId: String, userId: String) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId) {
                grp.copy(admins = grp.admins.filter { it.id != userId })
            } else grp
        }
    }

    fun addModeratorToGroup(groupId: String, user: User) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId && grp.moderators.size < 15 && grp.moderators.none { it.id == user.id } && grp.admins.none { it.id == user.id }) {
                grp.copy(moderators = grp.moderators + user)
            } else grp
        }
    }

    fun removeModeratorFromGroup(groupId: String, userId: String) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId) {
                grp.copy(moderators = grp.moderators.filter { it.id != userId })
            } else grp
        }
    }

    // Live App Download Count state (starts at real active user count)
    private val _totalAppDownloadCount = MutableStateFlow(1)
    val totalAppDownloadCount: StateFlow<Int> = _totalAppDownloadCount.asStateFlow()

    fun recordNewAppDownload() {
        _totalAppDownloadCount.value += 1
    }

    fun toggleJoinGroup(groupId: String) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId) grp.copy(isJoined = !grp.isJoined) else grp
        }
    }

    fun createPage(
        name: String,
        category: String,
        avatarUrl: String = "",
        coverUrl: String = ""
    ): SocialPage {
        val newPage = SocialPage(
            id = "page_${UUID.randomUUID()}",
            name = name,
            category = category.ifBlank { "Personal blog" },
            avatarUrl = avatarUrl,
            coverUrl = coverUrl,
            followerCount = 0,
            isLiked = true,
            creatorName = _currentUser.value.fullName
        )
        _pages.value = listOf(newPage) + _pages.value
        com.example.data.FirebaseManager.saveSocialPage(newPage)
        return newPage
    }

    fun toggleLikePage(pageId: String) {
        _pages.value = _pages.value.map { page ->
            if (page.id == pageId) page.copy(isLiked = !page.isLiked) else page
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == id) notif.copy(isRead = true) else notif
        }
        _notificationsBadgeCount.value = _notifications.value.count { !it.isRead }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _notificationsBadgeCount.value = 0
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
        _notificationsBadgeCount.value = _notifications.value.count { !it.isRead }
    }

    fun toggleNotificationRead(id: String) {
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == id) notif.copy(isRead = !notif.isRead) else notif
        }
        _notificationsBadgeCount.value = _notifications.value.count { !it.isRead }
    }

    fun acceptFriendRequestNotification(notifId: String) {
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == notifId) {
                notif.copy(isFriendAccepted = true, isRead = true)
            } else notif
        }
        val actor = _notifications.value.find { it.id == notifId }?.actor
        if (actor != null) {
            _pendingRequests.value = _pendingRequests.value.filter { it.id != actor.id }
        }
    }

    fun declineFriendRequestNotification(notifId: String) {
        deleteNotification(notifId)
    }

    fun joinGroupNotification(notifId: String) {
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == notifId) notif.copy(isGroupJoined = true, isRead = true) else notif
        }
    }

    fun declineGroupNotification(notifId: String) {
        deleteNotification(notifId)
    }

    fun wishHappyBirthdayNotification(notifId: String, actor: User) {
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == notifId) notif.copy(isRead = true) else notif
        }
        // Send a celebratory birthday greeting directly to this user's conversation!
        val existing = _conversations.value.find { it.friend.id == actor.id }
            ?: Conversation(
                id = "chat_bday_${actor.id}",
                friend = actor,
                lastMessage = "You: শুভ জন্মদিন প্রিয় বন্ধু! 🎂🎉 May Allah bless you!",
                lastTime = "Just now"
            )
        openChat(existing)
        sendMessage(conversationId = existing.id, text = "শুভ জন্মদিন প্রিয় বন্ধু! 🎂🎉 অনেক অনেক শুভকামনা ও দোয়া রইলো। Have an awesome year ahead!")
    }

    // Followers and Following State
    private val _followers = MutableStateFlow(SampleData.initialFollowers)
    val followers: StateFlow<List<FollowerUser>> = _followers.asStateFlow()

    private val _following = MutableStateFlow(SampleData.initialFollowing)
    val following: StateFlow<List<FollowerUser>> = _following.asStateFlow()

    fun removeFollower(id: String) {
        _followers.value = _followers.value.filter { it.id != id }
        _currentUser.value = _currentUser.value.copy(followerCount = _followers.value.size)
    }

    fun toggleFollowBack(id: String) {
        _followers.value = _followers.value.map {
            if (it.id == id) it.copy(isFollowingBack = !it.isFollowingBack) else it
        }
    }

    fun toggleFollowing(id: String) {
        _following.value = _following.value.map {
            if (it.id == id) it.copy(isFollowingBack = !it.isFollowingBack) else it
        }
        val count = _following.value.count { it.isFollowingBack }
        _currentUser.value = _currentUser.value.copy(followingCount = count)
    }

    fun updateProfileDetails(
        fullName: String,
        bio: String,
        category: String,
        workplace: String,
        livesIn: String,
        fromCity: String,
        school: String,
        college: String,
        instagram: String,
        relationshipStatus: String = _currentUser.value.relationshipStatus,
        joinedDate: String = _currentUser.value.joinedDate
    ) {
        _currentUser.value = _currentUser.value.copy(
            fullName = fullName.ifBlank { _currentUser.value.fullName },
            bio = bio,
            category = category,
            workplace = workplace,
            livesIn = livesIn,
            fromCity = fromCity,
            school = school,
            college = college,
            instagramHandle = instagram,
            relationshipStatus = relationshipStatus,
            joinedDate = joinedDate
        )
    }

    // Multi-Account / Switch Account Feature (populated from active signed-in users on this device)
    private val _userAccounts = MutableStateFlow<List<UserAccount>>(emptyList())
    val userAccounts: StateFlow<List<UserAccount>> = _userAccounts.asStateFlow()

    fun switchAccount(accountId: String) {
        val target = _userAccounts.value.find { it.id == accountId } ?: return
        _userAccounts.value = _userAccounts.value.map {
            it.copy(isActive = it.id == accountId)
        }
        val cleanHandle = target.handle.removePrefix("@")
        val matchedSaved = _savedEmailAccounts.value.firstOrNull { saved ->
            saved.id == target.id ||
                saved.username.equals(cleanHandle, ignoreCase = true) ||
                saved.email.substringBefore("@").equals(cleanHandle, ignoreCase = true)
        }
        val isVerifiedOwnerSwitch = matchedSaved != null &&
            matchedSaved.isOwner &&
            com.example.data.AuthSessionStore.isReservedOwnerEmail(matchedSaved.email)
        val resolvedEmail = matchedSaved?.email?.takeIf { it.isNotBlank() } ?: "$cleanHandle@fotofilm.app"

        setOwnerAuthenticated(isVerifiedOwnerSwitch)
        if (!isVerifiedOwnerSwitch) {
            closeRightManagerFida()
        }
        _isFidaVerified.value = isVerifiedOwnerSwitch
        _currentUser.value = if (isVerifiedOwnerSwitch) {
            SampleData.currentUser.copy(
                id = target.id,
                fullName = target.name,
                username = cleanHandle,
                email = resolvedEmail,
                avatarUrl = target.avatarUrl.ifBlank { SampleData.currentUser.avatarUrl },
                isVerified = true,
                isOnline = _isActiveStatusEnabled.value
            )
        } else {
            _currentUser.value.copy(
                id = target.id,
                fullName = target.name,
                username = cleanHandle,
                email = resolvedEmail,
                avatarUrl = target.avatarUrl,
                isVerified = false,
                isOnline = _isActiveStatusEnabled.value
            )
        }
        appContextRef?.let { ctx ->
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = _currentUser.value,
                isOwnerVerified = isVerifiedOwnerSwitch,
                authMethod = if (isVerifiedOwnerSwitch) "owner_verified" else "switch_account"
            )
        }
    }

    fun addNewAccount(
        name: String,
        handle: String,
        gender: String = "পুরুষ",
        avatarUrl: String = "",
        context: android.content.Context? = appContextRef
    ): Pair<Boolean, String?> {
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx

        val cleanName = name.trim().ifBlank { "New FotoFilm User" }
        val rawHandle = handle.trim().ifBlank {
            cleanName.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user_${System.currentTimeMillis() % 10000}" }
        }
        val cleanHandle = if (rawHandle.startsWith("@")) rawHandle else "@$rawHandle"
        val emailCandidate = "${cleanHandle.removePrefix("@").lowercase()}@fotofilm.app"
        val finalAvatar = avatarUrl.trim()

        // ১ ব্যক্তি সর্বোচ্চ ৩টি আইডি সীমা (Switch account এর নিচে ৩টির বেশি আইডি খুলতে পারবে না)
        val deviceTotal = maxOf(_userAccounts.value.size, countDistinctAccountsOnDevice(ctx, excludingEmail = emailCandidate))
        if (deviceTotal >= com.example.util.FakeIdDetector.MAX_ACCOUNTS_PER_PERSON) {
            val limitMsg = "⛔ ১ জন ব্যক্তি মোট ৩টির বেশি আইডি খুলতে পারবেন না! (৩/৩ আইডি সীমা পূর্ণ হয়েছে)"
            recordFakeIdViolationReport(
                context = ctx,
                attemptedName = cleanName,
                attemptedUsername = cleanHandle,
                attemptedEmail = emailCandidate,
                selectedGender = gender.trim(),
                detectedSignal = "Switch Account এ ৪র্থ আইডি খোলার চেষ্টা ($deviceTotal/৩ পূর্ণ)",
                violationType = "LIMIT_3_ACCOUNTS",
                violationDetails = limitMsg,
                attemptedAvatarUrl = finalAvatar
            )
            return false to limitMsg
        }

        val newAccId = "acc_${System.currentTimeMillis()}"
        val check = com.example.util.FakeIdDetector.validateRegistrationOrUpdate(
            context = ctx,
            candidateUserId = newAccId,
            candidateEmail = emailCandidate,
            fullName = cleanName,
            username = cleanHandle.removePrefix("@"),
            selectedGender = gender.trim(),
            avatarUrl = finalAvatar,
            existingUsers = _allAppUsers.value,
            existingPosts = _posts.value,
            savedEmailAccounts = _savedEmailAccounts.value,
            isOwnerVerified = false
        )
        if (!check.isAllowed) {
            val msg = check.reasonMessage ?: "⛔ ফেইক আইডি শনাক্ত হওয়ায় নতুন আইডি তৈরি ব্লক করা হয়েছে!"
            recordFakeIdViolationReport(
                context = ctx,
                attemptedName = cleanName,
                attemptedUsername = cleanHandle,
                attemptedEmail = emailCandidate,
                selectedGender = gender.trim(),
                detectedSignal = check.detectedSignal,
                violationType = check.violationType ?: "FAKE_ID_BLOCKED",
                violationDetails = msg,
                attemptedAvatarUrl = finalAvatar,
                matchedExistingUser = check.matchedOwnerName
            )
            return false to msg
        }

        if (finalAvatar.isNotBlank()) {
            com.example.util.FakeIdDetector.registerPhotoFingerprint(
                context = ctx,
                ownerId = newAccId,
                ownerName = cleanName,
                ownerEmail = emailCandidate,
                ownerGender = gender.trim(),
                avatarUrl = finalAvatar,
                precomputed = check.fingerprint
            )
        }

        val newAcc = UserAccount(
            id = newAccId,
            name = cleanName,
            handle = cleanHandle,
            avatarUrl = finalAvatar,
            isActive = false,
            notificationCount = 0
        )
        _userAccounts.value = _userAccounts.value + newAcc
        val newSavedUser = User(
            id = newAccId,
            fullName = cleanName,
            username = cleanHandle.removePrefix("@"),
            email = emailCandidate,
            avatarUrl = finalAvatar,
            gender = gender.trim(),
            bio = "FotoFilm সদস্য ✨",
            isVerified = false,
            isOnline = true
        )
        recordSavedAccount(
            context = ctx,
            user = newSavedUser,
            isFirebaseLinked = false,
            isOwner = false
        )
        return true to null
    }

    // ==========================================
    // Fida AI Assistant (Replaces Meta AI)
    // ==========================================
    private val _showFidaAIDialog = MutableStateFlow(false)
    val showFidaAIDialog: StateFlow<Boolean> = _showFidaAIDialog.asStateFlow()

    private val _fidaAIMessages = MutableStateFlow(
        listOf(
            FidaChatMessage(
                sender = "FIDA_AI",
                text = "আসসালামু আলাইকুম ও শুভকামনা! 🌸\nআমি ফিদা এআই (Fida AI)—আপনার FotoFilm ব্যক্তিগত স্মার্ট সহকারী। আপনি আমাকে যেকোনো বিষয়, তথ্য, পড়াশোনা, প্রযুক্তি, সাহিত্য বা দৈনন্দিন জীবনের প্রশ্ন করতে পারেন। আমি সবসময় ভালোবাসা, সম্মান ও আন্তরিকতার সাথে দ্রুত উত্তর দিতে প্রস্তুত। বলুন, আজ আপনাকে কিভাবে সাহায্য করতে পারি? 😊",
                timestamp = "এখন"
            )
        )
    )
    val fidaAIMessages: StateFlow<List<FidaChatMessage>> = _fidaAIMessages.asStateFlow()

    private val _isFidaAITyping = MutableStateFlow(false)
    val isFidaAITyping: StateFlow<Boolean> = _isFidaAITyping.asStateFlow()

    fun openFidaAI() {
        _showFidaAIDialog.value = true
    }

    fun closeFidaAI() {
        _showFidaAIDialog.value = false
    }

    fun sendFidaAIMessage(userText: String) {
        if (userText.isBlank()) return
        if (!requireInternetConnection()) return
        val userMsg = FidaChatMessage(
            sender = "USER",
            text = userText.trim(),
            timestamp = "এখন"
        )
        _fidaAIMessages.value = _fidaAIMessages.value + userMsg
        _isFidaAITyping.value = true

        viewModelScope.launch {
            delay(700) // Realistic conversational latency
            val aiResponseText = generateFidaAIResponse(userText.trim())
            val aiMsg = FidaChatMessage(
                sender = "FIDA_AI",
                text = aiResponseText,
                timestamp = "এখন"
            )
            _fidaAIMessages.value = _fidaAIMessages.value + aiMsg
            _isFidaAITyping.value = false
        }
    }

    private fun generateFidaAIResponse(prompt: String): String {
        if (com.example.data.FidaAIPersistenceManager.isRightManagerQuery(prompt)) {
            return com.example.data.FidaAIPersistenceManager.RIGHT_MANAGER_ANSWER
        }
        if (com.example.data.FidaAIPersistenceManager.isAppCreatorQuery(prompt)) {
            return com.example.data.FidaAIPersistenceManager.APP_CREATOR_ANSWER
        }

        val lower = prompt.lowercase()
        return when {
            lower.contains("কেমন") || lower.contains("how are you") -> {
                "আলহামদুলিল্লাহ, আপনার দোয়ায় ও ভালোবাসায় আমি অনেক ভালো আছি প্রিয় ভাইয়া! 🌸 আশা করি আপনিও সুস্থ ও সুন্দর আছেন। আপনার দিনটি আনন্দময় হোক। আপনি কেমন আছেন?"
            }
            lower.contains("fotofilm") || lower.contains("facefeed") || lower.contains("অ্যাপ") || lower.contains("ফিচার") -> {
                "FotoFilm হলো একটি আধুনিক ও নিরাপদ সামাজিক যোগাযোগ মাধ্যম 🌟। এখানে আপনি ফটো, শর্ট ভিডিও, লং ভিডিও, স্টোরি, রঙিন টেক্সট স্ট্যাটাস এবং লাইভ স্ট্রিমিং করতে পারবেন। এছাড়া ৩ ডট মেনু থেকে 'Switch account' দিয়ে একাধিক আইডি ব্যবহার করা এবং 'Fida Verified' এর মাধ্যমে সবুজ ভেরিফাইড টিক পাওয়ার সুবিধাও রয়েছে!"
            }
            lower.contains("বাংলাদেশ") || lower.contains("bangladesh") -> {
                "বাংলাদেশ আমাদের অহংকার ও ভালোবাসার সবুজ মাতৃভূমি 🇧🇩। ১৯৭১ সালের মহান মুক্তিযুদ্ধের মাধ্যমে লাখো শহীদের আত্মত্যাগে আমরা পেয়েছি লাল-সবুজের পতাকা। এর প্রাকৃতিক রূপ ও মানুষের আন্তরিকতা বিশ্বের অন্যতম সুন্দর বিষয়। আপনি বাংলাদেশ নিয়ে কোনো নির্দিষ্ট ইতিহাস বা তথ্য জানতে চান?"
            }
            lower.contains("স্ট্যাটাস") || lower.contains("ক্যাপশন") || lower.contains("status") -> {
                "আপনার জন্য একটি হৃদয়স্পর্শী ও পজিটিভ স্ট্যাটাস লিখে দিচ্ছি:\n\n\"জীবনের প্রতিটি ধাপে ধৈর্য আর সততাই আসল শক্তি। মানুষের সাথে ভালো ব্যবহার করুন, কারণ আপনার একটি সুন্দর কথাই হয়তো কারো মন ভালো করে দিতে পারে। শুভকামনা সবার জন্য! 🌿✨ #FotoFilm #PositiveVibes\""
            }
            lower.contains("ভালোবাসা") || lower.contains("ব্যবহার") || lower.contains("উপদেশ") || lower.contains("কষ্ট") -> {
                "অত্যন্ত সুন্দর একটি কথা বলেছেন প্রিয় ভাইয়া 💖। মানুষের জীবনে সবচেয়ে দামি অলংকার হলো তার নম্রতা ও মিষ্টি ব্যবহার। কাউকে কোনোদিন কথায় বা কাজে কষ্ট না দিয়ে ক্ষমা ও ভালোবাসার হাত বাড়িয়ে দেওয়াই প্রকৃত মহত্ত্ব। পবিত্র ধর্মেও বলা হয়েছে—'একটি মিষ্টি কথা ও সুন্দর হাসিও সদকার সমতুল্য'।"
            }
            lower.contains("আইডি") || lower.contains("switch") || lower.contains("একাউন্ট") -> {
                "আপনি ৩ ডট মেনুতে গিয়ে 'Switch account' অপশনে চাপ দিলে আপনার সবগুলো তৈরি করা আইডি দেখতে পাবেন। এছাড়া নিচের '+ Add account' অপশনে চাপ দিয়ে খুব সহজেই আরেকটি নতুন অ্যাকাউন্ট যোগ বা সুইচ করতে পারবেন!"
            }
            lower.contains("সবুজ") || lower.contains("verified") || lower.contains("ভেরিফাইড") -> {
                "FotoFilm-এ এখন সম্মানজনক 'সবুজ ও হলুদ ভেরিফাইড ব্যাজ (Fida Verified Badge)' যুক্ত করা হয়েছে ✅! ৩ ডট মেনু থেকে 'Fida Verified' অপশনে গিয়ে আপনি ভেরিফিকেশন সক্রিয় করতে পারেন।"
            }
            else -> {
                "প্রিয় সুহৃদ, আপনার প্রশ্নটি অত্যন্ত সুন্দর ও গুরুত্বপূর্ণ 🌸। \"$prompt\" সম্পর্কে জেনে রাখুন: জীবনে প্রতিটি জ্ঞান ও তথ্যই আমাদের সামনে এগিয়ে নিয়ে যায়। বিশ্বের যেকোনো প্রশ্ন, পড়াশোনা, অংক, অনুবাদ বা যেকোনো বিষয় নিয়ে জিজ্ঞাসা করুন, FotoFilm ফিদা এআই সর্বদা আন্তরিকতার সাথে সমাধান দিতে প্রস্তুত আছে!"
            }
        }
    }

    // ==========================================
    // Fida Verified & Right Manager Fida System
    // ==========================================
    private val _showFidaVerifiedDialog = MutableStateFlow(false)
    val showFidaVerifiedDialog: StateFlow<Boolean> = _showFidaVerifiedDialog.asStateFlow()

    private val _isFidaVerified = MutableStateFlow(false)
    val isFidaVerified: StateFlow<Boolean> = _isFidaVerified.asStateFlow()

    // 50,000 Free Verified Quota (50K) for Niyon Islam Sohag only
    val freeVerifiedQuotaTotal: Int = 50000
    private val _freeVerifiedQuotaRemaining = MutableStateFlow(50000)
    val freeVerifiedQuotaRemaining: StateFlow<Int> = _freeVerifiedQuotaRemaining.asStateFlow()

    private val _freeVerifiedGrantedUsers = MutableStateFlow<Set<String>>(emptySet())
    val freeVerifiedGrantedUsers: StateFlow<Set<String>> = _freeVerifiedGrantedUsers.asStateFlow()

    // Option 24: ইউজার অ্যাড ভিউ ও স্কিপ ট্র্যাকার (কার সামনে কয়েটা দৈনিক এড গেলো, কে কয়টা দেখলো ও স্কিপ করলো)
    private val _userAdAnalytics = MutableStateFlow<List<UserAdAnalytics>>(SampleData.initialUserAdAnalytics)
    val userAdAnalytics: StateFlow<List<UserAdAnalytics>> = _userAdAnalytics.asStateFlow()

    fun recordUserAdEvent(userId: String, adType: String, wasSkipped: Boolean) {
        _userAdAnalytics.value = _userAdAnalytics.value.map { user ->
            if (user.userId == userId) {
                val log = if (wasSkipped) "$adType স্কিপ বোতাম চেপে কেটে দিয়েছে" else "$adType সফলভাবে সম্পূর্ণ দেখেছে"
                user.copy(
                    totalAdsSeen = user.totalAdsSeen + 1,
                    dailyAdsServed = user.dailyAdsServed + 1,
                    completedAdsWatched = if (!wasSkipped) user.completedAdsWatched + 1 else user.completedAdsWatched,
                    skippedAdsCount = if (wasSkipped) user.skippedAdsCount + 1 else user.skippedAdsCount,
                    bannerAdsViewed = if (adType.contains("Banner", true)) user.bannerAdsViewed + 1 else user.bannerAdsViewed,
                    interstitialAdsViewed = if (adType.contains("Interstitial", true)) user.interstitialAdsViewed + 1 else user.interstitialAdsViewed,
                    lastAdSeenTime = "এখনই",
                    recentAdLogs = listOf(log) + user.recentAdLogs.take(5)
                )
            } else user
        }
    }

    // Option 25: ১ কোটি ইউজারের মেসেনজার খারাপ ছবি, ভিডিও, ভয়েস ও আপত্তিকর চ্যাট নজরদারি রেকর্ড
    private val _flaggedMessengerAudits = MutableStateFlow<List<FlaggedMessengerAudit>>(SampleData.initialFlaggedMessengerAudits)
    val flaggedMessengerAudits: StateFlow<List<FlaggedMessengerAudit>> = _flaggedMessengerAudits.asStateFlow()

    fun deleteFlaggedMessage(id: String) {
        _flaggedMessengerAudits.value = _flaggedMessengerAudits.value.map { item ->
            if (item.id == id) item.copy(status = FlaggedAuditStatus.DELETED) else item
        }
    }

    fun banFlaggedSender(id: String) {
        val target = _flaggedMessengerAudits.value.find { it.id == id }
        if (target != null) {
            _flaggedMessengerAudits.value = _flaggedMessengerAudits.value.map { item ->
                if (item.id == id) item.copy(status = FlaggedAuditStatus.SENDER_BANNED) else item
            }
            _allAppUsers.value = _allAppUsers.value.map { u ->
                if (u.id == target.senderUser.id) u.copy(isAccountActive = false) else u
            }
        }
    }

    fun sendWarningToFlaggedSender(id: String) {
        _flaggedMessengerAudits.value = _flaggedMessengerAudits.value.map { item ->
            if (item.id == id) item.copy(status = FlaggedAuditStatus.WARNING_SENT) else item
        }
    }

    // Right Manager Fida Master PIN Lock (তালা লাগানো 🔒 - শুধুমাত্র মালিকের গোপন পাসওয়ার্ড)
    private val _rightManagerPin = MutableStateFlow("20219091")
    val rightManagerPin: StateFlow<String> = _rightManagerPin.asStateFlow()

    private val _isRightManagerUnlocked = MutableStateFlow(false)
    val isRightManagerUnlocked: StateFlow<Boolean> = _isRightManagerUnlocked.asStateFlow()

    fun unlockRightManager(enteredPin: String): Boolean {
        if (isCurrentUserNiyonIslamSohag() && enteredPin == _rightManagerPin.value) {
            _isRightManagerUnlocked.value = true
            return true
        }
        return false
    }

    fun lockRightManager() {
        _isRightManagerUnlocked.value = false
    }

    fun changeRightManagerPin(oldPin: String, newPin: String): Boolean {
        if (isCurrentUserNiyonIslamSohag() &&
            oldPin == _rightManagerPin.value &&
            newPin.length in 4..8 &&
            newPin.all { it.isDigit() }
        ) {
            _rightManagerPin.value = newPin
            return true
        }
        return false
    }

    // Option 14: ফ্রী ফিদা ভেরিফাই বিতরণ ইতিহাস (১ জনকে কতো বার দিছি, কোন মাসে দিছি, তারিখ সহ)
    private val _freeVerifiedGrantRecords = MutableStateFlow<List<FreeVerifiedGrantRecord>>(SampleData.initialFreeVerifiedGrantRecords)
    val freeVerifiedGrantRecords: StateFlow<List<FreeVerifiedGrantRecord>> = _freeVerifiedGrantRecords.asStateFlow()

    // Option 15: আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (তারিখ ও প্রোফাইল আইকন সহ)
    private val _userVerificationAuditEntries = MutableStateFlow<List<UserVerificationAuditEntry>>(SampleData.initialUserVerificationAuditEntries)
    val userVerificationAuditEntries: StateFlow<List<UserVerificationAuditEntry>> = _userVerificationAuditEntries.asStateFlow()

    // Option 16: নির্দিষ্ট মেয়াদে আইডি সার্চপেন্ড (১০টি মেয়াদ, অন/অফ, কেনছেল, প্রোফাইল ও তারিখ)
    private val _timedSuspensionRecords = MutableStateFlow<List<UserSuspensionRecord>>(SampleData.initialTimedSuspensions)
    val timedSuspensionRecords: StateFlow<List<UserSuspensionRecord>> = _timedSuspensionRecords.asStateFlow()

    // Right Manager Fida Admin Panel (Exclusively for Niyon Islam Sohag)
    private val _showRightManagerFidaDialog = MutableStateFlow(false)
    val showRightManagerFidaDialog: StateFlow<Boolean> = _showRightManagerFidaDialog.asStateFlow()

    private val _rightManagerInitialTab = MutableStateFlow(0)
    val rightManagerInitialTab: StateFlow<Int> = _rightManagerInitialTab.asStateFlow()

    // All App Registered Users Directory
    private val _allAppUsers = MutableStateFlow<List<User>>(SampleData.allAppUsersList)
    val allAppUsers: StateFlow<List<User>> = _allAppUsers.asStateFlow()

    // NID Card Documents State (empty until user uploads real NID)
    private val _nidFrontUri = MutableStateFlow<String?>(null)
    val nidFrontUri: StateFlow<String?> = _nidFrontUri.asStateFlow()

    private val _nidBackUri = MutableStateFlow<String?>(null)
    val nidBackUri: StateFlow<String?> = _nidBackUri.asStateFlow()

    private val _isNidSubmitted = MutableStateFlow(false)
    val isNidSubmitted: StateFlow<Boolean> = _isNidSubmitted.asStateFlow()

    private val _nidApplicantName = MutableStateFlow("")
    val nidApplicantName: StateFlow<String> = _nidApplicantName.asStateFlow()

    private val _nidNumber = MutableStateFlow("")
    val nidNumber: StateFlow<String> = _nidNumber.asStateFlow()

    // 12 Months Subscription State (Paid months at 500 BDT each)
    private val _paidMonths = MutableStateFlow<Set<String>>(setOf("January", "February", "March", "September"))
    val paidMonths: StateFlow<Set<String>> = _paidMonths.asStateFlow()

    // Payment Transaction Success Event message
    private val _paymentSuccessMessage = MutableStateFlow<String?>(null)
    val paymentSuccessMessage: StateFlow<String?> = _paymentSuccessMessage.asStateFlow()

    fun isCurrentUserNiyonIslamSohag(): Boolean {
        val ownerAuth = _isOwnerSession.value || isCurrentOwnerAuthenticated
        if (!_isLoggedIn.value || !ownerAuth) return false
        return com.example.data.AuthSessionStore.isReservedOwnerEmail(_currentUser.value.email)
    }

    fun verifyAndActivateOwnerSession(
        secretOrPin: String,
        context: android.content.Context? = appContextRef
    ): Boolean {
        val trimmed = secretOrPin.trim()
        if (!com.example.data.AuthSessionStore.verifyOwnerSecret(trimmed, _rightManagerPin.value)) {
            return false
        }
        val ctx = (context ?: appContextRef)?.applicationContext
        if (ctx != null) appContextRef = ctx

        val cur = _currentUser.value
        val resolvedEmail = if (com.example.data.AuthSessionStore.isReservedOwnerEmail(cur.email)) {
            cur.email
        } else {
            SampleData.currentUser.email
        }
        val ownerUser = SampleData.currentUser.copy(
            id = if (cur.id.isNotBlank() && cur.id != "guest_pending") cur.id else SampleData.currentUser.id,
            email = resolvedEmail,
            avatarUrl = cur.avatarUrl.ifBlank { SampleData.currentUser.avatarUrl },
            isVerified = true,
            isOnline = true
        )
        _currentUser.value = ownerUser
        setOwnerAuthenticated(true)
        _isFidaVerified.value = true
        _isLoggedIn.value = true
        if (trimmed == _rightManagerPin.value) {
            _isRightManagerUnlocked.value = true
        }
        recordSavedAccount(
            context = ctx,
            user = ownerUser,
            isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
            isOwner = true
        )
        if (ctx != null) {
            com.example.data.AuthSessionStore.saveActiveSession(
                context = ctx,
                user = ownerUser,
                isOwnerVerified = true,
                authMethod = "owner_pin"
            )
        }
        syncUserAccountsForCurrentUser(ownerUser)
        _rightManagerInitialTab.value = 0
        _showRightManagerFidaDialog.value = true
        return true
    }

    fun isUserNiyonIslamSohag(user: User?): Boolean {
        if (user == null) return false
        val email = user.email.trim().lowercase()
        return user.id == "user_me" ||
            com.example.data.AuthSessionStore.isReservedOwnerEmail(email)
    }

    /**
     * Re-applies every persistent OFF lock created by Niyon Islam Sohag in Right Manager Fida
     * so nothing can automatically turn back ON across restarts, tab switches, or cloud/Room syncs.
     */
    fun applyNiyonOwnerLocksToState() {
        val store = com.example.data.NiyonOwnerControlStore

        // 1. User accounts turned OFF
        val disabledUsers = store.disabledUserIds.value
        if (disabledUsers.isNotEmpty()) {
            _allAppUsers.value = _allAppUsers.value.map { user ->
                if (user.id in disabledUsers && user.isAccountActive) {
                    user.copy(
                        isAccountActive = false,
                        suspensionReason = user.suspensionReason ?: "Niyon Islam Sohag দ্বারা লক-বন্ধ করা হয়েছে 🔒"
                    )
                } else user
            }
            if (_currentUser.value.id in disabledUsers && _currentUser.value.isAccountActive) {
                _currentUser.value = _currentUser.value.copy(
                    isAccountActive = false,
                    suspensionReason = _currentUser.value.suspensionReason ?: "Niyon Islam Sohag দ্বারা লক-বন্ধ করা হয়েছে 🔒"
                )
            }
        }

        // 2. Languages turned OFF
        val disabledLangs = store.disabledLanguages.value
        if (disabledLangs.isNotEmpty()) {
            val updatedLangs = _languageStatus.value.mapValues { (lang, currentOn) ->
                if (lang.name in disabledLangs) false else currentOn
            }
            _languageStatus.value = updatedLangs
            if (_currentLanguage.value.name in disabledLangs) {
                val fallback = updatedLangs.entries.firstOrNull { it.value }?.key ?: AppLanguage.BENGALI
                _currentLanguage.value = fallback
            }
        }

        // 3. Boost Transactions paused/turned OFF or activated
        val pausedTxs = store.pausedBoostTxIds.value
        val activeTxs = store.activeBoostTxIds.value
        if (pausedTxs.isNotEmpty() || activeTxs.isNotEmpty()) {
            _boostTransactions.value = _boostTransactions.value.map { tx ->
                when {
                    tx.id in pausedTxs -> tx.copy(status = "PAUSED")
                    tx.id in activeTxs -> tx.copy(status = "ACTIVE")
                    else -> tx
                }
            }
        }

        // 4. Blaze Card Mode
        _isBlazeCardModeEnabled.value = store.isBlazeCardEnabled.value

        // 5. Pages turned OFF
        val disabledPages = store.disabledPageIds.value
        if (disabledPages.isNotEmpty()) {
            _pages.value = _pages.value.map { page ->
                if (page.id in disabledPages && page.isActive) page.copy(isActive = false) else page
            }
        }

        // 6. Groups & Hangout Groups turned OFF
        val disabledGroups = store.disabledGroupIds.value
        if (disabledGroups.isNotEmpty()) {
            _groups.value = _groups.value.map { group ->
                if (group.id in disabledGroups && group.isActive) group.copy(isActive = false) else group
            }
        }

        // 7. Hangout Preset Rooms turned OFF
        val disabledRooms = store.disabledHangoutRoomKeys.value
        if (disabledRooms.isNotEmpty()) {
            _hangoutPresetRooms.value = _hangoutPresetRooms.value.map { room ->
                if (room.roomKey in disabledRooms && room.isActive) room.copy(isActive = false) else room
            }
        }

        // 8. Timed Suspensions turned OFF
        val pausedSuspensions = store.pausedTimedSuspensionIds.value
        if (pausedSuspensions.isNotEmpty()) {
            _timedSuspensionRecords.value = _timedSuspensionRecords.value.map { rec ->
                if (rec.id in pausedSuspensions && rec.isSuspendedActive) rec.copy(isSuspendedActive = false) else rec
            }
        }

        // 9. Post Boosts turned OFF
        val disabledPostBoosts = store.disabledPostBoostIds.value
        if (disabledPostBoosts.isNotEmpty()) {
            _posts.value = _posts.value.map { post ->
                if (post.id in disabledPostBoosts && (post.isBoostActive || post.isBoosted)) {
                    post.copy(isBoostActive = false, isBoosted = false)
                } else post
            }
        }
    }

    fun openFidaVerified() {
        _showFidaVerifiedDialog.value = true
    }

    fun closeFidaVerified() {
        _showFidaVerifiedDialog.value = false
    }

    fun openRightManagerFida(tab: Int = 0) {
        if (isCurrentUserNiyonIslamSohag()) {
            _rightManagerInitialTab.value = tab
            _showRightManagerFidaDialog.value = true
        }
    }

    fun closeRightManagerFida() {
        _showRightManagerFidaDialog.value = false
        _isRightManagerUnlocked.value = false
    }

    // App Download & Chrome/Google Web Access Hub
    private val _showAppDownloadWebDialog = MutableStateFlow(false)
    val showAppDownloadWebDialog: StateFlow<Boolean> = _showAppDownloadWebDialog.asStateFlow()

    fun openAppDownloadWebHub() {
        _showAppDownloadWebDialog.value = true
    }

    fun closeAppDownloadWebHub() {
        _showAppDownloadWebDialog.value = false
    }

    fun toggleFidaVerified() {
        _isFidaVerified.value = !_isFidaVerified.value
        _currentUser.value = _currentUser.value.copy(isVerified = _isFidaVerified.value)
    }

    // Toggle user account active/suspended by Niyon Islam Sohag
    fun toggleUserAccountStatus(userId: String, active: Boolean, reason: String? = null) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setUserActive(userId, active, isOwner = true)
        _allAppUsers.value = _allAppUsers.value.map { user ->
            if (user.id == userId) {
                user.copy(
                    isAccountActive = active,
                    suspensionReason = if (!active) (reason ?: "Suspended by Administrator Niyon Islam Sohag") else null
                )
            } else user
        }
        if (userId == _currentUser.value.id) {
            _currentUser.value = _currentUser.value.copy(
                isAccountActive = active,
                suspensionReason = if (!active) reason else null
            )
        }
    }

    // Grant Fida Verified for free from the 3,000 quota
    fun grantFreeVerified(userId: String) {
        if (_freeVerifiedQuotaRemaining.value <= 0) return
        _freeVerifiedQuotaRemaining.value = _freeVerifiedQuotaRemaining.value - 1
        _freeVerifiedGrantedUsers.value = _freeVerifiedGrantedUsers.value + userId

        val targetUser = _allAppUsers.value.find { it.id == userId }
        val targetName = targetUser?.fullName ?: "ইউজার"
        val targetUsername = targetUser?.username ?: targetName.lowercase().replace(" ", "_")
        val targetAvatar = targetUser?.avatarUrl.orEmpty()

        // Update Option 14: ফ্রী ফিদা ভেরিফাই ১ জনকে কতো বার দিছি কোন মাসে দিছি তারিখ সহ
        val existingGrant = _freeVerifiedGrantRecords.value.find { it.recipientUserId == userId }
        val newTimes = (existingGrant?.grantCountTimes ?: 0) + 1
        val newDetail = GrantInstanceDetail(
            instanceNumber = newTimes,
            date = "০৫ অক্টোবর ২০২৬",
            month = "অক্টোবর ২০২৬",
            time = "১০:১৫ AM",
            note = "৫০,০০০ (50k) কোটা থেকে প্রধান প্রতিষ্ঠাতা কর্তৃক অনুমোদিত"
        )
        val updatedGrant = if (existingGrant != null) {
            existingGrant.copy(
                grantCountTimes = newTimes,
                grantMonth = "অক্টোবর ২০২৬, ${existingGrant.grantMonth}",
                grantDateFormatted = "০৫ অক্টোবর ২০২৬, সকাল ১০:১৫ AM",
                historyInstances = listOf(newDetail) + existingGrant.historyInstances,
                quotaDeducted = existingGrant.quotaDeducted + 1
            )
        } else {
            FreeVerifiedGrantRecord(
                id = "grant_${UUID.randomUUID().toString().take(6)}",
                recipientUserId = userId,
                recipientName = targetName,
                recipientUsername = targetUsername,
                recipientAvatar = targetAvatar,
                grantCountTimes = 1,
                grantMonth = "অক্টোবর ২০২৬",
                grantDateFormatted = "০৫ অক্টোবর ২০২৬, সকাল ১০:১৫ AM",
                historyInstances = listOf(newDetail),
                quotaDeducted = 1
            )
        }
        _freeVerifiedGrantRecords.value = listOf(updatedGrant) + _freeVerifiedGrantRecords.value.filter { it.recipientUserId != userId }

        // Update Option 15: আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে তারিখ ও প্রোফাইল আইকন সহ
        val existingAudit = _userVerificationAuditEntries.value.find { it.userId == userId }
        val totalTaken = (existingAudit?.totalTimesVerifiedTaken ?: 0) + 1
        val newAuditLog = VerificationInstanceRecord(
            instanceNumber = totalTaken,
            date = "০৫ অক্টোবর ২০২৬",
            month = "অক্টোবর ২০২৬",
            time = "১০:১৫ AM",
            source = "প্রধান প্রতিষ্ঠাতা ও ওনার নিয়ন ইসলাম সোহাগ",
            method = "ফ্রী কোটা অনুমোদন"
        )
        val updatedAudit = if (existingAudit != null) {
            existingAudit.copy(
                totalTimesVerifiedTaken = totalTaken,
                currentBadgeStatus = "সক্রিয় ভেরিফাইড ✅",
                historyInstances = listOf(newAuditLog) + existingAudit.historyInstances
            )
        } else {
            UserVerificationAuditEntry(
                id = "audit_${UUID.randomUUID().toString().take(6)}",
                userId = userId,
                userName = targetName,
                userUsername = targetUsername,
                userAvatar = targetAvatar,
                totalTimesVerifiedTaken = 1,
                currentBadgeStatus = "সক্রিয় ভেরিফাইড ✅",
                historyInstances = listOf(newAuditLog)
            )
        }
        _userVerificationAuditEntries.value = listOf(updatedAudit) + _userVerificationAuditEntries.value.filter { it.userId != userId }

        _allAppUsers.value = _allAppUsers.value.map { user ->
            if (user.id == userId) {
                user.copy(isVerified = true, isFidaVerifiedFreeGranted = true)
            } else user
        }
        if (userId == _currentUser.value.id) {
            _isFidaVerified.value = true
            _currentUser.value = _currentUser.value.copy(isVerified = true, isFidaVerifiedFreeGranted = true)
        }
    }

    // Post Action Helpers for 3-Dot Menus
    fun deletePost(postId: String) {
        synchronized(hiddenPostIdsCache) { hiddenPostIdsCache.add(postId) }
        _posts.value = _posts.value.filter { it.id != postId }
        _savedPosts.value = _savedPosts.value.filter { it.id != postId }
        localRepo?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) { repo.markPostHidden(postId) }
        }
        com.example.data.FirebaseManager.deletePostCloud(postId)
    }

    fun pinPost(postId: String) {
        val target = _posts.value.find { it.id == postId } ?: return
        _posts.value = listOf(target) + _posts.value.filter { it.id != postId }
        persistPostToRoom(target, isPinned = true)
    }

    fun movePostToArchive(postId: String) {
        synchronized(hiddenPostIdsCache) { hiddenPostIdsCache.add(postId) }
        _posts.value = _posts.value.filter { it.id != postId }
        localRepo?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) { repo.markPostHidden(postId) }
        }
    }

    fun movePostToTrash(postId: String) {
        synchronized(hiddenPostIdsCache) { hiddenPostIdsCache.add(postId) }
        _posts.value = _posts.value.filter { it.id != postId }
        localRepo?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) { repo.markPostHidden(postId) }
        }
    }

    fun editPostText(postId: String, newText: String) {
        var editedPostRef: Post? = null
        _posts.value = _posts.value.map {
            if (it.id == postId) {
                val upd = it.copy(text = newText)
                editedPostRef = upd
                upd
            } else it
        }
        editedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    // Revoke Verified status (returns quota if user was given free verification)
    fun revokeVerified(userId: String) {
        if (_freeVerifiedGrantedUsers.value.contains(userId)) {
            _freeVerifiedGrantedUsers.value = _freeVerifiedGrantedUsers.value - userId
            _freeVerifiedQuotaRemaining.value = (_freeVerifiedQuotaRemaining.value + 1).coerceAtMost(20000)
        }
        _allAppUsers.value = _allAppUsers.value.map { user ->
            if (user.id == userId) {
                user.copy(isVerified = false, isFidaVerifiedFreeGranted = false)
            } else user
        }
        if (userId == _currentUser.value.id) {
            _isFidaVerified.value = false
            _currentUser.value = _currentUser.value.copy(isVerified = false, isFidaVerifiedFreeGranted = false)
        }
    }

    // Instant Search & Suspend user for any offense against Niyon Islam Sohag
    fun suspendUserForOffense(userId: String, reason: String) {
        toggleUserAccountStatus(userId, active = false, reason = reason)
    }

    fun unsuspendUser(userId: String) {
        toggleUserAccountStatus(userId, active = true, reason = null)
    }

    // Option 16: নির্দিষ্ট মেয়াদে আইডি সার্চপেন্ড পরিচালনা (১০টি অপশন, অন/অফ, কেনছেল, প্রোফাইল ও তারিখ)
    fun addTimedSuspension(
        userId: String,
        durationIndex: Int,
        durationText: String,
        reasonNote: String
    ) {
        if (!isCurrentUserNiyonIslamSohag()) return
        val targetUser = _allAppUsers.value.find { it.id == userId } ?: return
        com.example.data.NiyonOwnerControlStore.setUserActive(userId, false, isOwner = true)
        val nowFormatted = "১০ অক্টোবর ২০২৬, দুপুর ০১:৩০ PM"
        val newRecord = UserSuspensionRecord(
            id = "susp_${System.currentTimeMillis()}",
            userId = targetUser.id,
            userName = targetUser.fullName,
            userUsername = targetUser.username,
            userAvatar = targetUser.avatarUrl,
            durationText = durationText,
            durationIndex = durationIndex,
            reasonNote = reasonNote.ifBlank { "নিয়ম লঙ্ঘন ও আপত্তিকর কার্যকলাপের জন্য" },
            suspendedDateFormatted = nowFormatted,
            isSuspendedActive = true
        )
        _timedSuspensionRecords.value = listOf(newRecord) + _timedSuspensionRecords.value.filter { it.userId != userId }
        _allAppUsers.value = _allAppUsers.value.map {
            if (it.id == userId) it.copy(
                isAccountActive = false,
                suspensionReason = reasonNote,
                suspensionDuration = durationText,
                suspensionDate = nowFormatted
            ) else it
        }
        if (userId == _currentUser.value.id) {
            _currentUser.value = _currentUser.value.copy(
                isAccountActive = false,
                suspensionReason = reasonNote
            )
        }
    }

    fun toggleTimedSuspensionActive(recordId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        val targetRecord = _timedSuspensionRecords.value.find { it.id == recordId } ?: return
        val newActiveState = !targetRecord.isSuspendedActive
        com.example.data.NiyonOwnerControlStore.setTimedSuspensionActive(recordId, newActiveState, isOwner = true)
        com.example.data.NiyonOwnerControlStore.setUserActive(targetRecord.userId, !newActiveState, isOwner = true)
        _timedSuspensionRecords.value = _timedSuspensionRecords.value.map {
            if (it.id == recordId) it.copy(isSuspendedActive = newActiveState) else it
        }
        _allAppUsers.value = _allAppUsers.value.map {
            if (it.id == targetRecord.userId) it.copy(isAccountActive = !newActiveState) else it
        }
        if (targetRecord.userId == _currentUser.value.id) {
            _currentUser.value = _currentUser.value.copy(isAccountActive = !newActiveState)
        }
    }

    fun cancelTimedSuspension(recordId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        val targetRecord = _timedSuspensionRecords.value.find { it.id == recordId }
        if (targetRecord != null) {
            com.example.data.NiyonOwnerControlStore.setTimedSuspensionActive(recordId, false, isOwner = true)
            com.example.data.NiyonOwnerControlStore.setUserActive(targetRecord.userId, true, isOwner = true)
            _timedSuspensionRecords.value = _timedSuspensionRecords.value.filter { it.id != recordId }
            _allAppUsers.value = _allAppUsers.value.map {
                if (it.id == targetRecord.userId) it.copy(
                    isAccountActive = true,
                    suspensionReason = null,
                    suspensionDuration = null,
                    suspensionDate = null
                ) else it
            }
            if (targetRecord.userId == _currentUser.value.id) {
                _currentUser.value = _currentUser.value.copy(
                    isAccountActive = true,
                    suspensionReason = null
                )
            }
        }
    }

    // Fida Verified Review Applications Queue (real user submissions only)
    private val _verifiedApplications = MutableStateFlow<List<com.example.model.FidaVerifiedApplication>>(emptyList())
    val verifiedApplications: StateFlow<List<com.example.model.FidaVerifiedApplication>> = _verifiedApplications.asStateFlow()

    fun approveVerifiedApplication(appId: String) {
        val app = _verifiedApplications.value.find { it.id == appId } ?: return
        _verifiedApplications.value = _verifiedApplications.value.map {
            if (it.id == appId) it.copy(status = "APPROVED") else it
        }
        _allAppUsers.value = _allAppUsers.value.map { u ->
            if (u.id == app.userId) u.copy(isVerified = true) else u
        }
    }

    fun rejectVerifiedApplication(appId: String) {
        _verifiedApplications.value = _verifiedApplications.value.map {
            if (it.id == appId) it.copy(status = "REJECTED") else it
        }
    }

    // Admin Chat Audit & Moderation in Right Manager Fida
    fun adminDeleteChatMessage(conversationId: String, messageId: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val updatedMessages = conv.messages.filter { it.id != messageId }
                val lastMsg = updatedMessages.lastOrNull()
                val updated = conv.copy(
                    messages = updatedMessages,
                    lastMessage = lastMsg?.text?.ifBlank {
                        if (lastMsg.isMediaVideo) "📹 Video" else if (lastMsg.mediaUrl != null) "📷 Photo" else ""
                    } ?: "মেসেজ মোছা হয়েছে",
                    lastTime = lastMsg?.timestamp ?: conv.lastTime
                )
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
    }

    // 10 Admin Slots for Right Manager Fida (Controlled exclusively by Niyon Islam Sohag)
    private val _adminSlots = MutableStateFlow<List<AdminSlot>>(SampleData.initialAdminSlots)
    val adminSlots: StateFlow<List<AdminSlot>> = _adminSlots.asStateFlow()

    fun assignAdmin(slotNumber: Int, user: User, roleTitle: String) {
        _adminSlots.value = _adminSlots.value.map { slot ->
            if (slot.slotNumber == slotNumber && !slot.isSuperAdmin) {
                slot.copy(user = user, roleTitle = roleTitle, assignedDate = "আজকে • সক্রিয়")
            } else slot
        }
    }

    fun removeAdmin(slotNumber: Int) {
        _adminSlots.value = _adminSlots.value.map { slot ->
            if (slot.slotNumber == slotNumber && !slot.isSuperAdmin) {
                slot.copy(user = null, roleTitle = "অ্যাডমিন পদ খালি (ফাঁকা)", assignedDate = "")
            } else slot
        }
    }

    fun setNidFront(uri: String) {
        _nidFrontUri.value = uri
    }

    fun setNidBack(uri: String) {
        _nidBackUri.value = uri
    }

    fun submitNid(number: String, name: String) {
        _nidNumber.value = number
        _nidApplicantName.value = name
        _isNidSubmitted.value = true
    }

    fun payMonthSubscription(month: String, method: String, pin: String) {
        _paidMonths.value = _paidMonths.value + month
        _isFidaVerified.value = true
        _currentUser.value = _currentUser.value.copy(isVerified = true)
        _paymentSuccessMessage.value = "৳৫০০ টাকা সফলভাবে $method-এর মাধ্যমে Niyon Islam Sohag-এর অ্যাকাউন্টে জমা হয়েছে! $month মাসের Fida Verified সাবস্ক্রিপশন চালু হয়েছে।"
    }

    fun clearPaymentSuccessMessage() {
        _paymentSuccessMessage.value = null
    }

    // ==========================================
    // Universal Global Search Feature
    // ==========================================
    private val _showUniversalSearch = MutableStateFlow(false)
    val showUniversalSearch: StateFlow<Boolean> = _showUniversalSearch.asStateFlow()

    private val _universalSearchQuery = MutableStateFlow("")
    val universalSearchQuery: StateFlow<String> = _universalSearchQuery.asStateFlow()

    private val _universalSearchCategory = MutableStateFlow(UniversalSearchCategory.ALL)
    val universalSearchCategory: StateFlow<UniversalSearchCategory> = _universalSearchCategory.asStateFlow()

    fun openUniversalSearch(initialQuery: String = "") {
        _universalSearchQuery.value = initialQuery
        _showUniversalSearch.value = true
    }

    fun closeUniversalSearch() {
        _showUniversalSearch.value = false
        _universalSearchQuery.value = ""
    }

    fun setUniversalSearchQuery(query: String) {
        _universalSearchQuery.value = query
    }

    fun setUniversalSearchCategory(category: UniversalSearchCategory) {
        _universalSearchCategory.value = category
    }

    // ==========================================
    // Dark Mode, Data Saver & Pro Mode Settings
    // ==========================================
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    private val _isDataSaverMode = MutableStateFlow(false)
    val isDataSaverMode: StateFlow<Boolean> = _isDataSaverMode.asStateFlow()

    fun toggleDataSaverMode() {
        _isDataSaverMode.value = !_isDataSaverMode.value
    }

    private val _isProModeActive = MutableStateFlow(true)
    val isProModeActive: StateFlow<Boolean> = _isProModeActive.asStateFlow()

    fun toggleProMode() {
        _isProModeActive.value = !_isProModeActive.value
    }

    // ==========================================
    // Option 17: Right Manager Fida - যেকোনো ব্যবহারকারীর পোস্ট ফ্রী বুস্ট ও প্রমোট
    // ==========================================
    fun freeBoostAnyPost(
        postId: String,
        reach: String = "1,000,000 (1M) রিচ",
        days: Int = 30,
        audience: String = "সমগ্র বাংলাদেশ"
    ) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setPostBoostActive(postId, true, isOwner = true)
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val upd = post.copy(
                    isBoosted = true,
                    isFreePromotedByNiyon = true,
                    isBoostActive = true,
                    boostTargetReach = reach,
                    boostDays = days,
                    boostAudience = audience,
                    boostStatus = "সক্রিয় (Niyon Islam Sohag ফ্রী প্রমোটেড)",
                    boostBudget = 0,
                    boostTrxId = "FREE_NIYON_VIP",
                    reachViews = if (post.reachViews.contains("M")) post.reachViews else "2.5M"
                )
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    fun togglePostBoostActive(postId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        var newActiveState: Boolean? = null
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newActive = !post.isBoostActive
                newActiveState = newActive
                val upd = post.copy(
                    isBoostActive = newActive,
                    isBoosted = newActive
                )
                updatedPostRef = upd
                upd
            } else post
        }
        newActiveState?.let {
            com.example.data.NiyonOwnerControlStore.setPostBoostActive(postId, it, isOwner = true)
        }
        updatedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    fun cancelPostBoost(postId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setPostBoostActive(postId, false, isOwner = true)
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val upd = post.copy(
                    isBoosted = false,
                    isFreePromotedByNiyon = false,
                    isBoostActive = false,
                    boostStatus = null,
                    boostTargetReach = null,
                    boostAudience = null
                )
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    // Option 21: Right Manager Fida - অ্যান্টি-হ্যাক ও অনুপ্রবেশ মনিটর (real security events only)
    private val _intrusionLogs = MutableStateFlow<List<IntrusionLog>>(emptyList())
    val intrusionLogs: StateFlow<List<IntrusionLog>> = _intrusionLogs.asStateFlow()

    // Option 28: Right Manager Fida - ফেইক আইডি ডিটেক্টর ও ব্লক (১ ব্যক্তি ৩ আইডি, অন্যের ছবি, ছেলে->মেয়ে / মেয়ে->ছেলে)
    private val _fakeIdReports = MutableStateFlow<List<FakeIdReport>>(emptyList())
    val fakeIdReports: StateFlow<List<FakeIdReport>> = _fakeIdReports.asStateFlow()

    fun markFakeIdReportPermanentBanned(reportId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        _fakeIdReports.value = com.example.util.FakeIdDetector.updateReportStatus(
            appContextRef,
            reportId,
            "PERMANENT_BANNED"
        )
    }

    fun deleteFakeIdReport(reportId: String) {
        if (!isCurrentUserNiyonIslamSohag()) return
        _fakeIdReports.value = com.example.util.FakeIdDetector.deleteReport(appContextRef, reportId)
    }

    fun clearAllFakeIdReports() {
        if (!isCurrentUserNiyonIslamSohag()) return
        _fakeIdReports.value = com.example.util.FakeIdDetector.clearReports(appContextRef)
    }

    fun simulateIntrusionTest() {
        val sample = IntrusionLog(
            ipAddress = "${kotlin.random.Random.nextInt(101, 220)}.${kotlin.random.Random.nextInt(10, 255)}.${kotlin.random.Random.nextInt(1, 255)}.${kotlin.random.Random.nextInt(1, 255)}",
            deviceModel = listOf("Redmi Note 12 (Rooted)", "Unknown Linux Pentester", "Samsung S21 (Modified Dex)", "TOR Proxy Node").random(),
            attackerName = "সন্দেহভাজন অনুপ্রবেশকারী #${kotlin.random.Random.nextInt(1000, 9999)}",
            attackVector = listOf(
                "অনুপযুক্ত API ডিকম্পাইল ও রিভার্স ইঞ্জিনিয়ারিং চেষ্টা",
                "Right Manager Fida সিক্রেট লক বাইপাস চেষ্টা",
                "ভেরিফাইড ব্যাজ হ্যাক ও ডাটাবেস টেম্পারিং",
                "অননুমোদিত বট ট্র্যাফিক ও স্ক্র্যাপিং অ্যাটাক"
            ).random(),
            timestamp = "এইমাত্র",
            targetArea = listOf("Right Manager Fida", "অ্যাডমিন গেটওয়ে", "ডাটাবেস ভল্ট", "মেসেঞ্জার API").random(),
            severity = "CRITICAL 🔴",
            status = "তাৎক্ষণিক প্রতিহত ও ব্লকড 🛡️",
            defenseLog = "স্বয়ংক্রিয় সিকিউরিটি শিল্ড দ্বারা আক্রমণকারীর আইপি ও ডিভাইস আইডি পার্মানেন্ট নিষিদ্ধ করা হয়েছে।"
        )
        _intrusionLogs.value = listOf(sample) + _intrusionLogs.value
    }

    fun blockIntruderIp(id: String) {
        _intrusionLogs.value = _intrusionLogs.value.map {
            if (it.id == id) it.copy(status = "পার্মানেন্ট আইপি ও ডিভাইস ব্লকড 🚫") else it
        }
    }

    fun clearResolvedIntrusions() {
        _intrusionLogs.value = emptyList()
    }

    // ==========================================
    // Option 22: Right Manager Fida - যেকোনো ভিডিও ফ্রী বুস্টার (Niyon Special)
    // ==========================================
    fun freeBoostAnyVideo(
        postId: String,
        targetViews: String = "1,000,000+ VIP ভাইরাল ভিউ",
        trendingBadge: String = "🔥 ভাইরাল ট্রেন্ডিং #1",
        days: Int = 30
    ) {
        if (!isCurrentUserNiyonIslamSohag()) return
        com.example.data.NiyonOwnerControlStore.setPostBoostActive(postId, true, isOwner = true)
        var updatedPostRef: Post? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedReactions = post.reactionCounts.toMutableMap().apply {
                    put(ReactionType.LOVE, (get(ReactionType.LOVE) ?: 500) + 3800)
                    put(ReactionType.HUNDRED, (get(ReactionType.HUNDRED) ?: 200) + 1500)
                }
                val upd = post.copy(
                    isBoosted = true,
                    isFreePromotedByNiyon = true,
                    isBoostActive = true,
                    boostTargetReach = targetViews,
                    reachViews = if (post.reachViews.contains("M")) post.reachViews else "2.1M",
                    boostDays = days,
                    boostAudience = "ভিআইপি ভিডিও ফিড ও শর্ট ভিডিও ট্রেন্ডিং (বাংলাদেশ ও বিশ্বব্যাপী)",
                    boostStatus = "সক্রিয় 🔥 (Niyon Islam Sohag ফ্রী ভিডিও বুস্টার)",
                    boostBudget = 0,
                    boostTrxId = "NIYON_VIDEO_BOOST_VIP",
                    reactionCounts = updatedReactions,
                    shareCount = post.shareCount + 420
                )
                updatedPostRef = upd
                upd
            } else post
        }
        updatedPostRef?.let {
            persistPostToRoom(it)
            com.example.data.FirebaseManager.savePost(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCloudSync(keepLoggedIn = true)
    }
}
