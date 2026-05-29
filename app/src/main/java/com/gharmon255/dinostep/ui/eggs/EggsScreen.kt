package com.gharmon255.dinostep.ui.eggs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.ui.common.StatRow
import com.gharmon255.dinostep.ui.components.CreatureStageVisual
import com.gharmon255.dinostep.ui.components.RarityBadge
import com.gharmon255.dinostep.ui.theme.rarityColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EggsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val activeEggRarity = viewModel.eggRarity
    val poolSize = CreatureCatalog.creaturesForEgg(activeEggRarity).size
    val active = viewModel.activeCreatureState
    val eggColors = rarityColors(activeEggRarity)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RarityBadge(eggRarity = activeEggRarity)

        Text(
            text = activeEggRarity.mysteryDisplayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = eggColors.accent,
        )

        Text(
            text = "$poolSize possible ${activeEggRarity.name.lowercase()} dinosaurs in this pool",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CreatureStageVisual(activeCreature = active)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = eggColors.container.copy(alpha = 0.35f),
            ),
        ) {
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

                StatRow(label = "Egg rarity", value = activeEggRarity.name)
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

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { viewModel.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = eggColors.accent,
                    trackColor = eggColors.container,
                )

                if (viewModel.isRevealed) {
                    Text(
                        text = "Hatched: ${active.creature.name} (${active.creature.rarity.name})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = rarityColors(active.creature.rarity).accent,
                    )
                } else {
                    Text(
                        text = "A random ${activeEggRarity.name.lowercase()} dinosaur is hidden inside until hatch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
