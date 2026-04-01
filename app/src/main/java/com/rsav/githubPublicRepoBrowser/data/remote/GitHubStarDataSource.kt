package com.rsav.githubPublicRepoBrowser.data.remote

import com.rsav.githubPublicRepoBrowser.data.auth.IGitHubAuthManager
import com.rsav.githubPublicRepoBrowser.di.IoDispatcher
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

sealed interface StarResult {
    data class Success(val isStarred: Boolean) : StarResult
    data object AuthRequired : StarResult
    data object Failed : StarResult
}

interface IGitHubStarDataSource {
    suspend fun isStarred(owner: String, repo: String): Boolean?
    suspend fun star(owner: String, repo: String): StarResult
    suspend fun unstar(owner: String, repo: String): StarResult
    suspend fun toggleStar(owner: String, repo: String): StarResult
}

@Singleton
class GitHubStarDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val authManager: IGitHubAuthManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IGitHubStarDataSource {
    private val starredCache = ConcurrentHashMap<String, Boolean>()
    private val fetchMutex = Mutex()

    override suspend fun isStarred(owner: String, repo: String): Boolean? = withContext(ioDispatcher) {
        val key = "$owner/$repo"
        starredCache[key]?.let { return@withContext it }

        // Mutex prevents duplicate API calls when multiple coroutines check the same uncached key
        fetchMutex.withLock {
            starredCache[key]?.let { return@withContext it }

            authManager.getAccessToken() ?: return@withContext null
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/user/starred/$owner/$repo")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val starred = response.code == 204 // 204 = starred, 404 = not starred
                starredCache[key] = starred
                L.d(TAG, "isStarred($key) = $starred")
                starred
            } catch (e: Exception) {
                L.e(TAG, "isStarred failed: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun star(owner: String, repo: String): StarResult = withContext(ioDispatcher) {
        val key = "$owner/$repo"
        authManager.getAccessToken() ?: return@withContext StarResult.AuthRequired
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user/starred/$owner/$repo")
                .addHeader("Content-Length", "0")
                .put(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.code == 401) return@withContext StarResult.AuthRequired
            val success = response.code == 204
            if (success) starredCache[key] = true
            L.d(TAG, "star($key) = $success (${response.code})")
            if (success) StarResult.Success(true) else StarResult.Failed
        } catch (e: Exception) {
            L.e(TAG, "star failed: ${e.message}", e)
            StarResult.Failed
        }
    }

    override suspend fun unstar(owner: String, repo: String): StarResult = withContext(ioDispatcher) {
        val key = "$owner/$repo"
        authManager.getAccessToken() ?: return@withContext StarResult.AuthRequired
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user/starred/$owner/$repo")
                .delete()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.code == 401) return@withContext StarResult.AuthRequired
            val success = response.code == 204
            if (success) starredCache[key] = false
            L.d(TAG, "unstar($key) = $success (${response.code})")
            if (success) StarResult.Success(false) else StarResult.Failed
        } catch (e: Exception) {
            L.e(TAG, "unstar failed: ${e.message}", e)
            StarResult.Failed
        }
    }

    override suspend fun toggleStar(owner: String, repo: String): StarResult = withContext(ioDispatcher) {
        val currentlyStarred = isStarred(owner, repo) ?: return@withContext StarResult.AuthRequired
        if (currentlyStarred) unstar(owner, repo) else star(owner, repo)
    }

    companion object {
        private const val TAG = "StarDataSource"
    }
}
