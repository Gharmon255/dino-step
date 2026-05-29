package com.gharmon255.dinostep.model

import com.gharmon255.dinostep.game.ActiveCreatureState

object CreatureVisualMapper {
    const val EGG_PLACEHOLDER = "🥚"
    const val LOCKED_PLACEHOLDER = "❓"

    fun getVisualForStage(
        creatureDefinition: CreatureDefinition,
        stage: GrowthStage,
        @Suppress("UNUSED_PARAMETER") eggRarity: EggRarity,
    ): StageVisual {
        val species = CreatureSpeciesIdentity.forId(creatureDefinition.id)
        // TODO: Resolve assetKey to @DrawableRes when art pipeline is wired.
        val assetKey = when (stage) {
            GrowthStage.EGG -> creatureDefinition.eggAssetKey
            GrowthStage.BABY -> creatureDefinition.babyAssetKey
            GrowthStage.JUVENILE -> creatureDefinition.juvenileAssetKey
            GrowthStage.ADULT -> creatureDefinition.adultAssetKey
        }
        val stagePresentation = stagePresentation(stage)
        val displayEmoji = if (stage == GrowthStage.EGG) {
            EGG_PLACEHOLDER
        } else {
            species.emoji
        }
        return StageVisual(
            assetKey = assetKey,
            speciesEmoji = species.emoji,
            displayEmoji = displayEmoji,
            speciesShortLabel = species.shortLabel,
            stageDetailLabel = stagePresentation.detailLabel,
            stageScale = stagePresentation.scale,
        )
    }

    fun visualForActiveCreature(active: ActiveCreatureState): StageVisual {
        return getVisualForStage(
            creatureDefinition = active.creature,
            stage = active.stage,
            eggRarity = active.eggRarity,
        )
    }

    /** Adult-stage visual for collection / list rows. */
    fun collectionVisual(creatureDefinition: CreatureDefinition): StageVisual {
        return getVisualForStage(
            creatureDefinition = creatureDefinition,
            stage = GrowthStage.ADULT,
            eggRarity = EggRarity.COMMON,
        )
    }

    private data class StagePresentation(
        val detailLabel: String,
        val scale: Float,
    )

    private fun stagePresentation(stage: GrowthStage): StagePresentation = when (stage) {
        GrowthStage.EGG -> StagePresentation(detailLabel = "Egg", scale = 0.65f)
        GrowthStage.BABY -> StagePresentation(detailLabel = "Baby", scale = 0.72f)
        GrowthStage.JUVENILE -> StagePresentation(detailLabel = "Juvenile", scale = 0.88f)
        GrowthStage.ADULT -> StagePresentation(detailLabel = "Adult", scale = 1f)
    }
}
