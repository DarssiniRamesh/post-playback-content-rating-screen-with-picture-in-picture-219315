package com.example.post_playback_rating_frontend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.example.post_playback_rating_frontend.core.FeatureFlags
import com.example.post_playback_rating_frontend.rating.PostPlaybackRatingViewModel
import com.example.post_playback_rating_frontend.rating.RatingOverlayFragment
import com.example.post_playback_rating_frontend.rating.RatingOverlayHost
import com.example.post_playback_rating_frontend.ui.IndiceFragment

/**
 * Main Activity for Android TV.
 * Hosts the app's fragments and manages back navigation.
 * Also acts as RatingOverlayHost and simulates player hooks for demo.
 */
class MainActivity : FragmentActivity(), RatingOverlayHost {

    private val ratingVm: PostPlaybackRatingViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    // Mock playback timer
    private var mockPositionMs: Long = 0L
    private var mockDurationMs: Long = 30_000L
    private var isPlaying: Boolean = false

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

        // Start mock playback loop if feature flag enabled (no external dependencies)
        if (FeatureFlags.useMocks) {
            startMockPlayback()
        }
    }

    private fun startMockPlayback() {
        isPlaying = true
        ratingVm.onDurationChanged(mockDurationMs)
        handler.post(object : Runnable {
            override fun run() {
                if (!isPlaying) return
                mockPositionMs += 1000L
                ratingVm.onPlaybackPositionChanged(mockPositionMs)

                // Trigger credits start at duration - 3000ms
                val rollingCreditsMs = mockDurationMs - 3000L
                if (mockPositionMs >= rollingCreditsMs && mockPositionMs < mockDurationMs) {
                    // Only trigger once when crossing boundary
                    if (mockPositionMs - 1000L < rollingCreditsMs) {
                        onCreditsStart()
                    }
                }

                if (mockPositionMs >= mockDurationMs) {
                    ratingVm.onPlaybackPositionChanged(mockDurationMs)
                    ratingVm.onEndReached()
                    isPlaying = false
                } else {
                    handler.postDelayed(this, 1000L)
                }
            }
        })
    }

    private fun onCreditsStart() {
        ratingVm.onCreditsStart(mockPositionMs)
        if (FeatureFlags.useMocks) {
            // Show overlay UI
            showRatingOverlay()
        }
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
    }

    // PUBLIC_INTERFACE
    /** Return to end player or main playback area (stub). */
    override fun showEndPlayer() {
        // Stub: In a real app, return to end screen. Nothing required for mock.
    }
}
