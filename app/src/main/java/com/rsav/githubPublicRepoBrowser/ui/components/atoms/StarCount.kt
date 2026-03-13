package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme
import java.util.Locale

@Composable
fun StarCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Stars",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(locale = Locale.getDefault(), format ="%.1fM", args = arrayOf(count / 1_000_000.0))
        count >= 1_000 -> String.format(locale = Locale.getDefault(), format = "%.1fk", args = arrayOf(count / 1_000.0))
        else -> count.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun StarCountPreview() {
    MyApplicationTheme {
        StarCount(count = 12_500)
    }
}
