package com.gharmon255.dinostep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EggSpeciesRollerTest {
    @Test
    fun rollSpecies_excludesJustCompletedSpecies() {
        val species = EggSpeciesRoller.rollSpecies(
            eggRarity = EggRarity.COMMON,
            excludeSpeciesIds = setOf("tiny_raptor"),
            collectedSpeciesIds = emptySet(),
            random = kotlin.random.Random(0),
        )
        assertNotEquals("tiny_raptor", species.id)
    }

    @Test
    fun rollSpecies_prefersUndiscovered() {
        val allCommonIds = CreatureCatalog.commonCreatures.map { it.id }.toSet()
        val collected = allCommonIds - "gallimimus"
        val species = EggSpeciesRoller.rollSpecies(
            eggRarity = EggRarity.COMMON,
            excludeSpeciesIds = setOf("tiny_raptor"),
            collectedSpeciesIds = collected,
            random = kotlin.random.Random(1),
        )
        assertEquals("gallimimus", species.id)
    }
}
