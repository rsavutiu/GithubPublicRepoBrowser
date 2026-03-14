package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.SearchBar
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.RepoList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoSearchScreen(
    onNavigateToDetail: (Repo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoSearchViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repos = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is SearchSideEffect.NavigateToDetail -> {
                    onNavigateToDetail(sideEffect.repo)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("GitHub Repositories") })
        },
    ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .fillMaxSize(),
            ) {
                SearchBar(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    query = uiState.query,
                    onQueryChanged = { viewModel.onIntent(SearchIntent.QueryChanged(it)) },
                    onSearch = { viewModel.onIntent(SearchIntent.Search) },
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                                text = error.localizedMessage ?: "Unknown error",
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
                                text = "No repositories found",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    else -> {
                        RepoList(
                            modifier = modifier.padding(horizontal = 8.dp),
                            repos = repos,
                            onRepoClick = { viewModel.onIntent(SearchIntent.RepoClicked(it)) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                        )
                    }
                }
        }
    }
}

@Preview
@Composable
private fun RepoSearchScreenPreview() {
    RepoSearchScreen(
        onNavigateToDetail = {}
    )
}
