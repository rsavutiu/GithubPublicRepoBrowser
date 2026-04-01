package com.rsav.githubPublicRepoBrowser.ui.screen.userrepos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.GithubAvatar
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@Composable
internal fun UserProfileHeader(
    uiState: UserReposUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        uiState.avatarUrl?.let {
            GithubAvatar(
                url = it,
                modifier = Modifier.size(64.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            uiState.userName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = uiState.userLogin,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            uiState.bio?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun UserProfileHeaderFullPreview() {
    MyApplicationTheme {
        UserProfileHeader(
            uiState = UserReposUiState(
                userLogin = "JakeWharton",
                userName = "Jake Wharton",
                avatarUrl = "https://avatars.githubusercontent.com/u/66577",
                bio = "Android developer at Google. Creator of Retrofit, Butter Knife, and other open-source libraries.",
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileHeaderMinimalPreview() {
    MyApplicationTheme {
        UserProfileHeader(
            uiState = UserReposUiState(userLogin = "octocat"),
        )
    }
}
