package com.rsav.githubPublicRepoBrowser.data.remote

import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SparklineDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private val cache = ConcurrentHashMap<String, List<Int>>()

    suspend fun getWeeklyCommits(owner: String, repo: String): List<Int> {
        val key = "$owner/$repo"
        cache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/$key/stats/participation")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    L.w(TAG, "Failed to fetch sparkline for $key: ${response.code}")
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val allArray = json.getJSONArray("all")
                val weeks = (0 until allArray.length()).map { allArray.getInt(it) }
                cache[key] = weeks
                L.d(TAG, "Sparkline for $key: ${weeks.size} weeks")
                weeks
            } catch (e: Exception) {
                L.w(TAG, "Error fetching sparkline for $key: ${e.message}")
                emptyList()
            }
        }
    }

    companion object {
        private const val TAG = "SparklineDS"
    }
}
