package com.gharmon255.dinostep.garmin

import com.gharmon255.dinostep.shared.wear.WearCreaturePayload

/**
 * Maps [WearCreaturePayload] to a HashMap for Connect IQ `sendMessage`.
 * Watch receives a Monkey C Dictionary (no JSON parsing required).
 */
object GarminCreaturePayloadMap {
    fun toMessageMap(payload: WearCreaturePayload): HashMap<String, Any> {
        val map = hashMapOf<String, Any>(
            "v" to GarminCompanionConstants.PAYLOAD_SCHEMA_VERSION,
            "display_name" to payload.displayName,
            "stage" to payload.stage,
            "current_steps" to payload.currentSteps,
            "progress_percent" to payload.progressPercent.toDouble(),
            "steps_until_next_stage" to payload.stepsUntilNextStage,
            "next_stage_label" to payload.nextStageLabel,
            "egg_rarity" to payload.eggRarity,
            "display_emoji" to payload.displayEmoji,
            "accent_color_argb" to payload.accentColorArgb,
            "event_type" to payload.eventType.name,
            "updated_at" to payload.updatedAtMillis,
        )
        if (payload.creatureId.isNotBlank()) {
            map["creature_id"] = payload.creatureId
        }
        if (payload.creatureRarity.isNotBlank()) {
            map["creature_rarity"] = payload.creatureRarity
        }
        return map
    }
}
