package com.rsav.githubPublicRepoBrowser.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_repos")
data class FavoriteRepoEntity(
    @PrimaryKey val id: String,
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
    val ownerType: String,
    val createdAt: String?,
    val updatedAt: String?,
    val licenseName: String?,
    val topics: String, // JSON-serialized list
    val openIssuesCount: Int,
    val closedIssuesCount: Int,
    val readmeHtml: String?, // cached README
    val savedAt: Long = System.currentTimeMillis(),
)
