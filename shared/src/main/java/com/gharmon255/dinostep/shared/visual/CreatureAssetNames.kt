package com.gharmon255.dinostep.shared.visual

/**
 * Cross-platform creature drawable naming (Sprint 2).
 *
 * Stage PNGs: `dino_{speciesId}_{stage}` where stage is `baby`, `juvenile`, or `adult`.
 * Eggs: `egg_common`, `egg_uncommon`, etc. (by rarity, not species).
 */
object CreatureAssetNames {
    const val DINO_DRAWABLE_PREFIX = "dino_"

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

  /** Canonical stage suffixes for `dino_{speciesId}_{stage}` drawables. */
    object StageSuffix {
        const val BABY = "baby"
        const val JUVENILE = "juvenile"
        const val ADULT = "adult"
    }

    /**
     * Legacy / alternate ids → canonical asset-backed species id.
     * Catalog legendaries keep their own ids; only map ids that should share base art.
     */
    private val legacySpeciesIdAliases: Map<String, String> = mapOf(
        "t_rex" to "trex",
        "tyrannosaurus" to "trex",
        "tyrannosaurus_rex" to "trex",
        "pterodactyl" to "pteranodon",
        "volcanic_t_rex" to "trex",
        "ancient_apex_rex" to "trex",
        "cosmic_pterodactyl" to "pteranodon",
    )

    /**
     * Normalize a creature id for drawable lookup: lowercase, `-` and spaces → `_`, then legacy alias.
     */
    fun normalizeSpeciesId(creatureId: String): String {
        val normalized = creatureId
            .trim()
            .lowercase()
            .replace('-', '_')
            .replace(' ', '_')
        return legacySpeciesIdAliases[normalized] ?: normalized
    }

    /** @see normalizeSpeciesId */
    fun resolveAssetSpeciesId(creatureId: String): String = normalizeSpeciesId(creatureId)

    fun isAssetBacked(creatureId: String): Boolean {
        return normalizeSpeciesId(creatureId) in assetBackedSpeciesIds
    }

    fun dinoDrawablePrefix(speciesId: String): String = "$DINO_DRAWABLE_PREFIX${normalizeSpeciesId(speciesId)}"

    /**
     * Logical drawable name: `dino_{speciesId}_{stageSuffix}`.
     * Returns null if [stageSuffix] is not a supported growth stage.
     */
    fun dinoStageDrawableName(speciesId: String, stageSuffix: String): String? {
        if (stageSuffix !in supportedStageSuffixes) {
            return null
        }
        val canonicalSpecies = normalizeSpeciesId(speciesId)
        if (canonicalSpecies !in assetBackedSpeciesIds) {
            return null
        }
        return "${DINO_DRAWABLE_PREFIX}${canonicalSpecies}_$stageSuffix"
    }

    /**
     * Maps enum-style stage names (`BABY`, `JUVENILE`, `ADULT`, or suffix strings) to drawable name.
     */
    fun stageDrawableName(creatureId: String, stageName: String): String? {
        if (stageName.equals("EGG", ignoreCase = true)) {
            return null
        }
        val suffix = stageSuffixFromName(stageName) ?: return null
        return dinoStageDrawableName(creatureId, suffix)
    }

    fun stageSuffixFromName(stageName: String): String? = when (stageName.lowercase()) {
        "baby" -> StageSuffix.BABY
        "juvenile" -> StageSuffix.JUVENILE
        "adult" -> StageSuffix.ADULT
        else -> when (stageName.uppercase()) {
            "BABY" -> StageSuffix.BABY
            "JUVENILE" -> StageSuffix.JUVENILE
            "ADULT" -> StageSuffix.ADULT
            else -> null
        }
    }

    fun hasStageAssets(creatureId: String): Boolean = isAssetBacked(creatureId)

    private val supportedStageSuffixes: Set<String> = setOf(
        StageSuffix.BABY,
        StageSuffix.JUVENILE,
        StageSuffix.ADULT,
    )

    fun eggDrawableName(eggRarityName: String): String = when (eggRarityName.uppercase()) {
        "UNCOMMON" -> "egg_uncommon"
        "RARE" -> "egg_rare"
        "EPIC" -> "egg_epic"
        "LEGENDARY" -> "egg_legendary"
        else -> "egg_common"
    }

    fun eggDrawableCandidates(eggRarityName: String): List<String> {
        val primary = eggDrawableName(eggRarityName)
        return listOf(primary, "${primary}_(1)")
    }
}
