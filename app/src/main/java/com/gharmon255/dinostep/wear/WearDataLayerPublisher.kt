package com.gharmon255.dinostep.wear

import android.content.Context
import android.util.Log
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearCreaturePayloadCodec
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.shared.wear.WearSyncPaths
import com.gharmon255.dinostep.shared.wear.toLogString
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Publishes game state to the paired watch over the Wearable Data Layer.
 *
 * Phone and wear APKs share applicationId [com.gharmon255.dinostep] and the same
 * signing certificate (debug keystore from this project).
 */
class WearDataLayerPublisher(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)
    private val nodeClient = Wearable.getNodeClient(appContext)

    suspend fun publishActiveCreature(
        activeCreature: ActiveCreatureState,
        eventType: WearSyncEventType,
    ): WearSyncPublishResult {
        val payload = WearCreaturePayloadMapper.fromActiveCreature(activeCreature, eventType)
        return publishPayload(payload)
    }

    suspend fun publishPayload(payload: WearCreaturePayload): WearSyncPublishResult {
        val path = WearSyncPaths.CURRENT_CREATURE
        val payloadToSend = payload.copy(updatedAtMillis = System.currentTimeMillis())

        Log.i(TAG, "Attempting Data Layer send to path=$path")
        Log.i(TAG, "Payload: ${payloadToSend.toLogString()}")

        val connectedNodes = runCatching {
            nodeClient.connectedNodes.await()
        }.getOrElse { error ->
            Log.e(TAG, "Failed to query connected Wear nodes", error)
            emptyList()
        }

        Log.i(TAG, "Connected Wear node count=${connectedNodes.size}")
        connectedNodes.forEach { node ->
            Log.i(
                TAG,
                "Wear node id=${node.id}, displayName=${node.displayName}, isNearby=${node.isNearby}",
            )
        }

        if (connectedNodes.isEmpty()) {
            val message = "No Wear nodes connected — pair watch in Galaxy Wearable / Wear OS app"
            Log.w(TAG, message)
            return buildResult(
                success = false,
                message = message,
                connectedNodeCount = 0,
                payload = payloadToSend,
                path = path,
            )
        }

        return runCatching {
            val request = PutDataMapRequest.create(path).apply {
                dataMap.putAll(WearCreaturePayloadCodec.toDataMap(payloadToSend))
            }.asPutDataRequest().setUrgent()

            val dataItem = dataClient.putDataItem(request).await()
            val message = "Data Layer put OK (uri=${dataItem.uri}, updatedAt=${payloadToSend.updatedAtMillis})"
            Log.i(TAG, message)
            buildResult(
                success = true,
                message = message,
                connectedNodeCount = connectedNodes.size,
                payload = payloadToSend,
                path = path,
            )
        }.getOrElse { error ->
            val message = "Data Layer put failed: ${error.localizedMessage ?: error.javaClass.simpleName}"
            Log.e(TAG, message, error)
            buildResult(
                success = false,
                message = message,
                connectedNodeCount = connectedNodes.size,
                payload = payloadToSend,
                path = path,
            )
        }
    }

    private fun buildResult(
        success: Boolean,
        message: String,
        connectedNodeCount: Int,
        payload: WearCreaturePayload,
        path: String,
    ): WearSyncPublishResult {
        return WearSyncPublishResult(
            success = success,
            statusMessage = message,
            connectedNodeCount = connectedNodeCount,
            payloadSummary = payload.toLogString(),
            dataPath = path,
            payloadDisplayName = payload.displayName,
            payloadStage = payload.stage,
            payloadSteps = payload.currentSteps,
        )
    }

    companion object {
        private const val TAG = "DinoStepWearPhone"
    }
}
