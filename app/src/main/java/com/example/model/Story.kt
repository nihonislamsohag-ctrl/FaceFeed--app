package com.example.model

data class Story(
    val id: String,
    val author: User,
    val mediaUrl: String,
    val timestamp: String,
    val isViewed: Boolean = false,
    val caption: String = ""
)
