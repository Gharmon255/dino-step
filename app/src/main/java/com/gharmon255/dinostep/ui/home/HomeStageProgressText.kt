package com.gharmon255.dinostep.ui.home

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.shared.wear.WearStageProgress
import java.text.NumberFormat

/**
 * Phone Home copy for steps until the next growth stage (display only).
 * Uses [WearStageProgress] for remaining steps — same math as Wear, clearer wording on Home.
 */
object HomeStageProgressText {
    const val READY_TO_CLAIM_REWARD = "Ready to claim reward"

    fun formatNextStageLine(
        activeCreature: ActiveCreatureState,
        numberFormat: NumberFormat,
    ): String {
        val creature = activeCreature.creature
        val info = WearStageProgress.calculate(
            stageName = activeCreature.stage.name,
            currentSteps = activeCreature.steps.coerceAtLeast(0),
            hatchStep = creature.hatchStep,
            juvenileStep = creature.juvenileStep,
            totalStepsRequired = creature.totalStepsRequired,
        )

        if (info.nextStageLabel == WearStageProgress.LABEL_READY_TO_CLAIM) {
            return READY_TO_CLAIM_REWARD
        }

        val stepsRemaining = info.stepsUntilNextStage.coerceAtLeast(0)
        val label = info.nextStageLabel

        if (stepsRemaining == 0) {
            return "Ready to $label"
        }

        return "${numberFormat.format(stepsRemaining)} steps to $label"
    }
}
