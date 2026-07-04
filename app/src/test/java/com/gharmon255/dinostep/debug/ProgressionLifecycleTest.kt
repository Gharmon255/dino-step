package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.health.StepProgression
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureEconomy
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.model.ProgressionThresholds
import com.gharmon255.dinostep.model.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Verifies the growth economy stays internally consistent: thresholds are ordered and sized as
 * designed, stage boundaries are exact, a creature always reaches adult at its total, and the
 * reward-egg roll table maps roll values to the intended rarities.
 */
class ProgressionLifecycleTest {

    private val expectedAdultTotals = mapOf(
        Rarity.COMMON to 40_000,
        Rarity.UNCOMMON to 65_000,
        Rarity.RARE to 100_000,
        Rarity.EPIC to 150_000,
        Rarity.LEGENDARY to 240_000,
    )

    @Test
    fun v2ThresholdsAreOrderedAndSizedForEveryRarity() {
        expectedAdultTotals.forEach { (rarity, expectedTotal) ->
            val t = CreatureEconomy.catalogThresholdsFor(rarity)

            assertEquals("Adult total for $rarity", expectedTotal, t.totalStepsRequired)
            assertEquals(CreatureEconomy.ECONOMY_V2, t.economyVersion)

            assertTrue("hatch < juvenile for $rarity", t.hatchStep < t.juvenileStep)
            assertTrue("juvenile < total for $rarity", t.juvenileStep < t.totalStepsRequired)
            assertTrue("hatch > 0 for $rarity", t.hatchStep > 0)

            // Hatch ~18% of total, juvenile ~45% of total (allow rounding slack).
            assertTrue("hatch ~18% for $rarity", t.hatchStep in (expectedTotal * 17 / 100)..(expectedTotal * 19 / 100))
            assertTrue("juvenile ~45% for $rarity", t.juvenileStep in (expectedTotal * 44 / 100)..(expectedTotal * 46 / 100))
        }
    }

    @Test
    fun stageBoundariesAreExact() {
        val t = ProgressionThresholds(hatchStep = 100, juvenileStep = 200, totalStepsRequired = 300, economyVersion = 2)

        assertEquals(GrowthStage.EGG, t.stageForSteps(0))
        assertEquals(GrowthStage.EGG, t.stageForSteps(99))
        assertEquals(GrowthStage.BABY, t.stageForSteps(100))
        assertEquals(GrowthStage.BABY, t.stageForSteps(199))
        assertEquals(GrowthStage.JUVENILE, t.stageForSteps(200))
        assertEquals(GrowthStage.JUVENILE, t.stageForSteps(299))
        assertEquals(GrowthStage.ADULT, t.stageForSteps(300))
        assertEquals(GrowthStage.ADULT, t.stageForSteps(9_999))
    }

    @Test
    fun everyRaritySpeciesReachesAdultExactlyAtItsTotal() {
        Rarity.entries.forEach { rarity ->
            val species = CreatureCatalog.byRarity(rarity).firstOrNull() ?: return@forEach
            var creature = ActiveCreatureState.newEgg(species, EggRarity.COMMON)
            var stats = PlayerStats()
            val total = creature.progression.totalStepsRequired

            // One step short of the goal must NOT be adult.
            val justShort = StepProgression.applySteps(creature, stats, total - 1, countAsFake = false)
            assertTrue("$rarity should not be adult one step short", !justShort.activeCreature.isAdult)

            // Landing exactly on the total flips to adult.
            val done = StepProgression.applySteps(creature, stats, total, countAsFake = false)
            creature = done.activeCreature
            stats = done.playerStats
            assertTrue("$rarity (${species.id}) should be adult at $total steps", creature.isAdult)
            assertEquals(GrowthStage.ADULT, creature.stage)
            assertEquals(1, stats.eggsHatched)
        }
    }

    @Test
    fun rewardRollTableMapsRollValuesToIntendedRarities() {
        val cases = mapOf(
            0 to EggRarity.COMMON,
            64 to EggRarity.COMMON,
            65 to EggRarity.UNCOMMON,
            86 to EggRarity.UNCOMMON,
            87 to EggRarity.RARE,
            95 to EggRarity.RARE,
            96 to EggRarity.EPIC,
            98 to EggRarity.EPIC,
            99 to EggRarity.LEGENDARY,
        )
        cases.forEach { (roll, expected) ->
            assertEquals("roll=$roll", expected, EggRewardRoller.rollWeighted(roll).eggRarity)
        }
    }

    @Test
    fun rewardRollClampsOutOfRangeInput() {
        assertEquals(EggRarity.COMMON, EggRewardRoller.rollWeighted(-5).eggRarity)
        assertEquals(EggRarity.LEGENDARY, EggRewardRoller.rollWeighted(1_000).eggRarity)
    }

    @Test
    fun rewardDistributionFavorsCommonOverLegendary() {
        val random = Random(1234)
        val counts = mutableMapOf<EggRarity, Int>()
        repeat(10_000) {
            val rarity = EggRewardRoller.rollWeighted(random).eggRarity
            counts[rarity] = (counts[rarity] ?: 0) + 1
        }

        val common = counts[EggRarity.COMMON] ?: 0
        val legendary = counts[EggRarity.LEGENDARY] ?: 0
        assertTrue("Common ($common) should dominate legendary ($legendary)", common > legendary * 10)
    }
}
