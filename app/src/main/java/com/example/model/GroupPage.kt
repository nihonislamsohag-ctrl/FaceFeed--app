package com.example.model

data class SocialGroup(
    val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val memberCount: Int,
    val isPrivate: Boolean,
    val isJoined: Boolean = false,
    val creatorName: String
)

data class SocialPage(
    val id: String,
    val name: String,
    val category: String,
    val avatarUrl: String,
    val coverUrl: String,
    val followerCount: Int,
    val isLiked: Boolean = false,
    val isVerified: Boolean = true
)
