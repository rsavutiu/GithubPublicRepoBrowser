package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.data.remote.cached.CacheUnavailableException
import com.rsav.githubPublicRepoBrowser.domain.model.ApiMode
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DelegatingSearchRepository @Inject constructor(
    private val graphQlImpl: SearchRepositoriesImpl,
    private val restImpl: RestSearchRepositoriesImpl,
    private val cachedImpl: CachedSearchRepositoriesImpl,
    private val apiModeRepository: ApiModeRepository,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        val mode = apiModeRepository.apiMode.value
        L.d(TAG, "searchRepositories via $mode (cacheable=${CachedSearchRepositoriesImpl.isCacheable(query)})")

        // For cacheable queries, try the static cache first, then fall back to live API
        if (CachedSearchRepositoriesImpl.isCacheable(query)) {
            return flow {
                emitAll(
                    cachedImpl.searchRepositories(query)
                        .catch { e ->
                            if (e is CacheUnavailableException) {
                                L.w(TAG, "Cache unavailable, falling back to $mode: ${e.message}")
                                emitAll(liveSearch(mode, query))
                            } else {
                                throw e
                            }
                        }
                )
            }
        }

        return liveSearch(mode, query)
    }

    private fun liveSearch(mode: ApiMode, query: String): Flow<PagingData<Repo>> =
        when (mode) {
            ApiMode.GRAPHQL -> graphQlImpl.searchRepositories(query)
            ApiMode.REST -> restImpl.searchRepositories(query)
        }

    companion object {
        private const val TAG = "DelegatingSearch"
    }
}
