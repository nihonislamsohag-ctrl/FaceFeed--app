package com.example.model

data class ReactionUser(
    val user: User,
    val reaction: ReactionType,
    val timestamp: String = "Just now"
)

data class CommentReply(
    val id: String,
    val author: User,
    val text: String,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val userReaction: ReactionType? = null,
    val reactionCounts: Map<ReactionType, Int> = emptyMap(),
    val reactors: List<ReactionUser> = emptyList()
) {
    val totalReactions: Int
        get() = if (reactionCounts.isNotEmpty()) reactionCounts.values.sum() else likeCount
}

data class Comment(
    val id: String,
    val postId: String,
    val author: User,
    val text: String,
    val timestamp: String,
    val likeCount: Int = 12,
    val isLiked: Boolean = false,
    val userReaction: ReactionType? = null,
    val reactionCounts: Map<ReactionType, Int> = emptyMap(),
    val reactors: List<ReactionUser> = emptyList(),
    val replies: List<CommentReply> = emptyList()
) {
    val totalReactions: Int
        get() = if (reactionCounts.isNotEmpty()) reactionCounts.values.sum() else likeCount
}

data class PostAudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: String,
    val duration: String = "3:45",
    val audioUrl: String? = null,
    val coverArtUrl: String? = null
)

data class Post(
    val id: String,
    val author: User,
    val timestamp: String,
    val title: String? = null,
    val text: String,
    val hashtags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val isVideo: Boolean = false,
    val isReel: Boolean = false,
    val isFaceGram: Boolean = false,
    val isFaceClip: Boolean = false,
    val videoTitle: String? = null,
    val videoDuration: String? = null,
    val reachViews: String = "1.2M",
    val reactionCounts: Map<ReactionType, Int> = mapOf(
        ReactionType.LIKE to 1420,
        ReactionType.LOVE to 3890,
        ReactionType.CARE to 980,
        ReactionType.HUNDRED to 650,
        ReactionType.WOW to 210
    ),
    val userReaction: ReactionType? = null,
    val reactors: List<ReactionUser> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val cloudCommentCount: Int = 0,
    val shareCount: Int = 412,
    val isSaved: Boolean = false,
    val location: String? = null,
    val musicInfo: String? = null,
    val audioTrack: PostAudioTrack? = null,
    val isBoosted: Boolean = false,
    val boostStatus: String? = null,
    val boostBudget: Int? = null,
    val boostTargetReach: String? = null,
    val boostTrxId: String? = null,
    val boostDays: Int? = null,
    val isFreePromotedByNiyon: Boolean = false,
    val boostAudience: String? = null,
    val isBoostActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalReactions: Int
        get() = reactionCounts.values.sum()
}
