package com.rsav.githubPublicRepoBrowser.data.mapper

import com.rsav.githubPublicRepoBrowser.data.remote.rest.RestRepo
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.util.L

private const val TAG = "RestRepoMapper"
private const val MAX_DESCRIPTION_LENGTH = 300

fun RestRepo.toDomainModel(): Repo {
    L.d(TAG, "mapping $fullName — stars=$stargazersCount, forks=$forksCount, lang=$language, topics=$topics")
    return Repo(
        id = nodeId,
        name = name,
        nameWithOwner = fullName,
        description = description?.take(MAX_DESCRIPTION_LENGTH),
        url = htmlUrl,
        stargazerCount = stargazersCount,
        forkCount = forksCount,
        languageName = language,
        languageColor = null, // REST API doesn't return language color
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        ownerType = owner.type,
        createdAt = createdAt,
        updatedAt = updatedAt,
        licenseName = license?.spdxId,
        topics = topics,
        openIssuesCount = openIssuesCount,
        closedIssuesCount = 0, // REST search API doesn't return closed issues count
    )
}
