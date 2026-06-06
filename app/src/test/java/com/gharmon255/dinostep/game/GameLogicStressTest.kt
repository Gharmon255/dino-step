package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class GameLogicStressTest {
    @Test
    fun allCatalogSpecies_stageTransitionsUnderRapidSteps() {
        CreatureCatalog.all.forEach { creature ->
            var steps = 0
            val seenStages = mutableSetOf<GrowthStage>()
            while (steps <= creature.totalStepsRequired + 1_000) {
                val stage = creature.stageForSteps(steps)
                seenStages += stage
                val progress = creature.progressPercent(steps)
                assertTrue("progress out of range for ${creature.id} at $steps", progress in 0f..100f)
                steps += 137
            }
            assertTrue(seenStages.contains(GrowthStage.EGG))
            assertTrue(seenStages.contains(GrowthStage.ADULT))
        }
    }

    @Test
    fun activeCreatureState_normalizesRevealOnHatch() {
        val creature = CreatureCatalog.tRex
        val state = ActiveCreatureState(
            creature = creature,
            eggRarity = EggRarity.COMMON,
            steps = creature.hatchStep,
            isRevealed = false,
        )
        val normalized = state.normalized()
        assertTrue(normalized.isRevealed)
        assertEquals(creature.name, normalized.displayName)
    }

    @Test
    fun eggRewardRoller_manyRolls_onlyValidRarities() {
        val allowed = EggRarity.entries.toSet()
        repeat(20_000) { iteration ->
            val roll = com.gharmon255.dinostep.model.EggRewardRoller.rollWeighted(iteration % 100)
            assertTrue(roll.eggRarity in allowed)
            assertTrue(roll.rollValue in 0..99)
        }
    }
}
