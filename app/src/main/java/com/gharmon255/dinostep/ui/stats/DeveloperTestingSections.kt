package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.components.RarityBadge
import com.gharmon255.dinostep.ui.components.RarityOutlinedButton
import com.gharmon255.dinostep.game.EggRewardDebugState
import com.gharmon255.dinostep.wear.WearSyncDebugState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeveloperSpeciesOverrideCard(
    viewModel: GameViewModel,
    onForceEgg: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSpeciesMenu by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
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
                text = "Test Species Override applies only to Force Selected Species Egg. " +
                    "Rarity buttons and normal gameplay ignore it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Test Species Override",
                style = MaterialTheme.typography.labelLarge,
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showSpeciesMenu = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(viewModel.nextEggTestSpecies.displayName)
                }
                DropdownMenu(
                    expanded = showSpeciesMenu,
                    onDismissRequest = { showSpeciesMenu = false },
                ) {
                    NextEggTestSpecies.selectableOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                viewModel.updateNextEggTestSpecies(option)
                                showSpeciesMenu = false
                            },
                        )
                    }
                }
            }

            Button(
                onClick = onForceEgg,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Force Selected Species Egg")
            }
        }
    }
}

@Composable
fun GiveRandomEggByRarityCard(
    viewModel: GameViewModel,
    eggDebug: EggRewardDebugState,
    onRequestEggGrant: (PendingEggGrant) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Give Random Egg by Rarity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Ignores Test Species Override. Weighted random: " +
                    "Common 65%, Uncommon 22%, Rare 9%, Epic 3%, Legendary 1%.",
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
                onClick = { onRequestEggGrant(PendingEggGrant.Random) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Give Random Egg (Weighted)")
            }
            RarityOutlinedButton(
                label = "Give Common Egg",
                eggRarity = EggRarity.COMMON,
                onClick = { onRequestEggGrant(PendingEggGrant.Specific(EggRarity.COMMON)) },
            )
            RarityOutlinedButton(
                label = "Give Uncommon Egg",
                eggRarity = EggRarity.UNCOMMON,
                onClick = { onRequestEggGrant(PendingEggGrant.Specific(EggRarity.UNCOMMON)) },
            )
            RarityOutlinedButton(
                label = "Give Rare Egg",
                eggRarity = EggRarity.RARE,
                onClick = { onRequestEggGrant(PendingEggGrant.Specific(EggRarity.RARE)) },
            )
            RarityOutlinedButton(
                label = "Give Epic Egg",
                eggRarity = EggRarity.EPIC,
                onClick = { onRequestEggGrant(PendingEggGrant.Specific(EggRarity.EPIC)) },
            )
            RarityOutlinedButton(
                label = "Give Legendary Egg",
                eggRarity = EggRarity.LEGENDARY,
                onClick = { onRequestEggGrant(PendingEggGrant.Specific(EggRarity.LEGENDARY)) },
            )
        }
    }
}

@Composable
fun DestructiveTestingToolsCard(
    onClearCollection: () -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
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
                onClick = onClearCollection,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Clear Collection")
            }

            OutlinedButton(
                onClick = onResetGame,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset Game")
            }
        }
    }
}

@Composable
fun WearSyncDebugCard(
    wearDebug: WearSyncDebugState,
    onForceWatchSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val lastSyncTimeLabel = wearDebug.lastAttemptTimeMillis?.let { millis ->
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))
    } ?: "—"

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Wear sync debug",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "DEBUG only. Emulator: pair phone + Wear AVDs; no real steps required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            StatRow(
                label = "Wear nodes connected",
                value = numberFormat.format(wearDebug.connectedNodeCount),
            )
            StatRow(
                label = "Last sync attempt",
                value = lastSyncTimeLabel,
            )
            StatRow(
                label = "Last sync status",
                value = wearDebug.lastStatusMessage,
            )
            StatRow(
                label = "Last species ID",
                value = wearDebug.lastPayloadCreatureId,
            )
            StatRow(
                label = "Last display name",
                value = wearDebug.lastPayloadDisplayName,
            )
            StatRow(
                label = "Last stage",
                value = wearDebug.lastPayloadStage,
            )
            StatRow(
                label = "Last egg rarity",
                value = wearDebug.lastPayloadEggRarity,
            )
            StatRow(
                label = "Last creature rarity",
                value = wearDebug.lastPayloadCreatureRarity,
            )
            StatRow(
                label = "Last progress %",
                value = wearDebug.lastPayloadProgressPercent?.let { "${it.toInt()}%" } ?: "—",
            )
            StatRow(
                label = "Asset-backed species",
                value = if (wearDebug.lastPayloadIsAssetBacked) "yes" else "no",
            )
            StatRow(
                label = "Last drawable key",
                value = wearDebug.lastPayloadStageDrawableKey,
            )
            StatRow(
                label = "Last steps",
                value = wearDebug.lastPayloadSteps?.let { numberFormat.format(it) } ?: "—",
            )
            StatRow(
                label = "Last steps to next stage",
                value = wearDebug.lastPayloadStepsUntilNext?.let { numberFormat.format(it) } ?: "—",
            )
            StatRow(
                label = "Last next stage label",
                value = wearDebug.lastPayloadNextStageLabel,
            )
            Text(
                text = "Payload log: ${wearDebug.lastPayloadSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onForceWatchSync,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Force Wear Sync")
            }
        }
    }
}

sealed class PendingEggGrant {
    data object Random : PendingEggGrant()

    data class Specific(val eggRarity: EggRarity) : PendingEggGrant()

    data object ForceTestEgg : PendingEggGrant()
}

fun executeEggGrant(viewModel: GameViewModel, grant: PendingEggGrant) {
    when (grant) {
        PendingEggGrant.Random -> viewModel.giveRandomEggForTesting()
        is PendingEggGrant.Specific -> viewModel.giveEggForTesting(grant.eggRarity)
        PendingEggGrant.ForceTestEgg -> viewModel.forceTestEggForTesting()
    }
}
