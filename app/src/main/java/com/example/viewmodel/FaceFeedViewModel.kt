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

    private var activeChatListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var postsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var musicListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var pagesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var groupsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val commentListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
    private var listenersStartedForUid: String? = null
    private var isCurrentOwnerAuthenticated: Boolean = false

    val hasMorePosts: StateFlow<Boolean> = com.example.data.FirebaseManager.hasMorePosts
    val isLoadingMorePosts: StateFlow<Boolean> = com.example.data.FirebaseManager.isLoadingMorePosts
    val cloudQuotaNotice: StateFlow<String?> = com.example.data.FirebaseManager.cloudQuotaNotice

    fun dismissQuotaNotice() {
        com.example.data.FirebaseManager.dismissQuotaNotice()
    }

    private val _savedEmailAccounts = MutableStateFlow<List<com.example.data.SavedEmailAccount>>(emptyList())
    val savedEmailAccounts: StateFlow<List<com.example.data.SavedEmailAccount>> = _savedEmailAccounts.asStateFlow()

    fun loadSavedEmailAccounts(context: android.content.Context) {
        val loaded = com.example.data.SavedAccountsStore.loadAccounts(context)
        val merged = linkedMapOf<String, com.example.data.SavedEmailAccount>()
        loaded.forEach { acc -> merged[acc.email.trim().lowercase()] = acc }

        // Also include Firebase currentUser if signed in
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { fbUser ->
                val fbEmail = fbUser.email?.trim().orEmpty()
                if (fbEmail.isNotBlank()) {
                    val key = fbEmail.lowercase()
                    val isOwner = key == "smd224292@gmail.com" || key == "nihonislamsohag@gmail.com"
                    merged[key] = com.example.data.SavedEmailAccount(
                        id = fbUser.uid,
                        fullName = fbUser.displayName?.takeIf { it.isNotBlank() } ?: fbEmail.substringBefore("@"),
                        username = fbEmail.substringBefore("@"),
                        email = fbEmail,
                        avatarUrl = fbUser.photoUrl?.toString()?.takeIf { it.isNotBlank() }
                            ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                        isFirebaseLinked = true,
                        isOwner = isOwner
                    )
                }
            }
        } catch (_: Exception) {
        }

        _savedEmailAccounts.value = merged.values.sortedByDescending { it.lastUsedAt }

        try {
            val billingPrefs = context.applicationContext.getSharedPreferences(
                "fotofilm_billing_prefs",
                android.content.Context.MODE_PRIVATE
            )
            _isBlazeCardModeEnabled.value = billingPrefs.getBoolean("blaze_card_mode_enabled", false)
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

    fun startCloudSyncForUser(
        firebaseUser: com.google.firebase.auth.FirebaseUser,
        context: android.content.Context? = null
    ) {
        val emailLower = firebaseUser.email?.trim()?.lowercase().orEmpty()
        val nameLower = firebaseUser.displayName?.trim()?.lowercase().orEmpty()
        val isOwner = emailLower == "smd224292@gmail.com" ||
            emailLower == "nihonislamsohag@gmail.com" ||
            (nameLower.contains("niyon") && nameLower.contains("sohag"))
        isCurrentOwnerAuthenticated = isOwner

        if (isOwner) {
            _currentUser.value = SampleData.currentUser.copy(
                id = firebaseUser.uid,
                fullName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.fullName,
                email = firebaseUser.email?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.email,
                avatarUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.avatarUrl,
                isVerified = true
            )
        } else {
            val resolvedName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: emailLower.substringBefore("@").ifBlank { "FotoFilm User" }
            val resolvedHandle = resolvedName.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user" }
            _currentUser.value = _currentUser.value.copy(
                id = firebaseUser.uid,
                fullName = resolvedName,
                username = resolvedHandle,
                email = firebaseUser.email ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() }
                    ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500"
            )
            closeRightManagerFida()
        }

        recordSavedAccount(
            context = context,
            user = _currentUser.value,
            isFirebaseLinked = true,
            isOwner = isOwner
        )

        _isLoggedIn.value = true
        _showAuthDialog.value = false

        if (listenersStartedForUid == firebaseUser.uid) return

        stopCloudSync(keepLoggedIn = true)
        listenersStartedForUid = firebaseUser.uid

        com.example.data.FirebaseManager.syncSignedInUserProfile(_currentUser.value)

        postsListener = com.example.data.FirebaseManager.listenToPosts { cloudPosts ->
            val existingById = _posts.value.associateBy { it.id }
            val mergedCloud = cloudPosts.map { cp ->
                val old = existingById[cp.id]
                if (old != null) {
                    cp.copy(
                        comments = old.comments.ifEmpty { cp.comments },
                        userReaction = old.userReaction ?: cp.userReaction,
                        reactors = old.reactors.ifEmpty { cp.reactors }
                    )
                } else cp
            }
            val cloudIds = cloudPosts.map { it.id }.toSet()
            val localOnly = _posts.value.filter { it.id !in cloudIds }
            _posts.value = (mergedCloud + localOnly).sortedByDescending { it.createdAt }
        }

        musicListener = com.example.data.FirebaseManager.listenToMusicTracks { cloudTracks ->
            val existingIds = cloudTracks.map { it.id }.toSet()
            val localTracks = _musicTracks.value.filter { it.id !in existingIds }
            _musicTracks.value = cloudTracks + localTracks
        }

        pagesListener = com.example.data.FirebaseManager.listenToPages { cloudPages ->
            val existingIds = cloudPages.map { it.id }.toSet()
            val localPages = _pages.value.filter { it.id !in existingIds }
            _pages.value = cloudPages + localPages
        }

        groupsListener = com.example.data.FirebaseManager.listenToGroups { cloudGroups ->
            val existingIds = cloudGroups.map { it.id }.toSet()
            val localGroups = _groups.value.filter { it.id !in existingIds }
            _groups.value = cloudGroups + localGroups
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
            isCurrentOwnerAuthenticated = false
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

    fun toggleAutoCompression(enabled: Boolean) {
        com.example.data.DailyQuotaCompressionStore.setAutoCompressionEnabled(enabled)
    }

    fun toggleDaily43MbLimit(enabled: Boolean) {
        com.example.data.DailyQuotaCompressionStore.setDaily43MbLimitEnabled(enabled)
    }

    fun resetUserDailyQuota(userId: String) {
        com.example.data.DailyQuotaCompressionStore.resetUserDailyUsage(userId, _currentUser.value.id)
    }

    fun resetAllUsersDailyQuota() {
        com.example.data.DailyQuotaCompressionStore.resetAllDailyUsage(_currentUser.value.id)
    }

    fun toggleBlazeCardMode(enabled: Boolean, context: android.content.Context) {
        _isBlazeCardModeEnabled.value = enabled
        try {
            context.applicationContext
                .getSharedPreferences("fotofilm_billing_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("blaze_card_mode_enabled", enabled)
                .apply()
        } catch (_: Exception) {
        }
    }

    fun toggleCloudinaryEnabled(enabled: Boolean) {
        com.example.data.CloudinaryManager.setEnabled(enabled)
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

    // Current logged-in user
    private val _currentUser = MutableStateFlow(SampleData.currentUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Auth status
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

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

    // Blocked Users State Flow & Profiles
    private val _blockedProfiles = MutableStateFlow<List<User>>(
        listOf(
            User(id = "blk_shakil", fullName = "শাকিল আহমেদ", username = "shakil_ahmed", avatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500", bio = "Blocked Profile", isVerified = false),
            User(id = "blk_sumaiya", fullName = "সুমাইয়া জান্নাত", username = "sumaiya_jannat", avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500", bio = "Blocked Profile", isVerified = false),
            User(id = "blk_kamrul", fullName = "কামরুল ইসলাম", username = "kamrul_isl", avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500", bio = "Blocked Profile", isVerified = false)
        )
    )
    val blockedProfiles: StateFlow<List<User>> = _blockedProfiles.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(setOf("blk_shakil", "blk_sumaiya", "blk_kamrul"))
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    // Restricted Profiles State Flow
    private val _restrictedProfiles = MutableStateFlow<List<User>>(
        listOf(
            User(id = "res_rokibul", fullName = "রকিবুল ইসলাম", username = "rokibul_isl", avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500", bio = "Digital Creator", isVerified = false, friendCount = 1240),
            User(id = "res_mehedi", fullName = "মেহেদী হাসান", username = "mehedi_hasan", avatarUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500", bio = "Freelancer", isVerified = true, friendCount = 890),
            User(id = "res_anika", fullName = "আনিকা তাবাসসুম", username = "anika_tabassum", avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500", bio = "Student", isVerified = false, friendCount = 640)
        )
    )
    val restrictedProfiles: StateFlow<List<User>> = _restrictedProfiles.asStateFlow()

    // Permanent Message Delivery Vault (কখনোই মেসেজ, ছবি, ভিডিও, ভয়েস ডিলেট হবে না)
    private val _permanentDeliveryVault = MutableStateFlow<List<DeliveredVaultMessage>>(
        listOf(
            DeliveredVaultMessage(
                id = "vault_msg_1",
                conversationId = "chat_md_mohiuddin",
                contactId = "user_md_mohiuddin",
                contactName = "মোঃ মহিউদ্দিন",
                contactAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                senderId = "user_md_mohiuddin",
                senderName = "মোঃ মহিউদ্দিন",
                isFromMe = false,
                text = "তুমি ঢাকা কী চাকরি করো,,",
                timestamp = "6:26 PM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_2",
                conversationId = "chat_md_mohiuddin",
                contactId = "user_md_mohiuddin",
                contactName = "মোঃ মহিউদ্দিন",
                contactAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                senderId = "user_me",
                senderName = "Niyon Islam Sohag",
                isFromMe = true,
                text = "আমি ডিজিটাল ক্রিয়েটর ও সফটওয়্যার ইঞ্জিনিয়ার হিসেবে কাজ করছি ভাইয়া।",
                timestamp = "6:28 PM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_3",
                conversationId = "chat_rahim_rahima",
                contactId = "user_rahim",
                contactName = "আব্দুর রহিম (Rahim)",
                contactAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500",
                senderId = "user_rahim",
                senderName = "আব্দুর রহিম",
                isFromMe = false,
                text = "আসসালামু আলাইকুম রহিমা, কেমন আছো? কালকের প্রোগ্রামের ছবিগুলো কি রেডি?",
                timestamp = "10:14 AM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_4",
                conversationId = "chat_rahim_rahima",
                contactId = "user_rahim",
                contactName = "রহিমা খাতুন",
                contactAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
                senderId = "user_rahima",
                senderName = "রহিমা খাতুন",
                isFromMe = false,
                text = "📷 এই যে অনুষ্ঠানের প্রধান ব্যানার ও স্টেজ ফটো।",
                mediaUrl = "https://images.unsplash.com/photo-1511578314322-379afb476865?w=800",
                isMediaVideo = false,
                timestamp = "10:18 AM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_5",
                conversationId = "chat_rahim_rahima",
                contactId = "user_rahim",
                contactName = "রহিমা খাতুন",
                contactAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
                senderId = "user_rahima",
                senderName = "রহিমা খাতুন",
                isFromMe = false,
                text = "📹 হ্যাঁ, ১৫ সেকেন্ডের একটা সুন্দর হাইলাইটস রিলস ভিডিও বানিয়েছি। এই যে ভিডিও ক্লিপটা:",
                mediaUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800",
                isMediaVideo = true,
                timestamp = "10:22 AM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_6",
                conversationId = "chat_hasan_mia",
                contactId = "user_hasan_mia_howlader",
                contactName = "হাঢান মিয়া হওলাদার",
                contactAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=500",
                senderId = "user_hasan_mia_howlader",
                senderName = "হাঢান মিয়া হওলাদার",
                isFromMe = false,
                text = "🎤 অডিও ভয়েস (18s)",
                isAudioVoice = true,
                audioDurationSeconds = 18,
                timestamp = "12:12 PM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_7",
                conversationId = "chat_hasan_mia",
                contactId = "user_hasan_mia_howlader",
                contactName = "হাঢান মিয়া হওলাদার",
                contactAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=500",
                senderId = "user_hasan_mia_howlader",
                senderName = "হাঢান মিয়া হওলাদার",
                isFromMe = false,
                text = "ভুল স্বীকার করলো বানর",
                mediaUrl = "https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?w=800",
                isMediaVideo = false,
                timestamp = "10:43 AM",
                formattedDate = "আজ"
            ),
            DeliveredVaultMessage(
                id = "vault_msg_8",
                conversationId = "chat_tanvir",
                contactId = "user_tanvir",
                contactName = "Tanvir Ahmed",
                contactAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                senderId = "user_tanvir",
                senderName = "Tanvir Ahmed",
                isFromMe = false,
                text = "🎤 অডিও ভয়েস (25s)",
                isAudioVoice = true,
                audioDurationSeconds = 25,
                timestamp = "4:15 PM",
                formattedDate = "গতকাল"
            )
        )
    )
    val permanentDeliveryVault: StateFlow<List<DeliveredVaultMessage>> = _permanentDeliveryVault.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Posts
    private val _posts = MutableStateFlow(SampleData.initialPosts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Admin Payment Receiving Numbers for Boost & Promotions (strictly 01842450605 for bKash & Nagad)
    private val _adminBkashNumber = MutableStateFlow("01842450605")
    val adminBkashNumber: StateFlow<String> = _adminBkashNumber.asStateFlow()

    private val _adminNagadNumber = MutableStateFlow("01842450605")
    val adminNagadNumber: StateFlow<String> = _adminNagadNumber.asStateFlow()

    private val _adminRocketNumber = MutableStateFlow("01842450605")
    val adminRocketNumber: StateFlow<String> = _adminRocketNumber.asStateFlow()

    // Saved Videos & Posts (Per user request: "Saved জারা যে কোনো ভিডিও ছেইভ করবে সেই ভিডিও এখানে এসে জমা হবে আর দেখা জাবে")
    private val _savedPosts = MutableStateFlow<List<Post>>(
        SampleData.initialPosts.filter { it.isVideo || it.isReel }
    )
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    fun savePost(post: Post) {
        if (_savedPosts.value.none { it.id == post.id }) {
            _savedPosts.value = listOf(post) + _savedPosts.value
        }
    }

    fun unsavePost(postId: String) {
        _savedPosts.value = _savedPosts.value.filter { it.id != postId }
    }

    fun isPostSaved(postId: String): Boolean {
        return _savedPosts.value.any { it.id == postId }
    }

    // Boost & Ads Transactions
    private val _boostTransactions = MutableStateFlow<List<com.example.model.BoostTransaction>>(
        listOf(
            com.example.model.BoostTransaction(
                id = "boost_demo_1",
                postId = "post_1",
                postTitleOrText = "Face Feed Official 🇧🇩 অফিসিয়াল প্ল্যাটফর্ম পেজে স্বাগতম",
                isVideo = false,
                authorName = "Face Feed Official",
                paymentMethod = "bKash",
                senderNumber = "01789123456",
                trxId = "BKH9281745",
                amountBdt = 300,
                durationDays = 3,
                estimatedReach = "১৫,০০০ - ২০,০০০ মানুষের ফিডে বিজ্ঞাপন",
                goal = "বেসিক বুস্ট 🚀",
                status = "ACTIVE",
                createdAt = "০২ অক্টোবর ২০২৬, ১১:২০ AM"
            ),
            com.example.model.BoostTransaction(
                id = "boost_demo_2",
                postId = "post_2",
                postTitleOrText = "আমাদের নতুন ড্রোন ভিডিও ফুটেজ ও পর্যটন ব্লগ 🎬",
                isVideo = true,
                authorName = "Tanvir Ahmed",
                paymentMethod = "Nagad",
                senderNumber = "01855234981",
                trxId = "NGD8841209",
                amountBdt = 200,
                durationDays = 2,
                estimatedReach = "১০,০০০ ভিডিও ভিউ ও রিচ",
                goal = "১০,০০০ ভিউ স্পেশাল 👁️",
                status = "PENDING",
                createdAt = "০২ অক্টোবর ২০২৬, ০৯:৪৫ AM"
            ),
            com.example.model.BoostTransaction(
                id = "boost_demo_3",
                postId = "post_3",
                postTitleOrText = "ডিজিটাল ক্রিয়েটর ও কোডিং কমিউনিটি মেগা অফার 💻",
                isVideo = false,
                authorName = "Arifur Rahman",
                paymentMethod = "Rocket",
                senderNumber = "01912445566",
                trxId = "RCK3319022",
                amountBdt = 500,
                durationDays = 7,
                estimatedReach = "৫০,০০০ - ৮০,০০০ মানুষ",
                goal = "স্ট্যান্ডার্ড বুস্ট 🔥",
                status = "ACTIVE",
                createdAt = "০১ অক্টোবর ২০২৬, ০৪:১৫ PM"
            ),
            com.example.model.BoostTransaction(
                id = "boost_demo_4",
                postId = "post_4",
                postTitleOrText = "ঢাকা স্ট্রিট ফটোগ্রাফি এক্সিবিশন ২০২৬ 📸",
                isVideo = false,
                authorName = "Nusrat Jahan",
                paymentMethod = "bKash",
                senderNumber = "01711223344",
                trxId = "BKH5501928",
                amountBdt = 1000,
                durationDays = 15,
                estimatedReach = "১,২০,০০০+ মানুষ",
                goal = "প্রিমিয়াম ভিআইপি 👑",
                status = "PENDING",
                createdAt = "৩০ সেপ্টেম্বর ২০২৬, ০৮:১০ PM"
            ),
            com.example.model.BoostTransaction(
                id = "boost_demo_5",
                postId = "post_5",
                postTitleOrText = "মেগা গ্র্যান্ড লঞ্চিং ইভেন্ট ও সেলিব্রেশন 💎",
                isVideo = true,
                authorName = "Sadia Islam",
                paymentMethod = "Nagad",
                senderNumber = "01699887766",
                trxId = "NGD1199345",
                amountBdt = 2000,
                durationDays = 30,
                estimatedReach = "৩,০০,০০০+ মানুষ",
                goal = "মেগা প্রোমোশন 💎",
                status = "COMPLETED",
                createdAt = "১৫ সেপ্টেম্বর ২০২৬, ১২:০০ PM"
            ),
            com.example.model.BoostTransaction(
                id = "boost_demo_rahim",
                postId = "post_sahid",
                postTitleOrText = "রহিম (Rahim) - ৩ দিনের বেসিক পোস্ট বুস্ট ও রিচ 🚀",
                isVideo = false,
                authorName = "আব্দুর রহিম (Rahim)",
                paymentMethod = "bKash",
                senderNumber = "01812345678",
                trxId = "BK11012026",
                amountBdt = 300,
                durationDays = 3,
                estimatedReach = "১৫,০০০ - ২০,০০০ মানুষ",
                goal = "বেসিক বুস্ট 🚀",
                status = "ACTIVE",
                createdAt = "০১/০১/২০২৬ (1/1/2026)"
            )
        )
    )
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

        boostedPost?.let { com.example.data.FirebaseManager.savePost(it) }
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
            senderNumber = "01812345678 (Owner VIP)",
            trxId = "VIP-MILLION-VIRAL-FREE",
            amountBdt = 0,
            durationDays = 365,
            estimatedReach = reachMultiplier,
            goal = "মিলিয়ন মানুষের কাছে পৌঁছে দেওয়া",
            status = "ACTIVE",
            createdAt = "০১/০১/২০২৬ (1/1/2026)"
        )
        _boostTransactions.value = listOf(newTx) + _boostTransactions.value
        boostedPost?.let { com.example.data.FirebaseManager.savePost(it) }
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

    // Stories
    private val _stories = MutableStateFlow(SampleData.initialStories)
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _viewingStory = MutableStateFlow<Story?>(null)
    val viewingStory: StateFlow<Story?> = _viewingStory.asStateFlow()

    // Highlights (Friends এর নিচে হাইলাইটস)
    private val _userHighlights = MutableStateFlow<Map<String, List<StoryHighlight>>>(SampleData.initialHighlightsMap)
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
        _pages.value = _pages.value.map { page ->
            if (page.id == pageId) page.copy(isActive = isActive) else page
        }
    }

    fun toggleGroupStatus(groupId: String, isActive: Boolean) {
        _groups.value = _groups.value.map { group ->
            if (group.id == groupId) group.copy(isActive = isActive) else group
        }
    }

    // Notifications
    private val _notifications = MutableStateFlow(SampleData.initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Fully Functional Dynamic Header Notification Badges (Facebook-like)
    private val _homeBadgeCount = MutableStateFlow(16) // Initial: 15+ new posts
    val homeBadgeCount: StateFlow<Int> = _homeBadgeCount.asStateFlow()

    private val _friendsBadgeCount = MutableStateFlow(SampleData.pendingRequestUsers.size) // Initial: 7 pending friend requests
    val friendsBadgeCount: StateFlow<Int> = _friendsBadgeCount.asStateFlow()

    private val _messagesBadgeCount = MutableStateFlow(SampleData.initialConversations.sumOf { it.unreadCount }) // Initial: 3 unread messages
    val messagesBadgeCount: StateFlow<Int> = _messagesBadgeCount.asStateFlow()

    private val _hasNewMessageDot = MutableStateFlow(true)
    val hasNewMessageDot: StateFlow<Boolean> = _hasNewMessageDot.asStateFlow()

    private val _watchBadgeCount = MutableStateFlow(16) // Initial: 15+ unwatched videos
    val watchBadgeCount: StateFlow<Int> = _watchBadgeCount.asStateFlow()

    private val _notificationsBadgeCount = MutableStateFlow(SampleData.initialNotifications.count { !it.isRead }) // Initial: 2 unread notifications
    val notificationsBadgeCount: StateFlow<Int> = _notificationsBadgeCount.asStateFlow()

    // Notification Settings: Sound & Vibration
    private val _isNotificationSoundEnabled = MutableStateFlow(true)
    val isNotificationSoundEnabled: StateFlow<Boolean> = _isNotificationSoundEnabled.asStateFlow()

    private val _isNotificationVibrationEnabled = MutableStateFlow(true)
    val isNotificationVibrationEnabled: StateFlow<Boolean> = _isNotificationVibrationEnabled.asStateFlow()

    private val _showNotificationDropdown = MutableStateFlow(false)
    val showNotificationDropdown: StateFlow<Boolean> = _showNotificationDropdown.asStateFlow()

    fun clearHomeBadge() {
        _homeBadgeCount.value = 0
    }

    fun refreshHomeFeed() {
        _homeBadgeCount.value = 0
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
        val newReport = CyberOffenderReport(
            id = "offender_${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            userName = userName,
            userUsername = userName.lowercase().replace(" ", "_"),
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            reportedComment = comment,
            targetPostTitle = postTitle,
            timestamp = "০১/০১/২০২৬ (1/1/2026)",
            nationalId = "১৯${(10000000000000L..99999999999999L).random()}",
            phoneNumber = "+৮৮০ ১৭১${(1000000..9999999).random()}",
            ipAddress = "103.${(10..250).random()}.${(10..250).random()}.${(10..250).random()}",
            deviceModel = "Android Device (আইপি ও আইএমইআই চিহ্নিত)",
            location = "বাংলাদেশ (সাইবার ক্রাইম ট্র্যাকিং সক্রিয়)",
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
            title = title.ifBlank { "FaceFeed নতুন আপডেট প্রকাশিত হয়েছে!" },
            description = description.ifBlank { "সকল ফিচারের চমৎকার পারফরম্যান্স ও নতুন সুবিধা যুক্ত করা হয়েছে।" },
            appLogoUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300",
            ownerAvatar = owner.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300" },
            ownerName = owner.fullName,
            releaseDate = "০১/০১/২০২৬ (1/1/2026)",
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
        val user = _currentUser.value
        val isNiyon = user.id == "user_niyon" || user.fullName.contains("Niyon", ignoreCase = true)
        if (!isNiyon) {
            return false // Unauthorized: Only Niyon Islam Sohag can toggle languages
        }
        val updated = _languageStatus.value.toMutableMap()
        updated[language] = isEnabled
        _languageStatus.value = updated
        // If disabled language is currently selected, fall back to Bengali immediately
        if (!isEnabled && _currentLanguage.value == language) {
            _currentLanguage.value = AppLanguage.BENGALI
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
        _showLiveStudio.value = true
    }

    fun closeLiveStudio() {
        _showLiveStudio.value = false
    }

    fun publishLongVideo(title: String, videoUrl: String, description: String) {
        createPost(
            text = if (description.isNotBlank()) "$title\n\n$description" else title,
            hashtags = listOf("#LongVideo", "#FaceFeedWatch", "#ViralVideo", "#Bangladesh"),
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
            text = if (caption.isNotBlank()) caption else "New Reel on FaceFeed 🎬✨",
            hashtags = listOf("#Reels", "#ShortVideo", "#FaceFeedReels", "#Trending"),
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
            hashtags = listOf("#FaceFeed", "#Status", "#Thoughts"),
            images = emptyList(),
            isVideo = false,
            videoTitle = null,
            isReel = false
        )
        _activeMediaPickerTarget.value = MediaPickerTarget.NONE
        _activeTab.value = AppTab.HOME
    }

    fun setShowAuthDialog(show: Boolean) {
        _showAuthDialog.value = show
    }

    fun login(
        emailOrUser: String,
        pass: String,
        context: android.content.Context? = null
    ): Boolean {
        val input = emailOrUser.trim()
        if (input.isBlank()) return false
        val lower = input.lowercase()
        val isOwner = lower == "smd224292@gmail.com" ||
            lower == "nihonislamsohag@gmail.com" ||
            lower == "niyon_sohag" ||
            lower == "niyonislamsohag" ||
            (lower.contains("niyon") && lower.contains("sohag"))

        val matchedSaved = _savedEmailAccounts.value.firstOrNull {
            it.email.trim().equals(input, ignoreCase = true) ||
                it.username.trim().equals(input, ignoreCase = true)
        }

        if (isOwner) {
            _currentUser.value = SampleData.currentUser.copy(
                email = if (input.contains("@")) input else SampleData.currentUser.email,
                isVerified = true
            )
            isCurrentOwnerAuthenticated = true
        } else if (matchedSaved != null) {
            _currentUser.value = _currentUser.value.copy(
                id = matchedSaved.id,
                fullName = matchedSaved.fullName,
                username = matchedSaved.username,
                email = matchedSaved.email,
                avatarUrl = matchedSaved.avatarUrl,
                isVerified = false
            )
            isCurrentOwnerAuthenticated = false
            closeRightManagerFida()
        } else {
            val resolvedName = input.substringBefore("@").ifBlank { "FotoFilm User" }
            val resolvedHandle = resolvedName.lowercase().replace(Regex("[^a-z0-9_]"), "_").trim('_').ifBlank { "user" }
            _currentUser.value = _currentUser.value.copy(
                id = "user_${resolvedHandle}_${UUID.randomUUID().toString().take(6)}",
                fullName = resolvedName,
                username = resolvedHandle,
                email = if (input.contains("@")) input else "$resolvedHandle@fotofilm.app",
                isVerified = false
            )
            isCurrentOwnerAuthenticated = false
            closeRightManagerFida()
        }
        recordSavedAccount(
            context = context,
            user = _currentUser.value,
            isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
            isOwner = isOwner
        )
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
        if (fullName.isBlank()) return false
        val emailLower = email.trim().lowercase()
        val nameLower = fullName.trim().lowercase()
        val userLower = username.trim().lowercase()
        val isOwner = emailLower == "smd224292@gmail.com" ||
            emailLower == "nihonislamsohag@gmail.com" ||
            userLower == "niyon_sohag" ||
            userLower == "niyonislamsohag" ||
            (nameLower.contains("niyon") && nameLower.contains("sohag"))

        val resolvedUsername = if (username.isNotBlank()) username.trim() else fullName.trim().lowercase().replace(" ", "_")
        val activeFirebaseUid = com.example.data.FirebaseManager.activeAuthUid()
        val newUser = if (isOwner) {
            SampleData.currentUser.copy(
                id = activeFirebaseUid ?: SampleData.currentUser.id,
                fullName = fullName.trim(),
                username = resolvedUsername,
                email = email.trim().ifBlank { SampleData.currentUser.email },
                gender = gender.trim(),
                country = country.trim(),
                birthDate = birthDate.trim(),
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: SampleData.currentUser.avatarUrl,
                isVerified = true
            )
        } else {
            User(
                id = activeFirebaseUid ?: "user_${UUID.randomUUID().toString().take(8)}",
                fullName = fullName.trim(),
                username = resolvedUsername,
                email = email.trim(),
                gender = gender.trim(),
                country = country.trim(),
                birthDate = birthDate.trim(),
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
                    ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
                bio = "FotoFilm সদস্য ✨",
                isVerified = false
            )
        }

        isCurrentOwnerAuthenticated = isOwner
        if (!isOwner) {
            closeRightManagerFida()
        }
        _currentUser.value = newUser
        recordSavedAccount(
            context = context,
            user = newUser,
            isFirebaseLinked = activeFirebaseUid != null,
            isOwner = isOwner
        )
        com.example.data.FirebaseManager.syncSignedInUserProfile(newUser)
        _isLoggedIn.value = true
        _showAuthDialog.value = false
        return true
    }

    fun loginWithAppleId(context: android.content.Context? = null) {
        val appleUser = _currentUser.value.copy(
            id = "apple_${UUID.randomUUID().toString().take(8)}",
            fullName = "Apple User",
            username = "apple_user",
            email = "apple.user@icloud.com",
            isVerified = true
        )
        isCurrentOwnerAuthenticated = false
        closeRightManagerFida()
        _currentUser.value = appleUser
        recordSavedAccount(
            context = context,
            user = appleUser,
            isFirebaseLinked = com.example.data.FirebaseManager.activeAuthUid() != null,
            isOwner = false
        )
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
        val updatedUser = _currentUser.value.copy(
            fullName = fullName.trim(),
            username = if (username.isNotBlank()) username.trim() else fullName.trim().lowercase().replace(" ", "_"),
            gender = gender.trim(),
            country = country.trim(),
            birthDate = birthDate.trim(),
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: _currentUser.value.avatarUrl
        )
        _currentUser.value = updatedUser
        com.example.data.FirebaseManager.syncSignedInUserProfile(updatedUser)
        _showAuthDialog.value = false
        return true
    }

    fun logout() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
        }
        stopCloudSync(keepLoggedIn = false)
        _showAuthDialog.value = true
    }

    // Profile updates
    fun updateProfileAvatar(uri: String) {
        _currentUser.value = _currentUser.value.copy(avatarUrl = uri)
    }

    fun updateCoverPhoto(uri: String) {
        _currentUser.value = _currentUser.value.copy(coverUrl = uri)
    }

    fun updateAvatarFrame(frameId: String?) {
        _currentUser.value = _currentUser.value.copy(avatarFrame = frameId)
    }

    fun shareProfileAvatarToStory(caption: String = "নতুন প্রোফাইল ছবি ✨") {
        val avatar = _currentUser.value.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" }
        addStory(avatar, caption)
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

        // Sync to cloud Firebase so all users see it in real-time
        com.example.data.FirebaseManager.savePost(newPost)

        // Track daily 43.1 MB budget & perform real auto-compression on media
        if (quotaCategory != null) {
            val ctx = com.example.data.CloudinaryManager.getAppContext()
            val isAutoCompress = com.example.data.DailyQuotaCompressionStore.config.value.isAutoCompressionEnabled

            viewModelScope.launch(Dispatchers.IO) {
                var totalUsedUnit = 0.0
                var totalSavedMb = 0.0
                var latestBanner: String? = null
                val compressedLocalMap = mutableMapOf<String, android.net.Uri>()

                cappedImages.forEach { mediaUriStr ->
                    if (mediaUriStr.startsWith("content://") || mediaUriStr.startsWith("file://")) {
                        try {
                            val rawUri = android.net.Uri.parse(mediaUriStr)
                            if (ctx != null && isAutoCompress) {
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
                    itemCount = if (quotaCategory == com.example.data.DailyQuotaCategory.FEED_PHOTO) cappedImages.size else 1,
                    usedMbOrKb = totalUsedUnit,
                    savedMb = totalSavedMb
                )

                if (latestBanner != null) {
                    _compressionToastMessage.value = latestBanner
                }

                // Upload compressed files to Cloudinary (free, no card needed) or Firebase Storage fallback
                cappedImages.forEach { mediaUriStr ->
                    if (mediaUriStr.startsWith("content://") || mediaUriStr.startsWith("file://")) {
                        try {
                            val uploadUri = compressedLocalMap[mediaUriStr] ?: android.net.Uri.parse(mediaUriStr)
                            val onCloudReady: (String) -> Unit = { downloadUrl ->
                                _posts.value = _posts.value.map { cur ->
                                    if (cur.id == newPost.id) {
                                        val updated = cur.copy(
                                            images = cur.images.map { if (it == mediaUriStr) downloadUrl else it }
                                        )
                                        com.example.data.FirebaseManager.savePost(updated)
                                        updated
                                    } else cur
                                }
                            }
                            if (com.example.data.CloudinaryManager.isEnabled() && com.example.data.CloudinaryManager.isConfigured()) {
                                com.example.data.CloudinaryManager.uploadMedia(
                                    uri = uploadUri,
                                    isVideoOrAudio = effectiveIsVideo || effectiveIsReel,
                                    folder = if (effectiveIsVideo || effectiveIsReel) "fotofilm/videos" else "fotofilm/photos",
                                    compressionKind = compressionKind,
                                    onSuccess = onCloudReady,
                                    onFailure = {
                                        com.example.data.FirebaseManager.uploadFileToStorage(
                                            uri = uploadUri,
                                            folder = if (effectiveIsVideo || effectiveIsReel) "videos" else "photos",
                                            onSuccess = onCloudReady,
                                            onFailure = { }
                                        )
                                    }
                                )
                            } else {
                                com.example.data.FirebaseManager.uploadFileToStorage(
                                    uri = uploadUri,
                                    folder = if (effectiveIsVideo || effectiveIsReel) "videos" else "photos",
                                    onSuccess = onCloudReady,
                                    onFailure = { }
                                )
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
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
            title = "📢 $headline",
            text = "$headline\n\n🎯 টার্গেটেড বিজ্ঞাপন • বাজেট: $budget • লক্ষ্যমাত্রা: $reach\nFace AD এডমিন দ্বারা অনুমোদিত স্পনসর্ড বিজ্ঞাপন।",
            hashtags = listOf("#Sponsored", "#Ad", "#FaceFeedPromo", "#Business"),
            images = listOf("https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800"),
            reachViews = reach.ifBlank { "50K" },
            isBoosted = true,
            boostStatus = "Active",
            boostTargetReach = reach,
            reactionCounts = mapOf(ReactionType.LIKE to 28, ReactionType.LOVE to 14)
        )
        _posts.value = listOf(newAdPost) + _posts.value
        com.example.data.FirebaseManager.savePost(newAdPost)
    }

    fun reactToPost(postId: String, reaction: ReactionType) {
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
        val newComment = Comment(
            id = "c_${UUID.randomUUID()}",
            postId = postId,
            author = customAuthor ?: _currentUser.value,
            text = text,
            timestamp = "Just now"
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val nextComments = post.comments + newComment
                post.copy(
                    comments = nextComments,
                    cloudCommentCount = maxOf(post.cloudCommentCount + 1, nextComments.size)
                )
            } else post
        }
        // Sync to cloud Firestore
        com.example.data.FirebaseManager.saveComment(postId, newComment)
    }

    fun replyToComment(postId: String, commentId: String, text: String, customAuthor: User? = null) {
        if (text.isBlank()) return
        val newReply = CommentReply(
            id = "cr_${UUID.randomUUID()}",
            author = customAuthor ?: _currentUser.value,
            text = text,
            timestamp = "Just now"
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        comment.copy(replies = comment.replies + newReply)
                    } else comment
                }
                post.copy(comments = updatedComments)
            } else post
        }
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
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(comments = post.comments.filter { it.id != commentId })
            } else post
        }
    }

    fun deleteReply(postId: String, commentId: String, replyId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        comment.copy(replies = comment.replies.filter { it.id != replyId })
                    } else comment
                }
                post.copy(comments = updatedComments)
            } else post
        }
    }

    fun boostPost(postId: String, budget: Int, days: Int) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    isBoosted = true,
                    boostStatus = "Sponsored",
                    boostBudget = budget,
                    boostDays = days,
                    isBoostActive = true
                )
            } else post
        }
    }

    fun reactToComment(postId: String, commentId: String, reaction: ReactionType) {
        var updatedCommentRef: Comment? = null
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
                post.copy(comments = updatedComments)
            } else post
        }
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
                post.copy(comments = updatedComments)
            } else post
        }
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
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(shareCount = post.shareCount + 1)
            } else post
        }
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
        val cover = audioTrack.coverArtUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800"
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
        val existingForUser = _posts.value.filter {
            it.author.id == user.id ||
                (user.username.isNotBlank() && it.author.username.equals(user.username, ignoreCase = true)) ||
                it.author.fullName.equals(user.fullName, ignoreCase = true)
        }
        if (existingForUser.size >= 3) return

        val sampleCommenter = _conversations.value.firstOrNull { it.friend.id != user.id }?.friend
            ?: SampleData.friendTanvir
        val defaultPosts = listOf(
            Post(
                id = "post_${user.id}_photo_1",
                author = user,
                timestamp = "২ ঘণ্টা আগে • 🌍",
                text = "আজকের সুন্দর বিকেলটা বন্ধুদের সাথে চমৎকার কাটলো! সবাইকে আন্তরিক ধন্যবাদ ❤️ #FotoFilm #Friendship",
                images = listOf("https://images.unsplash.com/photo-1540573133985-87b6da6d54a9?w=800"),
                isVideo = false,
                isReel = false,
                shareCount = 45,
                comments = listOf(
                    Comment(
                        id = "c_${user.id}_1",
                        postId = "post_${user.id}_photo_1",
                        author = sampleCommenter,
                        text = "অসাধারণ ছবি হয়েছে ভাই! ❤️🔥",
                        timestamp = "১ ঘণ্টা আগে"
                    )
                )
            ),
            Post(
                id = "post_${user.id}_video_2",
                author = user,
                timestamp = "গতকাল বিকাল ৫:৩০ • 🌍",
                text = "🎬 প্রকৃতি ও নদীর অপূর্ব দৃশ্য! ভিডিওটি দেখুন এবং শেয়ার করুন 🌿",
                images = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
                isVideo = true,
                videoTitle = "প্রকৃতি ও সূর্যাস্তের অপূর্ব দৃশ্য",
                videoDuration = "03:45",
                reachViews = "45.2K",
                shareCount = 89
            ),
            Post(
                id = "post_${user.id}_reel_3",
                author = user,
                timestamp = "২ দিন আগে • 🌍",
                text = "🔥 নতুন রিল ভিডিও: নিজের প্রতি বিশ্বাস রাখো, সফলতা আসবেই ইনশাআল্লাহ! 🌟 #Reels #Viral",
                images = listOf("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800"),
                isVideo = true,
                isReel = true,
                videoTitle = "সফলতার অনুপ্রেরণা",
                reachViews = "120K",
                musicInfo = "🎵 Original Audio - ${user.fullName}",
                shareCount = 210
            ),
            Post(
                id = "post_${user.id}_story_4",
                author = user,
                timestamp = "৪ দিন আগে • 🌍",
                text = "⏱️ ২৪ ঘণ্টার বিশেষ মুহূর্ত: নতুন প্রজেক্টের কাজ সফলভাবে শেষ হলো আলহামদুলিল্লাহ! 🤲",
                images = listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800"),
                isVideo = false,
                isReel = false,
                isFaceClip = true,
                shareCount = 18
            ),
            Post(
                id = "post_${user.id}_live_5",
                author = user,
                timestamp = "১ সপ্তাহ আগে • 🌍",
                text = "🔴 লাইভ ভিডিও রেকর্ড: সমসাময়িক বিষয় ও প্রযুক্তি নিয়ে লাইভ প্রশ্নোত্তর পর্ব",
                images = listOf("https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800"),
                isVideo = true,
                videoTitle = "লাইভ প্রশ্নোত্তর ও গল্প-আড্ডা",
                videoDuration = "45:10",
                reachViews = "89.5K",
                shareCount = 156
            )
        )

        val existingIds = _posts.value.map { it.id }.toSet()
        val toAppend = defaultPosts.filter { it.id !in existingIds }
        if (toAppend.isNotEmpty()) {
            _posts.value = _posts.value + toAppend
        }
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

    fun sendMessage(
        conversationId: String,
        text: String,
        mediaUrl: String? = null,
        isVideo: Boolean = false,
        replyTo: ChatMessage? = null
    ) {
        if (text.isBlank() && mediaUrl == null) return
        val currentConv = _conversations.value.find { it.id == conversationId }
        val isRecipientOnline = currentConv?.friend?.isOnline ?: true
        val deliveryStatus = if (isRecipientOnline) MessageDeliveryStatus.DELIVERED else MessageDeliveryStatus.SENT

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
            status = deliveryStatus
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

        // Interactive Auto-reply capability so conversation can continue back and forth seamlessly
        if (currentConv != null && !currentConv.friend.id.equals(_currentUser.value.id)) {
            viewModelScope.launch {
                delay(1600)
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
                    replyTo = newMsg,
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
                    // When a message comes to me, instantly jump this chat to position 1 on top!
                    _conversations.value = listOf(upd) + _conversations.value.filter { it.id != conversationId }
                }
            }
        }
    }

    fun sendVoiceMessage(
        conversationId: String,
        durationSeconds: Int,
        audioUrl: String? = null,
        replyTo: ChatMessage? = null
    ) {
        val safeDuration = if (durationSeconds > 0) durationSeconds else 5
        val currentConv = _conversations.value.find { it.id == conversationId }
        val isRecipientOnline = currentConv?.friend?.isOnline ?: true
        val deliveryStatus = if (isRecipientOnline) MessageDeliveryStatus.DELIVERED else MessageDeliveryStatus.SENT

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
            status = deliveryStatus
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

        // Always log Voice note to Permanent Delivery Vault
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
    }

    fun reactToChatMessage(conversationId: String, messageId: String, reaction: String) {
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
                        if (prevReaction != reaction) {
                            currentReactions[reaction] = (currentReactions[reaction] ?: 0) + 1
                            msg.copy(reactions = currentReactions, userReaction = reaction)
                        } else {
                            msg.copy(reactions = currentReactions, userReaction = null)
                        }
                    } else msg
                }
                val updated = conv.copy(messages = updatedMessages)
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
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

    private val _messageRequests = MutableStateFlow(
        listOf(
            User(
                id = "req_user_1",
                fullName = "Anika Tabassum",
                username = "anika_tab",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400"
            ),
            User(
                id = "req_user_2",
                fullName = "Tariqul Islam",
                username = "tariqul_it",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400"
            ),
            User(
                id = "req_user_3",
                fullName = "Mehedi Hasan",
                username = "mehedi_bd",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400"
            )
        )
    )
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
    }

    // Forward message to selected conversations (Screenshot 1: "Forward")
    fun forwardChatMessage(message: ChatMessage, targetConversationIds: List<String>) {
        val me = _currentUser.value
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
                    isFromMe = true,
                    status = MessageDeliveryStatus.SENT
                )
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

    // Calling System (Agora HD + Real-Time Firestore Signaling)
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
        val signal = _incomingCallSignal.value ?: return
        _incomingCallSignal.value = null
        com.example.data.FirebaseManager.updateCallSignalStatus(signal.callId, "ACCEPTED")
        val callerUser = allAppUsers.value.find { it.id == signal.callerId } ?: User(
            id = signal.callerId,
            fullName = signal.callerName,
            username = signal.callerName.lowercase().replace(" ", "_"),
            avatarUrl = signal.callerAvatar.ifBlank { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500" }
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
        val closingCallId = _activeCall.value?.callId.orEmpty()
        callTimerJob?.cancel()
        outgoingCallListener?.remove()
        outgoingCallListener = null
        if (updateRemote && closingCallId.isNotBlank()) {
            com.example.data.FirebaseManager.updateCallSignalStatus(closingCallId, "ENDED")
        }
        com.example.data.AgoraCallManager.leaveCall()
        _activeCall.value = null
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

    // Groups & Pages (৫ এডমিন ও ১৫ মডারেটর সাপোর্ট)
    fun createGroup(
        name: String,
        description: String,
        isPrivate: Boolean,
        coverUrl: String = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&auto=format&fit=crop&q=80",
        initialAdmins: List<User> = emptyList(),
        initialModerators: List<User> = emptyList()
    ): SocialGroup {
        val defaultAdmins = if (initialAdmins.isEmpty()) {
            listOf(_currentUser.value, SampleData.friendTanvir, SampleData.friendSamrat, SampleData.friendSadia, SampleData.friendHasanMiaHowlader).take(5)
        } else {
            (listOf(_currentUser.value) + initialAdmins).distinctBy { it.id }.take(5)
        }
        val defaultMods = if (initialModerators.isEmpty()) {
            listOf(
                SampleData.friendJannat, SampleData.friendManu, SampleData.friendRiya,
                SampleData.friendShunya, SampleData.friendUmor, SampleData.friendMdMohiuddin,
                SampleData.friendSahid, SampleData.friendJoyAhmed, SampleData.friendArifur,
                SampleData.friendNusrat, SampleData.friendRahim, SampleData.friendKajol,
                SampleData.friendShakib, SampleData.friendMim, SampleData.friendSultana
            ).filter { mod -> defaultAdmins.none { it.id == mod.id } }.take(15)
        } else {
            initialModerators.filter { mod -> defaultAdmins.none { it.id == mod.id } }.take(15)
        }

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

    // Live App Download Count state & simulation (১ জন, ৫০ জন, ... ক্রমবৃদ্ধি)
    private val _totalAppDownloadCount = MutableStateFlow(128)
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
        avatarUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80",
        coverUrl: String = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80"
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

    // ==========================================
    // Multi-Account / Switch Account Feature
    // ==========================================
    private val _userAccounts = MutableStateFlow(
        listOf(
            UserAccount(
                id = "acc_1",
                name = "Niyon Islam Sohag",
                handle = "@niyonislamsohag",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                isActive = true,
                notificationCount = 3
            ),
            UserAccount(
                id = "acc_2",
                name = "Sohag Official",
                handle = "@sohagofficial",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                isActive = false,
                notificationCount = 0
            ),
            UserAccount(
                id = "acc_3",
                name = "Niyon Tech BD",
                handle = "@niyontechbd",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                isActive = false,
                notificationCount = 1
            )
        )
    )
    val userAccounts: StateFlow<List<UserAccount>> = _userAccounts.asStateFlow()

    fun switchAccount(accountId: String) {
        val target = _userAccounts.value.find { it.id == accountId } ?: return
        _userAccounts.value = _userAccounts.value.map {
            it.copy(isActive = it.id == accountId)
        }
        if (target.id == "acc_1") {
            _currentUser.value = SampleData.currentUser.copy(
                avatarUrl = target.avatarUrl.ifBlank { SampleData.currentUser.avatarUrl },
                isOnline = _isActiveStatusEnabled.value
            )
        } else {
            _currentUser.value = _currentUser.value.copy(
                id = target.id,
                fullName = target.name,
                username = target.handle.removePrefix("@"),
                email = "${target.handle.removePrefix("@")}@fotofilm.app",
                avatarUrl = target.avatarUrl,
                isOnline = _isActiveStatusEnabled.value
            )
            closeRightManagerFida()
        }
    }

    fun addNewAccount(name: String, handle: String, avatarUrl: String = "") {
        val finalAvatar = avatarUrl.ifBlank {
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
        }
        val cleanHandle = if (handle.startsWith("@")) handle else "@$handle"
        val newAcc = UserAccount(
            id = "acc_${System.currentTimeMillis()}",
            name = name.ifBlank { "New FaceFeed User" },
            handle = cleanHandle.ifBlank { "@user_${System.currentTimeMillis() % 10000}" },
            avatarUrl = finalAvatar,
            isActive = false,
            notificationCount = 0
        )
        _userAccounts.value = _userAccounts.value + newAcc
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
                text = "আসসালামু আলাইকুম ও শুভকামনা! 🌸\nআমি ফিদা এআই (Fida AI)—আপনার FaceFeed ব্যক্তিগত স্মার্ট সহকারী। আপনি আমাকে যেকোনো বিষয়, তথ্য, পড়াশোনা, প্রযুক্তি, সাহিত্য বা দৈনন্দিন জীবনের প্রশ্ন করতে পারেন। আমি সবসময় ভালোবাসা, সম্মান ও আন্তরিকতার সাথে দ্রুত উত্তর দিতে প্রস্তুত। বলুন, আজ আপনাকে কিভাবে সাহায্য করতে পারি? 😊",
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
                "FotoFilm হলো একটি আধুনিক ও নিরাপদ সামাজিক যোগাযোগ মাধ্যম 🌟। এখানে আপনি ফটো, শর্ট ভিডিও (রিলস), লং ভিডিও, স্টোরি, রঙিন টেক্সট স্ট্যাটাস এবং লাইভ স্ট্রিমিং করতে পারবেন। এছাড়া ৩ ডট মেনু থেকে 'Switch account' দিয়ে একাধিক আইডি ব্যবহার করা এবং 'Fida Verified' এর মাধ্যমে সবুজ ভেরিফাইড টিক পাওয়ার সুবিধাও রয়েছে!"
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
                "FotoFilm-এ এখন নীল টিকের বদলে সম্মানজনক 'সবুজ ভেরিফাইড টিক (Green Verified Badge)' যুক্ত করা হয়েছে 🟢! ৩ ডট মেনু থেকে 'Fida Verified' অপশনে গিয়ে আপনি এক ক্লিকেই ভেরিফিকেশন সক্রিয় করতে পারেন।"
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

    private val _isFidaVerified = MutableStateFlow(true)
    val isFidaVerified: StateFlow<Boolean> = _isFidaVerified.asStateFlow()

    // 50,000 Free Verified Quota (50K) for Niyon Islam Sohag only
    val freeVerifiedQuotaTotal: Int = 50000
    private val _freeVerifiedQuotaRemaining = MutableStateFlow(49997)
    val freeVerifiedQuotaRemaining: StateFlow<Int> = _freeVerifiedQuotaRemaining.asStateFlow()

    private val _freeVerifiedGrantedUsers = MutableStateFlow<Set<String>>(setOf("user_tanvir", "user_me"))
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

    // NID Card Documents State
    private val _nidFrontUri = MutableStateFlow<String?>("https://images.unsplash.com/photo-1544717305-2782549b5136?w=600&auto=format&fit=crop&q=80")
    val nidFrontUri: StateFlow<String?> = _nidFrontUri.asStateFlow()

    private val _nidBackUri = MutableStateFlow<String?>("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=600&auto=format&fit=crop&q=80")
    val nidBackUri: StateFlow<String?> = _nidBackUri.asStateFlow()

    private val _isNidSubmitted = MutableStateFlow(true)
    val isNidSubmitted: StateFlow<Boolean> = _isNidSubmitted.asStateFlow()

    private val _nidApplicantName = MutableStateFlow("Niyon Islam Sohag")
    val nidApplicantName: StateFlow<String> = _nidApplicantName.asStateFlow()

    private val _nidNumber = MutableStateFlow("19985217489632145")
    val nidNumber: StateFlow<String> = _nidNumber.asStateFlow()

    // 12 Months Subscription State (Paid months at 500 BDT each)
    private val _paidMonths = MutableStateFlow<Set<String>>(setOf("January", "February", "March", "September"))
    val paidMonths: StateFlow<Set<String>> = _paidMonths.asStateFlow()

    // Payment Transaction Success Event message
    private val _paymentSuccessMessage = MutableStateFlow<String?>(null)
    val paymentSuccessMessage: StateFlow<String?> = _paymentSuccessMessage.asStateFlow()

    fun isCurrentUserNiyonIslamSohag(): Boolean {
        if (isCurrentOwnerAuthenticated) return true
        val user = _currentUser.value
        val name = user.fullName.trim().lowercase()
        val username = user.username.trim().lowercase()
        val email = user.email.trim().lowercase()
        return email == "smd224292@gmail.com" ||
            email == "nihonislamsohag@gmail.com" ||
            (name.contains("niyon") && name.contains("sohag")) ||
            username == "niyon_sohag" ||
            username == "niyonislamsohag"
    }

    fun isUserNiyonIslamSohag(user: User?): Boolean {
        if (user == null) return false
        val name = user.fullName.trim().lowercase()
        val username = user.username.trim().lowercase()
        val email = user.email.trim().lowercase()
        return user.id == "user_me" ||
            email == "smd224292@gmail.com" ||
            email == "nihonislamsohag@gmail.com" ||
            (name.contains("niyon") && name.contains("sohag")) ||
            username == "niyon_sohag" ||
            username == "niyonislamsohag"
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
        val targetAvatar = targetUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"

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
                currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
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
                currentBadgeStatus = "সক্রিয় ভেরিফাইড 🔵",
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
        _posts.value = _posts.value.filter { it.id != postId }
    }

    fun pinPost(postId: String) {
        val target = _posts.value.find { it.id == postId } ?: return
        _posts.value = listOf(target) + _posts.value.filter { it.id != postId }
    }

    fun movePostToArchive(postId: String) {
        _posts.value = _posts.value.filter { it.id != postId }
    }

    fun movePostToTrash(postId: String) {
        _posts.value = _posts.value.filter { it.id != postId }
    }

    fun editPostText(postId: String, newText: String) {
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(text = newText) else it
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
        val targetUser = _allAppUsers.value.find { it.id == userId } ?: return
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
        val targetRecord = _timedSuspensionRecords.value.find { it.id == recordId } ?: return
        val newActiveState = !targetRecord.isSuspendedActive
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
        val targetRecord = _timedSuspensionRecords.value.find { it.id == recordId }
        if (targetRecord != null) {
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

    // Fida Verified Review Applications Queue (Paid / Free Submissions)
    private val _verifiedApplications = MutableStateFlow<List<com.example.model.FidaVerifiedApplication>>(
        listOf(
            com.example.model.FidaVerifiedApplication(
                id = "vapp_1",
                userId = "user_nusrat",
                userName = "Nusrat Jahan",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
                userPhone = "01812345678",
                amountBdt = 500,
                paymentMethod = "bKash",
                senderNumber = "01812345678",
                trxId = "BK7894231",
                submittedDate = "০২ অক্টোবর ২০২৬, ১১:৪০ AM",
                status = "PENDING",
                nidOrDocText = "জাতীয় পরিচয়পত্র (NID): 1998269123456 • উভয় পিঠের ছবি ও সেলফি সাবমিট করা হয়েছে"
            ),
            com.example.model.FidaVerifiedApplication(
                id = "vapp_2",
                userId = "user_sadia",
                userName = "Sadia Islam",
                userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500",
                userPhone = "01998765432",
                amountBdt = 500,
                paymentMethod = "Nagad",
                senderNumber = "01998765432",
                trxId = "NGD5512903",
                submittedDate = "০১ অক্টোবর ২০২৬, ০৭:১৫ PM",
                status = "PENDING",
                nidOrDocText = "পাসপোর্ট নং: A02918451 • স্মার্টকার্ড যাচাই আবেদন"
            ),
            com.example.model.FidaVerifiedApplication(
                id = "vapp_3",
                userId = "user_samrat",
                userName = "MD Samrat",
                userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=500",
                userPhone = "01755123987",
                amountBdt = 500,
                paymentMethod = "Rocket",
                senderNumber = "01755123987",
                trxId = "RCK992147",
                submittedDate = "২৮ সেপ্টেম্বর ২০২৬, ০৩:০০ PM",
                status = "APPROVED",
                nidOrDocText = "জাতীয় পরিচয়পত্র ও ব্যবসায়িক ট্রেড লাইসেন্স অনুমোদিত"
            ),
            com.example.model.FidaVerifiedApplication(
                id = "vapp_rahim",
                userId = "user_rahim",
                userName = "আব্দুর রহিম (Rahim)",
                userAvatar = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500",
                userPhone = "01812345678",
                amountBdt = 500,
                paymentMethod = "bKash",
                senderNumber = "01812345678",
                trxId = "BK11012026",
                submittedDate = "০১/০১/২০২৬ (1/1/2026)",
                status = "PENDING",
                nidOrDocText = "জাতীয় পরিচয়পত্র (NID) ও স্মার্ট সেলফি কার্ড যাচাইয়ের জন্য জমা"
            )
        )
    )
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
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
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
            } else post
        }
    }

    fun togglePostBoostActive(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newActive = !post.isBoostActive
                post.copy(
                    isBoostActive = newActive,
                    isBoosted = newActive
                )
            } else post
        }
    }

    fun cancelPostBoost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    isBoosted = false,
                    isFreePromotedByNiyon = false,
                    isBoostActive = false,
                    boostStatus = null,
                    boostTargetReach = null,
                    boostAudience = null
                )
            } else post
        }
    }

    // ==========================================
    // Option 21: Right Manager Fida - অ্যান্টি-হ্যাক ও অনুপ্রবেশ মনিটর (Anti-Hack Logs)
    // ==========================================
    private val _intrusionLogs = MutableStateFlow<List<IntrusionLog>>(
        listOf(
            IntrusionLog(
                ipAddress = "197.234.12.89",
                deviceModel = "Linux / Kali Pentest 64-bit",
                attackerName = "ডার্কনেট অনুপ্রবেশকারী #409",
                attackVector = "Reverse Engineering & Signature Bypass (Mod APK Exploit)",
                timestamp = "আজ দুপুর ০২:১৫ মিনিট",
                targetArea = "Right Manager Fida সিকিউরিটি গেটওয়ে",
                severity = "CRITICAL 🔴",
                status = "AUTO-BLOCKED & IP BANNED 🛡️",
                defenseLog = "MD5/SHA256 সিগনেচার যাচাই ফেইল্ড। আক্রমণকারীর আইপি চিরতরে ব্ল্যাকলিস্ট করা হয়েছে।"
            ),
            IntrusionLog(
                ipAddress = "103.145.78.21",
                deviceModel = "Rooted OnePlus 9 Pro (Magisk/Xposed Hook)",
                attackerName = "সন্দেহভাজন হ্যাকার (UID: 8812)",
                attackVector = "Admin Privileges Escalation Injection",
                timestamp = "আজ সকাল ১১:৪০ মিনিট",
                targetArea = "Admin 10-Slot Controller",
                severity = "CRITICAL 🔴",
                status = "BLOCKED & DEVICE ID BANNED 🛡️",
                defenseLog = "রুট ডিভাইসের আন-অথরাইজড অ্যাডমিন এক্সেস রিকুয়েস্ট তাৎক্ষণিক রিজেক্ট করা হয়েছে।"
            ),
            IntrusionLog(
                ipAddress = "185.220.101.5",
                deviceModel = "TOR Exit Node / Proxy Relay",
                attackerName = "বটনেট রিলে #99",
                attackVector = "Payment Webhook Spoofing (bKash/Nagad fake TrxID)",
                timestamp = "গতকাল রাত ১০:০৮ মিনিট",
                targetArea = "Fida Verified Payment Gateway",
                severity = "HIGH 🟠",
                status = "BLOCKED & PROXY DROPPED 🛡️",
                defenseLog = "এনক্রিপ্টেড পেমেন্ট টোকেন মিসম্যাচ। জাল ট্রানজেকশন আক্রমণ প্রতিহত করা হয়েছে।"
            ),
            IntrusionLog(
                ipAddress = "45.134.22.190",
                deviceModel = "Python-urllib Automated Script",
                attackerName = "অজ্ঞাত ডাটা স্ক্র্যাপার",
                attackVector = "Messenger Private Vault Brute Force Scraper",
                timestamp = "গতকাল বিকাল ০৪:২২ মিনিট",
                targetArea = "Permanent Delivery Vault API",
                severity = "CRITICAL 🔴",
                status = "BLOCKED & RATE-LIMITED 🛡️",
                defenseLog = "জিরো-ট্রাস্ট এনক্রিপশন সক্রিয়। বট স্ক্রিপ্ট স্থায়ী ব্যান ও ট্রাফিক ড্রপ।"
            )
        )
    )
    val intrusionLogs: StateFlow<List<IntrusionLog>> = _intrusionLogs.asStateFlow()

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
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedReactions = post.reactionCounts.toMutableMap().apply {
                    put(ReactionType.LOVE, (get(ReactionType.LOVE) ?: 500) + 3800)
                    put(ReactionType.HUNDRED, (get(ReactionType.HUNDRED) ?: 200) + 1500)
                }
                post.copy(
                    isBoosted = true,
                    isFreePromotedByNiyon = true,
                    isBoostActive = true,
                    boostTargetReach = targetViews,
                    reachViews = if (post.reachViews.contains("M")) post.reachViews else "2.1M",
                    boostDays = days,
                    boostAudience = "ভিআইপি ভিডিও ফিড ও রিলস ট্রেন্ডিং (বাংলাদেশ ও বিশ্বব্যাপী)",
                    boostStatus = "সক্রিয় 🔥 (Niyon Islam Sohag ফ্রী ভিডিও বুস্টার)",
                    boostBudget = 0,
                    boostTrxId = "NIYON_VIDEO_BOOST_VIP",
                    reactionCounts = updatedReactions,
                    shareCount = post.shareCount + 420
                )
            } else post
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCloudSync(keepLoggedIn = true)
    }
}
