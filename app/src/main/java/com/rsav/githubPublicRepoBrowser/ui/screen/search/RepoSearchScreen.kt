package com.rsav.githubPublicRepoBrowser.ui.screen.search

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rsav.githubPublicRepoBrowser.ui.components.molecules.SearchBar
import com.rsav.githubPublicRepoBrowser.ui.components.organisms.RepoList
import androidx.core.net.toUri

@Composable
fun RepoSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: RepoSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //Initially load random public repositories without a search query
    LaunchedEffect(Unit) {
        viewModel.onIntent(SearchIntent.Search)
    }

    //Details page to be implemented later
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { sideEffect ->
            when (sideEffect) {
                is SearchSideEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, sideEffect.url.toUri())
                    context.startActivity(intent)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SearchBar(
            query = uiState.query,
            onQueryChanged = { viewModel.onIntent(SearchIntent.QueryChanged(it)) },
            onSearch = { viewModel.onIntent(SearchIntent.Search) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            uiState.repos.isEmpty() && uiState.query.isNotBlank() && !uiState.isLoading -> {
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
                    repos = uiState.repos,
                    onRepoClick = { viewModel.onIntent(SearchIntent.RepoClicked(it)) },
                )
            }
        }
    }
}

@Preview
@Composable
fun RepoSearchScreenPreview() {
    RepoSearchScreen()
}
