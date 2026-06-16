package com.gharmon255.dinostep.ui.home

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.shared.wear.WearStageProgress
import java.text.NumberFormat

/**
 * Phone Home display copy for stage vs overall progress (display only).
 * Stage % matches Wear ring semantics via [ActiveCreatureState.progressPercent].
 */
object HomeStageProgressText {
    const val READY_TO_CLAIM_REWARD = "Ready to claim reward"

    /** Progress within the current growth stage (egg→hatch, etc.) — same as Wear ring. */
    fun stageProgressPercent(activeCreature: ActiveCreatureState): Float {
        return activeCreature.progressPercent.coerceIn(0f, 100f)
    }

    /** Lifetime progress from 0 steps to fully adult. */
    fun overallProgressPercent(activeCreature: ActiveCreatureState): Float {
        val total = activeCreature.progression.totalStepsRequired
        if (total <= 0) {
            return 0f
        }
        val steps = activeCreature.steps.coerceAtLeast(0)
        return ((steps.toFloat() / total) * 100f).coerceIn(0f, 100f)
    }

    fun formatStageProgressLabel(activeCreature: ActiveCreatureState): String {
        val percent = stageProgressPercent(activeCreature).toInt()
        val phase = stagePhaseDescription(activeCreature.stage, activeCreature.isAdult)
        return "Stage Progress: $percent% $phase"
    }

    fun formatOverallProgressLabel(activeCreature: ActiveCreatureState): String {
        val percent = overallProgressPercent(activeCreature).toInt()
        return if (activeCreature.isAdult) {
            "Overall Progress: 100% to adult"
        } else {
            "Overall Progress: $percent% to adult"
        }
    }

    private fun stagePhaseDescription(stage: GrowthStage, isAdult: Boolean): String {
        if (isAdult) {
            return "(ready to claim)"
        }
        return when (stage) {
            GrowthStage.EGG -> "to hatch"
            GrowthStage.BABY -> "to juvenile"
            GrowthStage.JUVENILE -> "to adult"
            GrowthStage.ADULT -> "(ready to claim)"
        }
    }

    fun formatNextStageLine(
        activeCreature: ActiveCreatureState,
        numberFormat: NumberFormat,
    ): String {
        val progression = activeCreature.progression
        val info = WearStageProgress.calculate(
            stageName = activeCreature.stage.name,
            currentSteps = activeCreature.steps.coerceAtLeast(0),
            hatchStep = progression.hatchStep,
            juvenileStep = progression.juvenileStep,
            totalStepsRequired = progression.totalStepsRequired,
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
