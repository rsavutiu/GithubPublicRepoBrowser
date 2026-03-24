package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import androidx.compose.runtime.Immutable

sealed interface DetailIntent {
    data class LoadDetails(val name: String, val owner: String) : DetailIntent
    data class OpenUrl(val url: String) : DetailIntent
    data object NavigateBack : DetailIntent
    data object AskClaude : DetailIntent
}

@Immutable
data class DetailUiState(
    val readmeHtml: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weeklyCommits: List<Int> = emptyList(),
)

sealed interface DetailSideEffect {
    data class OpenBrowser(val url: String) : DetailSideEffect
    data object NavigateBack : DetailSideEffect
    data class AskClaude(val prompt: String) : DetailSideEffect
}
