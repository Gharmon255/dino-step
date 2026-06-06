package com.gharmon255.dinostep.wear.complication

import android.content.Context
import android.graphics.drawable.Icon
import com.gharmon255.dinostep.shared.visual.DrawableCreatureResolver
import com.gharmon255.dinostep.wear.R
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage

object ComplicationCreatureIcon {
    fun centerEmoji(state: WatchCreatureState): String {
        if (!state.isFromPhone) {
            return "🥚"
        }
        val emoji = state.displayEmoji.trim()
        if (emoji.isNotEmpty()) {
            return emoji
        }
        return when (state.stage) {
            WearGrowthStage.EGG -> "🥚"
            else -> "🦕"
        }
    }

    fun resolveIcon(context: Context, state: WatchCreatureState): Icon {
        val resources = context.resources
        val packageName = context.packageName
        val drawableId = when (state.stage) {
            WearGrowthStage.EGG -> DrawableCreatureResolver.eggDrawableId(
                resources = resources,
                packageName = packageName,
                eggRarityName = state.eggRarity.ifBlank { "COMMON" },
            )
            else -> DrawableCreatureResolver.stageDrawableIdFromSync(
                resources = resources,
                packageName = packageName,
                speciesId = state.speciesIdForArt,
                stageName = state.stage.name,
                stageDrawableKey = state.resolvedStageDrawableKey(),
            )
        }
        val resolvedId = if (drawableId != 0) drawableId else R.drawable.ic_complication_egg
        return Icon.createWithResource(context, resolvedId)
    }
}
