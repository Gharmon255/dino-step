package com.gharmon255.dinostep.model

data class CreatureDefinition(
    val id: String,
    val name: String,
    val rarity: Rarity,
    val habitat: Habitat,
    val totalStepsRequired: Int,
    val hatchStep: Int,
    val juvenileStep: Int,
    val eggAssetKey: String,
    val babyAssetKey: String,
    val juvenileAssetKey: String,
    val adultAssetKey: String,
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
}
