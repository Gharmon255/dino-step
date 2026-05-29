package com.gharmon255.dinostep.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.ui.common.HealthConnectCard
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.components.CreatureStageVisual
import com.gharmon255.dinostep.ui.components.GameCreatureCard
import com.gharmon255.dinostep.ui.theme.DinoStepTheme
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
        progressPercent = viewModel.progressPercent,
        isAdult = viewModel.isAdult,
        healthConnectStatus = viewModel.healthConnectStatus,
        lastSyncedStepTotal = viewModel.lastSyncedStepTotal,
        syncStatusMessage = viewModel.syncStatusMessage,
        isSyncing = viewModel.isSyncing,
        onAddSteps = viewModel::addSteps,
        onSyncSteps = viewModel::syncHealthSteps,
        onRequestHealthPermission = onRequestHealthPermission,
        onClaimReward = viewModel::claimReward,
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
    progressPercent: Float,
    isAdult: Boolean,
    healthConnectStatus: HealthConnectUiStatus,
    lastSyncedStepTotal: Int,
    syncStatusMessage: String?,
    isSyncing: Boolean,
    onAddSteps: (Int) -> Unit,
    onSyncSteps: () -> Unit,
    onRequestHealthPermission: () -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val canSync = healthConnectStatus is HealthConnectUiStatus.Ready && !isSyncing
    val creatureRarity = activeCreature.creature.rarity.takeIf { activeCreature.isRevealed }
    val progressColors = if (creatureRarity != null) {
        rarityColors(creatureRarity)
    } else {
        rarityColors(activeCreature.eggRarity)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GameCreatureCard(
            title = displayName,
            stage = stage,
            eggRarity = activeCreature.eggRarity,
            creatureRarity = creatureRarity,
            progressPercent = progressPercent,
        ) {
            CreatureStageVisual(activeCreature = activeCreature)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = progressColors.accent,
                trackColor = progressColors.container,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatRow(label = "Steps", value = numberFormat.format(steps))
                StatRow(
                    label = "Next milestone",
                    value = nextMilestone?.let { numberFormat.format(it) } ?: "Complete",
                )
            }
        }

        Button(
            onClick = onSyncSteps,
            enabled = canSync,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSyncing) "Syncing…" else "Sync Steps")
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

        if (isAdult) {
            Button(
                onClick = onClaimReward,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Claim Reward")
            }
        }
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
            progressPercent = 56.25f,
            isAdult = false,
            healthConnectStatus = HealthConnectUiStatus.PermissionRequired,
            lastSyncedStepTotal = 0,
            syncStatusMessage = null,
            isSyncing = false,
            onAddSteps = {},
            onSyncSteps = {},
            onRequestHealthPermission = {},
            onClaimReward = {},
        )
    }
}
