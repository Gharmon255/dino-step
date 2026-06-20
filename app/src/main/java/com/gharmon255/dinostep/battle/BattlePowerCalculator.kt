package com.gharmon255.dinostep.battle

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.Rarity
import kotlin.math.floor
import kotlin.math.min

data class FighterPower(
    val combatPower: Int,
    val maxHp: Int,
    val attack: Int,
    val packCount: Int,
    val packMultiplier: Double,
    val exLevel: Int,
    val speciesId: String,
    val displayName: String,
)

object BattlePowerCalculator {
    private val speciesBasePower = mapOf(
        Rarity.COMMON to 100,
        Rarity.UNCOMMON to 130,
        Rarity.RARE to 170,
        Rarity.EPIC to 220,
        Rarity.LEGENDARY to 280,
    )

    private val eggBonus = mapOf(
        EggRarity.COMMON to 0,
        EggRarity.UNCOMMON to 10,
        EggRarity.RARE to 20,
        EggRarity.EPIC to 30,
        EggRarity.LEGENDARY to 40,
    )

    fun packCount(collection: List<CompletedCreature>, speciesId: String): Int {
        return collection.count { it.creature.id == speciesId }
    }

    fun packMultiplier(packCount: Int): Double {
        if (packCount <= 1) {
            return 1.0
        }
        return 1.0 + min(packCount - 1, 3) * 0.15
    }

    fun packAbilityLabel(speciesId: String): String {
        return when {
            speciesId.contains("raptor") -> "Pack Hunt"
            speciesId.contains("triceratops") -> "Herd Stomp"
            else -> "Team Up"
        }
    }

    fun compute(
        fighter: CompletedCreature,
        collection: List<CompletedCreature>,
    ): FighterPower {
        val base = speciesBasePower[fighter.creature.rarity] ?: 100
        val egg = eggBonus[fighter.eggRarityAtHatch] ?: 0
        val ex = fighter.exLevel * 3
        val count = packCount(collection, fighter.creature.id)
        val multiplier = packMultiplier(count)
        val combatPower = floor((base + egg + ex) * multiplier).toInt()
        return FighterPower(
            combatPower = combatPower,
            maxHp = floor(combatPower * 1.2).toInt(),
            attack = floor(combatPower * 0.35).toInt().coerceAtLeast(1),
            packCount = count,
            packMultiplier = multiplier,
            exLevel = fighter.exLevel,
            speciesId = fighter.creature.id,
            displayName = fighter.displayName,
        )
    }
}
