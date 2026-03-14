package com.rsav.githubPublicRepoBrowser.data.paging

import androidx.paging.PagingSource
import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloQueryException
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepoPagingSourceTest {

    private lateinit var dataSource: ApolloRepoDataSource
    private lateinit var pagingSource: RepoPagingSource

    @Before
    fun setUp() {
        dataSource = mockk()
        pagingSource = RepoPagingSource(dataSource, "kotlin")
    }

    @Test
    fun `first page load returns repos with nextKey`() = runTest {
        val queryData = createTestData(
            repos = listOf(createOnRepository("1", "repo-1")),
            endCursor = "cursor1",
            hasNextPage = true,
        )
        coEvery { dataSource.searchRepositories("kotlin", 20, null) } returns queryData

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("repo-1", page.data[0].name)
        assertNull(page.prevKey)
        assertEquals("cursor1", page.nextKey)
    }

    @Test
    fun `next page uses cursor from previous page`() = runTest {
        val queryData = createTestData(
            repos = listOf(createOnRepository("2", "repo-2")),
            endCursor = "cursor2",
            hasNextPage = true,
        )
        coEvery { dataSource.searchRepositories("kotlin", 20, "cursor1") } returns queryData

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = "cursor1", loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals("repo-2", page.data[0].name)
        assertEquals("cursor2", page.nextKey)
    }

    @Test
    fun `last page returns null nextKey`() = runTest {
        val queryData = createTestData(
            repos = listOf(createOnRepository("3", "repo-3")),
            endCursor = null,
            hasNextPage = false,
        )
        coEvery { dataSource.searchRepositories("kotlin", 20, "cursor2") } returns queryData

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = "cursor2", loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
    }

    @Test
    fun `null nodes are filtered out`() = runTest {
        val queryData = SearchRepositoriesQuery.Data(
            search = SearchRepositoriesQuery.Search(
                repositoryCount = 0,
                pageInfo = SearchRepositoriesQuery.PageInfo(
                    endCursor = null,
                    hasNextPage = false,
                ),
                nodes = listOf(null),
            ),
        )
        coEvery { dataSource.searchRepositories("kotlin", 20, null) } returns queryData

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(0, (result as PagingSource.LoadResult.Page).data.size)
    }

    @Test
    fun `exception returns LoadResult Error`() = runTest {
        coEvery {
            dataSource.searchRepositories("kotlin", 20, null)
        } throws ApolloQueryException("GraphQL error")

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("GraphQL error", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun `getRefreshKey returns null`() {
        assertNull(pagingSource.getRefreshKey(mockk(relaxed = true)))
    }

    private fun createTestData(
        repos: List<SearchRepositoriesQuery.OnRepository>,
        endCursor: String?,
        hasNextPage: Boolean,
    ): SearchRepositoriesQuery.Data {
        val nodes = repos.map { onRepo ->
            SearchRepositoriesQuery.Node(
                __typename = "Repository",
                onRepository = onRepo,
            )
        }
        return SearchRepositoriesQuery.Data(
            search = SearchRepositoriesQuery.Search(
                repositoryCount = repos.size,
                pageInfo = SearchRepositoriesQuery.PageInfo(
                    endCursor = endCursor,
                    hasNextPage = hasNextPage,
                ),
                nodes = nodes,
            ),
        )
    }

    private fun createOnRepository(id: String, name: String): SearchRepositoriesQuery.OnRepository {
        return SearchRepositoriesQuery.OnRepository(
            id = id,
            name = name,
            nameWithOwner = "owner/$name",
            description = "A test repo",
            url = "https://github.com/owner/$name",
            stargazerCount = 100,
            forkCount = 10,
            primaryLanguage = SearchRepositoriesQuery.PrimaryLanguage(
                name = "Kotlin",
                color = "#A97BFF",
            ),
            owner = SearchRepositoriesQuery.Owner(
                login = "owner",
                avatarUrl = "https://github.com/owner.png",
                __typename = "Organization"
            ),
            createdAt = "2020-01-15T10:30:00Z",
            updatedAt = "2024-01-15T10:30:00Z",
        )
    }
}
