package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsav.githubPublicRepoBrowser.domain.usecase.GetRepoDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import javax.inject.Inject

@HiltViewModel
class RepoDetailsViewModel @Inject constructor(
    private val getRepoDetailsUseCase: GetRepoDetailsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<DetailSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    fun onIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadDetails -> reduceLoadDetails(intent.name, intent.owner)
            is DetailIntent.OpenUrl -> reduceSideEffect(DetailSideEffect.OpenBrowser(intent.url))
            is DetailIntent.NavigateBack -> reduceSideEffect(DetailSideEffect.NavigateBack)
        }
    }

    private fun reduceLoadDetails(name: String, owner: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val markdown = getRepoDetailsUseCase(name = name, owner = owner)
                val html = withContext(Dispatchers.Default) { markdownToHtml(markdown) }
                _uiState.update { it.copy(readmeHtml = html, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun markdownToHtml(markdown: String?): String? {
        if (markdown.isNullOrBlank()) return null
        val parser = Parser.builder().build()
        val document = parser.parse(markdown)
        return HtmlRenderer.builder().build().render(document)
    }

    private fun reduceSideEffect(effect: DetailSideEffect) {
        viewModelScope.launch {
            _sideEffects.send(effect)
        }
    }
}
