package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.RepositoryDetailsQuery
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class RepositoryDetailsImpl @Inject constructor(
    private val dataSource: ApolloRepoDataSource,
): IRepositoryDetails {
    override suspend fun getRepositoryDetails(
        name: String,
        owner: String
    ): String? {
        L.d(TAG, "getRepositoryDetails(owner=$owner, name=$name)")
        val data: RepositoryDetailsQuery.Data = dataSource.getRepositoryReadme(owner = owner, name = name)
        val repo = data.repository
        if (repo == null) {
            L.w(TAG, "repository is null for $owner/$name")
            return null
        }
        val readme = repo.readmeUpper?.onBlob?.text ?: repo.readmeLower?.onBlob?.text
        L.d(TAG, "readme resolved — variant=${if (repo.readmeUpper?.onBlob?.text != null) "UPPER" else if (readme != null) "LOWER" else "NONE"}, length=${readme?.length ?: 0}")
        return readme
    }

    companion object {
        private const val TAG = "RepoDetailsRepo"
    }
}
