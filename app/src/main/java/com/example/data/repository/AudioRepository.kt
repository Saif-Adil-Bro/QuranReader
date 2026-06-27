package com.example.data.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository to manage audio playback using ExoPlayer.
 */
class AudioRepository(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingAyahNumber = MutableStateFlow<Int?>(null)
    val currentPlayingAyahNumber: StateFlow<Int?> = _currentPlayingAyahNumber.asStateFlow()

    fun initializePlayer() {
        if (exoPlayer == null) {
            val attributionContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    context.createAttributionContext("audio_playback")
                } catch (e: Exception) {
                    context
                }
            } else {
                context
            }
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            exoPlayer = ExoPlayer.Builder(attributionContext).build().apply {
                setAudioAttributes(audioAttributes, true)
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        _isPlaying.value = isPlayingState
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            _currentPlayingAyahNumber.value = null
                        }
                    }
                })
            }
        }
    }

    fun playAudio(url: String, ayahNumber: Int) {
        initializePlayer()
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
            _currentPlayingAyahNumber.value = ayahNumber
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
