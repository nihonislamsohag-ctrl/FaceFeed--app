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
    val mutualFriendsCount: String = "0 mutual friends",
    val isOnline: Boolean = true
)

data class User(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val bio: String = "Alhamdulillah for everything ✨ | Excited to join Face Feed",
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
    val isAccountActive: Boolean = true,
    val isFidaVerifiedFreeGranted: Boolean = false,
    val suspensionReason: String? = null,
    val avatarFrame: String? = null
) {
    val isMaxFriendsReached: Boolean
        get() = friendCount >= 5000
}

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
    val assignedDate: String = "২০ মে, ২০২৬"
)

