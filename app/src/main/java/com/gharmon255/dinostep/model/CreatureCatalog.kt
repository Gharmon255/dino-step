package com.gharmon255.dinostep.model

import com.gharmon255.dinostep.shared.visual.CreatureAssetNames

/**
 * Creature roster. Sprint 1 canonical asset-backed species (cross-platform):
 * tiny_raptor, triceratops, trex, stegosaurus, brachiosaurus, ankylosaurus,
 * parasaurolophus, spinosaurus, pteranodon — drawable prefix `dino_{id}` per stage.
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
        total = 8_000,
        hatch = 1_600,
        juvenile = 4_000,
    )

    val triceratops = d(
        id = "triceratops",
        name = "Triceratops",
        rarity = Rarity.COMMON,
        habitat = Habitat.PLAINS,
        total = 10_000,
        hatch = 2_000,
        juvenile = 5_000,
    )

    val ankylosaurus = d(
        id = "ankylosaurus",
        name = "Ankylosaurus",
        rarity = Rarity.COMMON,
        habitat = Habitat.ROCKY,
        total = 12_000,
        hatch = 2_400,
        juvenile = 6_000,
    )

    val parasaurolophus = d(
        id = "parasaurolophus",
        name = "Parasaurolophus",
        rarity = Rarity.COMMON,
        habitat = Habitat.FOREST,
        total = 11_000,
        hatch = 2_200,
        juvenile = 5_500,
    )

    val pachycephalosaurus = d(
        id = "pachycephalosaurus",
        name = "Pachycephalosaurus",
        rarity = Rarity.COMMON,
        habitat = Habitat.ROCKY,
        total = 12_500,
        hatch = 2_500,
        juvenile = 6_250,
    )

    val gallimimus = d(
        id = "gallimimus",
        name = "Gallimimus",
        rarity = Rarity.COMMON,
        habitat = Habitat.PLAINS,
        total = 9_000,
        hatch = 1_800,
        juvenile = 4_500,
    )

    // UNCOMMON
    val stegosaurus = d(
        id = "stegosaurus",
        name = "Stegosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.FOREST,
        total = 18_000,
        hatch = 3_600,
        juvenile = 9_000,
    )

    val brachiosaurus = d(
        id = "brachiosaurus",
        name = "Brachiosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.PLAINS,
        total = 24_000,
        hatch = 4_800,
        juvenile = 12_000,
    )

    val pteranodon = d(
        id = "pteranodon",
        name = "Pteranodon",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.MOUNTAIN,
        total = 22_000,
        hatch = 4_400,
        juvenile = 11_000,
    )

    val dilophosaurus = d(
        id = "dilophosaurus",
        name = "Dilophosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.JUNGLE,
        total = 20_000,
        hatch = 4_000,
        juvenile = 10_000,
    )

    val iguanodon = d(
        id = "iguanodon",
        name = "Iguanodon",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.FOREST,
        total = 19_000,
        hatch = 3_800,
        juvenile = 9_500,
    )

    val carnotaurus = d(
        id = "carnotaurus",
        name = "Carnotaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.VOLCANO,
        total = 24_000,
        hatch = 4_800,
        juvenile = 12_000,
    )

    val baryonyx = d(
        id = "baryonyx",
        name = "Baryonyx",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.SWAMP,
        total = 25_000,
        hatch = 5_000,
        juvenile = 12_500,
    )

    // RARE
    val tRex = d(
        id = "trex",
        name = "T-Rex",
        rarity = Rarity.RARE,
        habitat = Habitat.VOLCANO,
        total = 50_000,
        hatch = 10_000,
        juvenile = 25_000,
    )

    val spinosaurus = d(
        id = "spinosaurus",
        name = "Spinosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.SWAMP,
        total = 60_000,
        hatch = 12_000,
        juvenile = 30_000,
    )

    val velociRaptorAlpha = d(
        id = "velociraptor_alpha",
        name = "Velociraptor Alpha",
        rarity = Rarity.RARE,
        habitat = Habitat.JUNGLE,
        total = 45_000,
        hatch = 9_000,
        juvenile = 22_500,
    )

    val allosaurus = d(
        id = "allosaurus",
        name = "Allosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.ROCKY,
        total = 48_000,
        hatch = 9_600,
        juvenile = 24_000,
    )

    val therizinosaurus = d(
        id = "therizinosaurus",
        name = "Therizinosaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.FOREST,
        total = 55_000,
        hatch = 11_000,
        juvenile = 27_500,
    )

    val mosasaurus = d(
        id = "mosasaurus",
        name = "Mosasaurus",
        rarity = Rarity.RARE,
        habitat = Habitat.OCEAN,
        total = 65_000,
        hatch = 13_000,
        juvenile = 32_500,
    )

    // EPIC
    val giganotosaurus = d(
        id = "giganotosaurus",
        name = "Giganotosaurus",
        rarity = Rarity.EPIC,
        habitat = Habitat.PLAINS,
        total = 85_000,
        hatch = 17_000,
        juvenile = 42_500,
    )

    val quetzalcoatlus = d(
        id = "quetzalcoatlus",
        name = "Quetzalcoatlus",
        rarity = Rarity.EPIC,
        habitat = Habitat.MOUNTAIN,
        total = 90_000,
        hatch = 18_000,
        juvenile = 45_000,
    )

    val indominusHybrid = d(
        id = "indominus_hybrid",
        name = "Indominus Rex Style Hybrid",
        rarity = Rarity.EPIC,
        habitat = Habitat.LAB,
        total = 95_000,
        hatch = 19_000,
        juvenile = 47_500,
    )

    val ancientSpinosaurus = d(
        id = "ancient_spinosaurus",
        name = "Ancient Spinosaurus",
        rarity = Rarity.EPIC,
        habitat = Habitat.SWAMP,
        total = 100_000,
        hatch = 20_000,
        juvenile = 50_000,
    )

    // LEGENDARY
    val volcanicTRex = d(
        id = "volcanic_t_rex",
        name = "Volcanic T-Rex",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.VOLCANO,
        total = 125_000,
        hatch = 25_000,
        juvenile = 62_500,
    )

    val frostRaptor = d(
        id = "frost_raptor",
        name = "Frost Raptor",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.ICE,
        total = 110_000,
        hatch = 22_000,
        juvenile = 55_000,
    )

    val shadowTriceratops = d(
        id = "shadow_triceratops",
        name = "Shadow Triceratops",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.DARK,
        total = 130_000,
        hatch = 26_000,
        juvenile = 65_000,
    )

    val titanosaur = d(
        id = "titanosaur",
        name = "Titanosaur",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.PLAINS,
        total = 150_000,
        hatch = 30_000,
        juvenile = 75_000,
    )

    val cosmicPterodactyl = d(
        id = "cosmic_pterodactyl",
        name = "Cosmic Pterodactyl",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.SKY,
        total = 175_000,
        hatch = 35_000,
        juvenile = 87_500,
    )

    val ancientApexRex = d(
        id = "ancient_apex_rex",
        name = "Ancient Apex Rex",
        rarity = Rarity.LEGENDARY,
        habitat = Habitat.VOLCANO,
        total = 200_000,
        hatch = 40_000,
        juvenile = 100_000,
    )

    val all: List<CreatureDefinition> = listOf(
        tinyRaptor,
        triceratops,
        ankylosaurus,
        parasaurolophus,
        pachycephalosaurus,
        gallimimus,
        stegosaurus,
        brachiosaurus,
        pteranodon,
        dilophosaurus,
        iguanodon,
        carnotaurus,
        baryonyx,
        tRex,
        spinosaurus,
        velociRaptorAlpha,
        allosaurus,
        therizinosaurus,
        mosasaurus,
        giganotosaurus,
        quetzalcoatlus,
        indominusHybrid,
        ancientSpinosaurus,
        volcanicTRex,
        frostRaptor,
        shadowTriceratops,
        titanosaur,
        cosmicPterodactyl,
        ancientApexRex,
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
            totalStepsRequired = tinyRaptor.totalStepsRequired,
            hatchStep = tinyRaptor.hatchStep,
            juvenileStep = tinyRaptor.juvenileStep,
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
        total: Int,
        hatch: Int,
        juvenile: Int,
    ): CreatureDefinition {
        return CreatureDefinition(
            id = id,
            name = name,
            rarity = rarity,
            habitat = habitat,
            totalStepsRequired = total,
            hatchStep = hatch,
            juvenileStep = juvenile,
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
