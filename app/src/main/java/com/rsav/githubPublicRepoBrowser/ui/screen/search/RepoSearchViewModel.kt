package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rsav.githubPublicRepoBrowser.domain.model.PROGRAMMING_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.SPOKEN_LANGUAGES
import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.domain.repository.ISavedSearchRepository
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Encapsulates all filter state that triggers a new search. */
private data class SearchParams(
    val freeText: String = "",
    val trendingPeriod: TrendingPeriod? = TrendingPeriod.TODAY,
    val programmingLanguage: ProgrammingLanguage? = null,
    val spokenLanguage: SpokenLanguage? = null,
    val topic: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RepoSearchViewModel @Inject constructor(
    private val searchReposUseCase: SearchReposUseCase,
    private val savedSearchRepository: ISavedSearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<SearchSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val _searchTrigger = MutableSharedFlow<SearchParams>(replay = 1)

    @OptIn(FlowPreview::class)
    val pagingData: StateFlow<PagingData<Repo>> = _searchTrigger
        .debounce { if (it.freeText.isEmpty()) 0 else 500 }
        .flatMapLatest { params ->
            L.d(TAG, "flatMapLatest triggered — params=$params")
            searchReposUseCase(
                freeText = params.freeText,
                trendingPeriod = params.trendingPeriod,
                programmingLanguage = params.programmingLanguage,
                spokenLanguage = params.spokenLanguage,
                topic = params.topic,
            ).cachedIn(viewModelScope)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    init {
        L.i(TAG, "init — emitting default search params")
        _searchTrigger.tryEmit(SearchParams())

        viewModelScope.launch {
            savedSearchRepository.getSavedSearches().collect { searches ->
                _uiState.update { it.copy(savedSearches = searches) }
            }
        }
    }

    fun onIntent(intent: SearchIntent) {
        L.d(TAG, "onIntent: $intent")
        when (intent) {
            is SearchIntent.QueryChanged -> handleQueryChanged(intent.query)
            is SearchIntent.Search -> emitSearch()
            is SearchIntent.RepoClicked -> handleRepoClicked(intent.repo)
            is SearchIntent.TrendingPeriodChanged -> handleTrendingPeriod(intent.period)
            is SearchIntent.ProgrammingLanguageSelected -> handleProgrammingLanguage(intent.language)
            is SearchIntent.SpokenLanguageSelected -> handleSpokenLanguage(intent.language)
            is SearchIntent.ShowLanguagePicker -> _uiState.update { it.copy(showLanguagePicker = true) }
            is SearchIntent.ShowSpokenLanguagePicker -> _uiState.update { it.copy(showSpokenLanguagePicker = true) }
            is SearchIntent.DismissPicker -> _uiState.update {
                it.copy(showLanguagePicker = false, showSpokenLanguagePicker = false)
            }
            is SearchIntent.ShowSaveSearchDialog -> _uiState.update { it.copy(showSaveSearchDialog = true) }
            is SearchIntent.DismissSaveSearchDialog -> _uiState.update { it.copy(showSaveSearchDialog = false) }
            is SearchIntent.ConfirmSaveSearch -> confirmSaveSearch(intent.name)
            is SearchIntent.LoadSavedSearch -> loadSavedSearch(intent.savedSearch)
            is SearchIntent.RequestDeleteSavedSearch -> _uiState.update { it.copy(savedSearchPendingDelete = intent.savedSearch) }
            is SearchIntent.ConfirmDeleteSavedSearch -> confirmDeleteSavedSearch()
            is SearchIntent.DismissDeleteSavedSearch -> _uiState.update { it.copy(savedSearchPendingDelete = null) }
            is SearchIntent.TopicSelected -> handleTopic(intent.topic)
        }
    }

    private fun confirmSaveSearch(name: String) {
        val state = _uiState.value
        val savedSearch = SavedSearch(
            id = UUID.randomUUID().toString(),
            name = name,
            query = state.query,
            trendingPeriodName = state.trendingPeriod?.name,
            programmingLanguageName = state.selectedLanguage?.name,
            spokenLanguageCode = state.selectedSpokenLanguage?.code,
        )
        L.d(TAG, "confirmSaveSearch: $savedSearch")
        _uiState.update { it.copy(showSaveSearchDialog = false) }
        viewModelScope.launch { savedSearchRepository.addSavedSearch(savedSearch) }
    }

    private fun loadSavedSearch(saved: SavedSearch) {
        L.d(TAG, "loadSavedSearch: ${saved.name}")
        _uiState.update {
            it.copy(
                query = saved.query,
                trendingPeriod = saved.trendingPeriodName?.let { name ->
                    TrendingPeriod.entries.find { it.name == name }
                },
                selectedLanguage = saved.programmingLanguageName?.let { name ->
                    PROGRAMMING_LANGUAGES.find { it.name == name }
                },
                selectedSpokenLanguage = saved.spokenLanguageCode?.let { code ->
                    SPOKEN_LANGUAGES.find { it.code == code }
                },
                selectedTopic = null,
            )
        }
        emitSearch()
    }

    private fun confirmDeleteSavedSearch() {
        val pending = _uiState.value.savedSearchPendingDelete ?: return
        L.d(TAG, "confirmDeleteSavedSearch: ${pending.id}")
        _uiState.update { it.copy(savedSearchPendingDelete = null) }
        viewModelScope.launch { savedSearchRepository.deleteSavedSearch(pending.id) }
    }

    private fun handleTopic(topic: String?) {
        _uiState.update { it.copy(selectedTopic = topic) }
        emitSearch()
    }

    private fun handleQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun emitSearch() {
        val state = _uiState.value
        L.d(TAG, "emitSearch: query='${state.query}', period=${state.trendingPeriod}, lang=${state.selectedLanguage?.name}, spoken=${state.selectedSpokenLanguage?.name}, topic=${state.selectedTopic}")
        _searchTrigger.tryEmit(
            SearchParams(
                freeText = state.query,
                trendingPeriod = state.trendingPeriod,
                programmingLanguage = state.selectedLanguage,
                spokenLanguage = state.selectedSpokenLanguage,
                topic = state.selectedTopic,
            )
        )
    }

    private fun handleTrendingPeriod(period: TrendingPeriod?) {
        _uiState.update { it.copy(trendingPeriod = period) }
        emitSearch()
    }

    private fun handleProgrammingLanguage(language: ProgrammingLanguage?) {
        _uiState.update { it.copy(selectedLanguage = language, showLanguagePicker = false) }
        emitSearch()
    }

    private fun handleSpokenLanguage(language: SpokenLanguage?) {
        _uiState.update { it.copy(selectedSpokenLanguage = language, showSpokenLanguagePicker = false) }
        emitSearch()
    }

    private fun handleRepoClicked(repo: Repo) {
        L.d(TAG, "handleRepoClicked: ${repo.nameWithOwner}")
        viewModelScope.launch {
            _sideEffects.send(SearchSideEffect.NavigateToDetail(repo))
        }
    }

    companion object {
        private const val TAG = "SearchVM"
    }
}
