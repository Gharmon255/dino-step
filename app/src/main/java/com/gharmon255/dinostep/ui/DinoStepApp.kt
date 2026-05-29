package com.gharmon255.dinostep.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.ui.collection.CollectionScreen
import com.gharmon255.dinostep.ui.eggs.EggsScreen
import com.gharmon255.dinostep.ui.home.HomeScreen
import com.gharmon255.dinostep.ui.navigation.AppTab
import com.gharmon255.dinostep.ui.stats.StatsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinoStepApp(
    viewModel: GameViewModel,
    healthConnectPermissionContract: ActivityResultContract<Set<String>, Set<String>>,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.title) },
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (selectedTab) {
            AppTab.Home -> HomeScreen(
                viewModel = viewModel,
                onRequestHealthPermission = onRequestHealthPermission,
                modifier = screenModifier,
            )
            AppTab.Eggs -> EggsScreen(
                viewModel = viewModel,
                modifier = screenModifier,
            )
            AppTab.Collection -> CollectionScreen(
                viewModel = viewModel,
                modifier = screenModifier,
            )
            AppTab.Stats -> StatsScreen(
                viewModel = viewModel,
                onRequestHealthPermission = onRequestHealthPermission,
                modifier = screenModifier,
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
    Row(modifier = modifier.fillMaxWidth()) {
        AppTab.entries.forEach { tab ->
            TextButton(
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
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
