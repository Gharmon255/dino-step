package com.gharmon255.dinostep.model

object CreatureCatalog {
    val tinyRaptor = CreatureDefinition(
        id = "tiny_raptor",
        name = "Tiny Raptor",
        rarity = Rarity.COMMON,
        habitat = Habitat.JUNGLE,
        totalStepsRequired = 8_000,
        hatchStep = 1_600,
        juvenileStep = 4_000,
        emoji = "🦖",
    )

    val triceratops = CreatureDefinition(
        id = "triceratops",
        name = "Triceratops",
        rarity = Rarity.COMMON,
        habitat = Habitat.PLAINS,
        totalStepsRequired = 10_000,
        hatchStep = 2_000,
        juvenileStep = 5_000,
        emoji = "🦕",
    )

    val ankylosaurus = CreatureDefinition(
        id = "ankylosaurus",
        name = "Ankylosaurus",
        rarity = Rarity.COMMON,
        habitat = Habitat.ROCKY,
        totalStepsRequired = 12_000,
        hatchStep = 2_400,
        juvenileStep = 6_000,
        emoji = "🦕",
    )

    val stegosaurus = CreatureDefinition(
        id = "stegosaurus",
        name = "Stegosaurus",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.FOREST,
        totalStepsRequired = 18_000,
        hatchStep = 3_600,
        juvenileStep = 9_000,
        emoji = "🦕",
    )

    val pterodactyl = CreatureDefinition(
        id = "pterodactyl",
        name = "Pterodactyl",
        rarity = Rarity.UNCOMMON,
        habitat = Habitat.MOUNTAIN,
        totalStepsRequired = 22_000,
        hatchStep = 4_400,
        juvenileStep = 11_000,
        emoji = "🪽",
    )

    val tRex = CreatureDefinition(
        id = "t_rex",
        name = "T-Rex",
        rarity = Rarity.RARE,
        habitat = Habitat.VOLCANO,
        totalStepsRequired = 50_000,
        hatchStep = 10_000,
        juvenileStep = 25_000,
        emoji = "🦖",
    )

    val all: List<CreatureDefinition> = listOf(
        tinyRaptor,
        triceratops,
        ankylosaurus,
        stegosaurus,
        pterodactyl,
        tRex,
    )

    val commonCreatures: List<CreatureDefinition> = all.filter { it.rarity == Rarity.COMMON }

    fun byId(id: String): CreatureDefinition? = all.find { it.id == id }

    fun randomCommonCreature(): CreatureDefinition = commonCreatures.random()
}
