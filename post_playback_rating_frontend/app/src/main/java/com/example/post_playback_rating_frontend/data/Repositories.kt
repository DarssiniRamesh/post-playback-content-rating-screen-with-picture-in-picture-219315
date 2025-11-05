package com.example.post_playback_rating_frontend.data

import android.content.Context
import kotlinx.coroutines.delay

// PUBLIC_INTERFACE
/** ContentRepository provides content metadata for rating overlay and screens. */
interface ContentRepository {
    suspend fun getHomeSections(): List<String>
}

// PUBLIC_INTERFACE
/** MetadataRepository provides app copy, labels and configuration metadata. */
interface MetadataRepository {
    suspend fun getIndiceCopy(): IndiceCopy
}

// PUBLIC_INTERFACE
/** AssetsRepository exposes local asset paths or URLs for images. */
interface AssetsRepository {
    fun getInstructionImages(): InstructionImages
}

// PUBLIC_INTERFACE
/** RatingRepository provides like/liked states and allows toggling. */
interface RatingRepository {
    suspend fun isLiked(contentId: String): Boolean
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

/**
 * Mock implementations - zero dependency offline mode.
 * These return bundled assets and static strings.
 */
class MockContentRepository : ContentRepository {
    override suspend fun getHomeSections(): List<String> {
        delay(50) // Simulate quick fetch
        return listOf("Prototipo 1", "Prototipo 2", "Prototipo 3")
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

    override suspend fun isLiked(contentId: String): Boolean {
        return likes[contentId] ?: false
    }

    override suspend fun setLike(contentId: String, liked: Boolean): Boolean {
        likes[contentId] = liked
        return true
    }
}
