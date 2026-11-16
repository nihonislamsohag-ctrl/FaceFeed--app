package com.example.model

enum class BoostGoal(val titleBn: String, val titleEn: String, val icon: String, val description: String) {
    MORE_REACH("পোস্ট ও ভিডিও রিচ বৃদ্ধি", "More Post & Video Reach", "🎯", "বেশি সংখ্যক মানুষের ফিডে পোস্টটি তুলে ধরা হবে"),
    MORE_VIEWS("ভিডিও ভিউ বৃদ্ধি", "More Video Views", "👁️", "ভিডিও প্লে ও ওয়াচ টাইম দ্রুত বাড়ানোর জন্য"),
    MORE_MESSAGES("মেসেজ ও কমেন্ট ইনবক্স", "More Messages & Engagement", "💬", "গ্রাহকরা সরাসরি মেসেজ ও যোগাযোগ করতে পারবে"),
    MORE_FOLLOWERS("পেজ/প্রোফাইল ফলোয়ার বৃদ্ধি", "More Page Followers", "👥", "পেজে নতুন লাইক ও ফলোয়ার আকর্ষণ করা")
}

data class BoostPackage(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val durationDays: Int,
    val priceBdt: Int,
    val estimatedReach: String,
    val likesCount: Int = 10000,
    val followersCount: Int = 3000,
    val viewsCount: Int = 50000,
    val hasTenReactions: Boolean = true,
    val isPopular: Boolean = false,
    val badge: String? = null
)

data class BoostPaymentMethod(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val colorHex: Long,
    val defaultNumber: String,
    val accountType: String = "Personal / Send Money"
)

data class BoostTransaction(
    val id: String,
    val postId: String,
    val postTitleOrText: String,
    val isVideo: Boolean,
    val authorName: String,
    val paymentMethod: String,
    val senderNumber: String,
    val trxId: String,
    val amountBdt: Int,
    val durationDays: Int,
    val estimatedReach: String,
    val goal: String,
    val status: String = "ACTIVE", // "ACTIVE", "PENDING", "COMPLETED"
    val createdAt: String = "০১/০১/২০২৬ (1/1/2026)"
)
