package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.domain.model.ApiMode
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class DelegatingDetailsRepository @Inject constructor(
    private val graphQlImpl: RepositoryDetailsImpl,
    private val restImpl: RestRepositoryDetailsImpl,
    private val apiModeRepository: ApiModeRepository,
) : IRepositoryDetails {

    override suspend fun getRepositoryDetails(name: String, owner: String): String? {
        val mode = apiModeRepository.apiMode.value
        L.d(TAG, "getRepositoryDetails via $mode")
        return when (mode) {
            ApiMode.GRAPHQL -> graphQlImpl.getRepositoryDetails(name, owner)
            ApiMode.REST -> restImpl.getRepositoryDetails(name, owner)
        }
    }

    companion object {
        private const val TAG = "DelegatingDetails"
    }
}
