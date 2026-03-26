package com.rsav.githubPublicRepoBrowser.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.util.L

/**
 * PagingSource that paginates over a pre-fetched list of cached repos.
 * Supports pull-to-refresh (invalidation creates a new instance with fresh data).
 */
class CachedRepoPagingSource(
    private val loader: suspend () -> List<Repo>,
) : PagingSource<Int, Repo>() {

    private var cachedList: List<Repo>? = null

    private suspend fun getList(): List<Repo> {
        return cachedList ?: loader().also { cachedList = it }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return try {
            val allRepos = getList()
            val page = params.key ?: 0
            val fromIndex = page * params.loadSize
            val toIndex = minOf(fromIndex + params.loadSize, allRepos.size)

            if (fromIndex >= allRepos.size) {
                L.d(TAG, "load page=$page — beyond end (${allRepos.size} total)")
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val slice = allRepos.subList(fromIndex, toIndex)
            val nextKey = if (toIndex < allRepos.size) page + 1 else null
            val prevKey = if (page > 0) page - 1 else null

            L.d(TAG, "load page=$page — ${slice.size} repos [$fromIndex..$toIndex of ${allRepos.size}], nextKey=$nextKey")
            LoadResult.Page(data = slice, prevKey = prevKey, nextKey = nextKey)
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
        private const val TAG = "CachedPagingSource"
    }
}
