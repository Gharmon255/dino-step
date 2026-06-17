package com.gharmon255.dinostep.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.DiscoveryCelebration
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.ui.collection.CollectionRoster
import com.gharmon255.dinostep.ui.components.CollectionCreatureAvatar
import com.gharmon255.dinostep.ui.theme.rarityColors

@Composable
fun HomeCollectionStrip(
    collection: List<CompletedCreature>,
    dexDiscovered: Int,
    dexTotal: Int,
    modifier: Modifier = Modifier,
) {
    val entries = CollectionRoster.buildEntries(collection)
        .filter { it.isCollected }
        .sortedByDescending { it.latestCompletedAt ?: 0L }

    if (entries.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
        ) {
            Text(
                text = "Gotta grow them to full adult before your first dino joins the collection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Your dinos · $dexDiscovered/$dexTotal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(entries, key = { it.creature.id }) { entry ->
                val colors = rarityColors(entry.creature.rarity)
                Card(
                    modifier = Modifier.width(132.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        colors.border.copy(alpha = 0.7f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CollectionCreatureAvatar(
                            visual = CreatureVisualMapper.collectionVisual(entry.creature),
                            creatureId = entry.creature.id,
                            rarity = entry.creature.rarity,
                            frameSize = 64.dp,
                            imageSize = 52.dp,
                        )
                        Text(
                            text = entry.creature.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (entry.collectCount > 1) {
                            Text(
                                text = "×${entry.collectCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveryCelebrationDialog(
    celebration: DiscoveryCelebration?,
    onDismiss: () -> Unit,
) {
    val current = celebration ?: return

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Egg hatched!")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Meet ${current.speciesName}!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = current.funFact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Awesome!")
            }
        },
    )
}
