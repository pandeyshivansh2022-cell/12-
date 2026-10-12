package com.example.data.remote

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class AudioPlayerManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentlyPlayingMessageId = MutableStateFlow<String?>(null)
    val currentlyPlayingMessageId: StateFlow<String?> = _currentlyPlayingMessageId.asStateFlow()

    init {
        androidTts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            androidTts?.language = Locale.US
            isTtsInitialized = true
        }
    }

    /**
     * Play base64 audio (MP3/WAV or Raw PCM 24kHz)
     */
    suspend fun playAudio(messageId: String, base64Data: String) = withContext(Dispatchers.IO) {
        stopAudio()
        try {
            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
            _currentlyPlayingMessageId.value = messageId
            _isPlaying.value = true

            // Try standard media player with temp file
            val tempFile = File.createTempFile("gemini_audio_", ".mp3", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    setOnCompletionListener {
                        _isPlaying.value = false
                        _currentlyPlayingMessageId.value = null
                        tempFile.delete()
                    }
                    start()
                }
            } catch (e: Exception) {
                // If headerless PCM (typical from Gemini 24kHz raw PCM), play via AudioTrack
                playRawPcm(audioBytes)
            }
        } catch (e: Exception) {
            _isPlaying.value = false
            _currentlyPlayingMessageId.value = null
        }
    }

    private fun playRawPcm(pcmBytes: ByteArray) {
        val sampleRate = 24000
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferSize, pcmBytes.size))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.apply {
            write(pcmBytes, 0, pcmBytes.size)
            setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(track: AudioTrack?) {}
                override fun onMarkerReached(track: AudioTrack?) {
                    _isPlaying.value = false
                    _currentlyPlayingMessageId.value = null
                }
            })
            notificationMarkerPosition = pcmBytes.size / 2
            play()
        }
    }

    /**
     * Fallback to Android TTS if offline or direct audio not generated
     */
    fun speakWithAndroidTts(messageId: String, text: String) {
        stopAudio()
        if (isTtsInitialized && androidTts != null) {
            _currentlyPlayingMessageId.value = messageId
            _isPlaying.value = true
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, messageId)
        }
    }

    fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null

            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null

            if (androidTts?.isSpeaking == true) {
                androidTts?.stop()
            }
        } catch (e: Exception) {
            // Ignore cleanup exceptions
        } finally {
            _isPlaying.value = false
            _currentlyPlayingMessageId.value = null
        }
    }

    fun release() {
        stopAudio()
        androidTts?.shutdown()
        androidTts = null
    }
}
