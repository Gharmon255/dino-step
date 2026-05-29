package com.gharmon255.dinostep.model

enum class EggRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    ;

    val displayName: String
        get() = when (this) {
            COMMON -> "Common Egg"
            UNCOMMON -> "Uncommon Egg"
            RARE -> "Rare Egg"
            EPIC -> "Epic Egg"
            LEGENDARY -> "Legendary Egg"
        }
}
