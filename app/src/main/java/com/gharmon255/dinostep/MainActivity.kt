package com.gharmon255.dinostep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.game.GameViewModelFactory
import com.gharmon255.dinostep.ui.DinoStepApp
import com.gharmon255.dinostep.ui.theme.DinoStepTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val app = application as DinoStepApplication
        app.garminConnectIQManager.bindActivity(this)

        val viewModelFactory = GameViewModelFactory(
            repository = app.gameRepository,
            developerPreferences = app.developerPreferences,
            appExperiencePreferences = app.appExperiencePreferences,
            healthConnectRepository = app.healthConnectRepository,
            healthStepSyncEngine = app.healthStepSyncEngine,
            wearDataLayerPublisher = app.wearDataLayerPublisher,
            garminCompanionPublisher = app.garminCompanionPublisher,
            stageMilestoneNotifier = app.stageMilestoneNotifier,
        )

        setContent {
            DinoStepTheme {
                val viewModel: GameViewModel = viewModel(factory = viewModelFactory)

                if (viewModel.isReady) {
                    DinoStepApp(
                        viewModel = viewModel,
                        healthConnectPermissionContract = app.healthConnectRepository.permissionContract,
                    )
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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
