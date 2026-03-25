package com.rsav.githubPublicRepoBrowser.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteRepoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteRepoDao(): FavoriteRepoDao
}
