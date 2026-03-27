package com.rsav.githubPublicRepoBrowser.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Repo(
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
