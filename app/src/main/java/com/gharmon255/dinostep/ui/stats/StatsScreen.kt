package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.DevTools
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.ui.common.HealthConnectCard
import com.gharmon255.dinostep.ui.common.StatRow
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onRequestHealthPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    var showClearCollectionDialog by remember { mutableStateOf(false) }
    var showResetGameDialog by remember { mutableStateOf(false) }
    var showReplaceEggDialog by remember { mutableStateOf(false) }
    var pendingEggGrant by remember { mutableStateOf<PendingEggGrant?>(null) }

    val eggDebug = viewModel.eggRewardDebug

    fun requestEggGrant(grant: PendingEggGrant) {
        if (viewModel.needsReplaceConfirmationForNewEgg()) {
            pendingEggGrant = grant
            showReplaceEggDialog = true
        } else {
            executeEggGrant(viewModel, grant)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HealthConnectCard(
            status = viewModel.healthConnectStatus,
            lastSyncedStepTotal = viewModel.lastSyncedStepTotal,
            onRequestPermission = onRequestHealthPermission,
        )

        if (DevTools.isEnabled) {
            WearSyncDebugCard(
                wearDebug = viewModel.wearSyncDebug,
                onForceWatchSync = viewModel::forceWatchSync,
            )

            DeveloperSpeciesOverrideCard(
                viewModel = viewModel,
                onForceEgg = {
                    if (viewModel.needsReplaceConfirmationForNewEgg()) {
                        pendingEggGrant = PendingEggGrant.ForceTestEgg
                        showReplaceEggDialog = true
                    } else {
                        viewModel.forceTestEggForTesting()
                    }
                },
            )

            GiveRandomEggByRarityCard(
                viewModel = viewModel,
                eggDebug = eggDebug,
                onRequestEggGrant = ::requestEggGrant,
            )

            DestructiveTestingToolsCard(
                onClearCollection = { showClearCollectionDialog = true },
                onResetGame = { showResetGameDialog = true },
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Player stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                StatRow(
                    label = "Active creature steps",
                    value = numberFormat.format(viewModel.steps),
                )
                StatRow(
                    label = "Total fake steps added",
                    value = numberFormat.format(viewModel.totalFakeStepsAdded),
                )
                StatRow(
                    label = "Last synced HC total (today)",
                    value = numberFormat.format(viewModel.lastSyncedStepTotal),
                )
                StatRow(
                    label = "Eggs hatched",
                    value = numberFormat.format(viewModel.eggsHatched),
                )
                StatRow(
                    label = "Completed dinosaurs",
                    value = numberFormat.format(viewModel.completedCount),
                )
                StatRow(
                    label = "Current stage",
                    value = viewModel.stage.name,
                )
                StatRow(
                    label = "Current progress",
                    value = "${viewModel.progressPercent.toInt()}%",
                )
            }
        }
    }

    if (showReplaceEggDialog && pendingEggGrant != null) {
        val grant = pendingEggGrant!!
        AlertDialog(
            onDismissRequest = {
                showReplaceEggDialog = false
                pendingEggGrant = null
            },
            title = { Text("Replace active egg?") },
            text = {
                Text(
                    "Testing only. This replaces your current egg and progress with a new egg. " +
                        "Current steps and hatch progress will be lost.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        executeEggGrant(viewModel, grant)
                        showReplaceEggDialog = false
                        pendingEggGrant = null
                    },
                ) {
                    Text("Replace")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReplaceEggDialog = false
                        pendingEggGrant = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showClearCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showClearCollectionDialog = false },
            title = { Text("Clear collection?") },
            text = {
                Text(
                    "Testing only. This deletes all completed dinosaurs from your collection. " +
                        "Your active egg is kept. Health Connect permissions are not changed.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCollectionForTesting()
                        showClearCollectionDialog = false
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCollectionDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showResetGameDialog) {
        AlertDialog(
            onDismissRequest = { showResetGameDialog = false },
            title = { Text("Reset game?") },
            text = {
                Text(
                    "Testing only. This clears your collection, resets fake-step stats, and gives you " +
                        "a new Mystery Common Egg. Health Connect permission and today's synced step " +
                        "baseline are kept.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetGameForTesting()
                        showResetGameDialog = false
                    },
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetGameDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

