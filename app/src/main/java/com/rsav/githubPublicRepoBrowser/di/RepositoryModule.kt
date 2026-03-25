package com.rsav.githubPublicRepoBrowser.di

import com.rsav.githubPublicRepoBrowser.data.local.SavedSearchDataStore
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingDetailsRepository
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingSearchRepository
import com.rsav.githubPublicRepoBrowser.data.repository.DelegatingUserProfileRepository
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.domain.repository.ISavedSearchRepository
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
import com.rsav.githubPublicRepoBrowser.domain.repository.IUserProfileRepository
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
}
