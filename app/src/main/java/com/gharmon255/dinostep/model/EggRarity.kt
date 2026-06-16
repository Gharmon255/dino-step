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
        get() = MYSTERY_EGG_LABEL

    companion object {
        const val MYSTERY_EGG_LABEL = "Mystery Egg"

        fun fromRaw(value: String?): EggRarity {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: COMMON
        }
    }
}
