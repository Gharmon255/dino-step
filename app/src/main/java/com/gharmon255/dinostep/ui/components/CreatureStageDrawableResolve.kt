package com.gharmon255.dinostep.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.shared.visual.CreatureAssetNames
import com.gharmon255.dinostep.shared.visual.DrawableCreatureResolver

/**
 * Phone-side helper: resolves `@DrawableRes` id for egg or dino stage from stable [speciesId].
 */
object CreatureStageDrawableResolve {
    @Composable
    fun resolveDrawableId(
        speciesId: String,
        stage: GrowthStage,
        eggRarity: EggRarity,
    ): Int {
        val resources = LocalContext.current.resources
        val packageName = LocalContext.current.packageName
        return remember(speciesId, stage, eggRarity, packageName) {
            when (stage) {
                GrowthStage.EGG -> DrawableCreatureResolver.eggDrawableId(
                    resources = resources,
                    packageName = packageName,
                    eggRarityName = eggRarity.name,
                )
                else -> {
                    val drawableKey = CreatureAssetNames.stageDrawableLogicalName(speciesId, stage.name).orEmpty()
                    DrawableCreatureResolver.stageDrawableIdFromSync(
                        resources = resources,
                        packageName = packageName,
                        speciesId = speciesId,
                        stageName = stage.name,
                        stageDrawableKey = drawableKey,
                    )
                }
            }
        }
    }

    @Composable
    fun resolveAdultDrawableId(speciesId: String): Int {
        val resources = LocalContext.current.resources
        val packageName = LocalContext.current.packageName
        return remember(speciesId, packageName) {
            val drawableKey = CreatureAssetNames.stageDrawableLogicalName(
                speciesId,
                GrowthStage.ADULT.name,
            ).orEmpty()
            DrawableCreatureResolver.stageDrawableIdFromSync(
                resources = resources,
                packageName = packageName,
                speciesId = speciesId,
                stageName = GrowthStage.ADULT.name,
                stageDrawableKey = drawableKey,
            )
        }
    }
}
