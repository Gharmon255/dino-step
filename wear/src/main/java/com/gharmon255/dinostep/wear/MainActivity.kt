package com.gharmon255.dinostep.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gharmon255.dinostep.wear.ui.WearMainScreen
import com.gharmon255.dinostep.wear.ui.theme.WearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as WearApplication).watchStateRepository
        val viewModelFactory = WearMainViewModelFactory(repository)

        setContent {
            val viewModel: WearMainViewModel = viewModel(factory = viewModelFactory)
            val watchState by viewModel.watchState.collectAsStateWithLifecycle()

            WearTheme {
                WearMainScreen(state = watchState)
            }
        }
    }
}
