package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.runtime.Immutable
import com.rsav.githubPublicRepoBrowser.domain.model.Repo

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Search : SearchIntent
    data class RepoClicked(val repo: Repo) : SearchIntent
}

@Immutable
data class SearchUiState(
    val query: String = "",
)

sealed interface SearchSideEffect {
    data class NavigateToDetail(val repo: Repo) : SearchSideEffect
}
