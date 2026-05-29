package com.gharmon255.dinostep.shared.visual

/**
 * Central rarity color tokens (ARGB). Used by phone UI and synced to Wear for accents.
 */
object RarityTheme {
    const val COMMON_ARGB: Long = 0xFF6B8E6B
    const val UNCOMMON_ARGB: Long = 0xFF4A90D9
    const val RARE_ARGB: Long = 0xFF9B59B6
    const val EPIC_ARGB: Long = 0xFFE040FB
    const val LEGENDARY_ARGB: Long = 0xFFFFC107

    fun accentArgbForName(rarityName: String?): Long {
        return when (rarityName?.uppercase()) {
            "UNCOMMON" -> UNCOMMON_ARGB
            "RARE" -> RARE_ARGB
            "EPIC" -> EPIC_ARGB
            "LEGENDARY" -> LEGENDARY_ARGB
            else -> COMMON_ARGB
        }
    }

    /** Egg color before hatch; creature rarity after reveal. */
    fun resolveAccentArgb(
        isRevealed: Boolean,
        eggRarityName: String,
        creatureRarityName: String?,
    ): Long {
        return if (isRevealed && !creatureRarityName.isNullOrBlank()) {
            accentArgbForName(creatureRarityName)
        } else {
            accentArgbForName(eggRarityName)
        }
    }
}
