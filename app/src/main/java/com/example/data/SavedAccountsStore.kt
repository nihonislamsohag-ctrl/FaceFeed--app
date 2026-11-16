package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedEmailAccount(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val avatarUrl: String,
    val isFirebaseLinked: Boolean = false,
    val isOwner: Boolean = false,
    val lastUsedAt: Long = System.currentTimeMillis()
)

object SavedAccountsStore {
    private const val PREFS_NAME = "fotofilm_saved_accounts"
    private const val KEY_ACCOUNTS_JSON = "accounts_json"

    private fun defaultSeedAccounts(): List<SavedEmailAccount> = listOf(
        SavedEmailAccount(
            id = "seed_niyon_1",
            fullName = "Niyon Islam Sohag",
            username = "niyonislamsohag",
            email = "smd224292@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            isFirebaseLinked = true,
            isOwner = true,
            lastUsedAt = 2L
        ),
        SavedEmailAccount(
            id = "seed_niyon_2",
            fullName = "Niyon Islam Sohag",
            username = "niyon_sohag",
            email = "nihonislamsohag@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            isFirebaseLinked = true,
            isOwner = true,
            lastUsedAt = 1L
        )
    )

    fun loadAccounts(context: Context): List<SavedEmailAccount> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null)
        val parsed = mutableListOf<SavedEmailAccount>()
        if (!raw.isNullOrBlank()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val email = obj.optString("email").trim()
                    if (email.isBlank()) continue
                    parsed.add(
                        SavedEmailAccount(
                            id = obj.optString("id").ifBlank { "acc_${email.hashCode()}" },
                            fullName = obj.optString("fullName").ifBlank { "FotoFilm User" },
                            username = obj.optString("username").ifBlank { email.substringBefore("@") },
                            email = email,
                            avatarUrl = obj.optString("avatarUrl").ifBlank {
                                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500"
                            },
                            isFirebaseLinked = obj.optBoolean("isFirebaseLinked", false),
                            isOwner = obj.optBoolean("isOwner", false),
                            lastUsedAt = obj.optLong("lastUsedAt", 0L)
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        val byEmail = linkedMapOf<String, SavedEmailAccount>()
        parsed.forEach { acc ->
            byEmail[acc.email.trim().lowercase()] = acc
        }
        defaultSeedAccounts().forEach { seed ->
            val key = seed.email.trim().lowercase()
            if (!byEmail.containsKey(key)) {
                byEmail[key] = seed
            }
        }
        return byEmail.values.sortedByDescending { it.lastUsedAt }
    }

    fun saveAccount(context: Context, account: SavedEmailAccount): List<SavedEmailAccount> {
        val cleanEmail = account.email.trim()
        if (cleanEmail.isBlank()) return loadAccounts(context)
        val current = loadAccounts(context).toMutableList()
        val key = cleanEmail.lowercase()
        val existingIdx = current.indexOfFirst { it.email.trim().lowercase() == key }
        val updatedEntry = if (existingIdx >= 0) {
            val prev = current[existingIdx]
            account.copy(
                isFirebaseLinked = account.isFirebaseLinked || prev.isFirebaseLinked,
                isOwner = account.isOwner || prev.isOwner,
                lastUsedAt = System.currentTimeMillis()
            ).also { current.removeAt(existingIdx) }
        } else {
            account.copy(lastUsedAt = System.currentTimeMillis())
        }
        current.add(0, updatedEntry)

        val arr = JSONArray()
        current.take(25).forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("id", acc.id)
                    put("fullName", acc.fullName)
                    put("username", acc.username)
                    put("email", acc.email)
                    put("avatarUrl", acc.avatarUrl)
                    put("isFirebaseLinked", acc.isFirebaseLinked)
                    put("isOwner", acc.isOwner)
                    put("lastUsedAt", acc.lastUsedAt)
                }
            )
        }
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCOUNTS_JSON, arr.toString())
            .apply()

        return current
    }
}
