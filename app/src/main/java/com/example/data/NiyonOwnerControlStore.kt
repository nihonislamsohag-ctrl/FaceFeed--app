package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistent Owner-Lock Controller for Right Manager Fida.
 * Any option turned OFF anywhere inside Right Manager Fida by Niyon Islam Sohag
 * is permanently persisted and CANNOT be turned back ON by app restarts, tab switches,
 * cloud/Room refreshes, or any other user until Niyon Islam Sohag explicitly turns it ON.
 */
object NiyonOwnerControlStore {

    private const val PREFS_NAME = "niyon_owner_control_locks"

    private const val KEY_DISABLED_USERS = "disabled_user_ids"
    private const val KEY_DISABLED_LANGUAGES = "disabled_languages"
    private const val KEY_PAUSED_BOOST_TXS = "paused_boost_tx_ids"
    private const val KEY_ACTIVE_BOOST_TXS = "active_boost_tx_ids"
    private const val KEY_CLOUDINARY_ENABLED = "cloudinary_enabled"
    private const val KEY_BLAZE_CARD_ENABLED = "blaze_card_enabled"
    private const val KEY_DISABLED_PAGES = "disabled_page_ids"
    private const val KEY_DISABLED_GROUPS = "disabled_group_ids"
    private const val KEY_DISABLED_HANGOUT_ROOMS = "disabled_hangout_room_keys"
    private const val KEY_PAUSED_TIMED_SUSPENSIONS = "paused_timed_suspension_ids"
    private const val KEY_DISABLED_POST_BOOSTS = "disabled_post_boost_ids"
    private const val KEY_ADMOB_MASTER_ENABLED = "admob_master_enabled"
    private const val KEY_ADMOB_BANNER_ENABLED = "admob_banner_enabled"
    private const val KEY_ADMOB_INTERSTITIAL_ENABLED = "admob_interstitial_enabled"
    private const val KEY_ADMOB_REWARDED_ENABLED = "admob_rewarded_enabled"
    private const val KEY_AUTO_COMPRESSION_ENABLED = "auto_compression_enabled"
    private const val KEY_DAILY_43MB_LIMIT_ENABLED = "daily_43mb_limit_enabled"

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _disabledUserIds = MutableStateFlow<Set<String>>(emptySet())
    val disabledUserIds: StateFlow<Set<String>> = _disabledUserIds.asStateFlow()

    private val _disabledLanguages = MutableStateFlow<Set<String>>(emptySet())
    val disabledLanguages: StateFlow<Set<String>> = _disabledLanguages.asStateFlow()

    private val _pausedBoostTxIds = MutableStateFlow<Set<String>>(emptySet())
    val pausedBoostTxIds: StateFlow<Set<String>> = _pausedBoostTxIds.asStateFlow()

    private val _activeBoostTxIds = MutableStateFlow<Set<String>>(emptySet())
    val activeBoostTxIds: StateFlow<Set<String>> = _activeBoostTxIds.asStateFlow()

    private val _isCloudinaryEnabled = MutableStateFlow(true)
    val isCloudinaryEnabled: StateFlow<Boolean> = _isCloudinaryEnabled.asStateFlow()

    private val _isBlazeCardEnabled = MutableStateFlow(false)
    val isBlazeCardEnabled: StateFlow<Boolean> = _isBlazeCardEnabled.asStateFlow()

    private val _disabledPageIds = MutableStateFlow<Set<String>>(emptySet())
    val disabledPageIds: StateFlow<Set<String>> = _disabledPageIds.asStateFlow()

    private val _disabledGroupIds = MutableStateFlow<Set<String>>(emptySet())
    val disabledGroupIds: StateFlow<Set<String>> = _disabledGroupIds.asStateFlow()

    private val _disabledHangoutRoomKeys = MutableStateFlow<Set<String>>(emptySet())
    val disabledHangoutRoomKeys: StateFlow<Set<String>> = _disabledHangoutRoomKeys.asStateFlow()

    private val _pausedTimedSuspensionIds = MutableStateFlow<Set<String>>(emptySet())
    val pausedTimedSuspensionIds: StateFlow<Set<String>> = _pausedTimedSuspensionIds.asStateFlow()

    private val _disabledPostBoostIds = MutableStateFlow<Set<String>>(emptySet())
    val disabledPostBoostIds: StateFlow<Set<String>> = _disabledPostBoostIds.asStateFlow()

    private val _isAdMobMasterEnabled = MutableStateFlow(true)
    val isAdMobMasterEnabled: StateFlow<Boolean> = _isAdMobMasterEnabled.asStateFlow()

    private val _isAdMobBannerEnabled = MutableStateFlow(true)
    val isAdMobBannerEnabled: StateFlow<Boolean> = _isAdMobBannerEnabled.asStateFlow()

    private val _isAdMobInterstitialEnabled = MutableStateFlow(true)
    val isAdMobInterstitialEnabled: StateFlow<Boolean> = _isAdMobInterstitialEnabled.asStateFlow()

    private val _isAdMobRewardedEnabled = MutableStateFlow(true)
    val isAdMobRewardedEnabled: StateFlow<Boolean> = _isAdMobRewardedEnabled.asStateFlow()

    private val _isAutoCompressionEnabled = MutableStateFlow(true)
    val isAutoCompressionEnabled: StateFlow<Boolean> = _isAutoCompressionEnabled.asStateFlow()

    private val _isDaily43MbLimitEnabled = MutableStateFlow(true)
    val isDaily43MbLimitEnabled: StateFlow<Boolean> = _isDaily43MbLimitEnabled.asStateFlow()

    private val _lockedOffCount = MutableStateFlow(0)
    val lockedOffCount: StateFlow<Int> = _lockedOffCount.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        val appCtx = context.applicationContext
        val p = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p

        _disabledUserIds.value = p.getStringSet(KEY_DISABLED_USERS, emptySet())?.toSet() ?: emptySet()
        _disabledLanguages.value = p.getStringSet(KEY_DISABLED_LANGUAGES, emptySet())?.toSet() ?: emptySet()
        _pausedBoostTxIds.value = p.getStringSet(KEY_PAUSED_BOOST_TXS, emptySet())?.toSet() ?: emptySet()
        _activeBoostTxIds.value = p.getStringSet(KEY_ACTIVE_BOOST_TXS, emptySet())?.toSet() ?: emptySet()
        _isCloudinaryEnabled.value = p.getBoolean(KEY_CLOUDINARY_ENABLED, true)

        val billingFallback = try {
            appCtx.getSharedPreferences("fotofilm_billing_prefs", Context.MODE_PRIVATE)
                .getBoolean("blaze_card_mode_enabled", false)
        } catch (_: Exception) {
            false
        }
        _isBlazeCardEnabled.value = p.getBoolean(KEY_BLAZE_CARD_ENABLED, billingFallback)

        _disabledPageIds.value = p.getStringSet(KEY_DISABLED_PAGES, emptySet())?.toSet() ?: emptySet()
        _disabledGroupIds.value = p.getStringSet(KEY_DISABLED_GROUPS, emptySet())?.toSet() ?: emptySet()
        _disabledHangoutRoomKeys.value = p.getStringSet(KEY_DISABLED_HANGOUT_ROOMS, emptySet())?.toSet() ?: emptySet()
        _pausedTimedSuspensionIds.value = p.getStringSet(KEY_PAUSED_TIMED_SUSPENSIONS, emptySet())?.toSet() ?: emptySet()
        _disabledPostBoostIds.value = p.getStringSet(KEY_DISABLED_POST_BOOSTS, emptySet())?.toSet() ?: emptySet()
        _isAdMobMasterEnabled.value = p.getBoolean(KEY_ADMOB_MASTER_ENABLED, true)
        _isAdMobBannerEnabled.value = p.getBoolean(KEY_ADMOB_BANNER_ENABLED, true)
        _isAdMobInterstitialEnabled.value = p.getBoolean(KEY_ADMOB_INTERSTITIAL_ENABLED, true)
        _isAdMobRewardedEnabled.value = p.getBoolean(KEY_ADMOB_REWARDED_ENABLED, true)

        val quotaPrefs = try {
            appCtx.getSharedPreferences("fotofilm_daily_43mb_quota_prefs", Context.MODE_PRIVATE)
        } catch (_: Exception) {
            null
        }
        val autoCompFallback = quotaPrefs?.getBoolean("auto_compress_enabled", true) ?: true
        val dailyLimitFallback = quotaPrefs?.getBoolean("daily_43mb_limit_enabled", true) ?: true
        _isAutoCompressionEnabled.value = p.getBoolean(KEY_AUTO_COMPRESSION_ENABLED, autoCompFallback)
        _isDaily43MbLimitEnabled.value = p.getBoolean(KEY_DAILY_43MB_LIMIT_ENABLED, dailyLimitFallback)

        recalculateLockedCount()
    }

    private fun recalculateLockedCount() {
        var count = 0
        count += _disabledUserIds.value.size
        count += _disabledLanguages.value.size
        count += _pausedBoostTxIds.value.size
        count += _disabledPageIds.value.size
        count += _disabledGroupIds.value.size
        count += _disabledHangoutRoomKeys.value.size
        count += _pausedTimedSuspensionIds.value.size
        count += _disabledPostBoostIds.value.size
        if (!_isCloudinaryEnabled.value) count++
        if (!_isBlazeCardEnabled.value && prefs?.contains(KEY_BLAZE_CARD_ENABLED) == true) count++
        if (!_isAdMobMasterEnabled.value) count++
        if (!_isAdMobBannerEnabled.value) count++
        if (!_isAdMobInterstitialEnabled.value) count++
        if (!_isAdMobRewardedEnabled.value) count++
        if (!_isAutoCompressionEnabled.value) count++
        if (!_isDaily43MbLimitEnabled.value) count++
        _lockedOffCount.value = count
    }

    private fun saveStringSet(key: String, value: Set<String>) {
        prefs?.edit()?.putStringSet(key, value.toHashSet())?.apply()
        recalculateLockedCount()
    }

    private fun saveBoolean(key: String, value: Boolean) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
        recalculateLockedCount()
    }

    fun isUserLockedOff(userId: String): Boolean = userId in _disabledUserIds.value
    fun isLanguageLockedOff(langName: String): Boolean = langName in _disabledLanguages.value
    fun isPageLockedOff(pageId: String): Boolean = pageId in _disabledPageIds.value
    fun isGroupLockedOff(groupId: String): Boolean = groupId in _disabledGroupIds.value
    fun isHangoutRoomLockedOff(roomKey: String): Boolean = roomKey in _disabledHangoutRoomKeys.value
    fun isTimedSuspensionPaused(recordId: String): Boolean = recordId in _pausedTimedSuspensionIds.value
    fun isPostBoostLockedOff(postId: String): Boolean = postId in _disabledPostBoostIds.value
    fun isBoostTxPaused(txId: String): Boolean = txId in _pausedBoostTxIds.value

    fun isBannerAdEffectiveEnabled(): Boolean =
        _isAdMobMasterEnabled.value && _isAdMobBannerEnabled.value

    fun isInterstitialAdEffectiveEnabled(): Boolean =
        _isAdMobMasterEnabled.value && _isAdMobInterstitialEnabled.value

    fun isRewardedAdEffectiveEnabled(): Boolean =
        _isAdMobMasterEnabled.value && _isAdMobRewardedEnabled.value

    fun setUserActive(userId: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _disabledUserIds.value - userId else _disabledUserIds.value + userId
        _disabledUserIds.value = next
        saveStringSet(KEY_DISABLED_USERS, next)
        return true
    }

    fun setLanguageEnabled(langName: String, enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (enabled) _disabledLanguages.value - langName else _disabledLanguages.value + langName
        _disabledLanguages.value = next
        saveStringSet(KEY_DISABLED_LANGUAGES, next)
        return true
    }

    fun setBoostTxStatus(txId: String, status: String, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val isOff = !status.equals("ACTIVE", ignoreCase = true)
        val nextPaused = if (isOff) _pausedBoostTxIds.value + txId else _pausedBoostTxIds.value - txId
        val nextActive = if (!isOff) _activeBoostTxIds.value + txId else _activeBoostTxIds.value - txId
        _pausedBoostTxIds.value = nextPaused
        _activeBoostTxIds.value = nextActive
        prefs?.edit()
            ?.putStringSet(KEY_PAUSED_BOOST_TXS, nextPaused.toHashSet())
            ?.putStringSet(KEY_ACTIVE_BOOST_TXS, nextActive.toHashSet())
            ?.apply()
        recalculateLockedCount()
        return true
    }

    fun setCloudinaryEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isCloudinaryEnabled.value = enabled
        saveBoolean(KEY_CLOUDINARY_ENABLED, enabled)
        return true
    }

    fun setBlazeCardEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isBlazeCardEnabled.value = enabled
        saveBoolean(KEY_BLAZE_CARD_ENABLED, enabled)
        return true
    }

    fun setPageActive(pageId: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _disabledPageIds.value - pageId else _disabledPageIds.value + pageId
        _disabledPageIds.value = next
        saveStringSet(KEY_DISABLED_PAGES, next)
        return true
    }

    fun setGroupActive(groupId: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _disabledGroupIds.value - groupId else _disabledGroupIds.value + groupId
        _disabledGroupIds.value = next
        saveStringSet(KEY_DISABLED_GROUPS, next)
        return true
    }

    fun setHangoutRoomActive(roomKey: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _disabledHangoutRoomKeys.value - roomKey else _disabledHangoutRoomKeys.value + roomKey
        _disabledHangoutRoomKeys.value = next
        saveStringSet(KEY_DISABLED_HANGOUT_ROOMS, next)
        return true
    }

    fun setTimedSuspensionActive(recordId: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _pausedTimedSuspensionIds.value - recordId else _pausedTimedSuspensionIds.value + recordId
        _pausedTimedSuspensionIds.value = next
        saveStringSet(KEY_PAUSED_TIMED_SUSPENSIONS, next)
        return true
    }

    fun setPostBoostActive(postId: String, active: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        val next = if (active) _disabledPostBoostIds.value - postId else _disabledPostBoostIds.value + postId
        _disabledPostBoostIds.value = next
        saveStringSet(KEY_DISABLED_POST_BOOSTS, next)
        return true
    }

    fun setAdMobMasterEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isAdMobMasterEnabled.value = enabled
        saveBoolean(KEY_ADMOB_MASTER_ENABLED, enabled)
        return true
    }

    fun setAdMobBannerEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isAdMobBannerEnabled.value = enabled
        saveBoolean(KEY_ADMOB_BANNER_ENABLED, enabled)
        return true
    }

    fun setAdMobInterstitialEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isAdMobInterstitialEnabled.value = enabled
        saveBoolean(KEY_ADMOB_INTERSTITIAL_ENABLED, enabled)
        return true
    }

    fun setAdMobRewardedEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isAdMobRewardedEnabled.value = enabled
        saveBoolean(KEY_ADMOB_REWARDED_ENABLED, enabled)
        return true
    }

    fun setAutoCompressionEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isAutoCompressionEnabled.value = enabled
        saveBoolean(KEY_AUTO_COMPRESSION_ENABLED, enabled)
        return true
    }

    fun setDaily43MbLimitEnabled(enabled: Boolean, isOwner: Boolean): Boolean {
        if (!isOwner) return false
        _isDaily43MbLimitEnabled.value = enabled
        saveBoolean(KEY_DAILY_43MB_LIMIT_ENABLED, enabled)
        return true
    }
}
