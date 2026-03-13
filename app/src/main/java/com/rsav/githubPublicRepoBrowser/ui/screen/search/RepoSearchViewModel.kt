package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val searchReposUseCase: SearchReposUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<SearchSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> reduceQueryChanged(intent.query)
            is SearchIntent.Search -> reduceSearch()
            is SearchIntent.RepoClicked -> reduceRepoClicked(intent.url)
        }
    }

    private fun reduceQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun reduceSearch() {
        val query = _uiState.value.query

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            searchReposUseCase(query)
                .onSuccess { repos ->
                    _uiState.update { it.copy(isLoading = false, repos = repos.toImmutableList()) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Unknown error")
                    }
                }
        }
    }

    private fun reduceRepoClicked(url: String) {
        viewModelScope.launch {
            _sideEffects.send(SearchSideEffect.OpenUrl(url))
        }
    }
}
