package com.rsav.githubPublicRepoBrowser.ui.screen.userrepos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserReposViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchRepository: ISearchRepositories,
    private val dataSource: ApolloRepoDataSource,
) : ViewModel() {

    private val userLogin: String = savedStateHandle.get<String>("userLogin") ?: ""

    private val _uiState = MutableStateFlow(UserReposUiState(userLogin = userLogin))
    val uiState: StateFlow<UserReposUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<UserReposSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    val pagingData: StateFlow<PagingData<Repo>> =
        MutableStateFlow(PagingData.empty<Repo>()).also { flow ->
            viewModelScope.launch {
                searchRepository.searchRepositories("user:$userLogin sort:stars")
                    .cachedIn(viewModelScope)
                    .collect { flow.value = it }
            }
        }

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val profile = dataSource.getUserProfile(userLogin)
                val user = profile.user
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        userName = user.name,
                        avatarUrl = user.avatarUrl as? String,
                        bio = user.bio,
                        company = user.company,
                        location = user.location,
                        followers = user.followers.totalCount,
                        following = user.following.totalCount,
                        repoCount = user.repositories.totalCount,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found")
                }
            } catch (e: Exception) {
                L.e(TAG, "Failed to load user profile: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onIntent(intent: UserReposIntent) {
        when (intent) {
            is UserReposIntent.RepoClicked -> {
                viewModelScope.launch { _sideEffects.send(UserReposSideEffect.NavigateToDetail(intent.repo)) }
            }
            is UserReposIntent.NavigateBack -> {
                viewModelScope.launch { _sideEffects.send(UserReposSideEffect.NavigateBack) }
            }
        }
    }

    companion object {
        private const val TAG = "UserReposVM"
    }
}
