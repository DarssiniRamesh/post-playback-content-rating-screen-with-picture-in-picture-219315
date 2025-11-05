package com.example.post_playback_rating_frontend.data.network

import android.content.Context
import android.util.Log
import com.example.post_playback_rating_frontend.data.AssetsRepository
import com.example.post_playback_rating_frontend.data.ContentInfo
import com.example.post_playback_rating_frontend.data.ContentRepository
import com.example.post_playback_rating_frontend.data.IndiceCopy
import com.example.post_playback_rating_frontend.data.InstructionImages
import com.example.post_playback_rating_frontend.data.MetadataRepository
import com.example.post_playback_rating_frontend.data.RatingRepository
import com.example.post_playback_rating_frontend.data.VodRatingSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Import mapper extension functions explicitly
import com.example.post_playback_rating_frontend.data.network.NetworkModels.toDomainSafe as mapIndiceCopy
import com.example.post_playback_rating_frontend.data.network.NetworkModels.toDomainSafe as mapVodSettings
import com.example.post_playback_rating_frontend.data.network.NetworkModels.toDomainSafe as mapContentInfo
import com.example.post_playback_rating_frontend.data.network.NetworkModels.toDomainSafe as mapInstructionImages

/**
 * Network-backed repositories with safe error handling and defaults that match current overlay behavior.
 * If any call fails, fall back to local-safe defaults.
 */

private const val TAG = "NetworkRepos"

class NetworkContentRepository(
    private val api: ContentApi
) : ContentRepository {
    override suspend fun getHomeSections(): List<String> = withIoCatching(listOf("Prototipo 1", "Prototipo 2", "Prototipo 3")) {
        api.getHomeSections()
    }

    override suspend fun getContentInfo(contentId: String): ContentInfo = withIoCatching(
        ContentInfo(
            id = contentId,
            title = "The Ocean and the Sky",
            posterUrl = "file:///android_asset/figmaimages/figma_image_176_1015.png"
        )
    ) {
        // Use fully qualified call to avoid overload confusion
        NetworkModels.run { api.getContentInfo(contentId).toDomainSafe() }
    }
}

class NetworkMetadataRepository(
    private val context: Context,
    private val api: MetadataApi
) : MetadataRepository {
    override suspend fun getIndiceCopy(): IndiceCopy = withIoCatching(defaultIndiceCopy(context)) {
        NetworkModels.run { api.getIndiceCopy().toDomainSafe() }
    }

    override suspend fun getVodRatingSettings(): VodRatingSettings = withIoCatching(
        VodRatingSettings(
            displayTimeSeconds = 10,
            maxDisplayTimeSeconds = 60,
            rollingCreditsTimeMs = 3000L
        )
    ) {
        NetworkModels.run { api.getVodRatingSettings().toDomainSafe() }
    }

    private fun defaultIndiceCopy(context: Context): IndiceCopy {
        val r = context.resources
        return IndiceCopy(
            title = r.getString(com.example.post_playback_rating_frontend.R.string.indice_title),
            section1 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_section1_header),
            section2 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_section2_header),
            section3 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_section3_header),
            cta1 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_cta1),
            cta2 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_cta2),
            cta3 = r.getString(com.example.post_playback_rating_frontend.R.string.indice_cta3),
            helper = r.getString(com.example.post_playback_rating_frontend.R.string.indice_helper),
        )
    }
}

class NetworkAssetsRepository(
    private val api: AssetsApi
) : AssetsRepository {
    override fun getInstructionImages(): InstructionImages {
        // Assets fetching is not suspend by design here; keep sync by returning safe defaults.
        // If/when network needed, adjust to suspend and fetch once with caching.
        return try {
            // In absence of network call on main thread, return defaults; actual fetch should be orchestrated by a caller.
            NetworkModels.run { NetworkModels.NetworkInstructionImages().toDomainSafe() }
        } catch (t: Throwable) {
            Log.w(TAG, "Assets default used due to exception: ${t.message}")
            NetworkModels.run { NetworkModels.NetworkInstructionImages().toDomainSafe() }
        }
    }
}

class NetworkRatingRepository(
    private val api: LikesApi
) : RatingRepository {
    override suspend fun isLiked(contentId: String): Boolean = withIoCatching(false) {
        api.isLiked(contentId).liked ?: false
    }

    override suspend fun setLike(contentId: String, liked: Boolean): Boolean = withIoCatching(true) {
        api.setLike(contentId, liked).liked ?: liked
    }
}

private suspend fun <T> withIoCatching(default: T, block: suspend () -> T): T {
    return try {
        withContext(Dispatchers.IO) {
            block()
        }
    } catch (t: Throwable) {
        Log.w(TAG, "Network call failed, using default: ${t.message}")
        default
    }
}
