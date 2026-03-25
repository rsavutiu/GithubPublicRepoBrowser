package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.rest.RestRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.ApiMode
import com.rsav.githubPublicRepoBrowser.domain.model.UserProfile
import com.rsav.githubPublicRepoBrowser.domain.repository.IUserProfileRepository
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class DelegatingUserProfileRepository @Inject constructor(
    private val apolloDataSource: ApolloRepoDataSource,
    private val restDataSource: RestRepoDataSource,
    private val apiModeRepository: ApiModeRepository,
) : IUserProfileRepository {

    override suspend fun getUserProfile(login: String): UserProfile? {
        val mode = apiModeRepository.apiMode.value
        L.d(TAG, "getUserProfile via $mode")
        return when (mode) {
            ApiMode.GRAPHQL -> getFromGraphQl(login)
            ApiMode.REST -> getFromRest(login)
        }
    }

    private suspend fun getFromGraphQl(login: String): UserProfile? {
        val data = apolloDataSource.getUserProfile(login)
        val user = data.user ?: return null
        return UserProfile(
            login = user.login,
            name = user.name,
            avatarUrl = user.avatarUrl?.toString(),
            bio = user.bio,
            company = user.company,
            location = user.location,
            followers = user.followers.totalCount,
            following = user.following.totalCount,
            repoCount = user.repositories.totalCount,
        )
    }

    private suspend fun getFromRest(login: String): UserProfile {
        val user = restDataSource.getUserProfile(login)
        return UserProfile(
            login = user.login,
            name = user.name,
            avatarUrl = user.avatarUrl,
            bio = user.bio,
            company = user.company,
            location = user.location,
            followers = user.followers,
            following = user.following,
            repoCount = user.publicRepos,
        )
    }

    companion object {
        private const val TAG = "DelegatingUserProfile"
    }
}
