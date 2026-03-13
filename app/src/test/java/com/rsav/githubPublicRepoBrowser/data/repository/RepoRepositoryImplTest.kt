package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloQueryException
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepoRepositoryImplTest {

    private lateinit var dataSource: ApolloRepoDataSource
    private lateinit var repository: RepoRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = RepoRepositoryImpl(dataSource)
    }

    @Test
    fun `successful search maps data to domain models`() = runTest {
        val queryData = createTestData(
            repos = listOf(
                createOnRepository(
                    id = "123",
                    name = "test-repo",
                    nameWithOwner = "owner/test-repo",
                    description = "A test repository",
                    stargazerCount = 500,
                    forkCount = 100,
                    languageName = "Kotlin",
                    languageColor = "#A97BFF",
                    ownerLogin = "owner",
                ),
            ),
        )
        coEvery { dataSource.searchRepositories("kotlin", 20) } returns queryData

        val result = repository.searchRepositories("kotlin", 20)

        assertTrue(result.isSuccess)
        val repos = result.getOrNull()!!
        assertEquals(1, repos.size)
        assertEquals("123", repos[0].id)
        assertEquals("test-repo", repos[0].name)
        assertEquals("owner/test-repo", repos[0].nameWithOwner)
        assertEquals("A test repository", repos[0].description)
        assertEquals(500, repos[0].stargazerCount)
        assertEquals(100, repos[0].forkCount)
        assertEquals("Kotlin", repos[0].languageName)
        assertEquals("#A97BFF", repos[0].languageColor)
        assertEquals("owner", repos[0].ownerLogin)
    }

    @Test
    fun `null nodes are filtered out`() = runTest {
        val queryData = createTestDataWithNullNodes()
        coEvery { dataSource.searchRepositories("kotlin", 20) } returns queryData

        val result = repository.searchRepositories("kotlin", 20)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun `data source exception returns failure`() = runTest {
        coEvery {
            dataSource.searchRepositories("kotlin", 20)
        } throws ApolloQueryException("GraphQL error")

        val result = repository.searchRepositories("kotlin", 20)

        assertTrue(result.isFailure)
        assertEquals("GraphQL error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `network exception returns failure`() = runTest {
        coEvery {
            dataSource.searchRepositories("kotlin", 20)
        } throws RuntimeException("Network timeout")

        val result = repository.searchRepositories("kotlin", 20)

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }

    private fun createTestData(
        repos: List<SearchRepositoriesQuery.OnRepository>,
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
                nodes = nodes,
            ),
        )
    }

    private fun createTestDataWithNullNodes(): SearchRepositoriesQuery.Data {
        return SearchRepositoriesQuery.Data(
            search = SearchRepositoriesQuery.Search(
                repositoryCount = 0,
                nodes = listOf(null),
            ),
        )
    }

    private fun createOnRepository(
        id: String,
        name: String,
        nameWithOwner: String,
        description: String?,
        stargazerCount: Int,
        forkCount: Int,
        languageName: String?,
        languageColor: String?,
        ownerLogin: String,
    ): SearchRepositoriesQuery.OnRepository {
        return SearchRepositoriesQuery.OnRepository(
            id = id,
            name = name,
            nameWithOwner = nameWithOwner,
            description = description,
            url = "https://github.com/$nameWithOwner",
            stargazerCount = stargazerCount,
            forkCount = forkCount,
            primaryLanguage = if (languageName != null) {
                SearchRepositoriesQuery.PrimaryLanguage(
                    name = languageName,
                    color = languageColor,
                )
            } else {
                null
            },
            owner = SearchRepositoriesQuery.Owner(
                login = ownerLogin,
                avatarUrl = "https://github.com/$ownerLogin.png",
            ),
        )
    }
}
