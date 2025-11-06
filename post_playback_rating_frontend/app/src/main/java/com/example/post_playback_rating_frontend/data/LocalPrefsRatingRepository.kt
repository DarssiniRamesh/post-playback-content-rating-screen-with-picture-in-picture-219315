package com.example.post_playback_rating_frontend.data

import android.content.Context
import android.content.SharedPreferences

/**
 * LocalPrefsRatingRepository persists per-content rating state using SharedPreferences.
 * Keys are namespaced to avoid collisions:
 * - rated_{contentId} -> Boolean: whether the user has rated the content
 * - rating_{contentId} -> Int: 1 for liked, 0 for not liked/none
 *
 * This repository is used in mock/offline mode to retain state across app restarts.
 */
class LocalPrefsRatingRepository(private val appContext: Context) : RatingRepository {

    private val prefs: SharedPreferences by lazy {
        // Use applicationContext to avoid leaking Activity
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // PUBLIC_INTERFACE
    /** Returns whether the content has been rated locally. */
    override suspend fun hasRated(contentId: String): Boolean {
        return prefs.getBoolean(keyRated(contentId), false)
    }

    // PUBLIC_INTERFACE
    /** Marks the content as rated/unrated locally. Returns true if persisted. */
    override suspend fun setRated(contentId: String, rated: Boolean): Boolean {
        return prefs.edit().putBoolean(keyRated(contentId), rated).commit()
    }

    // PUBLIC_INTERFACE
    /**
     * Returns whether content is liked (maps love -> like=true, dislike -> like=false handled by caller).
     * If there is no previous value, defaults to false.
     */
    override suspend fun isLiked(contentId: String): Boolean {
        val def = false
        val stored = prefs.getInt(keyRating(contentId), if (def) 1 else 0)
        return stored == 1
    }

    // PUBLIC_INTERFACE
    /** Sets like state for content and marks as rated=true */
    override suspend fun setLike(contentId: String, liked: Boolean): Boolean {
        val ok = prefs.edit()
            .putInt(keyRating(contentId), if (liked) 1 else 0)
            .putBoolean(keyRated(contentId), true)
            .commit()
        return ok
    }

    private fun keyRated(contentId: String) = "rated_${contentId}"
    private fun keyRating(contentId: String) = "rating_${contentId}"

    companion object {
        private const val PREFS_NAME = "post_playback_rating_prefs"
    }
}
