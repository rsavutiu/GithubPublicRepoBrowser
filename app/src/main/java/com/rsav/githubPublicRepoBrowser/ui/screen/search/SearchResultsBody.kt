package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.RepoList
import com.rsav.githubPublicRepoBrowser.ui.preview.sampleRepos
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun RepoResultsBody(
    repos: LazyPagingItems<Repo>,
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    sharedTransitionScope: SharedTransitionScope?,
) {
    when {
        repos.loadState.refresh is LoadState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        repos.loadState.refresh is LoadState.Error -> {
            val error = (repos.loadState.refresh as LoadState.Error).error
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = error.localizedMessage ?: stringResource(R.string.unknown_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        repos.itemCount == 0 && repos.loadState.refresh is LoadState.NotLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_repos_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        else -> {
            RepoList(
                modifier = Modifier.padding(horizontal = 8.dp),
                repos = repos,
                onRepoClick = { onIntent(SearchIntent.RepoClicked(it)) },
                onTopicClick = { onIntent(SearchIntent.TopicToggled(it)) },
                contributorCounts = uiState.contributorCounts,
                onRequestContributorCount = { owner, repoName ->
                    onIntent(SearchIntent.LoadContributorCount(owner, repoName))
                },
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
private fun ResultsBodyWithReposPreview() {
    MyApplicationTheme {
        RepoResultsBody(
            repos = flowOf(PagingData.from(sampleRepos)).collectAsLazyPagingItems(),
            uiState = SearchUiState(),
            onIntent = {},
            animatedVisibilityScope = null,
            sharedTransitionScope = null,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultsBodyEmptyPreview() {
    MyApplicationTheme {
        RepoResultsBody(
            repos = flowOf(PagingData.empty<Repo>()).collectAsLazyPagingItems(),
            uiState = SearchUiState(),
            onIntent = {},
            animatedVisibilityScope = null,
            sharedTransitionScope = null,
        )
    }
}
