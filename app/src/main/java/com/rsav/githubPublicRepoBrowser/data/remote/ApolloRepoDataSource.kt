package com.rsav.githubPublicRepoBrowser.data.remote

import com.apollographql.apollo.ApolloClient
import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.selections.SearchRepositoriesQuerySelections
import javax.inject.Inject

class ApolloRepoDataSource @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    suspend fun searchRepositories(
        query: String,
        first: Int,
    ): SearchRepositoriesQuery.Data {
        val response = apolloClient.query(
            SearchRepositoriesQuery(query = "language: $query", first = first)
        ).execute()

        if (response.hasErrors()) {
            throw ApolloQueryException(
                response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
            )
        }

        return response.data ?: throw ApolloQueryException("No data returned")
    }
}

class ApolloQueryException(message: String) : Exception(message)
