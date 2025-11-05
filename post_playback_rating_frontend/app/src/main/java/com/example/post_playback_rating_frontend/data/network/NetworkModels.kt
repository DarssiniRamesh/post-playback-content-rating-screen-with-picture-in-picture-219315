package com.example.post_playback_rating_frontend.data.network

import com.example.post_playback_rating_frontend.data.ContentInfo
import com.example.post_playback_rating_frontend.data.IndiceCopy
import com.example.post_playback_rating_frontend.data.InstructionImages
import com.example.post_playback_rating_frontend.data.VodRatingSettings
import com.google.gson.annotations.SerializedName

/**
 * Network-layer DTOs and mappers to domain models.
 * These are intentionally minimal and align with current mock domain models.
 */
object NetworkModels {

    data class NetworkIndiceCopy(
        @SerializedName("title") val title: String? = null,
        @SerializedName("section1") val section1: String? = null,
        @SerializedName("section2") val section2: String? = null,
        @SerializedName("section3") val section3: String? = null,
        @SerializedName("cta1") val cta1: String? = null,
        @SerializedName("cta2") val cta2: String? = null,
        @SerializedName("cta3") val cta3: String? = null,
        @SerializedName("helper") val helper: String? = null
    )

    data class NetworkInstructionImages(
        @SerializedName("mainPreview") val mainPreview: String? = null,
        @SerializedName("stepAWindow") val stepAWindow: String? = null,
        @SerializedName("stepAZoom") val stepAZoom: String? = null,
        @SerializedName("panelTall") val panelTall: String? = null,
        @SerializedName("shortcutIcon") val shortcutIcon: String? = null,
        @SerializedName("overlaySquare") val overlaySquare: String? = null,
        @SerializedName("restartBar") val restartBar: String? = null
    )

    data class NetworkContentInfo(
        @SerializedName("id") val id: String? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("posterUrl") val posterUrl: String? = null
    )

    data class NetworkVodRatingSettings(
        @SerializedName("displayTimeSeconds") val displayTimeSeconds: Int? = null,
        @SerializedName("maxDisplayTimeSeconds") val maxDisplayTimeSeconds: Int? = null,
        @SerializedName("rollingCreditsTimeMs") val rollingCreditsTimeMs: Long? = null
    )

    data class NetworkLikeState(
        @SerializedName("liked") val liked: Boolean? = null
    )

    // Mapping helpers to domain with safe defaults

    fun NetworkIndiceCopy.toDomainSafe(): IndiceCopy = IndiceCopy(
        title = this.title ?: "Índice de prototipos",
        section1 = this.section1 ?: "Entrada y salida de calificador de contenidos",
        section2 = this.section2 ?: "Navegación por el Calificador de Contenidos",
        section3 = this.section3 ?: "Comportamiento de notificación al calificar el contenido.",
        cta1 = this.cta1 ?: "Click aquí para iniciar prototipo 1",
        cta2 = this.cta2 ?: "Click aquí para iniciar prototipo 2",
        cta3 = this.cta3 ?: "Click aquí para iniciar prototipo 3",
        helper = this.helper ?: "Da click en el botón amarillo para visualizar las interacciones descritas en el guión"
    )

    fun NetworkInstructionImages.toDomainSafe(): InstructionImages = InstructionImages(
        mainPreview = this.mainPreview ?: "file:///android_asset/figmaimages/figma_image_176_1015.png",
        stepAWindow = this.stepAWindow ?: "file:///android_asset/figmaimages/figma_image_176_1016.png",
        stepAZoom = this.stepAZoom ?: "file:///android_asset/figmaimages/figma_image_176_1018.png",
        panelTall = this.panelTall ?: "file:///android_asset/figmaimages/figma_image_176_1021.png",
        shortcutIcon = this.shortcutIcon ?: "file:///android_asset/figmaimages/figma_image_41_219.png",
        overlaySquare = this.overlaySquare ?: "file:///android_asset/figmaimages/figma_image_41_221.png",
        restartBar = this.restartBar ?: "file:///android_asset/figmaimages/figma_image_41_207.png",
    )

    fun NetworkContentInfo.toDomainSafe(): ContentInfo = ContentInfo(
        id = this.id ?: "content-unknown",
        title = this.title ?: "The Ocean and the Sky",
        posterUrl = this.posterUrl ?: "file:///android_asset/figmaimages/figma_image_176_1015.png"
    )

    fun NetworkVodRatingSettings.toDomainSafe(): VodRatingSettings = VodRatingSettings(
        displayTimeSeconds = this.displayTimeSeconds ?: 10,
        maxDisplayTimeSeconds = this.maxDisplayTimeSeconds ?: 60,
        rollingCreditsTimeMs = this.rollingCreditsTimeMs ?: 3000L
    )
}
