package com.rsav.githubPublicRepoBrowser.domain.usecase

import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.testing.FakeSearchRepositories
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchReposUseCaseTest {

    private lateinit var repository: FakeSearchRepositories
    private lateinit var useCase: SearchReposUseCase

    @Before
    fun setUp() {
        repository = FakeSearchRepositories()
        useCase = SearchReposUseCase(repository)
    }

    @Test
    fun `blank query produces trending query with date and sort`() {
        useCase(freeText = "")

        val query = repository.recordedQueries.last()
        assertTrue("Expected stars:>5 in '$query'", query.contains("stars:>5"))
        assertTrue("Expected created:> in '$query'", query.contains("created:>"))
        assertTrue("Expected sort:stars in '$query'", query.contains("sort:stars"))
    }

    @Test
    fun `non-blank free text is included in query`() {
        useCase(freeText = "android")

        val query = repository.recordedQueries.last()
        assertTrue("Expected 'android' in '$query'", query.contains("android"))
        assertTrue("Expected sort:stars in '$query'", query.contains("sort:stars"))
    }

    @Test
    fun `programming language adds language qualifier`() {
        useCase(freeText = "", programmingLanguage = ProgrammingLanguage("Kotlin"))

        val query = repository.recordedQueries.last()
        assertTrue("Expected language:Kotlin in '$query'", query.contains("language:Kotlin"))
    }

    @Test
    fun `buildQuery combines all filters`() {
        val query = SearchReposUseCase.buildQuery(
            freeText = "server",
            trendingPeriod = TrendingPeriod.THIS_WEEK,
            programmingLanguage = ProgrammingLanguage("Go"),
            spokenLanguage = null,
        )

        assertTrue(query.contains("server"))
        assertTrue(query.contains("language:Go"))
        assertTrue(query.contains("sort:stars"))
        // Free text present → no date filter
        assertTrue(!query.contains("created:>"))
    }

    @Test
    fun `buildQuery without free text includes date filter`() {
        val query = SearchReposUseCase.buildQuery(
            freeText = "",
            trendingPeriod = TrendingPeriod.TODAY,
            programmingLanguage = null,
            spokenLanguage = null,
        )

        assertTrue(query.contains("stars:>5"))
        assertTrue(query.contains("created:>"))
        assertTrue(query.contains("sort:stars"))
    }
}
