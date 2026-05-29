package com.gharmon255.dinostep.shared.wear

import com.google.android.gms.wearable.DataMap
import com.gharmon255.dinostep.shared.visual.RarityTheme

data class WearCreaturePayload(
    val creatureName: String,
    val displayName: String,
    val stage: String,
    val currentSteps: Int,
    val nextMilestone: Int,
    val totalStepsRequired: Int,
    val progressPercent: Float,
    val stepsUntilNextMilestone: Int,
    val stepsUntilNextStage: Int,
    val nextStageLabel: String,
    val isRevealed: Boolean,
    val displayEmoji: String,
    val speciesShortLabel: String = "",
    val stageScale: Float = 1f,
    val eggRarity: String = "COMMON",
    val creatureRarity: String = "",
    val accentColorArgb: Long = RarityTheme.COMMON_ARGB,
    val eventType: WearSyncEventType,
    val updatedAtMillis: Long = System.currentTimeMillis(),
) {
    /** @deprecated Use [nextStageLabel] */
    val nextStageGoal: String
        get() = if (nextStageLabel == WearStageProgress.LABEL_READY_TO_CLAIM) "claim" else nextStageLabel
}

object WearCreaturePayloadCodec {
    private const val KEY_CREATURE_NAME = "creature_name"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_STAGE = "stage"
    private const val KEY_CURRENT_STEPS = "current_steps"
    private const val KEY_NEXT_MILESTONE = "next_milestone"
    private const val KEY_TOTAL_STEPS_REQUIRED = "total_steps_required"
    private const val KEY_PROGRESS_PERCENT = "progress_percent"
    private const val KEY_STEPS_UNTIL_NEXT = "steps_until_next_milestone"
    private const val KEY_STEPS_UNTIL_NEXT_STAGE = "steps_until_next_stage"
    private const val KEY_NEXT_STAGE_LABEL = "next_stage_label"
    private const val KEY_NEXT_STAGE_GOAL = "next_stage_goal"
    private const val KEY_IS_REVEALED = "is_revealed"
    private const val KEY_DISPLAY_EMOJI = "display_emoji"
    private const val KEY_SPECIES_SHORT_LABEL = "species_short_label"
    private const val KEY_STAGE_SCALE = "stage_scale"
    private const val KEY_EGG_RARITY = "egg_rarity"
    private const val KEY_CREATURE_RARITY = "creature_rarity"
    private const val KEY_ACCENT_COLOR = "accent_color_argb"
    private const val KEY_EVENT_TYPE = "event_type"
    private const val KEY_UPDATED_AT = "updated_at"

    fun toDataMap(payload: WearCreaturePayload): DataMap {
        return DataMap().apply {
            putString(KEY_CREATURE_NAME, payload.creatureName)
            putString(KEY_DISPLAY_NAME, payload.displayName)
            putString(KEY_STAGE, payload.stage)
            putInt(KEY_CURRENT_STEPS, payload.currentSteps)
            putInt(KEY_NEXT_MILESTONE, payload.nextMilestone)
            putInt(KEY_TOTAL_STEPS_REQUIRED, payload.totalStepsRequired)
            putFloat(KEY_PROGRESS_PERCENT, payload.progressPercent)
            putInt(KEY_STEPS_UNTIL_NEXT, payload.stepsUntilNextMilestone)
            putInt(KEY_STEPS_UNTIL_NEXT_STAGE, payload.stepsUntilNextStage)
            putString(KEY_NEXT_STAGE_LABEL, payload.nextStageLabel)
            putString(KEY_NEXT_STAGE_GOAL, payload.nextStageGoal)
            putBoolean(KEY_IS_REVEALED, payload.isRevealed)
            putString(KEY_DISPLAY_EMOJI, payload.displayEmoji)
            putString(KEY_SPECIES_SHORT_LABEL, payload.speciesShortLabel)
            putFloat(KEY_STAGE_SCALE, payload.stageScale)
            putString(KEY_EGG_RARITY, payload.eggRarity)
            putString(KEY_CREATURE_RARITY, payload.creatureRarity)
            putLong(KEY_ACCENT_COLOR, payload.accentColorArgb)
            putString(KEY_EVENT_TYPE, payload.eventType.name)
            putLong(KEY_UPDATED_AT, payload.updatedAtMillis)
        }
    }

    fun fromDataMap(dataMap: DataMap): WearCreaturePayload? {
        if (!dataMap.containsKey(KEY_DISPLAY_NAME)) {
            return null
        }

        val stage = dataMap.getString(KEY_STAGE, "EGG")
        val legacyStepsUntil = dataMap.getInt(KEY_STEPS_UNTIL_NEXT, -1)
        val legacyGoal = dataMap.getString(KEY_NEXT_STAGE_GOAL, "")

        val stageProgress = if (dataMap.containsKey(KEY_STEPS_UNTIL_NEXT_STAGE) &&
            dataMap.containsKey(KEY_NEXT_STAGE_LABEL)
        ) {
            WearStageProgress.Info(
                stepsUntilNextStage = dataMap.getInt(KEY_STEPS_UNTIL_NEXT_STAGE, 0),
                nextStageLabel = dataMap.getString(KEY_NEXT_STAGE_LABEL, ""),
            )
        } else {
            resolveLegacyStageProgress(
                stage = stage,
                legacyStepsUntil = legacyStepsUntil,
                legacyGoal = legacyGoal,
            )
        }

        val stepsUntilNextMilestone = if (legacyStepsUntil >= 0) {
            legacyStepsUntil
        } else {
            stageProgress.stepsUntilNextStage
        }

        return WearCreaturePayload(
            creatureName = dataMap.getString(KEY_CREATURE_NAME, ""),
            displayName = dataMap.getString(KEY_DISPLAY_NAME, "Mystery Egg"),
            stage = stage,
            currentSteps = dataMap.getInt(KEY_CURRENT_STEPS, 0),
            nextMilestone = dataMap.getInt(KEY_NEXT_MILESTONE, 0),
            totalStepsRequired = dataMap.getInt(KEY_TOTAL_STEPS_REQUIRED, 0),
            progressPercent = dataMap.getFloat(KEY_PROGRESS_PERCENT, 0f),
            stepsUntilNextMilestone = stepsUntilNextMilestone,
            stepsUntilNextStage = stageProgress.stepsUntilNextStage,
            nextStageLabel = stageProgress.nextStageLabel,
            isRevealed = dataMap.getBoolean(KEY_IS_REVEALED, false),
            displayEmoji = dataMap.getString(KEY_DISPLAY_EMOJI, "🥚"),
            speciesShortLabel = dataMap.getString(KEY_SPECIES_SHORT_LABEL, ""),
            stageScale = dataMap.getFloat(KEY_STAGE_SCALE, 1f).coerceIn(0.5f, 1f),
            eggRarity = dataMap.getString(KEY_EGG_RARITY, "COMMON"),
            creatureRarity = dataMap.getString(KEY_CREATURE_RARITY, ""),
            accentColorArgb = if (dataMap.containsKey(KEY_ACCENT_COLOR)) {
                dataMap.getLong(KEY_ACCENT_COLOR)
            } else {
                RarityTheme.resolveAccentArgb(
                    isRevealed = dataMap.getBoolean(KEY_IS_REVEALED, false),
                    eggRarityName = dataMap.getString(KEY_EGG_RARITY, "COMMON"),
                    creatureRarityName = dataMap.getString(KEY_CREATURE_RARITY, ""),
                )
            },
            eventType = WearSyncEventType.fromRaw(dataMap.getString(KEY_EVENT_TYPE)),
            updatedAtMillis = dataMap.getLong(KEY_UPDATED_AT, 0L),
        )
    }

    private fun resolveLegacyStageProgress(
        stage: String,
        legacyStepsUntil: Int,
        legacyGoal: String,
    ): WearStageProgress.Info {
        val label = when {
            stage.equals("ADULT", ignoreCase = true) -> WearStageProgress.LABEL_READY_TO_CLAIM
            legacyGoal.isNotBlank() -> legacyGoal
            stage.equals("EGG", ignoreCase = true) -> "hatch"
            stage.equals("BABY", ignoreCase = true) -> "juvenile"
            stage.equals("JUVENILE", ignoreCase = true) -> "adult"
            else -> WearStageProgress.LABEL_READY_TO_CLAIM
        }
        return WearStageProgress.Info(
            stepsUntilNextStage = legacyStepsUntil.coerceAtLeast(0),
            nextStageLabel = label,
        )
    }
}
