package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchReposUseCase @Inject constructor(
    private val repository: ISearchRepositories
) {
    operator fun invoke(query: String): Flow<PagingData<Repo>> {
        val effectiveQuery = query.trim().ifBlank { DEFAULT_QUERY }
        return repository.searchRepositories(effectiveQuery)
    }

    companion object {
        const val DEFAULT_QUERY = "stars:>100 sort:stars"
    }
}
