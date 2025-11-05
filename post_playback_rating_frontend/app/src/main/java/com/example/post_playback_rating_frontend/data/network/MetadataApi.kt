package com.example.post_playback_rating_frontend.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Metadata endpoints for copy and rating settings (scaffold).
 */
interface MetadataApi {

    // PUBLIC_INTERFACE
    /** Returns copy/text for Índice. */
    @GET("v1/metadata/indice")
    suspend fun getIndiceCopy(
        @Query("locale") locale: String? = null
    ): NetworkModels.NetworkIndiceCopy

    // PUBLIC_INTERFACE
    /** Returns rating overlay settings. */
    @GET("v1/metadata/vod-rating-settings")
    suspend fun getVodRatingSettings(
        @Query("locale") locale: String? = null
    ): NetworkModels.NetworkVodRatingSettings
}
