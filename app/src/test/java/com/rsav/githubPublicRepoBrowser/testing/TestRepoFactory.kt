package com.rsav.githubPublicRepoBrowser.testing

import com.rsav.githubPublicRepoBrowser.domain.model.Repo

/** Shared test helper to create [Repo] instances with sensible defaults. */
fun createTestRepo(
    id: String = "1",
    name: String = "test-repo-$id",
    nameWithOwner: String = "owner/$name",
    description: String? = "A test repository",
    url: String = "https://github.com/$nameWithOwner",
    stargazerCount: Int = 100,
    forkCount: Int = 20,
    languageName: String? = "Kotlin",
    languageColor: String? = "#A97BFF",
    ownerLogin: String = "owner",
    ownerAvatarUrl: String? = "https://github.com/$ownerLogin.png",
): Repo = Repo(
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
)
