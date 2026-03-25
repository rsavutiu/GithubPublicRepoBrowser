package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rsav.githubPublicRepoBrowser.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.domain.model.PROGRAMMING_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.SPOKEN_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.SearchBar
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.LanguagePickerSheet
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.RepoList
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.SaveSearchDialog
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
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
    )
}

// ──────────────────────────────────────────────────────────────
// Stateless content — pure function of state + callbacks.
// Fully previewable.
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RepoSearchContent(
    uiState: SearchUiState,
    repos: LazyPagingItems<Repo>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    // Progressive back: clear filters/state before exiting
    val hasActiveFilters = uiState.selectedTopic != null ||
        uiState.selectedLanguage != null ||
        uiState.selectedSpokenLanguage != null ||
        uiState.query.isNotEmpty()

    BackHandler(enabled = hasActiveFilters) {
        when {
            uiState.showLanguagePicker || uiState.showSpokenLanguagePicker ->
                onIntent(SearchIntent.DismissPicker)
            uiState.showSaveSearchDialog ->
                onIntent(SearchIntent.DismissSaveSearchDialog)
            uiState.savedSearchPendingDelete != null ->
                onIntent(SearchIntent.DismissDeleteSavedSearch)
            uiState.selectedTopic != null ->
                onIntent(SearchIntent.TopicSelected(null))
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

    // Language picker bottom sheets
    if (uiState.showLanguagePicker) {
        LanguagePickerSheet(
            title = stringResource(R.string.prog_language_title),
            items = PROGRAMMING_LANGUAGES,
            selected = uiState.selectedLanguage,
            itemLabel = { it.name },
            onSelect = { onIntent(SearchIntent.ProgrammingLanguageSelected(it)) },
            onDismiss = { onIntent(SearchIntent.DismissPicker) },
            searchPlaceholder = stringResource(R.string.search_languages_placeholder),
        )
    }

    if (uiState.showSpokenLanguagePicker) {
        LanguagePickerSheet(
            title = stringResource(R.string.spoken_language_title),
            items = SPOKEN_LANGUAGES,
            selected = uiState.selectedSpokenLanguage,
            itemLabel = { it.name },
            onSelect = { onIntent(SearchIntent.SpokenLanguageSelected(it)) },
            onDismiss = { onIntent(SearchIntent.DismissPicker) },
            searchPlaceholder = stringResource(R.string.search_languages_placeholder),
        )
    }

    // Save search dialog
    if (uiState.showSaveSearchDialog) {
        SaveSearchDialog(
            onConfirm = { onIntent(SearchIntent.ConfirmSaveSearch(it)) },
            onDismiss = { onIntent(SearchIntent.DismissSaveSearchDialog) },
        )
    }

    // Delete confirmation dialog
    uiState.savedSearchPendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { onIntent(SearchIntent.DismissDeleteSavedSearch) },
            title = { Text(stringResource(R.string.delete_saved_search_title)) },
            text = { Text(stringResource(R.string.delete_saved_search_confirm, pending.name)) },
            confirmButton = {
                TextButton(onClick = { onIntent(SearchIntent.ConfirmDeleteSavedSearch) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(SearchIntent.DismissDeleteSavedSearch) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize(),
        ) {
            SearchBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                query = uiState.query,
                onQueryChanged = { onIntent(SearchIntent.QueryChanged(it)) },
                onSearch = { onIntent(SearchIntent.Search) },
                trailingIcons = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { onIntent(SearchIntent.ShowSaveSearchDialog) }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = stringResource(R.string.save_current_search_desc),
                            )
                        }
                    }
                    IconButton(onClick = { onIntent(SearchIntent.ShowLanguagePicker) }) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = stringResource(R.string.filter_prog_lang_desc),
                        )
                    }
                    IconButton(onClick = { onIntent(SearchIntent.ShowSpokenLanguagePicker) }) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = stringResource(R.string.filter_spoken_lang_desc),
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Saved searches chip row
            if (uiState.savedSearches.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.savedSearches,
                        key = { it.id },
                    ) { saved ->
                        InputChip(
                            selected = false,
                            onClick = { onIntent(SearchIntent.LoadSavedSearch(saved)) },
                            label = { Text(saved.name) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onIntent(SearchIntent.RequestDeleteSavedSearch(saved)) },
                                    modifier = Modifier.size(18.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete_saved_search_desc),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Trending period chips + active filter chips
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TrendingPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = uiState.trendingPeriod == period,
                        onClick = {
                            if (uiState.trendingPeriod != period) {
                                onIntent(SearchIntent.TrendingPeriodChanged(period))
                            } else {
                                onIntent(SearchIntent.TrendingPeriodChanged(null))
                            }
                        },
                        label = { Text(stringResource(period.labelResId)) },
                    )
                }

                uiState.selectedLanguage?.let { lang ->
                    InputChip(
                        selected = true,
                        onClick = { onIntent(SearchIntent.ProgrammingLanguageSelected(null)) },
                        label = { Text(lang.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_lang_filter_desc),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }

                uiState.selectedSpokenLanguage?.let { lang ->
                    InputChip(
                        selected = true,
                        onClick = { onIntent(SearchIntent.SpokenLanguageSelected(null)) },
                        label = { Text(lang.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_spoken_lang_filter_desc),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }

                uiState.selectedTopic?.let { topic ->
                    InputChip(
                        selected = true,
                        onClick = { onIntent(SearchIntent.TopicSelected(null)) },
                        label = { Text(topic) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_topic_filter_desc),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                repos.loadState.refresh is LoadState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                repos.loadState.refresh is LoadState.Error -> {
                    val error = (repos.loadState.refresh as LoadState.Error).error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = error.localizedMessage ?: stringResource(R.string.unknown_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                repos.itemCount == 0 && repos.loadState.refresh is LoadState.NotLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.no_repos_found),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                else -> {
                    RepoList(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        repos = repos,
                        onRepoClick = { onIntent(SearchIntent.RepoClicked(it)) },
                        onTopicClick = { onIntent(SearchIntent.TopicSelected(it)) },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope,
                    )
                }
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
