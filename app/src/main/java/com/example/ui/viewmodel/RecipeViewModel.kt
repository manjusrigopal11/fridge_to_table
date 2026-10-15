package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteRecipeEntity
import com.example.data.model.CommonIngredients
import com.example.data.model.Recipe
import com.example.data.model.RecipeStep
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.GenerationResult
import com.example.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeUiState(
    val ingredient1: String = "Eggs",
    val ingredient2: String = "Spinach",
    val ingredient3: String = "Leftover rice",
    val selectedPreference: String = "Balanced & Quick",
    val selectedDietaryRestrictions: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isAnalyzingImage: Boolean = false,
    val activeFocusedField: Int = 1,
    val showPantryBrowser: Boolean = false,
    val showFavoritesSheet: Boolean = false,
    val isCurrentFavorite: Boolean = false,
    val currentRecipe: Recipe? = null,
    val isLiveGemini: Boolean = false,
    val variationCount: Int = 0,
    val checkedIngredients: Set<String> = emptySet(),
    val completedSteps: Set<Int> = emptySet(),
    val activeTimerStep: RecipeStep? = null,
    val timerRemainingSeconds: Int = 0,
    val timerTotalSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showCookingMode: Boolean = false,
    val cookingModeStepIndex: Int = 0,
    val snackbarMessage: String? = null
) {
    val autoSuggestions: List<String>
        get() {
            val query = when (activeFocusedField) {
                1 -> ingredient1
                2 -> ingredient2
                3 -> ingredient3
                else -> ""
            }
            val existing = listOf(ingredient1, ingredient2, ingredient3)
            return CommonIngredients.getSuggestions(query, exclude = existing, limit = 5)
        }
}

class RecipeViewModel(
    application: Application,
    private val repository: RecipeRepository = RecipeRepository(),
    customFavoritesRepo: FavoritesRepository? = null
) : AndroidViewModel(application) {

    private val favoritesRepo: FavoritesRepository = customFavoritesRepo
        ?: FavoritesRepository(AppDatabase.getDatabase(application).favoriteRecipeDao())

    val favorites: StateFlow<List<FavoriteRecipeEntity>> = favoritesRepo.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Observe favorites to keep current favorite heart status in sync
        viewModelScope.launch {
            favorites.collect { favList ->
                val currentTitle = _uiState.value.currentRecipe?.title
                _uiState.update {
                    it.copy(isCurrentFavorite = favList.any { f -> f.title == currentTitle })
                }
            }
        }

        // Generate initial recipe on launch
        generateRecipe()
    }

    fun onIngredient1Changed(value: String) {
        _uiState.update { it.copy(ingredient1 = value, activeFocusedField = 1) }
    }

    fun onIngredient2Changed(value: String) {
        _uiState.update { it.copy(ingredient2 = value, activeFocusedField = 2) }
    }

    fun onIngredient3Changed(value: String) {
        _uiState.update { it.copy(ingredient3 = value, activeFocusedField = 3) }
    }

    fun setActiveField(field: Int) {
        _uiState.update { it.copy(activeFocusedField = field) }
    }

    fun togglePantryBrowser() {
        _uiState.update { it.copy(showPantryBrowser = !it.showPantryBrowser) }
    }

    fun openFavoritesSheet() {
        _uiState.update { it.copy(showFavoritesSheet = true) }
    }

    fun closeFavoritesSheet() {
        _uiState.update { it.copy(showFavoritesSheet = false) }
    }

    fun toggleFavoriteCurrentRecipe() {
        val recipe = _uiState.value.currentRecipe ?: return
        viewModelScope.launch {
            val isNowFav = favoritesRepo.toggleFavorite(recipe)
            _uiState.update {
                it.copy(
                    isCurrentFavorite = isNowFav,
                    snackbarMessage = if (isNowFav) "Saved \"${recipe.title}\" to Favorites ❤️" else "Removed from Favorites"
                )
            }
        }
    }

    fun deleteFavorite(id: Long) {
        viewModelScope.launch {
            favoritesRepo.deleteById(id)
            _uiState.update { it.copy(snackbarMessage = "Removed from Favorites") }
        }
    }

    fun loadRecipe(recipe: Recipe) {
        _uiState.update {
            it.copy(
                currentRecipe = recipe,
                showFavoritesSheet = false,
                checkedIngredients = emptySet(),
                completedSteps = emptySet(),
                activeTimerStep = null,
                isCurrentFavorite = true,
                snackbarMessage = "Loaded \"${recipe.title}\""
            )
        }
    }

    fun selectSuggestedIngredient(ingredient: String, targetField: Int? = null) {
        val field = targetField ?: _uiState.value.activeFocusedField
        when (field) {
            1 -> _uiState.update { it.copy(ingredient1 = ingredient, activeFocusedField = 2) }
            2 -> _uiState.update { it.copy(ingredient2 = ingredient, activeFocusedField = 3) }
            3 -> _uiState.update { it.copy(ingredient3 = ingredient) }
        }
    }

    fun applyIngredientFromQuickList(ingredient: String) {
        val s = _uiState.value
        when {
            s.ingredient1.isBlank() -> _uiState.update { it.copy(ingredient1 = ingredient, activeFocusedField = 2) }
            s.ingredient2.isBlank() -> _uiState.update { it.copy(ingredient2 = ingredient, activeFocusedField = 3) }
            s.ingredient3.isBlank() -> _uiState.update { it.copy(ingredient3 = ingredient) }
            else -> {
                when (s.activeFocusedField) {
                    1 -> _uiState.update { it.copy(ingredient1 = ingredient) }
                    2 -> _uiState.update { it.copy(ingredient2 = ingredient) }
                    3 -> _uiState.update { it.copy(ingredient3 = ingredient) }
                }
            }
        }
    }

    fun applySpeechInput(spokenText: String, targetField: Int? = null) {
        val clean = spokenText.trim()
        if (clean.isBlank()) return

        if (targetField != null) {
            when (targetField) {
                1 -> _uiState.update { it.copy(ingredient1 = clean, activeFocusedField = 2) }
                2 -> _uiState.update { it.copy(ingredient2 = clean, activeFocusedField = 3) }
                3 -> _uiState.update { it.copy(ingredient3 = clean) }
            }
            _uiState.update { it.copy(snackbarMessage = "Added \"$clean\" to Slot $targetField") }
            return
        }

        val parsed = parseSpokenPhraseIntoThree(clean)
        if (parsed.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    ingredient1 = parsed.getOrElse(0) { state.ingredient1 },
                    ingredient2 = parsed.getOrElse(1) { state.ingredient2 },
                    ingredient3 = parsed.getOrElse(2) { state.ingredient3 },
                    snackbarMessage = "Voice added: ${parsed.joinToString(", ")}"
                )
            }
            generateRecipe()
        } else {
            _uiState.update {
                it.copy(
                    ingredient1 = clean,
                    snackbarMessage = "Heard: \"$clean\""
                )
            }
        }
    }

    private fun parseSpokenPhraseIntoThree(phrase: String): List<String> {
        val normalized = phrase
            .replace(Regex("^(i have|i've got|we have|in the fridge there is|there are)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()

        val split = normalized.split(Regex("[,;]|\\band\\b|\\bplus\\b|\\bwith\\b", RegexOption.IGNORE_CASE))
            .map { it.trim().removePrefix("some ").removePrefix("a ").removePrefix("an ").trim() }
            .filter { it.isNotBlank() }

        return if (split.size >= 3) {
            split.take(3).map { capitalize(it) }
        } else if (split.size == 2) {
            listOf(capitalize(split[0]), capitalize(split[1]), "")
        } else {
            listOf(capitalize(phrase))
        }
    }

    private fun capitalize(s: String): String {
        return s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun analyzeFridgeImage(bitmap: Bitmap) {
        _uiState.update { it.copy(isAnalyzingImage = true) }
        viewModelScope.launch {
            val detected = repository.identifyIngredientsFromImage(bitmap)
            if (detected.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        isAnalyzingImage = false,
                        ingredient1 = detected.getOrElse(0) { "Eggs" },
                        ingredient2 = detected.getOrElse(1) { "Spinach" },
                        ingredient3 = detected.getOrElse(2) { "Leftover rice" },
                        snackbarMessage = "Fridge scanned! Detected: ${detected.joinToString(", ")}"
                    )
                }
                generateRecipe()
            } else {
                _uiState.update {
                    it.copy(
                        isAnalyzingImage = false,
                        snackbarMessage = "Could not identify 3 ingredients. Try typing or speaking."
                    )
                }
            }
        }
    }

    fun setQuickCombo(ing1: String, ing2: String, ing3: String) {
        _uiState.update {
            it.copy(
                ingredient1 = ing1,
                ingredient2 = ing2,
                ingredient3 = ing3
            )
        }
        generateRecipe()
    }

    fun setPreference(pref: String) {
        _uiState.update { it.copy(selectedPreference = pref) }
    }

    fun toggleDietaryRestriction(restriction: String) {
        _uiState.update { state ->
            val set = state.selectedDietaryRestrictions.toMutableSet()
            if (set.contains(restriction)) {
                set.remove(restriction)
            } else {
                set.add(restriction)
            }
            state.copy(selectedDietaryRestrictions = set)
        }
    }

    fun clearInputs() {
        _uiState.update {
            it.copy(
                ingredient1 = "",
                ingredient2 = "",
                ingredient3 = "",
                activeFocusedField = 1
            )
        }
    }

    fun generateRecipe(isRegenerate: Boolean = false) {
        val state = _uiState.value
        val i1 = state.ingredient1.trim()
        val i2 = state.ingredient2.trim()
        val i3 = state.ingredient3.trim()

        if (i1.isBlank() || i2.isBlank() || i3.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter all 3 fridge ingredients to generate a recipe") }
            return
        }

        stopTimer()

        val newVariation = if (isRegenerate) state.variationCount + 1 else 0
        _uiState.update {
            it.copy(
                isLoading = true,
                variationCount = newVariation,
                checkedIngredients = emptySet(),
                completedSteps = emptySet(),
                activeTimerStep = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.generateRecipe(
                ingredient1 = i1,
                ingredient2 = i2,
                ingredient3 = i3,
                preference = state.selectedPreference,
                dietaryRestrictions = state.selectedDietaryRestrictions.toList(),
                variationSeed = newVariation
            )) {
                is GenerationResult.Success -> {
                    val isFav = favorites.value.any { it.title == result.recipe.title }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentRecipe = result.recipe,
                            isLiveGemini = result.isFromLiveGemini,
                            isCurrentFavorite = isFav,
                            snackbarMessage = if (result.isFromLiveGemini) {
                                "Recipe generated fresh by Gemini AI!"
                            } else {
                                "Recipe generated! (Add GEMINI_API_KEY in Secrets for live cloud AI)"
                            }
                        )
                    }
                }
                is GenerationResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentRecipe = result.fallbackRecipe,
                            isLiveGemini = false,
                            snackbarMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleIngredientChecked(ingredient: String) {
        _uiState.update { state ->
            val newSet = state.checkedIngredients.toMutableSet()
            if (newSet.contains(ingredient)) {
                newSet.remove(ingredient)
            } else {
                newSet.add(ingredient)
            }
            state.copy(checkedIngredients = newSet)
        }
    }

    fun toggleStepCompleted(stepNumber: Int) {
        _uiState.update { state ->
            val newSet = state.completedSteps.toMutableSet()
            if (newSet.contains(stepNumber)) {
                newSet.remove(stepNumber)
            } else {
                newSet.add(stepNumber)
            }
            state.copy(completedSteps = newSet)
        }
    }

    fun startStepTimer(step: RecipeStep) {
        val seconds = step.timerSeconds ?: return
        stopTimer()
        _uiState.update {
            it.copy(
                activeTimerStep = step,
                timerTotalSeconds = seconds,
                timerRemainingSeconds = seconds,
                isTimerRunning = true
            )
        }
        runTimer()
    }

    fun pauseResumeTimer() {
        val isCurrentlyRunning = _uiState.value.isTimerRunning
        if (isCurrentlyRunning) {
            timerJob?.cancel()
            _uiState.update { it.copy(isTimerRunning = false) }
        } else {
            if (_uiState.value.timerRemainingSeconds > 0) {
                _uiState.update { it.copy(isTimerRunning = true) }
                runTimer()
            }
        }
    }

    fun resetTimer() {
        val total = _uiState.value.timerTotalSeconds
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerRemainingSeconds = total,
                isTimerRunning = false
            )
        }
    }

    fun dismissTimer() {
        stopTimer()
        _uiState.update {
            it.copy(
                activeTimerStep = null,
                timerRemainingSeconds = 0,
                timerTotalSeconds = 0,
                isTimerRunning = false
            )
        }
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                _uiState.update { current ->
                    val next = current.timerRemainingSeconds - 1
                    if (next <= 0) {
                        current.copy(
                            timerRemainingSeconds = 0,
                            isTimerRunning = false,
                            snackbarMessage = "⏰ Step timer finished! Ready for next step."
                        )
                    } else {
                        current.copy(timerRemainingSeconds = next)
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun openCookingMode() {
        _uiState.update { it.copy(showCookingMode = true, cookingModeStepIndex = 0) }
    }

    fun closeCookingMode() {
        _uiState.update { it.copy(showCookingMode = false) }
    }

    fun nextCookingStep() {
        _uiState.update { state ->
            val maxIndex = (state.currentRecipe?.instructions?.size ?: 1) - 1
            val next = (state.cookingModeStepIndex + 1).coerceAtMost(maxIndex)
            state.copy(cookingModeStepIndex = next)
        }
    }

    fun prevCookingStep() {
        _uiState.update { state ->
            val prev = (state.cookingModeStepIndex - 1).coerceAtLeast(0)
            state.copy(cookingModeStepIndex = prev)
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
