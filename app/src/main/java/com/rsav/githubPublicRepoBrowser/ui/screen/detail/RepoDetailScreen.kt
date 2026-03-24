package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.FormattedDate
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.GithubAvatar
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.MarkdownWebView
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.Sparkline
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.RepoStats
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleRepoProvider
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

private const val SHARED_ANIM_MS = 1000

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun RepoDetailScreen(
    modifier: Modifier = Modifier,
    repo: Repo,
    detailsViewModel: RepoDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onOwnerClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        detailsViewModel.setRepo(repo)
        detailsViewModel.onIntent(DetailIntent.LoadDetails(name = repo.name, owner = repo.ownerLogin))
    }

    LaunchedEffect(Unit) {
        detailsViewModel.sideEffects.collect { effect ->
            when (effect) {
                is DetailSideEffect.OpenBrowser -> {
                    val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                    context.startActivity(intent)
                }
                is DetailSideEffect.NavigateBack -> onNavigateBack()
                is DetailSideEffect.AskClaude -> {
                    launchClaudeIntent(context, effect.prompt)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(repo.name) },
                navigationIcon = {
                    IconButton(onClick = { detailsViewModel.onIntent(DetailIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
            ) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { detailsViewModel.onIntent(DetailIntent.OpenUrl(repo.url)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GitHub")
                    }
                    OutlinedButton(
                        onClick = { detailsViewModel.onIntent(DetailIntent.AskClaude) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Claude")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repo.ownerAvatarUrl?.let {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "avatar-${repo.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ -> tween(SHARED_ANIM_MS) },
                                )
                                .size(72.dp)
                        }
                    } else {
                        Modifier.size(72.dp)
                    }

                    GithubAvatar(
                        url = it,
                        modifier = avatarModifier,
                        isOrganization = repo.ownerType == "Organization",
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val nameModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "name-${repo.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> tween(SHARED_ANIM_MS) },
                            )
                        }
                    } else {
                        Modifier
                    }

                    Text(
                        modifier = nameModifier,
                        text = repo.nameWithOwner,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = repo.ownerLogin,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOwnerClick(repo.ownerLogin) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!repo.description.isNullOrBlank()) {
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            RepoStats(
                starCount = repo.stargazerCount,
                forkCount = repo.forkCount,
                language = repo.languageName,
                languageColor = repo.languageColor,
            )

            if (repo.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repo.topics.forEach { topic ->
                        AssistChip(
                            onClick = { onTopicClick(topic) },
                            label = { Text(topic, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            if (!repo.createdAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FormattedDate(
                    isoDate = repo.createdAt,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (!repo.updatedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                FormattedDate(prefix = "Last Update at:", isoDate = repo.updatedAt)
            }

            if (uiState.weeklyCommits.size >= 2) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Commit activity (last ${uiState.weeklyCommits.size} weeks)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Sparkline(
                    data = uiState.weeklyCommits,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            // Readme content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                !uiState.readmeHtml.isNullOrEmpty() -> {
                    MarkdownWebView(
                        html = uiState.readmeHtml!!,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun launchClaudeIntent(context: Context, prompt: String) {
    // Try Claude app first via share intent
    val claudePackage = "com.anthropic.claude"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, prompt)
        setPackage(claudePackage)
    }
    try {
        context.startActivity(shareIntent)
        return
    } catch (_: Exception) {
        // Claude app not installed, fall through
    }

    // Fallback: copy to clipboard and open claude.ai in browser
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Claude prompt", prompt))

    val browserIntent = Intent(Intent.ACTION_VIEW, "https://claude.ai/new".toUri())
    try {
        context.startActivity(browserIntent)
        Toast.makeText(context, "Prompt copied! Paste it in Claude.", Toast.LENGTH_LONG).show()
    } catch (_: Exception) {
        Toast.makeText(context, "Prompt copied to clipboard.", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
private fun RepoDetailScreenPreview(
    @PreviewParameter(SampleRepoProvider::class) repo: Repo,
) {
    MyApplicationTheme {
        RepoDetailScreen(
            repo = repo,
        )
    }
}
