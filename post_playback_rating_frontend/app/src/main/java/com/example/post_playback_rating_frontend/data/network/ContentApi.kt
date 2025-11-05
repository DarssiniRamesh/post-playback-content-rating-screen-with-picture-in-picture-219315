package com.example.post_playback_rating_frontend.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Content-related API endpoints (scaffold).
 * Safe defaults, no secrets, aligns with repository interfaces.
 */
interface ContentApi {

    // PUBLIC_INTERFACE
    /** Returns list of section names for Home; placeholder endpoint. */
    @GET("v1/home/sections")
    suspend fun getHomeSections(
        @Query("locale") locale: String? = null
    ): List<String>

    // PUBLIC_INTERFACE
    /** Minimal content info for overlay. */
    @GET("v1/content/{id}/min")
    suspend fun getContentInfo(
        @Path("id") contentId: String,
        @Query("locale") locale: String? = null
    ): NetworkModels.NetworkContentInfo
}
