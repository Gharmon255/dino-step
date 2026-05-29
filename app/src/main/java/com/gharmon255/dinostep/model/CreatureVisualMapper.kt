package com.gharmon255.dinostep.model

object CreatureVisualMapper {
    const val EGG_PLACEHOLDER = "🥚"
    const val BABY_PLACEHOLDER = "🦎"
    const val JUVENILE_PLACEHOLDER = "🦕"
    const val ADULT_PLACEHOLDER = "🦖"
    const val LOCKED_PLACEHOLDER = "❓"

    fun getVisualForStage(
        creatureDefinition: CreatureDefinition,
        stage: GrowthStage,
        eggRarity: EggRarity,
    ): StageVisual {
        val assetKey = when (stage) {
            GrowthStage.EGG -> creatureDefinition.eggAssetKey
            GrowthStage.BABY -> creatureDefinition.babyAssetKey
            GrowthStage.JUVENILE -> creatureDefinition.juvenileAssetKey
            GrowthStage.ADULT -> creatureDefinition.adultAssetKey
        }
        val placeholderEmoji = placeholderEmojiForStage(stage)
        return StageVisual(
            assetKey = assetKey,
            placeholderEmoji = placeholderEmoji,
        )
    }

    fun placeholderEmojiForStage(stage: GrowthStage): String = when (stage) {
        GrowthStage.EGG -> EGG_PLACEHOLDER
        GrowthStage.BABY -> BABY_PLACEHOLDER
        GrowthStage.JUVENILE -> JUVENILE_PLACEHOLDER
        GrowthStage.ADULT -> ADULT_PLACEHOLDER
    }

    fun visualForActiveCreature(active: com.gharmon255.dinostep.game.ActiveCreatureState): StageVisual {
        return getVisualForStage(
            creatureDefinition = active.creature,
            stage = active.stage,
            eggRarity = active.eggRarity,
        )
    }
}
