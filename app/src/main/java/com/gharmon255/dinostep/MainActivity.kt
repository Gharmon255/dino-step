package com.gharmon255.dinostep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.game.GameViewModelFactory
import com.gharmon255.dinostep.ui.DinoStepApp
import com.gharmon255.dinostep.ui.theme.DinoStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as DinoStepApplication).gameRepository
        val viewModelFactory = GameViewModelFactory(repository)

        setContent {
            DinoStepTheme {
                val viewModel: GameViewModel = viewModel(factory = viewModelFactory)

                if (viewModel.isReady) {
                    DinoStepApp(viewModel = viewModel)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
