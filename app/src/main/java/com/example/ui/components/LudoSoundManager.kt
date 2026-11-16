package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time Sound Synthesis & Audio Engine for Ludo King
 * Implements:
 * 1. Dice Roll Pounding Sound ("পাউন্ড অইবে") with tactile haptic rumble
 * 2. Token Cut / Kill Sound ("ফস করে শব্দ" - whoosh strike)
 * 3. Token Home Victory Sound ("গুটি পাকলে শব্দ" - victory fanfare)
 * 4. 5 Distinct Emoji Sounds (😂 Laugh, 🔥 Fire, 😡 Angry, 🎉 Party, 👏 Clap)
 */
object LudoSoundManager {
    private const val TAG = "LudoSoundManager"
    private const val SAMPLE_RATE = 22050

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e(TAG, "ToneGenerator init failed", e)
        }
    }

    /**
     * Synthesize and play raw PCM 16-bit audio buffer directly via AudioTrack
     */
    private fun playPcm(buffer: ShortArray) {
        audioScope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Let track play out before release
                val durationMs = (buffer.size * 1000L) / SAMPLE_RATE + 60
                kotlinx.coroutines.delay(durationMs)
                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing PCM audio", e)
            }
        }
    }

    /**
     * Vibrate phone for tactile pound sensation
     */
    fun vibratePound(context: Context, durationMs: Long = 80) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }

    /**
     * 1. 🎲 Dice Roll Pounding Sound ("৬ কক্কার উপরে চাপ দিলে পাউন্ড অইবে")
     * A deep percussive rhythmic rattle-pound (heavy thumps 130Hz -> 85Hz)
     */
    fun playDiceRollSound(context: Context? = null) {
        context?.let { vibratePound(it, 110) }

        val totalSamples = (SAMPLE_RATE * 0.35).toInt()
        val buffer = ShortArray(totalSamples)

        val hits = listOf(0, (SAMPLE_RATE * 0.08).toInt(), (SAMPLE_RATE * 0.17).toInt(), (SAMPLE_RATE * 0.25).toInt())
        for (i in 0 until totalSamples) {
            var sample = 0.0
            for (hit in hits) {
                val t = i - hit
                if (t in 0..(SAMPLE_RATE * 0.12).toInt()) {
                    val progress = t.toDouble() / (SAMPLE_RATE * 0.12)
                    val freq = 140.0 - progress * 65.0
                    val envelope = (1.0 - progress) * (1.0 - progress)
                    // Low thud punch + tiny rattle
                    val rattle = (Random.nextDouble() * 2.0 - 1.0) * 0.18 * envelope
                    sample += (sin(2.0 * Math.PI * freq * (t.toDouble() / SAMPLE_RATE)) * envelope + rattle)
                }
            }
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * 2. 💥 Token Cut / Kill Sound ("গুটি কাটলে ফস করে সদ্বো হবে")
     * High velocity "Fosshh / Whoosh" strike with descending pitch and white noise decay
     */
    fun playTokenCutSound(context: Context? = null) {
        context?.let { vibratePound(it, 140) }

        val totalSamples = (SAMPLE_RATE * 0.32).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            // "Fosh" shape: quick rising snap then rushing swoosh decay
            val envelope = if (progress < 0.12) {
                progress / 0.12
            } else {
                val p = (progress - 0.12) / 0.88
                Math.exp(-p * 4.2)
            }
            // Noise component (creates the "ফস / shhh" sound)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.75
            // High tonal whoosh (starts at 1800Hz falls to 400Hz)
            val freq = 1800.0 * (1.0 - progress * 0.75)
            val tone = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)) * 0.35

            val sample = (noise + tone) * envelope
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * 3. 👑 Token Reached Home Victory Sound ("গুটি পাকলে সদ্বো হবে")
     * Ascending 4-chord victory fanfare chime (C5 -> E5 -> G5 -> C6)
     */
    fun playTokenHomeSound(context: Context? = null) {
        context?.let { vibratePound(it, 70) }

        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val stepSamples = (SAMPLE_RATE * 0.11).toInt()
        val totalSamples = stepSamples * notes.size + (SAMPLE_RATE * 0.25).toInt()
        val buffer = ShortArray(totalSamples)

        for (idx in notes.indices) {
            val freq = notes[idx]
            val start = idx * stepSamples
            val noteDuration = if (idx == notes.lastIndex) stepSamples * 3 else stepSamples * 2
            val end = (start + noteDuration).coerceAtMost(totalSamples)

            for (i in start until end) {
                val t = (i - start).toDouble() / SAMPLE_RATE
                val noteProgress = (i - start).toDouble() / noteDuration
                val env = Math.exp(-noteProgress * 3.5)
                // Fundamental + rich chime harmonic
                val s = (sin(2.0 * Math.PI * freq * t) * 0.65 + sin(2.0 * Math.PI * freq * 2.0 * t) * 0.35) * env
                val current = buffer[i].toDouble() / 32767.0
                val mixed = (current + s * 0.8).coerceIn(-1.0, 1.0)
                buffer[i] = (mixed * 32000).toInt().toShort()
            }
        }
        playPcm(buffer)
    }

    /**
     * 4. 🎭 5 Distinct Emoji Sounds:
     * 😂 Laugh (হা হা)
     * 🔥 Fire (আগুন)
     * 😡 Angry (রাগ)
     * 🎉 Party (পার্টি)
     * 👏 Clap (তালি)
     */
    fun playEmojiSound(emoji: String, context: Context? = null) {
        when (emoji) {
            "😂" -> playLaughSound()
            "🔥" -> playFireSound()
            "😡" -> playAngrySound()
            "🎉" -> playPartySound()
            "👏" -> playClapSound()
            else -> playDiceRollSound(context)
        }
    }

    /**
     * 😂 Laugh Sound: Rapid cheerful "Ha-Ha-Ha!" alternating trill (720Hz <-> 940Hz)
     */
    private fun playLaughSound() {
        val totalSamples = (SAMPLE_RATE * 0.40).toInt()
        val buffer = ShortArray(totalSamples)
        val haLength = (SAMPLE_RATE * 0.08).toInt()

        for (i in 0 until totalSamples) {
            val haIndex = i / haLength
            val tWithin = (i % haLength).toDouble() / haLength
            val env = sin(tWithin * Math.PI) // bell curve per "ha"
            val freq = if (haIndex % 2 == 0) 720.0 else 940.0
            val s = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)) * env
            buffer[i] = (s.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * 🔥 Fire Sound: Crackling combustion sweep
     */
    private fun playFireSound() {
        val totalSamples = (SAMPLE_RATE * 0.32).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val env = (1.0 - progress) * (1.0 - progress)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.8
            val freq = 240.0 + progress * 400.0
            val hum = sin(2.0 * Math.PI * freq * (i.toDouble() / SAMPLE_RATE)) * 0.3
            val s = (noise + hum) * env
            buffer[i] = (s.coerceIn(-1.0, 1.0) * 27000).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * 😡 Angry Sound: Low grumbling rumble buzzer
     */
    private fun playAngrySound() {
        val totalSamples = (SAMPLE_RATE * 0.35).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / totalSamples
            val env = 1.0 - progress * 0.4
            // Low sawtooth/square-like buzz at 115Hz
            val base = sin(2.0 * Math.PI * 115.0 * t)
            val harmonic = sin(2.0 * Math.PI * 230.0 * t) * 0.6
            val grit = (Random.nextDouble() * 2.0 - 1.0) * 0.2
            val s = (base + harmonic + grit) * env
            buffer[i] = (s.coerceIn(-1.0, 1.0) * 29000).toInt().toShort()
        }
        playPcm(buffer)
    }

    /**
     * 🎉 Party Sound: Bright celebratory trumpet fanfare
     */
    private fun playPartySound() {
        val notes = listOf(392.0, 523.25, 659.25, 783.99) // G4, C5, E5, G5
        val step = (SAMPLE_RATE * 0.08).toInt()
        val totalSamples = step * 4 + (SAMPLE_RATE * 0.20).toInt()
        val buffer = ShortArray(totalSamples)

        for (n in notes.indices) {
            val freq = notes[n]
            val start = n * step
            val len = if (n == 3) step * 3 else step * 2
            val end = (start + len).coerceAtMost(totalSamples)
            for (i in start until end) {
                val t = (i - start).toDouble() / SAMPLE_RATE
                val p = (i - start).toDouble() / len
                val env = Math.exp(-p * 3.0)
                val s = sin(2.0 * Math.PI * freq * t) * env
                val cur = buffer[i].toDouble() / 32767.0
                buffer[i] = ((cur + s * 0.7).coerceIn(-1.0, 1.0) * 31000).toInt().toShort()
            }
        }
        playPcm(buffer)
    }

    /**
     * 👏 Clap Sound: Two crisp sharp applause hand claps
     */
    private fun playClapSound() {
        val totalSamples = (SAMPLE_RATE * 0.30).toInt()
        val buffer = ShortArray(totalSamples)
        val clapHits = listOf(0, (SAMPLE_RATE * 0.12).toInt())

        for (i in 0 until totalSamples) {
            var s = 0.0
            for (hit in clapHits) {
                val t = i - hit
                if (t in 0..(SAMPLE_RATE * 0.08).toInt()) {
                    val p = t.toDouble() / (SAMPLE_RATE * 0.08)
                    val env = Math.exp(-p * 8.0)
                    val noise = (Random.nextDouble() * 2.0 - 1.0) * env
                    s += noise
                }
            }
            buffer[i] = (s.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        playPcm(buffer)
    }
}
