package com.example.model

enum class NotificationType {
    LIKE, COMMENT, FRIEND_REQUEST, GROUP_INVITE, PAGE_LIKE, LIVE_STREAM
}

data class AppNotification(
    val id: String,
    val actor: User,
    val title: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val targetPreview: String? = null
)
