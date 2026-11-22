package com.example.data

import android.content.Context
import com.example.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

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

data class SavedAuthSession(
    val user: User,
    val isOwnerVerified: Boolean,
    val authMethod: String
)

object SavedAccountsStore {
    private const val PREFS_NAME = "fotofilm_saved_accounts"
    private const val KEY_ACCOUNTS_JSON = "accounts_json"

    fun loadAccounts(context: Context): List<SavedEmailAccount> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null)
        val parsed = mutableListOf<SavedEmailAccount>()
        if (!raw.isNullOrBlank()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id == "seed_niyon_1" || id == "seed_niyon_2") continue
                    val email = obj.optString("email").trim()
                    if (email.isBlank()) continue
                    parsed.add(
                        SavedEmailAccount(
                            id = id.ifBlank { "acc_${email.hashCode()}" },
                            fullName = obj.optString("fullName").ifBlank { "FotoFilm User" },
                            username = obj.optString("username").ifBlank { email.substringBefore("@") },
                            email = email,
                            avatarUrl = obj.optString("avatarUrl"),
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
                isOwner = account.isOwner && (prev.isOwner || account.isOwner),
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

object AuthSessionStore {
    private const val SESSION_PREFS = "fotofilm_auth_session"
    private const val CREDENTIAL_PREFS = "fotofilm_local_credentials"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SESSION_JSON = "session_json"
    private const val KEY_CREDENTIALS_JSON = "credentials_json"

    private const val OWNER_LOGIN_HASH = "9dbb2db01b270f3445c3bc88447c07f701efb7c882def21af27db9dbccf6e2e9"
    private const val OWNER_LOGIN_HASH_LOWER = "fa7be871ace7c7a27edd73af30bf30ea6dc9bd2c1ac999df268c43fe0aaa68b6"

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isReservedOwnerEmail(email: String): Boolean {
        val clean = email.trim().lowercase()
        return clean == "smd224292@gmail.com" ||
            clean == "nihonislamsohag@gmail.com" ||
            clean == "niyonislamsohag@gmail.com"
    }

    fun verifyOwnerSecret(password: String, rightManagerPin: String): Boolean {
        val clean = password.trim()
        if (clean.isBlank()) return false
        if (clean == rightManagerPin) return true
        return sha256(clean) == OWNER_LOGIN_HASH ||
            sha256(clean.lowercase()) == OWNER_LOGIN_HASH_LOWER
    }

    fun saveActiveSession(
        context: Context,
        user: User,
        isOwnerVerified: Boolean,
        authMethod: String
    ) {
        val verifiedOwner = isOwnerVerified && isReservedOwnerEmail(user.email)
        val obj = JSONObject().apply {
            put("id", user.id)
            put("fullName", user.fullName)
            put("username", user.username)
            put("email", user.email)
            put("avatarUrl", user.avatarUrl)
            put("coverUrl", user.coverUrl)
            put("bio", user.bio)
            put("gender", user.gender)
            put("country", user.country)
            put("birthDate", user.birthDate)
            put("isVerified", user.isVerified)
            put("isOwnerVerified", verifiedOwner)
            put("authMethod", authMethod)
        }
        context.applicationContext
            .getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_SESSION_JSON, obj.toString())
            .apply()
    }

    fun loadActiveSession(context: Context): SavedAuthSession? {
        val prefs = context.applicationContext.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return null
        val raw = prefs.getString(KEY_SESSION_JSON, null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val id = obj.optString("id").trim()
            val fullName = obj.optString("fullName").trim()
            val email = obj.optString("email").trim()
            if (id.isBlank() || (fullName.isBlank() && email.isBlank())) return null
            val restoredUser = User(
                id = id,
                fullName = fullName.ifBlank { email.substringBefore("@").ifBlank { "FotoFilm User" } },
                username = obj.optString("username").ifBlank { email.substringBefore("@").ifBlank { "user" } },
                email = email,
                avatarUrl = obj.optString("avatarUrl"),
                coverUrl = obj.optString("coverUrl"),
                bio = obj.optString("bio").ifBlank { "FotoFilm সদস্য ✨" },
                gender = obj.optString("gender").ifBlank { "পুরুষ" },
                country = obj.optString("country").ifBlank { "বাংলাদেশ 🇧🇩" },
                birthDate = obj.optString("birthDate"),
                isVerified = obj.optBoolean("isVerified", false)
            )
            val ownerVerified = obj.optBoolean("isOwnerVerified", false) && isReservedOwnerEmail(email)
            SavedAuthSession(
                user = restoredUser,
                isOwnerVerified = ownerVerified,
                authMethod = obj.optString("authMethod", "email")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearActiveSession(context: Context) {
        context.applicationContext
            .getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun saveLocalCredential(
        context: Context,
        email: String,
        username: String,
        password: String
    ) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) return
        val prefs = context.applicationContext.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
        val root = try {
            JSONObject(prefs.getString(KEY_CREDENTIALS_JSON, null) ?: "{}")
        } catch (_: Exception) {
            JSONObject()
        }
        val entry = JSONObject().apply {
            put("email", cleanEmail)
            put("username", username.trim().lowercase())
            put("passwordHash", sha256(password))
        }
        root.put(cleanEmail, entry)
        prefs.edit().putString(KEY_CREDENTIALS_JSON, root.toString()).apply()
    }

    fun hasLocalCredential(context: Context, emailOrUser: String): Boolean {
        return findCredentialEntry(context, emailOrUser) != null
    }

    fun verifyLocalCredential(context: Context, emailOrUser: String, password: String): Boolean {
        val entry = findCredentialEntry(context, emailOrUser) ?: return false
        val expectedHash = entry.optString("passwordHash")
        return expectedHash.isNotBlank() && expectedHash == sha256(password)
    }

    private fun findCredentialEntry(context: Context, emailOrUser: String): JSONObject? {
        val key = emailOrUser.trim().lowercase()
        if (key.isBlank()) return null
        val prefs = context.applicationContext.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CREDENTIALS_JSON, null) ?: return null
        return try {
            val root = JSONObject(raw)
            if (root.has(key)) {
                root.optJSONObject(key)
            } else {
                val keys = root.keys()
                var matched: JSONObject? = null
                while (keys.hasNext()) {
                    val k = keys.next()
                    val obj = root.optJSONObject(k) ?: continue
                    if (obj.optString("username").equals(key, ignoreCase = true)) {
                        matched = obj
                        break
                    }
                }
                matched
            }
        } catch (_: Exception) {
            null
        }
    }
}

