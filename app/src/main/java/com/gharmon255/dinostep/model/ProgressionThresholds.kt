package com.gharmon255.dinostep.model

data class ProgressionThresholds(
    val hatchStep: Int,
    val juvenileStep: Int,
    val totalStepsRequired: Int,
    val economyVersion: Int,
) {
    fun stageForSteps(steps: Int): GrowthStage = when {
        steps < hatchStep -> GrowthStage.EGG
        steps < juvenileStep -> GrowthStage.BABY
        steps < totalStepsRequired -> GrowthStage.JUVENILE
        else -> GrowthStage.ADULT
    }

    fun nextMilestone(steps: Int): Int? = when (stageForSteps(steps)) {
        GrowthStage.EGG -> hatchStep
        GrowthStage.BABY -> juvenileStep
        GrowthStage.JUVENILE -> totalStepsRequired
        GrowthStage.ADULT -> null
    }

    fun progressPercent(steps: Int): Float {
        val stage = stageForSteps(steps)
        val percent = when (stage) {
            GrowthStage.EGG -> steps.toFloat() / hatchStep
            GrowthStage.BABY -> (steps - hatchStep).toFloat() / (juvenileStep - hatchStep)
            GrowthStage.JUVENILE -> (steps - juvenileStep).toFloat() / (totalStepsRequired - juvenileStep)
            GrowthStage.ADULT -> 1f
        }
        return (percent * 100f).coerceIn(0f, 100f)
    }

    fun stageProgressPercent(steps: Int): Float {
        return when (stageForSteps(steps)) {
            GrowthStage.EGG -> {
                if (hatchStep <= 0) 0f else (steps.toFloat() / hatchStep * 100f).coerceIn(0f, 100f)
            }
            GrowthStage.BABY -> {
                val range = juvenileStep - hatchStep
                if (range <= 0) 100f else ((steps - hatchStep).toFloat() / range * 100f).coerceIn(0f, 100f)
            }
            GrowthStage.JUVENILE -> {
                val range = totalStepsRequired - juvenileStep
                if (range <= 0) 100f else ((steps - juvenileStep).toFloat() / range * 100f).coerceIn(0f, 100f)
            }
            GrowthStage.ADULT -> 100f
        }
    }
}
