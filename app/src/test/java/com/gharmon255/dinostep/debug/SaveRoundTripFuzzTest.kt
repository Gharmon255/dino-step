package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.cloud.CloudSaveMapper
import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureEconomy
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.promo.PromoCatalog
import com.gharmon255.dinostep.promo.PromoRedemptionCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.random.Random

/**
 * Fuzzes the save layer with thousands of *randomly generated but valid* game states and asserts
 * they survive both serialization paths a real save crosses:
 *   • Room entity round trip (`toEntity().toDomain()`) — the exact on-disk form between app versions;
 *   • cloud round trip (`toCloud().toSnapshot()`) — the device-swap / reinstall path.
 *
 * The point is future-proofing: if anyone adds a field to a model and forgets to map it, a random
 * value in that field will differ after the round trip and fail here — without anyone having to
 * remember to write a bespoke assertion for it.
 */
class SaveRoundTripFuzzTest {

    private val species = CreatureCatalog.all
    private val rarities = EggRarity.entries
    private val promoCodes = PromoCatalog.knownCodes().toList()

    @Test
    fun randomActiveCreaturesSurviveTheRoomEntityRoundTripExactly() {
        val random = Random(0xA11CE)
        repeat(1_000) {
            val creature = randomActiveCreature(random)
            // ActiveCreatureState is a data class; a full equality check catches ANY dropped field.
            assertEquals("iteration state=$creature", creature, creature.toEntity().toDomain())
        }
    }

    @Test
    fun randomCompletedCreaturesSurviveTheRoomEntityRoundTripExactly() {
        val random = Random(0xB0B)
        repeat(1_000) {
            val completed = randomCompletedCreature(random)
            assertEquals(completed, completed.toEntity().toDomain())
        }
    }

    @Test
    fun randomPlayerStatsSurviveTheRoomEntityRoundTripExactly() {
        val random = Random(0xC0FFEE)
        repeat(1_000) {
            val stats = randomPlayerStats(random)
            assertEquals(stats, stats.toEntity().toDomain())
        }
    }

    @Test
    fun randomFullSnapshotsSurviveTheCloudRoundTrip() {
        val random = Random(0xD00D)
        repeat(1_000) {
            val snapshot = GameSnapshot(
                activeCreature = randomActiveCreature(random),
                collection = List(random.nextInt(0, 6)) { randomCompletedCreature(random) },
                playerStats = randomPlayerStats(random),
            )

            val restored = CloudSaveMapper.toSnapshot(
                CloudSaveMapper.toCloud(snapshot, revision = random.nextLong(0, 1_000), updatedAt = "2026-07-04T00:00:00Z"),
            )
            assertNotNull(restored)
            requireNotNull(restored)

            val a = snapshot.activeCreature
            val ra = restored.activeCreature
            assertEquals(a.creature.id, ra.creature.id)
            assertEquals(a.eggRarity, ra.eggRarity)
            assertEquals(a.steps, ra.steps)
            assertEquals(a.startedAt, ra.startedAt)
            assertEquals(a.nickname, ra.nickname)
            assertEquals(a.progression, ra.progression)
            // Cloud normalizes reveal state on the way back.
            assertEquals(a.isRevealed || a.steps >= a.progression.hatchStep, ra.isRevealed)

            assertEquals(snapshot.collection.size, restored.collection.size)
            snapshot.collection.zip(restored.collection).forEach { (orig, back) ->
                assertEquals(orig.id, back.id)
                assertEquals(orig.creature.id, back.creature.id)
                assertEquals(orig.stepsCompleted, back.stepsCompleted)
                assertEquals(orig.completedAt, back.completedAt)
                assertEquals(orig.nickname, back.nickname)
                assertEquals(orig.eggRarityAtHatch, back.eggRarityAtHatch)
                assertEquals(orig.exSteps, back.exSteps)
                assertEquals(orig.exLevel, back.exLevel)
            }

            val s = snapshot.playerStats
            val rs = restored.playerStats
            assertEquals(s.eggsHatched, rs.eggsHatched)
            assertEquals(s.creaturesCompleted, rs.creaturesCompleted)
            assertEquals(s.lastSyncedStepTotal, rs.lastSyncedStepTotal)
            assertEquals(s.lastSyncDayStartMillis, rs.lastSyncDayStartMillis)
            assertEquals(s.lifetimeStepsApplied, rs.lifetimeStepsApplied)
            assertEquals(s.pendingRewardEggRarity, rs.pendingRewardEggRarity)
            assertEquals(s.redeemedPromoCodes, rs.redeemedPromoCodes)
            // Documented drop: the debug-only fake-step counter is not part of the cloud schema.
            assertEquals(0, rs.totalFakeStepsAdded)
        }
    }

    private fun randomActiveCreature(random: Random): ActiveCreatureState {
        val def = species[random.nextInt(species.size)]
        val progression = CreatureEconomy.thresholdsFor(def)
        return ActiveCreatureState(
            creature = def,
            eggRarity = rarities[random.nextInt(rarities.size)],
            progression = progression,
            steps = random.nextInt(0, progression.totalStepsRequired + 5_000),
            startedAt = random.nextLong(1, 2_000_000_000_000L),
            isRevealed = random.nextBoolean(),
            nickname = if (random.nextBoolean()) "Nick${random.nextInt(1000)}" else null,
        )
    }

    private fun randomCompletedCreature(random: Random): CompletedCreature {
        val def = species[random.nextInt(species.size)]
        return CompletedCreature(
            id = random.nextLong(1, 5_000_000L),
            creature = def,
            stepsCompleted = random.nextInt(0, 300_000),
            completedAt = random.nextLong(1, 2_000_000_000_000L),
            nickname = if (random.nextBoolean()) "C${random.nextInt(1000)}" else null,
            eggRarityAtHatch = rarities[random.nextInt(rarities.size)],
            exSteps = random.nextInt(0, 5_000),
            exLevel = random.nextInt(1, 50),
        )
    }

    private fun randomPlayerStats(random: Random): PlayerStats {
        val redeemed = promoCodes.filter { random.nextBoolean() }.toSet()
        return PlayerStats(
            totalFakeStepsAdded = random.nextInt(0, 500_000),
            eggsHatched = random.nextInt(0, 500),
            creaturesCompleted = random.nextInt(0, 200),
            lastSyncedStepTotal = random.nextInt(0, 100_000),
            lastSyncDayStartMillis = random.nextLong(0, 2_000_000_000_000L),
            lifetimeStepsApplied = random.nextInt(0, 5_000_000),
            pendingRewardEggRarity = if (random.nextBoolean()) rarities[random.nextInt(rarities.size)].name else null,
            redeemedPromoCodes = PromoRedemptionCodec.encode(redeemed),
        )
    }
}
