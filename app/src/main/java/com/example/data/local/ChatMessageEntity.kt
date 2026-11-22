package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val createdAt: Long,
    val isDeleted: Boolean = false,
    val messageJson: String
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY createdAt ASC")
    suspend fun getAllRecords(): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun markMessageDeleted(messageId: String)

    @Query("UPDATE chat_messages SET isDeleted = 1 WHERE conversationId = :conversationId")
    suspend fun markConversationDeleted(conversationId: String)
}
