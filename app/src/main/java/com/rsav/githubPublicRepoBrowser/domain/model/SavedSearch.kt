package com.rsav.githubPublicRepoBrowser.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedSearch(
    val id: String,
    val name: String,
    val query: String,
    val trendingPeriodName: String?,
    val programmingLanguageName: String?,
    val spokenLanguageCode: String?,
)
