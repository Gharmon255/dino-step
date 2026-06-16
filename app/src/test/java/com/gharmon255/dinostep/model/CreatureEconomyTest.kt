package com.gharmon255.dinostep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CreatureEconomyTest {
    @Test
    fun v2_commonThresholds_matchDesign() {
        val thresholds = CreatureEconomy.catalogThresholdsFor(Rarity.COMMON)
        assertEquals(40_000, thresholds.totalStepsRequired)
        assertEquals(7_200, thresholds.hatchStep)
        assertEquals(18_000, thresholds.juvenileStep)
        assertEquals(CreatureEconomy.ECONOMY_V2, thresholds.economyVersion)
    }

    @Test
    fun v2_legendaryThresholds_matchDesign() {
        val thresholds = CreatureEconomy.thresholdsForRarity(Rarity.LEGENDARY, CreatureEconomy.ECONOMY_V2)
        assertEquals(240_000, thresholds.totalStepsRequired)
        assertEquals(43_200, thresholds.hatchStep)
        assertEquals(108_000, thresholds.juvenileStep)
    }

    @Test
    fun legacyV1_preservesTinyRaptorCurve() {
        val legacy = CreatureEconomy.legacyV1Thresholds("tiny_raptor")
        assertEquals(8_000, legacy.totalStepsRequired)
        assertEquals(1_600, legacy.hatchStep)
        assertEquals(CreatureEconomy.ECONOMY_V1, legacy.economyVersion)
    }

    @Test
    fun catalogUsesV2PerRarityBand() {
        CreatureCatalog.commonCreatures.forEach { creature ->
            assertEquals(40_000, creature.totalStepsRequired)
        }
        CreatureCatalog.legendaryCreatures.forEach { creature ->
            assertEquals(240_000, creature.totalStepsRequired)
        }
    }
}
