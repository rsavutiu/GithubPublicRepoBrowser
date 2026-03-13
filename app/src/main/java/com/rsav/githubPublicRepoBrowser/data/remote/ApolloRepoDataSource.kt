package com.rsav.githubPublicRepoBrowser.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import javax.inject.Inject

class ApolloRepoDataSource @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    suspend fun searchRepositories(
        query: String,
        first: Int,
        after: String? = null,
    ): SearchRepositoriesQuery.Data {
        val response = apolloClient.query(
            SearchRepositoriesQuery(
                query = query,
                first = first,
                after = Optional.presentIfNotNull(after),
            )
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
