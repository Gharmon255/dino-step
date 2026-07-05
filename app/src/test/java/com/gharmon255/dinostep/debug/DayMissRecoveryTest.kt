package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.health.DailyActivityPenalty
import com.gharmon255.dinostep.health.DayRolloverEvaluator
import com.gharmon255.dinostep.health.StepProgression
import com.gharmon255.dinostep.health.StepTimeUtils
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Miss a day → the dino drops back to an egg → then grows forward correctly."
 *
 * This drives the real, deterministic domain logic end to end:
 *   grow to adult  ->  miss a day (inactivity penalty)  ->  reset to a 500-step egg  ->  regrow.
 *
 * The penalty must reset *progress* while preserving *identity* (same species, rarity, nickname,
 * progression curve, hatch date). After the reset the creature has to behave exactly like a fresh
 * egg: hidden until it re-hatches at [hatchStep], then advancing through baby/juvenile/adult again.
 */
class DayMissRecoveryTest {

    private val raptor = CreatureCatalog.byId("tiny_raptor")!!

    private fun grow(
        creature: ActiveCreatureState,
        stats: PlayerStats,
        amount: Int,
    ): StepProgression.ApplyResult =
        StepProgression.applySteps(
            activeCreature = creature,
            playerStats = stats,
            amount = amount,
            countAsFake = false,
        )

    @Test
    fun fullLifecycle_growToAdult_missADay_resetToEgg_thenRegrowToAdult() {
        var creature = ActiveCreatureState.newEgg(raptor, EggRarity.COMMON).copy(
            nickname = "Pip",
            startedAt = 1_700_000_000_000L,
        )
        var stats = PlayerStats()
        val hatch = creature.progression.hatchStep
        val juvenile = creature.progression.juvenileStep
        val total = creature.progression.totalStepsRequired

        // --- Grow all the way to adult, verifying each stage boundary ---
        assertEquals(GrowthStage.EGG, creature.stage)
        assertFalse(creature.isRevealed)

        grow(creature, stats, hatch).let { creature = it.activeCreature; stats = it.playerStats }
        assertEquals("Hatching happens at the hatch threshold", GrowthStage.BABY, creature.stage)
        assertTrue(creature.isRevealed)
        assertEquals("Hatch counter increments exactly once", 1, stats.eggsHatched)

        grow(creature, stats, juvenile - creature.steps).let { creature = it.activeCreature; stats = it.playerStats }
        assertEquals(GrowthStage.JUVENILE, creature.stage)

        grow(creature, stats, total - creature.steps).let { creature = it.activeCreature; stats = it.playerStats }
        assertEquals(GrowthStage.ADULT, creature.stage)
        assertTrue(creature.isAdult)
        assertEquals("Re-crossing hatch while revealed does not re-count", 1, stats.eggsHatched)
        val lifetimeAtAdult = stats.lifetimeStepsApplied
        assertEquals(total, lifetimeAtAdult)

        // --- Miss a day (yesterday < 5,000 steps) ---
        val penalty = DailyActivityPenalty.applyIfNeeded(
            yesterdaySteps = 1_200,
            activeCreature = creature,
        )
        assertNotNull("An adult must be penalized for an inactive day", penalty)
        val afterPenalty = penalty!!.creature

        // Progress is wiped back to the egg…
        assertEquals(DailyActivityPenalty.PENALTY_REMAINING_STEPS, afterPenalty.steps)
        assertEquals(500, afterPenalty.steps)
        assertFalse(afterPenalty.isRevealed)
        assertEquals(GrowthStage.EGG, afterPenalty.stage)
        // …but identity is fully preserved.
        assertEquals(raptor.id, afterPenalty.creature.id)
        assertEquals(EggRarity.COMMON, afterPenalty.eggRarity)
        assertEquals("Pip", afterPenalty.nickname)
        assertEquals(creature.progression, afterPenalty.progression)
        assertEquals(1_700_000_000_000L, afterPenalty.startedAt)
        // The penalty is not a step application, so lifetime steps are unaffected.
        assertEquals(lifetimeAtAdult, stats.lifetimeStepsApplied)

        // --- Regrow: it must behave like a brand new egg again ---
        creature = afterPenalty
        grow(creature, stats, hatch - creature.steps).let { creature = it.activeCreature; stats = it.playerStats }
        assertEquals(GrowthStage.BABY, creature.stage)
        assertTrue(creature.isRevealed)
        assertEquals("Re-hatching after a reset counts again", 2, stats.eggsHatched)

        grow(creature, stats, total - creature.steps).let { creature = it.activeCreature; stats = it.playerStats }
        assertEquals(GrowthStage.ADULT, creature.stage)
        assertTrue(creature.isAdult)
    }

    @Test
    fun exactlyMinimumStepsAvoidsPenalty_butOneStepShortTriggersIt() {
        val adult = ActiveCreatureState.newEgg(raptor, EggRarity.COMMON).copy(steps = 40_000, isRevealed = true)

        assertNull(
            "Meeting the 5,000 step goal must never reset the dino",
            DailyActivityPenalty.applyIfNeeded(DailyActivityPenalty.MINIMUM_DAILY_STEPS, adult),
        )
        assertNotNull(
            "Falling one step short triggers the reset",
            DailyActivityPenalty.applyIfNeeded(DailyActivityPenalty.MINIMUM_DAILY_STEPS - 1, adult),
        )
    }

    @Test
    fun penaltyIsIdempotent_freshEggIsNotStompedAgain() {
        val freshEgg = ActiveCreatureState.newEgg(raptor, EggRarity.COMMON).copy(steps = 500, isRevealed = false)
        // Already reset yesterday, still inactive today: must not "double reset" or hide anything new.
        assertNull(DailyActivityPenalty.applyIfNeeded(0, freshEgg))
    }

    @Test
    fun unhatchedEggWithHeadwayIsStillResetToFloor() {
        // An egg that has crept above the 500-step floor (but hasn't hatched) still loses that headway.
        val egg = ActiveCreatureState.newEgg(raptor, EggRarity.COMMON).copy(steps = 3_000, isRevealed = false)
        val result = DailyActivityPenalty.applyIfNeeded(0, egg)

        assertNotNull(result)
        assertEquals(500, result!!.creature.steps)
        assertEquals(GrowthStage.EGG, result.creature.stage)
    }

    @Test
    fun midStageJuvenileResetsAllTheWayToEgg() {
        val juvenile = ActiveCreatureState.newEgg(raptor, EggRarity.RARE).copy(
            steps = 20_000,
            isRevealed = true,
            nickname = "Blue",
        )
        val result = DailyActivityPenalty.applyIfNeeded(2_500, juvenile)

        assertNotNull(result)
        assertEquals(GrowthStage.EGG, result!!.creature.stage)
        assertEquals(EggRarity.RARE, result.creature.eggRarity)
        assertEquals("Blue", result.creature.nickname)
    }

    @Test
    fun yesterdayStepResolution_usesHigherOfFreshReadAndCachedPartialSync() = runBlocking {
        val yesterdayStart = StepTimeUtils.startOfYesterdayMillis()
        val stats = PlayerStats(lastSyncedStepTotal = 3_000, lastSyncDayStartMillis = yesterdayStart)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { 6_500 },
        )

        assertEquals(6_500, resolved)
    }

    @Test
    fun yesterdayStepResolution_fallsBackToCacheWhenHealthReadFails() = runBlocking {
        val yesterdayStart = StepTimeUtils.startOfYesterdayMillis()
        val stats = PlayerStats(lastSyncedStepTotal = 3_000, lastSyncDayStartMillis = yesterdayStart)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { null },
        )

        assertEquals(3_000, resolved)
    }

    @Test
    fun yesterdayStepResolution_returnsNullWhenNoSourceAvailable() = runBlocking {
        val stats = PlayerStats(lastSyncedStepTotal = 3_000, lastSyncDayStartMillis = 0L)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { null },
        )

        assertNull(resolved)
    }

    @Test
    fun yesterdayStepResolution_fallsBackToFetchWhenCacheIsStale() = runBlocking {
        val stats = PlayerStats(lastSyncedStepTotal = 3_000, lastSyncDayStartMillis = 0L)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { 4_200 },
        )

        assertEquals(4_200, resolved)
    }
}
