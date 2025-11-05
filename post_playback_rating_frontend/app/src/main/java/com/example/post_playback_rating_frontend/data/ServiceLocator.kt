package com.example.post_playback_rating_frontend.data

import android.content.Context
import com.example.post_playback_rating_frontend.core.FeatureFlags

/**
 * Service locator returning either mock or real implementations.
 * Real implementations can be added later behind the same interfaces.
 */
object ServiceLocator {
    fun contentRepository(): ContentRepository = MockContentRepository()

    fun metadataRepository(context: Context): MetadataRepository =
        MockMetadataRepository(context)

    fun assetsRepository(): AssetsRepository = MockAssetsRepository()

    fun ratingRepository(): RatingRepository = MockRatingRepository()

    // In future, when FeatureFlags.useMocks == false, return network-backed repos instead.
    fun useMocks(): Boolean = FeatureFlags.useMocks
}
