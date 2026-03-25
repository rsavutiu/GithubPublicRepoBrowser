package com.rsav.githubPublicRepoBrowser.domain.repository

import com.rsav.githubPublicRepoBrowser.domain.model.UserProfile

interface IUserProfileRepository {
    suspend fun getUserProfile(login: String): UserProfile?
}
