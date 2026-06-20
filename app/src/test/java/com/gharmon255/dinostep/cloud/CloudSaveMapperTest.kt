package com.gharmon255.dinostep.cloud

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.model.ProgressionThresholds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSaveMapperTest {
    @Test
    fun roundTrip_preservesSpeciesAndStats() {
        val creature = CreatureCatalog.byId("tiny_raptor")!!
        val progression = ProgressionThresholds(
            hatchStep = 100,
            juvenileStep = 200,
            totalStepsRequired = 300,
            economyVersion = 2,
        )
        val snapshot = GameSnapshot(
            activeCreature = ActiveCreatureState.newEgg(creature, EggRarity.COMMON).copy(
                steps = 42,
                progression = progression,
            ),
            collection = listOf(
                CompletedCreature(
                    id = 7L,
                    creature = CreatureCatalog.byId("trex")!!,
                    stepsCompleted = 500,
                    completedAt = 1_700_000_000_000L,
                    nickname = "Rex",
                    eggRarityAtHatch = EggRarity.RARE,
                    exSteps = 100,
                    exLevel = 2,
                ),
            ),
            playerStats = PlayerStats(
                eggsHatched = 2,
                creaturesCompleted = 1,
                lastSyncedStepTotal = 1000,
                lastSyncDayStartMillis = 1_700_000_000_000L,
                lifetimeStepsApplied = 9000,
            ),
        )

        val cloud = CloudSaveMapper.toCloud(
            snapshot = snapshot,
            revision = 5L,
            updatedAt = "2026-06-18T12:00:00Z",
        )
        assertEquals("tiny_raptor", cloud.activeCreature.speciesId)
        assertEquals("trex", cloud.completedCreatures.single().speciesId)
        assertEquals("RARE", cloud.completedCreatures.single().eggRarityAtHatch)
        assertEquals(CloudGameSave.SCHEMA_VERSION, cloud.schemaVersion)

        val restored = CloudSaveMapper.toSnapshot(cloud)
        assertNotNull(restored)
        assertEquals("tiny_raptor", restored!!.activeCreature.creature.id)
        assertEquals(42, restored.activeCreature.steps)
        assertEquals(1, restored.collection.size)
        assertEquals("trex", restored.collection.first().creature.id)
        assertEquals(9000, restored.playerStats.lifetimeStepsApplied)
    }

    @Test
    fun isLocalEmpty_detectsFreshInstall() {
        val creature = CreatureCatalog.byId("tiny_raptor")!!
        val snapshot = GameSnapshot(
            activeCreature = ActiveCreatureState.newEgg(creature, EggRarity.COMMON),
            collection = emptyList(),
            playerStats = PlayerStats(),
        )
        assertTrue(CloudSaveMapper.isLocalEmpty(snapshot))
    }

    @Test
    fun isLocalEmpty_falseWhenCollectionHasEntries() {
        val creature = CreatureCatalog.byId("tiny_raptor")!!
        val snapshot = GameSnapshot(
            activeCreature = ActiveCreatureState.newEgg(creature, EggRarity.COMMON),
            collection = listOf(
                CompletedCreature(
                    id = 1L,
                    creature = creature,
                    stepsCompleted = 10,
                    completedAt = 1L,
                ),
            ),
            playerStats = PlayerStats(),
        )
        assertFalse(CloudSaveMapper.isLocalEmpty(snapshot))
    }
}
