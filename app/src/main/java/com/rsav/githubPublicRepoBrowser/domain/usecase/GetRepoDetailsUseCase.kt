package com.rsav.githubPublicRepoBrowser.domain.usecase

import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import javax.inject.Inject

class GetRepoDetailsUseCase @Inject constructor(
    private val repository: IRepositoryDetails
) {
    suspend operator fun invoke(name: String, owner: String): String? {
        return repository.getRepositoryDetails(name=name, owner=owner)
    }
}
