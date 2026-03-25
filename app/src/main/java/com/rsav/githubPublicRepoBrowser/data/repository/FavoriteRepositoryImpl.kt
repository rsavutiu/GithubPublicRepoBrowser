package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.data.local.db.FavoriteRepoDao
import com.rsav.githubPublicRepoBrowser.data.local.db.toDomainModel
import com.rsav.githubPublicRepoBrowser.data.local.db.toEntity
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.IFavoriteRepository
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteRepoDao,
) : IFavoriteRepository {

    override fun getAllFavorites(): Flow<List<Repo>> =
        dao.getAllFavorites().map { entities -> entities.map { it.toDomainModel() } }

    override fun getAllFavoriteIds(): Flow<List<String>> =
        dao.getAllFavoriteIds()

    override fun isFavorite(repoId: String): Flow<Boolean> =
        dao.isFavorite(repoId)

    override suspend fun toggleFavorite(repo: Repo, readmeHtml: String?) {
        val isFav = dao.isFavorite(repo.id).first()
        if (isFav) {
            L.d(TAG, "Removing favorite: ${repo.nameWithOwner}")
            dao.delete(repo.id)
        } else {
            L.d(TAG, "Adding favorite: ${repo.nameWithOwner}")
            dao.insert(repo.toEntity(readmeHtml))
        }
    }

    companion object {
        private const val TAG = "FavoriteRepo"
    }
}
