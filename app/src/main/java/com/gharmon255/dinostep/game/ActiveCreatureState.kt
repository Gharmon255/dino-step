package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.GrowthStage

data class ActiveCreatureState(
    val creature: CreatureDefinition,
    val steps: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val isRevealed: Boolean = false,
) {
    val stage: GrowthStage
        get() = creature.stageForSteps(steps)

    val displayName: String
        get() = if (isRevealed) creature.name else MYSTERY_EGG_NAME

    val nextMilestone: Int?
        get() = creature.nextMilestone(steps)

    val progressPercent: Float
        get() = creature.progressPercent(steps)

    val isAdult: Boolean
        get() = stage == GrowthStage.ADULT

    fun normalized(): ActiveCreatureState {
        val revealed = isRevealed || steps >= creature.hatchStep
        return if (revealed == isRevealed) this else copy(isRevealed = revealed)
    }

    companion object {
        const val MYSTERY_EGG_NAME = "Mystery Egg"
    }
}
