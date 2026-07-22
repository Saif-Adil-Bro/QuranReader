package com.example.data.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

/**
 * Repository to manage audio playback using ExoPlayer.
 */
class AudioRepository(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingAyahNumber = MutableStateFlow<Int?>(null)
    val currentPlayingAyahNumber: StateFlow<Int?> = _currentPlayingAyahNumber.asStateFlow()

    private val _currentPlayingWordUrl = MutableStateFlow<String?>(null)
    val currentPlayingWordUrl: StateFlow<String?> = _currentPlayingWordUrl.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    var onPlaybackEnded: (() -> Unit)? = null

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun getLocalAudioFile(url: String): File {
        val dir = File(context.filesDir, "quran_audio")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val filename = url.substringAfter("://").replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
        return File(dir, filename)
    }

    fun initializePlayer() {
        if (exoPlayer == null) {
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            val audioContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.createAttributionContext("quran_audio")
            } else {
                context
            }
            exoPlayer = ExoPlayer.Builder(audioContext).build().apply {
                setAudioAttributes(audioAttributes, true)
                setPlaybackSpeed(_playbackSpeed.value)
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        _isPlaying.value = isPlayingState
                        if (!isPlayingState) {
                            _currentPlayingWordUrl.value = null
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            _currentPlayingAyahNumber.value = null
                            _currentPlayingWordUrl.value = null
                            onPlaybackEnded?.invoke()
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        _isPlaying.value = false
                        _currentPlayingAyahNumber.value = null
                        _currentPlayingWordUrl.value = null
                        if (!com.example.util.NetworkUtils.isNetworkAvailable(context)) {
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                android.widget.Toast.makeText(
                                    context,
                                    "নেটওয়ার্ক ত্রুটি! অডিও প্লে করতে ইন্টারনেট সংযোগ প্রয়োজন।",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
            }
        }
    }

    fun playAudio(url: String, ayahNumber: Int) {
        initializePlayer()
        
        if (ayahNumber == -1) {
            _currentPlayingWordUrl.value = url
        } else {
            _currentPlayingWordUrl.value = null
        }
        
        val localFile = getLocalAudioFile(url)
        val isLocal = localFile.exists() && localFile.length() > 0

        if (!isLocal && !com.example.util.NetworkUtils.isNetworkAvailable(context)) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "আপনি অফলাইনে আছেন! অডিও প্লে করতে ইন্টারনেট সংযোগ প্রয়োজন অথবা ডাউনলোড করা থাকতে হবে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        val uri = if (isLocal) {
            android.net.Uri.fromFile(localFile)
        } else {
            android.net.Uri.parse(url)
        }

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            setPlaybackSpeed(_playbackSpeed.value)
            prepare()
            play()
            _currentPlayingAyahNumber.value = ayahNumber
        }

        // Download asynchronously in the background if not already downloaded and network is available
        if (!isLocal && com.example.util.NetworkUtils.isNetworkAvailable(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tempFile = File(localFile.parent, localFile.name + ".temp")
                    URL(url).openStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (tempFile.exists() && tempFile.length() > 0) {
                        tempFile.renameTo(localFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun pauseAudio() {
        exoPlayer?.pause()
    }

    fun resumeAudio() {
        exoPlayer?.play()
    }

    fun stopAudio() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _currentPlayingAyahNumber.value = null
        _isPlaying.value = false
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
