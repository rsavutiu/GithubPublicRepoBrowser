package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class RepoBadge(
    val text: String,
    val icon: ImageVector,
    val color: Color,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepoBadges(
    repo: Repo,
    modifier: Modifier = Modifier,
) {
    val badges = remember(repo.id, repo.stargazerCount, repo.forkCount, repo.updatedAt) {
        buildBadges(repo)
    }

    if (badges.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        badges.forEach { badge ->
            BadgeItem(badge)
        }
    }
}

@Composable
private fun BadgeItem(badge: RepoBadge) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp),
    ) {
        Icon(
            imageVector = badge.icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = badge.color,
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = badge.text,
            style = MaterialTheme.typography.labelSmall,
            color = badge.color,
        )
    }
}

private fun buildBadges(repo: Repo): List<RepoBadge> {
    val badges = mutableListOf<RepoBadge>()

    // Popularity badge
    when {
        repo.stargazerCount >= 10_000 -> badges.add(
            RepoBadge("\u2B50 ${formatBadgeCount(repo.stargazerCount)}", Icons.Default.Star, Color(0xFFDAA520))
        )
        repo.stargazerCount >= 1_000 -> badges.add(
            RepoBadge("Popular", Icons.Default.TrendingUp, Color(0xFF4CAF50))
        )
    }

    // Activity / staleness badge
    val updatedAt = repo.updatedAt
    if (updatedAt != null) {
        try {
            val updated = ZonedDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME)
            val now = ZonedDateTime.now()
            val daysSinceUpdate = ChronoUnit.DAYS.between(updated, now)

            when {
                daysSinceUpdate <= 7 -> badges.add(
                    RepoBadge("Active", Icons.Default.LocalFireDepartment, Color(0xFFFF5722))
                )
                daysSinceUpdate > 365 -> badges.add(
                    RepoBadge("Inactive ${daysSinceUpdate / 365}y", Icons.Default.Warning, Color(0xFF9E9E9E))
                )
                daysSinceUpdate > 180 -> badges.add(
                    RepoBadge("Stale", Icons.Default.Warning, Color(0xFFFF9800))
                )
            }
        } catch (_: Exception) { /* skip badge */ }
    }

    // License badge
    val license = repo.licenseName
    when {
        license.isNullOrBlank() || license == "NOASSERTION" -> badges.add(
            RepoBadge("No license", Icons.Default.Warning, Color(0xFFFF9800))
        )
        else -> badges.add(
            RepoBadge(license, Icons.Default.Gavel, Color(0xFF78909C))
        )
    }

    // High fork ratio — indicates community contributions
    if (repo.forkCount > 100 && repo.stargazerCount > 0) {
        val forkRatio = repo.forkCount.toFloat() / repo.stargazerCount
        if (forkRatio > 0.3f) {
            badges.add(
                RepoBadge("${formatBadgeCount(repo.forkCount)} forks", Icons.Default.TrendingUp, Color(0xFF2196F3))
            )
        }
    }

    return badges
}

private fun formatBadgeCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}.${(count % 1_000_000) / 100_000}M"
    count >= 1_000 -> "${count / 1_000}.${(count % 1_000) / 100}k"
    else -> count.toString()
}
