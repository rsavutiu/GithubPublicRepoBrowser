package com.rsav.githubPublicRepoBrowser.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.rsav.githubPublicRepoBrowser.RepositoryDependenciesQuery
import com.rsav.githubPublicRepoBrowser.RepositoryDetailsQuery
import com.rsav.githubPublicRepoBrowser.SearchRepositoriesQuery
import com.rsav.githubPublicRepoBrowser.UserProfileQuery
import com.rsav.githubPublicRepoBrowser.util.L
import javax.inject.Inject

class ApolloRepoDataSource @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    suspend fun getRepositoryReadme(
        owner: String,
        name: String,
    ): RepositoryDetailsQuery.Data {
        L.d(TAG, "getRepositoryReadme(owner=$owner, name=$name)")
        val response = apolloClient.query(
            RepositoryDetailsQuery(
                owner = owner,
                name = name,
            )
        ).execute()
        if (response.hasErrors()) {
            val msg = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
            L.e(TAG, "getRepositoryReadme ERROR: $msg")
            throw ApolloQueryException(msg)
        }
        val data = response.data ?: throw ApolloQueryException("No data returned").also {
            L.e(TAG, "getRepositoryReadme — null data")
        }
        L.d(TAG, "getRepositoryReadme OK — hasRepo=${data.repository != null}")
        return data
    }

    suspend fun searchRepositories(
        query: String,
        first: Int,
        after: String? = null,
    ): SearchRepositoriesQuery.Data {
        L.d(TAG, "searchRepositories(query=$query, first=$first, after=$after)")
        val response = apolloClient.query(
            SearchRepositoriesQuery(
                query = query,
                first = first,
                after = Optional.presentIfNotNull(after),
            )
        ).execute()

        if (response.hasErrors()) {
            val msg = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
            L.e(TAG, "searchRepositories ERROR: $msg")
            throw ApolloQueryException(msg)
        }

        val data = response.data ?: throw ApolloQueryException("No data returned").also {
            L.e(TAG, "searchRepositories — null data")
        }
        val count = data.search.nodes?.size ?: 0
        L.d(TAG, "searchRepositories OK — $count nodes, hasNext=${data.search.pageInfo.hasNextPage}, endCursor=${data.search.pageInfo.endCursor}")
        return data
    }

    suspend fun getUserProfile(login: String): UserProfileQuery.Data {
        L.d(TAG, "getUserProfile(login=$login)")
        val response = apolloClient.query(UserProfileQuery(login = login)).execute()
        if (response.hasErrors()) {
            val msg = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
            L.e(TAG, "getUserProfile ERROR: $msg")
            throw ApolloQueryException(msg)
        }
        val data = response.data ?: throw ApolloQueryException("No data returned").also {
            L.e(TAG, "getUserProfile — null data")
        }
        L.d(TAG, "getUserProfile OK — user=${data.user?.login}")
        return data
    }

    suspend fun getRepositoryDependencies(
        owner: String,
        name: String,
    ): RepositoryDependenciesQuery.Data {
        L.d(TAG, "getRepositoryDependencies(owner=$owner, name=$name)")
        val response = apolloClient.query(
            RepositoryDependenciesQuery(
                owner = owner,
                name = name,
            )
        ).execute()
        if (response.hasErrors()) {
            val msg = response.errors?.firstOrNull()?.message ?: "Unknown GraphQL error"
            L.e(TAG, "getRepositoryDependencies ERROR: $msg")
            throw ApolloQueryException(msg)
        }
        val data = response.data ?: throw ApolloQueryException("No data returned").also {
            L.e(TAG, "getRepositoryDependencies — null data")
        }
        L.d(TAG, "getRepositoryDependencies OK — hasRepo=${data.repository != null}")
        return data
    }

    companion object {
        private const val TAG = "DataSource"
    }
}

class ApolloQueryException(message: String) : Exception(message)
