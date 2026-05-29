package com.gharmon255.dinostep.wear.data

import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage

/** Placeholder shown only until the first real phone payload arrives. */
object MockWatchStateProvider {
    fun waitingForPhone(): WatchCreatureState {
        return WatchCreatureState(
            creatureName = "",
            displayName = "Mystery Egg",
            stage = WearGrowthStage.EGG,
            currentSteps = 0,
            nextMilestone = 0,
            totalStepsRequired = 0,
            progressPercent = 0f,
            stepsUntilNextMilestone = 0,
            stepsUntilNextStage = 0,
            nextStageLabel = "",
            isRevealed = false,
            displayEmoji = "🥚",
            eventType = WearSyncEventType.NONE,
            isFromPhone = false,
            lastUpdatedAtMillis = null,
        )
    }
}
