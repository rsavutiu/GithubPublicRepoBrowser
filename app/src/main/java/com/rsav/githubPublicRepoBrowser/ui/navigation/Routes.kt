package com.rsav.githubPublicRepoBrowser.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

@Serializable
data class DetailRoute(val repoJson: String)

@Serializable
data class UserReposRoute(val userLogin: String)
