package com.gharmon255.dinostep.shared.wear

import com.google.android.gms.wearable.DataMap

data class WearCreaturePayload(
    val creatureName: String,
    val displayName: String,
    val stage: String,
    val currentSteps: Int,
    val nextMilestone: Int,
    val totalStepsRequired: Int,
    val progressPercent: Float,
    val stepsUntilNextMilestone: Int,
    val isRevealed: Boolean,
    val displayEmoji: String,
    val eventType: WearSyncEventType,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

object WearCreaturePayloadCodec {
    private const val KEY_CREATURE_NAME = "creature_name"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_STAGE = "stage"
    private const val KEY_CURRENT_STEPS = "current_steps"
    private const val KEY_NEXT_MILESTONE = "next_milestone"
    private const val KEY_TOTAL_STEPS_REQUIRED = "total_steps_required"
    private const val KEY_PROGRESS_PERCENT = "progress_percent"
    private const val KEY_STEPS_UNTIL_NEXT = "steps_until_next_milestone"
    private const val KEY_IS_REVEALED = "is_revealed"
    private const val KEY_DISPLAY_EMOJI = "display_emoji"
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
            putBoolean(KEY_IS_REVEALED, payload.isRevealed)
            putString(KEY_DISPLAY_EMOJI, payload.displayEmoji)
            putString(KEY_EVENT_TYPE, payload.eventType.name)
            putLong(KEY_UPDATED_AT, payload.updatedAtMillis)
        }
    }

    fun fromDataMap(dataMap: DataMap): WearCreaturePayload? {
        if (!dataMap.containsKey(KEY_DISPLAY_NAME)) {
            return null
        }

        return WearCreaturePayload(
            creatureName = dataMap.getString(KEY_CREATURE_NAME, ""),
            displayName = dataMap.getString(KEY_DISPLAY_NAME, "Mystery Egg"),
            stage = dataMap.getString(KEY_STAGE, "EGG"),
            currentSteps = dataMap.getInt(KEY_CURRENT_STEPS, 0),
            nextMilestone = dataMap.getInt(KEY_NEXT_MILESTONE, 0),
            totalStepsRequired = dataMap.getInt(KEY_TOTAL_STEPS_REQUIRED, 0),
            progressPercent = dataMap.getFloat(KEY_PROGRESS_PERCENT, 0f),
            stepsUntilNextMilestone = dataMap.getInt(KEY_STEPS_UNTIL_NEXT, 0),
            isRevealed = dataMap.getBoolean(KEY_IS_REVEALED, false),
            displayEmoji = dataMap.getString(KEY_DISPLAY_EMOJI, "🥚"),
            eventType = WearSyncEventType.fromRaw(dataMap.getString(KEY_EVENT_TYPE)),
            updatedAtMillis = dataMap.getLong(KEY_UPDATED_AT, 0L),
        )
    }
}
