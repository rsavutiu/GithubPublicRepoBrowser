package com.rsav.githubPublicRepoBrowser.data.remote.cached

import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Fetches pre-cached trending data from the GitHub Pages static JSON backend.
 * Uses an unauthenticated OkHttpClient since GitHub Pages requires no auth.
 */
@Singleton
class CachedRepoDataSource @Inject constructor(
    @param:Named("unauthenticated") private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getIndex(): CachedIndexResponse = withContext(Dispatchers.IO) {
        L.d(TAG, "getIndex()")
        fetchAndDecode("${BASE_URL}index.json")
    }

    suspend fun getTrending(period: String): CachedTrendingResponse = withContext(Dispatchers.IO) {
        L.d(TAG, "getTrending(period=$period)")
        fetchAndDecode("${BASE_URL}trending-$period.json")
    }

    suspend fun getTopicRepos(topic: String): CachedTrendingResponse = withContext(Dispatchers.IO) {
        L.d(TAG, "getTopicRepos(topic=$topic)")
        fetchAndDecode("${BASE_URL}topics/$topic.json")
    }

    private inline fun <reified T> fetchAndDecode(url: String): T {
        L.d(TAG, "GET $url")
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw CacheUnavailableException("Cache fetch failed: ${response.code} $url")
        }

        val body = response.body?.string()
            ?: throw CacheUnavailableException("Empty response from $url")
        return json.decodeFromString(body)
    }

    companion object {
        private const val TAG = "CachedDataSource"
        // TODO: Update with actual GitHub Pages URL after repo creation
        const val BASE_URL = "https://rsavu.github.io/github-trending-cache/"
    }
}

/** Thrown when the cache is unreachable or returns an error. Signals fallback to live API. */
class CacheUnavailableException(message: String) : Exception(message)
