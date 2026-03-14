package com.rsav.githubPublicRepoBrowser.di

import com.rsav.githubPublicRepoBrowser.data.repository.RepositoryDetailsImpl
import com.rsav.githubPublicRepoBrowser.data.repository.SearchRepositoriesImpl
import com.rsav.githubPublicRepoBrowser.domain.repository.IRepositoryDetails
import com.rsav.githubPublicRepoBrowser.domain.repository.ISearchRepositories
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
    abstract fun bindRepoRepository(impl: SearchRepositoriesImpl): ISearchRepositories

    @Binds
    @Singleton
    abstract fun bindRepositoryDetails(impl: RepositoryDetailsImpl): IRepositoryDetails
}
