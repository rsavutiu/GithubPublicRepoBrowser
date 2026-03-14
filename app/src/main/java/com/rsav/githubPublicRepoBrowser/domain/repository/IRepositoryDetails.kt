package com.rsav.githubPublicRepoBrowser.domain.repository

/** Returns the README content as a string, or null if unavailable. */
interface IRepositoryDetails {
    suspend fun getRepositoryDetails(name: String, owner: String): String?
}
