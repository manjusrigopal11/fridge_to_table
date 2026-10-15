package com.example.data.model

object CommonIngredients {
    val ALL_ITEMS = listOf(
        // Dairy & Eggs
        "Eggs",
        "Cheddar cheese",
        "Mozzarella cheese",
        "Parmesan cheese",
        "Feta cheese",
        "Greek yogurt",
        "Butter",
        "Milk",
        "Heavy cream",
        "Cream cheese",
        "Sour cream",
        "Cottage cheese",

        // Fresh Veggies & Herbs
        "Spinach",
        "Tomatoes",
        "Cherry tomatoes",
        "Bell pepper",
        "Broccoli",
        "Onions",
        "Red onion",
        "Green onions (Scallions)",
        "Garlic",
        "Carrots",
        "Mushrooms",
        "Zucchini",
        "Potatoes",
        "Sweet potatoes",
        "Cabbage",
        "Kale",
        "Cucumber",
        "Avocado",
        "Cauliflower",
        "Asparagus",
        "Green beans",
        "Corn",
        "Jalapeño",
        "Fresh cilantro",
        "Fresh basil",
        "Fresh parsley",

        // Proteins
        "Chicken breast",
        "Chicken thighs",
        "Ground beef",
        "Canned tuna",
        "Tofu",
        "Bacon",
        "Sausages",
        "Salmon fillet",
        "Shrimp",
        "Pork chops",
        "Ham",
        "Turkey slices",
        "Edamame",

        // Grains & Carbs
        "Leftover rice",
        "Jasmine rice",
        "Pasta",
        "Spaghetti",
        "Bread",
        "Tortillas",
        "Noodles",
        "Ramen noodles",
        "Quinoa",
        "Rolled oats",

        // Pantry & Legumes
        "Black beans",
        "Chickpeas",
        "Canned crushed tomatoes",
        "Peanut butter",
        "Lentils",
        "Coconut milk",
        "Soy sauce",
        "Kimchi"
    )

    val CATEGORIES = listOf(
        "Popular" to listOf("Eggs", "Spinach", "Leftover rice", "Cheddar cheese", "Tomatoes", "Chicken breast", "Pasta", "Bell pepper"),
        "Produce" to listOf("Spinach", "Tomatoes", "Broccoli", "Mushrooms", "Zucchini", "Carrots", "Avocado", "Green onions"),
        "Dairy" to listOf("Eggs", "Cheddar cheese", "Greek yogurt", "Mozzarella cheese", "Butter", "Milk", "Parmesan cheese"),
        "Proteins" to listOf("Chicken breast", "Canned tuna", "Ground beef", "Tofu", "Bacon", "Salmon fillet", "Shrimp"),
        "Carbs & Cans" to listOf("Leftover rice", "Pasta", "Tortillas", "Bread", "Black beans", "Chickpeas", "Ramen noodles")
    )

    fun getSuggestions(query: String, exclude: List<String> = emptyList(), limit: Int = 5): List<String> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return emptyList()

        val normalizedExclude = exclude.map { it.trim().lowercase() }

        return ALL_ITEMS
            .filter { item ->
                val lower = item.lowercase()
                lower.contains(trimmed) && !normalizedExclude.contains(lower)
            }
            .sortedBy { item ->
                val lower = item.lowercase()
                if (lower.startsWith(trimmed)) 0 else 1
            }
            .take(limit)
    }
}
