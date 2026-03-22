package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
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
    fun `blank query produces trending query with date and sort`() {
        every { repository.searchRepositories(any()) } returns flowOf(PagingData.empty())

        useCase(freeText = "")

        verify { repository.searchRepositories(match { it.contains("stars:>5") && it.contains("created:>") && it.contains("sort:stars") }) }
    }

    @Test
    fun `non-blank free text is included in query`() {
        every { repository.searchRepositories(any()) } returns flowOf(PagingData.empty())

        useCase(freeText = "android")

        verify { repository.searchRepositories(match { it.contains("android") && it.contains("sort:stars") }) }
    }

    @Test
    fun `programming language adds language qualifier`() {
        every { repository.searchRepositories(any()) } returns flowOf(PagingData.empty())

        useCase(freeText = "", programmingLanguage = ProgrammingLanguage("Kotlin"))

        verify { repository.searchRepositories(match { it.contains("language:Kotlin") }) }
    }

    @Test
    fun `buildQuery combines all filters`() {
        val query = SearchReposUseCase.buildQuery(
            freeText = "server",
            trendingPeriod = TrendingPeriod.THIS_WEEK,
            programmingLanguage = ProgrammingLanguage("Go"),
            spokenLanguage = null,
        )

        assert(query.contains("server"))
        assert(query.contains("language:Go"))
        assert(query.contains("sort:stars"))
        // Free text present → no date filter
        assert(!query.contains("created:>"))
    }

    @Test
    fun `buildQuery without free text includes date filter`() {
        val query = SearchReposUseCase.buildQuery(
            freeText = "",
            trendingPeriod = TrendingPeriod.TODAY,
            programmingLanguage = null,
            spokenLanguage = null,
        )

        assert(query.contains("stars:>5"))
        assert(query.contains("created:>"))
        assert(query.contains("sort:stars"))
    }
}
