package com.rsav.githubPublicRepoBrowser.domain.repository

import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import kotlinx.coroutines.flow.Flow

interface ISavedSearchRepository {
    fun getSavedSearches(): Flow<List<SavedSearch>>
    suspend fun addSavedSearch(savedSearch: SavedSearch)
    suspend fun deleteSavedSearch(id: String)
}
