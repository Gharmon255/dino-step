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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.ui.theme.DinoStepTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    HomeScreenContent(
        displayName = viewModel.displayName,
        isRevealed = viewModel.isRevealed,
        steps = viewModel.steps,
        stage = viewModel.stage,
        creatureEmoji = viewModel.creatureEmoji,
        nextMilestone = viewModel.nextMilestone,
        progressPercent = viewModel.progressPercent,
        isAdult = viewModel.isAdult,
        collection = viewModel.collection,
        onAddSteps = viewModel::addSteps,
        onClaimReward = viewModel::claimReward,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreenContent(
    displayName: String,
    isRevealed: Boolean,
    steps: Int,
    stage: GrowthStage,
    creatureEmoji: String,
    nextMilestone: Int?,
    progressPercent: Float,
    isAdult: Boolean,
    collection: List<CompletedCreature>,
    onAddSteps: (Int) -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        if (!isRevealed) {
            Text(
                text = "Mystery Common Egg",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        CreaturePlaceholder(
            stage = stage,
            emoji = creatureEmoji,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatRow(label = "Stage", value = stage.name)
                StatRow(label = "Steps", value = numberFormat.format(steps))
                StatRow(
                    label = "Next milestone",
                    value = nextMilestone?.let { numberFormat.format(it) } ?: "Complete",
                )
                StatRow(
                    label = "Progress",
                    value = "${progressPercent.toInt()}%",
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

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

        CollectionSection(
            collection = collection,
            numberFormat = numberFormat,
        )
    }
}

@Composable
private fun CollectionSection(
    collection: List<CompletedCreature>,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Collection (${collection.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (collection.isEmpty()) {
                Text(
                    text = "No completed dinosaurs yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                collection.asReversed().forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    CollectionEntryRow(
                        entry = entry,
                        numberFormat = numberFormat,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionEntryRow(
    entry: CompletedCreature,
    numberFormat: NumberFormat,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "${entry.creature.emoji} ${entry.creature.name}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${entry.creature.rarity.name} · ${entry.creature.habitat.name} · ${numberFormat.format(entry.stepsCompleted)} steps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
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
            displayName = ActiveCreatureState.MYSTERY_EGG_NAME,
            isRevealed = false,
            steps = 900,
            stage = GrowthStage.EGG,
            creatureEmoji = CreatureCatalog.tinyRaptor.emoji,
            nextMilestone = CreatureCatalog.tinyRaptor.hatchStep,
            progressPercent = 56.25f,
            isAdult = false,
            collection = listOf(
                CompletedCreature(
                    creature = CreatureCatalog.triceratops,
                    stepsCompleted = 10_000,
                ),
            ),
            onAddSteps = {},
            onClaimReward = {},
        )
    }
}
