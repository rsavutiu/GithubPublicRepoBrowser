package com.rsav.githubPublicRepoBrowser.domain.repository

import com.rsav.githubPublicRepoBrowser.domain.model.Repo

//Returns same object but with md blob if available (readmetext)
interface IRepositoryDetails {
    suspend fun getRepositoryDetails(name: String, owner: String): String?
}
