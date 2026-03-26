package com.rsav.githubPublicRepoBrowser.data.remote.cached

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.serialization.Serializable

/**
 * Response models for the static JSON served by GitHub Pages
 * from the github-trending-cache repository.
 *
 * Field names match [Repo] exactly, so repos deserialize directly.
 */

@Serializable
data class CachedTrendingResponse(
    val generatedAt: String,
    val period: String? = null,
    val topic: String? = null,
    val totalCount: Int,
    val repos: List<Repo>,
)

@Serializable
data class CachedIndexResponse(
    val lastUpdated: String,
    val availableTopics: List<String>,
    val periods: List<String>,
)
