package com.gharmon255.dinostep.shared.visual

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatureAssetNamesTest {
    @Test
    fun assetBackedSpecies_countIs34() {
        assertEquals(34, CreatureAssetNames.assetBackedSpeciesIds.size)
    }

    @Test
    fun legacyAliases_resolveToCanonicalSlug() {
        assertEquals("trex", CreatureAssetNames.assetSlugForSpeciesArt("t_rex"))
        assertEquals("pteranodon", CreatureAssetNames.assetSlugForSpeciesArt("pterodactyl"))
    }

    @Test
    fun variantLegendaries_doNotAliasToBaseSpecies() {
        assertEquals("ancient_apex_rex", CreatureAssetNames.assetSlugForSpeciesArt("ancient_apex_rex"))
        assertEquals("volcanic_t_rex", CreatureAssetNames.assetSlugForSpeciesArt("volcanic_t_rex"))
        assertEquals("shadow_triceratops", CreatureAssetNames.assetSlugForSpeciesArt("shadow_triceratops"))
        assertFalse(
            CreatureAssetNames.assetSlugForSpeciesArt("ancient_apex_rex") ==
                CreatureAssetNames.assetSlugForSpeciesArt("trex"),
        )
    }

    @Test
    fun unknownSpecies_usesPlaceholderDrawable() {
        assertNull(CreatureAssetNames.assetSlugForSpeciesArt("unknown_species"))
        assertEquals(
            "dino_placeholder_baby",
            CreatureAssetNames.stageDrawableLogicalName("unknown_species", "BABY"),
        )
    }

    @Test
    fun assetBackedSpecies_stageDrawableNames() {
        assertEquals(
            "dino_frost_raptor_adult",
            CreatureAssetNames.stageDrawableLogicalName("frost_raptor", "ADULT"),
        )
    }

    @Test
    fun eggDrawableNames_mapRarities() {
        assertEquals("egg_common", CreatureAssetNames.eggDrawableName("COMMON"))
        assertEquals("egg_legendary", CreatureAssetNames.eggDrawableName("legendary"))
    }
}
