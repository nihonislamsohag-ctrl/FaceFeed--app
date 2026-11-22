package com.example.model

data class SocialGroup(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val memberCount: Int,
    val isPrivate: Boolean,
    val isJoined: Boolean = false,
    val creatorName: String,
    val isActive: Boolean = true,
    val createdAt: String = "০১/০১/২০২৬ (1/1/2026)",
    val admins: List<User> = emptyList(),
    val moderators: List<User> = emptyList()
)

data class SocialPage(
    val id: String,
    val name: String,
    val category: String,
    val avatarUrl: String,
    val coverUrl: String,
    val followerCount: Int,
    val isLiked: Boolean = false,
    val isVerified: Boolean = true,
    val creatorName: String = "Niyon Islam Sohag",
    val isActive: Boolean = true,
    val createdAt: String = "০১/০১/২০২৬ (1/1/2026)"
)

data class FidaVerifiedApplication(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val userPhone: String,
    val amountBdt: Int = 500,
    val paymentMethod: String = "bKash", // "bKash", "Nagad", "Rocket"
    val senderNumber: String,
    val trxId: String,
    val submittedDate: String = "০১/০১/২০২৬ (1/1/2026)",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val nidOrDocText: String = "জাতীয় পরিচয়পত্র (NID) ও ছবি যাচাই সম্পন্ন",
    val isFreeQuota: Boolean = false
)

data class HangoutRoomControlEntry(
    val roomKey: String,
    val title: String,
    val creatorName: String,
    val avatarUrl: String,
    val memberCount: Int,
    val category: String,
    val createdAt: String = "আজ সক্রিয়",
    val isActive: Boolean = true
)

