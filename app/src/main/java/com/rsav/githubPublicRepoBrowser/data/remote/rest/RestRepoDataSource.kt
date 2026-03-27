package com.rsav.githubPublicRepoBrowser.data.remote.rest

import com.rsav.githubPublicRepoBrowser.di.IoDispatcher
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestRepoDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchRepositories(
        query: String,
        perPage: Int,
        page: Int,
    ): RestSearchResponse = withContext(ioDispatcher) {
        L.d(TAG, "searchRepositories(query=$query, perPage=$perPage, page=$page)")

        // Extract "sort:stars" from the query — REST API needs it as a separate param
        val cleanQuery = query.replace("sort:stars", "").trim()
        val url = "https://api.github.com/search/repositories".toHttpUrl().newBuilder()
            .addQueryParameter("q", cleanQuery)
            .addQueryParameter("sort", "stars")
            .addQueryParameter("order", "desc")
            .addQueryParameter("per_page", perPage.toString())
            .addQueryParameter("page", page.toString())
            .build()
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RestApiException("Search failed: ${response.code} ${response.message}")
        }

        val body = response.body?.string() ?: throw RestApiException("Empty response body")
        json.decodeFromString<RestSearchResponse>(body)
    }

    suspend fun getReadme(owner: String, repo: String): String? = withContext(ioDispatcher) {
        L.d(TAG, "getReadme(owner=$owner, repo=$repo)")

        // Use the raw content endpoint — returns README as plain text
        val url = "https://api.github.com/repos/$owner/$repo/readme"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github.raw+json")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            L.w(TAG, "README fetch failed: ${response.code} for $owner/$repo")
            return@withContext null
        }

        response.body?.string()
    }

    suspend fun getUserProfile(login: String): RestUserProfile = withContext(ioDispatcher) {
        L.d(TAG, "getUserProfile(login=$login)")

        val url = "https://api.github.com/users/$login"
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RestApiException("User fetch failed: ${response.code} ${response.message}")
        }

        val body = response.body?.string() ?: throw RestApiException("Empty response body")
        json.decodeFromString<RestUserProfile>(body)
    }

    companion object {
        private const val TAG = "RestDataSource"
    }
}

class RestApiException(message: String) : Exception(message)
