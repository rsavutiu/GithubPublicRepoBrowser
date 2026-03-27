package com.rsav.githubPublicRepoBrowser.testing

import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.domain.repository.ISavedSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Fake [ISavedSearchRepository] backed by an in-memory list.
 */
class FakeSavedSearchRepository : ISavedSearchRepository {

    private val _searches = MutableStateFlow<List<SavedSearch>>(emptyList())

    override fun getSavedSearches(): Flow<List<SavedSearch>> = _searches

    override suspend fun addSavedSearch(savedSearch: SavedSearch) {
        _searches.update { it + savedSearch }
    }

    override suspend fun deleteSavedSearch(id: String) {
        _searches.update { list -> list.filter { it.id != id } }
    }
}
