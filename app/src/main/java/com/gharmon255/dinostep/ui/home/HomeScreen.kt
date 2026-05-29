package com.gharmon255.dinostep.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.TinyRaptor
import com.gharmon255.dinostep.ui.theme.DinoStepTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    HomeScreenContent(
        steps = viewModel.steps,
        stage = viewModel.stage,
        nextMilestone = viewModel.nextMilestone,
        progressPercent = viewModel.progressPercent,
        isAdult = viewModel.isAdult,
        onAddSteps = viewModel::addSteps,
        onClaimReward = viewModel::claimReward,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreenContent(
    steps: Int,
    stage: GrowthStage,
    nextMilestone: Int?,
    progressPercent: Float,
    isAdult: Boolean,
    onAddSteps: (Int) -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = TinyRaptor.NAME,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        CreaturePlaceholder(stage = stage)

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
            steps = 900,
            stage = GrowthStage.EGG,
            nextMilestone = TinyRaptor.HATCH_STEPS,
            progressPercent = 56.25f,
            isAdult = false,
            onAddSteps = {},
            onClaimReward = {},
        )
    }
}
