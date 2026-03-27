package com.rsav.githubPublicRepoBrowser.data.remote.cached

import com.rsav.githubPublicRepoBrowser.di.IoDispatcher
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.CoroutineDispatcher
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getIndex(): CachedIndexResponse = withContext(ioDispatcher) {
        L.d(TAG, "getIndex()")
        fetchAndDecode("${BASE_URL}index.json")
    }

    suspend fun getTrending(period: String): CachedTrendingResponse = withContext(ioDispatcher) {
        L.d(TAG, "getTrending(period=$period)")
        fetchAndDecode("${BASE_URL}trending-$period.json")
    }

    suspend fun getTopicRepos(topic: String): CachedTrendingResponse = withContext(ioDispatcher) {
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
        const val BASE_URL = "https://rsavutiu.github.io/github-trending-cache/"
    }
}

/** Thrown when the cache is unreachable or returns an error. Signals fallback to live API. */
class CacheUnavailableException(message: String) : Exception(message)

/** Provides the list of available topics from the cache index. */
fun interface IAvailableTopicsProvider {
    suspend fun getAvailableTopics(): List<String>
}
