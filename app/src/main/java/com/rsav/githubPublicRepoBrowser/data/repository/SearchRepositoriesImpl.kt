package com.rsav.githubPublicRepoBrowser.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.data.paging.RepoPagingSource
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRepositoriesImpl @Inject constructor(
    private val dataSource: ApolloRepoDataSource,
) : ISearchRepositories {

    override fun searchRepositories(query: String): Flow<PagingData<Repo>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { RepoPagingSource(dataSource, query) },
        ).flow
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
