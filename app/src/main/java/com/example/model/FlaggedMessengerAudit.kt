package com.example.model

/**
 * Flagged Messenger Message / Content Audit for 1 Crore Users Monitor
 * (মেসেঞ্জার খারাপ ছবি, ভিডিও, ভয়েস ও চ্যাট নজরদারি)
 */
enum class FlaggedMessageType(val banglaLabel: String, val iconEmoji: String) {
    BAD_PHOTO("খারাপ / আপত্তিকর ছবি", "🔞📷"),
    BAD_VIDEO("খারাপ / অশ্লীল ভিডিও", "🔞🎬"),
    ABUSIVE_VOICE("বাজে / গালিগালাজপূর্ণ ভয়েস", "🎤⚠️"),
    HARMFUL_TEXT("আপত্তিকর / হুমকি চ্যাট", "💬🚫")
}

enum class FlaggedAuditStatus(val label: String) {
    PENDING("নজরদারিতে আছে"),
    DELETED("মেসেজ ডিলেট করা হয়েছে"),
    SENDER_BANNED("প্রেরক ব্যান করা হয়েছে"),
    WARNING_SENT("সতর্কবার্তা পাঠানো হয়েছে")
}

data class FlaggedMessengerAudit(
    val id: String,
    val senderUser: User,
    val recipientUser: User,
    val category: FlaggedMessageType,
    val contentPreview: String,
    val mediaUrl: String? = null,
    val isAudioVoice: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val timestamp: String,
    val aiSafetyDetection: String,
    val status: FlaggedAuditStatus = FlaggedAuditStatus.PENDING,
    val severity: String = "উচ্চ ঝুঁকি (Critical)",
    val isBlurredByDefault: Boolean = true
)
