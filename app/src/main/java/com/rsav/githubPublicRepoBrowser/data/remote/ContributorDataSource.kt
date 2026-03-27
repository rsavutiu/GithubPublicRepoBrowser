package com.rsav.githubPublicRepoBrowser.data.remote

import com.rsav.githubPublicRepoBrowser.di.IoDispatcher
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** Abstraction so the ViewModel can be tested with a fake. */
interface IContributorDataSource {
    suspend fun getContributorCount(owner: String, repo: String): Int?
}

@Singleton
class ContributorDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IContributorDataSource {
    private val cache = ConcurrentHashMap<String, Int>()

    /**
     * Gets the contributor count for a repo.
     * Uses `per_page=1&anon=true` and reads the `Link` header's last page number
     * to get the total count without fetching all contributors.
     */
    override suspend fun getContributorCount(owner: String, repo: String): Int? {
        val key = "$owner/$repo"
        cache[key]?.let { return it }

        return withContext(ioDispatcher) {
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/$key/contributors?per_page=1&anon=true")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    L.w(TAG, "Failed to fetch contributors for $key: ${response.code}")
                    return@withContext null
                }

                // Parse Link header to get total count
                // Format: <url?page=2>; rel="next", <url?page=423>; rel="last"
                val linkHeader = response.header("Link")
                val count = if (linkHeader != null) {
                    val lastMatch = Regex("""[?&]page=(\d+)>;\s*rel="last"""").find(linkHeader)
                    lastMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                } else {
                    // No Link header — single page, count the items
                    val body = response.body?.string() ?: "[]"
                    val array = org.json.JSONArray(body)
                    array.length()
                }

                cache[key] = count
                L.d(TAG, "Contributors for $key: $count")
                count
            } catch (e: Exception) {
                L.w(TAG, "Error fetching contributors for $key: ${e.message}")
                null
            }
        }
    }

    companion object {
        private const val TAG = "ContributorDS"
    }
}
