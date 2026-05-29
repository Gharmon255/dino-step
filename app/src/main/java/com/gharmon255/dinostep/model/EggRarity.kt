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

    val mysteryDisplayName: String
        get() = when (this) {
            COMMON -> "Mystery Common Egg"
            UNCOMMON -> "Mystery Uncommon Egg"
            RARE -> "Mystery Rare Egg"
            EPIC -> "Mystery Epic Egg"
            LEGENDARY -> "Mystery Legendary Egg"
        }

    companion object {
        fun fromRaw(value: String?): EggRarity {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: COMMON
        }
    }
}
