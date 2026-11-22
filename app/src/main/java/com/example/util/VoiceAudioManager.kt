package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class VoiceRecordingResult(
    val filePath: String,
    val fileUri: String,
    val durationSeconds: Int
)

/**
 * Real Android MediaRecorder + MediaPlayer engine for FotoFilm.
 * Handles uninterrupted AAC (.m4a) voice recording in internal app storage
 * and continuous, glitch-free playback for both chat voice messages and voice posts.
 */
object VoiceAudioManager {
    private const val TAG = "VoiceAudioManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var appContext: Context? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingTickerJob: Job? = null
    private var recordingStartWallMs: Long = 0L
    private var accumulatedRecordedMs: Long = 0L

    private var mediaPlayer: MediaPlayer? = null
    private var playbackProgressJob: Job? = null
    private var activeAudioFocusRequest: AudioFocusRequest? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isRecordingPaused = MutableStateFlow(false)
    val isRecordingPaused: StateFlow<Boolean> = _isRecordingPaused.asStateFlow()

    private val _activeRecordingTarget = MutableStateFlow<String?>(null)
    val activeRecordingTarget: StateFlow<String?> = _activeRecordingTarget.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _recordingAmplitude = MutableStateFlow(0f)
    val recordingAmplitude: StateFlow<Float> = _recordingAmplitude.asStateFlow()

    private val _playingItemId = MutableStateFlow<String?>(null)
    val playingItemId: StateFlow<String?> = _playingItemId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0)
    val playbackPositionMs: StateFlow<Int> = _playbackPositionMs.asStateFlow()

    private val _playbackDurationMs = MutableStateFlow(0)
    val playbackDurationMs: StateFlow<Int> = _playbackDurationMs.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun clearError() {
        _lastError.value = null
    }

    private fun getVoiceDir(context: Context): File {
        val dir = File(context.applicationContext.filesDir, "voice_notes")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    @Synchronized
    fun startRecording(context: Context, targetId: String): Boolean {
        init(context)
        stopPlayback()
        cancelRecording()

        val outFile = File(
            getVoiceDir(context),
            "voice_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.m4a"
        )

        val audioSources = listOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.DEFAULT
        )

        var started = false
        for (source in audioSources) {
            val recorder = createRecorderInstance(context.applicationContext)
            try {
                recorder.setAudioSource(source)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(128_000)
                recorder.setAudioSamplingRate(44_100)
                recorder.setAudioChannels(1)
                recorder.setOutputFile(outFile.absolutePath)
                recorder.prepare()
                recorder.start()
                mediaRecorder = recorder
                started = true
                break
            } catch (e: Throwable) {
                Log.w(TAG, "MediaRecorder failed for source=$source: ${e.message}")
                try {
                    recorder.reset()
                    recorder.release()
                } catch (_: Throwable) {
                }
            }
        }

        if (!started) {
            try {
                if (outFile.exists()) outFile.delete()
            } catch (_: Throwable) {
            }
            _lastError.value = "মাইক্রোফোন চালু করা যায়নি। অনুগ্রহ করে রেকর্ড পারমিশন দিন।"
            return false
        }

        currentOutputFile = outFile
        _activeRecordingTarget.value = targetId
        _isRecording.value = true
        _isRecordingPaused.value = false
        _recordingSeconds.value = 0
        _recordingAmplitude.value = 0f
        accumulatedRecordedMs = 0L
        recordingStartWallMs = System.currentTimeMillis()
        requestAudioFocus(context)

        recordingTickerJob?.cancel()
        recordingTickerJob = scope.launch {
            while (isActive && _isRecording.value) {
                if (!_isRecordingPaused.value) {
                    val elapsedMs = accumulatedRecordedMs + (System.currentTimeMillis() - recordingStartWallMs)
                    _recordingSeconds.value = (elapsedMs / 1000L).toInt()
                    val rawAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (_: Throwable) {
                        0
                    }
                    _recordingAmplitude.value = (rawAmp / 16000f).coerceIn(0.08f, 1f)
                } else {
                    _recordingAmplitude.value = 0.05f
                }
                delay(120)
            }
        }
        return true
    }

    fun togglePauseRecording() {
        val recorder = mediaRecorder ?: return
        if (!_isRecording.value) return
        try {
            if (_isRecordingPaused.value) {
                recorder.resume()
                recordingStartWallMs = System.currentTimeMillis()
                _isRecordingPaused.value = false
            } else {
                accumulatedRecordedMs += (System.currentTimeMillis() - recordingStartWallMs).coerceAtLeast(0L)
                recorder.pause()
                _isRecordingPaused.value = true
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Pause/resume recording error: ${e.message}")
        }
    }

    @Synchronized
    fun stopRecording(): VoiceRecordingResult? {
        recordingTickerJob?.cancel()
        recordingTickerJob = null

        val recorder = mediaRecorder
        val file = currentOutputFile
        val totalMs = if (_isRecordingPaused.value) {
            accumulatedRecordedMs
        } else {
            accumulatedRecordedMs + (System.currentTimeMillis() - recordingStartWallMs).coerceAtLeast(0L)
        }

        mediaRecorder = null
        currentOutputFile = null
        _isRecording.value = false
        _isRecordingPaused.value = false
        _activeRecordingTarget.value = null
        _recordingAmplitude.value = 0f

        var stopSucceeded = false
        if (recorder != null) {
            try {
                recorder.stop()
                stopSucceeded = true
            } catch (e: Throwable) {
                Log.w(TAG, "MediaRecorder stop ignored/short recording: ${e.message}")
            }
            try {
                recorder.reset()
                recorder.release()
            } catch (_: Throwable) {
            }
        }
        abandonAudioFocus()

        val durationSec = ((totalMs / 1000L).toInt()).coerceAtLeast(1)
        _recordingSeconds.value = 0

        return if (stopSucceeded && file != null && file.exists() && file.length() > 256L) {
            VoiceRecordingResult(
                filePath = file.absolutePath,
                fileUri = Uri.fromFile(file).toString(),
                durationSeconds = durationSec
            )
        } else {
            try {
                if (file != null && file.exists()) file.delete()
            } catch (_: Throwable) {
            }
            null
        }
    }

    @Synchronized
    fun cancelRecording() {
        recordingTickerJob?.cancel()
        recordingTickerJob = null
        val recorder = mediaRecorder
        val file = currentOutputFile
        mediaRecorder = null
        currentOutputFile = null
        _isRecording.value = false
        _isRecordingPaused.value = false
        _activeRecordingTarget.value = null
        _recordingSeconds.value = 0
        _recordingAmplitude.value = 0f
        if (recorder != null) {
            try {
                recorder.stop()
            } catch (_: Throwable) {
            }
            try {
                recorder.reset()
                recorder.release()
            } catch (_: Throwable) {
            }
        }
        try {
            if (file != null && file.exists()) file.delete()
        } catch (_: Throwable) {
        }
        abandonAudioFocus()
    }

    fun togglePlay(context: Context, itemId: String, uriOrPath: String): Boolean {
        init(context)
        if (_playingItemId.value == itemId && mediaPlayer != null) {
            if (_isPlaying.value) {
                pausePlayback()
            } else {
                resumePlayback()
            }
            return true
        }
        return playAudioSource(context, itemId, uriOrPath)
    }

    fun playAudioSource(context: Context, itemId: String, uriOrPath: String): Boolean {
        init(context)
        if (uriOrPath.isBlank()) return false

        stopPlayback()
        AppSoundManager.stopSynthOnly()
        com.example.ui.components.VideoPlaybackCoordinator.pauseActiveVideo()

        val ctx = context.applicationContext
        requestAudioFocus(ctx)

        val player = MediaPlayer()
        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )

            when {
                uriOrPath.startsWith("/") -> {
                    val f = File(uriOrPath)
                    if (!f.exists() || f.length() == 0L) {
                        player.release()
                        _lastError.value = "ভয়েস ফাইলটি খুঁজে পাওয়া যায়নি"
                        return false
                    }
                    player.setDataSource(f.absolutePath)
                }
                uriOrPath.startsWith("file://") -> {
                    val path = Uri.parse(uriOrPath).path
                    val f = if (!path.isNullOrBlank()) File(path) else null
                    if (f != null && f.exists() && f.length() > 0L) {
                        player.setDataSource(f.absolutePath)
                    } else {
                        player.setDataSource(ctx, Uri.parse(uriOrPath))
                    }
                }
                else -> {
                    player.setDataSource(ctx, Uri.parse(uriOrPath))
                }
            }

            mediaPlayer = player
            _playingItemId.value = itemId
            _isPlaying.value = true
            _playbackProgress.value = 0f
            _playbackPositionMs.value = 0
            _playbackDurationMs.value = 0

            player.setOnPreparedListener { mp ->
                if (mediaPlayer !== mp) return@setOnPreparedListener
                mp.setVolume(1.0f, 1.0f)
                val dur = mp.duration.coerceAtLeast(1)
                _playbackDurationMs.value = dur
                mp.start()
                _isPlaying.value = true
                startProgressLoop()
            }

            player.setOnCompletionListener { mp ->
                if (mediaPlayer === mp) {
                    stopPlayback()
                }
            }

            player.setOnErrorListener { mp, what, extra ->
                Log.w(TAG, "MediaPlayer error what=$what extra=$extra uri=$uriOrPath")
                if (mediaPlayer === mp) {
                    stopPlayback()
                    _lastError.value = "অডিও প্লে করতে সমস্যা হয়েছে"
                }
                true
            }

            player.prepareAsync()
            return true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start MediaPlayer for $uriOrPath", e)
            try {
                player.release()
            } catch (_: Throwable) {
            }
            mediaPlayer = null
            _playingItemId.value = null
            _isPlaying.value = false
            abandonAudioFocus()
            return false
        }
    }

    fun pausePlayback() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
            }
            _isPlaying.value = false
            playbackProgressJob?.cancel()
            playbackProgressJob = null
        } catch (e: Throwable) {
            Log.w(TAG, "Pause playback error: ${e.message}")
        }
    }

    fun resumePlayback() {
        val mp = mediaPlayer ?: return
        try {
            appContext?.let { requestAudioFocus(it) }
            mp.start()
            _isPlaying.value = true
            startProgressLoop()
        } catch (e: Throwable) {
            Log.w(TAG, "Resume playback error: ${e.message}")
            stopPlayback()
        }
    }

    @Synchronized
    fun stopPlayback() {
        playbackProgressJob?.cancel()
        playbackProgressJob = null
        val mp = mediaPlayer
        mediaPlayer = null
        _isPlaying.value = false
        _playingItemId.value = null
        _playbackProgress.value = 0f
        _playbackPositionMs.value = 0
        _playbackDurationMs.value = 0
        if (mp != null) {
            try {
                if (mp.isPlaying) mp.stop()
            } catch (_: Throwable) {
            }
            try {
                mp.reset()
                mp.release()
            } catch (_: Throwable) {
            }
        }
        abandonAudioFocus()
    }

    private fun startProgressLoop() {
        playbackProgressJob?.cancel()
        playbackProgressJob = scope.launch {
            while (isActive && _isPlaying.value && mediaPlayer != null) {
                val mp = mediaPlayer ?: break
                try {
                    val dur = mp.duration.coerceAtLeast(1)
                    val pos = mp.currentPosition.coerceIn(0, dur)
                    _playbackDurationMs.value = dur
                    _playbackPositionMs.value = pos
                    _playbackProgress.value = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                } catch (_: Throwable) {
                    break
                }
                delay(80)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorderInstance(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .build()
                activeAudioFocusRequest = req
                am.requestAudioFocus(req)
            } else {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
        } catch (_: Throwable) {
        }
    }

    @Suppress("DEPRECATION")
    private fun abandonAudioFocus() {
        try {
            val ctx = appContext ?: return
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activeAudioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                activeAudioFocusRequest = null
            } else {
                am.abandonAudioFocus(null)
            }
        } catch (_: Throwable) {
        }
    }
}
