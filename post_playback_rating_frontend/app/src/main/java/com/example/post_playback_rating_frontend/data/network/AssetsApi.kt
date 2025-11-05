package com.example.post_playback_rating_frontend.data.network

import retrofit2.http.GET

/**
 * Assets endpoints to retrieve image URLs used in instructions (scaffold).
 */
interface AssetsApi {

    // PUBLIC_INTERFACE
    /** Returns bundle of assets or URLs for instruction images. */
    @GET("v1/assets/instructions")
    suspend fun getInstructionImages(): NetworkModels.NetworkInstructionImages
}
