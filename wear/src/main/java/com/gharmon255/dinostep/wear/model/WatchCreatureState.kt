package com.gharmon255.dinostep.wear.model

import com.gharmon255.dinostep.shared.wear.WearStageProgress
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import java.text.NumberFormat
import java.util.Locale

data class WatchCreatureState(
    val creatureName: String,
    val displayName: String,
    val stage: WearGrowthStage,
    val currentSteps: Int,
    val nextMilestone: Int,
    val totalStepsRequired: Int,
    val progressPercent: Float,
    val stepsUntilNextMilestone: Int,
    val stepsUntilNextStage: Int = 0,
    val nextStageLabel: String = "",
    val isRevealed: Boolean,
    val displayEmoji: String,
    val speciesShortLabel: String = "",
    val stageScale: Float = 1f,
    val eggRarity: String = "COMMON",
    val creatureRarity: String = "",
    val accentColorArgb: Long = com.gharmon255.dinostep.shared.visual.RarityTheme.COMMON_ARGB,
    val eventType: WearSyncEventType = WearSyncEventType.NONE,
    val isFromPhone: Boolean = false,
    val lastUpdatedAtMillis: Long? = null,
) {
    val stageLabel: String
        get() = stage.name.lowercase(Locale.getDefault()).replaceFirstChar { char ->
            char.titlecase(Locale.getDefault())
        }

    val syncStatusMessage: String
        get() = if (isFromPhone) "Synced" else "Waiting"

    fun stepsUntilNextStageDisplay(numberFormat: NumberFormat): String {
        if (!isFromPhone) {
            return "Waiting for sync"
        }

        val steps = resolvedStepsUntilNextStage()
        val label = resolvedNextStageLabel()

        return WearStageProgress.formatDisplayLine(
            stepsUntilNextStage = steps,
            nextStageLabel = label,
            formattedSteps = numberFormat.format(steps),
        )
    }

    private fun resolvedStepsUntilNextStage(): Int {
        if (stepsUntilNextStage > 0 || nextStageLabel.isNotBlank()) {
            return stepsUntilNextStage
        }
        return stepsUntilNextMilestone
    }

    private fun resolvedNextStageLabel(): String {
        if (nextStageLabel.isNotBlank()) {
            return nextStageLabel
        }
        return when (stage) {
            WearGrowthStage.EGG -> "hatch"
            WearGrowthStage.BABY -> "juvenile"
            WearGrowthStage.JUVENILE -> "adult"
            WearGrowthStage.ADULT -> WearStageProgress.LABEL_READY_TO_CLAIM
        }
    }
}
