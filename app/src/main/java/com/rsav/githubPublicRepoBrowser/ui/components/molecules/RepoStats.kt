package com.rsav.githubPublicRepoBrowser.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.ForkCount
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.LanguageBadge
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.StarCount
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@Composable
fun RepoStats(
    starCount: Int,
    forkCount: Int,
    language: String?,
    languageColor: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (language != null) {
            LanguageBadge(language = language, colorHex = languageColor)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StarCount(count = starCount)
            ForkCount(count = forkCount)
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun RepoStatsPreview() {
    MyApplicationTheme {
        RepoStats(
            starCount = 45_000,
            forkCount = 1_200,
            language = "Kotlin",
            languageColor = "#A97BFF",
        )
    }
}
