package com.example.post_playback_rating_frontend.rating

// PUBLIC_INTERFACE
/**
 * RatingOverlayHost defines hooks for showing rating overlay and navigation targets.
 * Not implemented yet; fragments/activities can check and call these methods when integrated.
 */
interface RatingOverlayHost {
    /** Show the rating overlay UI. */
    fun showRatingOverlay()

    /** Navigate to a vCard or post-rating detail screen. */
    fun navigateToVcard()

    /** Return to end player or main playback area. */
    fun showEndPlayer()
}
