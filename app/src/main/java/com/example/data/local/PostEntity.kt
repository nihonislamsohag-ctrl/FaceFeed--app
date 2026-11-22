package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSaved: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val postJson: String
)

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAllRecords(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE isHidden = 0 ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getVisiblePosts(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE isSaved = 1 AND isHidden = 0 ORDER BY createdAt DESC")
    suspend fun getSavedPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("UPDATE posts SET isHidden = 1, updatedAt = :updatedAt WHERE id = :postId")
    suspend fun markPostHidden(postId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
}
