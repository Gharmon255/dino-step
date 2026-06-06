package com.gharmon255.dinostep.wear.data

import com.gharmon255.dinostep.shared.visual.CreatureAssetNames
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage

fun WearCreaturePayload.toWatchCreatureState(): WatchCreatureState {
    val normalizedCreatureId = CreatureAssetNames.normalizeCatalogSpeciesId(creatureId)
    val resolvedDrawableKey = stageDrawableKey.trim().ifBlank {
        CreatureAssetNames.stageDrawableLogicalName(normalizedCreatureId, stage).orEmpty()
    }
    return WatchCreatureState(
        creatureId = normalizedCreatureId.ifBlank { creatureId },
        creatureName = creatureName,
        displayName = displayName,
        stage = WearGrowthStage.fromRaw(stage),
        currentSteps = currentSteps,
        nextMilestone = nextMilestone,
        totalStepsRequired = totalStepsRequired,
        progressPercent = progressPercent,
        stepsUntilNextMilestone = stepsUntilNextMilestone,
        stepsUntilNextStage = stepsUntilNextStage,
        nextStageLabel = nextStageLabel,
        isRevealed = isRevealed,
        displayEmoji = displayEmoji,
        speciesShortLabel = speciesShortLabel,
        stageScale = stageScale,
        eggRarity = eggRarity,
        creatureRarity = creatureRarity,
        accentColorArgb = accentColorArgb,
        isAssetBacked = isAssetBacked,
        stageDrawableKey = resolvedDrawableKey,
        eventType = eventType,
        isFromPhone = true,
        lastUpdatedAtMillis = updatedAtMillis.takeIf { it > 0L },
    )
}

fun WearSyncEventType.toWatchLabel(): String? {
    return when (this) {
        WearSyncEventType.HATCHED -> "Hatched!"
        WearSyncEventType.GREW -> "Grew!"
        WearSyncEventType.COMPLETED -> "Completed!"
        WearSyncEventType.NONE -> null
    }
}
