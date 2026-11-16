package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.Comment
import com.example.model.CommentReply
import com.example.model.Post
import com.example.model.ReactionType
import com.example.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FirestoreSocialRulesRuleTest : FirestoreEmulatorTestBase() {

    @Before
    fun bindFirebaseManager() {
        FirebaseManager.setInstancesForTesting(firestore, auth)
    }

    @Test
    fun saveAndFetchPaginatedPost_authenticatedUser_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val postId = "post_${UUID.randomUUID()}"
        val alice = User(id = aliceUid, fullName = "Alice", username = "alice")
        val post = Post(
            id = postId,
            author = alice,
            timestamp = "Just now",
            text = "Hello FotoFilm Cloud!",
            reactionCounts = mapOf(ReactionType.LIKE to 1),
            createdAt = System.currentTimeMillis()
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { FirebaseManager.savePostAwait(post) }
        assertTrue("Expected savePostAwait to succeed", saveResult.isSuccess)

        val pageResult = withTimeout(DEFAULT_TIMEOUT_MS) { FirebaseManager.fetchPostsPageAwait(30L) }
        assertTrue("Expected fetchPostsPageAwait to succeed", pageResult.isSuccess)
        assertTrue(pageResult.getOrThrow().any { it.id == postId })
    }

    @Test
    fun reactAndReplyOnPost_crossUserAuthenticated_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val postId = "post_${UUID.randomUUID()}"
        val commentId = "c_${UUID.randomUUID()}"
        val replyId = "cr_${UUID.randomUUID()}"

        val alice = User(id = aliceUid, fullName = "Alice", username = "alice")
        val post = Post(
            id = postId,
            author = alice,
            timestamp = "Just now",
            text = "Cloud Sync Test Post",
            reactionCounts = mapOf(ReactionType.LIKE to 1),
            createdAt = System.currentTimeMillis()
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { FirebaseManager.savePostAwait(post).getOrThrow() }

        // Bob reacts, comments, and replies on Alice's post
        val bobUid = signInTestUser(BOB_EMAIL)
        val bob = User(id = bobUid, fullName = "Bob", username = "bob")

        val reactRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            FirebaseManager.reactToPostCloudAwait(
                postId = postId,
                user = bob,
                newReaction = ReactionType.LOVE,
                updatedCounts = mapOf(ReactionType.LIKE to 1, ReactionType.LOVE to 1)
            )
        }
        assertTrue("Expected reaction cloud sync to succeed", reactRes.isSuccess)

        val commentRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            FirebaseManager.saveCommentAwait(
                postId = postId,
                comment = Comment(id = commentId, postId = postId, author = bob, text = "Great post!", timestamp = "Now")
            )
        }
        assertTrue("Expected comment cloud sync to succeed", commentRes.isSuccess)

        val replyRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            FirebaseManager.saveCommentReplyAwait(
                postId = postId,
                commentId = commentId,
                reply = CommentReply(id = replyId, author = bob, text = "Replying across devices!", timestamp = "Now")
            )
        }
        assertTrue("Expected reply cloud sync to succeed", replyRes.isSuccess)
    }

    @Test
    fun editAnotherUsersPostText_crossUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val postId = "post_${UUID.randomUUID()}"
        val post = Post(
            id = postId,
            author = User(id = aliceUid, fullName = "Alice", username = "alice"),
            timestamp = "Just now",
            text = "Immutable by others",
            createdAt = System.currentTimeMillis()
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { FirebaseManager.savePostAwait(post).getOrThrow() }

        signInTestUser(BOB_EMAIL)
        var caught: Throwable? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("posts").document(postId)
                    .update(
                        mapOf(
                            "text" to "Tampered text",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
        } catch (t: Throwable) {
            caught = t
        }

        val fsEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException when tampering another user's post", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
    }

    @Test
    fun queryPosts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        var caught: Throwable? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("posts").limit(10).get().await()
            }
        } catch (t: Throwable) {
            caught = t
        }
        val fsEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException for unauthenticated read", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
