package com.rsav.githubPublicRepoBrowser.ui.screen.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterChipsRow(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TrendingPeriod.entries.forEach { period ->
            FilterChip(
                selected = uiState.trendingPeriod == period,
                onClick = {
                    if (uiState.trendingPeriod != period) {
                        onIntent(SearchIntent.TrendingPeriodChanged(period))
                    } else {
                        onIntent(SearchIntent.TrendingPeriodChanged(null))
                    }
                },
                label = { Text(stringResource(period.labelRes)) },
            )
        }

        uiState.selectedLanguage?.let { lang ->
            ActiveFilterChip(
                label = lang.name,
                icon = Icons.Default.Code,
                onDismiss = { onIntent(SearchIntent.ProgrammingLanguageSelected(null)) },
                dismissContentDescription = stringResource(R.string.remove_lang_filter_desc),
            )
        }

        uiState.selectedSpokenLanguage?.let { lang ->
            ActiveFilterChip(
                label = lang.name,
                icon = Icons.Default.Translate,
                onDismiss = { onIntent(SearchIntent.SpokenLanguageSelected(null)) },
                dismissContentDescription = stringResource(R.string.remove_spoken_lang_filter_desc),
            )
        }

        uiState.selectedTopics.forEach { topic ->
            ActiveFilterChip(
                label = topic,
                icon = Icons.Default.Tag,
                onDismiss = { onIntent(SearchIntent.TopicToggled(topic)) },
                dismissContentDescription = stringResource(R.string.remove_topic_filter_desc),
            )
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    dismissContentDescription: String,
) {
    InputChip(
        selected = true,
        onClick = onDismiss,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = dismissContentDescription,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun FilterChipsDefaultPreview() {
    MyApplicationTheme {
        FilterChipsRow(
            uiState = SearchUiState(trendingPeriod = TrendingPeriod.THIS_WEEK),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsWithActiveFiltersPreview() {
    MyApplicationTheme {
        FilterChipsRow(
            uiState = SearchUiState(
                trendingPeriod = TrendingPeriod.THIS_MONTH,
                selectedLanguage = ProgrammingLanguage("Kotlin"),
                selectedSpokenLanguage = SpokenLanguage("English", "en"),
                selectedTopics = setOf("android", "jetpack-compose"),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsNoTrendingPreview() {
    MyApplicationTheme {
        FilterChipsRow(
            uiState = SearchUiState(
                trendingPeriod = null,
                selectedLanguage = ProgrammingLanguage("Rust"),
                selectedTopics = setOf("cli", "performance"),
            ),
            onIntent = {},
        )
    }
}
