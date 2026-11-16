package com.example.data

import android.content.Context
import com.example.ui.screens.FidaAIMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * FidaAIPersistenceManager:
 * - Persists all Fida AI messages permanently in SharedPreferences so conversations are never lost
 * - Provides strict answers for Right Manager Fida query & App Creator query according to policy
 */
object FidaAIPersistenceManager {
    private const val PREFS_NAME = "fida_ai_chat_persistence_store"
    private const val KEY_SAVED_CHATS = "saved_chat_history_v1"

    fun isRightManagerQuery(query: String): Boolean {
        val q = query.lowercase().trim()
        return q.contains("রাইড মেনেজার") ||
                q.contains("রাইড ম্যানেজার") ||
                q.contains("রাইট ম্যানেজার") ||
                q.contains("রাইট মেনেজার") ||
                q.contains("right manager") ||
                q.contains("ride manager") ||
                q.contains("মেনেজার ফিদা") ||
                q.contains("ম্যানেজার ফিদা") ||
                q.contains("manager fida") ||
                q.contains("ফিদা রাইড") ||
                q.contains("ফিদা রাইট")
    }

    fun isAppCreatorQuery(query: String): Boolean {
        val q = query.lowercase().trim()
        return q.contains("তৈরি করছে কে") ||
                q.contains("তৈরি করেছে কে") ||
                q.contains("কে তৈরি করছে") ||
                q.contains("কে তৈরি করেছে") ||
                q.contains("বানিয়েছে কে") ||
                q.contains("কে বানিয়েছে") ||
                q.contains("বানাইছে কে") ||
                q.contains("কে বানাইছে") ||
                q.contains("কে বানালো") ||
                q.contains("এই অ্যাপ তৈরি করছে কে") ||
                q.contains("অ্যাপ তৈরি করছে কে") ||
                q.contains("অ্যাপ কে তৈরি করছে") ||
                q.contains("অ্যাপ কে বানিয়েছে") ||
                q.contains("অ্যাপের মালিক কে") ||
                q.contains("ডেভেলপার কে") ||
                q.contains("কার অ্যাপ") ||
                q.contains("who made this app") ||
                q.contains("who created this app") ||
                q.contains("who is the developer") ||
                q.contains("who developed") ||
                q.contains("creator of this app") ||
                q.contains("sohag") ||
                q.contains("niyon") ||
                q.contains("সোহাগ") ||
                q.contains("নয়ন") ||
                q.contains("নয়ন") ||
                q.contains("fotofilm") ||
                q.contains("ফটোফিল্ম") ||
                q.contains("gramfilm") ||
                q.contains("গ্রামফিল্ম") ||
                q.contains("prolevelcut") ||
                q.contains("প্রোলেভেলকাট") ||
                q.contains("৩ টা অ্যাপ") ||
                q.contains("তিনটি অ্যাপ")
    }

    val RIGHT_MANAGER_ANSWER = "আমি ফিদা রাইড মেনেজার সম্পর্কে আমি কীছু জানি না"

    val APP_CREATOR_ANSWER = "Niyon Islam Sohag ভাই FotoFilm ও GramFilm ও ProLevelCut এই ৩ টা অ্যাপ তৈরি করেছেন। Niyon Islam Sohag ভাইকে অসংখ্য ধন্যবাদ বাংলাদেশে এতো সুন্দর ৩ টা প্ল্যাটফর্ম তৈরি করার জন্য!\n\n• FotoFilm হলো সামাজিক যোগাযোগ মাধ্যম।\n• ভিআইপি এদের জন্য GramFilm অ্যাপ।\n• ভিডিও এডিট করার জন্য ProLevelCut অ্যাপ।"

    fun loadMessages(context: Context): List<FidaAIMessage> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_SAVED_CHATS, null)
            if (jsonStr.isNullOrBlank()) {
                getDefaultInitialMessages()
            } else {
                val jsonArray = JSONArray(jsonStr)
                val list = mutableListOf<FidaAIMessage>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        FidaAIMessage(
                            id = obj.optString("id", System.currentTimeMillis().toString()),
                            isUser = obj.optBoolean("isUser", false),
                            text = obj.optString("text", ""),
                            imageUri = null,
                            timestamp = obj.optString("timestamp", "এখন"),
                            isLiked = if (obj.has("isLiked")) obj.optBoolean("isLiked") else null
                        )
                    )
                }
                if (list.isEmpty()) getDefaultInitialMessages() else list
            }
        } catch (e: Exception) {
            getDefaultInitialMessages()
        }
    }

    fun saveAllMessages(context: Context, messages: List<FidaAIMessage>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            // Keep up to 200 messages so conversation history stays permanently intact
            messages.takeLast(200).forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("isUser", msg.isUser)
                    put("text", msg.text)
                    put("timestamp", msg.timestamp)
                    if (msg.isLiked != null) {
                        put("isLiked", msg.isLiked)
                    }
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_SAVED_CHATS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun appendMessage(context: Context, message: FidaAIMessage) {
        val current = loadMessages(context).toMutableList()
        current.add(message)
        saveAllMessages(context, current)
    }

    fun getDefaultInitialMessages(): List<FidaAIMessage> {
        return listOf(
            FidaAIMessage(
                isUser = false,
                text = "আসসালামু আলাইকুম! আমি FotoFilm **ফিদা এআই (Fida AI)**। 🌸\n\nআপনি আমাকে যেকোনো প্রশ্ন বাংলায় বা ইংরেজিতে জিজ্ঞাসা করতে পারেন। অথবা আপনার ফোন থেকে কোনো ছবি বা স্ক্রিনশট যুক্ত করে প্রশ্ন করতে পারেন — আমি সাথে সাথে নির্ভুল সমাধান ও উত্তর দিব!"
            )
        )
    }
}
