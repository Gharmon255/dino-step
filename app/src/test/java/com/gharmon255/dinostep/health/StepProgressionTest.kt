package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import org.junit.Assert.assertEquals
import org.junit.Test

class StepProgressionTest {
    @Test
    fun applyStepsIncrementsCreatureAndHatchCount() {
        val creature = CreatureCatalog.randomCreatureForEgg(EggRarity.COMMON)
        val activeCreature = ActiveCreatureState(
            creature = creature,
            eggRarity = EggRarity.COMMON,
            steps = creature.hatchStep - 100,
            isRevealed = false,
        )
        val playerStats = PlayerStats()

        val result = StepProgression.applySteps(
            activeCreature = activeCreature,
            playerStats = playerStats,
            amount = 150,
            countAsFake = false,
        )

        assertEquals(creature.hatchStep + 50, result.activeCreature.steps)
        assertEquals(true, result.activeCreature.isRevealed)
        assertEquals(1, result.playerStats.eggsHatched)
        assertEquals(150, result.playerStats.lifetimeStepsApplied)
    }
}
