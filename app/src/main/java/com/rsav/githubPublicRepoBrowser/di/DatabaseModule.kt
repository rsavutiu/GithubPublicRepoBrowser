package com.rsav.githubPublicRepoBrowser.di

import android.content.Context
import androidx.room.Room
import com.rsav.githubPublicRepoBrowser.data.local.db.AppDatabase
import com.rsav.githubPublicRepoBrowser.data.local.db.FavoriteRepoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "github_browser_db",
        ).build()

    @Provides
    fun provideFavoriteRepoDao(database: AppDatabase): FavoriteRepoDao =
        database.favoriteRepoDao()
}
