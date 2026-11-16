package com.example.model

enum class NotificationType {
    LIKE, COMMENT, SHARE, FRIEND_REQUEST, GROUP_INVITE, PAGE_LIKE, PAGE_POST, LIVE_STREAM, APP_UPDATE, TAX_INFO, STORY_REACTION, BIRTHDAY
}

data class AppNotification(
    val id: String,
    val actor: User,
    val title: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val isNewSection: Boolean = true, // true for "New", false for "Earlier"
    val targetPreview: String? = null,
    val appUpdateVersion: String? = null,
    val bannerUrl: String? = null,
    val hasActionButtons: Boolean = false,
    val isGroupJoined: Boolean = false,
    val isFriendAccepted: Boolean = false,
    val subBadgeType: String? = null // "heart", "tax", "group", "comment", "friend_accept", "like", "birthday", "live"
)
