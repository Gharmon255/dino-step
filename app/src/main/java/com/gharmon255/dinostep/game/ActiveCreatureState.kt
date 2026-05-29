package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.GrowthStage

data class ActiveCreatureState(
    val creature: CreatureDefinition,
    val steps: Int = 0,
) {
    val stage: GrowthStage
        get() = creature.stageForSteps(steps)

    val displayName: String
        get() = if (stage == GrowthStage.EGG) MYSTERY_EGG_NAME else creature.name

    val isRevealed: Boolean
        get() = stage != GrowthStage.EGG

    val nextMilestone: Int?
        get() = creature.nextMilestone(steps)

    val progressPercent: Float
        get() = creature.progressPercent(steps)

    val isAdult: Boolean
        get() = stage == GrowthStage.ADULT

    companion object {
        const val MYSTERY_EGG_NAME = "Mystery Egg"
    }
}
