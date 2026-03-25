package com.rsav.githubPublicRepoBrowser.data.mapper

import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.util.L

private const val TAG = "RepoMapper"

private const val MAX_DESCRIPTION_LENGTH = 300

fun SearchRepositoriesQuery.OnRepository.toDomainModel(): Repo {
    val topicNames = repositoryTopics.nodes?.mapNotNull { it?.topic?.name } ?: emptyList()
    L.d(TAG, "mapping $nameWithOwner — stars=$stargazerCount, forks=$forkCount, lang=${primaryLanguage?.name}, topics=$topicNames")
    return Repo(
        id = id,
        name = name,
        nameWithOwner = nameWithOwner,
        description = description?.take(MAX_DESCRIPTION_LENGTH),
        url = url,
        stargazerCount = stargazerCount,
        forkCount = forkCount,
        languageName = primaryLanguage?.name,
        languageColor = primaryLanguage?.color,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        ownerType = owner.__typename,
        createdAt = createdAt,
        updatedAt = updatedAt,
        licenseName = licenseInfo?.spdxId,
        topics = topicNames,
        openIssuesCount = openIssues.totalCount,
        closedIssuesCount = closedIssues.totalCount,
    )
}
