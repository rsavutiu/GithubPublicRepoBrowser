package com.rsav.githubPublicRepoBrowser.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleRepoProvider
import com.rsav.githubPublicRepoBrowser.ui.preview.sampleRepos
import kotlinx.coroutines.flow.flowOf

@Composable
fun RepoList(
    repos: LazyPagingItems<Repo>,
    modifier: Modifier = Modifier,
    onRepoClick: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = repos.itemCount,
            key = { index -> repos.peek(index)?.id ?: index },
        ) { index ->
            val repo = repos[index]
            if (repo != null) {
                RepoCardSimple(
                    repo = repo,
                    onClick = onRepoClick,
                )
            }
        }

        if (repos.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview
@Composable
private fun RepoListPreview() {
    val repos = flowOf(PagingData.from(sampleRepos)).collectAsLazyPagingItems()
    RepoList(
        repos = repos,
        onRepoClick = {},
    )
}