package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log
import com.example.model.PostAudioTrack
import com.example.model.ReactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * AppSoundManager delivers real, continuous audio directly from device speakers:
 * 1. Delegates recorded voice posts & audioUrl tracks to VoiceAudioManager (MediaPlayer)
 * 2. Smooth continuous-phase synthesizer for built-in post instrumentals (no stutter/popping)
 * 3. Instant low-latency SoundPool & AudioTrack effects for ChatFilm Messenger typing, send,
 *    delivery, incoming chime, and outgoing audio/video call dialing ringback tones.
 */
object AppSoundManager {
    private const val TAG = "AppSoundManager"
    private const val SAMPLE_RATE = 44100

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var appContext: Context? = null
    private var musicJob: Job? = null
    private var synthTrack: AudioTrack? = null
    private var mediaMirrorJob: Job? = null
    private var isUsingMediaPlayer = false

    // Low-latency SoundPool for Messenger-style ChatFilm SFX
    private var chatSoundPool: SoundPool? = null
    private var chatTypingSoundId: Int = 0
    private var chatSendSoundId: Int = 0
    private var chatDeliveredSoundId: Int = 0
    private var chatIncomingSoundId: Int = 0
    private val effectsReady = AtomicBoolean(false)
    private val lastTypingSoundMs = AtomicLong(0L)

    // Dedicated stream AudioTrack & coroutine for outgoing call dialing ringback ("বত বত")
    private var callDialJob: Job? = null
    private var callDialTrack: AudioTrack? = null
    private val isCallDialingActive = AtomicBoolean(false)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    val isAudioPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingTrackId = MutableStateFlow<String?>(null)
    val currentPlayingTrackId: StateFlow<String?> = _currentPlayingTrackId.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0)
    val playbackPositionMs: StateFlow<Int> = _playbackPositionMs.asStateFlow()

    private val _playbackDurationMs = MutableStateFlow(0)
    val playbackDurationMs: StateFlow<Int> = _playbackDurationMs.asStateFlow()

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 95)
    } catch (e: Throwable) {
        Log.e(TAG, "ToneGenerator init failed", e)
        null
    }

    fun init(context: Context) {
        val ctx = context.applicationContext
        if (appContext == null) {
            appContext = ctx
            VoiceAudioManager.init(ctx)
        }
        ensureChatEffectsInitialized(ctx)
    }

    private fun ensureChatEffectsInitialized(context: Context? = appContext) {
        val ctx = context?.applicationContext ?: return
        if (effectsReady.get() && chatSoundPool != null) return
        audioScope.launch(Dispatchers.IO) {
            if (effectsReady.get() && chatSoundPool != null) return@launch
            try {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val pool = SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(attrs)
                    .build()

                val dir = File(ctx.cacheDir, "ff_chat_sfx").apply { mkdirs() }
                val typeFile = File(dir, "ff_chat_type.wav")
                val sendFile = File(dir, "ff_chat_send.wav")
                val deliveredFile = File(dir, "ff_chat_delivered.wav")
                val incomingFile = File(dir, "ff_chat_incoming.wav")

                writeWavFile(typeFile, buildTypingBubblePcm())
                writeWavFile(sendFile, buildMessengerSendPcm())
                writeWavFile(deliveredFile, buildMessengerDeliveredPcm())
                writeWavFile(incomingFile, buildMessengerIncomingPcm())

                chatTypingSoundId = pool.load(typeFile.absolutePath, 1)
                chatSendSoundId = pool.load(sendFile.absolutePath, 1)
                chatDeliveredSoundId = pool.load(deliveredFile.absolutePath, 1)
                chatIncomingSoundId = pool.load(incomingFile.absolutePath, 1)

                chatSoundPool = pool
                effectsReady.set(true)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to initialize ChatFilm SoundPool SFX", e)
            }
        }
    }

    fun playReactionSound(reaction: ReactionType) {
        audioScope.launch {
            try {
                when (reaction) {
                    ReactionType.LOVE, ReactionType.CARE -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                    }
                    ReactionType.LIKE, ReactionType.HUNDRED -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                    }
                    ReactionType.HAHA, ReactionType.WOW -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    }
                    ReactionType.SAD, ReactionType.ANGRY, ReactionType.DISLIKE -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
                    }
                    ReactionType.OK -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 90)
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Failed playing reaction sound", e)
            }
        }
    }

    fun playReactionChime() {
        playReactionSound(ReactionType.LIKE)
    }

    fun playSocialAdChime() {
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING, 180)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed playing ad chime", e)
            }
        }
    }

    /**
     * Play or toggle playback for a PostAudioTrack.
     * If track.audioUrl is present (voice recording or real audio file/URL), uses MediaPlayer via VoiceAudioManager.
     * Otherwise, plays a seamless continuous-phase melody via AudioTrack without underrun gaps.
     */
    fun playAudio(track: PostAudioTrack, context: Context? = appContext) {
        val ctx = context?.applicationContext ?: appContext
        val realUrl = track.audioUrl?.trim().orEmpty()
        com.example.ui.components.VideoPlaybackCoordinator.pauseActiveVideo()

        if (realUrl.isNotBlank() && ctx != null) {
            stopSynthOnly()
            if (_currentPlayingTrackId.value == track.id && isUsingMediaPlayer) {
                VoiceAudioManager.togglePlay(ctx, track.id, realUrl)
                return
            }
            isUsingMediaPlayer = true
            startMediaMirroring(track.id)
            val started = VoiceAudioManager.playAudioSource(ctx, track.id, realUrl)
            if (!started) {
                isUsingMediaPlayer = false
                mediaMirrorJob?.cancel()
                _isPlaying.value = false
                _currentPlayingTrackId.value = null
            }
            return
        }

        if (_currentPlayingTrackId.value == track.id && _isPlaying.value) {
            pauseAudio()
            return
        }

        stopAudio()
        isUsingMediaPlayer = false
        _currentPlayingTrackId.value = track.id
        _isPlaying.value = true
        _playbackProgress.value = 0f
        _playbackPositionMs.value = 0
        _playbackDurationMs.value = 30000

        musicJob = audioScope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)
                val bufferSize = (minBufferSize * 4).coerceAtLeast(SAMPLE_RATE / 2)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val trackAudio = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                synthTrack = trackAudio
                trackAudio.setVolume(1.0f)
                trackAudio.play()

                val melodyFrequencies = getMelodyForCategory(track.category)
                var noteIndex = 0
                val noteDurationMs = 340
                var elapsedMs = 0
                val totalLoopMs = 30000
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0

                while (isActive && _isPlaying.value) {
                    val freq = melodyFrequencies[noteIndex % melodyFrequencies.size]
                    val result = generateContinuousNote(
                        freq = freq,
                        durationMs = noteDurationMs,
                        sampleRate = SAMPLE_RATE,
                        startPhase1 = phase1,
                        startPhase2 = phase2,
                        startPhase3 = phase3
                    )
                    phase1 = result.endPhase1
                    phase2 = result.endPhase2
                    phase3 = result.endPhase3

                    // WRITE_BLOCKING paces playback smoothly at hardware audio rate — no delay() gaps!
                    val written = trackAudio.write(result.samples, 0, result.samples.size, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) break

                    elapsedMs += noteDurationMs
                    val posMs = elapsedMs % totalLoopMs
                    _playbackPositionMs.value = posMs
                    _playbackProgress.value = posMs.toFloat() / totalLoopMs.toFloat()
                    noteIndex++
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error in music synthesis", e)
            } finally {
                cleanUpAudioTrack()
            }
        }
    }

    private fun startMediaMirroring(expectedTrackId: String) {
        mediaMirrorJob?.cancel()
        mediaMirrorJob = audioScope.launch {
            launch {
                VoiceAudioManager.playingItemId.collectLatest { id ->
                    if (isUsingMediaPlayer) {
                        _currentPlayingTrackId.value = if (id == expectedTrackId) id else id
                    }
                }
            }
            launch {
                VoiceAudioManager.isPlaying.collectLatest { playing ->
                    if (isUsingMediaPlayer) {
                        _isPlaying.value = playing
                    }
                }
            }
            launch {
                VoiceAudioManager.playbackProgress.collectLatest { progress ->
                    if (isUsingMediaPlayer) {
                        _playbackProgress.value = progress
                    }
                }
            }
            launch {
                VoiceAudioManager.playbackPositionMs.collectLatest { pos ->
                    if (isUsingMediaPlayer) {
                        _playbackPositionMs.value = pos
                    }
                }
            }
            launch {
                VoiceAudioManager.playbackDurationMs.collectLatest { dur ->
                    if (isUsingMediaPlayer) {
                        _playbackDurationMs.value = dur
                    }
                }
            }
        }
    }

    fun pauseAudio() {
        if (isUsingMediaPlayer) {
            VoiceAudioManager.pausePlayback()
            return
        }
        _isPlaying.value = false
        musicJob?.cancel()
        musicJob = null
        cleanUpAudioTrack()
    }

    fun stopSynthOnly() {
        musicJob?.cancel()
        musicJob = null
        cleanUpAudioTrack()
        if (!isUsingMediaPlayer) {
            _isPlaying.value = false
            _currentPlayingTrackId.value = null
            _playbackProgress.value = 0f
            _playbackPositionMs.value = 0
            _playbackDurationMs.value = 0
        }
    }

    fun stopAudio() {
        if (isUsingMediaPlayer) {
            isUsingMediaPlayer = false
            mediaMirrorJob?.cancel()
            mediaMirrorJob = null
            VoiceAudioManager.stopPlayback()
        }
        _isPlaying.value = false
        _currentPlayingTrackId.value = null
        _playbackProgress.value = 0f
        _playbackPositionMs.value = 0
        _playbackDurationMs.value = 0
        musicJob?.cancel()
        musicJob = null
        cleanUpAudioTrack()
    }

    private fun cleanUpAudioTrack() {
        try {
            synthTrack?.stop()
            synthTrack?.release()
        } catch (_: Throwable) {}
        synthTrack = null
    }

    private fun getMelodyForCategory(category: String): List<Double> {
        return when {
            category.contains("জন্মদিন") || category.contains("Birthday") || category.contains("বার্থডে") -> listOf(
                261.63, 261.63, 293.66, 261.63, 349.23, 329.63,
                261.63, 261.63, 293.66, 261.63, 392.00, 349.23,
                261.63, 261.63, 523.25, 440.00, 349.23, 329.63, 293.66,
                466.16, 466.16, 440.00, 349.23, 392.00, 349.23
            )
            category.contains("বাঁশি") || category.contains("Flute") -> listOf(
                440.00, 493.88, 523.25, 587.33, 659.25, 587.33, 523.25, 493.88,
                440.00, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 659.25
            )
            category.contains("গজল") || category.contains("Ghazal") -> listOf(
                293.66, 329.63, 349.23, 440.00, 493.88, 440.00, 349.23, 329.63,
                293.66, 261.63, 293.66, 349.23, 440.00, 523.25, 440.00, 349.23
            )
            category.contains("ডিজে") || category.contains("DJ") -> listOf(
                130.81, 146.83, 164.81, 196.00, 261.63, 196.00, 164.81, 146.83,
                130.81, 261.63, 329.63, 392.00, 261.63, 196.00
            )
            category.contains("দেশাত্মবোধক") || category.contains("বাংলা") -> listOf(
                261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 392.00, 349.23,
                329.63, 293.66, 261.63, 329.63, 392.00, 523.25
            )
            category.contains("রবীন্দ্র") -> listOf(
                329.63, 392.00, 440.00, 493.88, 523.25, 493.88, 440.00, 392.00,
                349.23, 329.63, 293.66, 329.63, 392.00
            )
            category.contains("লালন") || category.contains("ফোক") -> listOf(
                220.00, 261.63, 293.66, 329.63, 392.00, 329.63, 293.66, 261.63,
                220.00, 196.00, 220.00, 261.63
            )
            category.contains("হিন্দি") -> listOf(
                261.63, 311.13, 349.23, 392.00, 466.16, 523.25, 466.16, 392.00,
                349.23, 311.13, 261.63, 392.00
            )
            else -> listOf(
                261.63, 293.66, 329.63, 392.00, 440.00, 392.00, 329.63, 293.66
            )
        }
    }

    private class ContinuousNoteResult(
        val samples: ShortArray,
        val endPhase1: Double,
        val endPhase2: Double,
        val endPhase3: Double
    )

    private fun generateContinuousNote(
        freq: Double,
        durationMs: Int,
        sampleRate: Int,
        startPhase1: Double,
        startPhase2: Double,
        startPhase3: Double
    ): ContinuousNoteResult {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val inc1 = 2.0 * Math.PI * freq / sampleRate
        val inc2 = 2.0 * Math.PI * (freq * 2.0) / sampleRate
        val inc3 = 2.0 * Math.PI * (freq * 3.0) / sampleRate

        var p1 = startPhase1
        var p2 = startPhase2
        var p3 = startPhase3

        for (i in 0 until totalSamples) {
            val wave1 = sin(p1)
            val wave2 = 0.35 * sin(p2)
            val wave3 = 0.15 * sin(p3)
            val combined = (wave1 + wave2 + wave3) / 1.5

            buffer[i] = (combined * 20000.0).toInt().coerceIn(-32768, 32767).toShort()
            p1 = (p1 + inc1) % (2.0 * Math.PI)
            p2 = (p2 + inc2) % (2.0 * Math.PI)
            p3 = (p3 + inc3) % (2.0 * Math.PI)
        }
        return ContinuousNoteResult(buffer, p1, p2, p3)
    }

    fun playNotificationChime() {
        val pool = chatSoundPool
        val sid = chatIncomingSoundId
        if (pool != null && sid != 0) {
            try {
                pool.play(sid, 0.85f, 0.85f, 1, 0, 1.0f)
                return
            } catch (_: Throwable) {}
        }
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed playing notification chime", e)
            }
        }
    }

    /**
     * Soft warm Facebook Messenger-style typing bubble sound ("গর গর / টিক টিক").
     * Throttled so fast typing stays smooth and musical without distortion.
     */
    fun playChatTypingSound(context: Context? = appContext) {
        if (chatSoundPool == null && context != null) {
            ensureChatEffectsInitialized(context)
        }
        val now = System.currentTimeMillis()
        val prev = lastTypingSoundMs.get()
        if (now - prev < 75L) return
        if (!lastTypingSoundMs.compareAndSet(prev, now)) return

        val pool = chatSoundPool
        val sid = chatTypingSoundId
        if (pool != null && sid != 0) {
            try {
                // Slight pitch micro-variation makes typing sound organic like real Messenger bubbles
                val rate = 0.94f + ((now % 5).toFloat() * 0.03f)
                pool.play(sid, 0.78f, 0.78f, 1, 0, rate)
                return
            } catch (_: Throwable) {}
        }
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 28)
            } catch (_: Throwable) {}
        }
    }

    /**
     * Messenger-style crisp rising Send pop ("পপ").
     */
    fun playChatMessageSentSound(context: Context? = appContext) {
        if (chatSoundPool == null && context != null) {
            ensureChatEffectsInitialized(context)
        }
        val pool = chatSoundPool
        val sid = chatSendSoundId
        if (pool != null && sid != 0) {
            try {
                pool.play(sid, 0.90f, 0.90f, 1, 0, 1.0f)
                return
            } catch (_: Throwable) {}
        }
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
            } catch (_: Throwable) {}
        }
    }

    /**
     * Messenger-style bright Delivered chime ("ডিং / ✓✓").
     */
    fun playChatMessageDeliveredSound(context: Context? = appContext) {
        if (chatSoundPool == null && context != null) {
            ensureChatEffectsInitialized(context)
        }
        val pool = chatSoundPool
        val sid = chatDeliveredSoundId
        if (pool != null && sid != 0) {
            try {
                pool.play(sid, 0.72f, 0.72f, 1, 0, 1.0f)
                return
            } catch (_: Throwable) {}
        }
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 85)
            } catch (_: Throwable) {}
        }
    }

    /**
     * Messenger-style incoming reply ping.
     */
    fun playChatIncomingSound(context: Context? = appContext) {
        if (chatSoundPool == null && context != null) {
            ensureChatEffectsInitialized(context)
        }
        val pool = chatSoundPool
        val sid = chatIncomingSoundId
        if (pool != null && sid != 0) {
            try {
                pool.play(sid, 0.88f, 0.88f, 1, 0, 1.0f)
                return
            } catch (_: Throwable) {}
        }
        playNotificationChime()
    }

    /**
     * Starts Facebook Messenger-style outgoing dialing sound ("বত বত ... বত বত")
     * while an audio, video, or group call is waiting for the friend to pick up.
     */
    fun startCallDialingTone() {
        if (isCallDialingActive.getAndSet(true)) return
        callDialJob?.cancel()
        callDialJob = audioScope.launch {
            try {
                val loopSamples = buildMessengerCallDialCyclePcm()
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val format = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(attrs)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes((minBuf * 2).coerceAtLeast(16384))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                callDialTrack = track
                track.setVolume(0.85f)
                track.play()

                val chunkSamples = 2048
                while (isActive && isCallDialingActive.get()) {
                    var offset = 0
                    while (offset < loopSamples.size && isActive && isCallDialingActive.get()) {
                        val count = minOf(chunkSamples, loopSamples.size - offset)
                        val written = track.write(loopSamples, offset, count, AudioTrack.WRITE_BLOCKING)
                        if (written <= 0) break
                        offset += written
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Call dial tone error", e)
            } finally {
                releaseCallDialTrack()
                isCallDialingActive.set(false)
            }
        }
    }

    fun stopCallDialingTone() {
        isCallDialingActive.set(false)
        callDialJob?.cancel()
        callDialJob = null
        releaseCallDialTrack()
    }

    private fun releaseCallDialTrack() {
        try {
            callDialTrack?.pause()
            callDialTrack?.flush()
            callDialTrack?.stop()
            callDialTrack?.release()
        } catch (_: Throwable) {
        }
        callDialTrack = null
    }

    /**
     * Warm low-mid bubble thrum ("গর গর" typing sound): 42ms gentle double-thud pulse.
     */
    private fun buildTypingBubblePcm(): ShortArray {
        val durationMs = 42
        val total = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(total)
        var phaseLow = 0.0
        var phaseRes = 0.0
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / total
            // Warm downwards pitch-drop pop (215Hz -> 110Hz) + gentle 360Hz wooden bubble resonance
            val fLow = 215.0 - 105.0 * progress
            val fRes = 390.0 - 140.0 * progress
            phaseLow += 2.0 * PI * fLow / SAMPLE_RATE
            phaseRes += 2.0 * PI * fRes / SAMPLE_RATE

            val attack = (i / (SAMPLE_RATE * 0.004)).coerceIn(0.0, 1.0)
            val decay = exp(-progress * 4.2)
            // Soft subtle 42Hz double-rumble modulation ("গর-গর" texture)
            val rumble = 0.78 + 0.22 * sin(2.0 * PI * 42.0 * t)
            val signal = (0.72 * sin(phaseLow) + 0.28 * sin(phaseRes)) * attack * decay * rumble
            out[i] = (signal * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /**
     * Iconic Messenger Send sound ("পপ"): 75ms snappy rising bubble sweep 340Hz -> 920Hz.
     */
    private fun buildMessengerSendPcm(): ShortArray {
        val durationMs = 78
        val total = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(total)
        var phase = 0.0
        var phaseHarm = 0.0
        for (i in 0 until total) {
            val p = i.toDouble() / total
            val freq = 340.0 + 580.0 * (p * p * 0.65 + p * 0.35)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            phaseHarm += 2.0 * PI * (freq * 1.5) / SAMPLE_RATE
            val attack = (i / (SAMPLE_RATE * 0.005)).coerceIn(0.0, 1.0)
            val decay = exp(-p * 3.8)
            val signal = (0.78 * sin(phase) + 0.22 * sin(phaseHarm)) * attack * decay
            out[i] = (signal * 25000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /**
     * Iconic Messenger Delivered sound ("ডিং"): crisp warm 95ms bell chime.
     */
    private fun buildMessengerDeliveredPcm(): ShortArray {
        val durationMs = 100
        val total = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(total)
        val f1 = 880.0
        val f2 = 1318.5
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val p = i.toDouble() / total
            val attack = (i / (SAMPLE_RATE * 0.004)).coerceIn(0.0, 1.0)
            val decay = exp(-p * 4.5)
            val wave = 0.65 * sin(2.0 * PI * f1 * t) + 0.35 * sin(2.0 * PI * f2 * t)
            out[i] = (wave * attack * decay * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /**
     * Iconic Messenger Incoming ping: crisp two-note chime (E5 -> B5).
     */
    private fun buildMessengerIncomingPcm(): ShortArray {
        val note1Ms = 65
        val note2Ms = 110
        val total = (SAMPLE_RATE * (note1Ms + note2Ms)) / 1000
        val note1Samples = (SAMPLE_RATE * note1Ms) / 1000
        val out = ShortArray(total)
        for (i in 0 until total) {
            val isFirst = i < note1Samples
            val localIdx = if (isFirst) i else i - note1Samples
            val localLen = if (isFirst) note1Samples else (total - note1Samples)
            val p = localIdx.toDouble() / localLen.coerceAtLeast(1)
            val freq = if (isFirst) 659.25 else 987.77
            val t = localIdx.toDouble() / SAMPLE_RATE
            val attack = (localIdx / (SAMPLE_RATE * 0.004)).coerceIn(0.0, 1.0)
            val decay = exp(-p * if (isFirst) 3.0 else 4.2)
            val wave = 0.72 * sin(2.0 * PI * freq * t) + 0.28 * sin(2.0 * PI * freq * 2.0 * t)
            out[i] = (wave * attack * decay * 23000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /**
     * Facebook Messenger-style outgoing dialing ringback cycle ("বত বত ... বত বত"):
     * Two warm fluttered dual-chord pulses (360ms ON, 170ms OFF, 360ms ON, 1350ms pause).
     */
    private fun buildMessengerCallDialCyclePcm(): ShortArray {
        val pulseMs = 360
        val gapMs = 170
        val pauseMs = 1350
        val pulseSamples = (SAMPLE_RATE * pulseMs) / 1000
        val gapSamples = (SAMPLE_RATE * gapMs) / 1000
        val pauseSamples = (SAMPLE_RATE * pauseMs) / 1000
        val total = pulseSamples * 2 + gapSamples + pauseSamples
        val out = ShortArray(total)

        fun renderPulse(startOffset: Int) {
            val fadeSamples = (SAMPLE_RATE * 18) / 1000
            for (i in 0 until pulseSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Warm Messenger A-major tri-tone + gentle 24Hz flutter ("বত বত")
                val chord = (
                    0.50 * sin(2.0 * PI * 440.0 * t) +
                        0.32 * sin(2.0 * PI * 554.37 * t) +
                        0.18 * sin(2.0 * PI * 659.25 * t)
                    )
                val flutter = 0.76 + 0.24 * sin(2.0 * PI * 24.0 * t)
                val fadeIn = (i.toDouble() / fadeSamples).coerceIn(0.0, 1.0)
                val fadeOut = ((pulseSamples - 1 - i).toDouble() / fadeSamples).coerceIn(0.0, 1.0)
                val env = minOf(fadeIn, fadeOut)
                out[startOffset + i] = (chord * flutter * env * 20000.0).toInt().coerceIn(-32768, 32767).toShort()
            }
        }

        renderPulse(0)
        renderPulse(pulseSamples + gapSamples)
        return out
    }

    private fun writeWavFile(file: File, pcm: ShortArray) {
        val dataSize = pcm.size * 2
        val byteRate = SAMPLE_RATE * 2
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeIntLE(header, 4, 36 + dataSize)
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeIntLE(header, 16, 16)
        writeShortLE(header, 20, 1) // PCM
        writeShortLE(header, 22, 1) // Mono
        writeIntLE(header, 24, SAMPLE_RATE)
        writeIntLE(header, 28, byteRate)
        writeShortLE(header, 32, 2) // Block align
        writeShortLE(header, 34, 16) // Bits per sample
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeIntLE(header, 40, dataSize)

        val pcmBytes = ByteArray(dataSize)
        for (i in pcm.indices) {
            val v = pcm[i].toInt()
            pcmBytes[i * 2] = (v and 0xFF).toByte()
            pcmBytes[i * 2 + 1] = ((v ushr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { fos ->
            fos.write(header)
            fos.write(pcmBytes)
        }
    }

    private fun writeIntLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        buf[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        buf[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    fun triggerVibration(context: Context) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Throwable) {
        }
    }
}
