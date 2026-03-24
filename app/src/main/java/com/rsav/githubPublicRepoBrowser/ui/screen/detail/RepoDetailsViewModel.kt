package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsav.githubPublicRepoBrowser.data.remote.SparklineDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.usecase.GetRepoDetailsUseCase
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    private val sparklineDataSource: SparklineDataSource,
    private val parser: Parser,
    private val htmlRenderer: HtmlRenderer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<DetailSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var currentRepo: Repo? = null
    private var rawMarkdown: String? = null

    fun setRepo(repo: Repo) {
        currentRepo = repo
    }

    fun onIntent(intent: DetailIntent) {
        L.d(TAG, "onIntent: $intent")
        when (intent) {
            is DetailIntent.LoadDetails -> handleLoadDetails(intent.name, intent.owner)
            is DetailIntent.OpenUrl -> handleSideEffect(DetailSideEffect.OpenBrowser(intent.url))
            is DetailIntent.NavigateBack -> handleSideEffect(DetailSideEffect.NavigateBack)
            is DetailIntent.AskClaude -> handleAskClaude()
        }
    }

    private fun handleLoadDetails(name: String, owner: String) {
        L.d(TAG, "handleLoadDetails(owner=$owner, name=$name)")
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val readmeDeferred = async { getRepoDetailsUseCase(name = name, owner = owner) }
                val sparklineDeferred = async { sparklineDataSource.getWeeklyCommits(owner, name) }

                val markdown = readmeDeferred.await()
                val weeklyCommits = sparklineDeferred.await()

                rawMarkdown = markdown
                L.d(TAG, "markdown fetched — ${markdown?.length ?: 0} chars")

                val html = withContext(Dispatchers.Default) {
                    markdownToHtml(markdown, owner, name)
                }
                L.d(TAG, "html rendered — ${html?.length ?: 0} chars")
                _uiState.update { it.copy(readmeHtml = html, isLoading = false, weeklyCommits = weeklyCommits) }
            } catch (e: Exception) {
                L.e(TAG, "handleLoadDetails FAILED: ${e.message}", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun handleAskClaude() {
        val repo = currentRepo ?: return
        val prompt = buildClaudePrompt(repo, rawMarkdown)
        handleSideEffect(DetailSideEffect.AskClaude(prompt))
    }

    private fun buildClaudePrompt(repo: Repo, readme: String?): String {
        val sb = StringBuilder()
        sb.appendLine("I'm evaluating this GitHub repository and would like your analysis:")
        sb.appendLine()
        sb.appendLine("Repository: ${repo.nameWithOwner}")
        sb.appendLine("URL: ${repo.url}")
        if (!repo.description.isNullOrBlank()) {
            sb.appendLine("Description: ${repo.description}")
        }
        sb.appendLine("Stars: ${repo.stargazerCount} | Forks: ${repo.forkCount}")
        if (!repo.languageName.isNullOrBlank()) {
            sb.appendLine("Language: ${repo.languageName}")
        }
        if (repo.topics.isNotEmpty()) {
            sb.appendLine("Topics: ${repo.topics.joinToString(", ")}")
        }
        if (!repo.createdAt.isNullOrBlank()) {
            sb.appendLine("Created: ${repo.createdAt}")
        }
        if (!repo.updatedAt.isNullOrBlank()) {
            sb.appendLine("Last updated: ${repo.updatedAt}")
        }

        // Include a trimmed README excerpt (keep it reasonable for sharing)
        if (!readme.isNullOrBlank()) {
            val trimmed = if (readme.length > 3000) readme.take(3000) + "\n[...truncated]" else readme
            sb.appendLine()
            sb.appendLine("--- README (excerpt) ---")
            sb.appendLine(trimmed)
            sb.appendLine("--- End README ---")
        }

        sb.appendLine()
        sb.appendLine("Please provide:")
        sb.appendLine("1. A concise summary of what this project does and who it's for")
        sb.appendLine("2. Key strengths and standout features")
        sb.appendLine("3. Any concerns (maintenance activity, code quality signals, licensing)")
        sb.appendLine("4. How it compares to alternatives in the same space")
        sb.appendLine("5. Your overall assessment: would you recommend using this?")

        return sb.toString()
    }

    private fun markdownToHtml(markdown: String?, owner: String, name: String): String? {
        if (markdown.isNullOrBlank()) {
            L.w(TAG, "markdownToHtml — input is null/blank")
            return null
        }
        val document = parser.parse(markdown)
        var html = htmlRenderer.render(document)
        L.d(TAG, "markdownToHtml — raw html length=${html.length}")
        html = resolveRelativeImageUrls(html, owner, name)
        html = upgradeHttpToHttps(html)
        L.d(TAG, "markdownToHtml — final html length=${html.length}")
        return html
    }

    private fun resolveRelativeImageUrls(html: String, owner: String, name: String): String {
        val rawBase = "https://raw.githubusercontent.com/$owner/$name/HEAD/"
        return html.replace(Regex("""(<img\s[^>]*src=")(?!https?://|data:)([^"]+)(")""")) { match ->
            val prefix = match.groupValues[1]
            val path = match.groupValues[2].removePrefix("./")
            val suffix = match.groupValues[3]
            L.d(TAG, "rewriting relative image: ${match.groupValues[2]} → $rawBase$path")
            "$prefix$rawBase$path$suffix"
        }
    }

    private fun upgradeHttpToHttps(html: String): String {
        return html.replace(Regex("""(<img\s[^>]*src=")http://""")) { match ->
            L.d(TAG, "upgrading http→https for image")
            "${match.groupValues[1]}https://"
        }
    }

    private fun handleSideEffect(effect: DetailSideEffect) {
        L.d(TAG, "handleSideEffect: $effect")
        viewModelScope.launch {
            _sideEffects.send(effect)
        }
    }

    companion object {
        private const val TAG = "DetailVM"
    }
}
