package com.gharmon255.dinostep.garmin

import android.util.Log
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.shared.wear.toLogString
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper

/**
 * Phase 1 stub — encodes JSON and logs. Phase 2 replaces this with Garmin Mobile SDK send.
 */
class StubGarminCompanionPublisher : GarminCompanionPublisher {
    override suspend fun publishActiveCreature(
        activeCreature: ActiveCreatureState,
        eventType: WearSyncEventType,
    ): GarminPublishResult {
        val payload = WearCreaturePayloadMapper.fromActiveCreature(activeCreature, eventType)
        return publishPayload(payload)
    }

    override suspend fun publishPayload(payload: WearCreaturePayload): GarminPublishResult {
        val payloadToSend = payload.copy(updatedAtMillis = System.currentTimeMillis())
        val json = GarminCreaturePayloadJson.encode(payloadToSend)
        val message =
            "Garmin SDK not wired — JSON ready (${json.length} bytes, appId=${GarminCompanionConstants.CIQ_APPLICATION_ID})"
        Log.i(TAG, message)
        Log.d(TAG, "Garmin JSON: $json")
        Log.i(TAG, "Payload: ${payloadToSend.toLogString()}")
        return GarminPublishResult(
            success = false,
            statusMessage = message,
            jsonPayload = json,
            payloadSummary = payloadToSend.toLogString(),
        )
    }

    companion object {
        private const val TAG = "DinoStepGarminPhone"
    }
}
