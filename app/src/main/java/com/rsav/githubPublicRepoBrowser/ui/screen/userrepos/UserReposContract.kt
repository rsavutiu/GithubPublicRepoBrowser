package com.rsav.githubPublicRepoBrowser.ui.screen.userrepos

import androidx.compose.runtime.Immutable
import com.rsav.githubPublicRepoBrowser.domain.model.Repo

sealed interface UserReposIntent {
    data class RepoClicked(val repo: Repo) : UserReposIntent
    data object NavigateBack : UserReposIntent
}

@Immutable
data class UserReposUiState(
    val userLogin: String = "",
    val userName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val followers: Int = 0,
    val following: Int = 0,
    val repoCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface UserReposSideEffect {
    data class NavigateToDetail(val repo: Repo) : UserReposSideEffect
    data object NavigateBack : UserReposSideEffect
}
