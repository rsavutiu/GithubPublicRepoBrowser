package com.rsav.githubPublicRepoBrowser.domain.repository

import com.rsav.githubPublicRepoBrowser.domain.model.Repo

interface RepoRepository {
    suspend fun searchRepositories(query: String, first: Int = 20): Result<List<Repo>>
}
