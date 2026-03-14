package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ISO_PARSER = DateTimeFormatter.ISO_DATE_TIME
private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)

fun formatIsoDate(iso: String): String {
    return try {
        val dateTime = ZonedDateTime.parse(iso, ISO_PARSER)
        dateTime.format(DISPLAY_FORMAT)
    } catch (_: Exception) {
        iso
    }
}

@Composable
fun FormattedDate(
    isoDate: String,
    modifier: Modifier = Modifier,
    prefix: String = "Created ",
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val formatted = remember(isoDate) { formatIsoDate(isoDate) }
    Text(
        text = "$prefix$formatted",
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
