package com.rsav.githubPublicRepoBrowser.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rsav.githubPublicRepoBrowser.data.mapper.toDomainModel
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo

class RepoPagingSource(
    private val dataSource: ApolloRepoDataSource,
    private val query: String,
) : PagingSource<String, Repo>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Repo> {
        return try {
            val cursor = params.key
            val data = dataSource.searchRepositories(
                query = query,
                first = params.loadSize,
                after = cursor,
            )

            val repos = data.search.nodes?.mapNotNull { node -> node?.onRepository?.toDomainModel() } ?: emptyList()

            val pageInfo = data.search.pageInfo

            LoadResult.Page(
                data = repos,
                prevKey = null,
                nextKey = if (pageInfo.hasNextPage) pageInfo.endCursor else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Repo>): String? {
        return null
    }
}
