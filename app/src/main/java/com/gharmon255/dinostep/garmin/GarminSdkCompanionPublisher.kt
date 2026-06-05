package com.gharmon255.dinostep.garmin

import android.util.Log
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.shared.wear.toLogString
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Publishes active creature JSON to a paired Garmin watch via Connect IQ Mobile SDK.
 * Requires Garmin Connect Mobile and the CIQ watch app (Phase 3) on the device.
 */
class GarminSdkCompanionPublisher(
    private val connectIQManager: GarminConnectIQManager,
) : GarminCompanionPublisher {
    private val iqApp = IQApp(GarminCompanionConstants.CIQ_APPLICATION_ID)

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
        val summary = payloadToSend.toLogString()

        if (!connectIQManager.hasBindAttempted || !connectIQManager.isSdkReady) {
            val initError = connectIQManager.lastInitError
            val message = when {
                !connectIQManager.hasBindAttempted ->
                    "Garmin companion not started (optional — Samsung/Wear unaffected)"
                initError != null ->
                    "Connect IQ SDK not ready: $initError"
                else ->
                    "Connect IQ SDK not ready — open Garmin Connect Mobile"
            }
            Log.w(TAG, message)
            return GarminPublishResult(
                success = false,
                statusMessage = message,
                jsonPayload = json,
                payloadSummary = summary,
                connectedDeviceCount = 0,
            )
        }

        val devices = connectIQManager.connectedDevices()
        if (devices.isEmpty()) {
            val message =
                "No connected Garmin device — sync watch in Garmin Connect and keep app running"
            Log.w(TAG, message)
            return GarminPublishResult(
                success = false,
                statusMessage = message,
                jsonPayload = json,
                payloadSummary = summary,
                connectedDeviceCount = 0,
            )
        }

        val messageMap = GarminCreaturePayloadMap.toMessageMap(payloadToSend)
        Log.i(TAG, "Sending Garmin payload (${messageMap.size} fields) to ${devices.size} device(s)")
        Log.d(TAG, "Garmin JSON (debug): $json")
        Log.i(TAG, "Payload: $summary")

        val connectIQ = connectIQManager.requireConnectIQ()
        val results = withContext(Dispatchers.Main.immediate) {
            devices.map { device ->
                sendMessage(connectIQ, device, messageMap)
            }
        }

        val successCount = results.count { it.first }
        val success = successCount > 0
        val statusMessage = if (success) {
            "Garmin send OK on $successCount/${devices.size} device(s): ${results.joinToString { it.second }}"
        } else {
            "Garmin send failed: ${results.joinToString { "${it.second}" }}"
        }
        Log.i(TAG, statusMessage)

        return GarminPublishResult(
            success = success,
            statusMessage = statusMessage,
            jsonPayload = json,
            payloadSummary = summary,
            connectedDeviceCount = devices.size,
        )
    }

    private suspend fun sendMessage(
        connectIQ: ConnectIQ,
        device: IQDevice,
        messageMap: HashMap<String, Any>,
    ): Pair<Boolean, String> = suspendCancellableCoroutine { continuation ->
        try {
            connectIQ.sendMessage(device, iqApp, messageMap) { dev, _, status ->
                val label = "${dev.friendlyName}=${status.name}"
                val success = status == ConnectIQ.IQMessageStatus.SUCCESS
                if (continuation.isActive) {
                    continuation.resume(success to label)
                }
            }
        } catch (error: InvalidStateException) {
            if (continuation.isActive) {
                continuation.resume(false to "invalid_state")
            }
        } catch (error: ServiceUnavailableException) {
            if (continuation.isActive) {
                continuation.resume(false to "service_unavailable")
            }
        }
    }

    companion object {
        private const val TAG = "DinoStepGarminPhone"
    }
}
