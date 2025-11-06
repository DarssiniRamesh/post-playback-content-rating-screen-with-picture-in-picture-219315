package com.example.post_playback_rating_frontend.data

import android.content.Context
import kotlinx.coroutines.delay

// PUBLIC_INTERFACE
/** ContentRepository provides content metadata for rating overlay and screens. */
interface ContentRepository {
    suspend fun getHomeSections(): List<String>

    // PUBLIC_INTERFACE
    /** Returns minimal content info used by the rating overlay. */
    suspend fun getContentInfo(contentId: String): ContentInfo
}

// PUBLIC_INTERFACE
/** MetadataRepository provides app copy, labels and configuration metadata. */
interface MetadataRepository {
    suspend fun getIndiceCopy(): IndiceCopy

    // PUBLIC_INTERFACE
    /** Returns rating overlay settings and rolling credits time in milliseconds from end. */
    suspend fun getVodRatingSettings(): VodRatingSettings
}

// PUBLIC_INTERFACE
/** AssetsRepository exposes local asset paths or URLs for images. */
interface AssetsRepository {
    fun getInstructionImages(): InstructionImages
}

// PUBLIC_INTERFACE
/** RatingRepository provides rating state and like toggling, with per-content persistence. */
interface RatingRepository {
    // PUBLIC_INTERFACE
    /** Returns whether the user has rated the given content ID. */
    suspend fun hasRated(contentId: String): Boolean

    // PUBLIC_INTERFACE
    /** Persists whether the user has rated the given content ID. */
    suspend fun setRated(contentId: String, rated: Boolean): Boolean

    // PUBLIC_INTERFACE
    /** Returns whether the user liked the content (love maps to like=true by ViewModel). */
    suspend fun isLiked(contentId: String): Boolean

    // PUBLIC_INTERFACE
    /** Sets like state for the content (also implies rated=true). */
    suspend fun setLike(contentId: String, liked: Boolean): Boolean
}

data class IndiceCopy(
    val title: String,
    val section1: String,
    val section2: String,
    val section3: String,
    val cta1: String,
    val cta2: String,
    val cta3: String,
    val helper: String
)

data class InstructionImages(
    val mainPreview: String,
    val stepAWindow: String,
    val stepAZoom: String,
    val panelTall: String,
    val shortcutIcon: String,
    val overlaySquare: String,
    val restartBar: String
)

// PUBLIC_INTERFACE
/** Minimal content info for the overlay. */
data class ContentInfo(
    val id: String,
    val title: String,
    val posterUrl: String
)

// PUBLIC_INTERFACE
/** Rating overlay settings from metadata layer. */
data class VodRatingSettings(
    val displayTimeSeconds: Int,
    val maxDisplayTimeSeconds: Int,
    val rollingCreditsTimeMs: Long
)

/**
 * Mock implementations - zero dependency offline mode.
 * These return bundled assets and static strings.
 */
class MockContentRepository : ContentRepository {
    override suspend fun getHomeSections(): List<String> {
        delay(50) // Simulate quick fetch
        return listOf("Prototipo 1", "Prototipo 2", "Prototipo 3")
    }

    override suspend fun getContentInfo(contentId: String): ContentInfo {
        // Static mock content
        return ContentInfo(
            id = contentId,
            title = "The Ocean and the Sky",
            posterUrl = "file:///android_asset/figmaimages/figma_image_176_1015.png"
        )
    }
}

class MockMetadataRepository(private val context: Context) : MetadataRepository {
    override suspend fun getIndiceCopy(): IndiceCopy {
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

    override suspend fun getVodRatingSettings(): VodRatingSettings {
        // Return sensible defaults for demo; display capped to <= 60
        return VodRatingSettings(
            displayTimeSeconds = 10,
            maxDisplayTimeSeconds = 60,
            rollingCreditsTimeMs = 3000L
        )
    }
}

class MockAssetsRepository : AssetsRepository {
    override fun getInstructionImages(): InstructionImages {
        // Map to asset paths placed under /assets/figmaimages
        return InstructionImages(
            mainPreview = "file:///android_asset/figmaimages/figma_image_176_1015.png",
            stepAWindow = "file:///android_asset/figmaimages/figma_image_176_1016.png",
            stepAZoom = "file:///android_asset/figmaimages/figma_image_176_1018.png",
            panelTall = "file:///android_asset/figmaimages/figma_image_176_1021.png",
            shortcutIcon = "file:///android_asset/figmaimages/figma_image_41_219.png",
            overlaySquare = "file:///android_asset/figmaimages/figma_image_41_221.png",
            restartBar = "file:///android_asset/figmaimages/figma_image_41_207.png",
        )
    }
}

class MockRatingRepository : RatingRepository {
    private val likes = HashMap<String, Boolean>()
    private val rated = HashMap<String, Boolean>()

    override suspend fun hasRated(contentId: String): Boolean {
        return rated[contentId] ?: false
    }

    override suspend fun setRated(contentId: String, rated: Boolean): Boolean {
        this.rated[contentId] = rated
        return true
    }

    override suspend fun isLiked(contentId: String): Boolean {
        return likes[contentId] ?: false
    }

    override suspend fun setLike(contentId: String, liked: Boolean): Boolean {
        likes[contentId] = liked
        rated[contentId] = true
        return true
    }
}
