package com.rsav.githubPublicRepoBrowser.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleReposProvider
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RepoList(
    modifier: Modifier = Modifier,
    repos: ImmutableList<Repo>,
    onRepoClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = repos,
            key = { it.id },
        ) { repo ->
            RepoCardSimple(
                repo = repo,
                onClick = onRepoClick
            )
        }
    }
}

@Preview
@Composable
private fun RepoListPreview(@PreviewParameter(SampleReposProvider ::class) repos: ImmutableList<Repo>) {
    RepoList(
        repos = repos
    )
}
