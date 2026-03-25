package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.DependencyInfo
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.AI_PROVIDERS
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.AiProvider
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.CommitHeatmap
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.FormattedDate
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.GithubAvatar
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.HeatmapLegend
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.MarkdownWebView
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.getInstalledAiProviders
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.launchAiProvider
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.RepoStats
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleRepoProvider
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

private const val SHARED_ANIM_MS = 1000

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
                is DetailSideEffect.LaunchAi -> {
                    launchAiProvider(context, effect.provider, effect.prompt)
                }
            }
        }
    }

    RepoDetailContent(
        modifier = modifier,
        repo = repo,
        uiState = uiState,
        onIntent = detailsViewModel::onIntent,
        onTopicClick = onTopicClick,
        onOwnerClick = onOwnerClick,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun RepoDetailContent(
    modifier: Modifier = Modifier,
    repo: Repo,
    uiState: DetailUiState,
    onIntent: (DetailIntent) -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onOwnerClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(repo.name) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(DetailIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    // Offline favorite button
                    IconButton(onClick = { onIntent(DetailIntent.ToggleFavorite) }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = stringResource(
                                if (uiState.isFavorite) R.string.unsave_repo_desc else R.string.save_repo_desc
                            ),
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // GitHub star button (only if logged in)
                    if (uiState.isLoggedIn) {
                        IconButton(onClick = { onIntent(DetailIntent.ToggleStar) }) {
                            Icon(
                                imageVector = if (uiState.isStarred == true) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star on GitHub",
                                tint = if (uiState.isStarred == true) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    // Share button
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_repo_text, repo.url))
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_desc),
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
                        onClick = { onIntent(DetailIntent.OpenUrl(repo.url)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.github_button))
                    }
                    OutlinedButton(
                        onClick = { onIntent(DetailIntent.RequestAskAi) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.ask_ai_button))
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

            if (!repo.licenseName.isNullOrBlank() && repo.licenseName != "NOASSERTION") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = repo.licenseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Contributor count + bus factor warning
            if (uiState.contributorCount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.contributorCount} contributors",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Bus factor warning
                    if (uiState.contributorCount <= 2 && repo.stargazerCount >= 1000) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF9800),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Single maintainer risk",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                        )
                    } else if (uiState.contributorCount >= 100) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Strong community",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

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
                FormattedDate(prefix = stringResource(R.string.last_update_prefix), isoDate = repo.updatedAt)
            }

            if (uiState.weeklyCommits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.commit_activity_last_weeks, uiState.weeklyCommits.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                CommitHeatmap(
                    weeklyData = uiState.weeklyCommits,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                HeatmapLegend(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                )
            }

            // Dependencies section
            Spacer(modifier = Modifier.height(12.dp))
            DependenciesSection(
                uiState = uiState,
                onLoadDependencies = { onIntent(DetailIntent.LoadDependencies) },
                onToggleDependencies = { onIntent(DetailIntent.ToggleDependencies) },
            )

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
                            text = uiState.error,
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

    // AI picker dialog — must be outside Scaffold to avoid clipping
    if (uiState.showAiPicker) {
        AiPickerDialog(
            context = context,
            onProviderSelected = { provider ->
                onIntent(DetailIntent.ConfirmAskAi(provider))
            },
            onDismiss = { onIntent(DetailIntent.DismissAskAi) },
        )
    }
}

@Composable
private fun AiPickerDialog(
    context: Context,
    onProviderSelected: (AiProvider) -> Unit,
    onDismiss: () -> Unit,
) {
    val installedProviders = getInstalledAiProviders(context)
    val webOnlyProviders = AI_PROVIDERS.filter { it !in installedProviders }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.ask_ai_title))
            }
        },
        text = {
            Column {
                if (installedProviders.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.installed_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    installedProviders.forEach { provider ->
                        AiProviderRow(
                            provider = provider,
                            isInstalled = true,
                            onClick = { onProviderSelected(provider) },
                        )
                    }
                }
                if (webOnlyProviders.isNotEmpty()) {
                    if (installedProviders.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    Text(
                        text = stringResource(R.string.open_in_browser_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    webOnlyProviders.forEach { provider ->
                        AiProviderRow(
                            provider = provider,
                            isInstalled = false,
                            onClick = { onProviderSelected(provider) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun AiProviderRow(
    provider: AiProvider,
    isInstalled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = provider.brandColor.copy(alpha = 0.12f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = provider.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = provider.brandColor,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = provider.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isInstalled) provider.tagline else "${provider.tagline} \u00b7 ${provider.webUrl.removePrefix("https://").removeSuffix("/")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!isInstalled) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = stringResource(R.string.open_in_browser_desc),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DependenciesSection(
    uiState: DetailUiState,
    onLoadDependencies: () -> Unit,
    onToggleDependencies: () -> Unit,
) {
    val hasLoaded = uiState.dependencyInfos.isNotEmpty() || (!uiState.isDependenciesLoading && uiState.showDependencies)

    if (!hasLoaded && !uiState.isDependenciesLoading) {
        // Show the button to load dependencies
        OutlinedButton(
            onClick = onLoadDependencies,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dependencies_button))
        }
    } else if (uiState.isDependenciesLoading) {
        // Loading state
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dependencies_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else if (uiState.dependencyInfos.isEmpty()) {
        // No dependencies found
        Text(
            text = stringResource(R.string.dependencies_none),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        // Show toggle header
        OutlinedButton(
            onClick = onToggleDependencies,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.dependencies_button))
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (uiState.showDependencies) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        if (uiState.showDependencies) {
            Spacer(modifier = Modifier.height(8.dp))
            uiState.dependencyInfos.forEach { info ->
                DependencyEcosystemSection(info)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DependencyEcosystemSection(info: DependencyInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${info.ecosystem} (${info.sourceFile}) \u2014 ${info.dependencies.size} ${if (info.dependencies.size == 1) "dependency" else "dependencies"}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        info.dependencies.forEach { dep ->
            Text(
                text = if (dep.version != null) "${dep.name}: ${dep.version}" else dep.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RepoDetailScreenPreview(
    @PreviewParameter(SampleRepoProvider::class) repo: Repo,
) {
    MyApplicationTheme {
        RepoDetailContent(
            repo = repo,
            uiState = DetailUiState(
                readmeHtml = "<h1>Sample README</h1><p>This is a preview of the repository detail screen.</p>",
            ),
        )
    }
}
