package com.rsav.githubPublicRepoBrowser.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRepoDao {

    @Query("SELECT * FROM favorite_repos ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRepoEntity>>

    @Query("SELECT id FROM favorite_repos")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_repos WHERE id = :repoId)")
    fun isFavorite(repoId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteRepoEntity)

    @Query("DELETE FROM favorite_repos WHERE id = :repoId")
    suspend fun delete(repoId: String)
}
