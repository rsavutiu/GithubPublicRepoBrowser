package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.data.paging.RepoPagingSource
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoriesImpl @Inject constructor(
    private val dataSource: ApolloRepoDataSource,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        L.d(TAG, "searchRepositories(query=$query, pageSize=$PAGE_SIZE)")
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PREFETCH_DISTANCE,
            ),
            pagingSourceFactory = { RepoPagingSource(dataSource, query) },
        ).flow
    }

    companion object {
        private const val TAG = "SearchRepo"
        const val PAGE_SIZE = 10
        private const val INITIAL_LOAD_SIZE = 10
        private const val PREFETCH_DISTANCE = 4
    }
}
