package com.rsav.githubPublicRepoBrowser.ui.components.organisms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.GithubAvatar
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleRepoProvider
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

const val avatarSize = 64
@Composable
fun RepoCardSimple(
    repo: Repo,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(repo.url) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            if (repo.ownerAvatarUrl.isNullOrBlank()) {
                Box(modifier = Modifier.size(avatarSize.dp))
            }
            else {
                GithubAvatar(
                    modifier = Modifier.size(avatarSize.dp),
                    url = repo.ownerAvatarUrl,
                    isOrganization = repo.ownerType == "Organization",
                )
            }
            Column(modifier = Modifier) {
                Text(
                    text = repo.nameWithOwner,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (!repo.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = repo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RepoCardPreview(
    @PreviewParameter(SampleRepoProvider::class) repo: Repo
) {
    MyApplicationTheme {
        RepoCardSimple(
            repo = repo,
            onClick = {},
        )
    }
}
