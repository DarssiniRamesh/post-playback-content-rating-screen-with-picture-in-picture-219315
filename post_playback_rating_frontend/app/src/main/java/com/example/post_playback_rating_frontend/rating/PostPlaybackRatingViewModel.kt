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
 * Requirements implemented:
 * - Gate overlay visibility by LocalPrefsRatingRepository.hasRated(contentId)
 * - Start a countdown (defaults to 10s from metadata or fallback) when visible
 * - Auto-close at 0 (navigate to vCard if end reached)
 * - On any rating, setRated(contentId)=true and persist like value
 * - Use only mock/local repositories via ServiceLocator
 */
class PostPlaybackRatingViewModel(application: Application) : AndroidViewModel(application) {

    // Public UI state exposed for the overlay fragment
    private val _state = MutableLiveData<OverlayState>(OverlayState.Hidden)
    val state: LiveData<OverlayState> = _state

    // Internal mock data (mock-only mode through ServiceLocator)
    private val contentRepo = ServiceLocator.contentRepository()
    private val metadataRepo = ServiceLocator.metadataRepository(application.applicationContext)
    private val assetsRepo = ServiceLocator.assetsRepository()
    private val ratingRepo = ServiceLocator.ratingRepository(application.applicationContext)

    // Simulated current content id and metadata (for demo)
    private var currentContentId: String = "content-123"
    private var title: String = "Sample Title"
    private var posterUrl: String = "file:///android_asset/figmaimages/figma_image_176_1015.png"

    // Metadata/settings
    private var rollingCreditsTimeMs: Long = 3000L
    private var displayTimeSec: Int = 10
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
        // Load settings from mock metadata safely (fallbacks already set)
        viewModelScope.launch {
            try {
                val settings = metadataRepo.getVodRatingSettings()
                displayTimeSec = settings.displayTimeSeconds
                maxDisplayTimeSec = settings.maxDisplayTimeSeconds
                rollingCreditsTimeMs = settings.rollingCreditsTimeMs
            } catch (_: Throwable) {
                // keep defaults
            }
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

        // Always re-check persisted state at credits start
        viewModelScope.launch {
            try {
                wasRated = ratingRepo.hasRated(currentContentId)
            } catch (_: Throwable) {
                // keep prior in-memory value
            }

            if (!shouldShowOverlay()) return@launch

            wasClosedWithoutRating = false
            showOverlayInternal()
        }
    }

    // PUBLIC_INTERFACE
    /** Player hook: playback position changed. Used to detect rewind before credits to allow re-show. */
    fun onPlaybackPositionChanged(posMs: Long) {
        positionMs = posMs
        // If rewound before credits, a future onCreditsStart will handle re-show
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
        // Auto-close path will handle routing
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
            // Persist locally and mark as rated using mock/local repo
            wasRated = true
            try {
                ratingRepo.setLike(currentContentId, like || love) // map love -> like=true
                ratingRepo.setRated(currentContentId, true)
            } catch (_: Throwable) {
                // best-effort persistence
            }
            cancelCountdown()
            _state.postValue(OverlayState.Closed)
        }
    }

    /**
     * Decide if overlay should be shown now.
     * Rules:
     * - Do not show if already rated (persisted or in-session).
     * - If previously closed without rating, allow re-show when credits are triggered again (e.g., after rewind).
     */
    private fun shouldShowOverlay(): Boolean {
        if (wasRated) return false
        // If it was closed without rating earlier, we allow showing again at credits
        return true
    }

    private fun showOverlayInternal() {
        viewModelScope.launch {
            _state.postValue(OverlayState.Loading)

            // Load minimal content info (mock/local only)
            try {
                val info = contentRepo.getContentInfo(currentContentId)
                title = info.title
                posterUrl = info.posterUrl
            } catch (_: Throwable) {
                // Fallback to assets preview if contentRepo fails
                title = "The Ocean and the Sky"
                posterUrl = try {
                    assetsRepo.getInstructionImages().mainPreview
                } catch (_: Throwable) {
                    "file:///android_asset/figmaimages/figma_image_176_1015.png"
                }
            }

            // Cap display time by metadata max and 60s hard cap. Requirement: 10s default countdown.
            val base = displayTimeSec.takeIf { it > 0 } ?: 10
            val capped = min(min(base, maxDisplayTimeSec), 60)
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
                // Emit updated countdown to the UI
                val current = state.value
                when (current) {
                    is OverlayState.Visible -> {
                        _state.postValue(current.copy(countdownSeconds = remainingSeconds))
                    }
                    is OverlayState.AutoClosing -> {
                        _state.postValue(OverlayState.AutoClosing(remainingSeconds))
                    }
                    else -> {
                        // No-op if already closed/hidden
                    }
                }
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
