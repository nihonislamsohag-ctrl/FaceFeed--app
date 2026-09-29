package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.*
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
    private val _isFreeMode = MutableStateFlow(true)
    val isFreeMode: StateFlow<Boolean> = _isFreeMode.asStateFlow()

    private val _currentSimOperator = MutableStateFlow(SimOperator.GRAMEENPHONE)
    val currentSimOperator: StateFlow<SimOperator> = _currentSimOperator.asStateFlow()

    private val _showFreeModeVideoWarning = MutableStateFlow(false)
    val showFreeModeVideoWarning: StateFlow<Boolean> = _showFreeModeVideoWarning.asStateFlow()

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

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Posts
    private val _posts = MutableStateFlow(SampleData.initialPosts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Stories
    private val _stories = MutableStateFlow(SampleData.initialStories)
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _viewingStory = MutableStateFlow<Story?>(null)
    val viewingStory: StateFlow<Story?> = _viewingStory.asStateFlow()

    // Conversations
    private val _conversations = MutableStateFlow(SampleData.initialConversations)
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeChat = MutableStateFlow<Conversation?>(null)
    val activeChat: StateFlow<Conversation?> = _activeChat.asStateFlow()

    // Calling state
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()
    private var callTimerJob: Job? = null

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

    // Notifications
    private val _notifications = MutableStateFlow(SampleData.initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Create post dialog state
    private val _showCreatePostDialog = MutableStateFlow(false)
    val showCreatePostDialog: StateFlow<Boolean> = _showCreatePostDialog.asStateFlow()

    private val _pendingPostImages = MutableStateFlow<List<String>>(emptyList())
    val pendingPostImages: StateFlow<List<String>> = _pendingPostImages.asStateFlow()

    fun openCreatePostWithImages(images: List<String>) {
        _pendingPostImages.value = images
        _showCreatePostDialog.value = true
    }

    fun clearPendingPostImages() {
        _pendingPostImages.value = emptyList()
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

    // Auth functions
    fun login(emailOrUser: String, pass: String): Boolean {
        if (emailOrUser.isNotBlank() && pass.isNotBlank()) {
            _isLoggedIn.value = true
            _showAuthDialog.value = false
            return true
        }
        return false
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        gender: String,
        country: String,
        birthDate: String,
        pass: String,
        avatarUrl: String?
    ): Boolean {
        if (fullName.isNotBlank() && pass.isNotBlank()) {
            val newUser = _currentUser.value.copy(
                fullName = fullName,
                username = if (username.isNotBlank()) username else fullName.lowercase().replace(" ", "_"),
                email = email,
                gender = gender,
                country = country,
                birthDate = birthDate,
                avatarUrl = avatarUrl ?: _currentUser.value.avatarUrl
            )
            _currentUser.value = newUser
            _isLoggedIn.value = true
            _showAuthDialog.value = false
            return true
        }
        return false
    }

    fun loginWithAppleId() {
        val appleUser = _currentUser.value.copy(
            fullName = "Niyon Islam (Apple ID)",
            username = "niyon_apple",
            email = "apple.user@icloud.com",
            isVerified = true
        )
        _currentUser.value = appleUser
        _isLoggedIn.value = true
        _showAuthDialog.value = false
    }

    fun logout() {
        _isLoggedIn.value = false
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
        _currentUser.value = _currentUser.value.copy(note = newNote)
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
        audioTrack: PostAudioTrack? = null
    ) {
        val newPost = Post(
            id = "post_${UUID.randomUUID()}",
            author = _currentUser.value,
            timestamp = "Just now • 🌐 Public",
            title = title?.takeIf { it.isNotBlank() },
            text = text,
            hashtags = hashtags.take(10), // Limit to 10 hashtags
            images = images.take(15),     // Limit to 15 photos
            isVideo = isVideo,
            videoTitle = videoTitle,
            isReel = isReel,
            reachViews = "1.5K",
            reactionCounts = mapOf(ReactionType.LIKE to 1),
            audioTrack = audioTrack,
            musicInfo = audioTrack?.let { "${it.title} • ${it.artist}" }
        )
        _posts.value = listOf(newPost) + _posts.value
        _showCreatePostDialog.value = false

        // Update user post count
        _currentUser.value = _currentUser.value.copy(postCount = _currentUser.value.postCount + 1)
    }

    fun reactToPost(postId: String, reaction: ReactionType) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val currentReaction = post.userReaction
                val updatedCounts = post.reactionCounts.toMutableMap()

                if (currentReaction == reaction) {
                    // Remove reaction
                    val count = updatedCounts[reaction] ?: 1
                    if (count <= 1) updatedCounts.remove(reaction) else updatedCounts[reaction] = count - 1
                    post.copy(userReaction = null, reactionCounts = updatedCounts)
                } else {
                    // Change reaction
                    if (currentReaction != null) {
                        val oldCount = updatedCounts[currentReaction] ?: 1
                        if (oldCount <= 1) updatedCounts.remove(currentReaction) else updatedCounts[currentReaction] = oldCount - 1
                    }
                    val newCount = (updatedCounts[reaction] ?: 0) + 1
                    updatedCounts[reaction] = newCount
                    post.copy(userReaction = reaction, reactionCounts = updatedCounts)
                }
            } else post
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val newComment = Comment(
            id = "c_${UUID.randomUUID()}",
            postId = postId,
            author = _currentUser.value,
            text = text,
            timestamp = "Just now"
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(comments = post.comments + newComment)
            } else post
        }
    }

    fun replyToComment(postId: String, commentId: String, text: String) {
        if (text.isBlank()) return
        val newReply = CommentReply(
            id = "cr_${UUID.randomUUID()}",
            author = _currentUser.value,
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
    }

    fun sharePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(shareCount = post.shareCount + 1)
            } else post
        }
    }

    // Story
    fun addStory(mediaUrl: String, caption: String = "") {
        val newStory = Story(
            id = "story_${UUID.randomUUID()}",
            author = _currentUser.value,
            mediaUrl = mediaUrl,
            timestamp = "Just now",
            caption = caption
        )
        _stories.value = listOf(newStory) + _stories.value
    }

    fun openStoryViewer(story: Story) {
        _viewingStory.value = story
    }

    fun closeStoryViewer() {
        _viewingStory.value = null
    }

    // Chat & Messaging
    fun openChat(conversation: Conversation) {
        _activeChat.value = conversation
    }

    fun closeChat() {
        _activeChat.value = null
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
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val updated = conv.copy(
                    lastMessage = if (text.isNotBlank()) text else if (isVideo) "📹 Video" else "📷 Photo",
                    lastTime = "Just now",
                    messages = conv.messages + newMsg
                )
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
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
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                val updated = conv.copy(
                    lastMessage = "🎤 অডিও ভয়েস (${safeDuration}s)",
                    lastTime = "Just now",
                    messages = conv.messages + newMsg
                )
                if (_activeChat.value?.id == conversationId) {
                    _activeChat.value = updated
                }
                updated
            } else conv
        }
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

    // Calling System
    fun startAudioCall(user: User) {
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = CallType.AUDIO,
            user = user,
            durationSeconds = 0
        )
        startCallTimer()
    }

    fun startVideoCall(user: User) {
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = CallType.VIDEO,
            user = user,
            durationSeconds = 0
        )
        startCallTimer()
    }

    fun startGroupCall(groupName: String, members: List<User>) {
        _activeCall.value = ActiveCallState(
            isActive = true,
            callType = CallType.GROUP,
            groupName = groupName,
            groupMembers = members,
            durationSeconds = 0
        )
        startCallTimer()
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
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleCameraFlip() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoFrontCamera = !it.isVideoFrontCamera) }
    }

    fun endCall() {
        callTimerJob?.cancel()
        _activeCall.value = null
    }

    // Friend requests & 5000 friend auto-follow
    fun acceptFriendRequest(user: User) {
        _pendingRequests.value = _pendingRequests.value.filter { it.id != user.id }
        _currentUser.value = _currentUser.value.copy(
            friendCount = _currentUser.value.friendCount + 1
        )
    }

    fun declineFriendRequest(user: User) {
        _pendingRequests.value = _pendingRequests.value.filter { it.id != user.id }
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
    fun createGroup(name: String, description: String, isPrivate: Boolean) {
        val newGroup = SocialGroup(
            id = "grp_${UUID.randomUUID()}",
            name = name,
            description = description,
            coverUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&auto=format&fit=crop&q=80",
            memberCount = 1,
            isPrivate = isPrivate,
            isJoined = true,
            creatorName = _currentUser.value.fullName
        )
        _groups.value = listOf(newGroup) + _groups.value
    }

    fun toggleJoinGroup(groupId: String) {
        _groups.value = _groups.value.map { grp ->
            if (grp.id == groupId) grp.copy(isJoined = !grp.isJoined) else grp
        }
    }

    fun createPage(name: String, category: String) {
        val newPage = SocialPage(
            id = "page_${UUID.randomUUID()}",
            name = name,
            category = category,
            avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80",
            coverUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80",
            followerCount = 1,
            isLiked = true
        )
        _pages.value = listOf(newPage) + _pages.value
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
        instagram: String
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
            instagramHandle = instagram
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
        _currentUser.value = _currentUser.value.copy(
            fullName = target.name,
            username = target.handle.removePrefix("@"),
            avatarUrl = target.avatarUrl
        )
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
        val lower = prompt.lowercase()
        return when {
            lower.contains("কেমন") || lower.contains("how are you") -> {
                "আলহামদুলিল্লাহ, আপনার দোয়ায় ও ভালোবাসায় আমি অনেক ভালো আছি প্রিয় ভাইয়া! 🌸 আশা করি আপনিও সুস্থ ও সুন্দর আছেন। আপনার দিনটি আনন্দময় হোক। আপনি কেমন আছেন?"
            }
            lower.contains("facefeed") || lower.contains("অ্যাপ") || lower.contains("ফিচার") -> {
                "FaceFeed হলো একটি আধুনিক ও নিরাপদ সামাজিক যোগাযোগ মাধ্যম 🌟। এখানে আপনি ফটো, শর্ট ভিডিও (রিলস), লং ভিডিও, স্টোরি, রঙিন টেক্সট স্ট্যাটাস এবং লাইভ স্ট্রিমিং করতে পারবেন। এছাড়া ৩ ডট মেনু থেকে 'Switch account' দিয়ে একাধিক আইডি ব্যবহার করা এবং 'Fida Verified' এর মাধ্যমে সবুজ ভেরিফাইড টিক পাওয়ার সুবিধাও রয়েছে!"
            }
            lower.contains("বাংলাদেশ") || lower.contains("bangladesh") -> {
                "বাংলাদেশ আমাদের অহংকার ও ভালোবাসার সবুজ মাতৃভূমি 🇧🇩। ১৯৭১ সালের মহান মুক্তিযুদ্ধের মাধ্যমে লাখো শহীদের আত্মত্যাগে আমরা পেয়েছি লাল-সবুজের পতাকা। এর প্রাকৃতিক রূপ ও মানুষের আন্তরিকতা বিশ্বের অন্যতম সুন্দর বিষয়। আপনি বাংলাদেশ নিয়ে কোনো নির্দিষ্ট ইতিহাস বা তথ্য জানতে চান?"
            }
            lower.contains("স্ট্যাটাস") || lower.contains("ক্যাপশন") || lower.contains("status") -> {
                "আপনার জন্য একটি হৃদয়স্পর্শী ও পজিটিভ স্ট্যাটাস লিখে দিচ্ছি:\n\n\"জীবনের প্রতিটি ধাপে ধৈর্য আর সততাই আসল শক্তি। মানুষের সাথে ভালো ব্যবহার করুন, কারণ আপনার একটি সুন্দর কথাই হয়তো কারো মন ভালো করে দিতে পারে। শুভকামনা সবার জন্য! 🌿✨ #FaceFeed #PositiveVibes\""
            }
            lower.contains("ভালোবাসা") || lower.contains("ব্যবহার") || lower.contains("উপদেশ") || lower.contains("কষ্ট") -> {
                "অত্যন্ত সুন্দর একটি কথা বলেছেন প্রিয় ভাইয়া 💖। মানুষের জীবনে সবচেয়ে দামি অলংকার হলো তার নম্রতা ও মিষ্টি ব্যবহার। কাউকে কোনোদিন কথায় বা কাজে কষ্ট না দিয়ে ক্ষমা ও ভালোবাসার হাত বাড়িয়ে দেওয়াই প্রকৃত মহত্ত্ব। পবিত্র ধর্মেও বলা হয়েছে—'একটি মিষ্টি কথা ও সুন্দর হাসিও সদকার সমতুল্য'।"
            }
            lower.contains("আইডি") || lower.contains("switch") || lower.contains("একাউন্ট") -> {
                "আপনি ৩ ডট মেনুতে গিয়ে 'Switch account' অপশনে চাপ দিলে আপনার সবগুলো তৈরি করা আইডি দেখতে পাবেন। এছাড়া নিচের '+ Add account' অপশনে চাপ দিয়ে খুব সহজেই আরেকটি নতুন অ্যাকাউন্ট যোগ বা সুইচ করতে পারবেন!"
            }
            lower.contains("সবুজ") || lower.contains("verified") || lower.contains("ভেরিফাইড") -> {
                "FaceFeed-এ এখন নীল টিকের বদলে সম্মানজনক 'সবুজ ভেরিফাইড টিক (Green Verified Badge)' যুক্ত করা হয়েছে 🟢! ৩ ডট মেনু থেকে 'Fida Verified' অপশনে গিয়ে আপনি এক ক্লিকেই ভেরিফিকেশন সক্রিয় করতে পারেন।"
            }
            else -> {
                "প্রিয় সুহৃদ, আপনার প্রশ্নটি অত্যন্ত সুন্দর ও গুরুত্বপূর্ণ 🌸। \"$prompt\" সম্পর্কে জেনে রাখুন: জীবনে প্রতিটি জ্ঞান ও তথ্যই আমাদের সামনে এগিয়ে নিয়ে যায়। সবসময় সততা ও ভালোবাসার সাথে ইতিবাচক চিন্তা বজায় রাখলে যেকোনো ক্ষেত্রে সাফল্য অর্জন সম্ভব। আপনার যদি এ বিষয়ে আরও কোনো তথ্য প্রয়োজন হয়, নিঃসঙ্কোচে আমাকে বলুন, আমি অত্যন্ত আন্তরিকতার সাথে উত্তর দিতে প্রস্তুত আছি!"
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

    // 3,000 Free Verified Quota for Niyon Islam Sohag only
    val freeVerifiedQuotaTotal: Int = 3000
    private val _freeVerifiedQuotaRemaining = MutableStateFlow(2997)
    val freeVerifiedQuotaRemaining: StateFlow<Int> = _freeVerifiedQuotaRemaining.asStateFlow()

    private val _freeVerifiedGrantedUsers = MutableStateFlow<Set<String>>(setOf("user_tanvir", "user_me"))
    val freeVerifiedGrantedUsers: StateFlow<Set<String>> = _freeVerifiedGrantedUsers.asStateFlow()

    // Right Manager Fida Admin Panel (Exclusively for Niyon Islam Sohag)
    private val _showRightManagerFidaDialog = MutableStateFlow(false)
    val showRightManagerFidaDialog: StateFlow<Boolean> = _showRightManagerFidaDialog.asStateFlow()

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
        val name = _currentUser.value.fullName.lowercase()
        return _currentUser.value.id == "user_me" || name.contains("niyon")
    }

    fun openFidaVerified() {
        _showFidaVerifiedDialog.value = true
    }

    fun closeFidaVerified() {
        _showFidaVerifiedDialog.value = false
    }

    fun openRightManagerFida() {
        if (isCurrentUserNiyonIslamSohag()) {
            _showRightManagerFidaDialog.value = true
        }
    }

    fun closeRightManagerFida() {
        _showRightManagerFidaDialog.value = false
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

    // Revoke Verified status
    fun revokeVerified(userId: String) {
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
}
