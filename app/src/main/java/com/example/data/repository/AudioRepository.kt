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

    var onPlaybackEnded: (() -> Unit)? = null

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

            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setAudioAttributes(audioAttributes, true)
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        _isPlaying.value = isPlayingState
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                            _currentPlayingAyahNumber.value = null
                            onPlaybackEnded?.invoke()
                        }
                    }
                })
            }
        }
    }

    fun playAudio(url: String, ayahNumber: Int) {
        initializePlayer()
        
        val localFile = getLocalAudioFile(url)
        val uri = if (localFile.exists() && localFile.length() > 0) {
            android.net.Uri.fromFile(localFile)
        } else {
            android.net.Uri.parse(url)
        }

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
            _currentPlayingAyahNumber.value = ayahNumber
        }

        // Download asynchronously in the background if not already downloaded
        if (!localFile.exists() || localFile.length() == 0L) {
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
