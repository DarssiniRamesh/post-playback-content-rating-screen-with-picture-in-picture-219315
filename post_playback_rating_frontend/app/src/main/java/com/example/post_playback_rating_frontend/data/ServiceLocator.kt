package com.example.post_playback_rating_frontend.data

import android.content.Context
import android.util.Log
import com.example.post_playback_rating_frontend.core.FeatureFlags
import com.example.post_playback_rating_frontend.data.network.ApiClient
import com.example.post_playback_rating_frontend.data.network.AssetsApi
import com.example.post_playback_rating_frontend.data.network.ContentApi
import com.example.post_playback_rating_frontend.data.network.LikesApi
import com.example.post_playback_rating_frontend.data.network.MetadataApi
import com.example.post_playback_rating_frontend.data.network.NetworkAssetsRepository
import com.example.post_playback_rating_frontend.data.network.NetworkContentRepository
import com.example.post_playback_rating_frontend.data.network.NetworkMetadataRepository
import com.example.post_playback_rating_frontend.data.network.NetworkRatingRepository

/**
 * Service locator returning either mock or real implementations.
 * Auto-falls back to mocks if base URLs are missing or placeholders.
 */
object ServiceLocator {

    private const val TAG = "ServiceLocator"

    // Cache created APIs when network is enabled
    private var contentApi: ContentApi? = null
    private var metadataApi: MetadataApi? = null
    private var assetsApi: AssetsApi? = null
    private var likesApi: LikesApi? = null
    private var dataSourceLogged = false

    fun contentRepository(): ContentRepository {
        return if (shouldUseNetwork()) {
            ensureApis()
            NetworkContentRepository(contentApi!!)
        } else {
            MockContentRepository()
        }
    }

    fun metadataRepository(context: Context): MetadataRepository {
        return if (shouldUseNetwork()) {
            ensureApis()
            NetworkMetadataRepository(context, metadataApi!!)
        } else {
            MockMetadataRepository(context)
        }
    }

    fun assetsRepository(): AssetsRepository {
        return if (shouldUseNetwork()) {
            ensureApis()
            NetworkAssetsRepository(assetsApi!!)
        } else {
            MockAssetsRepository()
        }
    }

    fun ratingRepository(): RatingRepository {
        return if (shouldUseNetwork()) {
            ensureApis()
            NetworkRatingRepository(likesApi!!)
        } else {
            MockRatingRepository()
        }
    }

    // PUBLIC_INTERFACE
    /** Returns whether mocks mode is currently active (may be forced if config missing). */
    fun useMocks(): Boolean = !shouldUseNetwork()

    private fun shouldUseNetwork(): Boolean {
        val featureFlagEnabled = !FeatureFlags.useMocks
        if (!featureFlagEnabled) {
            logDataSourceOnce("Mock (FeatureFlags.USE_MOCKS=true)")
            return false
        }
        val config = ApiClient.NetworkConfig.fromBuildConfig()
        if (config.isMissingOrPlaceholder()) {
            Log.w(TAG, "Base URL missing or placeholder; falling back to mocks. Provide real endpoints to enable network.")
            logDataSourceOnce("Mock (Missing/placeholder base URLs)")
            return false
        }
        logDataSourceOnce("Network")
        return true
    }

    private fun ensureApis() {
        if (contentApi != null && metadataApi != null && assetsApi != null && likesApi != null) return
        val cfg = ApiClient.NetworkConfig.fromBuildConfig()
        val contentRetrofit = ApiClient.retrofit(cfg.contentBaseUrl)
        val metadataRetrofit = ApiClient.retrofit(cfg.metadataBaseUrl)
        val assetsRetrofit = ApiClient.retrofit(cfg.assetsBaseUrl)
        val likesRetrofit = ApiClient.retrofit(cfg.likesBaseUrl)

        contentApi = contentRetrofit.create(ContentApi::class.java)
        metadataApi = metadataRetrofit.create(MetadataApi::class.java)
        assetsApi = assetsRetrofit.create(AssetsApi::class.java)
        likesApi = likesRetrofit.create(LikesApi::class.java)
    }

    private fun logDataSourceOnce(source: String) {
        if (!dataSourceLogged) {
            Log.i(TAG, "Data source: $source")
            dataSourceLogged = true
        }
    }
}
