package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.game.NextEggTestSpecies
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.ui.common.HealthConnectCard
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.components.RarityBadge
import com.gharmon255.dinostep.ui.components.RarityOutlinedButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onRequestHealthPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val wearDebug = viewModel.wearSyncDebug
    val lastSyncTimeLabel = wearDebug.lastAttemptTimeMillis?.let { millis ->
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))
    } ?: "—"

    var showDeveloperSpeciesMenu by remember { mutableStateOf(false) }

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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Watch sync debug",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                StatRow(
                    label = "Watch nodes connected",
                    value = numberFormat.format(wearDebug.connectedNodeCount),
                )
                StatRow(
                    label = "Last watch sync attempt",
                    value = lastSyncTimeLabel,
                )
                StatRow(
                    label = "Last watch sync status",
                    value = wearDebug.lastStatusMessage,
                )
                StatRow(
                    label = "Last payload name",
                    value = wearDebug.lastPayloadDisplayName,
                )
                StatRow(
                    label = "Last payload stage",
                    value = wearDebug.lastPayloadStage,
                )
                StatRow(
                    label = "Last payload steps",
                    value = wearDebug.lastPayloadSteps?.let { numberFormat.format(it) } ?: "—",
                )
                StatRow(
                    label = "Last payload steps to next",
                    value = wearDebug.lastPayloadStepsUntilNext?.let { numberFormat.format(it) } ?: "—",
                )
                StatRow(
                    label = "Last payload next stage",
                    value = wearDebug.lastPayloadNextStageLabel,
                )
                Text(
                    text = "Last payload detail: ${wearDebug.lastPayloadSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = viewModel::forceWatchSync,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Force Watch Sync")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Developer Testing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Choose which species the next egg will hatch into, then force a new egg to test art and stages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Next Egg Species",
                    style = MaterialTheme.typography.labelLarge,
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDeveloperSpeciesMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(viewModel.nextEggTestSpecies.displayName)
                    }
                    DropdownMenu(
                        expanded = showDeveloperSpeciesMenu,
                        onDismissRequest = { showDeveloperSpeciesMenu = false },
                    ) {
                        NextEggTestSpecies.selectableOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    viewModel.updateNextEggTestSpecies(option)
                                    showDeveloperSpeciesMenu = false
                                },
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (viewModel.needsReplaceConfirmationForNewEgg()) {
                            pendingEggGrant = PendingEggGrant.ForceTestEgg
                            showReplaceEggDialog = true
                        } else {
                            viewModel.forceTestEggForTesting()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Force New Egg")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Egg testing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Testing only. Weighted reward: Common 65%, Uncommon 22%, Rare 9%, Epic 3%, Legendary 1%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                RarityBadge(eggRarity = viewModel.eggRarity)
                StatRow(label = "Current egg rarity", value = viewModel.eggRarity.name)
                StatRow(
                    label = "Hatched creature rarity",
                    value = viewModel.hatchedCreatureRarity?.name ?: "Not hatched",
                )
                StatRow(
                    label = "Last rewarded egg",
                    value = eggDebug.lastRewardedEggRarity?.name ?: "—",
                )
                StatRow(
                    label = "Last reward roll (0–99)",
                    value = eggDebug.lastRewardRollValue?.toString() ?: "—",
                )

                OutlinedButton(
                    onClick = { requestEggGrant(PendingEggGrant.Random) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Give Random Egg")
                }
                RarityOutlinedButton(
                    label = "Give Uncommon Egg",
                    eggRarity = EggRarity.UNCOMMON,
                    onClick = { requestEggGrant(PendingEggGrant.Specific(EggRarity.UNCOMMON)) },
                )
                RarityOutlinedButton(
                    label = "Give Rare Egg",
                    eggRarity = EggRarity.RARE,
                    onClick = { requestEggGrant(PendingEggGrant.Specific(EggRarity.RARE)) },
                )
                RarityOutlinedButton(
                    label = "Give Epic Egg",
                    eggRarity = EggRarity.EPIC,
                    onClick = { requestEggGrant(PendingEggGrant.Specific(EggRarity.EPIC)) },
                )
                RarityOutlinedButton(
                    label = "Give Legendary Egg",
                    eggRarity = EggRarity.LEGENDARY,
                    onClick = { requestEggGrant(PendingEggGrant.Specific(EggRarity.LEGENDARY)) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Testing tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Debug-only actions. Use while testing — not for normal play.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(
                    onClick = { showClearCollectionDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear Collection")
                }

                OutlinedButton(
                    onClick = { showResetGameDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset Game")
                }
            }
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

private sealed class PendingEggGrant {
    data object Random : PendingEggGrant()

    data class Specific(val eggRarity: EggRarity) : PendingEggGrant()

    data object ForceTestEgg : PendingEggGrant()
}

private fun executeEggGrant(viewModel: GameViewModel, grant: PendingEggGrant) {
    when (grant) {
        PendingEggGrant.Random -> viewModel.giveRandomEggForTesting()
        is PendingEggGrant.Specific -> viewModel.giveEggForTesting(grant.eggRarity)
        PendingEggGrant.ForceTestEgg -> viewModel.forceTestEggForTesting()
    }
}
