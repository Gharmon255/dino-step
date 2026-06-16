package com.gharmon255.dinostep.model

/**
 * Step economy versioning. v2 applies to new eggs only; in-progress eggs keep a snapshotted v1 curve.
 */
object CreatureEconomy {
    const val ECONOMY_V1 = 1
    const val ECONOMY_V2 = 2
    const val CURRENT_ECONOMY = ECONOMY_V2

    private const val HATCH_FRACTION = 0.18f
    private const val JUVENILE_FRACTION = 0.45f

    private val adultTotalV2: Map<Rarity, Int> = mapOf(
        Rarity.COMMON to 40_000,
        Rarity.UNCOMMON to 65_000,
        Rarity.RARE to 100_000,
        Rarity.EPIC to 150_000,
        Rarity.LEGENDARY to 240_000,
    )

    /** Frozen per-species v1 thresholds for eggs already in progress at upgrade time. */
    private val legacyV1BySpeciesId: Map<String, ProgressionThresholds> = mapOf(
        "tiny_raptor" to triple(1_600, 4_000, 8_000),
        "triceratops" to triple(2_000, 5_000, 10_000),
        "ankylosaurus" to triple(2_400, 6_000, 12_000),
        "parasaurolophus" to triple(2_200, 5_500, 11_000),
        "pachycephalosaurus" to triple(2_500, 6_250, 12_500),
        "gallimimus" to triple(1_800, 4_500, 9_000),
        "compsognathus" to triple(1_500, 3_750, 7_500),
        "stegosaurus" to triple(3_600, 9_000, 18_000),
        "brachiosaurus" to triple(4_000, 10_000, 20_000),
        "pteranodon" to triple(4_400, 11_000, 22_000),
        "dilophosaurus" to triple(4_000, 10_000, 20_000),
        "iguanodon" to triple(3_800, 9_500, 19_000),
        "carnotaurus" to triple(4_800, 12_000, 24_000),
        "baryonyx" to triple(5_000, 12_500, 25_000),
        "plesiosaurus" to triple(4_200, 10_500, 21_000),
        "trex" to triple(10_000, 25_000, 50_000),
        "spinosaurus" to triple(12_000, 30_000, 60_000),
        "velociraptor_alpha" to triple(9_000, 22_500, 45_000),
        "allosaurus" to triple(9_600, 24_000, 48_000),
        "therizinosaurus" to triple(11_000, 27_500, 55_000),
        "mosasaurus" to triple(13_000, 32_500, 65_000),
        "diplodocus" to triple(10_400, 26_000, 52_000),
        "giganotosaurus" to triple(17_000, 42_500, 85_000),
        "quetzalcoatlus" to triple(18_000, 45_000, 90_000),
        "indominus_hybrid" to triple(19_000, 47_500, 95_000),
        "ancient_spinosaurus" to triple(20_000, 50_000, 100_000),
        "crystal_ceratosaurus" to triple(18_400, 46_000, 92_000),
        "volcanic_t_rex" to triple(25_000, 62_500, 125_000),
        "frost_raptor" to triple(22_000, 55_000, 110_000),
        "shadow_triceratops" to triple(26_000, 65_000, 130_000),
        "titanosaur" to triple(30_000, 75_000, 150_000),
        "cosmic_pterodactyl" to triple(35_000, 87_500, 175_000),
        "ancient_apex_rex" to triple(40_000, 100_000, 200_000),
        "abyssal_mosasaurus" to triple(38_000, 95_000, 190_000),
    ).mapValues { (_, values) ->
        ProgressionThresholds(
            hatchStep = values.first,
            juvenileStep = values.second,
            totalStepsRequired = values.third,
            economyVersion = ECONOMY_V1,
        )
    }

    fun catalogThresholdsFor(rarity: Rarity): ProgressionThresholds =
        thresholdsForRarity(rarity, CURRENT_ECONOMY)

    fun thresholdsFor(creature: CreatureDefinition, economyVersion: Int = CURRENT_ECONOMY): ProgressionThresholds =
        when (economyVersion) {
            ECONOMY_V1 -> legacyV1Thresholds(creature.id)
            else -> thresholdsForRarity(creature.rarity, economyVersion)
        }

    fun legacyV1Thresholds(speciesId: String): ProgressionThresholds {
        val canonical = CreatureCatalog.byId(speciesId)?.id ?: speciesId
        return legacyV1BySpeciesId[canonical]
            ?: thresholdsForRarity(Rarity.COMMON, ECONOMY_V1)
    }

    fun thresholdsForRarity(rarity: Rarity, economyVersion: Int): ProgressionThresholds {
        if (economyVersion == ECONOMY_V1) {
            val sample = CreatureCatalog.byRarity(rarity).firstOrNull()
            return if (sample != null) {
                legacyV1Thresholds(sample.id)
            } else {
                thresholdsFromTotal(8_000, ECONOMY_V1)
            }
        }
        val total = adultTotalV2[rarity] ?: adultTotalV2[Rarity.COMMON]!!
        return thresholdsFromTotal(total, economyVersion)
    }

    private fun thresholdsFromTotal(total: Int, economyVersion: Int): ProgressionThresholds {
        val hatch = (total * HATCH_FRACTION).toInt()
        val juvenile = (total * JUVENILE_FRACTION).toInt()
        return ProgressionThresholds(
            hatchStep = hatch.coerceAtLeast(1),
            juvenileStep = juvenile.coerceAtLeast(hatch + 1),
            totalStepsRequired = total,
            economyVersion = economyVersion,
        )
    }

    private fun triple(hatch: Int, juvenile: Int, total: Int): Triple<Int, Int, Int> =
        Triple(hatch, juvenile, total)
}
