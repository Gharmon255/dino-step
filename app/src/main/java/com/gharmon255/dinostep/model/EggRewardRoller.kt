package com.gharmon255.dinostep.model

import kotlin.random.Random

object EggRewardRoller {
    data class RollResult(
        val eggRarity: EggRarity,
        /** Uniform roll in 0..99 used for the weighted table. */
        val rollValue: Int,
    ) {
        val rollPercentLabel: String
            get() = "$rollValue"
    }

    private val weightedTable: List<Pair<EggRarity, Int>> = listOf(
        EggRarity.COMMON to 65,
        EggRarity.UNCOMMON to 22,
        EggRarity.RARE to 9,
        EggRarity.EPIC to 3,
        EggRarity.LEGENDARY to 1,
    )

    fun rollWeighted(random: Random = Random.Default): RollResult = rollWeighted(random.nextInt(100))

    /** Deterministic roll for unit tests (`rollValue` in 0..99). */
    fun rollWeighted(rollValue: Int): RollResult {
        val roll = rollValue.coerceIn(0, 99)
        var cumulative = 0
        for ((rarity, weight) in weightedTable) {
            cumulative += weight
            if (roll < cumulative) {
                return RollResult(eggRarity = rarity, rollValue = roll)
            }
        }
        return RollResult(eggRarity = EggRarity.COMMON, rollValue = roll)
    }
}
