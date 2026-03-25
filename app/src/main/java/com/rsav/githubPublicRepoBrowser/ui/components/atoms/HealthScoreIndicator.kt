package com.rsav.githubPublicRepoBrowser.ui.components.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rsav.githubPublicRepoBrowser.domain.model.HealthScore
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.computeHealthScore

@Composable
fun HealthScoreIndicator(
    repo: Repo,
    modifier: Modifier = Modifier,
) {
    val health = remember(repo.id, repo.stargazerCount, repo.forkCount, repo.updatedAt, repo.licenseName, repo.topics.size, repo.openIssuesCount, repo.closedIssuesCount) {
        computeHealthScore(repo)
    }

    HealthScoreRing(health = health, modifier = modifier)
}

@Composable
private fun HealthScoreRing(
    health: HealthScore,
    modifier: Modifier = Modifier,
) {
    val scoreColor = Color(health.color)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val sweepAngle = (health.score / 100f) * 360f

    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Track (background ring)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            // Score arc
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            text = health.score.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = scoreColor,
        )
    }
}
