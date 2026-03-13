package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import androidx.core.graphics.toColorInt

@Composable
fun LanguageBadge(
    language: String,
    colorHex: String?,
    modifier: Modifier = Modifier,
) {
    val dotColor = colorHex?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.outline

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = language,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun parseHexColor(hex: String): Color {
    return try {
        Color(hex.toColorInt())
    } catch (_: IllegalArgumentException) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageBadgePreview() {
    MyApplicationTheme {
        LanguageBadge(language = "Kotlin", colorHex = "#A97BFF")
    }
}
