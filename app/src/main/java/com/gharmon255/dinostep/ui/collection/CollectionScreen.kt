package com.gharmon255.dinostep.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.CreatureFacts
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.Habitat
import com.gharmon255.dinostep.model.StageVisual
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.ui.components.CollectionCreatureAvatar
import com.gharmon255.dinostep.ui.components.LockedCollectionAvatar
import com.gharmon255.dinostep.ui.components.RarityBadge
import com.gharmon255.dinostep.ui.theme.rarityColors
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
    var sort by remember { mutableStateOf(CollectionDefaultSort) }
    var selectedEntry by remember { mutableStateOf<RosterEntry?>(null) }

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    val chipRarity = option.toRarity()
                    val chipColors = chipRarity?.let { rarityColors(it) }
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(filterLabel(option)) },
                        colors = if (chipColors != null) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColors.container,
                                selectedLabelColor = chipColors.onContainer,
                                selectedLeadingIconColor = chipColors.accent,
                            )
                        } else {
                            FilterChipDefaults.filterChipColors()
                        },
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
                onCollectedClick = { selectedEntry = entry },
            )
        }
    }

    selectedEntry?.let { entry ->
        CollectionSpeciesDetailSheet(
            entry = entry,
            dateFormat = dateFormat,
            onDismiss = { selectedEntry = null },
        )
    }
}

@Composable
private fun CollectionSummaryCard(summary: CollectionSummary) {
    val overallProgress = if (summary.totalPossibleSpecies == 0) {
        0f
    } else {
        summary.uniqueSpeciesCollected.toFloat() / summary.totalPossibleSpecies
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "DINO DEX",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Discovered",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = summary.uniqueSpeciesCollected.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = " / ${summary.totalPossibleSpecies}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.completionPercent}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary,
            )

            if (summary.totalCollectedDinosaurs > summary.uniqueSpeciesCollected) {
                Text(
                    text = "${summary.totalCollectedDinosaurs} adults claimed " +
                        "(${summary.uniqueSpeciesCollected} unique species)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                )
            }

            Text(
                text = "By rarity",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            summary.rarityProgress.forEach { progress ->
                RarityProgressRow(progress = progress)
            }
        }
    }
}

@Composable
private fun RarityProgressRow(progress: RarityProgress) {
    val colors = rarityColors(progress.rarity)
    val fraction = if (progress.totalSpecies == 0) {
        0f
    } else {
        progress.collectedSpecies.toFloat() / progress.totalSpecies
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RarityBadge(rarity = progress.rarity)
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            trackColor = colors.container,
            color = colors.accent,
        )
        Text(
            text = "${progress.collectedSpecies}/${progress.totalSpecies}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.accent,
            modifier = Modifier.width(40.dp),
        )
    }
}

@Composable
private fun RosterCreatureCard(
    entry: RosterEntry,
    numberFormat: NumberFormat,
    dateFormat: DateFormat,
    onCollectedClick: (RosterEntry) -> Unit,
) {
    val collected = entry.isCollected
    val colors = rarityColors(entry.creature.rarity)
    val avatarVisual = CreatureVisualMapper.collectionVisual(entry.creature)

    val cardShape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (collected) {
                    Modifier
                        .clickable { onCollectedClick(entry) }
                        .border(2.dp, colors.border.copy(alpha = 0.85f), cardShape)
                } else {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        cardShape,
                    )
                },
            ),
        shape = cardShape,
        colors = if (collected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 1f,
                ),
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            )
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (collected) 2.dp else 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (collected) {
                CollectionCreatureAvatar(
                    visual = avatarVisual,
                    creatureId = entry.creature.id,
                    rarity = entry.creature.rarity,
                )
            } else {
                LockedCollectionAvatar()
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (collected) entry.creature.name else "Undiscovered",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (collected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RarityBadge(rarity = entry.creature.rarity)
                    CollectionStatusChip(collected = collected)
                }

                if (collected) {
                    Text(
                        text = habitatLabel(entry.creature.habitat),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = CreatureFacts.forSpecies(entry.creature.id),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tap to view all growth stages",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                    Text(
                        text = "Adult · ${numberFormat.format(entry.creature.totalStepsRequired)} steps to grow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                            text = "Latest: ${dateFormat.format(Date(completedAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Text(
                        text = "Complete this species to reveal its art",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    )
                    Text(
                        text = "${numberFormat.format(entry.creature.totalStepsRequired)} steps to grow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun CollectionFilter.toRarity(): Rarity? = when (this) {
    CollectionFilter.COMMON -> Rarity.COMMON
    CollectionFilter.UNCOMMON -> Rarity.UNCOMMON
    CollectionFilter.RARE -> Rarity.RARE
    CollectionFilter.EPIC -> Rarity.EPIC
    CollectionFilter.LEGENDARY -> Rarity.LEGENDARY
    else -> null
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
    CollectionSort.CATALOG -> "Catalog"
    CollectionSort.STEPS -> "Steps"
}

@Composable
private fun CollectionStatusChip(collected: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (collected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        },
    ) {
        Text(
            text = if (collected) "Discovered" else "Locked",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (collected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private fun habitatLabel(habitat: Habitat): String {
    val name = habitat.name.lowercase().replaceFirstChar { it.uppercase() }
    return "Habitat: $name"
}
