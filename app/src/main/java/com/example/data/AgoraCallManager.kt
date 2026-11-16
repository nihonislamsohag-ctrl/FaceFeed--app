package com.example.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import com.example.BuildConfig
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val AGORA_FREE_SIGNUP_URL = "https://console.agora.io/"

data class AgoraRtcState(
    val isConfigured: Boolean = false,
    val isSdkActive: Boolean = false,
    val isJoinedChannel: Boolean = false,
    val isVideoActive: Boolean = false,
    val channelName: String = "",
    val remoteUids: List<Int> = emptyList(),
    val statusText: String = "প্রস্তুত",
    val isMicMuted: Boolean = false,
    val isSpeakerEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val lastError: String? = null
)

object AgoraCallManager {
    private const val TAG = "AgoraCallManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    private var rtcEngine: RtcEngine? = null
    private var currentVideoMode: Boolean = false

    private val _callState = MutableStateFlow(
        AgoraRtcState(
            isConfigured = isConfigured(),
            statusText = if (isConfigured()) "Agora HD প্রস্তুত ✓" else "AI Studio Secrets-এ AGORA_APP_ID দিন"
        )
    )
    val callState: StateFlow<AgoraRtcState> = _callState.asStateFlow()

    private fun readCleanAppId(): String {
        return try {
            BuildConfig.AGORA_APP_ID
                .trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .trim()
        } catch (_: Exception) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        val appId = readCleanAppId()
        return appId.isNotBlank() && appId != "MY_AGORA_APP_ID"
    }

    fun refreshConfigStatus() {
        val configured = isConfigured()
        val cur = _callState.value
        if (!cur.isSdkActive) {
            _callState.value = cur.copy(
                isConfigured = configured,
                statusText = if (configured) "Agora HD প্রস্তুত ✓" else "AI Studio Secrets-এ AGORA_APP_ID দিন"
            )
        }
    }

    fun sanitizeChannelName(raw: String): String {
        val cleaned = raw.lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .trim('_')
            .take(56)
        return if (cleaned.isBlank()) "fotofilm_call_room" else cleaned
    }

    fun buildDirectCallChannel(myId: String, friendId: String): String {
        val pair = listOf(myId.trim(), friendId.trim()).sorted()
        return sanitizeChannelName("ff_${pair[0]}_${pair[1]}")
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            mainHandler.post {
                Log.d(TAG, "Agora joined channel=$channel uid=$uid")
                _callState.value = _callState.value.copy(
                    isSdkActive = true,
                    isJoinedChannel = true,
                    channelName = channel ?: _callState.value.channelName,
                    statusText = "Agora HD সংযুক্ত ✓",
                    lastError = null
                )
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            mainHandler.post {
                Log.d(TAG, "Agora remote user joined: $uid")
                val updated = (_callState.value.remoteUids + uid).distinct()
                _callState.value = _callState.value.copy(
                    remoteUids = updated,
                    statusText = "কথা হচ্ছে (Agora HD) ✓"
                )
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            mainHandler.post {
                val updated = _callState.value.remoteUids.filter { it != uid }
                _callState.value = _callState.value.copy(
                    remoteUids = updated,
                    statusText = if (updated.isEmpty()) "কল সংযোগ বিচ্ছিন্ন" else "কথা হচ্ছে (Agora HD) ✓"
                )
            }
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            mainHandler.post {
                if (state == Constants.CONNECTION_STATE_FAILED) {
                    _callState.value = _callState.value.copy(
                        statusText = "নেটওয়ার্ক সংযোগ সমস্যা ($reason)",
                        lastError = "Connection failed reason=$reason"
                    )
                }
            }
        }

        override fun onError(err: Int) {
            mainHandler.post {
                Log.w(TAG, "Agora RTC error code=$err")
                val msg = when (err) {
                    101 -> "Agora App ID সঠিক নয় (কোড 101)"
                    109, 110 -> "Agora Console-এ Testing Mode (App ID only) চালু করুন (কোড $err)"
                    else -> "Agora স্ট্যাটাস কোড ($err)"
                }
                _callState.value = _callState.value.copy(
                    lastError = msg,
                    statusText = msg
                )
            }
        }
    }

    fun joinCall(
        context: Context,
        channelName: String,
        isVideoCall: Boolean,
        startMuted: Boolean = false
    ) {
        val safeChannel = sanitizeChannelName(channelName)
        if (!isConfigured()) {
            _callState.value = _callState.value.copy(
                isConfigured = false,
                isSdkActive = false,
                isJoinedChannel = false,
                isVideoActive = false,
                channelName = safeChannel,
                statusText = "Agora প্রিভিউ (Secrets-এ AGORA_APP_ID দিন)"
            )
            return
        }

        val current = _callState.value
        if (current.isSdkActive && current.isJoinedChannel && current.channelName == safeChannel && currentVideoMode == isVideoCall) {
            return
        }

        val appId = readCleanAppId()
        try {
            if (rtcEngine != null && (current.isJoinedChannel || current.channelName.isNotBlank())) {
                try {
                    rtcEngine?.stopPreview()
                    rtcEngine?.leaveChannel()
                } catch (_: Exception) {
                }
            }

            if (rtcEngine == null) {
                val config = RtcEngineConfig().apply {
                    mContext = context.applicationContext
                    mAppId = appId
                    mEventHandler = rtcEventHandler
                    mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                }
                rtcEngine = RtcEngine.create(config)
            }

            val engine = rtcEngine ?: return
            currentVideoMode = isVideoCall
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            engine.enableAudio()
            engine.setDefaultAudioRoutetoSpeakerphone(isVideoCall)
            engine.setEnableSpeakerphone(isVideoCall)
            engine.muteLocalAudioStream(startMuted)

            if (isVideoCall) {
                engine.enableVideo()
                engine.setVideoEncoderConfiguration(
                    VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_640x360,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    )
                )
                engine.startPreview()
            } else {
                engine.stopPreview()
                engine.disableVideo()
            }

            val options = ChannelMediaOptions().apply {
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                publishMicrophoneTrack = true
                publishCameraTrack = isVideoCall
                autoSubscribeAudio = true
                autoSubscribeVideo = isVideoCall
            }

            val joinCode = engine.joinChannel(null, safeChannel, 0, options)
            if (joinCode != 0 && joinCode != -17) {
                Log.w(TAG, "joinChannel returned error code: $joinCode")
                _callState.value = _callState.value.copy(
                    isConfigured = true,
                    isSdkActive = false,
                    isVideoActive = false,
                    channelName = safeChannel,
                    lastError = "Agora জয়েন কোড: $joinCode",
                    statusText = "Agora জয়েন কোড ($joinCode)"
                )
            } else {
                _callState.value = _callState.value.copy(
                    isConfigured = true,
                    isSdkActive = true,
                    isVideoActive = isVideoCall,
                    channelName = safeChannel,
                    isMicMuted = startMuted,
                    isSpeakerEnabled = isVideoCall,
                    lastError = null,
                    statusText = if (isVideoCall) "Agora ভিডিও রিং হচ্ছে..." else "Agora অডিও রিং হচ্ছে..."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora call: ${e.message}", e)
            _callState.value = _callState.value.copy(
                isSdkActive = false,
                isVideoActive = false,
                lastError = e.localizedMessage,
                statusText = "Agora কল শুরু করা যায়নি"
            )
        }
    }

    fun setupLocalVideo(surfaceView: SurfaceView) {
        try {
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            rtcEngine?.setupLocalVideo(canvas)
            rtcEngine?.startPreview()
        } catch (e: Exception) {
            Log.w(TAG, "setupLocalVideo failed: ${e.message}")
        }
    }

    fun setupRemoteVideo(surfaceView: SurfaceView, remoteUid: Int) {
        try {
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, remoteUid)
            rtcEngine?.setupRemoteVideo(canvas)
        } catch (e: Exception) {
            Log.w(TAG, "setupRemoteVideo failed: ${e.message}")
        }
    }

    fun setMicMuted(muted: Boolean) {
        try {
            rtcEngine?.muteLocalAudioStream(muted)
        } catch (_: Exception) {
        }
        _callState.value = _callState.value.copy(isMicMuted = muted)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        try {
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(enabled)
            rtcEngine?.setEnableSpeakerphone(enabled)
        } catch (_: Exception) {
        }
        _callState.value = _callState.value.copy(isSpeakerEnabled = enabled)
    }

    fun switchCamera() {
        try {
            rtcEngine?.switchCamera()
        } catch (_: Exception) {
        }
        _callState.value = _callState.value.copy(isFrontCamera = !_callState.value.isFrontCamera)
    }

    fun leaveCall() {
        try {
            rtcEngine?.stopPreview()
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Agora destroy warning: ${e.message}")
        } finally {
            rtcEngine = null
            currentVideoMode = false
            _callState.value = AgoraRtcState(
                isConfigured = isConfigured(),
                statusText = if (isConfigured()) "Agora HD প্রস্তুত ✓" else "AI Studio Secrets-এ AGORA_APP_ID দিন"
            )
        }
    }
}
