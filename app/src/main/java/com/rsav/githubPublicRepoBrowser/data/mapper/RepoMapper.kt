package com.rsav.githubPublicRepoBrowser.data.mapper

import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.domain.model.Repo

fun SearchRepositoriesQuery.OnRepository.toDomainModel(): Repo {
    return Repo(
        id = id,
        name = name,
        nameWithOwner = nameWithOwner,
        description = description,
        url = url.toString(),
        stargazerCount = stargazerCount,
        forkCount = forkCount,
        languageName = primaryLanguage?.name,
        languageColor = primaryLanguage?.color,
        ownerLogin = owner.login,
        ownerAvatarUrl = owner.avatarUrl.toString(),
    )
}
