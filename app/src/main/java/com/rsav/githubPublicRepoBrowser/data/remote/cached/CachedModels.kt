package com.rsav.githubPublicRepoBrowser.data.remote.cached

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.serialization.Serializable

/**
 * Response models for the static JSON served by GitHub Pages
 * from the github-trending-cache repository.
 *
 * [CachedRepoDto] is the serializable DTO; it maps to the domain [Repo].
 */

@Serializable
data class CachedTrendingResponse(
    val generatedAt: String,
    val period: String? = null,
    val topic: String? = null,
    val totalCount: Int,
    val repos: List<CachedRepoDto>,
)

@Serializable
data class CachedIndexResponse(
    val lastUpdated: String,
    val availableTopics: List<String>,
    val periods: List<String>,
)

@Serializable
data class CachedRepoDto(
    val id: String,
    val name: String,
    val nameWithOwner: String,
    val description: String? = null,
    val url: String,
    val stargazerCount: Int,
    val forkCount: Int,
    val languageName: String? = null,
    val languageColor: String? = null,
    val ownerLogin: String,
    val ownerAvatarUrl: String? = null,
    val ownerType: String = "User",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val licenseName: String? = null,
    val topics: List<String> = emptyList(),
    val openIssuesCount: Int = 0,
    val closedIssuesCount: Int = 0,
)

fun CachedRepoDto.toDomainModel(): Repo = Repo(
    id = id,
    name = name,
    nameWithOwner = nameWithOwner,
    description = description,
    url = url,
    stargazerCount = stargazerCount,
    forkCount = forkCount,
    languageName = languageName,
    languageColor = languageColor,
    ownerLogin = ownerLogin,
    ownerAvatarUrl = ownerAvatarUrl,
    ownerType = ownerType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    licenseName = licenseName,
    topics = topics,
    openIssuesCount = openIssuesCount,
    closedIssuesCount = closedIssuesCount,
)
