package com.gharmon255.dinostep.model

import com.gharmon255.dinostep.game.ActiveCreatureState

object EggCrackLevel {
    fun fromEggProgress(progress: Float): Int = when {
        progress >= 0.85f -> 3
        progress >= 0.55f -> 2
        progress >= 0.25f -> 1
        else -> 0
    }

    fun forActiveEgg(activeCreature: ActiveCreatureState): Int {
        if (activeCreature.stage != GrowthStage.EGG) {
            return 0
        }
        val hatchStep = activeCreature.progression.hatchStep
        if (hatchStep <= 0) {
            return 0
        }
        val progress = activeCreature.steps.toFloat() / hatchStep.toFloat()
        return fromEggProgress(progress)
    }
}
