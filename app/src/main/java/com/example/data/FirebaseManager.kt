package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    const val POSTS_PAGE_SIZE = 30L
    const val COLLECTIONS_PAGE_SIZE = 30L
    const val MESSAGES_PAGE_SIZE = 50L
    const val COMMENTS_PAGE_SIZE = 50L

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var firestore: FirebaseFirestore? = null
    private var authOverride: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null
    private var paginationCursor: DocumentSnapshot? = null

    private val _isCloudConnected = MutableStateFlow(false)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val _databaseStatusMessage = MutableStateFlow("Firebase ইনিশিয়ালাইজ হচ্ছে...")
    val databaseStatusMessage: StateFlow<String> = _databaseStatusMessage.asStateFlow()

    private val _hasMorePosts = MutableStateFlow(false)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()

    private val _isLoadingMorePosts = MutableStateFlow(false)
    val isLoadingMorePosts: StateFlow<Boolean> = _isLoadingMorePosts.asStateFlow()

    private val _cloudQuotaNotice = MutableStateFlow<String?>(null)
    val cloudQuotaNotice: StateFlow<String?> = _cloudQuotaNotice.asStateFlow()

    var isInitialized: Boolean = false
        private set

    fun initialize(context: Context) {
        try {
            val appCtx = context.applicationContext
            ServiceQuotaTracker.initialize(appCtx)
            if (FirebaseApp.getApps(appCtx).isNotEmpty()) {
                val dbId = appCtx.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
                try {
                    db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(
                            PersistentCacheSettings.newBuilder()
                                .setSizeBytes(100L * 1024L * 1024L)
                                .build()
                        )
                        .build()
                } catch (_: Exception) {
                    // Settings already configured on active Firestore instance
                }
                firestore = db

                try {
                    storage = FirebaseStorage.getInstance()
                } catch (se: Exception) {
                    Log.w(TAG, "Firebase Storage not yet configured: ${se.message}")
                }

                isInitialized = true
                _isCloudConnected.value = true
                _databaseStatusMessage.value =
                    "Google Firebase ক্লাউড ডাটাবেজ সংযুক্ত • ৩০টি করে পেজিনেশন ও ক্যাশ সক্রিয়"
                Log.d(TAG, "Firebase Firestore initialized with database: $dbId")
            } else {
                _isCloudConnected.value = false
                _databaseStatusMessage.value = "Firebase কনফিগারেশন অনুপস্থিত (অফলাইন)"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization error: ${e.message}")
            isInitialized = false
            _isCloudConnected.value = false
            _databaseStatusMessage.value = "ক্লাউড কানেকশন এরর: ${e.localizedMessage}"
        }
    }

    internal fun setInstancesForTesting(db: FirebaseFirestore, auth: FirebaseAuth) {
        firestore = db
        authOverride = auth
        isInitialized = true
        _isCloudConnected.value = true
    }

    private fun currentAuth(): FirebaseAuth {
        return authOverride ?: Firebase.auth
    }

    fun activeAuthUid(): String? {
        return try {
            currentAuth().currentUser?.uid
        } catch (_: Exception) {
            null
        }
    }

    private fun requireUserId(): String {
        return activeAuthUid()
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun dismissQuotaNotice() {
        _cloudQuotaNotice.value = null
    }

    private fun recordFirestoreFailure(e: Throwable, op: OperationType, path: String) {
        handleFirestoreError(e, op, path)
        if (isQuotaExceededError(e)) {
            _cloudQuotaNotice.value =
                "দৈনিক ফ্রি কোটা সীমা পূর্ণ হয়েছে (Spark Plan)। ২৪ ঘণ্টার মধ্যে রিসেট হবে অথবা Blaze Plan চালু করুন: $FIREBASE_BLAZE_UPGRADE_URL"
        }
    }

    // =========================================================================
    // User Profiles (Split Private PII /users/{uid} & Public /public_profiles/{uid})
    // =========================================================================

    fun syncSignedInUserProfile(user: User) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val now = System.currentTimeMillis()
        val privateData = hashMapOf<String, Any?>(
            "userId" to uid,
            "fullName" to user.fullName.take(100),
            "username" to user.username.ifBlank { user.fullName.lowercase().replace(" ", "_") }.take(80),
            "email" to user.email.take(160),
            "avatarUrl" to user.avatarUrl.take(1500),
            "coverUrl" to user.coverUrl.take(1500),
            "bio" to user.bio.take(500),
            "note" to user.note.take(120),
            "gender" to user.gender.take(40),
            "country" to user.country.take(80),
            "birthDate" to user.birthDate.take(40),
            "isVerified" to user.isVerified,
            "createdAt" to now,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

        val publicData = hashMapOf<String, Any?>(
            "userId" to uid,
            "fullName" to user.fullName.take(100),
            "username" to user.username.ifBlank { user.fullName.lowercase().replace(" ", "_") }.take(80),
            "avatarUrl" to user.avatarUrl.take(1500),
            "coverUrl" to user.coverUrl.take(1500),
            "bio" to user.bio.take(500),
            "note" to user.note.take(120),
            "isVerified" to user.isVerified,
            "createdAt" to now,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null } as Map<String, Any>

        val userRef = db.collection("users").document(uid)
        val publicRef = db.collection("public_profiles").document(uid)

        userRef.get().addOnSuccessListener { existing ->
            val finalPrivate = if (existing.exists()) {
                val created = existing.getLong("createdAt") ?: now
                privateData.toMutableMap().apply { put("createdAt", created) }
            } else {
                privateData
            }
            userRef.set(finalPrivate, SetOptions.merge()).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.WRITE, userRef.path)
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, userRef.path)
        }

        publicRef.get().addOnSuccessListener { existing ->
            val finalPublic = if (existing.exists()) {
                val created = existing.getLong("createdAt") ?: now
                publicData.toMutableMap().apply { put("createdAt", created) }
            } else {
                publicData
            }
            publicRef.set(finalPublic, SetOptions.merge()).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.WRITE, publicRef.path)
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, publicRef.path)
        }

        registerFcmToken()
    }

    fun registerFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) {
                        saveFcmToken(token)
                    }
                }
        } catch (_: Exception) {
        }
    }

    fun saveFcmToken(token: String) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val clean = token.trim().take(500)
        if (clean.isBlank()) return
        val userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { existing ->
            if (existing.exists()) {
                userRef.update(
                    mapOf(
                        "fcmToken" to clean,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).addOnFailureListener { e ->
                    recordFirestoreFailure(e, OperationType.UPDATE, userRef.path)
                }
            }
        }
    }

    /**
     * Play Store mandatory self-service account & PII deletion.
     */
    fun deleteCurrentUserAccountData(onComplete: (Boolean, String) -> Unit) {
        val db = firestore
        val authInstance = try {
            com.google.firebase.Firebase.auth
        } catch (_: Exception) {
            null
        }
        val fbUser = authInstance?.currentUser
        val uid = fbUser?.uid

        if (uid != null && db != null) {
            db.collection("users").document(uid).delete()
            db.collection("public_profiles").document(uid).delete()
            fbUser.delete()
                .addOnSuccessListener {
                    onComplete(true, "অ্যাকাউন্ট ও ক্লাউড ডাটা স্থায়ীভাবে ডিলিট করা হয়েছে ✓")
                }
                .addOnFailureListener {
                    authInstance.signOut()
                    onComplete(true, "ক্লাউড প্রোফাইল ডিলিট ও সাইন আউট সম্পন্ন হয়েছে ✓")
                }
        } else {
            onComplete(true, "লোকাল অ্যাকাউন্ট ডাটা সফলভাবে পরিষ্কার করা হয়েছে ✓")
        }
    }

    // =========================================================================
    // Storage Upload
    // =========================================================================

    fun uploadFileToStorage(
        uri: Uri,
        folder: String = "posts",
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = activeAuthUid()
        if (uid == null) {
            onFailure(IllegalStateException("Authentication required for storage upload"))
            return
        }
        val s = storage
        if (s == null) {
            onFailure(Exception("Firebase Storage not initialized"))
            return
        }
        val fileRef = s.reference.child("$folder/$uid/${System.currentTimeMillis()}_${UUID.randomUUID()}")
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }.addOnFailureListener { e ->
                    onFailure(e)
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    // =========================================================================
    // Posts + Paginated Feed (30 items per page)
    // =========================================================================

    private fun buildPostPayload(post: Post, uid: String): Map<String, Any> {
        val photoUrl = post.images.firstOrNull { !it.contains(".mp4") && !it.contains("video") }
            ?: (if (!post.isVideo) post.images.firstOrNull() ?: "" else "")
        val videoUrl = if (post.isVideo || post.isReel) (post.images.firstOrNull() ?: "") else ""
        val caption = when {
            !post.title.isNullOrBlank() && post.text.isNotBlank() -> "${post.title}\n${post.text}"
            !post.title.isNullOrBlank() -> post.title
            else -> post.text
        }
        val rxMap = post.reactionCounts.mapKeys { it.key.name }.mapValues { it.value.toLong() }

        return hashMapOf<String, Any?>(
            "userId" to uid,
            "authorId" to uid,
            "userName" to post.author.fullName.take(100),
            "authorFullName" to post.author.fullName.take(100),
            "authorUsername" to post.author.username.take(80),
            "authorAvatar" to post.author.avatarUrl.take(1500),
            "authorIsVerified" to post.author.isVerified,
            "photoUrl" to photoUrl.take(1500),
            "videoUrl" to videoUrl.take(1500),
            "caption" to caption.take(5300),
            "timestamp" to post.timestamp.take(100),
            "createdAt" to post.createdAt,
            "updatedAt" to FieldValue.serverTimestamp(),
            "title" to (post.title ?: "").take(300),
            "text" to post.text.take(5000),
            "hashtags" to post.hashtags.take(15),
            "images" to post.images.take(20),
            "isVideo" to post.isVideo,
            "isReel" to post.isReel,
            "isFaceGram" to post.isFaceGram,
            "isFaceClip" to post.isFaceClip,
            "videoTitle" to (post.videoTitle ?: "").take(300),
            "videoDuration" to (post.videoDuration ?: "").take(40),
            "location" to (post.location ?: "").take(160),
            "musicInfo" to (post.musicInfo ?: "").take(300),
            "audioTrackId" to (post.audioTrack?.id ?: "").take(120),
            "audioTrackTitle" to (post.audioTrack?.title ?: "").take(200),
            "audioTrackArtist" to (post.audioTrack?.artist ?: "").take(150),
            "audioTrackCategory" to (post.audioTrack?.category ?: "").take(100),
            "audioTrackDuration" to (post.audioTrack?.duration ?: "").take(40),
            "audioTrackUrl" to (post.audioTrack?.audioUrl ?: "").take(1500),
            "audioTrackCover" to (post.audioTrack?.coverArtUrl ?: "").take(1500),
            "isBoosted" to post.isBoosted,
            "boostStatus" to (post.boostStatus ?: "").take(40),
            "boostTargetReach" to (post.boostTargetReach ?: "").take(80),
            "reachViews" to post.reachViews.take(40),
            "reactionCounts" to rxMap,
            "commentCount" to maxOf(post.comments.size, post.cloudCommentCount),
            "shareCount" to post.shareCount
        ).filterValues { it != null } as Map<String, Any>
    }

    fun deletePostCloud(postId: String) {
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val docRef = db.collection("posts").document(postId)
        docRef.delete().addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.DELETE, docRef.path)
        }
    }

    fun savePost(post: Post) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val docRef = db.collection("posts").document(post.id)
        try {
            val payload = buildPostPayload(post, uid)
            docRef.set(payload)
                .addOnSuccessListener {
                    ServiceQuotaTracker.recordFirebaseWrites(1)
                    Log.d(TAG, "Post ${post.id} synced to cloud Firestore!")
                }
                .addOnFailureListener { e ->
                    recordFirestoreFailure(e, OperationType.CREATE, docRef.path)
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun savePostAwait(post: Post): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val uid = activeAuthUid() ?: return Result.failure(IllegalStateException("User not signed in"))
        val docRef = db.collection("posts").document(post.id)
        return try {
            docRef.set(buildPostPayload(post, uid)).await()
            ServiceQuotaTracker.recordFirebaseWrites(1)
            Result.success(post.id)
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    private fun parseReactionCounts(raw: Any?): Map<ReactionType, Int> {
        val rawMap = raw as? Map<*, *> ?: return emptyMap()
        val parsed = mutableMapOf<ReactionType, Int>()
        rawMap.forEach { (k, v) ->
            val keyName = k as? String ?: return@forEach
            val rxType = ReactionType.entries.firstOrNull { it.name == keyName } ?: return@forEach
            val count = (v as? Number)?.toInt() ?: 0
            if (count > 0) {
                parsed[rxType] = count
            }
        }
        return parsed
    }

    private fun parsePostDocument(doc: DocumentSnapshot): Post? {
        return try {
            val userId = doc.getString("userId") ?: doc.getString("authorId") ?: return null
            val userName = doc.getString("userName") ?: doc.getString("authorFullName") ?: "User"
            val userAvatar = doc.getString("authorAvatar").orEmpty()
            val isVerified = doc.getBoolean("authorIsVerified") ?: false
            val author = User(
                id = userId,
                fullName = userName,
                username = doc.getString("authorUsername") ?: userId,
                avatarUrl = userAvatar,
                isVerified = isVerified
            )

            val textVal = doc.getString("text") ?: doc.getString("caption") ?: ""
            val title = doc.getString("title")
            val photoUrl = doc.getString("photoUrl")
            val videoUrl = doc.getString("videoUrl")
            val isVideo = doc.getBoolean("isVideo") ?: (!videoUrl.isNullOrBlank())
            val isReel = doc.getBoolean("isReel") ?: false

            @Suppress("UNCHECKED_CAST")
            val rawImages = (doc.get("images") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val images = if (rawImages.isNotEmpty()) {
                rawImages
            } else {
                listOfNotNull(
                    photoUrl?.takeIf { it.isNotBlank() },
                    videoUrl?.takeIf { it.isNotBlank() && isVideo }
                )
            }

            @Suppress("UNCHECKED_CAST")
            val hashtags = (doc.get("hashtags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val timestamp = doc.getString("timestamp") ?: "Just now • 🌐 Public"
            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            val rxCounts = parseReactionCounts(doc.get("reactionCounts"))

            val audioTrackId = doc.getString("audioTrackId")?.takeIf { it.isNotBlank() }
            val audioTrackTitle = doc.getString("audioTrackTitle")?.takeIf { it.isNotBlank() }
            val parsedAudioTrack = if (audioTrackId != null || audioTrackTitle != null) {
                PostAudioTrack(
                    id = audioTrackId ?: "aud_${doc.id}",
                    title = audioTrackTitle ?: "Audio Track",
                    artist = doc.getString("audioTrackArtist")?.takeIf { it.isNotBlank() } ?: userName,
                    category = doc.getString("audioTrackCategory")?.takeIf { it.isNotBlank() } ?: "Audio",
                    duration = doc.getString("audioTrackDuration")?.takeIf { it.isNotBlank() } ?: "0:30",
                    audioUrl = doc.getString("audioTrackUrl")?.takeIf { it.isNotBlank() },
                    coverArtUrl = doc.getString("audioTrackCover")?.takeIf { it.isNotBlank() }
                )
            } else null

            Post(
                id = doc.id,
                author = author,
                timestamp = timestamp,
                title = title?.takeIf { it.isNotBlank() },
                text = textVal,
                hashtags = hashtags,
                images = images,
                isVideo = isVideo,
                isReel = isReel,
                isFaceGram = doc.getBoolean("isFaceGram") ?: false,
                isFaceClip = doc.getBoolean("isFaceClip") ?: false,
                videoTitle = doc.getString("videoTitle")?.takeIf { it.isNotBlank() },
                videoDuration = doc.getString("videoDuration")?.takeIf { it.isNotBlank() },
                location = doc.getString("location")?.takeIf { it.isNotBlank() },
                musicInfo = doc.getString("musicInfo")?.takeIf { it.isNotBlank() },
                audioTrack = parsedAudioTrack,
                isBoosted = doc.getBoolean("isBoosted") ?: false,
                boostStatus = doc.getString("boostStatus")?.takeIf { it.isNotBlank() },
                boostTargetReach = doc.getString("boostTargetReach")?.takeIf { it.isNotBlank() },
                reachViews = doc.getString("reachViews") ?: "1.5K",
                reactionCounts = rxCounts,
                cloudCommentCount = (doc.getLong("commentCount") ?: 0L).toInt(),
                shareCount = (doc.getLong("shareCount") ?: 0L).toInt(),
                createdAt = createdAt
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Listen to ONLY the first page (30 newest posts) in real time for massive quota savings.
     */
    fun listenToPosts(onPostsUpdated: (List<Post>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val path = "posts"
        return try {
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(POSTS_PAGE_SIZE)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val readDocs = snapshots.documentChanges.size.coerceAtLeast(
                            if (snapshots.metadata.isFromCache) 0 else snapshots.size()
                        )
                        if (readDocs > 0) {
                            ServiceQuotaTracker.recordFirebaseReads(readDocs)
                        }
                        val cloudPosts = snapshots.documents.mapNotNull { parsePostDocument(it) }
                        if (paginationCursor == null && snapshots.documents.isNotEmpty()) {
                            paginationCursor = snapshots.documents.last()
                            _hasMorePosts.value = snapshots.size().toLong() >= POSTS_PAGE_SIZE
                        } else if (snapshots.isEmpty) {
                            _hasMorePosts.value = false
                        }
                        onPostsUpdated(cloudPosts)
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    /**
     * Cursor-based pagination (startAfter last visible document) loading 30 older posts per call.
     */
    fun loadMorePosts(onMorePostsLoaded: (List<Post>) -> Unit) {
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val cursor = paginationCursor ?: run {
            _hasMorePosts.value = false
            return
        }
        if (_isLoadingMorePosts.value || !_hasMorePosts.value) return

        _isLoadingMorePosts.value = true
        val path = "posts"
        db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .startAfter(cursor)
            .limit(POSTS_PAGE_SIZE)
            .get()
            .addOnSuccessListener { snapshot ->
                _isLoadingMorePosts.value = false
                ServiceQuotaTracker.recordFirebaseReads(snapshot.size().coerceAtLeast(1))
                if (snapshot.documents.isNotEmpty()) {
                    paginationCursor = snapshot.documents.last()
                }
                _hasMorePosts.value = snapshot.size().toLong() >= POSTS_PAGE_SIZE
                val nextPosts = snapshot.documents.mapNotNull { parsePostDocument(it) }
                if (nextPosts.isNotEmpty()) {
                    onMorePostsLoaded(nextPosts)
                }
            }
            .addOnFailureListener { e ->
                _isLoadingMorePosts.value = false
                recordFirestoreFailure(e, OperationType.LIST, path)
            }
    }

    suspend fun fetchPostsPageAwait(limitCount: Long = POSTS_PAGE_SIZE): Result<List<Post>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        if (activeAuthUid() == null) {
            return Result.failure(IllegalStateException("User not signed in"))
        }
        return try {
            val snap = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
            ServiceQuotaTracker.recordFirebaseReads(snap.size().coerceAtLeast(1))
            Result.success(snap.documents.mapNotNull { parsePostDocument(it) })
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, "posts")
            Result.failure(e)
        }
    }

    // =========================================================================
    // Cloud Sync: Post Reactions, Share Count, Comments, Comment Replies, Reactions
    // =========================================================================

    fun reactToPostCloud(
        postId: String,
        user: User,
        newReaction: ReactionType?,
        updatedCounts: Map<ReactionType, Int>
    ) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val postRef = db.collection("posts").document(postId)
        val reactionRef = postRef.collection("reactions").document(uid)

        val serializedCounts = updatedCounts.mapKeys { it.key.name }.mapValues { it.value.toLong() }

        postRef.get().addOnSuccessListener { postSnap ->
            if (!postSnap.exists()) return@addOnSuccessListener
            val batch = db.batch()
            if (newReaction != null) {
                val reactionPayload = hashMapOf<String, Any>(
                    "userId" to uid,
                    "reactionType" to newReaction.name,
                    "userName" to user.fullName.take(100),
                    "userAvatar" to user.avatarUrl.take(1500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(reactionRef, reactionPayload)
            } else {
                batch.delete(reactionRef)
            }
            batch.update(
                postRef,
                mapOf(
                    "reactionCounts" to serializedCounts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.UPDATE, postRef.path)
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, postRef.path)
        }
    }

    suspend fun reactToPostCloudAwait(
        postId: String,
        user: User,
        newReaction: ReactionType?,
        updatedCounts: Map<ReactionType, Int>
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val uid = activeAuthUid() ?: return Result.failure(IllegalStateException("User not signed in"))
        val postRef = db.collection("posts").document(postId)
        val reactionRef = postRef.collection("reactions").document(uid)
        val serializedCounts = updatedCounts.mapKeys { it.key.name }.mapValues { it.value.toLong() }
        return try {
            val batch = db.batch()
            if (newReaction != null) {
                batch.set(
                    reactionRef,
                    mapOf(
                        "userId" to uid,
                        "reactionType" to newReaction.name,
                        "userName" to user.fullName.take(100),
                        "userAvatar" to user.avatarUrl.take(1500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.delete(reactionRef)
            }
            batch.update(
                postRef,
                mapOf(
                    "reactionCounts" to serializedCounts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.UPDATE, postRef.path)
            Result.failure(e)
        }
    }

    fun incrementShareCountCloud(postId: String) {
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val postRef = db.collection("posts").document(postId)
        postRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) return@addOnSuccessListener
            postRef.update(
                mapOf(
                    "shareCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.UPDATE, postRef.path)
            }
        }
    }

    fun saveComment(postId: String, comment: Comment) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val postRef = db.collection("posts").document(postId)
        val commentRef = postRef.collection("comments").document(comment.id)
        val commentData = hashMapOf<String, Any>(
            "postId" to postId,
            "authorId" to uid,
            "authorFullName" to comment.author.fullName.take(100),
            "authorAvatar" to comment.author.avatarUrl.take(1500),
            "text" to comment.text.take(2000),
            "timestamp" to comment.timestamp.take(100),
            "reactionCounts" to emptyMap<String, Long>(),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        postRef.get().addOnSuccessListener { postSnap ->
            val batch = db.batch()
            batch.set(commentRef, commentData)
            if (postSnap.exists()) {
                batch.update(
                    postRef,
                    mapOf(
                        "commentCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.CREATE, commentRef.path)
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, postRef.path)
        }
    }

    suspend fun saveCommentAwait(postId: String, comment: Comment): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val uid = activeAuthUid() ?: return Result.failure(IllegalStateException("User not signed in"))
        val commentRef = db.collection("posts").document(postId)
            .collection("comments").document(comment.id)
        return try {
            commentRef.set(
                mapOf(
                    "postId" to postId,
                    "authorId" to uid,
                    "authorFullName" to comment.author.fullName.take(100),
                    "authorAvatar" to comment.author.avatarUrl.take(1500),
                    "text" to comment.text.take(2000),
                    "timestamp" to comment.timestamp.take(100),
                    "reactionCounts" to emptyMap<String, Long>(),
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(comment.id)
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    fun reactToCommentCloud(
        postId: String,
        commentId: String,
        user: User,
        newReaction: ReactionType?,
        updatedCounts: Map<ReactionType, Int>
    ) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val commentRef = db.collection("posts").document(postId)
            .collection("comments").document(commentId)
        val reactionRef = commentRef.collection("reactions").document(uid)
        val serializedCounts = updatedCounts.mapKeys { it.key.name }.mapValues { it.value.toLong() }

        commentRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) return@addOnSuccessListener
            val batch = db.batch()
            if (newReaction != null) {
                batch.set(
                    reactionRef,
                    mapOf(
                        "userId" to uid,
                        "reactionType" to newReaction.name,
                        "userName" to user.fullName.take(100),
                        "userAvatar" to user.avatarUrl.take(1500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.delete(reactionRef)
            }
            batch.update(
                commentRef,
                mapOf(
                    "reactionCounts" to serializedCounts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.UPDATE, commentRef.path)
            }
        }
    }

    fun saveCommentReply(postId: String, commentId: String, reply: CommentReply) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val replyRef = db.collection("posts").document(postId)
            .collection("comments").document(commentId)
            .collection("replies").document(reply.id)
        val replyData = hashMapOf<String, Any>(
            "postId" to postId,
            "commentId" to commentId,
            "authorId" to uid,
            "authorFullName" to reply.author.fullName.take(100),
            "authorAvatar" to reply.author.avatarUrl.take(1500),
            "text" to reply.text.take(2000),
            "timestamp" to reply.timestamp.take(100),
            "reactionCounts" to emptyMap<String, Long>(),
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        replyRef.set(replyData).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.CREATE, replyRef.path)
        }
    }

    suspend fun saveCommentReplyAwait(postId: String, commentId: String, reply: CommentReply): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        val uid = activeAuthUid() ?: return Result.failure(IllegalStateException("User not signed in"))
        val replyRef = db.collection("posts").document(postId)
            .collection("comments").document(commentId)
            .collection("replies").document(reply.id)
        return try {
            replyRef.set(
                mapOf(
                    "postId" to postId,
                    "commentId" to commentId,
                    "authorId" to uid,
                    "authorFullName" to reply.author.fullName.take(100),
                    "authorAvatar" to reply.author.avatarUrl.take(1500),
                    "text" to reply.text.take(2000),
                    "timestamp" to reply.timestamp.take(100),
                    "reactionCounts" to emptyMap<String, Long>(),
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(reply.id)
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, replyRef.path)
            Result.failure(e)
        }
    }

    fun reactToReplyCloud(
        postId: String,
        commentId: String,
        replyId: String,
        user: User,
        newReaction: ReactionType?,
        updatedCounts: Map<ReactionType, Int>
    ) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val replyRef = db.collection("posts").document(postId)
            .collection("comments").document(commentId)
            .collection("replies").document(replyId)
        val reactionRef = replyRef.collection("reactions").document(uid)
        val serializedCounts = updatedCounts.mapKeys { it.key.name }.mapValues { it.value.toLong() }

        replyRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) return@addOnSuccessListener
            val batch = db.batch()
            if (newReaction != null) {
                batch.set(
                    reactionRef,
                    mapOf(
                        "userId" to uid,
                        "reactionType" to newReaction.name,
                        "userName" to user.fullName.take(100),
                        "userAvatar" to user.avatarUrl.take(1500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.delete(reactionRef)
            }
            batch.update(
                replyRef,
                mapOf(
                    "reactionCounts" to serializedCounts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.UPDATE, replyRef.path)
            }
        }
    }

    fun listenToPostCommentsAndReplies(
        postId: String,
        onCommentsLoaded: (List<Comment>, ReactionType?) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        val uid = activeAuthUid() ?: return null
        val commentsPath = "posts/$postId/comments"
        return db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(COMMENTS_PAGE_SIZE)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    recordFirestoreFailure(error, OperationType.LIST, commentsPath)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                ioScope.launch {
                    try {
                        val myPostRxSnap = try {
                            db.collection("posts").document(postId)
                                .collection("reactions").document(uid).get().await()
                        } catch (_: Exception) {
                            null
                        }
                        val myPostRx = myPostRxSnap?.getString("reactionType")
                            ?.let { name -> ReactionType.entries.firstOrNull { it.name == name } }

                        val parsedComments = snapshot.documents.mapNotNull { cDoc ->
                            val cAuthorId = cDoc.getString("authorId") ?: return@mapNotNull null
                            val cAuthor = User(
                                id = cAuthorId,
                                fullName = cDoc.getString("authorFullName") ?: "User",
                                username = cAuthorId,
                                avatarUrl = cDoc.getString("authorAvatar") ?: ""
                            )
                            val cRxCounts = parseReactionCounts(cDoc.get("reactionCounts"))
                            val myCommentRx = try {
                                cDoc.reference.collection("reactions").document(uid).get().await()
                                    .getString("reactionType")
                                    ?.let { name -> ReactionType.entries.firstOrNull { it.name == name } }
                            } catch (_: Exception) {
                                null
                            }

                            val repliesSnap = try {
                                cDoc.reference.collection("replies")
                                    .orderBy("createdAt", Query.Direction.ASCENDING)
                                    .limit(COMMENTS_PAGE_SIZE)
                                    .get().await()
                            } catch (_: Exception) {
                                null
                            }
                            val replies = repliesSnap?.documents?.mapNotNull { rDoc ->
                                val rAuthorId = rDoc.getString("authorId") ?: return@mapNotNull null
                                val rRxCounts = parseReactionCounts(rDoc.get("reactionCounts"))
                                val myReplyRx = try {
                                    rDoc.reference.collection("reactions").document(uid).get().await()
                                        .getString("reactionType")
                                        ?.let { name -> ReactionType.entries.firstOrNull { it.name == name } }
                                } catch (_: Exception) {
                                    null
                                }
                                CommentReply(
                                    id = rDoc.id,
                                    author = User(
                                        id = rAuthorId,
                                        fullName = rDoc.getString("authorFullName") ?: "User",
                                        username = rAuthorId,
                                        avatarUrl = rDoc.getString("authorAvatar") ?: ""
                                    ),
                                    text = rDoc.getString("text") ?: "",
                                    timestamp = rDoc.getString("timestamp") ?: "Just now",
                                    likeCount = rRxCounts.values.sum(),
                                    isLiked = myReplyRx != null,
                                    userReaction = myReplyRx,
                                    reactionCounts = rRxCounts
                                )
                            } ?: emptyList()

                            Comment(
                                id = cDoc.id,
                                postId = postId,
                                author = cAuthor,
                                text = cDoc.getString("text") ?: "",
                                timestamp = cDoc.getString("timestamp") ?: "Just now",
                                likeCount = cRxCounts.values.sum(),
                                isLiked = myCommentRx != null,
                                userReaction = myCommentRx,
                                reactionCounts = cRxCounts,
                                replies = replies
                            )
                        }
                        onCommentsLoaded(parsedComments, myPostRx)
                    } catch (e: Exception) {
                        recordFirestoreFailure(e, OperationType.LIST, commentsPath)
                    }
                }
            }
    }

    // =========================================================================
    // Stories
    // =========================================================================

    fun saveStory(story: Story) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val storyRef = db.collection("stories").document(story.id)
        try {
            val storyData = hashMapOf<String, Any?>(
                "authorId" to uid,
                "authorFullName" to story.author.fullName.take(100),
                "authorUsername" to story.author.username.take(80),
                "authorAvatar" to story.author.avatarUrl.take(1500),
                "mediaUrl" to story.mediaUrl.take(1500),
                "caption" to story.caption.take(500),
                "musicTrack" to (story.musicTrack ?: "").take(200),
                "musicArtist" to (story.musicArtist ?: "").take(150),
                "audioUrl" to (story.audioUrl ?: "").take(1500),
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            storyRef.set(storyData).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.CREATE, storyRef.path)
            }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, storyRef.path)
        }
    }

    private fun parseStoryDocument(doc: DocumentSnapshot): Story? {
        return try {
            val authorId = doc.getString("authorId") ?: return null
            val mediaUrl = doc.getString("mediaUrl")?.takeIf { it.isNotBlank() } ?: return null
            val authorName = doc.getString("authorFullName") ?: "FotoFilm User"
            val authorUsername = doc.getString("authorUsername") ?: authorId
            val authorAvatar = doc.getString("authorAvatar").orEmpty()
            Story(
                id = doc.id,
                author = User(
                    id = authorId,
                    fullName = authorName,
                    username = authorUsername,
                    avatarUrl = authorAvatar
                ),
                mediaUrl = mediaUrl,
                timestamp = "Just now",
                caption = doc.getString("caption").orEmpty(),
                musicTrack = doc.getString("musicTrack")?.takeIf { it.isNotBlank() },
                musicArtist = doc.getString("musicArtist")?.takeIf { it.isNotBlank() },
                audioUrl = doc.getString("audioUrl")?.takeIf { it.isNotBlank() }
            )
        } catch (_: Exception) {
            null
        }
    }

    fun listenToStories(onStoriesUpdated: (List<Story>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val path = "stories"
        return try {
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(COLLECTIONS_PAGE_SIZE)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val readDocs = snapshots.documentChanges.size.coerceAtLeast(
                            if (snapshots.metadata.isFromCache) 0 else snapshots.size()
                        )
                        if (readDocs > 0) {
                            ServiceQuotaTracker.recordFirebaseReads(readDocs)
                        }
                        val stories = snapshots.documents.mapNotNull { parseStoryDocument(it) }
                        onStoriesUpdated(stories)
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    suspend fun fetchStoriesAwait(limitCount: Long = COLLECTIONS_PAGE_SIZE): Result<List<Story>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        if (activeAuthUid() == null) {
            return Result.failure(IllegalStateException("User not signed in"))
        }
        return try {
            val snap = db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limitCount)
                .get()
                .await()
            ServiceQuotaTracker.recordFirebaseReads(snap.size().coerceAtLeast(1))
            Result.success(snap.documents.mapNotNull { parseStoryDocument(it) })
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, "stories")
            Result.failure(e)
        }
    }

    // =========================================================================
    // ChatFilm Conversations & Messages (Participant-Isolated, Bounded)
    // =========================================================================

    fun sendMessage(conversationId: String, message: ChatMessage, recipientId: String = "user_tanvir") {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val now = System.currentTimeMillis()
        val reverseTsPrefix = String.format(Locale.US, "%013d", 9_999_999_999_999L - now)
        val safeDocId = "${reverseTsPrefix}_${message.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}".take(120)
        val participants = listOf(uid, recipientId.ifBlank { uid }).distinct().take(50)

        val convRef = db.collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document(safeDocId)

        val msgData = hashMapOf<String, Any>(
            "conversationId" to conversationId,
            "senderId" to uid,
            "senderName" to message.senderName.take(100),
            "participantIds" to participants,
            "text" to message.text.take(5000),
            "mediaUrl" to (message.mediaUrl ?: "").take(1500),
            "isMediaVideo" to message.isMediaVideo,
            "isAudioVoice" to message.isAudioVoice,
            "audioDurationSeconds" to message.audioDurationSeconds.coerceIn(0, 3600),
            "timestamp" to message.timestamp.take(100),
            "createdAt" to now,
            "updatedAt" to FieldValue.serverTimestamp(),
            "callLogType" to (message.callLogType?.name ?: ""),
            "callDurationSeconds" to message.callDurationSeconds.coerceIn(0, 86400),
            "isMissedCall" to message.isMissedCall
        )

        convRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                convRef.set(
                    mapOf(
                        "createdBy" to uid,
                        "participantIds" to participants,
                        "isGroup" to false,
                        "lastMessage" to message.text.take(1000),
                        "createdAt" to now,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).addOnFailureListener { e ->
                    recordFirestoreFailure(e, OperationType.CREATE, convRef.path)
                }
            }
            msgRef.set(msgData).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.CREATE, msgRef.path)
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, convRef.path)
        }
    }

    fun listenToChatMessages(
        conversationId: String,
        onMessagesUpdated: (List<ChatMessage>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        val uid = activeAuthUid() ?: return null
        val path = "conversations/$conversationId/messages"
        return try {
            db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereArrayContains("participantIds", uid)
                .limit(MESSAGES_PAGE_SIZE)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val msgList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val senderId = doc.getString("senderId") ?: ""
                                val createdAt = doc.getLong("createdAt") ?: 0L
                                Pair(
                                    createdAt,
                                    ChatMessage(
                                        id = doc.id,
                                        senderId = senderId,
                                        senderName = doc.getString("senderName") ?: "",
                                        text = doc.getString("text") ?: "",
                                        timestamp = doc.getString("timestamp") ?: "Just now",
                                        mediaUrl = doc.getString("mediaUrl")?.takeIf { it.isNotBlank() },
                                        isMediaVideo = doc.getBoolean("isMediaVideo") ?: false,
                                        isAudioVoice = doc.getBoolean("isAudioVoice") ?: false,
                                        audioDurationSeconds = (doc.getLong("audioDurationSeconds") ?: 0L).toInt(),
                                        isFromMe = senderId == uid,
                                        status = MessageDeliveryStatus.DELIVERED,
                                        callLogType = doc.getString("callLogType")?.takeIf { it.isNotBlank() }?.let { typeStr ->
                                            CallType.entries.firstOrNull { it.name == typeStr }
                                        },
                                        callDurationSeconds = (doc.getLong("callDurationSeconds") ?: 0L).toInt(),
                                        isMissedCall = doc.getBoolean("isMissedCall") ?: false
                                    )
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedBy { it.first }.map { it.second }
                        if (msgList.isNotEmpty()) {
                            onMessagesUpdated(msgList)
                        }
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    // =========================================================================
    // Groups, Pages, Music Tracks (Paginated .limit(30))
    // =========================================================================

    fun saveGroup(groupId: String, groupData: Map<String, Any>) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val groupRef = db.collection("groups").document(groupId)
        val safePayload = hashMapOf<String, Any>(
            "creatorId" to uid,
            "name" to ((groupData["name"] as? String) ?: "FotoFilm Group").take(120),
            "description" to ((groupData["description"] as? String) ?: "").take(1000),
            "coverUrl" to ((groupData["coverUrl"] as? String) ?: "").take(1500),
            "memberCount" to ((groupData["memberCount"] as? Number)?.toLong() ?: 1L),
            "isPrivate" to (groupData["isPrivate"] as? Boolean ?: false),
            "isJoined" to true,
            "creatorName" to ((groupData["creatorName"] as? String) ?: (groupData["creator"] as? String) ?: "FotoFilm User").take(100),
            "isActive" to true,
            "createdAt" to "০১/০১/২০২৬ (1/1/2026)",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        groupRef.set(safePayload).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.CREATE, groupRef.path)
        }
    }

    fun saveSocialGroup(group: SocialGroup) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val groupRef = db.collection("groups").document(group.id)
        try {
            val groupData = hashMapOf<String, Any>(
                "creatorId" to uid,
                "name" to group.name.take(120),
                "description" to group.description.take(1000),
                "coverUrl" to group.coverUrl.take(1500),
                "memberCount" to group.memberCount,
                "isPrivate" to group.isPrivate,
                "isJoined" to group.isJoined,
                "creatorName" to group.creatorName.take(100),
                "isActive" to group.isActive,
                "createdAt" to group.createdAt.take(100),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            groupRef.set(groupData).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.CREATE, groupRef.path)
            }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, groupRef.path)
        }
    }

    fun listenToGroups(onGroupsUpdated: (List<SocialGroup>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val path = "groups"
        return try {
            db.collection(path)
                .limit(COLLECTIONS_PAGE_SIZE)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val groupsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                SocialGroup(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "FotoFilm Group",
                                    description = doc.getString("description") ?: "",
                                    coverUrl = doc.getString("coverUrl") ?: "",
                                    memberCount = (doc.getLong("memberCount") ?: 1L).toInt(),
                                    isPrivate = doc.getBoolean("isPrivate") ?: false,
                                    isJoined = doc.getBoolean("isJoined") ?: false,
                                    creatorName = doc.getString("creatorName") ?: "Niyon Islam Sohag",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    createdAt = doc.getString("createdAt") ?: "০১/০১/২০২৬ (1/1/2026)"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (groupsList.isNotEmpty()) {
                            onGroupsUpdated(groupsList)
                        }
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    fun saveSocialPage(page: SocialPage) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val pageRef = db.collection("pages").document(page.id)
        try {
            val pageData = hashMapOf<String, Any>(
                "creatorId" to uid,
                "name" to page.name.take(120),
                "category" to page.category.take(100),
                "avatarUrl" to page.avatarUrl.take(1500),
                "coverUrl" to page.coverUrl.take(1500),
                "followerCount" to page.followerCount,
                "isLiked" to page.isLiked,
                "isVerified" to page.isVerified,
                "creatorName" to page.creatorName.take(100),
                "isActive" to page.isActive,
                "createdAt" to page.createdAt.take(100),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            pageRef.set(pageData).addOnFailureListener { e ->
                recordFirestoreFailure(e, OperationType.CREATE, pageRef.path)
            }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, pageRef.path)
        }
    }

    fun listenToPages(onPagesUpdated: (List<SocialPage>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val path = "pages"
        return try {
            db.collection(path)
                .limit(COLLECTIONS_PAGE_SIZE)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val pagesList = snapshot.documents.mapNotNull { doc ->
                            try {
                                SocialPage(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "FotoFilm Page",
                                    category = doc.getString("category") ?: "Personal blog",
                                    avatarUrl = doc.getString("avatarUrl") ?: "",
                                    coverUrl = doc.getString("coverUrl") ?: "",
                                    followerCount = (doc.getLong("followerCount") ?: 0L).toInt(),
                                    isLiked = doc.getBoolean("isLiked") ?: false,
                                    isVerified = doc.getBoolean("isVerified") ?: true,
                                    creatorName = doc.getString("creatorName") ?: "Niyon Islam Sohag",
                                    isActive = doc.getBoolean("isActive") ?: true,
                                    createdAt = doc.getString("createdAt") ?: "০১/০১/২০২৬ (1/1/2026)"
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (pagesList.isNotEmpty()) {
                            onPagesUpdated(pagesList)
                        }
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    fun saveMusicTrack(track: MusicTrack, onComplete: ((Boolean) -> Unit)? = null) {
        val db = firestore
        val uid = activeAuthUid()
        if (db == null || uid == null) {
            onComplete?.invoke(false)
            return
        }
        val trackRef = db.collection("music_tracks").document(track.id)
        try {
            val trackData = hashMapOf<String, Any>(
                "uploaderId" to uid,
                "title" to track.title.take(200),
                "artist" to track.artist.take(150),
                "views" to track.views.take(40),
                "thumbnailUrl" to track.thumbnailUrl.take(1500),
                "audioUrl" to track.audioUrl.take(1500),
                "lyrics" to track.lyrics.take(5000),
                "category" to track.category.take(80),
                "duration" to track.duration.take(30),
                "isTrending" to track.isTrending,
                "addedBy" to track.addedBy.take(100),
                "createdAt" to track.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            trackRef.set(trackData)
                .addOnSuccessListener {
                    onComplete?.invoke(true)
                }
                .addOnFailureListener { e ->
                    recordFirestoreFailure(e, OperationType.CREATE, trackRef.path)
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.CREATE, trackRef.path)
            onComplete?.invoke(false)
        }
    }

    fun listenToMusicTracks(onTracksUpdated: (List<MusicTrack>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val path = "music_tracks"
        return try {
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(COLLECTIONS_PAGE_SIZE)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        val tracks = snapshots.documents.mapNotNull { doc ->
                            try {
                                MusicTrack(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "অজানা গান",
                                    artist = doc.getString("artist") ?: "Niyon Islam Sohag",
                                    views = doc.getString("views") ?: "12K",
                                    thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                                    audioUrl = doc.getString("audioUrl") ?: "",
                                    lyrics = doc.getString("lyrics") ?: "",
                                    category = doc.getString("category") ?: "For you",
                                    duration = doc.getString("duration") ?: "3:45",
                                    isTrending = doc.getBoolean("isTrending") ?: false,
                                    addedBy = doc.getString("addedBy") ?: "Niyon Islam Sohag",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (tracks.isNotEmpty()) {
                            onTracksUpdated(tracks)
                        }
                    } else if (snapshots != null && snapshots.isEmpty) {
                        seedDefaultMusicTracks()
                    }
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    fun seedDefaultMusicTracks() {
        if (activeAuthUid() == null) return
        getDefaultMusicTracks().forEach { track ->
            saveMusicTrack(track)
        }
    }

    fun getDefaultMusicTracks(): List<MusicTrack> {
        return listOf(
            MusicTrack(
                id = "track_palli_flute_1",
                title = "Palli Gramer Bashir Sur",
                artist = "Niyon Islam Sohag",
                views = "12K",
                lyrics = "বাঁশির সুরে মন ভরে যায়, মন ছুটে যায় দূর গাঁয়ে... মেঠো পথ আর নদীর বাঁকে বাজে মধুর সুর।",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/foley/wind_chimes_breeze.ogg",
                category = "বাঁশির সুর",
                duration = "3:30",
                isTrending = true,
                addedBy = "Niyon Islam Sohag"
            ),
            MusicTrack(
                id = "track_hasbi_rabbi_2",
                title = "Hasbi Rabbi Jallallah (ইসলামিক গজল)",
                artist = "Sami Yusuf & Niyon",
                views = "1.4M",
                lyrics = "হাসবি রাব্বি জাল্লাল্লাহ, মাফি কালবি গাইরুল্লাহ, নূর মুহাম্মদ সাল্লাল্লাহ...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/ambiences/humming_in_the_hall.ogg",
                category = "গজল",
                duration = "4:15",
                isTrending = true,
                addedBy = "Niyon Islam Sohag"
            ),
            MusicTrack(
                id = "track_dj_party_3",
                title = "Bangla DJ Remix 2026 Party Mix",
                artist = "DJ Sonik BD",
                views = "850K",
                lyrics = "ঝাঁকাস ড্রামস ও বিটে মেতে ওঠো সবাই, পার্টি নাইট কাঁপিয়ে দাও...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/transportation/car_passing_by.ogg",
                category = "ডিজে গান",
                duration = "2:55",
                isTrending = true,
                addedBy = "FotoFilm Admin"
            ),
            MusicTrack(
                id = "track_neel_doriya_4",
                title = "O Re Neel Doriya (পল্লীগীতি)",
                artist = "Abdul Alim",
                views = "2.1M",
                lyrics = "এই কূল ভাঙ্গে ও কূল গড়ে এই তো নদীর খেলা, সকাল বেলার ধনী রে ভাই বিকাল বেলার ফকির...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/foley/water_lapping.ogg",
                category = "For you",
                duration = "4:20",
                isTrending = false,
                addedBy = "Niyon Islam Sohag"
            ),
            MusicTrack(
                id = "track_reels_viral_5",
                title = "Trending Short Video Beat 2026",
                artist = "FotoFilm Beats",
                views = "3.2M",
                lyrics = "Trending viral audio on Short Videos, dance and vibe...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/musical_instruments/bass_riff.ogg",
                category = "Trending",
                duration = "1:45",
                isTrending = true,
                addedBy = "FotoFilm Official"
            ),
            MusicTrack(
                id = "track_flute_shur_6",
                title = "Bhatiyali Flute Symphony",
                artist = "Niyon Islam Sohag",
                views = "45K",
                lyrics = "মন মাঝি তোর বৈঠা নে রে, আমি আর বাইতে পারলাম না...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/foley/wind_chimes_breeze.ogg",
                category = "বাঁশির সুর",
                duration = "3:50",
                isTrending = false,
                addedBy = "Niyon Islam Sohag"
            ),
            MusicTrack(
                id = "track_lalon_7",
                title = "Milon Hobe Koto Dine (লালনগীতি)",
                artist = "Farida Parveen",
                views = "950K",
                lyrics = "মিলন হবে কত দিনে, আমার মনের মানুষের সনে...",
                thumbnailUrl = "",
                audioUrl = "https://actions.google.com/sounds/v1/foley/wind_chimes_breeze.ogg",
                category = "For you",
                duration = "5:12",
                isTrending = false,
                addedBy = "FotoFilm Music"
            )
        )
    }

    // =========================================================================
    // Ludo Multiplayer Rooms
    // =========================================================================

    fun createOrJoinLudoRoom(
        roomId: String,
        user: User,
        color: String,
        onRoomUpdate: (Map<String, Any>) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        val uid = activeAuthUid() ?: return null
        val roomRef = db.collection("ludo_rooms").document(roomId)

        val playerMap = hashMapOf<String, Any>(
            "id" to uid,
            "name" to user.fullName.take(100),
            "avatar" to user.avatarUrl.take(1500),
            "color" to color,
            "isOnline" to true
        )

        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val initialData = hashMapOf<String, Any>(
                    "roomId" to roomId,
                    "hostId" to uid,
                    "hostName" to user.fullName.take(100),
                    "players" to listOf(playerMap),
                    "turnColor" to "BLUE",
                    "diceValue" to 1,
                    "isDiceRolled" to false,
                    "status" to "WAITING",
                    "lastUpdated" to System.currentTimeMillis(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                roomRef.set(initialData).addOnFailureListener { e ->
                    recordFirestoreFailure(e, OperationType.CREATE, roomRef.path)
                }
            } else {
                @Suppress("UNCHECKED_CAST")
                val existingPlayers = (snapshot.get("players") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                if (existingPlayers.none { it["id"] == uid } && existingPlayers.size < 4) {
                    existingPlayers.add(playerMap)
                    roomRef.update(
                        mapOf(
                            "players" to existingPlayers,
                            "status" to if (existingPlayers.size >= 2) "PLAYING" else "WAITING",
                            "lastUpdated" to System.currentTimeMillis(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).addOnFailureListener { e ->
                        recordFirestoreFailure(e, OperationType.UPDATE, roomRef.path)
                    }
                }
            }
        }.addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.GET, roomRef.path)
        }

        return roomRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                recordFirestoreFailure(error, OperationType.GET, roomRef.path)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                onRoomUpdate(data)
            }
        }
    }

    fun syncLudoTurnAndDice(roomId: String, dice: Int, turnColor: String, isDiceRolled: Boolean) {
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val roomRef = db.collection("ludo_rooms").document(roomId)
        roomRef.update(
            mapOf(
                "diceValue" to dice.coerceIn(1, 6),
                "turnColor" to turnColor,
                "isDiceRolled" to isDiceRolled,
                "lastUpdated" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.UPDATE, roomRef.path)
        }
    }

    fun syncLudoTokens(roomId: String, tokenDataList: List<Map<String, Any>>, turnColor: String, isDiceRolled: Boolean) {
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val roomRef = db.collection("ludo_rooms").document(roomId)
        roomRef.update(
            mapOf(
                "tokens" to tokenDataList.take(16),
                "turnColor" to turnColor,
                "isDiceRolled" to isDiceRolled,
                "lastUpdated" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.UPDATE, roomRef.path)
        }
    }

    // =========================================================================
    // Real-Time Agora Call Signaling (/call_signals/{callId})
    // =========================================================================

    data class IncomingCallSignal(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerAvatar: String,
        val calleeId: String,
        val calleeName: String,
        val channelName: String,
        val callType: CallType,
        val groupName: String?,
        val status: String,
        val createdAt: Long
    )

    fun sendCallSignal(
        callId: String,
        caller: User,
        callee: User,
        channelName: String,
        callType: CallType,
        groupName: String? = null
    ) {
        val db = firestore ?: return
        val uid = activeAuthUid() ?: return
        val safeCallId = callId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(100)
        val safeCalleeId = callee.id.ifBlank { "friend" }.take(120)
        val docRef = db.collection("call_signals").document(safeCallId)
        val now = System.currentTimeMillis()
        val data = hashMapOf<String, Any?>(
            "callId" to safeCallId,
            "callerId" to uid,
            "callerName" to caller.fullName.take(100),
            "callerAvatar" to caller.avatarUrl.take(1500),
            "calleeId" to safeCalleeId,
            "calleeName" to callee.fullName.take(100),
            "channelName" to channelName.take(100),
            "callType" to callType.name,
            "groupName" to groupName?.take(120),
            "status" to "RINGING",
            "createdAt" to now,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.CREATE, docRef.path)
        }
    }

    fun updateCallSignalStatus(callId: String, status: String) {
        if (callId.isBlank()) return
        val db = firestore ?: return
        if (activeAuthUid() == null) return
        val safeCallId = callId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(100)
        val docRef = db.collection("call_signals").document(safeCallId)
        docRef.update(
            mapOf(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { e ->
            recordFirestoreFailure(e, OperationType.UPDATE, docRef.path)
        }
    }

    fun listenToIncomingCalls(
        onIncomingCall: (IncomingCallSignal?) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        val uid = activeAuthUid() ?: return null
        val path = "call_signals"
        return try {
            db.collection(path)
                .whereEqualTo("calleeId", uid)
                .limit(10)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        recordFirestoreFailure(error, OperationType.LIST, path)
                        return@addSnapshotListener
                    }
                    val now = System.currentTimeMillis()
                    val latestRinging = snapshots?.documents
                        ?.mapNotNull { doc ->
                            val status = doc.getString("status") ?: return@mapNotNull null
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            if (status != "RINGING" || (now - createdAt) > 120_000L) {
                                return@mapNotNull null
                            }
                            val typeStr = doc.getString("callType") ?: "AUDIO"
                            val callType = when (typeStr) {
                                "VIDEO" -> CallType.VIDEO
                                "GROUP" -> CallType.GROUP
                                else -> CallType.AUDIO
                            }
                            IncomingCallSignal(
                                callId = doc.getString("callId") ?: doc.id,
                                callerId = doc.getString("callerId") ?: "",
                                callerName = doc.getString("callerName") ?: "FotoFilm Friend",
                                callerAvatar = doc.getString("callerAvatar") ?: "",
                                calleeId = doc.getString("calleeId") ?: uid,
                                calleeName = doc.getString("calleeName") ?: "",
                                channelName = doc.getString("channelName") ?: "",
                                callType = callType,
                                groupName = doc.getString("groupName"),
                                status = status,
                                createdAt = createdAt
                            )
                        }
                        ?.maxByOrNull { it.createdAt }
                    onIncomingCall(latestRinging)
                }
        } catch (e: Exception) {
            recordFirestoreFailure(e, OperationType.LIST, path)
            null
        }
    }

    fun listenToOutgoingCallStatus(
        callId: String,
        onStatusChanged: (String) -> Unit
    ): ListenerRegistration? {
        if (callId.isBlank()) return null
        val db = firestore ?: return null
        if (activeAuthUid() == null) return null
        val safeCallId = callId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(100)
        val docRef = db.collection("call_signals").document(safeCallId)
        return docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                recordFirestoreFailure(error, OperationType.GET, docRef.path)
                return@addSnapshotListener
            }
            val status = snapshot?.getString("status") ?: return@addSnapshotListener
            onStatusChanged(status)
        }
    }
}
