package com.example.model

/**
 * Model representing a Music Track in the Facebook-style Music feature.
 * Stored in and retrieved from Firebase Firestore collection `music_tracks`.
 */
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val views: String = "12K",
    val thumbnailUrl: String = "",
    val audioUrl: String = "",
    val lyrics: String = "",
    val category: String = "For you", // "For you", "Trending", "বাঁশির সুর", "গজল", "ডিজে গান", "মিউজিক"
    val duration: String = "3:45",
    val isTrending: Boolean = false,
    val addedBy: String = "Niyon Islam Sohag",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toPostAudioTrack(): PostAudioTrack {
        return PostAudioTrack(
            id = id,
            title = title,
            artist = artist,
            category = category,
            duration = duration,
            audioUrl = audioUrl.takeIf { it.isNotBlank() },
            coverArtUrl = thumbnailUrl.takeIf { it.isNotBlank() }
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): MusicTrack {
            return MusicTrack(
                id = id,
                title = map["title"] as? String ?: "অজানা গান",
                artist = map["artist"] as? String ?: "Niyon Islam Sohag",
                views = map["views"] as? String ?: "12K",
                thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
                audioUrl = map["audioUrl"] as? String ?: "",
                lyrics = map["lyrics"] as? String ?: "",
                category = map["category"] as? String ?: "For you",
                duration = map["duration"] as? String ?: "3:45",
                isTrending = map["isTrending"] as? Boolean ?: false,
                addedBy = map["addedBy"] as? String ?: "Niyon Islam Sohag",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
