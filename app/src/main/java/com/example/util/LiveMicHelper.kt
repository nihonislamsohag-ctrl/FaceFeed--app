package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Real Android hardware microphone monitor & Speech-to-Text controller.
 * - Uses AudioRecord (when Agora SDK is not holding the channel) so real hardware mic capture,
 *   OS mic privacy indicator, and live voice-level amplitude work immediately after RECORD_AUDIO
 *   permission is granted across Calls, Hangout Room, and Ludo King.
 * - Uses Android SpeechRecognizer for real live Bengali/English voice input in Fida AI.
 */
object LiveMicHelper {
    private const val TAG = "LiveMicHelper"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var audioRecord: AudioRecord? = null
    private var monitorJob: Job? = null
    private var activeOwnerTag: String? = null

    private val _isMicActive = MutableStateFlow(false)
    val isMicActive: StateFlow<Boolean> = _isMicActive.asStateFlow()

    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    fun hasMicPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun startMonitoring(context: Context, ownerTag: String): Boolean {
        if (!hasMicPermission(context)) {
            stopMonitoring()
            return false
        }
        if (_isMicActive.value && activeOwnerTag == ownerTag && monitorJob?.isActive == true) {
            return true
        }
        stopMonitoring()

        val sampleRate = 16000
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT
        )

        var startedRecord: AudioRecord? = null
        for (source in sources) {
            try {
                val rec = AudioRecord(
                    source,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf
                )
                if (rec.state == AudioRecord.STATE_INITIALIZED) {
                    rec.startRecording()
                    if (rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        startedRecord = rec
                        break
                    } else {
                        rec.release()
                    }
                } else {
                    rec.release()
                }
            } catch (e: Throwable) {
                Log.w(TAG, "AudioRecord init failed for source=$source: ${e.message}")
            }
        }

        if (startedRecord == null) {
            _isMicActive.value = false
            _micLevel.value = 0f
            return false
        }

        audioRecord = startedRecord
        activeOwnerTag = ownerTag
        _isMicActive.value = true

        monitorJob = scope.launch {
            val buffer = ShortArray(minBuf / 2)
            try {
                while (isActive && _isMicActive.value) {
                    val rec = audioRecord ?: break
                    val read = rec.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sumSquares = 0.0
                        for (i in 0 until read) {
                            val s = buffer[i].toDouble()
                            sumSquares += s * s
                        }
                        val rms = sqrt(sumSquares / read)
                        val normalized = (rms / 6000.0).toFloat().coerceIn(0.05f, 1f)
                        _micLevel.value = normalized
                    }
                }
            } catch (e: Throwable) {
                Log.w(TAG, "AudioRecord read loop stopped: ${e.message}")
            }
        }
        return true
    }

    @Synchronized
    fun stopMonitoring(ownerTag: String? = null) {
        if (ownerTag != null && activeOwnerTag != null && activeOwnerTag != ownerTag) {
            return
        }
        monitorJob?.cancel()
        monitorJob = null
        activeOwnerTag = null
        _isMicActive.value = false
        _micLevel.value = 0f
        val rec = audioRecord
        audioRecord = null
        if (rec != null) {
            try {
                if (rec.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    rec.stop()
                }
            } catch (_: Throwable) {
            }
            try {
                rec.release()
            } catch (_: Throwable) {
            }
        }
    }
}

class FidaSpeechSession(
    private val context: Context,
    private val onPartialOrFinalText: (String) -> Unit,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onErrorMessage: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null

    fun start(): Boolean {
        if (!LiveMicHelper.hasMicPermission(context)) {
            onErrorMessage("ভয়েস টাইপিংয়ের জন্য মাইক্রোফোন অনুমতি দিন")
            return false
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            // Fallback to live hardware mic monitor so mic still activates cleanly
            LiveMicHelper.startMonitoring(context, "fida_ai_speech_fallback")
            onListeningChanged(true)
            onErrorMessage("ডিভাইসে Google ভয়েস সার্ভিস পাওয়া যায়নি, অনুগ্রহ করে লিখে প্রশ্ন করুন")
            return false
        }

        stop()
        return try {
            val sr = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningChanged(true)
                }

                override fun onBeginningOfSpeech() {
                    onListeningChanged(true)
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    onListeningChanged(false)
                }

                override fun onError(error: Int) {
                    onListeningChanged(false)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন অনুমতি পাওয়া যায়নি"
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কথা স্পষ্ট শোনা যায়নি, আবার চেষ্টা করুন"
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ভয়েস চিনতে ইন্টারনেট সংযোগ প্রয়োজন"
                        else -> "ভয়েস ইনপুট সম্পন্ন হয়েছে"
                    }
                    if (error != SpeechRecognizer.ERROR_CLIENT) {
                        onErrorMessage(msg)
                    }
                }

                override fun onResults(results: Bundle?) {
                    onListeningChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim()
                    if (!spoken.isNullOrBlank()) {
                        onPartialOrFinalText(spoken)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim()
                    if (!spoken.isNullOrBlank()) {
                        onPartialOrFinalText(spoken)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            recognizer = sr
            sr.startListening(intent)
            onListeningChanged(true)
            true
        } catch (e: Throwable) {
            Log.w("FidaSpeechSession", "SpeechRecognizer start failed: ${e.message}")
            onListeningChanged(false)
            onErrorMessage("ভয়েস ইনপুট চালু করা যায়নি")
            false
        }
    }

    fun stop() {
        LiveMicHelper.stopMonitoring("fida_ai_speech_fallback")
        try {
            recognizer?.stopListening()
        } catch (_: Throwable) {
        }
        try {
            recognizer?.destroy()
        } catch (_: Throwable) {
        }
        recognizer = null
        onListeningChanged(false)
    }
}
