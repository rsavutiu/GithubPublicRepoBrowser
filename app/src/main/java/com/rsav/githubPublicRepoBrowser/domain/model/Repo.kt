package com.rsav.githubPublicRepoBrowser.domain.model

data class Repo(
    val id: String,
    val name: String,
    val nameWithOwner: String,
    val description: String?,
    val url: String,
    val stargazerCount: Int,
    val forkCount: Int,
    val languageName: String?,
    val languageColor: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val ownerType: String = "User",
)
