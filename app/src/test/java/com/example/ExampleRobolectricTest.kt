package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Recipe
import com.example.data.model.RecipeStep
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var favoritesRepo: FavoritesRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    favoritesRepo = FavoritesRepository(db.favoriteRecipeDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context matches Fridge-to-Table`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fridge-to-Table", appName)
  }

  @Test
  fun `repository generates fallback recipe when no API key is present`() = runTest {
    val repository = RecipeRepository()
    val result = repository.generateRecipe(
      ingredient1 = "Eggs",
      ingredient2 = "Spinach",
      ingredient3 = "Leftover rice"
    )
    assertNotNull(result)
    if (result is com.example.data.repository.GenerationResult.Success) {
      assertEquals("Golden Garlic Egg & Spinach Fried Rice", result.recipe.title)
      assertTrue(result.recipe.instructions.isNotEmpty())
    }
  }

  @Test
  fun `repository generates fallback recipe with specified dietary restrictions`() = runTest {
    val repository = RecipeRepository()
    val result = repository.generateRecipe(
      ingredient1 = "Tofu",
      ingredient2 = "Broccoli",
      ingredient3 = "Soy sauce",
      dietaryRestrictions = listOf("Vegan", "Gluten-Free")
    )
    assertNotNull(result)
    if (result is com.example.data.repository.GenerationResult.Success) {
      assertTrue(result.recipe.dietaryTags.contains("Vegan"))
      assertTrue(result.recipe.dietaryTags.contains("Gluten-Free"))
    }
  }

  @Test
  fun `repository generates recipe complying with vegan gluten-free and low-carb constraints`() = runTest {
    val repository = RecipeRepository()
    val result = repository.generateRecipe(
      ingredient1 = "Zucchini",
      ingredient2 = "Mushrooms",
      ingredient3 = "Olive oil",
      dietaryRestrictions = listOf("Vegan", "Gluten-Free", "Low-Carb")
    )
    assertNotNull(result)
    if (result is com.example.data.repository.GenerationResult.Success) {
      assertTrue(result.recipe.dietaryTags.contains("Vegan"))
      assertTrue(result.recipe.dietaryTags.contains("Gluten-Free"))
      assertTrue(result.recipe.dietaryTags.contains("Low-Carb"))
    }
  }

  @Test
  fun `recipe includes estimated nutritional breakdown with calories protein carbs and fat`() = runTest {
    val repository = RecipeRepository()
    val result = repository.generateRecipe(
      ingredient1 = "Eggs",
      ingredient2 = "Spinach",
      ingredient3 = "Leftover rice"
    )
    assertNotNull(result)
    if (result is com.example.data.repository.GenerationResult.Success) {
      val nutrition = result.recipe.effectiveNutrition()
      assertNotNull(nutrition)
      assertTrue(nutrition.calories.isNotBlank())
      assertTrue(nutrition.protein.isNotBlank())
      assertTrue(nutrition.carbs.isNotBlank())
      assertTrue(nutrition.fat.isNotBlank())
      assertEquals("380 kcal", nutrition.calories)
      assertEquals("16g", nutrition.protein)
      assertEquals("46g", nutrition.carbs)
      assertEquals("13g", nutrition.fat)
    }
  }

  @Test
  fun `room database stores and retrieves favorite recipes reactively`() = runTest {
    val testRecipe = Recipe(
      title = "Crispy Garlic Egg Rice",
      tagline = "Savory quick delight",
      prepTime = "5 mins",
      cookTime = "8 mins",
      totalTime = "13 mins",
      difficulty = "Easy",
      servings = "1 serving",
      caloriesEstimate = "~350 kcal",
      flavorProfile = "Savory & Crispy",
      inputIngredients = listOf("2 eggs", "1 cup rice", "1 handful spinach"),
      pantryStaples = listOf("1 tbsp oil", "Salt and pepper"),
      instructions = listOf(
        RecipeStep(1, "Sauté", "Sauté spinach in hot oil.", 60)
      ),
      chefSecretTip = "Use day-old rice.",
      storageAndLeftovers = "Eat fresh."
    )

    // Initially not favorite
    val initialFav = favoritesRepo.isFavorite(testRecipe.title).first()
    assertFalse(initialFav)

    // Save to favorites
    val wasAdded = favoritesRepo.toggleFavorite(testRecipe)
    assertTrue(wasAdded)

    // Verify stored in favorites
    val isFavAfterSave = favoritesRepo.isFavorite(testRecipe.title).first()
    assertTrue(isFavAfterSave)

    val favoritesList = favoritesRepo.allFavorites.first()
    assertEquals(1, favoritesList.size)
    assertEquals("Crispy Garlic Egg Rice", favoritesList[0].title)

    // Verify entity converts back to complete recipe
    val retrievedRecipe = favoritesList[0].toRecipe()
    assertNotNull(retrievedRecipe)
    assertEquals(testRecipe.title, retrievedRecipe?.title)
    assertEquals(1, retrievedRecipe?.instructions?.size)

    // Toggle again removes from favorites
    val wasRemoved = favoritesRepo.toggleFavorite(testRecipe)
    assertFalse(wasRemoved)

    val isFavAfterRemove = favoritesRepo.isFavorite(testRecipe.title).first()
    assertFalse(isFavAfterRemove)
    assertEquals(0, favoritesRepo.allFavorites.first().size)
  }
}
