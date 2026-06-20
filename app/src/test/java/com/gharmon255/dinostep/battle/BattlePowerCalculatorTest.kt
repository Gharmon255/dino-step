package com.gharmon255.dinostep.battle

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattlePowerCalculatorTest {
    @Test
    fun packBonus_increasesPowerForDuplicates() {
        val raptor = CompletedCreature(
            creature = CreatureCatalog.byId("tiny_raptor")!!,
            stepsCompleted = 8000,
            completedAt = 1L,
            eggRarityAtHatch = EggRarity.COMMON,
            exLevel = 10,
            exSteps = 1000,
        )
        val collectionSingle = listOf(raptor)
        val collectionDouble = listOf(raptor, raptor.copy(id = 2L))

        val singlePower = BattlePowerCalculator.compute(raptor, collectionSingle).combatPower
        val doublePower = BattlePowerCalculator.compute(raptor, collectionDouble).combatPower

        assertTrue(doublePower > singlePower)
    }

    @Test
    fun higherRaritySpecies_hasHigherBasePower() {
        val raptor = CompletedCreature(
            creature = CreatureCatalog.byId("tiny_raptor")!!,
            stepsCompleted = 8000,
            completedAt = 1L,
        )
        val trex = CompletedCreature(
            creature = CreatureCatalog.byId("trex")!!,
            stepsCompleted = 50000,
            completedAt = 2L,
        )
        val collection = listOf(raptor, trex)

        val raptorPower = BattlePowerCalculator.compute(raptor, collection).combatPower
        val trexPower = BattlePowerCalculator.compute(trex, collection).combatPower

        assertTrue(trexPower > raptorPower)
    }
}
