package com.gharmon255.dinostep.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureVisualMapper
import java.text.NumberFormat

@Composable
fun StatRow(
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
fun CollectionEntryRow(
    entry: CompletedCreature,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val emoji = CreatureVisualMapper.collectionVisual(entry.creature).speciesEmoji
        Text(
            text = "$emoji ${entry.creature.name}",
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
fun CollectionListCard(
    collection: List<CompletedCreature>,
    numberFormat: NumberFormat,
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
