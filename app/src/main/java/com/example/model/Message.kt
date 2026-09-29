package com.example.model

enum class MessageDeliveryStatus {
    SENT,       // ছেন্ড (Recipient is offline: grey circle with single tick)
    DELIVERED,  // ডেলিভারি (Recipient is online: filled blue circle with white tick)
    SEEN        // দেখা হয়েছে (Recipient seen: avatar/seen indicator)
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val mediaUrl: String? = null,
    val isMediaVideo: Boolean = false,
    val isFromMe: Boolean = false,
    val replyTo: ChatMessage? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val userReaction: String? = null,
    val isAudioVoice: Boolean = false,
    val audioDurationSeconds: Int = 0,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.DELIVERED
)

data class Conversation(
    val id: String,
    val friend: User,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val groupMembers: List<User> = emptyList(),
    val lastMessage: String,
    val lastTime: String,
    val unreadCount: Int = 0,
    val messages: List<ChatMessage> = emptyList()
)

enum class CallType {
    AUDIO, VIDEO, GROUP
}

data class ActiveCallState(
    val isActive: Boolean = false,
    val callType: CallType = CallType.AUDIO,
    val user: User? = null,
    val groupName: String? = null,
    val groupMembers: List<User> = emptyList(),
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isVideoFrontCamera: Boolean = true
)
