package com.rsav.githubPublicRepoBrowser.ui.navigation

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Serializable DTO used exclusively for passing [Repo] through navigation routes.
 * Keeps the domain [Repo] free of serialization annotations.
 */
@Serializable
private data class NavRepoDto(
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
    val readmeText: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val licenseName: String? = null,
    val topics: List<String> = emptyList(),
    val openIssuesCount: Int = 0,
    val closedIssuesCount: Int = 0,
)

private fun Repo.toNavDto(): NavRepoDto = NavRepoDto(
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
    readmeText = readmeText,
    createdAt = createdAt,
    updatedAt = updatedAt,
    licenseName = licenseName,
    topics = topics,
    openIssuesCount = openIssuesCount,
    closedIssuesCount = closedIssuesCount,
)

private fun NavRepoDto.toDomain(): Repo = Repo(
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
    readmeText = readmeText,
    createdAt = createdAt,
    updatedAt = updatedAt,
    licenseName = licenseName,
    topics = topics,
    openIssuesCount = openIssuesCount,
    closedIssuesCount = closedIssuesCount,
)

private val navJson = Json { ignoreUnknownKeys = true }

/** Encode a [Repo] to a JSON string for passing through navigation routes. */
fun Repo.toNavJson(): String = navJson.encodeToString(NavRepoDto.serializer(), toNavDto())

/** Decode a [Repo] from a navigation JSON string. */
fun repoFromNavJson(json: String): Repo = navJson.decodeFromString(NavRepoDto.serializer(), json).toDomain()
