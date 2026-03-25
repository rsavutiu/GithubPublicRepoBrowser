package com.rsav.githubPublicRepoBrowser.domain.model

data class UserProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val bio: String?,
    val company: String?,
    val location: String?,
    val followers: Int,
    val following: Int,
    val repoCount: Int,
)
