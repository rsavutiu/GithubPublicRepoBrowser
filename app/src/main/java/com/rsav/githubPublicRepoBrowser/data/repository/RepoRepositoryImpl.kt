package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.data.mapper.toDomainModel
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.RepoRepository
import javax.inject.Inject

class RepoRepositoryImpl @Inject constructor(
    private val dataSource: ApolloRepoDataSource,
) : RepoRepository {

    override suspend fun searchRepositories(query: String, first: Int): Result<List<Repo>> {
        return try {
            val data = dataSource.searchRepositories(query, first)
            val repos = data.search.nodes?.mapNotNull { node ->
                node?.onRepository?.toDomainModel()
            } ?: emptyList()
            Result.success(repos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
