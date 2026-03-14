package com.rsav.githubPublicRepoBrowser.domain.repository

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import kotlinx.coroutines.flow.Flow

interface ISearchRepositories {
    fun searchRepositories(query: String): Flow<PagingData<Repo>>
}
