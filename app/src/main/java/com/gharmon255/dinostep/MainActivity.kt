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
import com.gharmon255.dinostep.cloud.extractGoogleIdToken
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.game.GameViewModelFactory
import com.gharmon255.dinostep.ui.DinoStepApp
import com.gharmon255.dinostep.ui.theme.DinoStepTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {
    private var googleSignInTokenHandler: ((String) -> Unit)? = null
    private var gameViewModel: GameViewModel? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val handler = googleSignInTokenHandler
        googleSignInTokenHandler = null
        if (result.resultCode == RESULT_OK && handler != null) {
            try {
                val idToken = extractGoogleIdToken(result.data)
                handler(idToken)
            } catch (_: Exception) {
                // ViewModel surfaces errors via cloud sync state.
            }
        }
    }

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
            cloudSaveSyncEngine = app.cloudSaveSyncEngine,
            battleRepository = app.battleRepository,
            promoRepository = app.promoRepository,
        )

        setContent {
            DinoStepTheme {
                val viewModel: GameViewModel = viewModel(factory = viewModelFactory)
                gameViewModel = viewModel

                if (viewModel.isReady) {
                    DinoStepApp(
                        viewModel = viewModel,
                        healthConnectPermissionContract = app.healthConnectRepository.permissionContract,
                        supabaseConfig = app.supabaseConfig,
                        onGoogleSignIn = { startGoogleSignIn(app.supabaseConfig.googleWebClientId) },
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

    private fun startGoogleSignIn(webClientId: String) {
        if (webClientId.isBlank()) {
            return
        }
        googleSignInTokenHandler = { idToken ->
            gameViewModel?.signInWithGoogleIdToken(idToken)
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInLauncher.launch(GoogleSignIn.getClient(this, gso).signInIntent)
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
