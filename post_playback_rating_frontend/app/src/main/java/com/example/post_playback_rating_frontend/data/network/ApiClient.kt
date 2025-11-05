package com.example.post_playback_rating_frontend.data.network

import android.util.Log
import com.example.post_playback_rating_frontend.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API client builder with safe defaults.
 * - Placeholder base URLs; do not require secrets.
 * - 5–10s timeouts.
 * - Logging in debug only.
 * - Adds basic headers and user-agent.
 *
 * If base URL is invalid/blank, callers should handle by falling back to mocks.
 */
object ApiClient {

    private const val TAG = "ApiClient"

    // PUBLIC_INTERFACE
    /** NetworkConfig holds per-service base URLs extracted from BuildConfig or defaults. */
    data class NetworkConfig(
        val contentBaseUrl: String,
        val metadataBaseUrl: String,
        val assetsBaseUrl: String,
        val likesBaseUrl: String,
    ) {
        companion object {
            // PUBLIC_INTERFACE
            /** Builds NetworkConfig from BuildConfig fields or placeholder defaults. */
            fun fromBuildConfig(): NetworkConfig {
                // Defaults are placeholders; keep inert until real endpoints provided.
                val content = safeBuildConfigString("CONTENT_BASE_URL") ?: "https://placeholder.invalid/"
                val metadata = safeBuildConfigString("METADATA_BASE_URL") ?: "https://placeholder.invalid/"
                val assets = safeBuildConfigString("ASSETS_BASE_URL") ?: "https://placeholder.invalid/"
                val likes = safeBuildConfigString("LIKES_BASE_URL") ?: "https://placeholder.invalid/"
                return NetworkConfig(content, metadata, assets, likes)
            }

            private fun safeBuildConfigString(field: String): String? {
                return try {
                    val clazz = Class.forName("com.example.post_playback_rating_frontend.BuildConfig")
                    val f = clazz.getField(field)
                    (f.get(null) as? String)?.trim()?.ifEmpty { null }
                } catch (_: Throwable) {
                    null
                }
            }
        }

        // PUBLIC_INTERFACE
        /** True if any base URL is blank or equals placeholder.invalid; prefer mocks in that case. */
        fun isMissingOrPlaceholder(): Boolean {
            fun invalid(u: String) = u.isBlank() || u.contains("placeholder.invalid")
            return invalid(contentBaseUrl) || invalid(metadataBaseUrl) || invalid(assetsBaseUrl) || invalid(likesBaseUrl)
        }
    }

    private fun baseOkHttpClient(): OkHttpClient {
        val userAgentInterceptor = Interceptor { chain ->
            val req = chain.request()
                .newBuilder()
                .header("User-Agent", "PostPlaybackRatingTV/1.0 (Android)")
                .build()
            chain.proceed(req)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .build()
    }

    // PUBLIC_INTERFACE
    /** Builds a Retrofit instance for the provided baseUrl. */
    fun retrofit(baseUrl: String): Retrofit {
        if (baseUrl.isBlank()) {
            Log.w(TAG, "Base URL is blank; network calls will fail. Ensure ServiceLocator falls back to mocks.")
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(baseOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun String.ensureTrailingSlash(): String =
        if (this.endsWith("/")) this else "$this/"
}
