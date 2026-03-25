package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.ApiMode
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DelegatingSearchRepository @Inject constructor(
    private val graphQlImpl: SearchRepositoriesImpl,
    private val restImpl: RestSearchRepositoriesImpl,
    private val apiModeRepository: ApiModeRepository,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        val mode = apiModeRepository.apiMode.value
        L.d(TAG, "searchRepositories via $mode")
        return when (mode) {
            ApiMode.GRAPHQL -> graphQlImpl.searchRepositories(query)
            ApiMode.REST -> restImpl.searchRepositories(query)
        }
    }

    companion object {
        private const val TAG = "DelegatingSearch"
    }
}
