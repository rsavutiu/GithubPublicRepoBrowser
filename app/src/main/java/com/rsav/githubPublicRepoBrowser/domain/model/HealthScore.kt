package com.rsav.githubPublicRepoBrowser.domain.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class HealthScore(
    val score: Int,
    val label: String,
    val breakdown: List<ScoreComponent>,
) {
    val color: Long
        get() = when {
            score >= 80 -> 0xFF4CAF50  // Green
            score >= 60 -> 0xFF8BC34A  // Light green
            score >= 40 -> 0xFFFF9800  // Orange
            score >= 20 -> 0xFFFF5722  // Deep orange
            else -> 0xFF9E9E9E         // Grey
        }
}

data class ScoreComponent(
    val name: String,
    val earned: Float,
    val max: Float,
)

/**
 * Computes a 0-100 health score for a repository.
 *
 * The score is a weighted sum of 7 signals, each scored independently:
 *
 * | # | Signal               | Max pts | What it measures                          |
 * |---|----------------------|---------|-------------------------------------------|
 * | 1 | Update recency       | 25      | Days since last commit/update              |
 * | 2 | Star growth velocity | 25      | Stars per year (popularity momentum)       |
 * | 3 | Community engagement | 10      | Fork-to-star ratio (contribution interest) |
 * | 4 | Issue responsiveness | 15      | Ratio of closed to total issues            |
 * | 5 | Documentation        | 10      | Has description (proxy for README)         |
 * | 6 | License              | 10      | Has an OSS license                         |
 * | 7 | Discoverability      |  5      | Has topics/tags                            |
 * |   | **Total**            | **100** |                                           |
 *
 * Final score = sum of earned points, clamped to 0-100.
 */
fun computeHealthScore(repo: Repo): HealthScore {
    val components = mutableListOf<ScoreComponent>()

    // 1. Update recency (25 pts)
    var recencyScore = 0f
    val updatedAt = repo.updatedAt
    if (updatedAt != null) {
        try {
            val updated = ZonedDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME)
            val daysSince = ChronoUnit.DAYS.between(updated, ZonedDateTime.now())
            recencyScore = when {
                daysSince <= 7 -> 25f
                daysSince <= 30 -> 21f
                daysSince <= 90 -> 16f
                daysSince <= 180 -> 10f
                daysSince <= 365 -> 5f
                else -> 0f
            }
        } catch (_: Exception) { /* 0 points */ }
    }
    components.add(ScoreComponent("Update recency", recencyScore, 25f))

    // 2. Star growth velocity (25 pts)
    var growthScore = 0f
    val createdAt = repo.createdAt
    if (createdAt != null) {
        try {
            val created = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
            val ageYears = (ChronoUnit.DAYS.between(created, ZonedDateTime.now()) / 365.0).coerceAtLeast(1.0 / 365.0)
            val starsPerYear = repo.stargazerCount / ageYears
            growthScore = when {
                starsPerYear >= 1000 -> 25f
                starsPerYear >= 500 -> 21f
                starsPerYear >= 100 -> 17f
                starsPerYear >= 50 -> 13f
                starsPerYear >= 10 -> 9f
                starsPerYear >= 1 -> 4f
                else -> 0f
            }
        } catch (_: Exception) { /* 0 points */ }
    }
    components.add(ScoreComponent("Star growth", growthScore, 20f))

    // 3. Community engagement — fork ratio (10 pts)
    var forkScore = 0f
    if (repo.stargazerCount > 0) {
        val forkRatio = repo.forkCount.toFloat() / repo.stargazerCount
        forkScore = when {
            forkRatio >= 0.3f -> 10f
            forkRatio >= 0.15f -> 8f
            forkRatio >= 0.05f -> 5f
            forkRatio > 0f -> 3f
            else -> 0f
        }
    }
    components.add(ScoreComponent("Community", forkScore, 15f))

    // 4. Issue responsiveness (15 pts)
    val totalIssues = repo.openIssuesCount + repo.closedIssuesCount
    var issueScore = 0f
    if (totalIssues > 0) {
        val closeRatio = repo.closedIssuesCount.toFloat() / totalIssues
        issueScore = when {
            closeRatio >= 0.9f -> 15f   // 90%+ closed — excellent maintenance
            closeRatio >= 0.75f -> 12f  // 75%+ — good
            closeRatio >= 0.5f -> 8f    // 50%+ — fair
            closeRatio >= 0.25f -> 4f   // 25%+ — below average
            else -> 0f                   // <25% — issues pile up
        }
    } else {
        // No issues at all — neutral, give half points
        issueScore = 7f
    }
    components.add(ScoreComponent("Issue response", issueScore, 15f))

    // 5. Has description / README (10 pts)
    val docScore = if (!repo.description.isNullOrBlank()) 10f else 0f
    components.add(ScoreComponent("Documentation", docScore, 10f))

    // 6. Has license (10 pts)
    val licenseScore = if (!repo.licenseName.isNullOrBlank() && repo.licenseName != "NOASSERTION") 10f else 0f
    components.add(ScoreComponent("License", licenseScore, 10f))

    // 7. Has topics (5 pts)
    val topicScore = when {
        repo.topics.size >= 3 -> 5f
        repo.topics.size >= 1 -> 3f
        else -> 0f
    }
    components.add(ScoreComponent("Discoverability", topicScore, 5f))

    val total = components.sumOf { it.earned.toDouble() }.toFloat()
    val score = total.toInt().coerceIn(0, 100)

    val label = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        score >= 20 -> "Poor"
        else -> "Low"
    }

    return HealthScore(score = score, label = label, breakdown = components)
}
