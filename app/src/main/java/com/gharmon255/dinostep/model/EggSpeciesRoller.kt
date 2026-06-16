package com.gharmon255.dinostep.model

import kotlin.random.Random

object EggSpeciesRoller {
    fun rollSpecies(
        eggRarity: EggRarity,
        excludeSpeciesIds: Set<String> = emptySet(),
        collectedSpeciesIds: Set<String> = emptySet(),
        random: Random = Random.Default,
    ): CreatureDefinition {
        val pool = CreatureCatalog.creaturesForEgg(eggRarity)
        if (pool.isEmpty()) {
            return CreatureCatalog.randomCreatureForEgg(EggRarity.COMMON)
        }

        val withoutExcluded = pool.filter { it.id !in excludeSpeciesIds }
        val undiscovered = withoutExcluded.filter { it.id !in collectedSpeciesIds }
        val preferred = undiscovered.ifEmpty { withoutExcluded }
        val finalPool = preferred.ifEmpty { pool }
        return finalPool[random.nextInt(finalPool.size)]
    }
}
