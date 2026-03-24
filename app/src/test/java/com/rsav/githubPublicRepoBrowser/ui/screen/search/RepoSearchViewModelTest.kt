package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.paging.PagingData
import app.cash.turbine.test
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoSearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var searchReposUseCase: SearchReposUseCase
    private lateinit var viewModel: RepoSearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchReposUseCase = mockk()
        every { searchReposUseCase(any(), any(), any(), any()) } returns flowOf(PagingData.from(emptyList()))
        viewModel = RepoSearchViewModel(searchReposUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query and default trending period`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(TrendingPeriod.THIS_WEEK, state.trendingPeriod)
        assertNull(state.selectedLanguage)
        assertNull(state.selectedSpokenLanguage)
    }

    @Test
    fun `QueryChanged intent updates query in state`() = runTest {
        viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))
        assertEquals("kotlin", viewModel.uiState.value.query)
    }

    @Test
    fun `TrendingPeriodChanged updates state`() = runTest {
        viewModel.onIntent(SearchIntent.TrendingPeriodChanged(TrendingPeriod.THIS_MONTH))
        assertEquals(TrendingPeriod.THIS_MONTH, viewModel.uiState.value.trendingPeriod)
    }

    @Test
    fun `ProgrammingLanguageSelected updates state and dismisses picker`() = runTest {
        viewModel.onIntent(SearchIntent.ShowLanguagePicker)
        assertTrue(viewModel.uiState.value.showLanguagePicker)

        val kotlin = ProgrammingLanguage("Kotlin")
        viewModel.onIntent(SearchIntent.ProgrammingLanguageSelected(kotlin))
        assertEquals(kotlin, viewModel.uiState.value.selectedLanguage)
        assertTrue(!viewModel.uiState.value.showLanguagePicker)
    }

    @Test
    fun `SpokenLanguageSelected updates state and dismisses picker`() = runTest {
        viewModel.onIntent(SearchIntent.ShowSpokenLanguagePicker)
        assertTrue(viewModel.uiState.value.showSpokenLanguagePicker)

        val english = SpokenLanguage("English", "en")
        viewModel.onIntent(SearchIntent.SpokenLanguageSelected(english))
        assertEquals(english, viewModel.uiState.value.selectedSpokenLanguage)
        assertTrue(!viewModel.uiState.value.showSpokenLanguagePicker)
    }

    @Test
    fun `clearing language sets it to null`() = runTest {
        viewModel.onIntent(SearchIntent.ProgrammingLanguageSelected(ProgrammingLanguage("Go")))
        assertNotNull(viewModel.uiState.value.selectedLanguage)

        viewModel.onIntent(SearchIntent.ProgrammingLanguageSelected(null))
        assertNull(viewModel.uiState.value.selectedLanguage)
    }

    @Test
    fun `pagingData flow is not null`() = runTest {
        assertNotNull(viewModel.pagingData)
    }

    @Test
    fun `RepoClicked intent emits NavigateToDetail side effect`() = runTest {
        val testRepo = createTestRepo("1")
        viewModel.sideEffects.test {
            viewModel.onIntent(SearchIntent.RepoClicked(testRepo))

            val effect = awaitItem()
            assertTrue(effect is SearchSideEffect.NavigateToDetail)
            assertEquals(
                "https://github.com/owner/test-repo-1",
                (effect as SearchSideEffect.NavigateToDetail).repo.url,
            )
        }
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
