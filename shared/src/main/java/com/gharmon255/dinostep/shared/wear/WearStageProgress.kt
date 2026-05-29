package com.gharmon255.dinostep.shared.wear

/**
 * Phone and watch share the same rules for "steps until next growth stage" display.
 */
object WearStageProgress {
    const val LABEL_READY_TO_CLAIM = "Ready to claim"

    data class Info(
        val stepsUntilNextStage: Int,
        val nextStageLabel: String,
    )

    fun calculate(
        stageName: String,
        currentSteps: Int,
        hatchStep: Int,
        juvenileStep: Int,
        totalStepsRequired: Int,
    ): Info {
        return when (stageName.uppercase()) {
            "EGG" -> Info(
                stepsUntilNextStage = (hatchStep - currentSteps).coerceAtLeast(0),
                nextStageLabel = "hatch",
            )
            "BABY" -> Info(
                stepsUntilNextStage = (juvenileStep - currentSteps).coerceAtLeast(0),
                nextStageLabel = "juvenile",
            )
            "JUVENILE" -> Info(
                stepsUntilNextStage = (totalStepsRequired - currentSteps).coerceAtLeast(0),
                nextStageLabel = "adult",
            )
            else -> Info(
                stepsUntilNextStage = 0,
                nextStageLabel = LABEL_READY_TO_CLAIM,
            )
        }
    }

    fun formatDisplayLine(
        stepsUntilNextStage: Int,
        nextStageLabel: String,
        formattedSteps: String,
    ): String {
        if (nextStageLabel == LABEL_READY_TO_CLAIM) {
            return LABEL_READY_TO_CLAIM
        }
        return "$formattedSteps to $nextStageLabel"
    }
}
