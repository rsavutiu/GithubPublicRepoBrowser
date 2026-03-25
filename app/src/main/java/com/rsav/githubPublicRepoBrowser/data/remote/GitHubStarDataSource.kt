package com.rsav.githubPublicRepoBrowser.data.remote

import com.rsav.githubPublicRepoBrowser.data.auth.GitHubAuthManager
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubStarDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val authManager: GitHubAuthManager,
) {
    private val starredCache = ConcurrentHashMap<String, Boolean>()

    suspend fun isStarred(owner: String, repo: String): Boolean? = withContext(Dispatchers.IO) {
        val key = "$owner/$repo"
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

    suspend fun star(owner: String, repo: String): Boolean = withContext(Dispatchers.IO) {
        val key = "$owner/$repo"
        authManager.getAccessToken() ?: return@withContext false
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user/starred/$owner/$repo")
                .addHeader("Content-Length", "0")
                .put(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val success = response.code == 204
            if (success) starredCache[key] = true
            L.d(TAG, "star($key) = $success (${response.code})")
            success
        } catch (e: Exception) {
            L.e(TAG, "star failed: ${e.message}", e)
            false
        }
    }

    suspend fun unstar(owner: String, repo: String): Boolean = withContext(Dispatchers.IO) {
        val key = "$owner/$repo"
        authManager.getAccessToken() ?: return@withContext false
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user/starred/$owner/$repo")
                .delete()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val success = response.code == 204
            if (success) starredCache[key] = false
            L.d(TAG, "unstar($key) = $success (${response.code})")
            success
        } catch (e: Exception) {
            L.e(TAG, "unstar failed: ${e.message}", e)
            false
        }
    }

    suspend fun toggleStar(owner: String, repo: String): Boolean? = withContext(Dispatchers.IO) {
        val currentlyStarred = isStarred(owner, repo) ?: return@withContext null
        if (currentlyStarred) {
            if (unstar(owner, repo)) false else null
        } else {
            if (star(owner, repo)) true else null
        }
    }

    companion object {
        private const val TAG = "StarDataSource"
    }
}
