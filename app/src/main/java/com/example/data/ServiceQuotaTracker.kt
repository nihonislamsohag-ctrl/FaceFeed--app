package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServiceQuotaSnapshot(
    val dayKey: String = "",
    val monthKey: String = "",
    // Firebase Spark Free Daily Limits
    val firebaseReadLimit: Long = 50_000L,
    val firebaseWriteLimit: Long = 20_000L,
    val firebaseReadsUsed: Long = 0L,
    val firebaseWritesUsed: Long = 0L,
    // Cloudinary Free Monthly Limit (25 GB / 25 Credits)
    val cloudinaryMonthlyLimitBytes: Long = 25L * 1024L * 1024L * 1024L,
    val cloudinaryBytesUsed: Long = 0L,
    val cloudinaryFilesUploadedMonth: Int = 0,
    // Agora Free Monthly Limit (10,000 Minutes = 600,000 Seconds)
    val agoraMonthlyLimitSeconds: Long = 10_000L * 60L,
    val agoraSecondsUsed: Long = 0L,
    val agoraCallsCountMonth: Int = 0
) {
    val firebaseReadsRemaining: Long
        get() = (firebaseReadLimit - firebaseReadsUsed).coerceAtLeast(0L)

    val firebaseWritesRemaining: Long
        get() = (firebaseWriteLimit - firebaseWritesUsed).coerceAtLeast(0L)

    val cloudinaryBytesRemaining: Long
        get() = (cloudinaryMonthlyLimitBytes - cloudinaryBytesUsed).coerceAtLeast(0L)

    val cloudinaryUsedPercent: Float
        get() = ((cloudinaryBytesUsed.toDouble() / cloudinaryMonthlyLimitBytes.toDouble()) * 100.0)
            .toFloat()
            .coerceIn(0f, 100f)

    val agoraSecondsRemaining: Long
        get() = (agoraMonthlyLimitSeconds - agoraSecondsUsed).coerceAtLeast(0L)

    val agoraUsedPercent: Float
        get() = ((agoraSecondsUsed.toDouble() / agoraMonthlyLimitSeconds.toDouble()) * 100.0)
            .toFloat()
            .coerceIn(0f, 100f)
}

object ServiceQuotaTracker {
    private const val PREFS_NAME = "fotofilm_service_quota_tracker"
    private const val KEY_DAY = "quota_day_key"
    private const val KEY_MONTH = "quota_month_key"
    private const val KEY_FB_READS = "fb_reads_used"
    private const val KEY_FB_WRITES = "fb_writes_used"
    private const val KEY_CLOUD_BYTES = "cloud_bytes_used"
    private const val KEY_CLOUD_FILES = "cloud_files_count"
    private const val KEY_AGORA_SECONDS = "agora_seconds_used"
    private const val KEY_AGORA_CALLS = "agora_calls_count"

    private var prefs: SharedPreferences? = null

    private val _snapshot = MutableStateFlow(
        ServiceQuotaSnapshot(
            dayKey = currentDayKey(),
            monthKey = currentMonthKey()
        )
    )
    val snapshot: StateFlow<ServiceQuotaSnapshot> = _snapshot.asStateFlow()

    private fun currentDayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun currentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    @Synchronized
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        loadAndRollover()
    }

    @Synchronized
    private fun loadAndRollover() {
        val p = prefs ?: return
        val today = currentDayKey()
        val thisMonth = currentMonthKey()

        val savedDay = p.getString(KEY_DAY, today) ?: today
        val savedMonth = p.getString(KEY_MONTH, thisMonth) ?: thisMonth

        val reads = if (savedDay == today) p.getLong(KEY_FB_READS, 0L).coerceAtLeast(0L) else 0L
        val writes = if (savedDay == today) p.getLong(KEY_FB_WRITES, 0L).coerceAtLeast(0L) else 0L

        val cloudBytes = if (savedMonth == thisMonth) p.getLong(KEY_CLOUD_BYTES, 0L).coerceAtLeast(0L) else 0L
        val cloudFiles = if (savedMonth == thisMonth) p.getInt(KEY_CLOUD_FILES, 0).coerceAtLeast(0) else 0
        val agoraSec = if (savedMonth == thisMonth) p.getLong(KEY_AGORA_SECONDS, 0L).coerceAtLeast(0L) else 0L
        val agoraCalls = if (savedMonth == thisMonth) p.getInt(KEY_AGORA_CALLS, 0).coerceAtLeast(0) else 0

        if (savedDay != today || savedMonth != thisMonth) {
            p.edit()
                .putString(KEY_DAY, today)
                .putString(KEY_MONTH, thisMonth)
                .putLong(KEY_FB_READS, reads)
                .putLong(KEY_FB_WRITES, writes)
                .putLong(KEY_CLOUD_BYTES, cloudBytes)
                .putInt(KEY_CLOUD_FILES, cloudFiles)
                .putLong(KEY_AGORA_SECONDS, agoraSec)
                .putInt(KEY_AGORA_CALLS, agoraCalls)
                .apply()
        }

        _snapshot.value = ServiceQuotaSnapshot(
            dayKey = today,
            monthKey = thisMonth,
            firebaseReadsUsed = reads,
            firebaseWritesUsed = writes,
            cloudinaryBytesUsed = cloudBytes,
            cloudinaryFilesUploadedMonth = cloudFiles,
            agoraSecondsUsed = agoraSec,
            agoraCallsCountMonth = agoraCalls
        )
    }

    @Synchronized
    fun recordFirebaseReads(count: Int) {
        if (count <= 0) return
        ensureCurrentPeriod()
        val next = _snapshot.value.firebaseReadsUsed + count.toLong()
        _snapshot.value = _snapshot.value.copy(firebaseReadsUsed = next)
        prefs?.edit()?.putLong(KEY_FB_READS, next)?.apply()
    }

    @Synchronized
    fun recordFirebaseWrites(count: Int = 1) {
        if (count <= 0) return
        ensureCurrentPeriod()
        val next = _snapshot.value.firebaseWritesUsed + count.toLong()
        _snapshot.value = _snapshot.value.copy(firebaseWritesUsed = next)
        prefs?.edit()?.putLong(KEY_FB_WRITES, next)?.apply()
    }

    @Synchronized
    fun recordCloudinaryUpload(bytes: Long) {
        ensureCurrentPeriod()
        val safeBytes = bytes.coerceAtLeast(1024L)
        val nextBytes = _snapshot.value.cloudinaryBytesUsed + safeBytes
        val nextFiles = _snapshot.value.cloudinaryFilesUploadedMonth + 1
        _snapshot.value = _snapshot.value.copy(
            cloudinaryBytesUsed = nextBytes,
            cloudinaryFilesUploadedMonth = nextFiles
        )
        prefs?.edit()
            ?.putLong(KEY_CLOUD_BYTES, nextBytes)
            ?.putInt(KEY_CLOUD_FILES, nextFiles)
            ?.apply()
    }

    @Synchronized
    fun recordAgoraCallSeconds(seconds: Long, countCall: Boolean = true) {
        if (seconds <= 0L) return
        ensureCurrentPeriod()
        val nextSec = _snapshot.value.agoraSecondsUsed + seconds
        val nextCalls = if (countCall) _snapshot.value.agoraCallsCountMonth + 1 else _snapshot.value.agoraCallsCountMonth
        _snapshot.value = _snapshot.value.copy(
            agoraSecondsUsed = nextSec,
            agoraCallsCountMonth = nextCalls
        )
        prefs?.edit()
            ?.putLong(KEY_AGORA_SECONDS, nextSec)
            ?.putInt(KEY_AGORA_CALLS, nextCalls)
            ?.apply()
    }

    fun refreshSnapshot() {
        loadAndRollover()
    }

    private fun ensureCurrentPeriod() {
        val today = currentDayKey()
        val thisMonth = currentMonthKey()
        val cur = _snapshot.value
        if (cur.dayKey != today || cur.monthKey != thisMonth) {
            loadAndRollover()
        }
    }

    fun formatBytesCompact(bytes: Long): String {
        val safe = bytes.coerceAtLeast(0L)
        val gb = safe / (1024.0 * 1024.0 * 1024.0)
        val mb = safe / (1024.0 * 1024.0)
        val kb = safe / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb)
        }
    }

    fun formatMinutesCompact(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val mins = safe / 60L
        val secs = safe % 60L
        return if (mins > 0L) {
            if (secs > 0L) "${mins} মিনিট ${secs} সেকেন্ড" else "${mins} মিনিট"
        } else {
            "${secs} সেকেন্ড"
        }
    }
}
