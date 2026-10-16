package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NutritionInfo(
    val calories: String = "~380 kcal",
    val protein: String = "15g",
    val carbs: String = "45g",
    val fat: String = "12g"
)

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
    val nutrition: NutritionInfo? = null,
    val flavorProfile: String = "Savory & Comforting",
    val dietaryTags: List<String> = emptyList(),
    val inputIngredients: List<String> = emptyList(),
    val pantryStaples: List<String> = emptyList(),
    val instructions: List<RecipeStep> = emptyList(),
    val chefSecretTip: String = "",
    val storageAndLeftovers: String = ""
) {
    fun effectiveNutrition(): NutritionInfo {
        if (nutrition != null) return nutrition
        return NutritionInfo(
            calories = if (caloriesEstimate.isNotBlank()) caloriesEstimate else "~380 kcal",
            protein = "16g",
            carbs = "42g",
            fat = "12g"
        )
    }
}

@JsonClass(generateAdapter = true)
data class RecipeStep(
    val stepNumber: Int,
    val actionName: String,
    val description: String,
    val timerSeconds: Int? = null
)

/**
 * Intelligent helper to extract cooking duration from instruction description
 * if timerSeconds is not explicitly provided.
 */
fun parseCookingTimeSeconds(description: String): Int? {
    if (description.isBlank()) return null

    // 1. Check for hour patterns: "1 hour", "1.5 hours", "1-2 hours"
    val hourRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:-|to)?\s*\d*\s*h(?:ou)?rs?\b""", RegexOption.IGNORE_CASE)
    hourRegex.find(description)?.let { match ->
        val hours = match.groupValues[1].toFloatOrNull() ?: 1f
        return (hours * 3600).toInt()
    }

    // 2. Check for minute patterns: "5 minutes", "8-10 mins", "3 to 4 min"
    val minRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:-|to)?\s*\d*\s*(?:min(?:ute)?s?)\b""", RegexOption.IGNORE_CASE)
    minRegex.find(description)?.let { match ->
        val mins = match.groupValues[1].toFloatOrNull() ?: 1f
        return (mins * 60).toInt()
    }

    // 3. Check for second patterns: "30 seconds", "45 secs", "60 sec"
    val secRegex = Regex("""(\d+)\s*(?:-|to)?\s*\d*\s*(?:sec(?:ond)?s?)\b""", RegexOption.IGNORE_CASE)
    secRegex.find(description)?.let { match ->
        val secs = match.groupValues[1].toIntOrNull() ?: 30
        return secs
    }

    return null
}

fun RecipeStep.effectiveTimerSeconds(): Int? {
    if (timerSeconds != null && timerSeconds > 0) return timerSeconds
    return parseCookingTimeSeconds(description)
}
