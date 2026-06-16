package com.gharmon255.dinostep.model

import com.gharmon255.dinostep.shared.visual.CreatureAssetNames

/**
 * Creature roster. Sprint 1 canonical asset-backed species (cross-platform):
 * tiny_raptor, triceratops, trex, stegosaurus, brachiosaurus, ankylosaurus,
 * parasaurolophus, spinosaurus, pteranodon, dilophosaurus, carnotaurus, mosasaurus,
 * pachycephalosaurus, allosaurus, iguanodon, gallimimus, baryonyx, velociraptor_alpha,
 * therizinosaurus, giganotosaurus — drawable prefix `dino_{id}` per stage.
 */
object CreatureCatalog {
    private const val TAG = "CreatureCatalog"

    /** Species ids with baby/juvenile/adult PNG art in drawable-nodpi. */
    val assetBackedSpeciesIds: Set<String> = CreatureAssetNames.assetBackedSpeciesIds
    // COMMON
    val tinyRaptor = d(
        id = "tiny_raptor",
        name = "Tiny Raptor",
        rarity = Rarity.COMMON,
        habitat = Habitat.JUNGLE,
    )

    val triceratops = d(
        id = "triceratops",
        name = "Triceratops",
        rarity = Rarity.COMMON,
        habitat = Habitat.PLAINS,
    )

    val ankylosaurus = d(
        id = "ankylosaurus",
        name = "Ankylosaurus",
        rarity = Rarity.COMMON,
        habitat = Habitat.ROCKY,
    )

    val parasaurolophus = d(
        id = "parasaurolophus",
        name = "Parasaurolophus",
        rarity = Rarity.COMMON,
        habitat = Habitat.FOREST,
    )

    val pachycephalosaurus = d(
        id = "pachycephalosaurus",
        name = "Pachycephalosaurus",
        rarity = Rarity.COMMON,
        habitat = Habitat.ROCKY,
    )

    val gallimimus = d(
        id = "gallimimus",
        name = "Gallimimus",
        rarity = Rarity.COMMON,
        habitat = Habitat.PLAINS,
    )

    val compsognathus = d(
        id = "compsognathus",
        name = "Compsognathus",
        rarity = Rarity.COMMON,
        habitat = Habitat.JUNGLE,
    )

    // UNCOMMON
    val stegosaurus = d(
        id = "stegosaurus",
        name = "Stegosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.FOREST,
    )

    val brachiosaurus = d(
        id = "brachiosaurus",
        name = "Brachiosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.PLAINS,
    )

    val pteranodon = d(
        id = "pteranodon",
        name = "Pteranodon",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.MOUNTAIN,
    )

    val dilophosaurus = d(
        id = "dilophosaurus",
        name = "Dilophosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.JUNGLE,
    )

    val iguanodon = d(
        id = "iguanodon",
        name = "Iguanodon",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.FOREST,
    )

    val carnotaurus = d(
        id = "carnotaurus",
        name = "Carnotaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.VOLCANO,
    )

    val baryonyx = d(
        id = "baryonyx",
        name = "Baryonyx",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.SWAMP,
    )

    val plesiosaurus = d(
        id = "plesiosaurus",
        name = "Plesiosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.OCEAN,
    )

    // RARE
    val tRex = d(
        id = "trex",
        name = "T-Rex",
        rarity = Rarity.RARE,
        habitat = Habitat.VOLCANO,
    )

    val spinosaurus = d(
        id = "spinosaurus",
        name = "Spinosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.SWAMP,
    )

    val velociRaptorAlpha = d(
        id = "velociraptor_alpha",
        name = "Velociraptor Alpha",
        rarity = Rarity.RARE,
        habitat = Habitat.JUNGLE,
    )

    val allosaurus = d(
        id = "allosaurus",
        name = "Allosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.ROCKY,
    )

    val therizinosaurus = d(
        id = "therizinosaurus",
        name = "Therizinosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.FOREST,
    )

    val mosasaurus = d(
        id = "mosasaurus",
        name = "Mosasaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.OCEAN,
    )

    val diplodocus = d(
        id = "diplodocus",
        name = "Diplodocus",
        rarity = Rarity.RARE,
        habitat = Habitat.PLAINS,
    )

    // EPIC
    val giganotosaurus = d(
        id = "giganotosaurus",
        name = "Giganotosaurus",
        rarity = Rarity.EPIC,
        habitat = Habitat.PLAINS,
    )

    val quetzalcoatlus = d(
        id = "quetzalcoatlus",
        name = "Quetzalcoatlus",
        rarity = Rarity.EPIC,
        habitat = Habitat.MOUNTAIN,
    )

    val indominusHybrid = d(
        id = "indominus_hybrid",
        name = "Indominus Rex Style Hybrid",
        rarity = Rarity.EPIC,
        habitat = Habitat.LAB,
    )

    val ancientSpinosaurus = d(
        id = "ancient_spinosaurus",
        name = "Ancient Spinosaurus",
        rarity = Rarity.EPIC,
        habitat = Habitat.SWAMP,
    )

    val crystalCeratosaurus = d(
        id = "crystal_ceratosaurus",
        name = "Crystal Ceratosaurus",
        rarity = Rarity.EPIC,
        habitat = Habitat.ICE,
    )

    // LEGENDARY
    val volcanicTRex = d(
        id = "volcanic_t_rex",
        name = "Volcanic T-Rex",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.VOLCANO,
    )

    val frostRaptor = d(
        id = "frost_raptor",
        name = "Frost Raptor",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.ICE,
    )

    val shadowTriceratops = d(
        id = "shadow_triceratops",
        name = "Shadow Triceratops",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.DARK,
    )

    val titanosaur = d(
        id = "titanosaur",
        name = "Titanosaur",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.PLAINS,
    )

    val cosmicPterodactyl = d(
        id = "cosmic_pterodactyl",
        name = "Cosmic Pterodactyl",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.SKY,
    )

    val ancientApexRex = d(
        id = "ancient_apex_rex",
        name = "Ancient Apex Rex",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.VOLCANO,
    )

    val abyssalMosasaurus = d(
        id = "abyssal_mosasaurus",
        name = "Abyssal Mosasaurus",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.OCEAN,
    )

    val all: List<CreatureDefinition> = listOf(
        tinyRaptor,
        triceratops,
        ankylosaurus,
        parasaurolophus,
        pachycephalosaurus,
        gallimimus,
        compsognathus,
        stegosaurus,
        brachiosaurus,
        pteranodon,
        dilophosaurus,
        iguanodon,
        carnotaurus,
        baryonyx,
        plesiosaurus,
        tRex,
        spinosaurus,
        velociRaptorAlpha,
        allosaurus,
        therizinosaurus,
        mosasaurus,
        diplodocus,
        giganotosaurus,
        quetzalcoatlus,
        indominusHybrid,
        ancientSpinosaurus,
        crystalCeratosaurus,
        volcanicTRex,
        frostRaptor,
        shadowTriceratops,
        titanosaur,
        cosmicPterodactyl,
        ancientApexRex,
        abyssalMosasaurus,
    )

    val commonCreatures: List<CreatureDefinition> = byRarity(Rarity.COMMON)

    val uncommonCreatures: List<CreatureDefinition> = byRarity(Rarity.UNCOMMON)

    val rareCreatures: List<CreatureDefinition> = byRarity(Rarity.RARE)

    val epicCreatures: List<CreatureDefinition> = byRarity(Rarity.EPIC)

    val legendaryCreatures: List<CreatureDefinition> = byRarity(Rarity.LEGENDARY)

    fun byId(id: String): CreatureDefinition? {
        all.find { it.id == id }?.let { return it }
        return legacyCreatureIdAliases[id]?.let { legacyId -> all.find { it.id == legacyId } }
    }

    /**
     * Older saves may still reference pre-roster ids; map to canonical catalog entries.
     * [cosmic_pterodactyl] and [volcanic_t_rex] remain distinct legendaries in [all].
     */
    private val legacyCreatureIdAliases: Map<String, String> = mapOf(
        "t_rex" to "trex",
        "pterodactyl" to "pteranodon",
        "indominus_rex_style_hybrid" to "indominus_hybrid",
    )

    fun isAssetBacked(creatureId: String): Boolean = CreatureAssetNames.isAssetBacked(creatureId)

    fun assetPrefixFor(creatureId: String): String {
        val slug = CreatureAssetNames.assetSlugForSpeciesArt(creatureId)
        return if (slug != null) {
            "${CreatureAssetNames.DINO_DRAWABLE_PREFIX}$slug"
        } else {
            CreatureAssetNames.PLACEHOLDER_PREFIX
        }
    }

    fun assetBackedCreatures(): List<CreatureDefinition> =
        assetBackedSpeciesIds.mapNotNull { byId(it) }

    fun byRarity(rarity: Rarity): List<CreatureDefinition> = all.filter { it.rarity == rarity }

    fun creaturesForEgg(eggRarity: EggRarity): List<CreatureDefinition> = when (eggRarity) {
        EggRarity.COMMON -> commonCreatures
        EggRarity.UNCOMMON -> uncommonCreatures
        EggRarity.RARE -> rareCreatures
        EggRarity.EPIC -> epicCreatures
        EggRarity.LEGENDARY -> legendaryCreatures
    }

    fun randomCreatureForEgg(eggRarity: EggRarity): CreatureDefinition {
        val pool = creaturesForEgg(eggRarity)
        if (pool.isEmpty()) {
            android.util.Log.w(
                TAG,
                "Empty creature pool for $eggRarity — falling back to COMMON",
            )
            return commonCreatures.random()
        }
        return pool.random()
    }

    /** Legacy saves with unknown ids still load without crashing. */
    fun fallbackCreature(creatureId: String): CreatureDefinition {
        return byId(creatureId) ?: creatureFromId(creatureId, creatureId.replace('_', ' ').replaceFirstChar { it.uppercase() })
    }

    private fun creatureFromId(id: String, name: String, rarity: Rarity = Rarity.COMMON): CreatureDefinition {
        return CreatureDefinition(
            id = id,
            name = name,
            rarity = rarity,
            habitat = Habitat.JUNGLE,
            totalStepsRequired = CreatureEconomy.catalogThresholdsFor(rarity).totalStepsRequired,
            hatchStep = CreatureEconomy.catalogThresholdsFor(rarity).hatchStep,
            juvenileStep = CreatureEconomy.catalogThresholdsFor(rarity).juvenileStep,
            eggAssetKey = "${id}_egg",
            babyAssetKey = "${id}_baby",
            juvenileAssetKey = "${id}_juvenile",
            adultAssetKey = "${id}_adult",
        )
    }

    @Deprecated("Use randomCreatureForEgg(EggRarity.COMMON)", ReplaceWith("randomCreatureForEgg(EggRarity.COMMON)"))
    fun randomCommonCreature(): CreatureDefinition = randomCreatureForEgg(EggRarity.COMMON)

    private fun d(
        id: String,
        name: String,
        rarity: Rarity,
        habitat: Habitat,
    ): CreatureDefinition {
        val thresholds = CreatureEconomy.catalogThresholdsFor(rarity)
        return CreatureDefinition(
            id = id,
            name = name,
            rarity = rarity,
            habitat = habitat,
            totalStepsRequired = thresholds.totalStepsRequired,
            hatchStep = thresholds.hatchStep,
            juvenileStep = thresholds.juvenileStep,
            eggAssetKey = CreatureAssetNames.eggDrawableName(rarity.name),
            babyAssetKey = CreatureAssetNames.stageDrawableLogicalName(id, GrowthStage.BABY.name)
                ?: CreatureAssetNames.placeholderStageDrawableName(CreatureAssetNames.StageSuffix.BABY),
            juvenileAssetKey = CreatureAssetNames.stageDrawableLogicalName(id, GrowthStage.JUVENILE.name)
                ?: CreatureAssetNames.placeholderStageDrawableName(CreatureAssetNames.StageSuffix.JUVENILE),
            adultAssetKey = CreatureAssetNames.stageDrawableLogicalName(id, GrowthStage.ADULT.name)
                ?: CreatureAssetNames.placeholderStageDrawableName(CreatureAssetNames.StageSuffix.ADULT),
        )
    }
}
