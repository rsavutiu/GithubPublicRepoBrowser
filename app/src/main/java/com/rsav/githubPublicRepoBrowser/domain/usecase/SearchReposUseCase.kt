package com.rsav.githubPublicRepoBrowser.domain.usecase

import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.RepoRepository
import javax.inject.Inject

class SearchReposUseCase @Inject constructor(
    private val repository: RepoRepository,
) {
    suspend operator fun invoke(query: String): Result<List<Repo>> {
        val effectiveQuery = query.trim().ifBlank { DEFAULT_QUERY }
        return repository.searchRepositories(effectiveQuery)
    }

    companion object {
        const val DEFAULT_QUERY = "stars:>1000 sort:stars"
    }
}
