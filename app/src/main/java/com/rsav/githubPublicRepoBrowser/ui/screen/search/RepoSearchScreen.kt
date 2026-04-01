package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.ui.preview.sampleRepos
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.flowOf

// ──────────────────────────────────────────────────────────────
// Stateful wrapper — owns ViewModel, collects side-effects.
// Not previewed.
// ──────────────────────────────────────────────────────────────

@Composable
fun RepoSearchScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (Repo) -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    viewModel: RepoSearchViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repos = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is SearchSideEffect.NavigateToDetail -> onNavigateToDetail(sideEffect.repo)
            }
        }
    }

    RepoSearchContent(
        modifier = modifier,
        uiState = uiState,
        repos = repos,
        onIntent = viewModel::onIntent,
        onNavigateToFavorites = onNavigateToFavorites,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
    )
}

// ──────────────────────────────────────────────────────────────
// Stateless content — pure function of state + callbacks.
// Fully previewable.
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoSearchContent(
    uiState: SearchUiState,
    repos: androidx.paging.compose.LazyPagingItems<Repo>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToFavorites: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    ProgressiveBackHandler(uiState = uiState, onIntent = onIntent)
    SearchDialogs(uiState = uiState, onIntent = onIntent)

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
        ) {
            SearchToolbar(
                uiState = uiState,
                onIntent = onIntent,
                onNavigateToFavorites = onNavigateToFavorites,
            )

            Spacer(modifier = Modifier.height(8.dp))

            SavedSearchChips(
                savedSearches = uiState.savedSearches,
                onIntent = onIntent,
            )

            FilterChipsRow(uiState = uiState, onIntent = onIntent)

            Spacer(modifier = Modifier.height(8.dp))

            RepoResultsBody(
                repos = repos,
                uiState = uiState,
                onIntent = onIntent,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Back handler — progressively clears filters on back press
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProgressiveBackHandler(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
) {
    val hasActiveFilters = uiState.selectedTopics.isNotEmpty() ||
        uiState.selectedLanguage != null ||
        uiState.selectedSpokenLanguage != null ||
        uiState.query.isNotEmpty()

    BackHandler(enabled = hasActiveFilters) {
        when {
            uiState.showLanguagePicker || uiState.showSpokenLanguagePicker ->
                onIntent(SearchIntent.DismissPicker)
            uiState.showTopicPicker ->
                onIntent(SearchIntent.DismissTopicPicker)
            uiState.showSaveSearchDialog ->
                onIntent(SearchIntent.DismissSaveSearchDialog)
            uiState.savedSearchPendingDelete != null ->
                onIntent(SearchIntent.DismissDeleteSavedSearch)
            uiState.selectedTopics.isNotEmpty() ->
                onIntent(SearchIntent.ClearTopics)
            uiState.selectedLanguage != null ->
                onIntent(SearchIntent.ProgrammingLanguageSelected(null))
            uiState.selectedSpokenLanguage != null ->
                onIntent(SearchIntent.SpokenLanguageSelected(null))
            uiState.query.isNotEmpty() -> {
                onIntent(SearchIntent.QueryChanged(""))
                onIntent(SearchIntent.Search)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Previews — no ViewModel, no Hilt, just state + data
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchContentDefaultPreview() {
    MyApplicationTheme {
        RepoSearchContent(
            uiState = SearchUiState(),
            repos = flowOf(PagingData.from(sampleRepos)).collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchContentWithFiltersPreview() {
    MyApplicationTheme {
        RepoSearchContent(
            uiState = SearchUiState(
                query = "server",
                trendingPeriod = TrendingPeriod.THIS_WEEK,
                selectedLanguage = ProgrammingLanguage("Kotlin"),
            ),
            repos = flowOf(PagingData.from(sampleRepos)).collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchContentEmptyPreview() {
    MyApplicationTheme {
        RepoSearchContent(
            uiState = SearchUiState(),
            repos = flowOf(PagingData.empty<Repo>()).collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}
