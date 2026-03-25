package com.rsav.githubPublicRepoBrowser.domain.repository

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.coroutines.flow.Flow

interface IFavoriteRepository {
    fun getAllFavorites(): Flow<List<Repo>>
    fun getAllFavoriteIds(): Flow<List<String>>
    fun isFavorite(repoId: String): Flow<Boolean>
    suspend fun toggleFavorite(repo: Repo, readmeHtml: String? = null)
}
