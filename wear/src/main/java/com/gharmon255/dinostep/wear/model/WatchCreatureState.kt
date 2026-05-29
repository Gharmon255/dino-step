package com.gharmon255.dinostep.wear.model

import com.gharmon255.dinostep.shared.wear.WearSyncEventType

data class WatchCreatureState(
    val creatureName: String,
    val displayName: String,
    val stage: WearGrowthStage,
    val currentSteps: Int,
    val nextMilestone: Int,
    val totalStepsRequired: Int,
    val progressPercent: Float,
    val stepsUntilNextMilestone: Int,
    val isRevealed: Boolean,
    val displayEmoji: String,
    val eventType: WearSyncEventType = WearSyncEventType.NONE,
    val isFromPhone: Boolean = false,
    val lastUpdatedAtMillis: Long? = null,
) {
    val stageLabel: String
        get() = stage.name

    val syncStatusMessage: String
        get() = if (isFromPhone) "Synced from phone" else "Waiting for phone sync..."
}
