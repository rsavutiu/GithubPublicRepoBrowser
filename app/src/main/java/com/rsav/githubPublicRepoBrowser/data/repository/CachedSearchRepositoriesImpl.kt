package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.data.paging.CachedRepoPagingSource
import com.rsav.githubPublicRepoBrowser.data.remote.cached.CachedRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Serves search results from the pre-cached GitHub Pages JSON via a proper [Pager],
 * supporting pagination and pull-to-refresh.
 */
class CachedSearchRepositoriesImpl @Inject constructor(
    private val dataSource: CachedRepoDataSource,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        L.d(TAG, "searchRepositories(query=$query)")
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PREFETCH_DISTANCE,
            ),
            pagingSourceFactory = {
                CachedRepoPagingSource { fetchFromCache(query) }
            },
        ).flow
    }

    private suspend fun fetchFromCache(query: String): List<Repo> {
        // Check if this is a topic query
        val topicMatch = TOPIC_REGEX.find(query)
        if (topicMatch != null) {
            val topic = topicMatch.groupValues[1]
            L.d(TAG, "Fetching cached topic: $topic")
            return dataSource.getTopicRepos(topic).repos
        }

        // Otherwise, determine the period from the created:> date in the query
        val period = detectPeriod(query)
        L.d(TAG, "Fetching cached trending: $period")
        return dataSource.getTrending(period).repos
    }

    companion object {
        private const val TAG = "CachedSearchRepo"
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 4
        private val TOPIC_REGEX = Regex("""topic:(\S+)""")
        private val CREATED_REGEX = Regex("""created:>(\S+)""")

        fun detectPeriod(query: String): String {
            val dateMatch = CREATED_REGEX.find(query) ?: return "weekly"
            val dateStr = dateMatch.groupValues[1]
            return try {
                val since = java.time.LocalDate.parse(dateStr)
                val daysBack = java.time.temporal.ChronoUnit.DAYS.between(since, java.time.LocalDate.now())
                when {
                    daysBack <= 1 -> "daily"
                    daysBack <= 7 -> "weekly"
                    daysBack <= 30 -> "monthly"
                    else -> "yearly"
                }
            } catch (_: Exception) {
                "weekly"
            }
        }

        fun isCacheable(query: String): Boolean {
            val hasStarsFilter = "stars:>" in query
            val hasFreeText = query.replace(TOPIC_REGEX, "")
                .replace(CREATED_REGEX, "")
                .replace("stars:>5", "")
                .replace("sort:stars", "")
                .replace(Regex("""language:\S+"""), "")
                .trim()
                .isNotEmpty()
            val hasLanguageFilter = Regex("""language:\S+""").containsMatchIn(query)
            return hasStarsFilter && !hasFreeText && !hasLanguageFilter
        }
    }
}
