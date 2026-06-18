package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.CreatureEconomy
import com.gharmon255.dinostep.model.CreatureNickname
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.ProgressionThresholds

data class ActiveCreatureState(
    val creature: CreatureDefinition,
    val eggRarity: EggRarity,
    val progression: ProgressionThresholds,
    val steps: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val isRevealed: Boolean = false,
    val nickname: String? = null,
) {
    val stage: GrowthStage
        get() = progression.stageForSteps(steps)

    val displayName: String
        get() = CreatureNickname.activeDisplayName(
            speciesName = creature.name,
            nickname = nickname,
            isRevealed = isRevealed,
            mysteryDisplayName = eggRarity.mysteryDisplayName,
        )

    val speciesSubtitle: String?
        get() = CreatureNickname.speciesSubtitle(
            speciesName = creature.name,
            nickname = nickname,
            isRevealed = isRevealed,
        )

    val nextMilestone: Int?
        get() = progression.nextMilestone(steps)

    val progressPercent: Float
        get() = progression.progressPercent(steps)

    val isAdult: Boolean
        get() = stage == GrowthStage.ADULT

    fun normalized(): ActiveCreatureState {
        val revealed = isRevealed || steps >= progression.hatchStep
        return if (revealed == isRevealed) this else copy(isRevealed = revealed)
    }

    companion object {
        fun newEgg(
            creature: CreatureDefinition,
            eggRarity: EggRarity,
            economyVersion: Int = CreatureEconomy.CURRENT_ECONOMY,
        ): ActiveCreatureState {
            return ActiveCreatureState(
                creature = creature,
                eggRarity = eggRarity,
                progression = CreatureEconomy.thresholdsFor(creature, economyVersion),
                steps = 0,
                startedAt = System.currentTimeMillis(),
                isRevealed = false,
            )
        }
    }
}
