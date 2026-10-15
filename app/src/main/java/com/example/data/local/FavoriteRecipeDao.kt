package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {

    @Query("SELECT * FROM favorite_recipes ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT * FROM favorite_recipes WHERE title = :title LIMIT 1")
    fun getFavoriteByTitle(title: String): Flow<FavoriteRecipeEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE title = :title LIMIT 1)")
    fun isFavorite(title: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(recipe: FavoriteRecipeEntity): Long

    @Query("DELETE FROM favorite_recipes WHERE title = :title")
    suspend fun deleteFavoriteByTitle(title: String)

    @Query("DELETE FROM favorite_recipes WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)
}
