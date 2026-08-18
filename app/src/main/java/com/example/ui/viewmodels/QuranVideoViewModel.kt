package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.QuranData
import com.example.data.local.offline.OfflineQuranDatabase
import com.example.data.model.*
import com.example.util.AudioUtils
import com.example.util.QariData
import com.example.utils.QuranVideoExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class QuranVideoViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val offlineDao by lazy { OfflineQuranDatabase.getDatabase(context).offlineQuranDao() }

    // UI Config State
    private val _config = MutableStateFlow(QuranVideoConfig())
    val config: StateFlow<QuranVideoConfig> = _config.asStateFlow()

    // Current screen step (1: Content, 2: Customize, 3: Preview, 4: Export)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Audio Player State
    private var exoPlayer: ExoPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlaybackProgress = MutableStateFlow(0f)
    val currentPlaybackProgress: StateFlow<Float> = _currentPlaybackProgress.asStateFlow()

    private val _currentAyahIndex = MutableStateFlow(0)
    val currentAyahIndex: StateFlow<Int> = _currentAyahIndex.asStateFlow()

    // Export State
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportedVideoUri = MutableStateFlow<Uri?>(null)
    val exportedVideoUri: StateFlow<Uri?> = _exportedVideoUri.asStateFlow()

    private val _exportedVideoFile = MutableStateFlow<File?>(null)
    val exportedVideoFile: StateFlow<File?> = _exportedVideoFile.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError.asStateFlow()

    private var progressTrackingJob: Job? = null

    init {
        // Load initial Surah Al-Fatiha
        loadSurah(1, 1, 7)
    }

    fun setStep(step: Int) {
        _currentStep.value = step.coerceIn(1, 4)
        if (step != 3) {
            pausePreviewAudio()
        } else {
            prepareAndPlayPreviewAudio()
        }
    }

    fun loadSurah(surahNumber: Int, ayahStart: Int = 1, ayahEnd: Int = 1) {
        val totalCount = QuranData.getAyahCount(surahNumber)
        val start = ayahStart.coerceIn(1, totalCount)
        val end = ayahEnd.coerceIn(start, totalCount)
        val banglaName = QuranData.getSurahNameBangla(surahNumber)

        viewModelScope.launch {
            val dbAyahs = withContext(Dispatchers.IO) {
                try {
                    offlineDao.getAyahsBySurahRange(surahNumber, start, end)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val surahAyahs = if (dbAyahs.isNotEmpty()) {
                dbAyahs.map { entity ->
                    CombinedAyah(
                        number = entity.globalNumber,
                        numberInSurah = entity.numberInSurah,
                        page = entity.page,
                        juz = entity.juz,
                        surahNumber = surahNumber,
                        arabicText = entity.arabicText,
                        bengaliText = entity.bengaliText,
                        tafsirText = null,
                        audioUrl = null,
                        words = emptyList(),
                        textUthmaniTajweed = null
                    )
                }
            } else {
                (start..end).map { ayahNum ->
                    val globalNum = QuranData.getGlobalAyahNumber(surahNumber, ayahNum)
                    CombinedAyah(
                        number = globalNum,
                        numberInSurah = ayahNum,
                        page = 1,
                        juz = 1,
                        surahNumber = surahNumber,
                        arabicText = QuranData.getArabicAyah(surahNumber, ayahNum),
                        bengaliText = QuranData.getBanglaAyah(surahNumber, ayahNum),
                        tafsirText = null,
                        audioUrl = null,
                        words = emptyList(),
                        textUthmaniTajweed = null
                    )
                }
            }

            _config.value = _config.value.copy(
                surahNumber = surahNumber,
                surahName = banglaName,
                ayahStart = start,
                ayahEnd = end,
                selectedAyahs = surahAyahs
            )
        }
    }

    fun loadFromQuickCreate(surahNumber: Int, ayahNumber: Int, ayah: CombinedAyah, surahName: String) {
        _config.value = _config.value.copy(
            surahNumber = surahNumber,
            surahName = surahName,
            ayahStart = ayahNumber,
            ayahEnd = ayahNumber,
            selectedAyahs = listOf(ayah)
        )
        setStep(2) // Jump directly to Customize screen
    }

    fun setTemplate(template: QuranVideoTemplate) {
        _config.value = _config.value.copy(
            template = template,
            overlay = template.defaultOverlay,
            animationStyle = template.defaultAnimation
        )
    }

    fun setAspectRatio(ratio: VideoAspectRatio) {
        _config.value = _config.value.copy(aspectRatio = ratio)
    }

    fun setBackgroundPreset(presetName: String?) {
        _config.value = _config.value.copy(
            backgroundPresetName = presetName,
            customImageUri = null
        )
    }

    fun setCustomImage(uriString: String?) {
        _config.value = _config.value.copy(
            customImageUri = uriString,
            backgroundPresetName = null
        )
    }

    fun setOverlay(overlay: BackgroundOverlay) {
        _config.value = _config.value.copy(overlay = overlay)
    }

    fun setAnimationStyle(style: TextAnimationStyle) {
        _config.value = _config.value.copy(animationStyle = style)
    }

    fun toggleBanglaTranslation(show: Boolean) {
        _config.value = _config.value.copy(showBanglaTranslation = show)
    }

    fun toggleReference(show: Boolean) {
        _config.value = _config.value.copy(showReference = show)
    }

    fun toggleLogo(show: Boolean) {
        _config.value = _config.value.copy(showLogo = show)
    }

    fun toggleCredit(show: Boolean) {
        _config.value = _config.value.copy(showCredit = show)
    }

    fun setCreditText(text: String) {
        _config.value = _config.value.copy(creditText = text)
    }

    fun setLogoText(text: String) {
        _config.value = _config.value.copy(logoText = text)
    }

    fun toggleWaqfSigns(show: Boolean) {
        _config.value = _config.value.copy(showWaqfSigns = show)
    }

    fun setArabicFontSize(size: Float) {
        _config.value = _config.value.copy(arabicFontSize = size)
    }

    fun setTranslationFontSize(size: Float) {
        _config.value = _config.value.copy(translationFontSize = size)
    }

    fun setArabicFontName(fontName: String) {
        _config.value = _config.value.copy(arabicFontName = fontName)
    }

    fun setBengaliFontName(fontName: String) {
        _config.value = _config.value.copy(bengaliFontName = fontName)
    }

    fun setQari(qariId: String, qariName: String) {
        _config.value = _config.value.copy(qariId = qariId, qariName = qariName)
        if (_currentStep.value == 3) {
            prepareAndPlayPreviewAudio()
        }
    }

    // Audio Preview Controls
    fun prepareAndPlayPreviewAudio() {
        releasePlayer()
        val currentAyahs = _config.value.selectedAyahs
        if (currentAyahs.isEmpty()) return

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            val qari = _config.value.qariId
            val mediaItems = currentAyahs.map { ayah ->
                val audioUrl = AudioUtils.getAudioUrl(qari, ayah.number)
                MediaItem.fromUri(audioUrl)
            }
            setMediaItems(mediaItems)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentAyahIndex.value = currentMediaItemIndex
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        _currentPlaybackProgress.value = 1f
                    }
                }
            })
        }

        startProgressTracker()
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        } ?: prepareAndPlayPreviewAudio()
    }

    fun seekToProgress(progress: Float) {
        exoPlayer?.let { player ->
            val duration = player.duration
            if (duration > 0) {
                val seekPosition = (duration * progress).toLong()
                player.seekTo(seekPosition)
            }
        }
    }

    fun pausePreviewAudio() {
        exoPlayer?.pause()
        _isPlaying.value = false
        progressTrackingJob?.cancel()
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = player.duration
                    if (dur > 0) {
                        _currentPlaybackProgress.value = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                    }
                }
                delay(100)
            }
        }
    }

    // Export Video
    fun startExport() {
        pausePreviewAudio()
        _isExporting.value = true
        _exportProgress.value = 0f
        _exportError.value = null
        _exportedVideoUri.value = null
        _exportedVideoFile.value = null
        setStep(4)

        viewModelScope.launch {
            val audioUrls = _config.value.selectedAyahs.map { ayah ->
                AudioUtils.getAudioUrl(_config.value.qariId, ayah.number)
            }

            val result = QuranVideoExporter.exportQuranVideo(
                context = context,
                config = _config.value,
                audioFileUrls = audioUrls,
                onProgress = { progress ->
                    _exportProgress.value = progress
                }
            )

            _isExporting.value = false
            if (result.isSuccess) {
                _exportedVideoUri.value = result.videoUri
                _exportedVideoFile.value = result.videoFile
                Toast.makeText(context, "ভিডিও সফলভাবে গ্যালারিতে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
            } else {
                _exportError.value = result.errorMessage ?: "ভিডিও রেন্ডারিং ব্যর্থ হয়েছে।"
            }
        }
    }

    fun shareExportedVideo() {
        val file = _exportedVideoFile.value ?: return
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "সূরা ${_config.value.surahName} • আয়াত ${_config.value.ayahStart}\nকুরআন রিডার অ্যাপ থেকে তৈরি")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "ভিডিও শেয়ার করুন").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করতে ব্যর্থ হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun releasePlayer() {
        progressTrackingJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        _isPlaying.value = false
        _currentPlaybackProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
