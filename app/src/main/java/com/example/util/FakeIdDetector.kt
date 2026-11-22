package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.example.data.SavedEmailAccount
import com.example.model.FakeIdReport
import com.example.model.Post
import com.example.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class GenderSignal {
    MALE, FEMALE, NEUTRAL
}

data class PhotoFingerprint(
    val normalizedSource: String,
    val byteSha256: String,
    val dHash: Long?,
    val displayNameHint: String
)

data class StoredPhotoRecord(
    val ownerId: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerGender: String,
    val normalizedSource: String,
    val byteSha256: String,
    val dHash: Long?
)

data class FakeIdCheckResult(
    val isAllowed: Boolean,
    val violationType: String? = null,
    val reasonMessage: String? = null,
    val matchedOwnerName: String = "",
    val detectedSignal: String = "",
    val fingerprint: PhotoFingerprint? = null
)

object FakeIdDetector {

    const val MAX_ACCOUNTS_PER_PERSON = 3

    private const val PREFS_NAME = "fotofilm_fake_id_security"
    private const val KEY_PHOTO_RECORDS = "photo_records_json"
    private const val KEY_FAKE_REPORTS = "fake_reports_json"

    // =========================================================================
    // Comprehensive Bengali + Banglish + English Gender Name Dictionaries
    // =========================================================================

    private val FEMALE_NAME_TOKENS = setOf(
        // English / Banglish female names
        "faria", "sadia", "nusrat", "tasnim", "jannat", "jannatul", "mim", "sumaiya", "sumaya",
        "ayesha", "aisha", "fatema", "fatima", "khadija", "mariam", "maryam", "sanjida", "tanjina",
        "samira", "lamia", "rafia", "sharmin", "shirin", "nasrin", "taslima", "rokeya", "rehana",
        "parvin", "akhter", "akter", "begum", "khatun", "moni", "ritu", "mou", "proma", "asha",
        "nisha", "priya", "puja", "pooja", "tania", "sonia", "rubina", "sabina", "farzana",
        "marufa", "sultana", "jahan", "tabassum", "afroza", "dilruba", "humaira", "maisha",
        "raisa", "maliha", "nawrin", "naurin", "fabiha", "tasfia", "mehreen", "mehzabin",
        "mahjabin", "anjuman", "salma", "hasina", "khaleda", "anamika", "sraboni", "bristi",
        "shila", "sheela", "riya", "tiya", "keya", "trisha", "arpita", "afrin", "nargis",
        "fahmida", "tahmina", "shahnaz", "rumana", "sanzida", "tanjila", "mousumi", "shabana",
        "purnima", "suborna", "tisha", "borsha", "laboni", "tithi", "urmi", "mimmi", "munni",
        "shanta", "monira", "yasmin", "razwana", "farhana", "tamanna", "lubna", "naznin",
        "sarah", "sara", "emma", "olivia", "sophia", "ava", "mia", "chloe", "lisa", "jennifer",
        "jessica", "maria", "elena", "anna", "emily", "hannah", "grace", "zoya", "hira",
        "mahira", "alia", "katrina", "deepika", "anushka", "angel", "queen", "princess",
        "girl", "lady", "female", "woman", "sister", "bhabi", "apa", "pori", "pakhi",
        "meher", "roza", "nabila", "nadia", "humayra", "arifa", "tasneem", "bushra", "zarin",
        "saima", "shumi", "eva", "mita", "rita", "gita", "sita", "anita", "bonita",
        "sharmeen", "sabiha", "ramisa", "sabnam", "shabnam", "nilufa", "nilufar", "sufia",
        "halima", "kulsum", "rabeya", "amina", "asima", "jesmin", "laila", "sheuly", "shiuly",
        // Bengali female names
        "ফারিয়া", "ফারিয়া", "সাদিয়া", "সাদিয়া", "নুসরাত", "তাসনিম", "জান্নাত", "জান্নাতুল",
        "মিম", "সুমাইয়া", "সুমাইয়া", "আয়েশা", "আয়েশা", "ফাতেমা", "খাদিজা", "মরিয়ম", "মরিয়ম",
        "সানজিদা", "তানজিনা", "সামিরা", "লামিয়া", "লামিয়া", "রাফিয়া", "শারমিন", "শিরিন",
        "নাসরিন", "তাসলিমা", "রোকেয়া", "রেহানা", "পারভীন", "আক্তার", "বেগম", "খাতুন",
        "মনি", "ঋতু", "মৌ", "প্রমা", "আশা", "নিশা", "প্রিয়া", "প্রিয়া", "পূজা",
        "তানিয়া", "তানিয়া", "সোনিয়া", "সোনিয়া", "রুবিনা", "সাবিনা", "ফারজানা", "মারুফা",
        "সুলতানা", "জাহান", "তাবাসসুম", "আফরোজা", "দিলরুবা", "হুমাইরা", "মাইশা", "রাইসা",
        "মালিহা", "নওরিন", "ফাবিহা", "তাসফিয়া", "মেহজাবিন", "সালমা", "অনামিকা", "শ্রাবণী",
        "বৃষ্টি", "শিলা", "রিয়া", "রিয়া", "পিয়া", "তিয়া", "কেয়া", "তৃষা", "অর্পিতা",
        "আফরিন", "নার্গিস", "ফাহমিদা", "তাহমিনা", "শাহনাজ", "রুমানা", "মৌসুমি", "তিশা",
        "বর্ষা", "লাবণী", "তিথি", "উর্মি", "মুন্নি", "শান্তা", "মনিরা", "ইয়াসমিন", "ফারহানা",
        "তামান্না", "লুবনা", "নাজনীন", "নাবিলা", "নাদিয়া", "আরিফা", "বুশরা", "জারিন",
        "সায়মা", "সুমি", "মিতা", "রিতা", "অনিতা", "সাবিহা", "রামিসা", "শবনম", "নিলুফা",
        "সুফিয়া", "হালিমা", "কুলসুম", "রাবেয়া", "আমিনা", "জেসমিন", "লাইলা", "শিউলি",
        "মেয়ে", "নারী", "মহিলা", "বোন", "আপা", "ভাবী", "পরী", "পাখি", "রানী"
    )

    private val MALE_NAME_TOKENS = setOf(
        // English / Banglish male names
        "niyon", "nihon", "sohag", "rahim", "karim", "sakib", "shakib", "tamim", "mashrafe",
        "rakib", "hasan", "hossain", "hussain", "ahmed", "ahmad", "md", "mohammad", "muhammad",
        "muhammed", "abdul", "abdullah", "abdur", "habib", "sohel", "rasel", "rana", "babu",
        "kamrul", "nazmul", "saiful", "tanvir", "tarek", "tariq", "farhan", "imran", "arman",
        "jubayer", "zubayer", "riyad", "mahmudul", "mehedi", "mahadi", "ashik", "ashiq",
        "rifat", "fahim", "siam", "shanto", "mamun", "monir", "delwar", "jamal", "kamal",
        "babul", "anis", "faruk", "farooq", "masud", "shafiq", "rafiq", "alamin", "nadim",
        "naeem", "sajib", "sajeeb", "dipu", "joy", "bijoy", "sourav", "akash", "sagor",
        "nabil", "nahid", "tuhin", "ashraf", "zakir", "mizan", "mizanur", "kawsar", "rubel",
        "shahid", "sahid", "salman", "amir", "arafat", "fahad", "saad", "bilal", "yusuf",
        "ibrahim", "ismail", "omar", "umar", "usman", "ali", "zahir", "jahir", "zahid",
        "jahid", "shuvo", "shubo", "sujon", "sujan", "sumon", "shumon", "liton", "mithun",
        "biplob", "parvez", "forhad", "monirul", "aminul", "azad", "sojib", "emran",
        "roman", "sabbir", "rakibul", "asif", "atif", "armanul", "ripon", "khalid", "tareq",
        "kabir", "ashraful", "shahin", "shaheen", "rony", "robiul", "rabiul", "touhid",
        "tawhid", "mostafa", "mustafa", "kamruzzaman", "badol", "manik", "raton", "tapan",
        "john", "david", "michael", "james", "robert", "alex", "daniel", "kevin", "adam",
        "mark", "chris", "ryan", "jason", "brian", "king", "prince", "boy", "man", "male",
        "bhai", "bro", "uncle", "dada", "mama", "hero",
        // Bengali male names
        "নিয়ন", "নিয়ন", "নয়ন", "নয়ন", "সোহাগ", "রহিম", "করিম", "সাকিব", "শাকিব",
        "তামিম", "মাশরাফি", "রাকিব", "হাসান", "হোসেন", "আহমেদ", "আহমদ", "মোঃ", "মোহাম্মদ",
        "আব্দুল", "আবদুল", "আবদুল্লাহ", "আব্দুর", "হাবিব", "সোহেল", "রাসেল", "রানা", "বাবু",
        "কামরুল", "নাজমুল", "সাইফুল", "তানভীর", "তারেক", "ফারহান", "ইমরান", "আরমান",
        "জুবায়ের", "জুবায়ের", "রিয়াদ", "মাহমুদুল", "মেহেদী", "মাহাদী", "আশিক", "রিফাত",
        "ফাহিম", "সিয়াম", "শান্ত", "মামুন", "মনির", "দেলোয়ার", "জামাল", "কামাল",
        "বাবুল", "আনিস", "ফারুক", "মাসুদ", "শফিক", "রফিক", "আল-আমিন", "নাদিম", "নাঈম",
        "সজীব", "দিপু", "জয়", "জয়", "বিজয়", "সৌরভ", "আকাশ", "সাগর", "নাবিল", "নাহিদ",
        "তুহিন", "আশরাফ", "জাকির", "মিজান", "মিজানুর", "কাওসার", "রুবেল", "শহীদ", "সালমান",
        "আরাফাত", "ফাহাদ", "বিল্লাল", "ইউসুফ", "ইব্রাহিম", "ইসমাইল", "ওমর", "উসমান",
        "আলী", "জহির", "জাহিদ", "শুভ", "সুজন", "সুমন", "লিটন", "মিঠুন", "বিপ্লব",
        "পারভেজ", "ফরহাদ", "মনিরুল", "আমিনুল", "আজাদ", "সাব্বির", "রাকিবুল", "আসিফ",
        "রিপন", "খালেদ", "কবির", "আশরাফুল", "শাহীন", "রনি", "রবিউল", "তৌহিদ",
        "মোস্তফা", "বাদল", "মানিক", "রতন", "তপন", "ছেলে", "পুরুষ", "ভাই", "দাদা"
    )

    private val PHOTO_FEMALE_HINTS = listOf(
        "girl", "woman", "female", "women", "lady", "bride", "hijab", "saree", "sari",
        "salwar", "kameez", "lehenga", "queen", "princess", "makeup", "lipstick", "bhabi",
        "sister", "meher", "woman_", "female_", "girl_"
    )

    private val PHOTO_MALE_HINTS = listOf(
        "boy", "man_", "_man", "male", "groom", "beard", "moustache", "panjabi",
        "bhai", "king", "prince", "men_", "boy_"
    )

    fun isMaleGender(gender: String): Boolean {
        val g = gender.trim().lowercase()
        return g.contains("পুরুষ") || g == "male" || g == "m" || g.contains("boy")
    }

    fun isFemaleGender(gender: String): Boolean {
        val g = gender.trim().lowercase()
        return g.contains("নারী") || g.contains("মহিলা") || g == "female" || g == "f" || g.contains("girl") || g.contains("woman")
    }

    fun detectNameGender(fullName: String, username: String = ""): GenderSignal {
        val rawTokens = "$fullName $username"
            .lowercase()
            .replace(Regex("[^a-z\\u0980-\\u09FF0-9\\s_]"), " ")
            .split(Regex("[\\s_0-9]+"))
            .map { it.trim() }
            .filter { it.length >= 2 }

        var maleScore = 0
        var femaleScore = 0

        for (token in rawTokens) {
            if (token in FEMALE_NAME_TOKENS) femaleScore += 2
            if (token in MALE_NAME_TOKENS) maleScore += 2
        }

        // Substring check inside concatenated handles/usernames like "sadiarahman" or "rakibul99"
        if (maleScore == 0 && femaleScore == 0) {
            val joined = "$fullName$username".lowercase().replace(Regex("[^a-z\\u0980-\\u09FF]"), "")
            for (fToken in FEMALE_NAME_TOKENS) {
                if (fToken.length >= 4 && joined.contains(fToken)) {
                    femaleScore++
                    break
                }
            }
            for (mToken in MALE_NAME_TOKENS) {
                if (mToken.length >= 4 && joined.contains(mToken)) {
                    maleScore++
                    break
                }
            }
        }

        return when {
            femaleScore > maleScore -> GenderSignal.FEMALE
            maleScore > femaleScore -> GenderSignal.MALE
            else -> GenderSignal.NEUTRAL
        }
    }

    fun detectPhotoGenderHint(uriOrUrl: String, displayName: String = ""): GenderSignal {
        val combined = "$uriOrUrl $displayName".lowercase()
        if (combined.isBlank()) return GenderSignal.NEUTRAL
        val hasFemaleHint = PHOTO_FEMALE_HINTS.any { combined.contains(it) }
        val hasMaleHint = PHOTO_MALE_HINTS.any { combined.contains(it) }
        return when {
            hasFemaleHint && !hasMaleHint -> GenderSignal.FEMALE
            hasMaleHint && !hasFemaleHint -> GenderSignal.MALE
            else -> GenderSignal.NEUTRAL
        }
    }

    fun computePhotoFingerprint(context: Context?, source: String): PhotoFingerprint? {
        val trimmed = source.trim()
        if (trimmed.isBlank()) return null

        val normalized = normalizeUrl(trimmed)
        var byteHash = sha256(normalized)
        var dHash: Long? = null
        var displayName = trimmed.substringAfterLast('/')

        if (context != null && (trimmed.startsWith("content://") || trimmed.startsWith("file://"))) {
            try {
                val uri = Uri.parse(trimmed)
                // Try resolving display filename for gender cues
                if (trimmed.startsWith("content://")) {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0) {
                                displayName = cursor.getString(idx).orEmpty()
                            }
                        }
                    }
                }
                // Compute SHA-256 over image bytes (up to 6 MB)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buf = ByteArray(16384)
                    var total = 0
                    while (total < 6 * 1024 * 1024) {
                        val read = stream.read(buf)
                        if (read <= 0) break
                        digest.update(buf, 0, read)
                        total += read
                    }
                    if (total > 0) {
                        byteHash = digest.digest().joinToString("") { "%02x".format(it) }
                    }
                }
                // Compute 64-bit perceptual difference hash (9x8 grayscale bitmap)
                dHash = computeDHashFromUri(context, uri)
            } catch (_: Exception) {
            }
        }

        return PhotoFingerprint(
            normalizedSource = normalized,
            byteSha256 = byteHash,
            dHash = dHash,
            displayNameHint = displayName
        )
    }

    private fun computeDHashFromUri(context: Context, uri: Uri): Long? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val sampleSize = if (bounds.outWidth > 128 || bounds.outHeight > 128) {
                maxOf(1, minOf(bounds.outWidth / 64, bounds.outHeight / 64))
            } else 1
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return null

            val scaled = Bitmap.createScaledBitmap(decoded, 9, 8, true)
            if (scaled != decoded) decoded.recycle()

            var hash = 0L
            var bit = 0
            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val left = luminance(scaled.getPixel(x, y))
                    val right = luminance(scaled.getPixel(x + 1, y))
                    if (left > right) {
                        hash = hash or (1L shl bit)
                    }
                    bit++
                }
            }
            scaled.recycle()
            hash
        } catch (_: Exception) {
            null
        }
    }

    private fun luminance(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (r * 299 + g * 587 + b * 114) / 1000
    }

    private fun hammingDistance(a: Long, b: Long): Int {
        return java.lang.Long.bitCount(a xor b)
    }

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .substringBefore("?")
            .lowercase()
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun formatTimestampNow(): String {
        return try {
            SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
        } catch (_: Exception) {
            "আজ"
        }
    }

    // =========================================================================
    // Persistent Photo Registry & Fake ID Report Store
    // =========================================================================

    fun loadPhotoRecords(context: Context): List<StoredPhotoRecord> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_PHOTO_RECORDS, null) ?: return emptyList()
        val list = mutableListOf<StoredPhotoRecord>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val dHashStr = obj.optString("dHash", "")
                list.add(
                    StoredPhotoRecord(
                        ownerId = obj.optString("ownerId"),
                        ownerName = obj.optString("ownerName"),
                        ownerEmail = obj.optString("ownerEmail"),
                        ownerGender = obj.optString("ownerGender"),
                        normalizedSource = obj.optString("normalizedSource"),
                        byteSha256 = obj.optString("byteSha256"),
                        dHash = dHashStr.toLongOrNull()
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun registerPhotoFingerprint(
        context: Context?,
        ownerId: String,
        ownerName: String,
        ownerEmail: String,
        ownerGender: String,
        avatarUrl: String,
        precomputed: PhotoFingerprint? = null
    ) {
        if (context == null || avatarUrl.isBlank()) return
        val fp = precomputed ?: computePhotoFingerprint(context, avatarUrl) ?: return
        val current = loadPhotoRecords(context).toMutableList()
        current.removeAll {
            it.ownerId == ownerId ||
                (ownerEmail.isNotBlank() && it.ownerEmail.equals(ownerEmail, ignoreCase = true))
        }
        current.add(
            0,
            StoredPhotoRecord(
                ownerId = ownerId,
                ownerName = ownerName,
                ownerEmail = ownerEmail,
                ownerGender = ownerGender,
                normalizedSource = fp.normalizedSource,
                byteSha256 = fp.byteSha256,
                dHash = fp.dHash
            )
        )
        val arr = JSONArray()
        current.take(200).forEach { rec ->
            arr.put(
                JSONObject().apply {
                    put("ownerId", rec.ownerId)
                    put("ownerName", rec.ownerName)
                    put("ownerEmail", rec.ownerEmail)
                    put("ownerGender", rec.ownerGender)
                    put("normalizedSource", rec.normalizedSource)
                    put("byteSha256", rec.byteSha256)
                    put("dHash", rec.dHash?.toString() ?: "")
                }
            )
        }
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PHOTO_RECORDS, arr.toString())
            .apply()
    }

    fun loadReports(context: Context): List<FakeIdReport> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_FAKE_REPORTS, null) ?: return emptyList()
        val list = mutableListOf<FakeIdReport>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    FakeIdReport(
                        id = obj.optString("id"),
                        attemptedName = obj.optString("attemptedName"),
                        attemptedUsername = obj.optString("attemptedUsername"),
                        attemptedEmail = obj.optString("attemptedEmail"),
                        selectedGender = obj.optString("selectedGender"),
                        detectedSignal = obj.optString("detectedSignal"),
                        violationType = obj.optString("violationType"),
                        violationDetails = obj.optString("violationDetails"),
                        attemptedAvatarUrl = obj.optString("attemptedAvatarUrl"),
                        matchedExistingUser = obj.optString("matchedExistingUser"),
                        deviceModel = obj.optString("deviceModel").ifBlank { "${Build.MANUFACTURER} ${Build.MODEL}" },
                        timestamp = obj.optString("timestamp"),
                        status = obj.optString("status").ifBlank { "AUTO_BLOCKED" }
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun saveReports(context: Context, reports: List<FakeIdReport>) {
        val arr = JSONArray()
        reports.take(200).forEach { rep ->
            arr.put(
                JSONObject().apply {
                    put("id", rep.id)
                    put("attemptedName", rep.attemptedName)
                    put("attemptedUsername", rep.attemptedUsername)
                    put("attemptedEmail", rep.attemptedEmail)
                    put("selectedGender", rep.selectedGender)
                    put("detectedSignal", rep.detectedSignal)
                    put("violationType", rep.violationType)
                    put("violationDetails", rep.violationDetails)
                    put("attemptedAvatarUrl", rep.attemptedAvatarUrl)
                    put("matchedExistingUser", rep.matchedExistingUser)
                    put("deviceModel", rep.deviceModel)
                    put("timestamp", rep.timestamp)
                    put("status", rep.status)
                }
            )
        }
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAKE_REPORTS, arr.toString())
            .apply()
    }

    fun recordReport(context: Context?, report: FakeIdReport): List<FakeIdReport> {
        if (context == null) return listOf(report)
        val updated = listOf(report) + loadReports(context)
        saveReports(context, updated)
        return updated
    }

    fun updateReportStatus(context: Context?, reportId: String, newStatus: String): List<FakeIdReport> {
        if (context == null) return emptyList()
        val updated = loadReports(context).map {
            if (it.id == reportId) it.copy(status = newStatus) else it
        }
        saveReports(context, updated)
        return updated
    }

    fun deleteReport(context: Context?, reportId: String): List<FakeIdReport> {
        if (context == null) return emptyList()
        val updated = loadReports(context).filterNot { it.id == reportId }
        saveReports(context, updated)
        return updated
    }

    fun clearReports(context: Context?): List<FakeIdReport> {
        if (context == null) return emptyList()
        saveReports(context, emptyList())
        return emptyList()
    }

    fun createReport(
        attemptedName: String,
        attemptedUsername: String,
        attemptedEmail: String,
        selectedGender: String,
        detectedSignal: String,
        violationType: String,
        violationDetails: String,
        attemptedAvatarUrl: String = "",
        matchedExistingUser: String = ""
    ): FakeIdReport {
        return FakeIdReport(
            id = "fake_${UUID.randomUUID().toString().take(8)}",
            attemptedName = attemptedName.ifBlank { "অজানা ব্যবহারকারী" },
            attemptedUsername = attemptedUsername.ifBlank { "unknown" },
            attemptedEmail = attemptedEmail.ifBlank { "N/A" },
            selectedGender = selectedGender.ifBlank { "অনির্ধারিত" },
            detectedSignal = detectedSignal,
            violationType = violationType,
            violationDetails = violationDetails,
            attemptedAvatarUrl = attemptedAvatarUrl,
            matchedExistingUser = matchedExistingUser,
            timestamp = formatTimestampNow(),
            status = "AUTO_BLOCKED"
        )
    }

    // =========================================================================
    // Main Validation Entry Point
    // =========================================================================

    fun validateRegistrationOrUpdate(
        context: Context?,
        candidateUserId: String,
        candidateEmail: String,
        fullName: String,
        username: String,
        selectedGender: String,
        avatarUrl: String?,
        existingUsers: List<User>,
        existingPosts: List<Post>,
        savedEmailAccounts: List<SavedEmailAccount>,
        isOwnerVerified: Boolean = false
    ): FakeIdCheckResult {
        val cleanAvatar = avatarUrl?.trim().orEmpty()
        val fp = if (cleanAvatar.isNotBlank()) computePhotoFingerprint(context, cleanAvatar) else null

        // 1. Cross-Gender Fake ID Prevention:
        //    - Male cannot register with female name or female photo
        //    - Female cannot register with male name or male photo
        val nameGender = detectNameGender(fullName, username)
        val photoGenderHint = if (cleanAvatar.isNotBlank()) {
            detectPhotoGenderHint(cleanAvatar, fp?.displayNameHint.orEmpty())
        } else {
            GenderSignal.NEUTRAL
        }

        if (!isOwnerVerified) {
            if (isMaleGender(selectedGender)) {
                if (nameGender == GenderSignal.FEMALE) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "MALE_TO_FEMALE_FAKE",
                        reasonMessage = "⛔ ফেইক আইডি ব্লক! কোনো ছেলে কোনো মেয়ের নামে ফেইক আইডি খুলতে পারবেন না। আপনার নিজের আসল নাম দিন।",
                        detectedSignal = "পুরুষ (ছেলে) -> মেয়ের নাম: $fullName",
                        fingerprint = fp
                    )
                }
                if (photoGenderHint == GenderSignal.FEMALE) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "MALE_TO_FEMALE_FAKE",
                        reasonMessage = "⛔ ফেইক আইডি ব্লক! কোনো ছেলে কোনো মেয়ের ছবি দিয়ে ফেইক আইডি খুলতে পারবেন না। আপনার নিজের আসল ছবি ব্যবহার করুন।",
                        detectedSignal = "পুরুষ (ছেলে) -> মেয়ের ছবি (${fp?.displayNameHint.orEmpty()})",
                        fingerprint = fp
                    )
                }
            } else if (isFemaleGender(selectedGender)) {
                if (nameGender == GenderSignal.MALE) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "FEMALE_TO_MALE_FAKE",
                        reasonMessage = "⛔ ফেইক আইডি ব্লক! কোনো মেয়ে কোনো ছেলের নামে ফেইক আইডি খুলতে পারবেন না। আপনার নিজের আসল নাম দিন।",
                        detectedSignal = "নারী (মেয়ে) -> ছেলের নাম: $fullName",
                        fingerprint = fp
                    )
                }
                if (photoGenderHint == GenderSignal.MALE) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "FEMALE_TO_MALE_FAKE",
                        reasonMessage = "⛔ ফেইক আইডি ব্লক! কোনো মেয়ে কোনো ছেলের ছবি দিয়ে ফেইক আইডি খুলতে পারবেন না। আপনার নিজের আসল ছবি ব্যবহার করুন।",
                        detectedSignal = "নারী (মেয়ে) -> ছেলের ছবি (${fp?.displayNameHint.orEmpty()})",
                        fingerprint = fp
                    )
                }
            }
        }

        // 2. Anti-Stolen-Photo Check (using someone else's photo to open a fake ID)
        if (cleanAvatar.isNotBlank() && fp != null) {
            val cleanEmailLower = candidateEmail.trim().lowercase()

            // 2a. Check persisted photo fingerprint store (byte SHA-256 + perceptual dHash)
            if (context != null) {
                val stored = loadPhotoRecords(context)
                for (rec in stored) {
                    val isSameOwner = (candidateUserId.isNotBlank() && rec.ownerId == candidateUserId) ||
                        (cleanEmailLower.isNotBlank() && rec.ownerEmail.equals(cleanEmailLower, ignoreCase = true))
                    if (isSameOwner) continue

                    val exactHashMatch = rec.byteSha256 == fp.byteSha256 ||
                        (rec.normalizedSource.isNotBlank() && rec.normalizedSource == fp.normalizedSource)
                    val perceptualMatch = rec.dHash != null && fp.dHash != null &&
                        hammingDistance(rec.dHash, fp.dHash) <= 5

                    if (exactHashMatch || perceptualMatch) {
                        val ownerDisplay = rec.ownerName.ifBlank { "অন্য ব্যবহারকারী" }
                        return FakeIdCheckResult(
                            isAllowed = false,
                            violationType = "STOLEN_PHOTO",
                            reasonMessage = "⛔ অপরের ছবি ব্যবহার ব্লক! এই ছবিটি আগে থেকেই \"$ownerDisplay\" এর আইডিতে রয়েছে। অন্যের ছবি দিয়ে কোনো ধরনের ফেইক আইডি খোলা যাবে না।",
                            matchedOwnerName = ownerDisplay,
                            detectedSignal = if (exactHashMatch) "অন্যের ছবির হুবহু মিল (SHA-256)" else "অন্যের ছবির ভিজ্যুয়াল মিল (dHash)",
                            fingerprint = fp
                        )
                    }
                }
            }

            // 2b. Check against all existing users in memory
            for (u in existingUsers) {
                val isSameUser = (candidateUserId.isNotBlank() && u.id == candidateUserId) ||
                    (cleanEmailLower.isNotBlank() && u.email.equals(cleanEmailLower, ignoreCase = true))
                if (isSameUser) continue
                val existingNormalized = normalizeUrl(u.avatarUrl)
                if (existingNormalized.isNotBlank() && existingNormalized == fp.normalizedSource) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "STOLEN_PHOTO",
                        reasonMessage = "⛔ অপরের ছবি ব্যবহার ব্লক! এই ছবিটি আগে থেকেই \"${u.fullName}\" এর প্রোফাইলে রয়েছে। অন্যের ছবি দিয়ে কোনো ধরনের ফেইক আইডি খোলা যাবে না।",
                        matchedOwnerName = u.fullName,
                        detectedSignal = "অন্য ইউজারের প্রোফাইল ছবির সাথে মিল (${u.fullName})",
                        fingerprint = fp
                    )
                }
            }

            // 2c. Check against saved email accounts on this device
            for (acc in savedEmailAccounts) {
                val isSameAccount = (candidateUserId.isNotBlank() && acc.id == candidateUserId) ||
                    (cleanEmailLower.isNotBlank() && acc.email.equals(cleanEmailLower, ignoreCase = true))
                if (isSameAccount) continue
                val existingNormalized = normalizeUrl(acc.avatarUrl)
                if (existingNormalized.isNotBlank() && existingNormalized == fp.normalizedSource) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "STOLEN_PHOTO",
                        reasonMessage = "⛔ অপরের ছবি ব্যবহার ব্লক! এই ছবিটি আগে থেকেই \"${acc.fullName}\" এর অ্যাকাউন্টে ব্যবহৃত হয়েছে।",
                        matchedOwnerName = acc.fullName,
                        detectedSignal = "পূর্ববর্তী অ্যাকাউন্টের ছবির সাথে মিল (${acc.fullName})",
                        fingerprint = fp
                    )
                }
            }

            // 2d. Check against existing post photos / author avatars
            for (post in existingPosts) {
                if (candidateUserId.isNotBlank() && post.author.id == candidateUserId) continue
                val postAvatarNorm = normalizeUrl(post.author.avatarUrl)
                if (postAvatarNorm.isNotBlank() && postAvatarNorm == fp.normalizedSource) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "STOLEN_PHOTO",
                        reasonMessage = "⛔ অপরের ছবি ব্যবহার ব্লক! এই ছবিটি \"${post.author.fullName}\" এর ছবি হিসেবে পাওয়া গেছে। অন্যের ছবি দিয়ে ফেইক আইডি খোলা নিষিদ্ধ।",
                        matchedOwnerName = post.author.fullName,
                        detectedSignal = "অন্যের পোস্টের ছবির সাথে মিল (${post.author.fullName})",
                        fingerprint = fp
                    )
                }
                if (post.images.any { normalizeUrl(it) == fp.normalizedSource }) {
                    return FakeIdCheckResult(
                        isAllowed = false,
                        violationType = "STOLEN_PHOTO",
                        reasonMessage = "⛔ অপরের ছবি ব্যবহার ব্লক! এই ছবিটি \"${post.author.fullName}\" এর শেয়ার করা ছবি। নিজের আসল ছবি ব্যবহার করুন।",
                        matchedOwnerName = post.author.fullName,
                        detectedSignal = "অন্যের আপলোডকৃত পোস্টের ছবির সাথে মিল (${post.author.fullName})",
                        fingerprint = fp
                    )
                }
            }
        }

        return FakeIdCheckResult(
            isAllowed = true,
            fingerprint = fp
        )
    }
}
