package com.rsav.githubPublicRepoBrowser.ui.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleReposProvider
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RepoStaggeredColumn(
    modifier: Modifier = Modifier,
    repos: ImmutableList<Repo>,
    onRepoClick: (String) -> Unit = {}
) {
    val gridState = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = gridState,
        columns = StaggeredGridCells.Fixed(2),
    ) {
        items(repos.size,
            key = { repos[it].id }) {
            RepoCard(
                repo = repos[it],
                onClick = onRepoClick
            )
        }
    }
}

@Preview
@Composable
private fun RepoStaggeredColumnPreview(@PreviewParameter(SampleReposProvider::class) repos: ImmutableList<Repo>) {
    RepoStaggeredColumn(
        repos = repos
    )
}
