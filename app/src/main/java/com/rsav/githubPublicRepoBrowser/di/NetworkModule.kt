package com.rsav.githubPublicRepoBrowser.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import com.rsav.githubPublicRepoBrowser.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GITHUB_GRAPHQL_ENDPOINT = "https://api.github.com/graphql"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(GITHUB_GRAPHQL_ENDPOINT)
            .okHttpClient(okHttpClient)
            .build()
    }
}
