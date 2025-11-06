package com.example.post_playback_rating_frontend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.example.post_playback_rating_frontend.core.FeatureFlags
import com.example.post_playback_rating_frontend.rating.PostPlaybackRatingViewModel
import com.example.post_playback_rating_frontend.rating.RatingOverlayFragment
import com.example.post_playback_rating_frontend.rating.RatingOverlayHost
import com.example.post_playback_rating_frontend.ui.IndiceFragment

/**
 * Main Activity for Android TV.
 * Hosts the app's fragments and manages back navigation.
 * Also acts as RatingOverlayHost and simulates player hooks for demo.
 *
 * Integration requirements fulfilled here:
 * - Triggers rating overlay at rolling credits start and at end of mock playback.
 * - Observes ViewModel state to show/hide overlay and route after close.
 * - Consults persisted rating state via ViewModel; no direct network calls.
 * - MOCK MODE remains active; no initialization of network clients.
 */
class MainActivity : FragmentActivity(), RatingOverlayHost {

    private val ratingVm: PostPlaybackRatingViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    // Mock playback timer
    private var mockPositionMs: Long = 0L
    private var mockDurationMs: Long = 30_000L
    private var isPlaying: Boolean = false

    // Track if we've already issued a credits-start signal to avoid duplicates in the same window.
    private var creditsStartEmitted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Dev indicator: log current data source (Mock/Network) and explicit MOCK MODE banner
        val usingMocks = com.example.post_playback_rating_frontend.data.ServiceLocator.useMocks()
        if (usingMocks) {
            FeatureFlags.logMockModeIfEnabled()
        }
        Log.i("MainActivity", "Data source: ${if (usingMocks) "Mock" else "Network"}")

        // Load Índice on first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, IndiceFragment.newInstance())
                .commitNow()
        }

        // Observe rating overlay state and route UI actions accordingly (no network)
        observeRatingState()

        // Start mock playback loop if feature flag enabled (no external dependencies)
        if (FeatureFlags.useMocks) {
            startMockPlayback()
        }
    }

    private fun observeRatingState() {
        ratingVm.state.observe(this, Observer { s ->
            when (s) {
                is PostPlaybackRatingViewModel.OverlayState.Loading -> {
                    // Defer UI until ready
                }
                is PostPlaybackRatingViewModel.OverlayState.Visible -> {
                    // Gate: ViewModel already consulted hasRated(); just show
                    showRatingOverlay()
                }
                is PostPlaybackRatingViewModel.OverlayState.AutoClosing -> {
                    // Nothing to do here: fragment updates countdown via VM
                }
                is PostPlaybackRatingViewModel.OverlayState.Closed -> {
                    // Overlay dismissed → show end player (stub)
                    showEndPlayer()
                }
                is PostPlaybackRatingViewModel.OverlayState.NavigateToVcard -> {
                    // Overlay auto-closed at/after end → navigate to vCard
                    navigateToVcard()
                }
                is PostPlaybackRatingViewModel.OverlayState.Hidden -> {
                    // Ensure dialog removed if still present
                    val prev = supportFragmentManager.findFragmentByTag(RatingOverlayFragment.TAG)
                    if (prev is RatingOverlayFragment) {
                        prev.dismissAllowingStateLoss()
                    }
                }
                else -> {
                    // no-op for other states
                }
            }
        })
    }

    private fun startMockPlayback() {
        isPlaying = true
        creditsStartEmitted = false
        mockPositionMs = 0L
        ratingVm.onDurationChanged(mockDurationMs)
        handler.post(object : Runnable {
            override fun run() {
                if (!isPlaying) return
                mockPositionMs += 1000L
                ratingVm.onPlaybackPositionChanged(mockPositionMs)

                // Trigger credits start at duration - 3000ms (matches default rolling setting in mock)
                val rollingCreditsBoundaryMs = mockDurationMs - 3000L
                if (!creditsStartEmitted &&
                    mockPositionMs >= rollingCreditsBoundaryMs &&
                    mockPositionMs < mockDurationMs
                ) {
                    creditsStartEmitted = true
                    onCreditsStart()
                }

                if (mockPositionMs >= mockDurationMs) {
                    // Clamp to end
                    ratingVm.onPlaybackPositionChanged(mockDurationMs)
                    // Inform ViewModel end reached (will influence routing on auto-close)
                    ratingVm.onEndReached()
                    isPlaying = false

                    // If overlay wasn't shown during credits (e.g., already rated), still allow end routing logic
                    // ViewModel handles whether to show or just remain closed.
                } else {
                    handler.postDelayed(this, 1000L)
                }
            }
        })
    }

    private fun onCreditsStart() {
        // Let ViewModel decide based on persisted hasRated(contentId)
        ratingVm.onCreditsStart(mockPositionMs)
        // No direct call to show overlay here: observer will show if VM sets Visible
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Let focused fragment handle key if needed, else default behavior
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        // Predictable back behavior
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    // RatingOverlayHost implementation

    // PUBLIC_INTERFACE
    /** Show the rating overlay UI. */
    override fun showRatingOverlay() {
        val prev = supportFragmentManager.findFragmentByTag(RatingOverlayFragment.TAG)
        if (prev == null) {
            RatingOverlayFragment.newInstance()
                .show(supportFragmentManager, RatingOverlayFragment.TAG)
        }
    }

    // PUBLIC_INTERFACE
    /** Navigate to a vCard or post-rating detail screen (stub). */
    override fun navigateToVcard() {
        // Stub: For demo logics, show end player (could push a fragment in a real app)
        // Here we could show a toast/log; keeping silent as per TV
        // Ensure overlay is dismissed
        val prev = supportFragmentManager.findFragmentByTag(RatingOverlayFragment.TAG)
        if (prev is RatingOverlayFragment) {
            prev.dismissAllowingStateLoss()
        }
    }

    // PUBLIC_INTERFACE
    /** Return to end player or main playback area (stub). */
    override fun showEndPlayer() {
        // Stub: In a real app, return to end screen. Nothing required for mock.
        // Ensure overlay is dismissed so user sees the underlying end screen/fragment.
        val prev = supportFragmentManager.findFragmentByTag(RatingOverlayFragment.TAG)
        if (prev is RatingOverlayFragment) {
            prev.dismissAllowingStateLoss()
        }
    }
}
