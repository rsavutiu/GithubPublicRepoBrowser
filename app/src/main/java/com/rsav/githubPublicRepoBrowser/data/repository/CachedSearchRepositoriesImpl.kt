package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.data.remote.cached.CachedRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Serves search results from the pre-cached GitHub Pages JSON.
 * Returns a single-page [PagingData] since the cached data is a flat list (≤30 repos).
 *
 * This implementation parses the query string built by [SearchReposUseCase] to determine
 * which cached JSON file to fetch (period-based or topic-based).
 */
class CachedSearchRepositoriesImpl @Inject constructor(
    private val dataSource: CachedRepoDataSource,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> = flow {
        L.d(TAG, "searchRepositories(query=$query)")
        val repos = fetchFromCache(query)
        emit(PagingData.from(repos))
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
        private val TOPIC_REGEX = Regex("""topic:(\S+)""")
        private val CREATED_REGEX = Regex("""created:>(\S+)""")

        /**
         * Determines the cache period name from the query's `created:>` date.
         * Compares the date offset to [TrendingPeriod] thresholds.
         */
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

        /**
         * Returns true if the query can be served from the cache:
         * - Must be a default trending query (stars:>5 + period) or a topic query
         * - Must NOT have free-text search terms
         */
        fun isCacheable(query: String): Boolean {
            // Cache handles: "stars:>5 created:>{date} sort:stars" and "topic:X stars:>5 ..."
            // Not cacheable if it has programming language filters or spoken language keywords
            // that aren't part of the standard pattern
            val hasStarsFilter = "stars:>" in query
            val hasFreeText = query.replace(TOPIC_REGEX, "")
                .replace(CREATED_REGEX, "")
                .replace("stars:>5", "")
                .replace("sort:stars", "")
                .replace(Regex("""language:\S+"""), "")
                .trim()
                .isNotEmpty()

            // Language-filtered queries aren't cached (we only cache per-topic and per-period)
            val hasLanguageFilter = Regex("""language:\S+""").containsMatchIn(query)

            return hasStarsFilter && !hasFreeText && !hasLanguageFilter
        }
    }
}
