package com.gharmon255.dinostep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.ui.DinoStepApp
import com.gharmon255.dinostep.ui.theme.DinoStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DinoStepTheme {
                val viewModel: GameViewModel = viewModel()

                DinoStepApp(viewModel = viewModel)
            }
        }
    }
}
