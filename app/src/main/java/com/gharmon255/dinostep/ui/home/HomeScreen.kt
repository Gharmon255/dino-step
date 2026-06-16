package com.gharmon255.dinostep.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.game.DevTools
import com.gharmon255.dinostep.game.DuplicateTradeOffer
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.model.toRarity
import com.gharmon255.dinostep.ui.common.HealthConnectCard
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.components.CreatureStageVisual
import com.gharmon255.dinostep.ui.components.GameCreatureCard
import com.gharmon255.dinostep.ui.theme.DinoStepTheme
import com.gharmon255.dinostep.ui.theme.RarityScreenBackground
import com.gharmon255.dinostep.ui.theme.rarityColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onRequestHealthPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = viewModel.activeCreatureState
    HomeScreenContent(
        activeCreature = active,
        displayName = viewModel.displayName,
        steps = viewModel.steps,
        stage = viewModel.stage,
        nextMilestone = viewModel.nextMilestone,
        isAdult = viewModel.isAdult,
        healthConnectStatus = viewModel.healthConnectStatus,
        lastSyncedStepTotal = viewModel.lastSyncedStepTotal,
        syncStatusMessage = viewModel.syncStatusMessage,
        isSyncing = viewModel.isSyncing,
        onAddSteps = viewModel::addSteps,
        onSyncSteps = { viewModel.syncHealthSteps(manual = true) },
        onRequestHealthPermission = onRequestHealthPermission,
        duplicateTradeOffer = viewModel.duplicateTradeOffer,
        onClaimReward = viewModel::claimRandomReward,
        onTradeDuplicates = viewModel::tradeDuplicatesForTierUpEgg,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreenContent(
    activeCreature: ActiveCreatureState,
    displayName: String,
    steps: Int,
    stage: GrowthStage,
    nextMilestone: Int?,
    isAdult: Boolean,
    healthConnectStatus: HealthConnectUiStatus,
    lastSyncedStepTotal: Int,
    syncStatusMessage: String?,
    isSyncing: Boolean,
    onAddSteps: (Int) -> Unit,
    onSyncSteps: () -> Unit,
    onRequestHealthPermission: () -> Unit,
    duplicateTradeOffer: DuplicateTradeOffer?,
    onClaimReward: () -> Unit,
    onTradeDuplicates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTradeConfirmation by remember { mutableStateOf(false) }
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val canSync = healthConnectStatus is HealthConnectUiStatus.Ready && !isSyncing
    val creatureRarity = activeCreature.creature.rarity.takeIf { activeCreature.isRevealed }
    val ambientRarity = creatureRarity ?: activeCreature.eggRarity.toRarity()
    val progressColors = rarityColors(ambientRarity)

    Box(modifier = modifier.fillMaxSize()) {
        RarityScreenBackground(rarity = ambientRarity)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        val stageProgress = HomeStageProgressText.stageProgressPercent(activeCreature)
        val overallProgress = HomeStageProgressText.overallProgressPercent(activeCreature)

        GameCreatureCard(
            title = displayName,
            stage = stage,
            eggRarity = activeCreature.eggRarity,
            creatureRarity = creatureRarity,
        ) {
            CreatureStageVisual(activeCreature = activeCreature)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = HomeStageProgressText.formatStageProgressLabel(activeCreature),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = progressColors.accent,
                modifier = Modifier.fillMaxWidth(),
            )
            LinearProgressIndicator(
                progress = { stageProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = progressColors.accent,
                trackColor = progressColors.container,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = HomeStageProgressText.formatOverallProgressLabel(activeCreature),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            LinearProgressIndicator(
                progress = { overallProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(6.dp),
                color = progressColors.accent.copy(alpha = 0.55f),
                trackColor = progressColors.container.copy(alpha = 0.6f),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatRow(label = "Steps", value = numberFormat.format(steps))
                StatRow(
                    label = "Next stage",
                    value = HomeStageProgressText.formatNextStageLine(
                        activeCreature = activeCreature,
                        numberFormat = numberFormat,
                    ),
                )
                nextMilestone?.let { milestoneAt ->
                    StatRow(
                        label = "Milestone at",
                        value = "${numberFormat.format(milestoneAt)} total steps",
                    )
                }
            }
        }

        Button(
            onClick = onSyncSteps,
            enabled = canSync,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSyncing) "Syncing…" else "Sync Now")
        }

        syncStatusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (healthConnectStatus !is HealthConnectUiStatus.Ready) {
            HealthConnectCard(
                status = healthConnectStatus,
                lastSyncedStepTotal = lastSyncedStepTotal,
                onRequestPermission = onRequestHealthPermission,
            )
        }

        if (DevTools.isEnabled) {
            Text(
                text = "Debug: fake steps",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepButton(
                    label = "+500",
                    onClick = { onAddSteps(500) },
                    modifier = Modifier.weight(1f),
                )
                StepButton(
                    label = "+2000",
                    onClick = { onAddSteps(2_000) },
                    modifier = Modifier.weight(1f),
                )
                StepButton(
                    label = "+10000",
                    onClick = { onAddSteps(10_000) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (isAdult) {
            Button(
                onClick = onClaimReward,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Claim Random Egg")
            }

            duplicateTradeOffer?.let { tradeOffer ->
                OutlinedButton(
                    onClick = { showTradeConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(tradeOffer.tradeButtonTitle)
                }
                Text(
                    text = tradeOffer.helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        }
    }

    if (showTradeConfirmation && duplicateTradeOffer != null) {
        AlertDialog(
            onDismissRequest = { showTradeConfirmation = false },
            title = { Text("Trade for tier-up egg?") },
            text = { Text(duplicateTradeOffer.confirmationMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTradeConfirmation = false
                        onTradeDuplicates()
                    },
                ) {
                    Text("Trade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTradeConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun StepButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(label)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DinoStepTheme {
        HomeScreenContent(
            activeCreature = ActiveCreatureState(
                creature = CreatureCatalog.tinyRaptor,
                eggRarity = EggRarity.COMMON,
                steps = 900,
            ),
            displayName = "Mystery Common Egg",
            steps = 900,
            stage = GrowthStage.EGG,
            nextMilestone = CreatureCatalog.tinyRaptor.hatchStep,
            isAdult = false,
            healthConnectStatus = HealthConnectUiStatus.PermissionRequired,
            lastSyncedStepTotal = 0,
            syncStatusMessage = null,
            isSyncing = false,
            onAddSteps = {},
            onSyncSteps = {},
            onRequestHealthPermission = {},
            duplicateTradeOffer = null,
            onClaimReward = {},
            onTradeDuplicates = {},
        )
    }
}
