package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test

class SearchReposUseCaseTest {

    private lateinit var repository: ISearchRepositories
    private lateinit var useCase: SearchReposUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchReposUseCase(repository)
    }

    @Test
    fun `blank query uses default query`() {
        every { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY) } returns flowOf(PagingData.empty())

        useCase("")

        verify { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY) }
    }

    @Test
    fun `whitespace-only query uses default query`() {
        every { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY) } returns flowOf(PagingData.empty())

        useCase("   ")

        verify { repository.searchRepositories(SearchReposUseCase.DEFAULT_QUERY) }
    }

    @Test
    fun `query is trimmed before delegating to repository`() {
        every { repository.searchRepositories("kotlin") } returns flowOf(PagingData.empty())

        useCase("  kotlin  ")

        verify { repository.searchRepositories("kotlin") }
    }

    @Test
    fun `non-blank query delegates to repository`() {
        every { repository.searchRepositories("kotlin") } returns flowOf(PagingData.empty())

        useCase("kotlin")

        verify { repository.searchRepositories("kotlin") }
    }
}
