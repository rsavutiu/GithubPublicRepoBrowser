package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.RepoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchReposUseCase @Inject constructor(
    private val repository: RepoRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<Repo>> {
        val effectiveQuery = query.trim().ifBlank { DEFAULT_QUERY }
        return repository.searchRepositories(effectiveQuery)
    }

    companion object {
        const val DEFAULT_QUERY = "stars:>1000 sort:stars"
    }
}
