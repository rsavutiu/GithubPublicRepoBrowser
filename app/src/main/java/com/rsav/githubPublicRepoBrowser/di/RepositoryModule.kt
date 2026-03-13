package com.rsav.githubPublicRepoBrowser.di

import com.rsav.githubPublicRepoBrowser.data.repository.RepoRepositoryImpl
import com.rsav.githubPublicRepoBrowser.domain.repository.RepoRepository
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
    abstract fun bindRepoRepository(impl: RepoRepositoryImpl): RepoRepository
}
