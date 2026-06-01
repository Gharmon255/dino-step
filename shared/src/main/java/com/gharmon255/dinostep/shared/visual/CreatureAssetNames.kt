package com.gharmon255.dinostep.shared.visual

/**
 * Central drawable base names for creature art. Phone and Wear modules resolve these
 * to module-local `R.drawable` ids via [DrawableResourceResolver] implementations.
 */
object CreatureAssetNames {
    val assetBackedSpeciesIds: Set<String> = setOf(
        "tiny_raptor",
        "triceratops",
        "trex",
        "stegosaurus",
        "brachiosaurus",
        "ankylosaurus",
        "parasaurolophus",
        "spinosaurus",
        "pteranodon",
    )

    /** Legacy creature ids from older saves map to asset species ids. */
    fun resolveAssetSpeciesId(creatureId: String): String = when (creatureId) {
        "t_rex", "volcanic_t_rex", "ancient_apex_rex" -> "trex"
        "pterodactyl", "cosmic_pterodactyl" -> "pteranodon"
        else -> creatureId
    }

    fun hasStageAssets(creatureId: String): Boolean {
        return resolveAssetSpeciesId(creatureId) in assetBackedSpeciesIds
    }

    fun eggDrawableName(eggRarityName: String): String = when (eggRarityName.uppercase()) {
        "UNCOMMON" -> "egg_uncommon"
        "RARE" -> "egg_rare"
        "EPIC" -> "egg_epic"
        "LEGENDARY" -> "egg_legendary"
        else -> "egg_common"
    }

    /** Candidate drawable names, most preferred first. */
    fun eggDrawableCandidates(eggRarityName: String): List<String> {
        val primary = eggDrawableName(eggRarityName)
        return listOf(primary, "${primary}_(1)")
    }

    fun stageDrawableName(creatureId: String, stageName: String): String? {
        if (stageName.equals("EGG", ignoreCase = true)) {
            return null
        }
        val species = resolveAssetSpeciesId(creatureId)
        if (species !in assetBackedSpeciesIds) {
            return null
        }
        val suffix = when (stageName.uppercase()) {
            "BABY" -> "baby"
            "JUVENILE" -> "juvenile"
            "ADULT" -> "adult"
            else -> return null
        }
        return "dino_${species}_$suffix"
    }
}
