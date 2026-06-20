package com.gharmon255.dinostep.game

import org.junit.Assert.assertEquals
import org.junit.Test

class ExProgressionTest {
    @Test
    fun dripAmount_isFivePercentOfSteps() {
        assertEquals(50, ExProgression.dripAmount(1000))
        assertEquals(0, ExProgression.dripAmount(10))
    }

    @Test
    fun exLevel_startsAtOne() {
        assertEquals(1, ExProgression.exLevelFromSteps(0))
    }

    @Test
    fun applyDrip_updatesAllCreatures() {
        val creature = ExProgression.newCompletedCreature(
            creature = com.gharmon255.dinostep.model.CreatureCatalog.byId("tiny_raptor")!!,
            stepsCompleted = 100,
            completedAt = 1L,
            eggRarityAtHatch = com.gharmon255.dinostep.model.EggRarity.COMMON,
        )
        val updated = ExProgression.applyDrip(listOf(creature), 1000)
        assertEquals(50, updated.single().exSteps)
    }
}
