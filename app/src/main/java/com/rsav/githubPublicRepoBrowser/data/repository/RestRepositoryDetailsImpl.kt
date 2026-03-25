package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.data.remote.rest.RestRepoDataSource
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class RestRepositoryDetailsImpl @Inject constructor(
    private val dataSource: RestRepoDataSource,
) : IRepositoryDetails {

    override suspend fun getRepositoryDetails(name: String, owner: String): String? {
        L.d(TAG, "getRepositoryDetails(owner=$owner, name=$name)")
        val readme = dataSource.getReadme(owner, name)
        L.d(TAG, "readme resolved — length=${readme?.length ?: 0}")
        return readme
    }

    companion object {
        private const val TAG = "RestRepoDetails"
    }
}
