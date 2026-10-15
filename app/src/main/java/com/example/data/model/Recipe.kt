package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recipe(
    val title: String,
    val tagline: String,
    val prepTime: String = "5 mins",
    val cookTime: String = "10 mins",
    val totalTime: String = "15 mins",
    val difficulty: String = "Easy",
    val servings: String = "1-2 servings",
    val caloriesEstimate: String = "~380 kcal",
    val flavorProfile: String = "Savory & Comforting",
    val dietaryTags: List<String> = emptyList(),
    val inputIngredients: List<String> = emptyList(),
    val pantryStaples: List<String> = emptyList(),
    val instructions: List<RecipeStep> = emptyList(),
    val chefSecretTip: String = "",
    val storageAndLeftovers: String = ""
)

@JsonClass(generateAdapter = true)
data class RecipeStep(
    val stepNumber: Int,
    val actionName: String,
    val description: String,
    val timerSeconds: Int? = null
)
