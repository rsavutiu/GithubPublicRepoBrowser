package com.rsav.githubPublicRepoBrowser.data.repository

import com.rsav.githubPublicRepoBrowser.domain.model.ApiMode
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiModeRepository @Inject constructor() {

    private val _apiMode = MutableStateFlow(ApiMode.REST)
    val apiMode: StateFlow<ApiMode> = _apiMode.asStateFlow()

    fun setApiMode(mode: ApiMode) {
        L.d(TAG, "API mode changed: ${_apiMode.value} → $mode")
        _apiMode.value = mode
    }

    companion object {
        private const val TAG = "ApiModeRepo"
    }
}
