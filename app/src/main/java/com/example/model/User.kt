package com.example.model

data class FollowerUser(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String,
    val mutualFriendsCount: Int = 0,
    val isFollowingBack: Boolean = false,
    val isVerified: Boolean = false
)

data class ProfileFriend(
    val id: String,
    val name: String,
    val username: String = "",
    val avatarUrl: String,
    val mutualFriendsCount: String = "0 mutual friend",
    val isOnline: Boolean = true
)

data class User(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val bio: String = "Alhamdulillah for everything ✨ | Excited to join FotoFilm",
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenMinutesAgo: Int = 0,
    val friendCount: Int = 5000,
    val followerCount: Int = 50,
    val followingCount: Int = 5,
    val postCount: Int = 479,
    val note: String = "",
    val gender: String = "পুরুষ",
    val country: String = "বাংলাদেশ 🇧🇩",
    val birthDate: String = "01/05/2004",
    val isFriend: Boolean = false,
    val hasSentFriendRequest: Boolean = false,
    val hasReceivedFriendRequest: Boolean = false,
    val isFollowing: Boolean = false,
    val category: String = "Digital creator",
    val workplace: String = "Digital creator",
    val livesIn: String = "Dhaka, Bangladesh",
    val fromCity: String = "Dhaka, Bangladesh",
    val school: String = "লাউপাডা সাগর সৈকত মাধ্যমিক বিদ্যালয়",
    val college: String = "Taltali Degree College .তালতলী ডিগ্রী কলেজ",
    val instagramHandle: String = "niyonislamsohag",
    val website: String = "niyonislamsohag.com",
    val joinedDate: String = "Joined May 2020",
    val relationshipStatus: String = "Single",
    val accountCreationDate: String = "০১/০১/২০২৬ (1/1/2026)",
    val isAccountActive: Boolean = true,
    val isFidaVerifiedFreeGranted: Boolean = false,
    val suspensionReason: String? = null,
    val suspensionDuration: String? = null,
    val suspensionDate: String? = null,
    val suspensionCustomNote: String? = null,
    val avatarFrame: String? = null
) {
    val isMaxFriendsReached: Boolean
        get() = friendCount >= 5000
}

data class UserSuspensionRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val userUsername: String,
    val userAvatar: String,
    val durationText: String, // যেমন: "৭ দিনের"
    val durationIndex: Int, // 1 to 10
    val reasonNote: String, // লেখার অপশন - অপরাধ বিবরণ
    val suspendedDateFormatted: String, // তারিখ সহ
    val isSuspendedActive: Boolean = true // অন / অফ বাটন
)

data class GalleryMediaItem(
    val id: String,
    val url: String,
    val title: String,
    val category: String,
    val isVideo: Boolean = false,
    val duration: String? = null
)

data class LiveCommentItem(
    val id: String,
    val authorName: String,
    val avatarUrl: String,
    val comment: String
)

data class AdminSlot(
    val slotNumber: Int, // 1 to 10
    val user: User?,
    val roleTitle: String = "সহকারী অ্যাডমিন",
    val isSuperAdmin: Boolean = false,
    val assignedDate: String = "০১/০১/২০২৬ (1/1/2026)"
)

data class CyberOffenderReport(
    val id: String,
    val userId: String,
    val userName: String,
    val userUsername: String,
    val userAvatar: String,
    val reportedComment: String,
    val targetPostTitle: String,
    val timestamp: String = "০১/০১/২০২৬ (1/1/2026)",
    val nationalId: String = "১৯৯৪২৬৯২০১৯২৮৩৭৪",
    val phoneNumber: String = "+৮৮০ ১৭১২-৩৪৫৬৭৮",
    val ipAddress: String = "103.205.71.18",
    val deviceModel: String = "Samsung Galaxy A52 (Android 14)",
    val location: String = "ঢাকা, বাংলাদেশ (আইপি ট্রেসড)",
    val legalViolation: String = "সাইবার নিরাপত্তা আইন ২০২৩ ধারা ২৫ ও ২৯ (অনলাইন মানহানি, কুরুচিপূর্ণ মন্তব্য ও সাইবার অবমাননা)",
    val status: String = "UNDER_REVIEW" // "UNDER_REVIEW", "LEGAL_NOTICE_SENT", "PERMANENT_BANNED"
)

data class AppUpdateBroadcast(
    val id: String,
    val version: String,
    val title: String,
    val description: String,
    val appLogoUrl: String = "",
    val ownerAvatar: String = "",
    val ownerName: String = "Niyon Islam Sohag",
    val releaseDate: String = "০১/০১/২০২৬ (1/1/2026)",
    val isActive: Boolean = true
)

data class GrantInstanceDetail(
    val instanceNumber: Int, // ১, ২, ৩ ...
    val date: String, // যেমন: "০৫ অক্টোবর ২০২৬"
    val month: String, // যেমন: "অক্টোবর ২০২৬"
    val time: String = "১০:১৫ AM",
    val note: String = "৩,০০০ কোটা থেকে প্রধান ওনার কর্তৃক অনুমোদিত"
)

data class FreeVerifiedGrantRecord(
    val id: String,
    val recipientUserId: String,
    val recipientName: String,
    val recipientUsername: String,
    val recipientAvatar: String,
    val grantCountTimes: Int, // ১ জনকে কতো বার দিছি (যেমন: ২ বার, ৩ বার)
    val grantMonth: String, // কোন মাসে দিছি (যেমন: "অক্টোবর ২০২৬", "সেপ্টেম্বর ২০২৬")
    val grantDateFormatted: String, // তারিখ সহ (যেমন: "০৫ অক্টোবর ২০২৬, সকাল ১০:১৫ AM")
    val historyInstances: List<GrantInstanceDetail> = emptyList(),
    val quotaDeducted: Int = 1,
    val remarks: String = "৩,০০০ ফ্রী কোটা থেকে সরাসরি অনুমোদিত"
)

data class VerificationInstanceRecord(
    val instanceNumber: Int,
    val date: String, // যেমন: "০২ অক্টোবর ২০২৬"
    val month: String, // যেমন: "অক্টোবর ২০২৬"
    val time: String = "১১:৪৫ AM",
    val source: String = "প্রধান প্রতিষ্ঠাতা ও ওনার নিয়ন ইসলাম সোহাগ",
    val method: String = "ফ্রী কোটা অনুমোদন"
)

data class UserVerificationAuditEntry(
    val id: String,
    val userId: String,
    val userName: String,
    val userUsername: String,
    val userAvatar: String, // তারিখ ও প্রোফাইল আইকন সহ
    val totalTimesVerifiedTaken: Int, // আমার কাছ থেকে ১ জনে কতো বার ভেরিফাই নিছে (যেমন: ৩ বার)
    val currentBadgeStatus: String = "সক্রিয় ভেরিফাইড ✅",
    val historyInstances: List<VerificationInstanceRecord> = emptyList()
)

data class FakeIdReport(
    val id: String,
    val attemptedName: String,
    val attemptedUsername: String,
    val attemptedEmail: String,
    val selectedGender: String,
    val detectedSignal: String,
    val violationType: String, // "STOLEN_PHOTO", "MALE_TO_FEMALE_FAKE", "FEMALE_TO_MALE_FAKE", "LIMIT_3_ACCOUNTS"
    val violationDetails: String,
    val attemptedAvatarUrl: String = "",
    val matchedExistingUser: String = "",
    val deviceModel: String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
    val timestamp: String,
    val status: String = "AUTO_BLOCKED" // "AUTO_BLOCKED", "PERMANENT_BANNED"
)


