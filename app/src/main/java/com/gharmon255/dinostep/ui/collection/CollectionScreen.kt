package com.gharmon255.dinostep.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.Habitat
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.ui.common.StatRow
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun CollectionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    val summary = remember(viewModel.collection) { CollectionRoster.buildSummary(viewModel.collection) }
    val rosterEntries = remember(viewModel.collection) { CollectionRoster.buildEntries(viewModel.collection) }

    var filter by remember { mutableStateOf(CollectionFilter.ALL) }
    var sort by remember { mutableStateOf(CollectionSort.COLLECTED_FIRST) }

    val displayedEntries = remember(rosterEntries, filter, sort) {
        CollectionRoster.applySort(
            CollectionRoster.applyFilter(rosterEntries, filter),
            sort,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Collection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            CollectionSummaryCard(summary = summary)
        }

        item {
            Text(
                text = "Filter",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CollectionFilter.entries.toList(), key = { it.name }) { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(filterLabel(option)) },
                    )
                }
            }
        }

        item {
            Text(
                text = "Sort",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CollectionSort.entries.toList(), key = { it.name }) { option ->
                    FilterChip(
                        selected = sort == option,
                        onClick = { sort = option },
                        label = { Text(sortLabel(option)) },
                    )
                }
            }
        }

        item {
            Text(
                text = "${displayedEntries.size} shown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        items(
            items = displayedEntries,
            key = { it.creature.id },
        ) { entry ->
            RosterCreatureCard(
                entry = entry,
                numberFormat = numberFormat,
                dateFormat = dateFormat,
            )
        }
    }
}

@Composable
private fun CollectionSummaryCard(summary: CollectionSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Collection progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            StatRow(
                label = "Total collected",
                value = summary.totalCollectedDinosaurs.toString(),
            )
            StatRow(
                label = "Unique species",
                value = "${summary.uniqueSpeciesCollected} / ${summary.totalPossibleSpecies}",
            )
            StatRow(
                label = "Completion",
                value = "${summary.completionPercent}%",
            )

            LinearProgressIndicator(
                progress = {
                    if (summary.totalPossibleSpecies == 0) {
                        0f
                    } else {
                        summary.uniqueSpeciesCollected.toFloat() / summary.totalPossibleSpecies
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            summary.rarityProgress.forEach { progress ->
                StatRow(
                    label = progress.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                    value = "${progress.collectedSpecies} / ${progress.totalSpecies}",
                )
            }
        }
    }
}

@Composable
private fun RosterCreatureCard(
    entry: RosterEntry,
    numberFormat: NumberFormat,
    dateFormat: DateFormat,
) {
    val collected = entry.isCollected
    val cardAlpha = if (collected) 1f else 0.85f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        colors = if (collected) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (collected) entry.creature.emoji else "❓",
                fontSize = 40.sp,
                modifier = Modifier.alpha(if (collected) 1f else 0.5f),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (collected) entry.creature.name else "Undiscovered",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                RarityLabel(rarity = entry.creature.rarity)

                Text(
                    text = if (collected) {
                        habitatLabel(entry.creature.habitat)
                    } else {
                        "Habitat: ???"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "${numberFormat.format(entry.creature.totalStepsRequired)} steps to grow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (collected) {
                    if (entry.collectCount > 1) {
                        Text(
                            text = "Collected ×${entry.collectCount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    entry.latestCompletedAt?.let { completedAt ->
                        Text(
                            text = "Latest: ${dateFormat.format(Date(completedAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RarityLabel(rarity: Rarity) {
    val color = when (rarity) {
        Rarity.COMMON -> MaterialTheme.colorScheme.onSurfaceVariant
        Rarity.UNCOMMON -> MaterialTheme.colorScheme.primary
        Rarity.RARE -> MaterialTheme.colorScheme.tertiary
        Rarity.EPIC -> MaterialTheme.colorScheme.secondary
        Rarity.LEGENDARY -> MaterialTheme.colorScheme.error
    }
    Text(
        text = rarity.name,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

private fun filterLabel(filter: CollectionFilter): String = when (filter) {
    CollectionFilter.ALL -> "All"
    CollectionFilter.COMMON -> "Common"
    CollectionFilter.UNCOMMON -> "Uncommon"
    CollectionFilter.RARE -> "Rare"
    CollectionFilter.EPIC -> "Epic"
    CollectionFilter.LEGENDARY -> "Legendary"
    CollectionFilter.COLLECTED -> "Collected"
    CollectionFilter.LOCKED -> "Locked"
}

private fun sortLabel(sort: CollectionSort): String = when (sort) {
    CollectionSort.RARITY -> "Rarity"
    CollectionSort.NAME -> "Name"
    CollectionSort.COLLECTED_FIRST -> "Collected"
    CollectionSort.STEPS -> "Steps"
}

private fun habitatLabel(habitat: Habitat): String {
    val name = habitat.name.lowercase().replaceFirstChar { it.uppercase() }
    return "Habitat: $name"
}
