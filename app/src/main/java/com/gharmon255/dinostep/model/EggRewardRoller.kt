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

    fun rollWeighted(random: Random = Random.Default): RollResult {
        val rollValue = random.nextInt(100)
        var cumulative = 0
        for ((rarity, weight) in weightedTable) {
            cumulative += weight
            if (rollValue < cumulative) {
                return RollResult(eggRarity = rarity, rollValue = rollValue)
            }
        }
        return RollResult(eggRarity = EggRarity.COMMON, rollValue = rollValue)
    }
}
