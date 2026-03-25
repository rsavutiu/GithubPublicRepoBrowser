package com.rsav.githubPublicRepoBrowser.data.local.db

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.serialization.json.Json

fun Repo.toEntity(readmeHtml: String? = null): FavoriteRepoEntity = FavoriteRepoEntity(
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
    topics = Json.encodeToString(topics),
    openIssuesCount = openIssuesCount,
    closedIssuesCount = closedIssuesCount,
    readmeHtml = readmeHtml,
)

fun FavoriteRepoEntity.toDomainModel(): Repo = Repo(
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
    topics = try {
        Json.decodeFromString<List<String>>(topics)
    } catch (_: Exception) {
        emptyList()
    },
    openIssuesCount = openIssuesCount,
    closedIssuesCount = closedIssuesCount,
)
