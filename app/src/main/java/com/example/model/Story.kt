package com.example.model

data class Story(
    val id: String,
    val author: User,
    val mediaUrl: String,
    val timestamp: String,
    val isViewed: Boolean = false,
    val caption: String = "",
    val musicTrack: String? = null,
    val musicArtist: String? = null,
    val audioUrl: String? = null
)

data class StoryHighlight(
    val id: String,
    val userId: String,
    val title: String,
    val coverUrl: String,
    val stories: List<Story> = emptyList(),
    val createdAt: String = "সম্প্রতি"
)
