package com.rsav.githubPublicRepoBrowser.ui.screen.search

import app.cash.turbine.test
import com.rsav.githubPublicRepoBrowser.data.remote.IContributorDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import com.rsav.githubPublicRepoBrowser.testing.FakeContributorDataSource
import com.rsav.githubPublicRepoBrowser.testing.FakeSavedSearchRepository
import com.rsav.githubPublicRepoBrowser.testing.FakeSearchRepositories
import com.rsav.githubPublicRepoBrowser.testing.createTestRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var fakeRepository: FakeSearchRepositories
    private lateinit var fakeSavedSearchRepo: FakeSavedSearchRepository
    private lateinit var fakeContributors: FakeContributorDataSource
    private lateinit var viewModel: RepoSearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSearchRepositories()
        fakeSavedSearchRepo = FakeSavedSearchRepository()
        fakeContributors = FakeContributorDataSource()

        val useCase = SearchReposUseCase(fakeRepository)
        viewModel = RepoSearchViewModel(
            searchReposUseCase = useCase,
            savedSearchRepository = fakeSavedSearchRepo,
            contributorDataSource = object : IContributorDataSource {
                override suspend fun getContributorCount(owner: String, repo: String): Int? =
                    fakeContributors.getContributorCount(owner, repo)
            },
            availableTopicsProvider = { listOf("android", "kotlin", "compose") },
        )
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
}
