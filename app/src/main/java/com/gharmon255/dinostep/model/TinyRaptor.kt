package com.gharmon255.dinostep.model

object TinyRaptor {
    const val NAME = "Tiny Raptor"

    const val HATCH_STEPS = 1_600
    const val JUVENILE_STEPS = 4_000
    const val ADULT_STEPS = 8_000

    fun stageForSteps(steps: Int): GrowthStage = when {
        steps < HATCH_STEPS -> GrowthStage.EGG
        steps < JUVENILE_STEPS -> GrowthStage.BABY
        steps < ADULT_STEPS -> GrowthStage.JUVENILE
        else -> GrowthStage.ADULT
    }

    fun nextMilestone(steps: Int): Int? = when (stageForSteps(steps)) {
        GrowthStage.EGG -> HATCH_STEPS
        GrowthStage.BABY -> JUVENILE_STEPS
        GrowthStage.JUVENILE -> ADULT_STEPS
        GrowthStage.ADULT -> null
    }

    fun progressPercent(steps: Int): Float {
        val stage = stageForSteps(steps)
        val percent = when (stage) {
            GrowthStage.EGG -> steps.toFloat() / HATCH_STEPS
            GrowthStage.BABY -> (steps - HATCH_STEPS).toFloat() / (JUVENILE_STEPS - HATCH_STEPS)
            GrowthStage.JUVENILE -> (steps - JUVENILE_STEPS).toFloat() / (ADULT_STEPS - JUVENILE_STEPS)
            GrowthStage.ADULT -> 1f
        }
        return (percent * 100f).coerceIn(0f, 100f)
    }
}
