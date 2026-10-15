package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FridgeToTableApp
import com.example.ui.theme.FridgeToTableTheme
import com.example.ui.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FridgeToTableTheme {
                val viewModel: RecipeViewModel = viewModel()
                FridgeToTableApp(viewModel = viewModel)
            }
        }
    }
}
