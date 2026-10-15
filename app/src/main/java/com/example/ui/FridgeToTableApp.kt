package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.model.Recipe
import com.example.ui.components.ActiveTimerCard
import com.example.ui.components.CookingModeDialog
import com.example.ui.components.FavoritesBottomSheet
import com.example.ui.components.IngredientInputSection
import com.example.ui.components.RecipeDetailCard
import com.example.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeToTableApp(
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Kitchen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Fridge-to-Table",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openFavoritesSheet() },
                        modifier = Modifier.testTag("open_favorites_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (favorites.isNotEmpty()) {
                                    Badge {
                                        Text("${favorites.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Saved Favorites",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Fridge-to-Table & Secrets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Timer Banner (Sticky at top when timer is active)
            ActiveTimerCard(
                step = uiState.activeTimerStep,
                remainingSeconds = uiState.timerRemainingSeconds,
                totalSeconds = uiState.timerTotalSeconds,
                isRunning = uiState.isTimerRunning,
                onPauseResume = { viewModel.pauseResumeTimer() },
                onReset = { viewModel.resetTimer() },
                onDismiss = { viewModel.dismissTimer() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Input Section
                item {
                    IngredientInputSection(
                        uiState = uiState,
                        onIngredient1Change = { viewModel.onIngredient1Changed(it) },
                        onIngredient2Change = { viewModel.onIngredient2Changed(it) },
                        onIngredient3Change = { viewModel.onIngredient3Changed(it) },
                        onFieldFocused = { viewModel.setActiveField(it) },
                        onSelectSuggestion = { viewModel.selectSuggestedIngredient(it) },
                        onQuickItemSelect = { viewModel.applyIngredientFromQuickList(it) },
                        onSpeechInput = { text, target -> viewModel.applySpeechInput(text, target) },
                        onAnalyzeImage = { bitmap -> viewModel.analyzeFridgeImage(bitmap) },
                        onTogglePantryBrowser = { viewModel.togglePantryBrowser() },
                        onComboSelect = { a, b, c -> viewModel.setQuickCombo(a, b, c) },
                        onPreferenceSelect = { viewModel.setPreference(it) },
                        onToggleDietaryRestriction = { viewModel.toggleDietaryRestriction(it) },
                        onClearInputs = { viewModel.clearInputs() },
                        onGenerate = { viewModel.generateRecipe() }
                    )
                }

                // 2. Generated Recipe Section
                item {
                    uiState.currentRecipe?.let { recipe ->
                        RecipeDetailCard(
                            recipe = recipe,
                            isLiveGemini = uiState.isLiveGemini,
                            checkedIngredients = uiState.checkedIngredients,
                            completedSteps = uiState.completedSteps,
                            onToggleIngredient = { viewModel.toggleIngredientChecked(it) },
                            onToggleStep = { viewModel.toggleStepCompleted(it) },
                            onStartTimer = { viewModel.startStepTimer(it) },
                            onRegenerate = { viewModel.generateRecipe(isRegenerate = true) },
                            onOpenCookingMode = { viewModel.openCookingMode() },
                            isFavorite = uiState.isCurrentFavorite,
                            onToggleFavorite = { viewModel.toggleFavoriteCurrentRecipe() },
                            onShare = { shareRecipe(context, recipe) },
                            onCopy = {
                                copyRecipeToClipboard(context, recipe)
                                Toast.makeText(context, "Recipe copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Cooking Mode Fullscreen Overlay
    if (uiState.showCookingMode && uiState.currentRecipe != null) {
        CookingModeDialog(
            recipe = uiState.currentRecipe!!,
            currentStepIndex = uiState.cookingModeStepIndex,
            completedSteps = uiState.completedSteps,
            onToggleStepComplete = { viewModel.toggleStepCompleted(it) },
            onStartTimer = { viewModel.startStepTimer(it) },
            onNextStep = { viewModel.nextCookingStep() },
            onPrevStep = { viewModel.prevCookingStep() },
            onDismiss = { viewModel.closeCookingMode() }
        )
    }

    // Saved Favorites Bottom Sheet Overlay
    if (uiState.showFavoritesSheet) {
        FavoritesBottomSheet(
            favorites = favorites,
            onSelectRecipe = { viewModel.loadRecipe(it) },
            onDeleteFavorite = { viewModel.deleteFavorite(it) },
            onDismiss = { viewModel.closeFavoritesSheet() }
        )
    }

    // Info & Gemini API Key Guide Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("About Fridge-to-Table", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Fridge-to-Table transforms 3 random ingredients into viable gourmet recipes instantly without storing recipes in a database.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini API Integration",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Powered by Google Gemini 3.5 Flash! You can configure your free-tier GEMINI_API_KEY in AI Studio's Secrets panel to unlock infinite on-the-fly culinary creativity.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

private fun formatRecipeForSharing(recipe: Recipe): String {
    val sb = StringBuilder()
    sb.appendLine("🍽️ ${recipe.title}")
    sb.appendLine("✨ ${recipe.tagline}")
    sb.appendLine()
    sb.appendLine("⏱️ Total Time: ${recipe.totalTime} | ⚡ ${recipe.difficulty} | 👥 ${recipe.servings}")
    sb.appendLine()
    sb.appendLine("🛒 HERO FRIDGE INGREDIENTS:")
    recipe.inputIngredients.forEach { sb.appendLine("• $it") }
    if (recipe.pantryStaples.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("🧂 PANTRY STAPLES:")
        recipe.pantryStaples.forEach { sb.appendLine("• $it") }
    }
    sb.appendLine()
    sb.appendLine("👨‍🍳 INSTRUCTIONS:")
    recipe.instructions.forEach {
        val timer = if (it.timerSeconds != null) " [Timer: ${it.timerSeconds / 60}m ${it.timerSeconds % 60}s]" else ""
        sb.appendLine("${it.stepNumber}. ${it.actionName}$timer: ${it.description}")
    }
    if (recipe.chefSecretTip.isNotBlank()) {
        sb.appendLine()
        sb.appendLine("💡 Chef's Secret: ${recipe.chefSecretTip}")
    }
    sb.appendLine()
    sb.appendLine("Generated by Fridge-to-Table App")
    return sb.toString()
}

private fun shareRecipe(context: Context, recipe: Recipe) {
    val text = formatRecipeForSharing(recipe)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${recipe.title}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Recipe")
    context.startActivity(shareIntent)
}

private fun copyRecipeToClipboard(context: Context, recipe: Recipe) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Recipe", formatRecipeForSharing(recipe))
    clipboard.setPrimaryClip(clip)
}
