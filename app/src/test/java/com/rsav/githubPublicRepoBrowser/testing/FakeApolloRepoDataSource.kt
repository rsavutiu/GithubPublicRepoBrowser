package com.rsav.githubPublicRepoBrowser.testing

import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery

/**
 * Fake data source for [RepoPagingSource] tests.
 * Map query keys to responses, or set [exception] to simulate errors.
 */
class FakeApolloRepoDataSource {

    /** Keyed by "$query|$first|$after" */
    private val responses = mutableMapOf<String, SearchRepositoriesQuery.Data>()

    /** If set, [searchRepositories] throws this instead of returning a response. */
    var exception: Exception? = null

    fun enqueue(query: String, first: Int, after: String?, data: SearchRepositoriesQuery.Data) {
        responses["$query|$first|$after"] = data
    }

    suspend fun searchRepositories(
        query: String,
        first: Int,
        after: String?,
    ): SearchRepositoriesQuery.Data {
        exception?.let { throw it }
        return responses["$query|$first|$after"]
            ?: error("No response enqueued for query=$query, first=$first, after=$after")
    }
}
