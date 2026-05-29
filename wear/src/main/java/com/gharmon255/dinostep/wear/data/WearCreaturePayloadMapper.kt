package com.gharmon255.dinostep.wear.data

import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage

fun WearCreaturePayload.toWatchCreatureState(): WatchCreatureState {
    return WatchCreatureState(
        creatureName = creatureName,
        displayName = displayName,
        stage = WearGrowthStage.fromRaw(stage),
        currentSteps = currentSteps,
        nextMilestone = nextMilestone,
        totalStepsRequired = totalStepsRequired,
        progressPercent = progressPercent,
        stepsUntilNextMilestone = stepsUntilNextMilestone,
        isRevealed = isRevealed,
        displayEmoji = displayEmoji,
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
