package com.gharmon255.dinostep.ui.collection

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.Rarity

enum class CollectionFilter {
    ALL,
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    COLLECTED,
    LOCKED,
}

enum class CollectionSort {
    RARITY,
    NAME,
    /** Default: discovered first, then catalog order, then rarity tier, then name. */
    COLLECTED_FIRST,
    CATALOG,
    STEPS,
}

/** Default collection sort (Sprint 5). */
val CollectionDefaultSort: CollectionSort = CollectionSort.COLLECTED_FIRST

data class RarityProgress(
    val rarity: Rarity,
    val collectedSpecies: Int,
    val totalSpecies: Int,
)

data class CollectionSummary(
    val totalCollectedDinosaurs: Int,
    val uniqueSpeciesCollected: Int,
    val totalPossibleSpecies: Int,
    val completionPercent: Int,
    val rarityProgress: List<RarityProgress>,
)

data class RosterEntry(
    val creature: CreatureDefinition,
    val collectCount: Int,
    val latestCompletedAt: Long?,
) {
    val isCollected: Boolean
        get() = collectCount > 0
}

object CollectionRoster {
    fun buildSummary(collection: List<CompletedCreature>): CollectionSummary {
        val entries = buildEntries(collection)
        val unique = entries.count { it.isCollected }
        val totalPossible = CreatureCatalog.all.size
        val percent = if (totalPossible == 0) {
            0
        } else {
            ((unique * 100f) / totalPossible).toInt()
        }
        return CollectionSummary(
            totalCollectedDinosaurs = collection.size,
            uniqueSpeciesCollected = unique,
            totalPossibleSpecies = totalPossible,
            completionPercent = percent,
            rarityProgress = Rarity.entries.map { rarity ->
                RarityProgress(
                    rarity = rarity,
                    collectedSpecies = entries.count { it.isCollected && it.creature.rarity == rarity },
                    totalSpecies = CreatureCatalog.byRarity(rarity).size,
                )
            },
        )
    }

    fun buildEntries(collection: List<CompletedCreature>): List<RosterEntry> {
        val completionsById = collection.groupBy { it.creature.id }
        return CreatureCatalog.all.map { creature ->
            val completions = completionsById[creature.id].orEmpty()
            RosterEntry(
                creature = creature,
                collectCount = completions.size,
                latestCompletedAt = completions.maxOfOrNull { it.completedAt },
            )
        }
    }

    fun applyFilter(entries: List<RosterEntry>, filter: CollectionFilter): List<RosterEntry> {
        return when (filter) {
            CollectionFilter.ALL -> entries
            CollectionFilter.COMMON -> entries.filter { it.creature.rarity == Rarity.COMMON }
            CollectionFilter.UNCOMMON -> entries.filter { it.creature.rarity == Rarity.UNCOMMON }
            CollectionFilter.RARE -> entries.filter { it.creature.rarity == Rarity.RARE }
            CollectionFilter.EPIC -> entries.filter { it.creature.rarity == Rarity.EPIC }
            CollectionFilter.LEGENDARY -> entries.filter { it.creature.rarity == Rarity.LEGENDARY }
            CollectionFilter.COLLECTED -> entries.filter { it.isCollected }
            CollectionFilter.LOCKED -> entries.filter { !it.isCollected }
        }
    }

    fun applySort(entries: List<RosterEntry>, sort: CollectionSort): List<RosterEntry> {
        return when (sort) {
            CollectionSort.RARITY -> entries.sortedWith(
                compareByDescending<RosterEntry> { it.isCollected }
                    .thenByDescending { it.creature.rarity.tier }
                    .thenBy { catalogOrderIndex(it.creature.id) }
                    .thenBy { it.creature.name },
            )
            CollectionSort.NAME -> entries.sortedWith(
                compareByDescending<RosterEntry> { it.isCollected }
                    .thenBy { it.creature.name },
            )
            CollectionSort.COLLECTED_FIRST -> entries.sortedWith(collectedFirstComparator())
            CollectionSort.CATALOG -> entries.sortedWith(
                compareByDescending<RosterEntry> { it.isCollected }
                    .thenBy { catalogOrderIndex(it.creature.id) },
            )
            CollectionSort.STEPS -> entries.sortedWith(
                compareByDescending<RosterEntry> { it.isCollected }
                    .thenBy { it.creature.totalStepsRequired }
                    .thenBy { it.creature.name },
            )
        }
    }

    fun catalogOrderIndex(creatureId: String): Int {
        val index = CreatureCatalog.all.indexOfFirst { it.id == creatureId }
        return if (index >= 0) index else Int.MAX_VALUE
    }

    private fun collectedFirstComparator(): Comparator<RosterEntry> {
        return compareByDescending<RosterEntry> { it.isCollected }
            .thenBy { catalogOrderIndex(it.creature.id) }
            .thenByDescending { it.creature.rarity.tier }
            .thenBy { it.creature.name }
    }
}

private val Rarity.tier: Int
    get() = when (this) {
        Rarity.COMMON -> 0
        Rarity.UNCOMMON -> 1
        Rarity.RARE -> 2
        Rarity.EPIC -> 3
        Rarity.LEGENDARY -> 4
    }
