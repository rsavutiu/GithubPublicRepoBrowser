package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.PROGRAMMING_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.SPOKEN_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.LanguagePickerSheet
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.SaveSearchDialog
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.TopicPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchDialogs(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
) {
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

    if (uiState.showTopicPicker) {
        TopicPickerSheet(
            topics = uiState.availableTopics,
            selectedTopics = uiState.selectedTopics,
            loading = uiState.loadingTopics,
            onToggle = { onIntent(SearchIntent.TopicToggled(it)) },
            onClear = { onIntent(SearchIntent.ClearTopics) },
            onDismiss = { onIntent(SearchIntent.DismissTopicPicker) },
        )
    }

    if (uiState.showSaveSearchDialog) {
        SaveSearchDialog(
            onConfirm = { onIntent(SearchIntent.ConfirmSaveSearch(it)) },
            onDismiss = { onIntent(SearchIntent.DismissSaveSearchDialog) },
        )
    }

    uiState.savedSearchPendingDelete?.let { pending ->
        DeleteSavedSearchDialog(pending = pending, onIntent = onIntent)
    }
}

@Composable
private fun DeleteSavedSearchDialog(
    pending: SavedSearch,
    onIntent: (SearchIntent) -> Unit,
) {
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

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun DeleteSavedSearchDialogPreview() {
    com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme {
        SearchDialogs(
            uiState = SearchUiState(
                savedSearchPendingDelete = SavedSearch(
                    id = "1",
                    name = "Kotlin Android",
                    query = "kotlin android",
                    trendingPeriodName = null,
                    programmingLanguageName = null,
                    spokenLanguageCode = null,
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaveSearchDialogPreview() {
    com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme {
        SearchDialogs(
            uiState = SearchUiState(showSaveSearchDialog = true),
            onIntent = {},
        )
    }
}
