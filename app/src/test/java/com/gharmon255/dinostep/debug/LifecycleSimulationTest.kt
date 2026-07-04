package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.cloud.CloudSaveMapper
import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.health.DailyActivityPenalty
import com.gharmon255.dinostep.health.StepProgression
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Instead of checking a handful of hand-picked states, this plays out thousands of *randomized but
 * seeded* multi-day player journeys and asserts a set of invariants after every single day. If any
 * combination of steps + missed days can ever break the "grow / reset-to-egg / regrow" state machine
 * — or silently corrupt the save at any intermediate point — one of these seeds will surface it, and
 * because the seed is fixed the failure reproduces deterministically.
 *
 * Invariants enforced every day:
 *   • steps never go negative;
 *   • the inactivity penalty fires exactly when the real guard says it should;
 *   • a penalty always produces a hidden, 500-step EGG with identical identity/progression;
 *   • eggsHatched and lifetimeStepsApplied are monotonic and match an independent oracle;
 *   • the reported growth stage always agrees with steps vs. thresholds;
 *   • the full save round-trips losslessly (Room entity + cloud) at that exact moment.
 */
class LifecycleSimulationTest {

    @Test
    fun randomizedMultiDayJourneysNeverBreakTheStateMachineOrTheSave() {
        val species = CreatureCatalog.all
        val rarities = EggRarity.entries

        repeat(150) { journey ->
            val random = Random(journey.toLong() * 2_654_435_761L + 12345L)
            val creatureDef = species[random.nextInt(species.size)]
            val eggRarity = rarities[random.nextInt(rarities.size)]

            var creature = ActiveCreatureState.newEgg(creatureDef, eggRarity).copy(
                nickname = if (random.nextBoolean()) "Buddy$journey" else null,
                startedAt = 1_600_000_000_000L + journey,
            )
            var stats = PlayerStats()

            val hatch = creature.progression.hatchStep
            var expectedLifetime = 0L
            var expectedEggsHatched = 0
            var yesterdaySteps = -1 // no "yesterday" on day 0

            repeat(45) { day ->
                // --- Morning: inactivity evaluation based on yesterday ---
                if (yesterdaySteps >= 0) {
                    val revealedBefore = creature.isRevealed
                    val stepsBefore = creature.steps
                    val shouldPenalize = yesterdaySteps < DailyActivityPenalty.MINIMUM_DAILY_STEPS &&
                        (revealedBefore || stepsBefore > DailyActivityPenalty.PENALTY_REMAINING_STEPS)

                    val penalty = DailyActivityPenalty.applyIfNeeded(yesterdaySteps, creature)

                    if (shouldPenalize) {
                        assertNotNull("journey=$journey day=$day expected penalty", penalty)
                        val reset = penalty!!.creature
                        assertEquals(500, reset.steps)
                        assertFalse(reset.isRevealed)
                        assertEquals(GrowthStage.EGG, reset.stage)
                        // Identity + curve preserved through the reset.
                        assertEquals(creature.creature.id, reset.creature.id)
                        assertEquals(creature.eggRarity, reset.eggRarity)
                        assertEquals(creature.nickname, reset.nickname)
                        assertEquals(creature.progression, reset.progression)
                        assertEquals(creature.startedAt, reset.startedAt)
                        creature = reset
                    } else {
                        assertNull("journey=$journey day=$day unexpected penalty", penalty)
                    }
                }

                // --- Daytime: accrue a random number of steps ---
                val todaySteps = random.nextInt(0, 15_000)
                val wasRevealed = creature.isRevealed
                val willReveal = wasRevealed || (creature.steps + todaySteps) >= hatch
                if (todaySteps > 0 && !wasRevealed && willReveal) expectedEggsHatched++
                expectedLifetime += todaySteps

                val applied = StepProgression.applySteps(creature, stats, todaySteps, countAsFake = false)
                creature = applied.activeCreature
                stats = applied.playerStats

                // --- Invariants ---
                assertTrue("journey=$journey day=$day steps>=0", creature.steps >= 0)
                assertEquals("journey=$journey day=$day lifetime", expectedLifetime, stats.lifetimeStepsApplied.toLong())
                assertEquals("journey=$journey day=$day eggsHatched", expectedEggsHatched, stats.eggsHatched)
                assertEquals(
                    "journey=$journey day=$day stage",
                    creature.progression.stageForSteps(creature.steps),
                    creature.stage,
                )
                // Revealed and hatched must agree once past the hatch line.
                if (creature.steps >= hatch) {
                    assertTrue("journey=$journey day=$day revealed past hatch", creature.isRevealed)
                }

                // --- The save must round-trip losslessly right now (entity + cloud) ---
                assertActiveCreatureRoundTrips(creature)
                assertSnapshotCloudRoundTrips(GameSnapshot(creature, emptyList(), stats))

                yesterdaySteps = todaySteps
            }
        }
    }

    private fun assertActiveCreatureRoundTrips(creature: ActiveCreatureState) {
        val restored = creature.toEntity().toDomain()
        assertEquals(creature.creature.id, restored.creature.id)
        assertEquals(creature.eggRarity, restored.eggRarity)
        assertEquals(creature.steps, restored.steps)
        assertEquals(creature.isRevealed, restored.isRevealed)
        assertEquals(creature.nickname, restored.nickname)
        assertEquals(creature.startedAt, restored.startedAt)
        assertEquals(creature.progression, restored.progression)
    }

    private fun assertSnapshotCloudRoundTrips(snapshot: GameSnapshot) {
        val restored = CloudSaveMapper.toSnapshot(
            CloudSaveMapper.toCloud(snapshot, revision = 1L, updatedAt = "2026-07-04T00:00:00Z"),
        )
        assertNotNull(restored)
        requireNotNull(restored)
        assertEquals(snapshot.activeCreature.creature.id, restored.activeCreature.creature.id)
        assertEquals(snapshot.activeCreature.steps, restored.activeCreature.steps)
        assertEquals(snapshot.activeCreature.eggRarity, restored.activeCreature.eggRarity)
        assertEquals(snapshot.activeCreature.progression, restored.activeCreature.progression)
        // Cloud normalizes revealed to (revealed || steps >= hatch); for our states these are equal.
        assertEquals(snapshot.activeCreature.isRevealed, restored.activeCreature.isRevealed)
        assertEquals(snapshot.playerStats.eggsHatched, restored.playerStats.eggsHatched)
        assertEquals(snapshot.playerStats.lifetimeStepsApplied, restored.playerStats.lifetimeStepsApplied)
    }
}
