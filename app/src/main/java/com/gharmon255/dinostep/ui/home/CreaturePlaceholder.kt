package com.gharmon255.dinostep.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.ui.components.CreatureStageVisual

/** @deprecated Use [CreatureStageVisual] with [ActiveCreatureState]. */
@Deprecated("Use CreatureStageVisual(activeCreature = …)")
@Composable
fun CreaturePlaceholder(
    stage: GrowthStage,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    // Legacy signature kept for any external callers; stage/emoji are ignored.
    CreatureStageVisual(
        activeCreature = ActiveCreatureState.newEgg(
            creature = com.gharmon255.dinostep.model.CreatureCatalog.tinyRaptor,
            eggRarity = com.gharmon255.dinostep.model.EggRarity.COMMON,
        ).copy(
            steps = when (stage) {
                GrowthStage.EGG -> 0
                GrowthStage.BABY -> 2_000
                GrowthStage.JUVENILE -> 6_000
                GrowthStage.ADULT -> 10_000
            },
            isRevealed = stage != GrowthStage.EGG,
        ),
        modifier = modifier,
    )
}
