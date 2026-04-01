package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import androidx.compose.runtime.Immutable
import com.rsav.githubPublicRepoBrowser.domain.model.DependencyInfo
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.AiProvider

sealed interface DetailIntent {
    data class LoadDetails(val name: String, val owner: String) : DetailIntent
    data class OpenUrl(val url: String) : DetailIntent
    data object NavigateBack : DetailIntent
    data object RequestAskAi : DetailIntent
    data class ConfirmAskAi(val provider: AiProvider) : DetailIntent
    data object DismissAskAi : DetailIntent
    data object LoadDependencies : DetailIntent
    data object ToggleDependencies : DetailIntent
    data object ToggleFavorite : DetailIntent
    data object ToggleStar : DetailIntent
}

@Immutable
data class DetailUiState(
    val readmeHtml: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weeklyCommits: List<Int> = emptyList(),
    val contributorCount: Int? = null,
    val showAiPicker: Boolean = false,
    val dependencyInfos: List<DependencyInfo> = emptyList(),
    val isDependenciesLoading: Boolean = false,
    val showDependencies: Boolean = false,
    val isFavorite: Boolean = false,
    val isStarred: Boolean? = null, // null = unknown (not logged in or loading)
    val isLoggedIn: Boolean = false,
)

sealed interface DetailSideEffect {
    data class OpenBrowser(val url: String) : DetailSideEffect
    data object NavigateBack : DetailSideEffect
    data class LaunchAi(val provider: AiProvider, val prompt: String) : DetailSideEffect
    data class LaunchOAuth(val url: String) : DetailSideEffect
}
