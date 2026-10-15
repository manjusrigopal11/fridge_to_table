package com.example

import com.example.data.model.CommonIngredients
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun autoSuggest_returnsMatchingIngredients() {
    val eggSuggestions = CommonIngredients.getSuggestions("egg")
    assertTrue(eggSuggestions.contains("Eggs"))

    val spinSuggestions = CommonIngredients.getSuggestions("spin")
    assertTrue(spinSuggestions.contains("Spinach"))

    val riceSuggestions = CommonIngredients.getSuggestions("rice")
    assertTrue(riceSuggestions.any { it.contains("rice", ignoreCase = true) })
  }

  @Test
  fun autoSuggest_excludesAlreadySelectedIngredients() {
    val suggestions = CommonIngredients.getSuggestions("egg", exclude = listOf("Eggs"))
    assertTrue(!suggestions.contains("Eggs"))
  }
}
