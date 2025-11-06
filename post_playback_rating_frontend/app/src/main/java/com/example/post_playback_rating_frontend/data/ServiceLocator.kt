package com.example.post_playback_rating_frontend.data

import android.content.Context
import android.util.Log
import com.example.post_playback_rating_frontend.core.FeatureFlags

/**
 * Service locator for repositories.
 *
 * MOCK-ONLY MODE:
 * Per current requirements, the app must run without external dependencies and avoid any network initialization.
 * Therefore, ServiceLocator ALWAYS returns mock implementations and logs a clear "MOCK MODE" entry.
 */
object ServiceLocator {

    private const val TAG = "ServiceLocator"
    private var dataSourceLogged = false

    fun contentRepository(): ContentRepository {
        logMockOnce()
        return MockContentRepository()
    }

    fun metadataRepository(context: Context): MetadataRepository {
        logMockOnce()
        return MockMetadataRepository(context)
    }

    fun assetsRepository(): AssetsRepository {
        logMockOnce()
        return MockAssetsRepository()
    }

    fun ratingRepository(): RatingRepository {
        logMockOnce()
        return MockRatingRepository()
    }

    // PUBLIC_INTERFACE
    /** Returns whether mocks mode is currently active. Always true in mock-only mode. */
    fun useMocks(): Boolean = true

    private fun logMockOnce() {
        if (!dataSourceLogged) {
            FeatureFlags.logMockModeIfEnabled()
            Log.i(TAG, "Data source: MOCK (mock-only mode, network disabled)")
            dataSourceLogged = true
        }
    }
}
