package com.rsav.githubPublicRepoBrowser.ui.screen.search

import app.cash.turbine.test
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        viewModel = RepoSearchViewModel(searchReposUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        val state = viewModel.uiState.value

        assertEquals("", state.query)
        assertEquals(emptyList<Repo>(), state.repos)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `QueryChanged intent updates query in state`() = runTest {
        viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))

        assertEquals("kotlin", viewModel.uiState.value.query)
    }

    @Test
    fun `Search intent with blank query still triggers search`() = runTest {
        coEvery { searchReposUseCase("") } returns Result.success(emptyList())

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(SearchIntent.QueryChanged(""))
            awaitItem()

            viewModel.onIntent(SearchIntent.Search)
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
        }
    }

    @Test
    fun `Search intent success updates repos`() = runTest {
        val expectedRepos = listOf(createTestRepo("1"))
        coEvery { searchReposUseCase("kotlin") } returns Result.success(expectedRepos)

        viewModel.uiState.test {
            assertEquals(SearchUiState(), awaitItem()) // initial

            viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))
            assertEquals("kotlin", awaitItem().query)

            viewModel.onIntent(SearchIntent.Search)
            // With UnconfinedTestDispatcher, the coroutine completes immediately
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(expectedRepos, finalState.repos)
            assertNull(finalState.error)
        }
    }

    @Test
    fun `Search intent failure sets error`() = runTest {
        coEvery { searchReposUseCase("kotlin") } returns Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(SearchUiState(), awaitItem()) // initial

            viewModel.onIntent(SearchIntent.QueryChanged("kotlin"))
            awaitItem() // query changed

            viewModel.onIntent(SearchIntent.Search)
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals("Network error", finalState.error)
        }
    }

    @Test
    fun `RepoClicked intent emits OpenUrl side effect`() = runTest {
        viewModel.sideEffects.test {
            viewModel.onIntent(SearchIntent.RepoClicked("https://github.com/test/repo"))

            val effect = awaitItem()
            assertTrue(effect is SearchSideEffect.OpenUrl)
            assertEquals("https://github.com/test/repo", (effect as SearchSideEffect.OpenUrl).url)
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
