package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller

/**
 * Sprint 3 — single entry point for developer/test egg creation.
 *
 * Rules (do not regress):
 * - [grantRandomEggForRarity] and [grantWeightedRandomEgg] never apply a test species override.
 * - [grantForcedSpeciesEgg] is the only path that forces a species (used by Force Selected Species Egg).
 * - Normal gameplay ([GameRepository.createRandomEgg]) must not call [grantForcedSpeciesEgg].
 */
class TestingEggFactory(
    private val repository: GameRepository,
) {
    data class WeightedEggGrant(
        val egg: ActiveCreatureState,
        val roll: EggRewardRoller.RollResult,
    )

    sealed class ForceButtonGrant {
        data class ForcedSpecies(val egg: ActiveCreatureState) : ForceButtonGrant()

        data class WeightedRandom(val grant: WeightedEggGrant) : ForceButtonGrant()
    }

    fun grantWeightedRandomEgg(): WeightedEggGrant {
        val roll = EggRewardRoller.rollWeighted()
        return WeightedEggGrant(
            egg = repository.createRandomEggWithRarity(roll.eggRarity),
            roll = roll,
        )
    }

    fun grantRandomEggForRarity(eggRarity: EggRarity): ActiveCreatureState {
        return repository.createRandomEggWithRarity(eggRarity)
    }

    fun grantForcedSpeciesEgg(speciesId: String): ActiveCreatureState {
        return repository.createForcedSpeciesEgg(speciesId)
    }

    /**
     * Force Selected Species Egg button only.
     * @param testSpeciesOverrideId from [NextEggTestSpecies.testSpeciesOverrideId]; null = weighted random.
     */
    fun grantForceButtonEgg(testSpeciesOverrideId: String?): ForceButtonGrant {
        return if (testSpeciesOverrideId != null) {
            ForceButtonGrant.ForcedSpecies(grantForcedSpeciesEgg(testSpeciesOverrideId))
        } else {
            ForceButtonGrant.WeightedRandom(grantWeightedRandomEgg())
        }
    }
}
