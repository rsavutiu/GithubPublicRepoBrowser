package com.rsav.githubPublicRepoBrowser.data.remote.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestSearchResponse(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("incomplete_results") val incompleteResults: Boolean = false,
    val items: List<RestRepo>,
)

@Serializable
data class RestRepo(
    val id: Long,
    @SerialName("node_id") val nodeId: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("stargazers_count") val stargazersCount: Int = 0,
    @SerialName("forks_count") val forksCount: Int = 0,
    val language: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val owner: RestOwner,
    val license: RestLicense? = null,
    val topics: List<String> = emptyList(),
    @SerialName("open_issues_count") val openIssuesCount: Int = 0,
)

@Serializable
data class RestOwner(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val type: String = "User",
)

@Serializable
data class RestLicense(
    @SerialName("spdx_id") val spdxId: String? = null,
)

@Serializable
data class RestReadmeResponse(
    val content: String? = null,
    val encoding: String? = null,
    @SerialName("download_url") val downloadUrl: String? = null,
)

@Serializable
data class RestUserProfile(
    val login: String,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val followers: Int = 0,
    val following: Int = 0,
    @SerialName("public_repos") val publicRepos: Int = 0,
)
