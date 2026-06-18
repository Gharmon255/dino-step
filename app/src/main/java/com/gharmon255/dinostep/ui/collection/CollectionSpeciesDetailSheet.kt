package com.gharmon255.dinostep.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureFacts
import com.gharmon255.dinostep.ui.common.NicknameEditDialog
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.ui.components.CatalogCreatureStageVisual
import com.gharmon255.dinostep.ui.components.RarityBadge
import com.gharmon255.dinostep.ui.theme.rarityColors
import java.text.DateFormat
import java.util.Date

private val detailStages = listOf(
    GrowthStage.EGG,
    GrowthStage.BABY,
    GrowthStage.JUVENILE,
    GrowthStage.ADULT,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSpeciesDetailSheet(
    entry: RosterEntry,
    completedCreatures: List<CompletedCreature>,
    dateFormat: DateFormat,
    onDismiss: () -> Unit,
    onUpdateNickname: (Long, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val creature = entry.creature
    val colors = rarityColors(creature.rarity)
    var editingCreature by remember { mutableStateOf<CompletedCreature?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = creature.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RarityBadge(rarity = creature.rarity)
                Text(
                    text = creature.habitat.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "Growth journey",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            detailStages.forEach { stage ->
                CollectionStageDetailRow(creature = creature, stage = stage)
            }

            Text(
                text = "Paleontology fact",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = CreatureFacts.forSpecies(creature.id),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (entry.isCollected) {
                Text(
                    text = "Your adults",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                completedCreatures.forEach { completed ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = completed.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (completed.nickname != null) {
                                Text(
                                    text = creature.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(completed.completedAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { editingCreature = completed }) {
                            Text("Nickname")
                        }
                    }
                }
            }

            if (entry.collectCount > 1) {
                Text(
                    text = "Collected ×${entry.collectCount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent,
                )
            }
            entry.latestCompletedAt?.let { completedAt ->
                Text(
                    text = "Latest adult: ${dateFormat.format(Date(completedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    editingCreature?.let { completed ->
        NicknameEditDialog(
            title = "Nickname your dino",
            speciesName = creature.name,
            initialNickname = completed.nickname,
            onDismiss = { editingCreature = null },
            onSave = { nickname ->
                onUpdateNickname(completed.id, nickname)
            },
        )
    }
}

@Composable
private fun CollectionStageDetailRow(
    creature: com.gharmon255.dinostep.model.CreatureDefinition,
    stage: GrowthStage,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        CatalogCreatureStageVisual(
            creature = creature,
            stage = stage,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stageLabel(stage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = CreatureFacts.stepMilestoneLabel(creature, stage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = CreatureFacts.growthStageNote(creature, stage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun stageLabel(stage: GrowthStage): String = when (stage) {
    GrowthStage.EGG -> "Egg"
    GrowthStage.BABY -> "Hatchling"
    GrowthStage.JUVENILE -> "Juvenile"
    GrowthStage.ADULT -> "Adult"
}
