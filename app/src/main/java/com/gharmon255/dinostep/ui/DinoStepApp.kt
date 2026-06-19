package com.gharmon255.dinostep.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.cloud.SupabaseConfig
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.ui.collection.CollectionScreen
import com.gharmon255.dinostep.ui.eggs.EggsScreen
import com.gharmon255.dinostep.ui.home.HomeScreen
import com.gharmon255.dinostep.ui.navigation.AppTab
import com.gharmon255.dinostep.ui.onboarding.OnboardingScreen
import com.gharmon255.dinostep.ui.stats.StatsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinoStepApp(
    viewModel: GameViewModel,
    healthConnectPermissionContract: ActivityResultContract<Set<String>, Set<String>>,
    supabaseConfig: SupabaseConfig,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel.isReady) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && viewModel.isReady) {
                viewModel.onAppForeground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = healthConnectPermissionContract,
    ) { granted ->
        viewModel.onHealthPermissionsResult(granted)
        viewModel.refreshHealthConnectStatus()
    }

    val onRequestHealthPermission: () -> Unit = {
        when (viewModel.healthConnectStatus) {
            is HealthConnectUiStatus.PermissionRequired -> {
                permissionLauncher.launch(viewModel.readStepsPermissions)
            }
            else -> viewModel.refreshHealthConnectStatus()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(selectedTab.title) },
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        BottomNavBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                        )
                    }
                }
            },
        ) { innerPadding ->
            when (selectedTab) {
                AppTab.Home -> HomeScreen(
                    viewModel = viewModel,
                    onRequestHealthPermission = onRequestHealthPermission,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                AppTab.Eggs -> EggsScreen(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                AppTab.Collection -> CollectionScreen(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                AppTab.Stats -> StatsScreen(
                    viewModel = viewModel,
                    onRequestHealthPermission = onRequestHealthPermission,
                    onGoogleSignIn = onGoogleSignIn,
                    supabaseConfig = supabaseConfig,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }

        if (viewModel.showOnboarding) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                OnboardingScreen(onFinished = viewModel::completeOnboarding)
            }
        }

        if (viewModel.showWhatsNew) {
            AlertDialog(
                onDismissRequest = viewModel::dismissWhatsNew,
                title = { Text("What's new") },
                text = {
                    Text(
                        "• Daily step goal: walk 5,000+ steps or your dino resets to an egg (500 steps left).\n\n" +
                            "• Egg cracks, dino facts, and your collection on Home.\n\n" +
                            "• Steps must flow into Apple Health (or Health Connect on Android).",
                    )
                },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissWhatsNew) {
                        Text("Got it")
                    }
                },
            )
        }

        viewModel.inactivityPenaltyAlert?.let { message ->
            AlertDialog(
                onDismissRequest = viewModel::dismissInactivityPenaltyAlert,
                title = { Text("Your dino needs more steps") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissInactivityPenaltyAlert) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
    ) {
        AppTab.entries.forEach { tab ->
            TextButton(
                onClick = { onTabSelected(tab) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedTab == tab) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (selectedTab == tab) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
