package com.rsav.githubPublicRepoBrowser.ui.screen.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
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
import kotlin.math.roundToInt

private const val SHARED_ANIM_MS = 1000
private const val PARALLAX_FACTOR = 0.5f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun RepoDetailScreen(
    modifier: Modifier = Modifier,
    repo: Repo,
    detailsViewModel: RepoDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val headerHeightPx = remember { mutableIntStateOf(0) }
    val webViewScrollY = remember { mutableIntStateOf(0) }
    val animatedCollapse = remember { Animatable(0f) }

    // Smooth the raw scroll into the animated collapse value
    LaunchedEffect(webViewScrollY.intValue, headerHeightPx.intValue) {
        val maxCollapse = headerHeightPx.intValue.toFloat()
        if (maxCollapse <= 0f) return@LaunchedEffect
        val target = webViewScrollY.intValue
            .toFloat()
            .coerceIn(0f, maxCollapse)
        animatedCollapse.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    LaunchedEffect(Unit) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            // Collapsing parallax header — driven by WebView scroll position
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        if (headerHeightPx.intValue == 0) {
                            headerHeightPx.intValue = placeable.height
                        }
                        val collapse = animatedCollapse.value
                        val visibleHeight = (placeable.height - collapse)
                            .roundToInt()
                            .coerceAtLeast(0)
                        layout(placeable.width, visibleHeight) {
                            placeable.place(
                                x = 0,
                                y = (-collapse * PARALLAX_FACTOR).roundToInt(),
                            )
                        }
                    },
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        text = "Commit activity (last year)",
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
            }

            HorizontalDivider()

            // Scrollable middle — readme content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
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
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        onScrollChanged = { webViewScrollY.intValue = it },
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Fixed footer — Open on GitHub button
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { detailsViewModel.onIntent(DetailIntent.OpenUrl(repo.url)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open on GitHub")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
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
