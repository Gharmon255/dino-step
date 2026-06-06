package com.gharmon255.dinostep.model

import com.gharmon255.dinostep.shared.visual.CreatureAssetNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatureCatalogIntegrityTest {
    @Test
    fun catalog_has32AssetBackedSpecies() {
        assertEquals(32, CreatureCatalog.assetBackedSpeciesIds.size)
        assertEquals(32, CreatureCatalog.assetBackedCreatures().size)
    }

    @Test
    fun catalog_idsAreUnique() {
        val ids = CreatureCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun catalog_shippedAssetBackedSpecies_areMarkedAssetBacked() {
        CreatureCatalog.assetBackedCreatures().forEach { creature ->
            assertTrue(
                "Expected asset-backed: ${creature.id}",
                CreatureAssetNames.isAssetBacked(creature.id),
            )
        }
    }

    @Test
    fun catalog_stepThresholdsAreOrdered() {
        CreatureCatalog.all.forEach { creature ->
            assertTrue(creature.hatchStep < creature.juvenileStep)
            assertTrue(creature.juvenileStep < creature.totalStepsRequired)
        }
    }

    @Test
    fun catalog_assetDrawableNamesExistForAllStages() {
        CreatureCatalog.assetBackedCreatures().forEach { creature ->
            listOf("BABY", "JUVENILE", "ADULT").forEach { stage ->
                val name = CreatureAssetNames.stageDrawableLogicalName(creature.id, stage)
                assertTrue("Missing drawable for ${creature.id}/$stage", !name.isNullOrBlank())
                assertTrue(name!!.startsWith("dino_"))
            }
        }
    }
}
