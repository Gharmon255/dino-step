package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.ProgressionThresholds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DailyActivityPenaltyTest {
    private val definition = CreatureCatalog.all.first()
    private val creature = ActiveCreatureState(
        creature = definition,
        eggRarity = EggRarity.COMMON,
        progression = ProgressionThresholds(7_200, 18_000, 40_000, 2),
        steps = 20_000,
        isRevealed = true,
    )

    @Test
    fun applyIfNeeded_belowMinimum_resetsToEggWith500Steps() {
        val result = DailyActivityPenalty.applyIfNeeded(
            yesterdaySteps = 4_999,
            activeCreature = creature,
        )

        assertNotNull(result)
        assertEquals(500, result!!.creature.steps)
        assertEquals(false, result.creature.isRevealed)
        assertEquals(GrowthStage.EGG, result.creature.stage)
    }

    @Test
    fun applyIfNeeded_atOrAboveMinimum_noPenalty() {
        assertNull(DailyActivityPenalty.applyIfNeeded(5_000, creature))
        assertNull(DailyActivityPenalty.applyIfNeeded(12_000, creature))
    }

    @Test
    fun applyIfNeeded_alreadyAtMinimumEgg_noPenalty() {
        val egg = creature.copy(steps = 500, isRevealed = false)
        assertNull(DailyActivityPenalty.applyIfNeeded(0, egg))
    }
}
