package com.rsav.githubPublicRepoBrowser.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rsav.githubPublicRepoBrowser.domain.model.SavedSearch
import com.rsav.githubPublicRepoBrowser.domain.repository.ISavedSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SavedSearchDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ISavedSearchRepository {

    private val key = stringPreferencesKey("saved_searches")

    override fun getSavedSearches(): Flow<List<SavedSearch>> =
        dataStore.data.map { prefs ->
            val json = prefs[key] ?: return@map emptyList()
            Json.decodeFromString<List<SavedSearch>>(json)
        }

    override suspend fun addSavedSearch(savedSearch: SavedSearch) {
        dataStore.edit { prefs ->
            val current = prefs[key]
                ?.let { Json.decodeFromString<List<SavedSearch>>(it) }
                ?: emptyList()
            prefs[key] = Json.encodeToString(current + savedSearch)
        }
    }

    override suspend fun deleteSavedSearch(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[key]
                ?.let { Json.decodeFromString<List<SavedSearch>>(it) }
                ?: return@edit
            prefs[key] = Json.encodeToString(current.filter { it.id != id })
        }
    }
}
