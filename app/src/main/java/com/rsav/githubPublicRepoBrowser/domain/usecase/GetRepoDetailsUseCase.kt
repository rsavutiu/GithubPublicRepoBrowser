package com.rsav.githubPublicRepoBrowser.domain.usecase

import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class GetRepoDetailsUseCase @Inject constructor(
    private val repository: IRepositoryDetails
) {
    suspend operator fun invoke(name: String, owner: String): String? {
        L.d(TAG, "invoke(owner=$owner, name=$name)")
        val result = repository.getRepositoryDetails(name = name, owner = owner)
        L.d(TAG, "result — ${if (result != null) "${result.length} chars" else "null"}")
        return result
    }

    companion object {
        private const val TAG = "DetailsUseCase"
    }
}
