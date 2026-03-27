package com.rsav.githubPublicRepoBrowser.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.data.mapper.toDomainModel
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Functional interface for fetching search results.
 * Decouples the PagingSource from the concrete [ApolloRepoDataSource].
 */
fun interface RepoSearchFunction {
    suspend fun searchRepositories(
        query: String,
        first: Int,
        after: String?,
    ): SearchRepositoriesQuery.Data
}

class RepoPagingSource(
    private val searchFn: RepoSearchFunction,
    private val query: String,
) : PagingSource<String, Repo>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Repo> {
        val cursor = params.key
        L.d(TAG, "load(query=$query, loadSize=${params.loadSize}, cursor=$cursor)")
        return try {
            withContext(Dispatchers.IO) {
                val data = searchFn.searchRepositories(
                    query = query,
                    first = params.loadSize,
                    after = cursor,
                )

                val repos = data.search.nodes?.mapNotNull { node -> node?.onRepository?.toDomainModel() } ?: emptyList()
                val pageInfo = data.search.pageInfo
                val nextKey = if (pageInfo.hasNextPage) pageInfo.endCursor else null

                L.d(TAG, "load OK — ${repos.size} repos, nextKey=$nextKey")
                return@withContext LoadResult.Page(
                    data = repos,
                    prevKey = null,
                    nextKey = nextKey,
                )
            }
        } catch (e: Exception) {
            L.e(TAG, "load FAILED: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Repo>): String? {
        L.d(TAG, "getRefreshKey — returning null")
        return null
    }

    companion object {
        private const val TAG = "PagingSource"
    }
}
