package com.rsav.githubPublicRepoBrowser.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rsav.githubPublicRepoBrowser.data.mapper.toDomainModel
import com.rsav.githubPublicRepoBrowser.data.remote.rest.RestRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.util.L

class RestRepoPagingSource(
    private val dataSource: RestRepoDataSource,
    private val query: String,
) : PagingSource<Int, Repo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        val page = params.key ?: 1
        L.d(TAG, "load(query=$query, loadSize=${params.loadSize}, page=$page)")
        return try {
            val response = dataSource.searchRepositories(
                query = query,
                perPage = params.loadSize,
                page = page,
            )

            val repos = response.items.map { it.toDomainModel() }
            // GitHub REST search caps at 1000 results; use item count as primary signal
            val hasMore = repos.size == params.loadSize
            val nextKey = if (hasMore) page + 1 else null

            L.d(TAG, "load OK — ${repos.size} repos, nextKey=$nextKey, total=${response.totalCount}")
            LoadResult.Page(
                data = repos,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            L.e(TAG, "load FAILED: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    companion object {
        private const val TAG = "RestPagingSource"
    }
}
