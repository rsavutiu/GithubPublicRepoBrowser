package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.paging.PagingData
import app.cash.turbine.test
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        // Mock the initial empty query call from init block
        every { searchReposUseCase(any()) } returns flowOf(PagingData.from(emptyList()))
        viewModel = RepoSearchViewModel(searchReposUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.query)
    }

    @Test
    fun `QueryChanged intent updates query in state`() = runTest {
        viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))
        assertEquals("kotlin", viewModel.uiState.value.query)
    }

    @Test
    fun `pagingData flow is not null`() = runTest {
        assertNotNull(viewModel.pagingData)
    }

    @Test
    fun `RepoClicked intent emits OpenUrl side effect`() = runTest {
        val testRepos = listOf(createTestRepo("1"))
        viewModel.sideEffects.test {
            viewModel.onIntent(SearchIntent.RepoClicked(testRepos.first()))

            val effect = awaitItem()
            assertTrue(effect is SearchSideEffect.NavigateToDetail)
            assertEquals("https://github.com/owner/test-repo-1", (effect as SearchSideEffect.NavigateToDetail).repo.url)
        }
    }

    @Test
    fun `Search intent triggers new paging flow`() = runTest {
        val testRepos = listOf(createTestRepo("1"))
        every { searchReposUseCase(any()) } returns flowOf(PagingData.from(testRepos))

        // Start collecting pagingData so flatMapLatest actually runs
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pagingData.collect {}
        }

        // Clear recorded calls from init emission
        io.mockk.clearMocks(searchReposUseCase, answers = false)

        viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))
        viewModel.onIntent(SearchIntent.Search)

        // Verify the use case was called with the query
        verify { searchReposUseCase("kotlin") }

        collectJob.cancel()
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
