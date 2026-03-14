package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.RepositoryDetailsQuery
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import javax.inject.Inject

class RepositoryDetailsImpl@Inject constructor(
    private val dataSource: ApolloRepoDataSource,
): IRepositoryDetails {
    override suspend fun getRepositoryDetails(
        name: String,
        owner: String
    ): String? {
        val data: RepositoryDetailsQuery.Data = dataSource.getRepositoryReadme(owner = owner, name = name)
        val repo = data.repository ?: return null
        return repo.readmeUpper?.onBlob?.text
            ?: repo.readmeLower?.onBlob?.text
    }
}