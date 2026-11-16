package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

const val CLOUDINARY_FREE_SIGNUP_URL = "https://cloudinary.com/users/register_free"
const val CLOUDINARY_CONSOLE_URL = "https://console.cloudinary.com/"

data class CloudinaryStatus(
    val isEnabled: Boolean = true,
    val isConfigured: Boolean = false,
    val cloudName: String = "",
    val uploadPreset: String = "",
    val uploadedCount: Int = 0,
    val isUploading: Boolean = false,
    val lastUploadedUrl: String? = null,
    val lastError: String? = null
)

object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var appContext: Context? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val _status = MutableStateFlow(readBuildConfigStatus(isEnabled = true))
    val status: StateFlow<CloudinaryStatus> = _status.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _status.value = readBuildConfigStatus(isEnabled = _status.value.isEnabled)
    }

    fun getAppContext(): Context? = appContext

    private fun readBuildConfigStatus(isEnabled: Boolean): CloudinaryStatus {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
        val configured = cloudName.isNotBlank() &&
            cloudName != "MY_CLOUDINARY_CLOUD_NAME" &&
            preset.isNotBlank() &&
            preset != "MY_CLOUDINARY_UPLOAD_PRESET"
        return CloudinaryStatus(
            isEnabled = isEnabled,
            isConfigured = configured,
            cloudName = if (configured) cloudName else "",
            uploadPreset = if (configured) preset else ""
        )
    }

    fun isConfigured(): Boolean {
        val cur = readBuildConfigStatus(isEnabled = _status.value.isEnabled)
        return cur.isConfigured
    }

    fun isEnabled(): Boolean = _status.value.isEnabled

    fun setEnabled(enabled: Boolean) {
        _status.value = _status.value.copy(isEnabled = enabled)
    }

    fun refreshStatus() {
        val prevCount = _status.value.uploadedCount
        val prevUrl = _status.value.lastUploadedUrl
        _status.value = readBuildConfigStatus(isEnabled = _status.value.isEnabled).copy(
            uploadedCount = prevCount,
            lastUploadedUrl = prevUrl
        )
    }

    /**
     * Uploads local photo, video, or audio URI directly to Cloudinary (no credit card needed, 25 free credits/month).
     */
    fun uploadMedia(
        uri: Uri,
        isVideoOrAudio: Boolean,
        folder: String = "fotofilm",
        contextOverride: Context? = null,
        compressionKind: CompressionMediaKind? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val ctx = contextOverride?.applicationContext ?: appContext
        if (ctx == null) {
            onFailure(IllegalStateException("Application context not initialized for Cloudinary upload"))
            return
        }
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
        if (cloudName.isBlank() || cloudName == "MY_CLOUDINARY_CLOUD_NAME" ||
            preset.isBlank() || preset == "MY_CLOUDINARY_UPLOAD_PRESET"
        ) {
            val err = IllegalStateException("AI Studio Secrets-এ CLOUDINARY_CLOUD_NAME এবং CLOUDINARY_UPLOAD_PRESET যোগ করুন।")
            _status.value = _status.value.copy(lastError = err.message)
            onFailure(err)
            return
        }

        _status.value = _status.value.copy(isUploading = true, lastError = null)

        ioScope.launch {
            try {
                // Automatic pre-upload compression when enabled (100 MB -> 30 MB video, <= 300 KB photo)
                val effectiveKind = compressionKind ?: if (isVideoOrAudio) {
                    CompressionMediaKind.LONG_VIDEO
                } else {
                    CompressionMediaKind.PHOTO
                }
                val isAutoCompressionOn = DailyQuotaCompressionStore.config.value.isAutoCompressionEnabled
                val uploadUri = if (isAutoCompressionOn && uri.scheme != "https" && uri.scheme != "http") {
                    if (isVideoOrAudio) {
                        MediaCompressionManager.compressAndTrimVideo(ctx, uri, effectiveKind).outputUri
                    } else {
                        MediaCompressionManager.compressImageToMaxKb(ctx, uri, maxKb = 300).outputUri
                    }
                } else {
                    uri
                }

                val resourceType = if (isVideoOrAudio) "video" else "image"
                val endpoint = "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"
                val mimeType = ctx.contentResolver.getType(uploadUri)
                    ?: if (isVideoOrAudio) "video/mp4" else "image/jpeg"
                val fileName = if (isVideoOrAudio) "fotofilm_${System.currentTimeMillis()}.mp4"
                else "fotofilm_${System.currentTimeMillis()}.jpg"

                val requestBodyFile = object : RequestBody() {
                    override fun contentType() = mimeType.toMediaTypeOrNull()

                    override fun writeTo(sink: BufferedSink) {
                        val stream = if (uploadUri.scheme == "file" && uploadUri.path != null) {
                            File(uploadUri.path!!).inputStream()
                        } else {
                            ctx.contentResolver.openInputStream(uploadUri)
                        } ?: throw IllegalStateException("মিডিয়া ফাইল পড়া যায়নি")

                        stream.use { input ->
                            sink.writeAll(input.source())
                        }
                    }
                }

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", preset)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("file", fileName, requestBodyFile)
                    .build()

                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val errMsg = try {
                            JSONObject(raw).optJSONObject("error")?.optString("message")
                                ?: "Cloudinary upload failed (${response.code})"
                        } catch (_: Exception) {
                            "Cloudinary upload failed (${response.code})"
                        }
                        throw IllegalStateException(errMsg)
                    }
                    val json = JSONObject(raw)
                    val secureUrl = json.optString("secure_url")
                        .ifBlank { json.optString("url") }
                    if (secureUrl.isBlank()) {
                        throw IllegalStateException("Cloudinary did not return secure_url")
                    }

                    val compressedCloudUrl = if (isAutoCompressionOn) {
                        MediaCompressionManager.applyCloudinaryAutoCompressionUrl(secureUrl, effectiveKind)
                    } else {
                        secureUrl
                    }

                    _status.value = _status.value.copy(
                        isConfigured = true,
                        isUploading = false,
                        uploadedCount = _status.value.uploadedCount + 1,
                        lastUploadedUrl = compressedCloudUrl,
                        lastError = null
                    )
                    Log.d(TAG, "Cloudinary upload success (compressed): $compressedCloudUrl")
                    launch(Dispatchers.Main) {
                        onSuccess(compressedCloudUrl)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Cloudinary upload skipped/failed: ${e.message}")
                _status.value = _status.value.copy(
                    isUploading = false,
                    lastError = e.localizedMessage
                )
                launch(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }
}
