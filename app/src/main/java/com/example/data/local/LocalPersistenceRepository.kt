package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.model.ChatMessage
import com.example.model.Comment
import com.example.model.CommentReply
import com.example.model.MessageDeliveryStatus
import com.example.model.Post
import com.example.model.PostAudioTrack
import com.example.model.ReactionType
import com.example.model.ReactionUser
import com.example.model.User
import org.json.JSONArray
import org.json.JSONObject

data class LoadedPostsSnapshot(
    val visiblePosts: List<Post>,
    val hiddenPostIds: Set<String>
)

data class LoadedChatsSnapshot(
    val messagesByConversation: Map<String, List<ChatMessage>>,
    val deletedMessageIds: Set<String>
)

/**
 * Durable Room Database repository for Posts, Comments, Reactions, Voice Notes, and Chat Messages.
 * Works 100% offline and in APK builds without requiring google-services.json, while syncing seamlessly
 * alongside Firebase Firestore whenever cloud is active.
 */
class LocalPersistenceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = FotoFilmDatabase.getInstance(appContext)
    private val postDao = db.postDao()
    private val chatDao = db.chatMessageDao()

    companion object {
        private const val TAG = "LocalPersistenceRepo"
    }

    suspend fun loadPostsSnapshot(): LoadedPostsSnapshot {
        return try {
            val allRecords = postDao.getAllRecords()
            val visible = mutableListOf<Post>()
            val hidden = mutableSetOf<String>()
            allRecords.forEach { entity ->
                if (entity.isHidden) {
                    hidden.add(entity.id)
                } else {
                    decodePost(entity.postJson)?.let { visible.add(it) }
                }
            }
            LoadedPostsSnapshot(visiblePosts = visible, hiddenPostIds = hidden)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed loading Room posts", e)
            LoadedPostsSnapshot(emptyList(), emptySet())
        }
    }

    suspend fun upsertPost(post: Post, isPinned: Boolean = false) {
        try {
            val entity = PostEntity(
                id = post.id,
                authorId = post.author.id,
                createdAt = post.createdAt,
                updatedAt = System.currentTimeMillis(),
                isSaved = post.isSaved,
                isPinned = isPinned,
                isHidden = false,
                postJson = encodePost(post).toString()
            )
            postDao.upsertPost(entity)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed saving post ${post.id} to Room", e)
        }
    }

    suspend fun upsertPosts(posts: List<Post>) {
        if (posts.isEmpty()) return
        try {
            val now = System.currentTimeMillis()
            val entities = posts.map { post ->
                PostEntity(
                    id = post.id,
                    authorId = post.author.id,
                    createdAt = post.createdAt,
                    updatedAt = now,
                    isSaved = post.isSaved,
                    isPinned = false,
                    isHidden = false,
                    postJson = encodePost(post).toString()
                )
            }
            postDao.upsertAll(entities)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed batch saving posts to Room", e)
        }
    }

    suspend fun markPostHidden(postId: String) {
        try {
            val placeholder = Post(
                id = postId,
                author = User(id = "hidden", fullName = "", username = ""),
                timestamp = "",
                text = ""
            )
            val existing = postDao.getAllRecords().firstOrNull { it.id == postId }
            if (existing != null) {
                postDao.markPostHidden(postId)
            } else {
                postDao.upsertPost(
                    PostEntity(
                        id = postId,
                        authorId = "hidden",
                        createdAt = 0L,
                        updatedAt = System.currentTimeMillis(),
                        isHidden = true,
                        postJson = encodePost(placeholder).toString()
                    )
                )
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed hiding post $postId in Room", e)
        }
    }

    suspend fun loadChatsSnapshot(): LoadedChatsSnapshot {
        return try {
            val allRecords = chatDao.getAllRecords()
            val byConv = linkedMapOf<String, MutableList<ChatMessage>>()
            val deletedIds = mutableSetOf<String>()
            allRecords.forEach { entity ->
                if (entity.isDeleted) {
                    deletedIds.add(entity.id)
                } else {
                    decodeChatMessage(entity.messageJson)?.let { msg ->
                        byConv.getOrPut(entity.conversationId) { mutableListOf() }.add(msg)
                    }
                }
            }
            LoadedChatsSnapshot(messagesByConversation = byConv, deletedMessageIds = deletedIds)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed loading Room chat messages", e)
            LoadedChatsSnapshot(emptyMap(), emptySet())
        }
    }

    suspend fun upsertChatMessage(
        conversationId: String,
        message: ChatMessage,
        createdAt: Long = System.currentTimeMillis()
    ) {
        try {
            val existing = chatDao.getAllRecords().firstOrNull { it.id == message.id }
            val preservedCreatedAt = existing?.createdAt ?: createdAt
            chatDao.upsertMessage(
                ChatMessageEntity(
                    id = message.id,
                    conversationId = conversationId,
                    createdAt = preservedCreatedAt,
                    isDeleted = false,
                    messageJson = encodeChatMessage(message).toString()
                )
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Failed saving chat message ${message.id} to Room", e)
        }
    }

    suspend fun markChatMessageDeleted(conversationId: String, messageId: String) {
        try {
            val existing = chatDao.getAllRecords().firstOrNull { it.id == messageId }
            if (existing != null) {
                chatDao.markMessageDeleted(messageId)
            } else {
                chatDao.upsertMessage(
                    ChatMessageEntity(
                        id = messageId,
                        conversationId = conversationId,
                        createdAt = 0L,
                        isDeleted = true,
                        messageJson = "{}"
                    )
                )
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed marking chat message deleted $messageId", e)
        }
    }

    suspend fun markConversationDeleted(conversationId: String) {
        try {
            chatDao.markConversationDeleted(conversationId)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed marking conversation deleted $conversationId", e)
        }
    }

    // =========================================================================
    // JSON Encoding / Decoding
    // =========================================================================

    private fun encodeUser(user: User): JSONObject = JSONObject().apply {
        put("id", user.id)
        put("fullName", user.fullName)
        put("username", user.username)
        put("email", user.email)
        put("avatarUrl", user.avatarUrl)
        put("coverUrl", user.coverUrl)
        put("isVerified", user.isVerified)
        put("isOnline", user.isOnline)
    }

    private fun decodeUser(obj: JSONObject?): User {
        if (obj == null) return User(id = "user_unknown", fullName = "FotoFilm User", username = "user")
        return User(
            id = obj.optString("id", "user_unknown"),
            fullName = obj.optString("fullName", "FotoFilm User"),
            username = obj.optString("username", "user"),
            email = obj.optString("email", ""),
            avatarUrl = obj.optString("avatarUrl", ""),
            coverUrl = obj.optString("coverUrl", ""),
            isVerified = obj.optBoolean("isVerified", false),
            isOnline = obj.optBoolean("isOnline", true)
        )
    }

    private fun encodeReactionCounts(map: Map<ReactionType, Int>): JSONObject = JSONObject().apply {
        map.forEach { (k, v) -> put(k.name, v) }
    }

    private fun decodeReactionCounts(obj: JSONObject?): Map<ReactionType, Int> {
        if (obj == null) return emptyMap()
        val out = mutableMapOf<ReactionType, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val rx = ReactionType.entries.firstOrNull { it.name == k } ?: continue
            val count = obj.optInt(k, 0)
            if (count > 0) out[rx] = count
        }
        return out
    }

    private fun encodeReactors(list: List<ReactionUser>): JSONArray = JSONArray().apply {
        list.take(50).forEach { ru ->
            put(
                JSONObject().apply {
                    put("user", encodeUser(ru.user))
                    put("reaction", ru.reaction.name)
                    put("timestamp", ru.timestamp)
                }
            )
        }
    }

    private fun decodeReactors(arr: JSONArray?): List<ReactionUser> {
        if (arr == null) return emptyList()
        val out = mutableListOf<ReactionUser>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val rxName = o.optString("reaction")
            val rx = ReactionType.entries.firstOrNull { it.name == rxName } ?: continue
            out.add(
                ReactionUser(
                    user = decodeUser(o.optJSONObject("user")),
                    reaction = rx,
                    timestamp = o.optString("timestamp", "Just now")
                )
            )
        }
        return out
    }

    private fun encodeAudioTrack(track: PostAudioTrack): JSONObject = JSONObject().apply {
        put("id", track.id)
        put("title", track.title)
        put("artist", track.artist)
        put("category", track.category)
        put("duration", track.duration)
        put("audioUrl", track.audioUrl ?: "")
        put("coverArtUrl", track.coverArtUrl ?: "")
    }

    private fun decodeAudioTrack(obj: JSONObject?): PostAudioTrack? {
        if (obj == null) return null
        val id = obj.optString("id")
        val title = obj.optString("title")
        if (id.isBlank() && title.isBlank()) return null
        return PostAudioTrack(
            id = id.ifBlank { "aud_saved" },
            title = title.ifBlank { "Audio Track" },
            artist = obj.optString("artist", "Artist"),
            category = obj.optString("category", "Music"),
            duration = obj.optString("duration", "0:30"),
            audioUrl = obj.optString("audioUrl").takeIf { it.isNotBlank() },
            coverArtUrl = obj.optString("coverArtUrl").takeIf { it.isNotBlank() }
        )
    }

    private fun encodeComments(comments: List<Comment>): JSONArray = JSONArray().apply {
        comments.forEach { c ->
            put(
                JSONObject().apply {
                    put("id", c.id)
                    put("postId", c.postId)
                    put("author", encodeUser(c.author))
                    put("text", c.text)
                    put("timestamp", c.timestamp)
                    put("likeCount", c.likeCount)
                    put("isLiked", c.isLiked)
                    put("userReaction", c.userReaction?.name ?: "")
                    put("reactionCounts", encodeReactionCounts(c.reactionCounts))
                    put("reactors", encodeReactors(c.reactors))
                    put(
                        "replies",
                        JSONArray().apply {
                            c.replies.forEach { r ->
                                put(
                                    JSONObject().apply {
                                        put("id", r.id)
                                        put("author", encodeUser(r.author))
                                        put("text", r.text)
                                        put("timestamp", r.timestamp)
                                        put("likeCount", r.likeCount)
                                        put("isLiked", r.isLiked)
                                        put("userReaction", r.userReaction?.name ?: "")
                                        put("reactionCounts", encodeReactionCounts(r.reactionCounts))
                                        put("reactors", encodeReactors(r.reactors))
                                    }
                                )
                            }
                        }
                    )
                }
            )
        }
    }

    private fun decodeComments(arr: JSONArray?): List<Comment> {
        if (arr == null) return emptyList()
        val out = mutableListOf<Comment>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val repliesArr = o.optJSONArray("replies")
            val replies = mutableListOf<CommentReply>()
            if (repliesArr != null) {
                for (j in 0 until repliesArr.length()) {
                    val ro = repliesArr.optJSONObject(j) ?: continue
                    val rRxName = ro.optString("userReaction")
                    replies.add(
                        CommentReply(
                            id = ro.optString("id"),
                            author = decodeUser(ro.optJSONObject("author")),
                            text = ro.optString("text"),
                            timestamp = ro.optString("timestamp", "Just now"),
                            likeCount = ro.optInt("likeCount", 0),
                            isLiked = ro.optBoolean("isLiked", false),
                            userReaction = ReactionType.entries.firstOrNull { it.name == rRxName },
                            reactionCounts = decodeReactionCounts(ro.optJSONObject("reactionCounts")),
                            reactors = decodeReactors(ro.optJSONArray("reactors"))
                        )
                    )
                }
            }
            val cRxName = o.optString("userReaction")
            out.add(
                Comment(
                    id = o.optString("id"),
                    postId = o.optString("postId"),
                    author = decodeUser(o.optJSONObject("author")),
                    text = o.optString("text"),
                    timestamp = o.optString("timestamp", "Just now"),
                    likeCount = o.optInt("likeCount", 0),
                    isLiked = o.optBoolean("isLiked", false),
                    userReaction = ReactionType.entries.firstOrNull { it.name == cRxName },
                    reactionCounts = decodeReactionCounts(o.optJSONObject("reactionCounts")),
                    reactors = decodeReactors(o.optJSONArray("reactors")),
                    replies = replies
                )
            )
        }
        return out
    }

    private fun encodePost(post: Post): JSONObject = JSONObject().apply {
        put("id", post.id)
        put("author", encodeUser(post.author))
        put("timestamp", post.timestamp)
        put("title", post.title ?: "")
        put("text", post.text)
        put("hashtags", JSONArray(post.hashtags))
        put("images", JSONArray(post.images))
        put("isVideo", post.isVideo)
        put("isReel", post.isReel)
        put("isFaceGram", post.isFaceGram)
        put("isFaceClip", post.isFaceClip)
        put("videoTitle", post.videoTitle ?: "")
        put("videoDuration", post.videoDuration ?: "")
        put("reachViews", post.reachViews)
        put("reactionCounts", encodeReactionCounts(post.reactionCounts))
        put("userReaction", post.userReaction?.name ?: "")
        put("reactors", encodeReactors(post.reactors))
        put("comments", encodeComments(post.comments))
        put("cloudCommentCount", post.cloudCommentCount)
        put("shareCount", post.shareCount)
        put("isSaved", post.isSaved)
        put("location", post.location ?: "")
        put("musicInfo", post.musicInfo ?: "")
        post.audioTrack?.let { put("audioTrack", encodeAudioTrack(it)) }
        put("isBoosted", post.isBoosted)
        put("boostStatus", post.boostStatus ?: "")
        if (post.boostBudget != null) put("boostBudget", post.boostBudget)
        put("boostTargetReach", post.boostTargetReach ?: "")
        put("boostTrxId", post.boostTrxId ?: "")
        if (post.boostDays != null) put("boostDays", post.boostDays)
        put("isFreePromotedByNiyon", post.isFreePromotedByNiyon)
        put("boostAudience", post.boostAudience ?: "")
        put("isBoostActive", post.isBoostActive)
        put("createdAt", post.createdAt)
    }

    private fun decodePost(raw: String): Post? {
        return try {
            val o = JSONObject(raw)
            val hashtagsArr = o.optJSONArray("hashtags")
            val hashtags = mutableListOf<String>()
            if (hashtagsArr != null) {
                for (i in 0 until hashtagsArr.length()) {
                    hashtagsArr.optString(i).takeIf { it.isNotBlank() }?.let { hashtags.add(it) }
                }
            }
            val imagesArr = o.optJSONArray("images")
            val images = mutableListOf<String>()
            if (imagesArr != null) {
                for (i in 0 until imagesArr.length()) {
                    imagesArr.optString(i).takeIf { it.isNotBlank() }?.let { images.add(it) }
                }
            }
            val userRxName = o.optString("userReaction")
            Post(
                id = o.optString("id"),
                author = decodeUser(o.optJSONObject("author")),
                timestamp = o.optString("timestamp", "Just now • 🌐 Public"),
                title = o.optString("title").takeIf { it.isNotBlank() },
                text = o.optString("text", ""),
                hashtags = hashtags,
                images = images,
                isVideo = o.optBoolean("isVideo", false),
                isReel = o.optBoolean("isReel", false),
                isFaceGram = o.optBoolean("isFaceGram", false),
                isFaceClip = o.optBoolean("isFaceClip", false),
                videoTitle = o.optString("videoTitle").takeIf { it.isNotBlank() },
                videoDuration = o.optString("videoDuration").takeIf { it.isNotBlank() },
                reachViews = o.optString("reachViews", "1.5K"),
                reactionCounts = decodeReactionCounts(o.optJSONObject("reactionCounts")),
                userReaction = ReactionType.entries.firstOrNull { it.name == userRxName },
                reactors = decodeReactors(o.optJSONArray("reactors")),
                comments = decodeComments(o.optJSONArray("comments")),
                cloudCommentCount = o.optInt("cloudCommentCount", 0),
                shareCount = o.optInt("shareCount", 0),
                isSaved = o.optBoolean("isSaved", false),
                location = o.optString("location").takeIf { it.isNotBlank() },
                musicInfo = o.optString("musicInfo").takeIf { it.isNotBlank() },
                audioTrack = decodeAudioTrack(o.optJSONObject("audioTrack")),
                isBoosted = o.optBoolean("isBoosted", false),
                boostStatus = o.optString("boostStatus").takeIf { it.isNotBlank() },
                boostBudget = if (o.has("boostBudget")) o.optInt("boostBudget") else null,
                boostTargetReach = o.optString("boostTargetReach").takeIf { it.isNotBlank() },
                boostTrxId = o.optString("boostTrxId").takeIf { it.isNotBlank() },
                boostDays = if (o.has("boostDays")) o.optInt("boostDays") else null,
                isFreePromotedByNiyon = o.optBoolean("isFreePromotedByNiyon", false),
                boostAudience = o.optString("boostAudience").takeIf { it.isNotBlank() },
                isBoostActive = o.optBoolean("isBoostActive", true),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Failed decoding post JSON", e)
            null
        }
    }

    private fun encodeChatMessage(msg: ChatMessage, includeReply: Boolean = true): JSONObject = JSONObject().apply {
        put("id", msg.id)
        put("senderId", msg.senderId)
        put("senderName", msg.senderName)
        put("text", msg.text)
        put("timestamp", msg.timestamp)
        put("mediaUrl", msg.mediaUrl ?: "")
        put("isMediaVideo", msg.isMediaVideo)
        put("isFromMe", msg.isFromMe)
        if (includeReply && msg.replyTo != null) {
            put("replyTo", encodeChatMessage(msg.replyTo, includeReply = false))
        }
        put("reactions", JSONObject().apply {
            msg.reactions.forEach { (k, v) -> put(k, v) }
        })
        put("userReaction", msg.userReaction ?: "")
        put("isAudioVoice", msg.isAudioVoice)
        put("audioDurationSeconds", msg.audioDurationSeconds)
        put("status", msg.status.name)
        put("callLogType", msg.callLogType?.name ?: "")
        put("callDurationSeconds", msg.callDurationSeconds)
        put("isMissedCall", msg.isMissedCall)
    }

    private fun decodeChatMessage(raw: String): ChatMessage? {
        return try {
            decodeChatMessageObj(JSONObject(raw))
        } catch (_: Throwable) {
            null
        }
    }

    private fun decodeChatMessageObj(o: JSONObject): ChatMessage {
        val rxObj = o.optJSONObject("reactions")
        val rxMap = mutableMapOf<String, Int>()
        if (rxObj != null) {
            val keys = rxObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = rxObj.optInt(k, 0)
                if (v > 0) rxMap[k] = v
            }
        }
        val statusName = o.optString("status", MessageDeliveryStatus.DELIVERED.name)
        val status = MessageDeliveryStatus.entries.firstOrNull { it.name == statusName }
            ?: MessageDeliveryStatus.DELIVERED
        val callTypeName = o.optString("callLogType", "")
        val callLogType = com.example.model.CallType.entries.firstOrNull { it.name == callTypeName }

        val replyObj = o.optJSONObject("replyTo")
        return ChatMessage(
            id = o.optString("id"),
            senderId = o.optString("senderId"),
            senderName = o.optString("senderName"),
            text = o.optString("text"),
            timestamp = o.optString("timestamp", "Just now"),
            mediaUrl = o.optString("mediaUrl").takeIf { it.isNotBlank() },
            isMediaVideo = o.optBoolean("isMediaVideo", false),
            isFromMe = o.optBoolean("isFromMe", false),
            replyTo = replyObj?.let { decodeChatMessageObj(it) },
            reactions = rxMap,
            userReaction = o.optString("userReaction").takeIf { it.isNotBlank() },
            isAudioVoice = o.optBoolean("isAudioVoice", false),
            audioDurationSeconds = o.optInt("audioDurationSeconds", 0),
            status = status,
            callLogType = callLogType,
            callDurationSeconds = o.optInt("callDurationSeconds", 0),
            isMissedCall = o.optBoolean("isMissedCall", false)
        )
    }
}
