package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.runtime.Immutable
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Search : SearchIntent
    data class RepoClicked(val repo: Repo) : SearchIntent
    data class TrendingPeriodChanged(val period: TrendingPeriod?) : SearchIntent
    data class ProgrammingLanguageSelected(val language: ProgrammingLanguage?) : SearchIntent
    data class SpokenLanguageSelected(val language: SpokenLanguage?) : SearchIntent
    data object ShowLanguagePicker : SearchIntent
    data object ShowSpokenLanguagePicker : SearchIntent
    data object DismissPicker : SearchIntent
    data object ShowSaveSearchDialog : SearchIntent
    data object DismissSaveSearchDialog : SearchIntent
    data class ConfirmSaveSearch(val name: String) : SearchIntent
    data class LoadSavedSearch(val savedSearch: SavedSearch) : SearchIntent
    data class RequestDeleteSavedSearch(val savedSearch: SavedSearch) : SearchIntent
    data object ConfirmDeleteSavedSearch : SearchIntent
    data object DismissDeleteSavedSearch : SearchIntent
    data class TopicToggled(val topic: String) : SearchIntent
    data object ClearTopics : SearchIntent
    data object ShowTopicPicker : SearchIntent
    data object DismissTopicPicker : SearchIntent
    data class LoadContributorCount(val owner: String, val repoName: String) : SearchIntent
}

@Immutable
data class SearchUiState(
    val query: String = "",
    val trendingPeriod: TrendingPeriod? = TrendingPeriod.THIS_WEEK,
    val selectedLanguage: ProgrammingLanguage? = null,
    val selectedSpokenLanguage: SpokenLanguage? = null,
    val showLanguagePicker: Boolean = false,
    val showSpokenLanguagePicker: Boolean = false,
    val savedSearches: List<SavedSearch> = emptyList(),
    val showSaveSearchDialog: Boolean = false,
    val savedSearchPendingDelete: SavedSearch? = null,
    val selectedTopics: Set<String> = emptySet(),
    val showTopicPicker: Boolean = false,
    val availableTopics: List<String> = emptyList(),
    val loadingTopics: Boolean = false,
    val contributorCounts: Map<String, Int> = emptyMap(),
)

sealed interface SearchSideEffect {
    data class NavigateToDetail(val repo: Repo) : SearchSideEffect
}
