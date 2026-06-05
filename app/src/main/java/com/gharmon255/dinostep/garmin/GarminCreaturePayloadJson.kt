package com.gharmon255.dinostep.garmin

import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import org.json.JSONObject

/**
 * Encodes [WearCreaturePayload] to the Garmin JSON contract (v1).
 * See dino-step-garmin/docs/GARMIN_SYNC_CONTRACT.md
 */
object GarminCreaturePayloadJson {
    fun encode(payload: WearCreaturePayload): String {
        return JSONObject().apply {
            put("v", GarminCompanionConstants.PAYLOAD_SCHEMA_VERSION)
            if (payload.creatureId.isNotBlank()) {
                put("creature_id", payload.creatureId)
            }
            put("display_name", payload.displayName)
            put("stage", payload.stage)
            put("current_steps", payload.currentSteps)
            put("progress_percent", payload.progressPercent.toDouble())
            put("steps_until_next_stage", payload.stepsUntilNextStage)
            put("next_stage_label", payload.nextStageLabel)
            put("egg_rarity", payload.eggRarity)
            if (payload.creatureRarity.isNotBlank()) {
                put("creature_rarity", payload.creatureRarity)
            }
            put("display_emoji", payload.displayEmoji)
            put("accent_color_argb", payload.accentColorArgb)
            put("event_type", payload.eventType.name)
            put("updated_at", payload.updatedAtMillis)
        }.toString()
    }
}
