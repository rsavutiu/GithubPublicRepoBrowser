package com.rsav.githubPublicRepoBrowser.data.remote

import com.rsav.githubPublicRepoBrowser.di.IoDispatcher
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface ISparklineDataSource {
    suspend fun getWeeklyCommits(owner: String, repo: String): List<Int>
}

@Singleton
class SparklineDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ISparklineDataSource {
    private val cache = ConcurrentHashMap<String, List<Int>>()

    override suspend fun getWeeklyCommits(owner: String, repo: String): List<Int> {
        val key = "$owner/$repo"
        cache[key]?.let { return it }

        return withContext(ioDispatcher) {
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
                val allWeeks = (0 until allArray.length()).map { allArray.getInt(it) }
                // Trim leading zero-weeks, keeping 2 for context before first activity
                val firstNonZero = allWeeks.indexOfFirst { it > 0 }
                val weeks = if (firstNonZero <= 0) {
                    allWeeks
                } else {
                    allWeeks.drop((firstNonZero - 2).coerceAtLeast(0))
                }
                cache[key] = weeks
                L.d(TAG, "Sparkline for $key: ${allWeeks.size} total weeks, ${weeks.size} after trim")
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
