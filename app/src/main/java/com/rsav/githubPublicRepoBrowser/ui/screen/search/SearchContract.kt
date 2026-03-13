package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.runtime.Immutable
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Search : SearchIntent
    data class RepoClicked(val url: String) : SearchIntent
}

@Immutable
data class SearchUiState(
    val query: String = "",
    val repos: ImmutableList<Repo> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface SearchSideEffect {
    data class OpenUrl(val url: String) : SearchSideEffect
}
