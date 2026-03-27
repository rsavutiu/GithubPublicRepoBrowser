package com.rsav.githubPublicRepoBrowser.di

import com.rsav.githubPublicRepoBrowser.data.auth.GitHubAuthManager
import com.rsav.githubPublicRepoBrowser.data.auth.IGitHubAuthManager
import com.rsav.githubPublicRepoBrowser.data.local.SavedSearchDataStore
import com.rsav.githubPublicRepoBrowser.data.remote.ApolloRepoDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.ContributorDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.GitHubStarDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.IContributorDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.IDependencyDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.IGitHubStarDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.ISparklineDataSource
import com.rsav.githubPublicRepoBrowser.data.remote.SparklineDataSource
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingDetailsRepository
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingSearchRepository
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingUserProfileRepository
import com.rsav.githubPublicRepoBrowser.data.repository.FavoriteRepositoryImpl
import com.rsav.githubPublicRepoBrowser.domain.repository.IFavoriteRepository
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.domain.repository.ISavedSearchRepository
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.domain.repository.IUserProfileRepository
import com.rsav.githubPublicRepoBrowser.domain.usecase.GetRepoDetailsUseCase
import com.rsav.githubPublicRepoBrowser.domain.usecase.IGetRepoDetailsUseCase
import com.rsav.githubPublicRepoBrowser.domain.usecase.ISearchReposUseCase
import com.rsav.githubPublicRepoBrowser.domain.usecase.SearchReposUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepoRepository(impl: DelegatingSearchRepository): ISearchRepositories

    @Binds
    @Singleton
    abstract fun bindRepositoryDetails(impl: DelegatingDetailsRepository): IRepositoryDetails

    @Binds
    @Singleton
    abstract fun bindSavedSearchRepository(impl: SavedSearchDataStore): ISavedSearchRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: DelegatingUserProfileRepository): IUserProfileRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): IFavoriteRepository

    @Binds
    @Singleton
    abstract fun bindContributorDataSource(impl: ContributorDataSource): IContributorDataSource

    @Binds
    abstract fun bindSearchReposUseCase(impl: SearchReposUseCase): ISearchReposUseCase

    @Binds
    abstract fun bindGetRepoDetailsUseCase(impl: GetRepoDetailsUseCase): IGetRepoDetailsUseCase

    @Binds
    @Singleton
    abstract fun bindSparklineDataSource(impl: SparklineDataSource): ISparklineDataSource

    @Binds
    @Singleton
    abstract fun bindGitHubStarDataSource(impl: GitHubStarDataSource): IGitHubStarDataSource

    @Binds
    @Singleton
    abstract fun bindGitHubAuthManager(impl: GitHubAuthManager): IGitHubAuthManager

    @Binds
    @Singleton
    abstract fun bindDependencyDataSource(impl: ApolloRepoDataSource): IDependencyDataSource
}
