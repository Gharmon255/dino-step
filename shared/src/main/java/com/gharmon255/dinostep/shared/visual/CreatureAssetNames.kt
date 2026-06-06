package com.gharmon255.dinostep.shared.visual

/**
 * Cross-platform creature drawable naming.
 *
 * Species art (only when [assetSlugForSpeciesArt] is non-null):
 *   `dino_{slug}_baby` | `dino_{slug}_juvenile` | `dino_{slug}_adult`
 *
 * All other species use stage placeholders (never another real dinosaur):
 *   `dino_placeholder_baby` | `dino_placeholder_juvenile` | `dino_placeholder_adult`
 */
object CreatureAssetNames {
    const val DINO_DRAWABLE_PREFIX = "dino_"
    const val PLACEHOLDER_PREFIX = "dino_placeholder"

    /**
     * Species with imported stage PNGs. Only these ids may resolve to species-specific art.
     */
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
        "dilophosaurus",
        "carnotaurus",
        "mosasaurus",
        "pachycephalosaurus",
        "allosaurus",
        "iguanodon",
        "gallimimus",
        "baryonyx",
        "velociraptor_alpha",
        "therizinosaurus",
        "giganotosaurus",
        "quetzalcoatlus",
        "indominus_hybrid",
        "ancient_spinosaurus",
        "frost_raptor",
        "volcanic_t_rex",
        "shadow_triceratops",
        "cosmic_pterodactyl",
        "titanosaur",
        "ancient_apex_rex",
        "compsognathus",
        "plesiosaurus",
        "diplodocus",
    )

    object StageSuffix {
        const val BABY = "baby"
        const val JUVENILE = "juvenile"
        const val ADULT = "adult"
    }

    /**
     * Save/catalog id renames only (same species, different stored id). Must NOT map legendaries
     * to base species for artwork — e.g. ancient_apex_rex must not map to `trex` for art.
     */
    private val legacySaveIdToCatalogId: Map<String, String> = mapOf(
        "t_rex" to "trex",
        "pterodactyl" to "pteranodon",
    )

    /**
     * Older save ids that refer to an asset-backed species under a legacy slug.
     * Does not include variant legendaries.
     */
    private val legacySaveIdToAssetSlug: Map<String, String> = mapOf(
        "t_rex" to "trex",
        "tyrannosaurus" to "trex",
        "tyrannosaurus_rex" to "trex",
        "pterodactyl" to "pteranodon",
    )

    fun normalizeRawSpeciesId(creatureId: String): String {
        return creatureId
            .trim()
            .lowercase()
            .replace('-', '_')
            .replace(' ', '_')
    }

    /** Catalog lookup normalization (CreatureCatalog.byId), not for drawable aliasing. */
    fun normalizeCatalogSpeciesId(creatureId: String): String {
        val raw = normalizeRawSpeciesId(creatureId)
        return legacySaveIdToCatalogId[raw] ?: raw
    }

    @Deprecated("Use assetSlugForSpeciesArt for drawables", ReplaceWith("assetSlugForSpeciesArt(creatureId)"))
    fun resolveAssetSpeciesId(creatureId: String): String? = assetSlugForSpeciesArt(creatureId)

    /**
     * Slug used for `dino_{slug}_{stage}` when this creature has dedicated art.
     * Returns null for all other species (including legendaries like ancient_apex_rex).
     */
    fun assetSlugForSpeciesArt(creatureId: String): String? {
        val raw = normalizeRawSpeciesId(creatureId)
        if (raw in assetBackedSpeciesIds) {
            return raw
        }
        return legacySaveIdToAssetSlug[raw]
    }

    fun isAssetBacked(creatureId: String): Boolean = assetSlugForSpeciesArt(creatureId) != null

    fun hasStageAssets(creatureId: String): Boolean = isAssetBacked(creatureId)

    fun placeholderStageDrawableName(stageSuffix: String): String {
        require(stageSuffix in supportedStageSuffixes) {
            "Invalid stage suffix: $stageSuffix"
        }
        return "${PLACEHOLDER_PREFIX}_$stageSuffix"
    }

    fun dinoStageDrawableNameForSlug(assetSlug: String, stageSuffix: String): String {
        require(stageSuffix in supportedStageSuffixes)
        return "${DINO_DRAWABLE_PREFIX}${assetSlug}_$stageSuffix"
    }

    /**
     * Preferred logical name for a growth stage (species art or placeholder). Null for EGG.
     */
    fun stageDrawableLogicalName(creatureId: String, stageName: String): String? {
        if (stageName.equals("EGG", ignoreCase = true)) {
            return null
        }
        val suffix = stageSuffixFromName(stageName) ?: return null
        val slug = assetSlugForSpeciesArt(creatureId)
        return if (slug != null) {
            dinoStageDrawableNameForSlug(slug, suffix)
        } else {
            placeholderStageDrawableName(suffix)
        }
    }

    /** @deprecated Use [stageDrawableLogicalName] */
    fun stageDrawableName(creatureId: String, stageName: String): String? =
        stageDrawableLogicalName(creatureId, stageName)

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
