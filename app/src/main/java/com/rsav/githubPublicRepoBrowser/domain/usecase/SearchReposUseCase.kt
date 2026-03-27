package com.rsav.githubPublicRepoBrowser.domain.usecase

import androidx.paging.PagingData
import com.rsav.githubPublicRepoBrowser.domain.model.ProgrammingLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.Repo
import com.rsav.githubPublicRepoBrowser.domain.model.SpokenLanguage
import com.rsav.githubPublicRepoBrowser.domain.model.TrendingPeriod
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.util.L
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ISearchReposUseCase {
    operator fun invoke(
        freeText: String = "",
        trendingPeriod: TrendingPeriod? = TrendingPeriod.THIS_WEEK,
        programmingLanguage: ProgrammingLanguage? = null,
        spokenLanguage: SpokenLanguage? = null,
        topics: Set<String> = emptySet(),
    ): Flow<PagingData<Repo>>
}

class SearchReposUseCase @Inject constructor(
    private val repository: ISearchRepositories
) : ISearchReposUseCase {
    override operator fun invoke(
        freeText: String,
        trendingPeriod: TrendingPeriod?,
        programmingLanguage: ProgrammingLanguage?,
        spokenLanguage: SpokenLanguage?,
        topics: Set<String>,
    ): Flow<PagingData<Repo>> {
        val query = buildQuery(freeText, trendingPeriod, programmingLanguage, spokenLanguage, topics)
        L.d(TAG, "invoke → query='$query'")
        return repository.searchRepositories(query)
    }

    companion object {
        private const val TAG = "SearchUseCase"

        fun buildQuery(
            freeText: String,
            trendingPeriod: TrendingPeriod?,
            programmingLanguage: ProgrammingLanguage?,
            spokenLanguage: SpokenLanguage?,
            topics: Set<String> = emptySet(),
        ): String {
            val parts = mutableListOf<String>()

            val text = freeText.trim()
            if (text.isNotBlank()) {
                parts.add(text)
            }

            topics.forEach { parts.add("topic:$it") }
            programmingLanguage?.let { parts.add("language:${it.queryValue}") }

            // Spoken language — GitHub search doesn't have a qualifier for this,
            // so we add the language name as a keyword to bias results.
            spokenLanguage?.let { parts.add(it.name) }

            // When there's no free-text search, show trending repos for the period.
            if (text.isBlank()) {
                parts.add("stars:>5")
                trendingPeriod?.let {
                    parts.add("created:>${trendingPeriod.sinceDate()}")
                }
            }

            parts.add("sort:stars")

            return parts.joinToString(" ")
        }
    }
}
