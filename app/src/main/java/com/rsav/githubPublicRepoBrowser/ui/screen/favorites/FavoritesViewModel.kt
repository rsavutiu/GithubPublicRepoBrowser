package com.rsav.githubPublicRepoBrowser.ui.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsav.githubPublicRepoBrowser.data.auth.IGitHubAuthManager
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.repository.IFavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<Repo> = emptyList(),
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val avatarUrl: String? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: IFavoriteRepository,
    private val authManager: IGitHubAuthManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.getAllFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
        viewModelScope.launch {
            authManager.isLoggedIn.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            authManager.username.collect { username ->
                _uiState.update { it.copy(username = username) }
            }
        }
        viewModelScope.launch {
            authManager.avatarUrl.collect { avatarUrl ->
                _uiState.update { it.copy(avatarUrl = avatarUrl) }
            }
        }
    }

    fun getOAuthUrl(): String = authManager.getOAuthUrl()

    fun logout() {
        viewModelScope.launch { authManager.logout() }
    }
}
