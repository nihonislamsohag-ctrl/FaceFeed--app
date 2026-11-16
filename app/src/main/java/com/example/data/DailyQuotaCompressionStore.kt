package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class DailyQuotaCategory {
    LONG_VIDEO,   // ১ টি (৩ মিনিট) = ৩০ MB
    REEL_VIDEO,   // ১ টি (৩০ সেকেন্ড) = ৮ MB
    FEED_PHOTO,   // ৪ টি (৪ x ৩০০ KB) = ১.২ MB
    STORY_PHOTO,  // ৩ টি (৩ x ৩০০ KB) = ০.৯ MB
    STORY_VIDEO   // ১ টি (১৫ সেকেন্ড) = ৩.০ MB
}

data class UserDailyQuotaUsage(
    val userId: String,
    val userName: String,
    val avatarUrl: String,
    val dateKey: String,
    val longVideoCount: Int = 0,
    val longVideoMb: Double = 0.0,
    val reelCount: Int = 0,
    val reelMb: Double = 0.0,
    val photoCount: Int = 0,
    val photoKb: Double = 0.0,
    val storyPhotoCount: Int = 0,
    val storyPhotoKb: Double = 0.0,
    val storyVideoCount: Int = 0,
    val storyVideoMb: Double = 0.0,
    val savedByCompressionMb: Double = 0.0,
    val chatFilmFreeMediaCount: Int = 0
) {
    val photoMb: Double get() = photoKb / 1000.0
    val storyPhotoMb: Double get() = storyPhotoKb / 1000.0
    val storyTotalMb: Double get() = storyPhotoMb + storyVideoMb
    val totalUsedMb: Double get() = longVideoMb + reelMb + photoMb + storyTotalMb
    val remainingMb: Double get() = (DailyQuotaCompressionStore.DAILY_TOTAL_LIMIT_MB - totalUsedMb).coerceAtLeast(0.0)
    val usagePercent: Float get() = (totalUsedMb / DailyQuotaCompressionStore.DAILY_TOTAL_LIMIT_MB).toFloat().coerceIn(0f, 1f)
}

data class QuotaCompressionConfig(
    val isAutoCompressionEnabled: Boolean = true,
    val isDaily43MbLimitEnabled: Boolean = true,
    val isAllTimeFreeHubActive: Boolean = true,
    val totalCompressionSavedMbToday: Double = 0.0,
    val totalDailySpentMbAllUsers: Double = 0.0
)

data class QuotaCheckVerdict(
    val allowed: Boolean,
    val reasonBn: String = ""
)

object DailyQuotaCompressionStore {
    const val DAILY_TOTAL_LIMIT_MB = 43.1
    const val MAX_LONG_VIDEO_COUNT = 1
    const val MAX_LONG_VIDEO_MB = 30.0
    const val MAX_REEL_COUNT = 1
    const val MAX_REEL_MB = 8.0
    const val MAX_FEED_PHOTO_COUNT = 4
    const val MAX_PHOTO_UNIT_KB = 300.0
    const val MAX_FEED_PHOTO_TOTAL_MB = 1.2
    const val MAX_STORY_PHOTO_COUNT = 3
    const val MAX_STORY_PHOTO_TOTAL_MB = 0.9
    const val MAX_STORY_VIDEO_COUNT = 1
    const val MAX_STORY_VIDEO_MB = 3.0
    const val MAX_STORY_TOTAL_MB = 3.9

    private const val PREFS_NAME = "fotofilm_daily_43mb_quota_prefs"
    private const val KEY_DATE = "quota_date_key"
    private const val KEY_AUTO_COMPRESS = "auto_compress_enabled"
    private const val KEY_DAILY_LIMIT = "daily_43mb_limit_enabled"
    private const val KEY_USAGES_JSON = "user_daily_usages_json"

    private var appContext: Context? = null

    private val _config = MutableStateFlow(QuotaCompressionConfig())
    val config: StateFlow<QuotaCompressionConfig> = _config.asStateFlow()

    private val _userUsages = MutableStateFlow<List<UserDailyQuotaUsage>>(emptyList())
    val userUsages: StateFlow<List<UserDailyQuotaUsage>> = _userUsages.asStateFlow()

    private val _currentUserUsage = MutableStateFlow(
        UserDailyQuotaUsage(
            userId = "user_niyon",
            userName = "Niyon Islam Sohag",
            avatarUrl = "",
            dateKey = todayKey()
        )
    )
    val currentUserUsage: StateFlow<UserDailyQuotaUsage> = _currentUserUsage.asStateFlow()

    fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun initialize(context: Context, currentUserId: String, currentUserName: String, currentAvatar: String, allUsers: List<com.example.model.User> = emptyList()) {
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey()
        val storedDate = prefs.getString(KEY_DATE, null)

        val autoCompress = prefs.getBoolean(KEY_AUTO_COMPRESS, true)
        val dailyLimit = prefs.getBoolean(KEY_DAILY_LIMIT, true)

        val loadedList = if (storedDate == today) {
            parseUsagesJson(prefs.getString(KEY_USAGES_JSON, null), today)
        } else {
            prefs.edit().putString(KEY_DATE, today).remove(KEY_USAGES_JSON).apply()
            emptyList()
        }.toMutableList()

        // Ensure current user always has an entry
        if (loadedList.none { it.userId == currentUserId }) {
            loadedList.add(
                0,
                UserDailyQuotaUsage(
                    userId = currentUserId,
                    userName = currentUserName,
                    avatarUrl = currentAvatar,
                    dateKey = today
                )
            )
        }

        // Populate active community usage telemetry cleanly if newly reset today so Admin tracker shows real community accounts
        allUsers.take(8).forEach { u ->
            if (loadedList.none { it.userId == u.id }) {
                loadedList.add(
                    UserDailyQuotaUsage(
                        userId = u.id,
                        userName = u.fullName,
                        avatarUrl = u.avatarUrl,
                        dateKey = today
                    )
                )
            }
        }

        publishList(loadedList, currentUserId, autoCompress, dailyLimit)
    }

    fun syncCurrentUserIdentity(userId: String, userName: String, avatarUrl: String) {
        val today = todayKey()
        val list = _userUsages.value.toMutableList()
        val existingIdx = list.indexOfFirst { it.userId == userId }
        if (existingIdx >= 0) {
            list[existingIdx] = list[existingIdx].copy(userName = userName, avatarUrl = avatarUrl, dateKey = today)
        } else {
            list.add(0, UserDailyQuotaUsage(userId = userId, userName = userName, avatarUrl = avatarUrl, dateKey = today))
        }
        publishList(list, userId, _config.value.isAutoCompressionEnabled, _config.value.isDaily43MbLimitEnabled)
    }

    fun setAutoCompressionEnabled(enabled: Boolean) {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()?.putBoolean(KEY_AUTO_COMPRESS, enabled)?.apply()
        _config.value = _config.value.copy(isAutoCompressionEnabled = enabled)
    }

    fun setDaily43MbLimitEnabled(enabled: Boolean) {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()?.putBoolean(KEY_DAILY_LIMIT, enabled)?.apply()
        _config.value = _config.value.copy(isDaily43MbLimitEnabled = enabled)
    }

    fun checkQuotaBeforeUpload(
        userId: String,
        category: DailyQuotaCategory,
        itemCount: Int = 1
    ): QuotaCheckVerdict {
        if (!_config.value.isDaily43MbLimitEnabled) {
            return QuotaCheckVerdict(allowed = true)
        }
        val usage = _userUsages.value.find { it.userId == userId } ?: return QuotaCheckVerdict(allowed = true)
        return when (category) {
            DailyQuotaCategory.LONG_VIDEO -> {
                if (usage.longVideoCount >= MAX_LONG_VIDEO_COUNT) {
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজকের ১টি ফ্রি লং ভিডিও (৩০ MB / ৩ মিনিট) কোটা পূর্ণ হয়েছে। আগামীকাল আবার ফ্রি পাবেন! (ChatFilm ও ফিদা এআই অল-টাইম ফ্রি ✓)"
                    )
                } else QuotaCheckVerdict(allowed = true)
            }
            DailyQuotaCategory.REEL_VIDEO -> {
                if (usage.reelCount >= MAX_REEL_COUNT) {
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজকের ১টি ফ্রি রিল ভিডিও (৮ MB / ৩০ সেকেন্ড) কোটা পূর্ণ হয়েছে। আগামীকাল আবার ফ্রি পাবেন!"
                    )
                } else QuotaCheckVerdict(allowed = true)
            }
            DailyQuotaCategory.FEED_PHOTO -> {
                val nextCount = usage.photoCount + itemCount
                if (usage.photoCount >= MAX_FEED_PHOTO_COUNT) {
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজকের ৪টি ফ্রি ছবির কোটা (৪ × ৩০০ KB = ১.২ MB) পূর্ণ হয়েছে। আগামীকাল আবার ফ্রি পাবেন!"
                    )
                } else if (nextCount > MAX_FEED_PHOTO_COUNT) {
                    val left = MAX_FEED_PHOTO_COUNT - usage.photoCount
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজ আর মাত্র $left টি ছবি পোস্ট করা যাবে (দৈনিক সীমা ৪টি = ১.২ MB)।"
                    )
                } else QuotaCheckVerdict(allowed = true)
            }
            DailyQuotaCategory.STORY_PHOTO -> {
                if (usage.storyPhotoCount >= MAX_STORY_PHOTO_COUNT) {
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজকের ৩টি ফ্রি ছবি স্টোরি (০.৯ MB) পূর্ণ হয়েছে। আগামীকাল আবার ফ্রি পাবেন!"
                    )
                } else QuotaCheckVerdict(allowed = true)
            }
            DailyQuotaCategory.STORY_VIDEO -> {
                if (usage.storyVideoCount >= MAX_STORY_VIDEO_COUNT) {
                    QuotaCheckVerdict(
                        allowed = false,
                        reasonBn = "আজকের ১টি ফ্রি ভিডিও স্টোরি (৩ MB / ১৫ সেকেন্ড) পূর্ণ হয়েছে। আগামীকাল আবার ফ্রি পাবেন!"
                    )
                } else QuotaCheckVerdict(allowed = true)
            }
        }
    }

    fun recordUsage(
        userId: String,
        userName: String,
        avatarUrl: String,
        category: DailyQuotaCategory,
        itemCount: Int = 1,
        usedMbOrKb: Double,
        savedMb: Double = 0.0
    ) {
        val today = todayKey()
        val list = _userUsages.value.toMutableList()
        val idx = list.indexOfFirst { it.userId == userId }
        val base = if (idx >= 0) list[idx] else UserDailyQuotaUsage(
            userId = userId,
            userName = userName,
            avatarUrl = avatarUrl,
            dateKey = today
        )

        val updated = when (category) {
            DailyQuotaCategory.LONG_VIDEO -> base.copy(
                longVideoCount = base.longVideoCount + itemCount,
                longVideoMb = (base.longVideoMb + usedMbOrKb).coerceAtMost(MAX_LONG_VIDEO_MB),
                savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
            )
            DailyQuotaCategory.REEL_VIDEO -> base.copy(
                reelCount = base.reelCount + itemCount,
                reelMb = (base.reelMb + usedMbOrKb).coerceAtMost(MAX_REEL_MB),
                savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
            )
            DailyQuotaCategory.FEED_PHOTO -> base.copy(
                photoCount = (base.photoCount + itemCount).coerceAtMost(MAX_FEED_PHOTO_COUNT),
                photoKb = (base.photoKb + usedMbOrKb).coerceAtMost(MAX_FEED_PHOTO_COUNT * MAX_PHOTO_UNIT_KB),
                savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
            )
            DailyQuotaCategory.STORY_PHOTO -> base.copy(
                storyPhotoCount = (base.storyPhotoCount + itemCount).coerceAtMost(MAX_STORY_PHOTO_COUNT),
                storyPhotoKb = (base.storyPhotoKb + usedMbOrKb).coerceAtMost(MAX_STORY_PHOTO_COUNT * MAX_PHOTO_UNIT_KB),
                savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
            )
            DailyQuotaCategory.STORY_VIDEO -> base.copy(
                storyVideoCount = base.storyVideoCount + itemCount,
                storyVideoMb = (base.storyVideoMb + usedMbOrKb).coerceAtMost(MAX_STORY_VIDEO_MB),
                savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
            )
        }

        if (idx >= 0) list[idx] = updated else list.add(0, updated)
        publishList(list, userId, _config.value.isAutoCompressionEnabled, _config.value.isDaily43MbLimitEnabled)
    }

    fun recordChatFilmFreeMedia(userId: String, userName: String, avatarUrl: String, savedMb: Double = 0.0) {
        val today = todayKey()
        val list = _userUsages.value.toMutableList()
        val idx = list.indexOfFirst { it.userId == userId }
        val base = if (idx >= 0) list[idx] else UserDailyQuotaUsage(
            userId = userId,
            userName = userName,
            avatarUrl = avatarUrl,
            dateKey = today
        )
        val updated = base.copy(
            chatFilmFreeMediaCount = base.chatFilmFreeMediaCount + 1,
            savedByCompressionMb = base.savedByCompressionMb + savedMb.coerceAtLeast(0.0)
        )
        if (idx >= 0) list[idx] = updated else list.add(0, updated)
        publishList(list, userId, _config.value.isAutoCompressionEnabled, _config.value.isDaily43MbLimitEnabled)
    }

    fun resetUserDailyUsage(userId: String, currentActiveUserId: String) {
        val today = todayKey()
        val list = _userUsages.value.map {
            if (it.userId == userId) {
                UserDailyQuotaUsage(
                    userId = it.userId,
                    userName = it.userName,
                    avatarUrl = it.avatarUrl,
                    dateKey = today
                )
            } else it
        }
        publishList(list, currentActiveUserId, _config.value.isAutoCompressionEnabled, _config.value.isDaily43MbLimitEnabled)
    }

    fun resetAllDailyUsage(currentActiveUserId: String) {
        val today = todayKey()
        val list = _userUsages.value.map {
            UserDailyQuotaUsage(
                userId = it.userId,
                userName = it.userName,
                avatarUrl = it.avatarUrl,
                dateKey = today
            )
        }
        publishList(list, currentActiveUserId, _config.value.isAutoCompressionEnabled, _config.value.isDaily43MbLimitEnabled)
    }

    private fun publishList(
        list: List<UserDailyQuotaUsage>,
        currentUserId: String,
        autoCompress: Boolean,
        dailyLimit: Boolean
    ) {
        val sorted = list.sortedByDescending { it.totalUsedMb }
        _userUsages.value = sorted
        sorted.find { it.userId == currentUserId }?.let { _currentUserUsage.value = it }

        val totalSpent = sorted.sumOf { it.totalUsedMb }
        val totalSaved = sorted.sumOf { it.savedByCompressionMb }
        _config.value = QuotaCompressionConfig(
            isAutoCompressionEnabled = autoCompress,
            isDaily43MbLimitEnabled = dailyLimit,
            isAllTimeFreeHubActive = true,
            totalCompressionSavedMbToday = totalSaved,
            totalDailySpentMbAllUsers = totalSpent
        )
        persistList(sorted)
    }

    private fun persistList(list: List<UserDailyQuotaUsage>) {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        try {
            val arr = JSONArray()
            list.forEach { u ->
                val obj = JSONObject()
                obj.put("userId", u.userId)
                obj.put("userName", u.userName)
                obj.put("avatarUrl", u.avatarUrl)
                obj.put("dateKey", u.dateKey)
                obj.put("longVideoCount", u.longVideoCount)
                obj.put("longVideoMb", u.longVideoMb)
                obj.put("reelCount", u.reelCount)
                obj.put("reelMb", u.reelMb)
                obj.put("photoCount", u.photoCount)
                obj.put("photoKb", u.photoKb)
                obj.put("storyPhotoCount", u.storyPhotoCount)
                obj.put("storyPhotoKb", u.storyPhotoKb)
                obj.put("storyVideoCount", u.storyVideoCount)
                obj.put("storyVideoMb", u.storyVideoMb)
                obj.put("savedByCompressionMb", u.savedByCompressionMb)
                obj.put("chatFilmFreeMediaCount", u.chatFilmFreeMediaCount)
                arr.put(obj)
            }
            prefs.edit()
                .putString(KEY_DATE, todayKey())
                .putString(KEY_USAGES_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun parseUsagesJson(raw: String?, today: String): List<UserDailyQuotaUsage> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = mutableListOf<UserDailyQuotaUsage>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(
                    UserDailyQuotaUsage(
                        userId = o.optString("userId"),
                        userName = o.optString("userName"),
                        avatarUrl = o.optString("avatarUrl"),
                        dateKey = o.optString("dateKey", today),
                        longVideoCount = o.optInt("longVideoCount", 0),
                        longVideoMb = o.optDouble("longVideoMb", 0.0),
                        reelCount = o.optInt("reelCount", 0),
                        reelMb = o.optDouble("reelMb", 0.0),
                        photoCount = o.optInt("photoCount", 0),
                        photoKb = o.optDouble("photoKb", 0.0),
                        storyPhotoCount = o.optInt("storyPhotoCount", 0),
                        storyPhotoKb = o.optDouble("storyPhotoKb", 0.0),
                        storyVideoCount = o.optInt("storyVideoCount", 0),
                        storyVideoMb = o.optDouble("storyVideoMb", 0.0),
                        savedByCompressionMb = o.optDouble("savedByCompressionMb", 0.0),
                        chatFilmFreeMediaCount = o.optInt("chatFilmFreeMediaCount", 0)
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun formatMb(mb: Double): String {
        val rounded = (mb * 10.0).roundToInt() / 10.0
        return if (rounded == rounded.toLong().toDouble()) "${rounded.toLong()} MB" else String.format(Locale.US, "%.1f MB", mb)
    }
}
