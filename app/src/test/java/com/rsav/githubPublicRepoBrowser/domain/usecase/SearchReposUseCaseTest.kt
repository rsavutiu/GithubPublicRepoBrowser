package com.rsav.githubPublicRepoBrowser.domain.usecase

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.RepoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchReposUseCaseTest {

    private lateinit var repository: RepoRepository
    private lateinit var useCase: SearchReposUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchReposUseCase(repository)
    }

    @Test
    fun `blank query uses default query`() = runTest {
        coEvery { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY, any()) } returns Result.success(emptyList())

        val result = useCase("")

        assertTrue(result.isSuccess)
        coVerify { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY, any()) }
    }

    @Test
    fun `whitespace-only query uses default query`() = runTest {
        coEvery { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY, any()) } returns Result.success(emptyList())

        val result = useCase("   ")

        assertTrue(result.isSuccess)
        coVerify { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY, any()) }
    }

    @Test
    fun `query is trimmed before delegating to repository`() = runTest {
        coEvery { repository.searchRepositories("kotlin", any()) } returns Result.success(emptyList())

        useCase("  kotlin  ")

        coVerify { repository.searchRepositories("kotlin", any()) }
    }

    @Test
    fun `successful search returns repos from repository`() = runTest {
        val expectedRepos = listOf(createTestRepo("1"))
        coEvery { repository.searchRepositories("kotlin", any()) } returns Result.success(expectedRepos)

        val result = useCase("kotlin")

        assertTrue(result.isSuccess)
        assertEquals(expectedRepos, result.getOrNull())
    }

    @Test
    fun `repository failure propagates to caller`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { repository.searchRepositories("kotlin", any()) } returns Result.failure(exception)

        val result = useCase("kotlin")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    private fun createTestRepo(id: String) = Repo(
        id = id,
        name = "test-repo-$id",
        nameWithOwner = "owner/test-repo-$id",
        description = "A test repository",
        url = "https://github.com/owner/test-repo-$id",
        stargazerCount = 100,
        forkCount = 20,
        languageName = "Kotlin",
        languageColor = "#A97BFF",
        ownerLogin = "owner",
        ownerAvatarUrl = "https://github.com/owner.png",
    )
}
