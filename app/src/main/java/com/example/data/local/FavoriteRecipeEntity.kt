package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Recipe
import com.example.data.network.ApiClient

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val tagline: String,
    val heroIngredientsSummary: String,
    val totalTime: String,
    val difficulty: String,
    val servings: String,
    val recipeJson: String,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toRecipe(): Recipe? {
        return try {
            val adapter = ApiClient.moshi.adapter(Recipe::class.java)
            adapter.fromJson(recipeJson)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun fromRecipe(recipe: Recipe): FavoriteRecipeEntity {
            val adapter = ApiClient.moshi.adapter(Recipe::class.java)
            val json = adapter.toJson(recipe)
            val summary = recipe.inputIngredients.joinToString(", ")
            return FavoriteRecipeEntity(
                title = recipe.title,
                tagline = recipe.tagline,
                heroIngredientsSummary = summary,
                totalTime = recipe.totalTime,
                difficulty = recipe.difficulty,
                servings = recipe.servings,
                recipeJson = json
            )
        }
    }
}
