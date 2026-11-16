package com.example.model

/**
 * Support Ticket Model for FaceFeed Customer Helpdesk & Support Team
 * Used by users in Help & Support (Screenshot 2) and monitored by Niyon Islam Sohag in Right Manager Fida.
 */
data class SupportTicket(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val profileUrl: String,
    val message: String,
    val screenshotUrls: List<String> = emptyList(),
    val timestamp: String,
    val status: String = "Open", // Open, Resolved, Pending
    val adminReply: String? = null,
    val adminReplyTime: String? = null
)
