package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object MediaSaveHelper {

    private val PROFILE_LINK_REGEX = Regex(
        pattern = "(https?://(?:fotofilm\\.app|facefeed\\.com)/u/[A-Za-z0-9_.\\-]+|facefeed://u/[A-Za-z0-9_.\\-]+)",
        option = RegexOption.IGNORE_CASE
    )

    fun extractProfileLink(text: String): String? {
        return PROFILE_LINK_REGEX.find(text)?.value
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun shareTextOrMediaLink(context: Context, text: String, subject: String = "FotoFilm") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, "শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
        }
    }

    suspend fun saveMediaToGallery(
        context: Context,
        mediaUrl: String,
        isVideo: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val ext = if (isVideo) "mp4" else "jpg"
            val prefix = if (isVideo) "FotoFilm_VID" else "FotoFilm_IMG"
            val fileName = "${prefix}_${System.currentTimeMillis()}.$ext"
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"

            val inputBytes: ByteArray = readMediaBytes(context, mediaUrl)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val collection = if (isVideo) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }
                val relativePath = if (isVideo) {
                    Environment.DIRECTORY_MOVIES + "/FotoFilm"
                } else {
                    Environment.DIRECTORY_PICTURES + "/FotoFilm"
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val itemUri = resolver.insert(collection, values)
                    ?: throw IllegalStateException("মিডিয়া স্টোরে ফাইল তৈরি করা যায়নি")

                try {
                    resolver.openOutputStream(itemUri)?.use { out ->
                        out.write(inputBytes)
                        out.flush()
                    } ?: throw IllegalStateException("আউটপুট স্ট্রিম খোলা যায়নি")

                    val readyValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    resolver.update(itemUri, readyValues, null, null)
                } catch (e: Exception) {
                    resolver.delete(itemUri, null, null)
                    throw e
                }

                "$relativePath/$fileName"
            } else {
                val dirType = if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
                val baseDir = context.getExternalFilesDir(dirType) ?: context.filesDir
                val targetDir = File(baseDir, "FotoFilm").apply { mkdirs() }
                val outFile = File(targetDir, fileName)
                FileOutputStream(outFile).use { out ->
                    out.write(inputBytes)
                    out.flush()
                }
                outFile.absolutePath
            }
        }
    }

    private fun readMediaBytes(context: Context, mediaUrl: String): ByteArray {
        val trimmed = mediaUrl.trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> {
                var currentUrl = trimmed
                var connection: HttpURLConnection? = null
                try {
                    // Follow up to 3 redirects manually across protocols if needed
                    repeat(4) {
                        val url = URL(currentUrl)
                        connection = (url.openConnection() as HttpURLConnection).apply {
                            connectTimeout = 15000
                            readTimeout = 20000
                            instanceFollowRedirects = true
                            setRequestProperty("User-Agent", "FotoFilmAndroid/1.0")
                        }
                        val code = connection!!.responseCode
                        if (code in 300..399) {
                            val next = connection!!.getHeaderField("Location")
                            connection!!.disconnect()
                            if (!next.isNullOrBlank()) {
                                currentUrl = next
                            }
                        } else {
                            return@repeat
                        }
                    }
                    connection!!.inputStream.use { it.readBytes() }
                } finally {
                    connection?.disconnect()
                }
            }
            trimmed.startsWith("content://", ignoreCase = true) ||
                trimmed.startsWith("android.resource://", ignoreCase = true) -> {
                val uri = Uri.parse(trimmed)
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("ফাইলটি পড়া যায়নি")
            }
            trimmed.startsWith("file://", ignoreCase = true) -> {
                val path = Uri.parse(trimmed).path ?: throw IllegalArgumentException("অকার্যকর ফাইল পাথ")
                FileInputStream(File(path)).use { it.readBytes() }
            }
            else -> {
                val file = File(trimmed)
                if (file.exists()) {
                    FileInputStream(file).use { it.readBytes() }
                } else {
                    throw IllegalArgumentException("মিডিয়া সোর্স পাওয়া যায়নি")
                }
            }
        }
    }
}
