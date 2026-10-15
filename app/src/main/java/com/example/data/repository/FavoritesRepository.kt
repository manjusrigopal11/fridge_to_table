package com.example.data.repository

import com.example.data.local.FavoriteRecipeDao
import com.example.data.local.FavoriteRecipeEntity
import com.example.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FavoritesRepository(private val dao: FavoriteRecipeDao) {

    val allFavorites: Flow<List<FavoriteRecipeEntity>> = dao.getAllFavorites()

    fun isFavorite(title: String): Flow<Boolean> = dao.isFavorite(title)

    suspend fun saveFavorite(recipe: Recipe) {
        val entity = FavoriteRecipeEntity.fromRecipe(recipe)
        dao.insertFavorite(entity)
    }

    suspend fun removeFavorite(title: String) {
        dao.deleteFavoriteByTitle(title)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteFavoriteById(id)
    }

    suspend fun toggleFavorite(recipe: Recipe): Boolean {
        val currentFav = dao.getFavoriteByTitle(recipe.title).firstOrNull()
        return if (currentFav != null) {
            dao.deleteFavoriteByTitle(recipe.title)
            false
        } else {
            dao.insertFavorite(FavoriteRecipeEntity.fromRecipe(recipe))
            true
        }
    }
}
