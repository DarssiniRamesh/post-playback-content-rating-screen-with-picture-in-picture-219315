package com.example.post_playback_rating_frontend.data.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Likes endpoints (scaffold).
 */
interface LikesApi {

    // PUBLIC_INTERFACE
    /** Returns like state for a content id. */
    @GET("v1/likes/{id}")
    suspend fun isLiked(
        @Path("id") contentId: String
    ): NetworkModels.NetworkLikeState

    // PUBLIC_INTERFACE
    /** Sets like state for a content id. */
    @POST("v1/likes/{id}")
    suspend fun setLike(
        @Path("id") contentId: String,
        @Query("liked") liked: Boolean
    ): NetworkModels.NetworkLikeState
}
