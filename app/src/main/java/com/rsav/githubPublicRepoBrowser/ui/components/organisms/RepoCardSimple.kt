package com.rsav.githubPublicRepoBrowser.ui.components.organisms

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.FormattedDate
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.GithubAvatar
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.RepoBadges
import com.rsav.githubPublicRepoBrowser.ui.preview.SampleRepoProvider
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

private const val AVATAR_SIZE = 48
private const val SHARED_ANIM_MS = 1000

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun RepoCardSimple(
    repo: Repo,
    onClick: (Repo) -> Unit,
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(repo) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!repo.ownerAvatarUrl.isNullOrBlank()) {
                    val avatarModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "avatar-${repo.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ -> tween(SHARED_ANIM_MS) },
                                )
                                .size(AVATAR_SIZE.dp)
                        }
                    } else {
                        Modifier.size(AVATAR_SIZE.dp)
                    }

                    GithubAvatar(
                        modifier = avatarModifier,
                        url = repo.ownerAvatarUrl,
                        isOrganization = repo.ownerType == "Organization",
                    )
                }

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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }

            if (!repo.createdAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                FormattedDate(isoDate = repo.createdAt)
            }

            if (!repo.updatedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                FormattedDate(prefix = stringResource(R.string.last_update_prefix), isoDate = repo.updatedAt)
            }

            RepoBadges(repo = repo)

            if (repo.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    maxLines = 1,
                ) {
                    repo.topics.forEachIndexed { index, topic ->
                        if (index > 0) {
                            Text(
                                text = " \u00b7 ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onTopicClick(topic) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RepoCardPreview(
    @PreviewParameter(SampleRepoProvider::class) repo: Repo,
) {
    MyApplicationTheme {
        RepoCardSimple(
            repo = repo,
            onClick = {},
        )
    }
}
