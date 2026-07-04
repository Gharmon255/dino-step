package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.cloud.CloudActiveCreature
import com.gharmon255.dinostep.cloud.CloudCompletedCreature
import com.gharmon255.dinostep.cloud.CloudGameSave
import com.gharmon255.dinostep.cloud.CloudPlayerStats
import com.gharmon255.dinostep.cloud.CloudSaveMapper
import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.local.entity.ActiveCreatureEntity
import com.gharmon255.dinostep.data.local.entity.CompletedCreatureEntity
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.data.withLegacyV1SnapshotIfMissing
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureEconomy
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * "Nothing is lost when a player downloads a new version."
 *
 * A store update re-installs the binary but keeps the app's data directory, so the game reloads
 * from the same Room tables (and, for signed-in players, the same cloud row). These tests exercise
 * the exact serialization boundaries that data crosses on relaunch:
 *
 *   1. Room entity <-> domain mappers (what actually sits on disk between versions).
 *   2. The legacy back-fill that upgrades pre-economy saves in place.
 *   3. The cloud save round-trip, including the fields that are *intentionally* not synced.
 *
 * If a future refactor drops a field from any mapper, one of these fails before it can ship.
 */
class AppUpdateSaveIntegrityTest {

    // region Room on-disk round trips (survives an app update because the DB file persists)

    @Test
    fun playerStatsSurvivesEntityRoundTripWithEveryFieldPopulated() {
        val stats = PlayerStats(
            totalFakeStepsAdded = 4_321,
            eggsHatched = 12,
            creaturesCompleted = 7,
            lastSyncedStepTotal = 8_800,
            lastSyncDayStartMillis = 1_720_000_000_000L,
            lifetimeStepsApplied = 987_654,
            pendingRewardEggRarity = EggRarity.EPIC.name,
            redeemedPromoCodes = "epic20,legend20",
        )

        val restored = stats.toEntity().toDomain()

        assertEquals(stats, restored)
    }

    @Test
    fun activeCreatureSurvivesEntityRoundTrip() {
        val creature = CreatureCatalog.byId("trex")!!
        val active = ActiveCreatureState.newEgg(creature, EggRarity.LEGENDARY).copy(
            steps = 33_333,
            isRevealed = true,
            nickname = "Chomper",
            startedAt = 1_700_000_000_000L,
        )

        val restored = active.toEntity().toDomain()

        assertEquals(creature.id, restored.creature.id)
        assertEquals(EggRarity.LEGENDARY, restored.eggRarity)
        assertEquals(33_333, restored.steps)
        assertTrue(restored.isRevealed)
        assertEquals("Chomper", restored.nickname)
        assertEquals(1_700_000_000_000L, restored.startedAt)
        assertEquals(active.progression, restored.progression)
    }

    @Test
    fun completedCreatureSurvivesEntityRoundTrip() {
        val completed = CompletedCreature(
            id = 42L,
            creature = CreatureCatalog.byId("spinosaurus")!!,
            stepsCompleted = 60_000,
            completedAt = 1_711_111_111_111L,
            nickname = "Spiny",
            eggRarityAtHatch = EggRarity.EPIC,
            exSteps = 250,
            exLevel = 4,
        )

        val restored = completed.toEntity().toDomain()

        assertEquals(completed.id, restored.id)
        assertEquals(completed.creature.id, restored.creature.id)
        assertEquals(completed.stepsCompleted, restored.stepsCompleted)
        assertEquals(completed.completedAt, restored.completedAt)
        assertEquals(completed.nickname, restored.nickname)
        assertEquals(completed.eggRarityAtHatch, restored.eggRarityAtHatch)
        assertEquals(completed.exSteps, restored.exSteps)
        assertEquals(completed.exLevel, restored.exLevel)
    }

    // endregion

    // region Legacy save upgrade (a save written by an old build, opened by a new build)

    @Test
    fun preEconomySaveIsBackfilledWithLegacyThresholdsWithoutLosingProgress() {
        // Simulates a row written before the v4->v5 migration added economy columns: every
        // economy field defaults to 0, so `hasProgressionSnapshot` is false.
        val legacyRow = ActiveCreatureEntity(
            creatureId = "trex",
            eggRarity = "RARE",
            currentSteps = 12_345,
            startedAt = 1_699_000_000_000L,
            isRevealed = true,
            nickname = "Old Timer",
            hatchStep = 0,
            juvenileStep = 0,
            totalStepsRequired = 0,
            economyVersion = 0,
        )

        val upgraded = legacyRow.withLegacyV1SnapshotIfMissing()
        val expected = CreatureEconomy.legacyV1Thresholds("trex")

        // Player-visible progress is untouched…
        assertEquals(12_345, upgraded.currentSteps)
        assertEquals("RARE", upgraded.eggRarity)
        assertEquals("Old Timer", upgraded.nickname)
        assertTrue(upgraded.isRevealed)
        // …and the missing economy snapshot is filled with the frozen v1 curve for that species.
        assertEquals(expected.hatchStep, upgraded.hatchStep)
        assertEquals(expected.juvenileStep, upgraded.juvenileStep)
        assertEquals(expected.totalStepsRequired, upgraded.totalStepsRequired)
        assertEquals(expected.economyVersion, upgraded.economyVersion)

        // And the upgraded row maps to a domain object on the same v1 curve.
        val domain = upgraded.toDomain()
        assertEquals(expected, domain.progression)
    }

    @Test
    fun saveThatAlreadyHasEconomySnapshotIsLeftUntouched() {
        val modernRow = ActiveCreatureState.newEgg(CreatureCatalog.byId("tiny_raptor")!!, EggRarity.COMMON)
            .copy(steps = 500)
            .toEntity()

        assertEquals(modernRow, modernRow.withLegacyV1SnapshotIfMissing())
    }

    // endregion

    // region Cloud round trip (signed-in players restoring on a fresh device)

    @Test
    fun cloudRoundTripPreservesActiveCreatureCollectionAndPromoState() {
        val snapshot = richSnapshot()

        val cloud = CloudSaveMapper.toCloud(
            snapshot = snapshot,
            revision = 9L,
            updatedAt = "2026-07-04T12:00:00Z",
        )
        val restored = CloudSaveMapper.toSnapshot(cloud)

        assertNotNull(restored)
        requireNotNull(restored)

        // Active creature
        assertEquals("tiny_raptor", restored.activeCreature.creature.id)
        assertEquals(EggRarity.UNCOMMON, restored.activeCreature.eggRarity)
        assertEquals(5_000, restored.activeCreature.steps)
        assertTrue(restored.activeCreature.isRevealed)
        assertEquals("Dart", restored.activeCreature.nickname)
        assertEquals(snapshot.activeCreature.progression, restored.activeCreature.progression)
        assertEquals(snapshot.activeCreature.startedAt, restored.activeCreature.startedAt)

        // Collection
        assertEquals(2, restored.collection.size)
        assertEquals(listOf("trex", "stegosaurus"), restored.collection.map { it.creature.id })

        // Stats + promo bookkeeping that MUST survive a device swap
        assertEquals(6, restored.playerStats.eggsHatched)
        assertEquals(2, restored.playerStats.creaturesCompleted)
        assertEquals(9_000, restored.playerStats.lastSyncedStepTotal)
        assertEquals(123_456, restored.playerStats.lifetimeStepsApplied)
        assertEquals(EggRarity.EPIC.name, restored.playerStats.pendingRewardEggRarity)
        assertEquals("epic20,legend20", restored.playerStats.redeemedPromoCodes)
    }

    @Test
    fun cloudSchemaVersionIsStableSoOlderClientsCanStillDecode() {
        val cloud = CloudSaveMapper.toCloud(richSnapshot(), revision = 1L, updatedAt = "2026-07-04T12:00:00Z")
        assertEquals(2, cloud.schemaVersion)
        assertEquals(CloudGameSave.SCHEMA_VERSION, cloud.schemaVersion)
    }

    /**
     * Documents (and pins) a deliberate limitation: `totalFakeStepsAdded` is debug-only and is not
     * part of the cloud schema, so it resets to 0 after a cloud restore. Real progress
     * (`lifetimeStepsApplied`, collection, pending rewards) is preserved. If someone ever adds it to
     * the cloud schema, update this test to assert it survives instead.
     */
    @Test
    fun cloudRoundTripDropsDebugOnlyFakeStepCounter() {
        val snapshot = richSnapshot().let {
            it.copy(playerStats = it.playerStats.copy(totalFakeStepsAdded = 777))
        }

        val restored = CloudSaveMapper.toSnapshot(
            CloudSaveMapper.toCloud(snapshot, revision = 1L, updatedAt = "2026-07-04T12:00:00Z"),
        )

        assertNotNull(restored)
        assertEquals(0, restored!!.playerStats.totalFakeStepsAdded)
    }

    @Test
    fun cloudRestoreDropsUnknownSpeciesInsteadOfCrashing() {
        val cloud = CloudGameSave(
            revision = 1L,
            updatedAt = "2026-07-04T12:00:00Z",
            activeCreature = CloudActiveCreature(
                speciesId = "tiny_raptor",
                eggRarity = "COMMON",
                steps = 10,
                isRevealed = false,
                nickname = null,
                startedAt = Instant.ofEpochMilli(1_700_000_000_000L).toString(),
                hatchStep = 100,
                juvenileStep = 200,
                totalStepsRequired = 300,
                economyVersion = 2,
            ),
            completedCreatures = listOf(
                CloudCompletedCreature(
                    id = "1",
                    speciesId = "trex",
                    stepsCompleted = 50_000,
                    completedAt = Instant.ofEpochMilli(1_700_000_000_000L).toString(),
                    nickname = null,
                ),
                CloudCompletedCreature(
                    id = "2",
                    speciesId = "a_species_that_no_longer_exists",
                    stepsCompleted = 99,
                    completedAt = Instant.ofEpochMilli(1_700_000_000_000L).toString(),
                    nickname = null,
                ),
            ),
            playerStats = CloudPlayerStats(
                eggsHatched = 1,
                creaturesCompleted = 2,
                lastSyncedStepTotal = 0,
                lastSyncDayStartMillis = 0L,
                lifetimeStepsApplied = 50_000,
            ),
        )

        val restored = CloudSaveMapper.toSnapshot(cloud)

        assertNotNull(restored)
        assertEquals(listOf("trex"), restored!!.collection.map { it.creature.id })
    }

    @Test
    fun freshInstallSnapshotIsDetectedAsEmpty() {
        val fresh = GameSnapshot(
            activeCreature = ActiveCreatureState.newEgg(CreatureCatalog.byId("tiny_raptor")!!, EggRarity.COMMON),
            collection = emptyList(),
            playerStats = PlayerStats(),
        )
        assertTrue(CloudSaveMapper.isLocalEmpty(fresh))

        val played = fresh.copy(playerStats = fresh.playerStats.copy(lifetimeStepsApplied = 1))
        assertFalse(CloudSaveMapper.isLocalEmpty(played))
    }

    // endregion

    private fun richSnapshot(): GameSnapshot {
        val raptor = CreatureCatalog.byId("tiny_raptor")!!
        val active = ActiveCreatureState.newEgg(raptor, EggRarity.UNCOMMON).copy(
            steps = 5_000,
            isRevealed = true,
            nickname = "Dart",
            startedAt = 1_700_000_000_000L,
        )
        return GameSnapshot(
            activeCreature = active,
            collection = listOf(
                CompletedCreature(
                    id = 1L,
                    creature = CreatureCatalog.byId("trex")!!,
                    stepsCompleted = 50_000,
                    completedAt = 1_700_100_000_000L,
                    nickname = "Rex",
                    eggRarityAtHatch = EggRarity.RARE,
                    exSteps = 300,
                    exLevel = 3,
                ),
                CompletedCreature(
                    id = 2L,
                    creature = CreatureCatalog.byId("stegosaurus")!!,
                    stepsCompleted = 18_000,
                    completedAt = 1_700_200_000_000L,
                    nickname = null,
                    eggRarityAtHatch = EggRarity.COMMON,
                    exSteps = 0,
                    exLevel = 1,
                ),
            ),
            playerStats = PlayerStats(
                totalFakeStepsAdded = 0,
                eggsHatched = 6,
                creaturesCompleted = 2,
                lastSyncedStepTotal = 9_000,
                lastSyncDayStartMillis = 1_700_200_000_000L,
                lifetimeStepsApplied = 123_456,
                pendingRewardEggRarity = EggRarity.EPIC.name,
                redeemedPromoCodes = "epic20,legend20",
            ),
        )
    }
}
