package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRepoDetailsUseCase @Inject constructor(
    private val repository: IRepositoryDetails
) {
    suspend operator fun invoke(name: String, owner: String): String? {
        return repository.getRepositoryDetails(name=name, owner=owner)
    }
}
