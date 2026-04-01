package com.rsav.githubPublicRepoBrowser.ui.screen.userrepos

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.RepoList
import com.rsav.githubPublicRepoBrowser.ui.preview.sampleRepos
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.flowOf

// ──────────────────────────────────────────────────────────────
// Stateful wrapper — owns ViewModel, collects side-effects.
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun UserReposScreen(
    modifier: Modifier = Modifier,
    viewModel: UserReposViewModel = hiltViewModel(),
    onNavigateToDetail: (Repo) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repos = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is UserReposSideEffect.NavigateToDetail -> onNavigateToDetail(effect.repo)
                is UserReposSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    UserReposContent(
        modifier = modifier,
        uiState = uiState,
        repos = repos,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
    )
}

// ──────────────────────────────────────────────────────────────
// Stateless content — pure function of state + callbacks.
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReposContent(
    uiState: UserReposUiState,
    repos: LazyPagingItems<Repo>,
    onIntent: (UserReposIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.userLogin) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(UserReposIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            UserProfileHeader(uiState = uiState)

            UserStatsRow(uiState = uiState)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.repos_count, uiState.repoCount),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            RepoList(
                repos = repos,
                onRepoClick = { onIntent(UserReposIntent.RepoClicked(it)) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UserReposContentFullPreview() {
    MyApplicationTheme {
        UserReposContent(
            uiState = UserReposUiState(
                userLogin = "JakeWharton",
                userName = "Jake Wharton",
                avatarUrl = "https://avatars.githubusercontent.com/u/66577",
                bio = "Android developer at Google.",
                followers = 42_500,
                following = 12,
                location = "San Francisco, CA",
                repoCount = 128,
            ),
            repos = flowOf(PagingData.from(sampleRepos)).collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UserReposContentEmptyPreview() {
    MyApplicationTheme {
        UserReposContent(
            uiState = UserReposUiState(userLogin = "newuser"),
            repos = flowOf(PagingData.empty<Repo>()).collectAsLazyPagingItems(),
            onIntent = {},
        )
    }
}
