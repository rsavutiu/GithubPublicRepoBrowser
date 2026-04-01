package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.SearchBar
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@Composable
internal fun SearchToolbar(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToFavorites: () -> Unit,
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
            IconButton(onClick = { onIntent(SearchIntent.ShowTopicPicker) }) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = stringResource(R.string.filter_topics_desc),
                )
            }
            IconButton(onClick = onNavigateToFavorites) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = stringResource(R.string.favorites_desc),
                )
            }
        },
    )
}

@Composable
internal fun SavedSearchChips(
    savedSearches: List<SavedSearch>,
    onIntent: (SearchIntent) -> Unit,
) {
    if (savedSearches.isEmpty()) return

    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = savedSearches,
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

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SearchToolbarDefaultPreview() {
    MyApplicationTheme {
        SearchToolbar(
            uiState = SearchUiState(),
            onIntent = {},
            onNavigateToFavorites = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchToolbarWithQueryPreview() {
    MyApplicationTheme {
        SearchToolbar(
            uiState = SearchUiState(query = "jetpack compose"),
            onIntent = {},
            onNavigateToFavorites = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedSearchChipsPreview() {
    MyApplicationTheme {
        SavedSearchChips(
            savedSearches = listOf(
                SavedSearch(id = "1", name = "Kotlin Android", query = "kotlin android", trendingPeriodName = "THIS_WEEK", programmingLanguageName = "Kotlin", spokenLanguageCode = null),
                SavedSearch(id = "2", name = "Rust CLI", query = "rust cli", trendingPeriodName = null, programmingLanguageName = "Rust", spokenLanguageCode = null),
                SavedSearch(id = "3", name = "ML Papers", query = "machine learning", trendingPeriodName = "THIS_MONTH", programmingLanguageName = "Python", spokenLanguageCode = null),
            ),
            onIntent = {},
        )
    }
}
