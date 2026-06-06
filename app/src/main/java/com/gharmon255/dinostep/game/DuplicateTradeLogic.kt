package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity

data class DuplicateTradeOffer(
    val speciesId: String,
    val speciesName: String,
    val storedCount: Int,
    val rewardEggRarity: EggRarity,
) {
    val tradeButtonTitle: String
        get() = "Trade 2× $speciesName for ${rewardEggRarity.mysteryDisplayName}"

    val helperText: String
        get() = "Uses $storedCount in your collection plus this adult."

    val confirmationMessage: String
        get() = "Trade 2× $speciesName for a ${rewardEggRarity.mysteryDisplayName}? " +
            "One stored adult and this one will be removed. This cannot be undone."
}

object DuplicateTradeLogic {
    fun offer(
        activeCreature: ActiveCreatureState,
        collection: List<CompletedCreature>,
    ): DuplicateTradeOffer? {
        if (activeCreature.stage != GrowthStage.ADULT) {
            return null
        }
        if (!activeCreature.isRevealed) {
            return null
        }

        val speciesId = activeCreature.creature.id
        val storedCount = collectionCount(speciesId, collection)
        if (storedCount < 1) {
            return null
        }

        val rewardEggRarity = nextEggRarity(activeCreature.creature.rarity) ?: return null

        return DuplicateTradeOffer(
            speciesId = speciesId,
            speciesName = activeCreature.creature.name,
            storedCount = storedCount,
            rewardEggRarity = rewardEggRarity,
        )
    }

    fun nextEggRarity(speciesRarity: Rarity): EggRarity? = when (speciesRarity) {
        Rarity.COMMON -> EggRarity.UNCOMMON
        Rarity.UNCOMMON -> EggRarity.RARE
        Rarity.RARE -> EggRarity.EPIC
        Rarity.EPIC -> EggRarity.LEGENDARY
        Rarity.LEGENDARY -> null
    }

    fun collectionCount(speciesId: String, collection: List<CompletedCreature>): Int =
        collection.count { it.creature.id == speciesId }

    /**
     * Removes the oldest stored adult of [speciesId]. Returns the updated list and removed entry.
     */
    fun removeOneCompleted(
        speciesId: String,
        collection: List<CompletedCreature>,
    ): Pair<List<CompletedCreature>, CompletedCreature>? {
        val oldest = collection
            .filter { it.creature.id == speciesId }
            .minByOrNull { it.completedAt }
            ?: return null

        return collection.filterNot { it == oldest } to oldest
    }
}
