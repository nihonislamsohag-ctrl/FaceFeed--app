package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import android.util.Log
import com.example.model.PostAudioTrack
import com.example.model.ReactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * AppSoundManager delivers real audible sound directly from the device speakers:
 * 1. Rich musical synthesizer for Post audio tracks, music, and reels with realistic melodies
 * 2. Instant responsive sound effects for Like / Live Reactions, Comments, and Social Ad chimes
 * 3. Guaranteed playback without external network failures or missing local files
 */
object AppSoundManager {
    private const val TAG = "AppSoundManager"
    private const val SAMPLE_RATE = 44100

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    private var currentAudioTrack: AudioTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    val isAudioPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingTrackId = MutableStateFlow<String?>(null)
    val currentPlayingTrackId: StateFlow<String?> = _currentPlayingTrackId.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    // ToneGenerator for instant UI interaction sounds (pop, chime, heart)
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 95)
    } catch (e: Throwable) {
        Log.e(TAG, "ToneGenerator init failed", e)
        null
    }

    /**
     * Play live reaction sound (pop, chime, harmonic tone)
     */
    fun playReactionSound(reaction: ReactionType) {
        audioScope.launch {
            try {
                when (reaction) {
                    ReactionType.LOVE, ReactionType.CARE -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
                    }
                    ReactionType.LIKE, ReactionType.HUNDRED -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                    }
                    ReactionType.HAHA, ReactionType.WOW -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
                    }
                    ReactionType.SAD, ReactionType.ANGRY, ReactionType.DISLIKE -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 110)
                    }
                    ReactionType.OK -> {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Failed playing reaction sound", e)
            }
        }
    }

    /**
     * Quick reaction chime alias
     */
    fun playReactionChime() {
        playReactionSound(ReactionType.LIKE)
    }

    /**
     * Play social advertisement chime
     */
    fun playSocialAdChime() {
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING, 200)
            } catch (e: Throwable) {
                Log.w(TAG, "Failed playing ad chime", e)
            }
        }
    }

    /**
     * Start playing music for a given post audio track
     */
    fun playAudio(track: PostAudioTrack) {
        if (_currentPlayingTrackId.value == track.id && _isPlaying.value) {
            pauseAudio()
            return
        }

        stopAudio()

        _currentPlayingTrackId.value = track.id
        _isPlaying.value = true
        _playbackProgress.value = 0f

        musicJob = audioScope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufferSize * 2).coerceAtLeast(SAMPLE_RATE / 2)

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

                currentAudioTrack = trackAudio
                trackAudio.setVolume(1.0f)
                trackAudio.play()

                // Melodic notes based on category (Birthday, Bengali Folk, Ghazal, Flute, DJ, Hindi, Patriotic, Rabindra, Sad)
                val melodyFrequencies = getMelodyForCategory(track.category)
                var noteIndex = 0
                val noteDurationMs = 280
                var elapsedMs = 0
                val totalSimulationDurationMs = 30000 // 30 seconds loop

                while (isActive && _isPlaying.value) {
                    val freq = melodyFrequencies[noteIndex % melodyFrequencies.size]
                    val samples = generateMusicalNote(freq, noteDurationMs, SAMPLE_RATE)
                    trackAudio.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)

                    elapsedMs += noteDurationMs
                    _playbackProgress.value = ((elapsedMs % totalSimulationDurationMs).toFloat() / totalSimulationDurationMs)

                    noteIndex++
                    delay(noteDurationMs.toLong())
                }

            } catch (e: Throwable) {
                Log.e(TAG, "Error in music synthesis", e)
            } finally {
                cleanUpAudioTrack()
            }
        }
    }

    fun pauseAudio() {
        _isPlaying.value = false
        musicJob?.cancel()
        musicJob = null
        cleanUpAudioTrack()
    }

    fun stopAudio() {
        _isPlaying.value = false
        _currentPlayingTrackId.value = null
        _playbackProgress.value = 0f
        musicJob?.cancel()
        musicJob = null
        cleanUpAudioTrack()
    }

    private fun cleanUpAudioTrack() {
        try {
            currentAudioTrack?.stop()
            currentAudioTrack?.release()
        } catch (ignored: Throwable) {}
        currentAudioTrack = null
    }

    private fun getMelodyForCategory(category: String): List<Double> {
        return when {
            // জন্মদিন / Birthday: Universal Happy Birthday To You melody notes
            category.contains("জন্মদিন") || category.contains("Birthday") || category.contains("বার্থডে") -> listOf(
                261.63, 261.63, 293.66, 261.63, 349.23, 329.63, // Hap-py Birth-day to you
                261.63, 261.63, 293.66, 261.63, 392.00, 349.23, // Hap-py Birth-day to you
                261.63, 261.63, 523.25, 440.00, 349.23, 329.63, 293.66, // Hap-py Birth-day dear friend
                466.16, 466.16, 440.00, 349.23, 392.00, 349.23  // Hap-py Birth-day to you
            )
            // বাঁশির সুর (Soulful Flute)
            category.contains("বাঁশি") || category.contains("Flute") -> listOf(
                440.00, 493.88, 523.25, 587.33, 659.25, 587.33, 523.25, 493.88,
                440.00, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 659.25
            )
            // গজল (Ghazal / Sufi)
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

    private fun generateMusicalNote(freq: Double, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val attack = (totalSamples * 0.15).toInt()
        val decay = (totalSamples * 0.2).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Rich harmonic synthesis (fundamental + 2nd + 3rd harmonic for rich warm instrument tone)
            val wave1 = sin(2.0 * Math.PI * freq * t)
            val wave2 = 0.4 * sin(2.0 * Math.PI * freq * 2.0 * t)
            val wave3 = 0.2 * sin(2.0 * Math.PI * freq * 3.0 * t)
            val combined = (wave1 + wave2 + wave3) / 1.6

            // ADSR Envelope
            val envelope = when {
                i < attack -> (i.toDouble() / attack)
                i > totalSamples - decay -> ((totalSamples - i).toDouble() / decay)
                else -> 0.95
            }

            buffer[i] = (combined * envelope * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }
}
