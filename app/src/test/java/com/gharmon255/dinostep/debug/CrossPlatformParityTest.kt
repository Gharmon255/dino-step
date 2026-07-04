package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.health.DailyActivityPenalty
import com.gharmon255.dinostep.model.CreatureEconomy
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.promo.PromoCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Dino Step ships as two independent codebases (Android + iOS) that MUST behave identically. These
 * are the shared "rules of the game" numbers. The iOS project has a mirror of this file
 * (`CrossPlatformParityTests.swift`) asserting the same values — if you tune a number on one
 * platform, this test fails until you match it on the other, preventing silent cross-platform drift.
 *
 * ⚠️  Changing a value here is a game-balance/behavior change: update iOS to match and re-test both.
 */
class CrossPlatformParityTest {

    @Test
    fun inactivityPenaltyConstantsAreCanonical() {
        assertEquals(5_000, DailyActivityPenalty.MINIMUM_DAILY_STEPS)
        assertEquals(500, DailyActivityPenalty.PENALTY_REMAINING_STEPS)
    }

    @Test
    fun economyIsV2WithCanonicalAdultTotals() {
        assertEquals(2, CreatureEconomy.CURRENT_ECONOMY)
        assertEquals(40_000, CreatureEconomy.catalogThresholdsFor(Rarity.COMMON).totalStepsRequired)
        assertEquals(65_000, CreatureEconomy.catalogThresholdsFor(Rarity.UNCOMMON).totalStepsRequired)
        assertEquals(100_000, CreatureEconomy.catalogThresholdsFor(Rarity.RARE).totalStepsRequired)
        assertEquals(150_000, CreatureEconomy.catalogThresholdsFor(Rarity.EPIC).totalStepsRequired)
        assertEquals(240_000, CreatureEconomy.catalogThresholdsFor(Rarity.LEGENDARY).totalStepsRequired)
    }

    @Test
    fun rewardRollBoundariesAreCanonical() {
        // Weighted table: common 65, uncommon 22, rare 9, epic 3, legendary 1 (sums to 100).
        assertEquals(EggRarity.COMMON, EggRewardRoller.rollWeighted(64).eggRarity)
        assertEquals(EggRarity.UNCOMMON, EggRewardRoller.rollWeighted(65).eggRarity)
        assertEquals(EggRarity.UNCOMMON, EggRewardRoller.rollWeighted(86).eggRarity)
        assertEquals(EggRarity.RARE, EggRewardRoller.rollWeighted(87).eggRarity)
        assertEquals(EggRarity.RARE, EggRewardRoller.rollWeighted(95).eggRarity)
        assertEquals(EggRarity.EPIC, EggRewardRoller.rollWeighted(96).eggRarity)
        assertEquals(EggRarity.EPIC, EggRewardRoller.rollWeighted(98).eggRarity)
        assertEquals(EggRarity.LEGENDARY, EggRewardRoller.rollWeighted(99).eggRarity)
    }

    @Test
    fun promoCatalogIsCanonical() {
        assertEquals(setOf("epic20", "legend20"), PromoCatalog.knownCodes())
        assertEquals(EggRarity.EPIC, PromoCatalog.rewardFor("epic20"))
        assertEquals(EggRarity.LEGENDARY, PromoCatalog.rewardFor("legend20"))
    }
}
