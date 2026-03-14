package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val searchReposUseCase: SearchReposUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<SearchSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val _searchTrigger = MutableSharedFlow<String>(replay = 1)

    @OptIn(FlowPreview::class)
    val pagingData: Flow<PagingData<Repo>> = _searchTrigger
        .debounce { if (it.isEmpty()) 0 else 500 }
        .flatMapLatest { query -> searchReposUseCase(query).cachedIn(viewModelScope) }

    init {
        _searchTrigger.tryEmit("")
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> reduceQueryChanged(intent.query)
            is SearchIntent.Search -> reduceSearch()
            is SearchIntent.RepoClicked -> reduceRepoClicked(intent.repo)
        }
    }

    private fun reduceQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun reduceSearch() {
        _searchTrigger.tryEmit(_uiState.value.query)
    }

    private fun reduceRepoClicked(repo: Repo) {
        viewModelScope.launch {
            _sideEffects.send(SearchSideEffect.NavigateToDetail(repo))
        }
    }
}
