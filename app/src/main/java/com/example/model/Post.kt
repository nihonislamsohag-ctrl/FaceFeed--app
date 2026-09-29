package com.example.model

data class CommentReply(
    val id: String,
    val author: User,
    val text: String,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)

data class Comment(
    val id: String,
    val postId: String,
    val author: User,
    val text: String,
    val timestamp: String,
    val likeCount: Int = 12,
    val isLiked: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

data class PostAudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: String, // "গজল", "বাঁশির সুর", "জেমস ও আইয়ুব বাচ্চু", "ডিজে গান", "হিন্দি গান", "দেশাত্মবোধক", "লালনগীতি", "রবীন্দ্র সংগীত", "দুঃখের গান"
    val duration: String = "3:45",
    val audioUrl: String? = null,
    val coverArtUrl: String? = null
)

data class Post(
    val id: String,
    val author: User,
    val timestamp: String,
    val title: String? = null,                // Post title / caption at top
    val text: String,
    val hashtags: List<String> = emptyList(), // Up to 10 hashtags
    val images: List<String> = emptyList(),   // Up to 15 images
    val isVideo: Boolean = false,
    val isReel: Boolean = false,
    val videoTitle: String? = null,
    val videoDuration: String? = null,
    val reachViews: String = "1.2M",         // Simulated viral reach
    val reactionCounts: Map<ReactionType, Int> = mapOf(
        ReactionType.LIKE to 1420,
        ReactionType.LOVE to 3890,
        ReactionType.CARE to 980,
        ReactionType.HUNDRED to 650,
        ReactionType.WOW to 210
    ),
    val userReaction: ReactionType? = null,
    val comments: List<Comment> = emptyList(),
    val shareCount: Int = 412,
    val isSaved: Boolean = false,
    val location: String? = null,
    val musicInfo: String? = null,
    val audioTrack: PostAudioTrack? = null
) {
    val totalReactions: Int
        get() = reactionCounts.values.sum()
}
