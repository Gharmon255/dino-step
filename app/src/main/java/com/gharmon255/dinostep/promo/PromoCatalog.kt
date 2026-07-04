package com.gharmon255.dinostep.promo

import com.gharmon255.dinostep.model.EggRarity

object PromoCatalog {
    /** Normalized code (lowercase) → egg rarity on next reward claim. Keep in sync with Supabase `redeem-promo`. */
    private val codes: Map<String, EggRarity> = mapOf(
        "epic20" to EggRarity.EPIC,
        "legend20" to EggRarity.LEGENDARY,
    )

    fun normalize(raw: String): String = raw.trim().lowercase()

    fun rewardFor(rawCode: String): EggRarity? = codes[normalize(rawCode)]

    fun knownCodes(): Set<String> = codes.keys

    fun successMessage(rarity: EggRarity): String {
        return "Your next reward egg will be ${rarity.name.lowercase()}!"
    }
}

object PromoRedemptionCodec {
    private const val SEPARATOR = ","

    fun parse(stored: String?): Set<String> {
        if (stored.isNullOrBlank()) return emptySet()
        return stored.split(SEPARATOR)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun encode(codes: Set<String>): String? {
        if (codes.isEmpty()) return null
        return codes.sorted().joinToString(SEPARATOR)
    }

    fun markRedeemed(stored: String?, code: String): String? {
        val normalized = PromoCatalog.normalize(code)
        return encode(parse(stored) + normalized)
    }

    fun hasRedeemed(stored: String?, code: String): Boolean {
        return PromoCatalog.normalize(code) in parse(stored)
    }
}
