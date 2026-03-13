package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.runtime.Immutable

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Search : SearchIntent
    data class RepoClicked(val url: String) : SearchIntent
}

@Immutable
data class SearchUiState(
    val query: String = "",
)

sealed interface SearchSideEffect {
    data class OpenUrl(val url: String) : SearchSideEffect
}
