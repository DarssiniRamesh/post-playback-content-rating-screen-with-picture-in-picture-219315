package com.example.post_playback_rating_frontend.rating

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.post_playback_rating_frontend.data.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * ViewModel for Post-Playback Rating Overlay.
 * Handles player hooks and UI state transitions with a countdown auto-close.
 */
class PostPlaybackRatingViewModel(application: Application) : AndroidViewModel(application) {

    // Public UI state exposed for the overlay fragment
    private val _state = MutableLiveData<OverlayState>(OverlayState.Hidden)
    val state: LiveData<OverlayState> = _state

    // Internal mock data
    private val contentRepo = ServiceLocator.contentRepository()
    private val metadataRepo = ServiceLocator.metadataRepository(application.applicationContext)
    private val assetsRepo = ServiceLocator.assetsRepository()
    private val ratingRepo = ServiceLocator.ratingRepository(application.applicationContext)

    // Simulated current content id and metadata
    private var currentContentId: String = "content-123"
    private var title: String = "Sample Title"
    private var posterUrl: String = "file:///android_asset/figmaimages/figma_image_176_1015.png"

    // Mock metadata
    private var rollingCreditsTimeMs: Long = 0L
    private var displayTimeSec: Int = 8
    private var maxDisplayTimeSec: Int = 60

    // Playback state
    private var durationMs: Long = 0L
    private var positionMs: Long = 0L
    private var endReached: Boolean = false

    // Overlay internal
    private var countdownJob: Job? = null
    private var remainingSeconds: Int = 0
    private var wasClosedWithoutRating: Boolean = false
    private var wasRated: Boolean = false
    private var lastCreditsShownAtMs: Long = -1L

    init {
        // Preload mocks (no network)
        viewModelScope.launch {
            // Using metadata repo to simulate settings
            val m = metadataRepo.getIndiceCopy() // we have only indice copy; actual settings below are hardcoded
            // Static mock settings for the rating flow
            rollingCreditsTimeMs = 3_000L // 3s from end begin credits
            displayTimeSec = 10
            maxDisplayTimeSec = 60
        }
        // Load persisted rated state for current content id
        viewModelScope.launch {
            try {
                wasRated = ratingRepo.hasRated(currentContentId)
            } catch (_: Throwable) {
                // ignore, default false
            }
        }
    }

    // PUBLIC_INTERFACE
    /** State model for overlay rendering. */
    sealed class OverlayState {
        object Hidden : OverlayState()
        object Loading : OverlayState()
        data class Visible(
            val title: String,
            val posterUrl: String,
            val likeSelected: Boolean,
            val loveSelected: Boolean,
            val dislikeSelected: Boolean,
            val countdownSeconds: Int
        ) : OverlayState()
        data class AutoClosing(val remainingSeconds: Int) : OverlayState()
        object Closed : OverlayState()
        object NavigateToVcard : OverlayState()
    }

    // PUBLIC_INTERFACE
    /** Player hook: credits start reached at current playback position. */
    fun onCreditsStart(currentPositionMs: Long) {
        positionMs = currentPositionMs
        lastCreditsShownAtMs = currentPositionMs
        if (!shouldShowOverlay()) return

        wasClosedWithoutRating = false
        showOverlayInternal()
    }

    // PUBLIC_INTERFACE
    /** Player hook: playback position changed. Used to detect rewind before credits to allow re-show. */
    fun onPlaybackPositionChanged(posMs: Long) {
        positionMs = posMs
        // If user rewinds before rolling credits and we previously closed without rating, allow re-show on next credits
        // Simply tracking position; overlay will be triggered by external call to onCreditsStart again in host.
    }

    // PUBLIC_INTERFACE
    /** Player hook: duration updated. */
    fun onDurationChanged(durMs: Long) {
        durationMs = durMs
    }

    // PUBLIC_INTERFACE
    /** Player hook: end reached. Used to route to vCard if overlay auto-closes at/after end. */
    fun onEndReached() {
        endReached = true
        // If overlay is currently auto-closing or visible and countdown hits zero when end reached,
        // the auto-close path will emit NavigateToVcard.
    }

    // PUBLIC_INTERFACE
    /** Called by UI when Close button pressed. */
    fun onClosePressed() {
        cancelCountdown()
        wasClosedWithoutRating = !wasRated
        _state.postValue(OverlayState.Closed)
    }

    // PUBLIC_INTERFACE
    /** Called by UI when Like selected. */
    fun onLike() {
        handleRatingSelection(like = true, love = false, dislike = false)
    }

    // PUBLIC_INTERFACE
    /** Called by UI when Love selected. */
    fun onLove() {
        handleRatingSelection(like = false, love = true, dislike = false)
    }

    // PUBLIC_INTERFACE
    /** Called by UI when Dislike selected. */
    fun onDislike() {
        handleRatingSelection(like = false, love = false, dislike = true)
    }

    private fun handleRatingSelection(like: Boolean, love: Boolean, dislike: Boolean) {
        viewModelScope.launch {
            // Persist locally and mark as rated
            wasRated = true
            ratingRepo.setLike(currentContentId, like || love) // simple mapping for demo
            ratingRepo.setRated(currentContentId, true)
            cancelCountdown()
            _state.postValue(OverlayState.Closed)
        }
    }

    private fun shouldShowOverlay(): Boolean {
        if (wasRated) return false
        // Check persisted state (best-effort async refresh for subsequent calls)
        viewModelScope.launch {
            try {
                if (ratingRepo.hasRated(currentContentId)) {
                    wasRated = true
                }
            } catch (_: Throwable) { /* ignore */ }
        }
        if (wasRated) return false
        if (wasClosedWithoutRating) {
            // It can show again if rewound before credits and credits are triggered anew
            return true
        }
        return true
    }

    private fun showOverlayInternal() {
        viewModelScope.launch {
            _state.postValue(OverlayState.Loading)

            // In a real impl, fetch content info. Here assign static values.
            title = "The Ocean and the Sky"
            posterUrl = assetsRepo.getInstructionImages().mainPreview

            // Cap display time by metadata max and 60s hard cap
            val capped = min(min(displayTimeSec, maxDisplayTimeSec), 60)
            remainingSeconds = max(1, capped)
            _state.postValue(
                OverlayState.Visible(
                    title = title,
                    posterUrl = posterUrl,
                    likeSelected = false,
                    loveSelected = false,
                    dislikeSelected = false,
                    countdownSeconds = remainingSeconds
                )
            )
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds -= 1
                val s = state.value
                when (s) {
                    is OverlayState.Visible -> {
                        _state.postValue(s.copy(countdownSeconds = remainingSeconds))
                    }
                    is OverlayState.AutoClosing -> {
                        _state.postValue(OverlayState.AutoClosing(remainingSeconds))
                    }
                    else -> {
                        // keep silent if closed
                    }
                }
                if (remainingSeconds <= 0) break
            }
            handleCountdownFinished()
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun handleCountdownFinished() {
        // Auto-close; if end reached or exceeded, navigate to vCard else Closed
        val atOrPastEnd = endReached || (durationMs > 0 && positionMs >= durationMs)
        if (atOrPastEnd) {
            _state.postValue(OverlayState.NavigateToVcard)
        } else {
            _state.postValue(OverlayState.Closed)
        }
    }

    // PUBLIC_INTERFACE
    /** Host can force-hide overlay (e.g., navigating away). */
    fun hideOverlay() {
        cancelCountdown()
        _state.postValue(OverlayState.Hidden)
    }
}
