package com.gharmon255.dinostep.ui.eggs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.home.CreaturePlaceholder
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EggsScreen(
    viewModel: GameViewModel,
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
            text = "Mystery Common Egg",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        CreaturePlaceholder(
            stage = viewModel.stage,
            emoji = viewModel.creatureEmoji,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Active egg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                StatRow(label = "Status", value = viewModel.displayName)
                StatRow(label = "Stage", value = viewModel.stage.name)
                StatRow(label = "Steps", value = numberFormat.format(viewModel.steps))
                StatRow(
                    label = "Next milestone",
                    value = viewModel.nextMilestone?.let { numberFormat.format(it) } ?: "Complete",
                )
                StatRow(
                    label = "Progress",
                    value = "${viewModel.progressPercent.toInt()}%",
                )

                LinearProgressIndicator(
                    progress = { viewModel.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (viewModel.isRevealed) {
                    Text(
                        text = "Hatched: ${viewModel.activeCreatureState.creature.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "A common dinosaur is hidden inside until hatch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
