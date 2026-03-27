package com.rsav.githubPublicRepoBrowser.testing

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake [ISearchRepositories] that records queries and returns configurable results.
 */
class FakeSearchRepositories : ISearchRepositories {

    /** All queries that were passed to [searchRepositories]. */
    val recordedQueries = mutableListOf<String>()

    /** The result to return from [searchRepositories]. Override before calling. */
    var result: Flow<PagingData<Repo>> = flowOf(PagingData.from(emptyList()))

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        recordedQueries.add(query)
        return result
    }
}
