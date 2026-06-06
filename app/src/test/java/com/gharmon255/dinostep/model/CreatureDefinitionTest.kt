package com.gharmon255.dinostep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreatureDefinitionTest {
    private val trex = CreatureDefinition(
        id = "trex",
        name = "T-Rex",
        rarity = Rarity.RARE,
        habitat = Habitat.VOLCANO,
        totalStepsRequired = 50_000,
        hatchStep = 10_000,
        juvenileStep = 25_000,
        eggAssetKey = "egg",
        babyAssetKey = "baby",
        juvenileAssetKey = "juvenile",
        adultAssetKey = "adult",
    )

    @Test
    fun stageForSteps_eggBabyJuvenileAdult() {
        assertEquals(GrowthStage.EGG, trex.stageForSteps(0))
        assertEquals(GrowthStage.EGG, trex.stageForSteps(9_999))
        assertEquals(GrowthStage.BABY, trex.stageForSteps(10_000))
        assertEquals(GrowthStage.JUVENILE, trex.stageForSteps(25_000))
        assertEquals(GrowthStage.ADULT, trex.stageForSteps(50_000))
    }

    @Test
    fun nextMilestone_perStage() {
        assertEquals(10_000, trex.nextMilestone(0))
        assertEquals(25_000, trex.nextMilestone(10_000))
        assertEquals(50_000, trex.nextMilestone(25_000))
        assertNull(trex.nextMilestone(50_000))
    }

    @Test
    fun progressPercent_withinCurrentStageOnly() {
        assertEquals(50f, trex.progressPercent(5_000), 0.01f)
        assertEquals(0f, trex.progressPercent(10_000), 0.01f)
        assertEquals(50f, trex.progressPercent(17_500), 0.01f)
        assertEquals(100f, trex.progressPercent(50_000), 0.01f)
    }
}
