package com.gharmon255.dinostep.wear.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearCreaturePayloadCodec
import com.gharmon255.dinostep.shared.wear.WearSyncPaths
import com.gharmon255.dinostep.shared.wear.toLogString
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WatchStateRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)
    private val nodeClient = Wearable.getNodeClient(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(MockWatchStateProvider.waitingForPhone())
    val state: StateFlow<WatchCreatureState> = _state.asStateFlow()

    private var isListening = false

    private val dataListener = DataClient.OnDataChangedListener { events ->
        Log.d(TAG, "DataClient listener fired (${events.count} events)")
        handleDataEvents(events)
    }

    private val dataLayerUri: Uri = WearSyncPaths.createDataItemsQueryUri()
    private val dataLayerFilter: Int = DataClient.FILTER_PREFIX

    fun startListening() {
        if (isListening) {
            return
        }
        isListening = true
        dataClient.addListener(dataListener, dataLayerUri, dataLayerFilter)
        scope.launch {
            loadInitialState()
        }
        Log.i(
            TAG,
            "listener started for uri=$dataLayerUri path=${WearSyncPaths.CURRENT_CREATURE}",
        )
    }

    fun stopListening() {
        if (!isListening) {
            return
        }
        isListening = false
        dataClient.removeListener(dataListener)
        Log.i(TAG, "listener stopped")
    }

    fun handleDataEvents(events: DataEventBuffer) {
        try {
            for (event in events) {
                val uri = event.dataItem.uri
                val path = uri.path
                Log.i(TAG, "received path=$path uri=$uri type=${event.type}")

                if (event.type != com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) {
                    continue
                }

                if (!WearSyncPaths.matchesDataPath(path)) {
                    Log.d(TAG, "ignored path=$path")
                    continue
                }

                val payload = parsePayloadFromItem(event.dataItem, path) ?: continue
                applyPhonePayload(payload.toWatchCreatureState())
            }
        } finally {
            events.release()
        }
    }

    private fun parsePayloadFromItem(
        dataItem: com.google.android.gms.wearable.DataItem,
        path: String?,
    ): WearCreaturePayload? {
        val payload = WearCreaturePayloadCodec.fromDataMap(
            DataMapItem.fromDataItem(dataItem).dataMap,
        )
        if (payload == null) {
            Log.w(TAG, "could not parse payload at path=$path")
            return null
        }
        Log.i(TAG, "parsed payload: ${payload.toLogString()}")
        return payload
    }

    private fun applyPhonePayload(watchState: WatchCreatureState) {
        _state.value = watchState
        Log.i(
            TAG,
            "sync state updated: creatureId=${watchState.speciesIdForArt.ifBlank { "(legacy)" }}, " +
                "displayName=${watchState.displayName}, stage=${watchState.stage}, " +
                "progress=${watchState.progressPercent}%, fromPhone=${watchState.isFromPhone}, " +
                "updatedAt=${watchState.lastUpdatedAtMillis}",
        )
    }

    private suspend fun loadInitialState() {
        runCatching {
            val candidates = mutableListOf<WearCreaturePayload>()
            val queryUris = buildList {
                add(WearSyncPaths.createDataItemsQueryUri())
                add(WearSyncPaths.createDataItemsPrefixUri())
                add(WearSyncPaths.createAllDataItemsUri())
                runCatching {
                    val localNode = nodeClient.localNode.await()
                    add(WearSyncPaths.createDataItemUriForNode(localNode.id))
                    Log.i(TAG, "local watch node id=${localNode.id}")
                }.onFailure { error ->
                    Log.w(TAG, "could not resolve local watch node", error)
                }
            }

            var totalItemsScanned = 0
            for (queryUri in queryUris.distinct()) {
                Log.i(TAG, "querying DataItems on launch: uri=$queryUri")
                val items = dataClient.getDataItems(queryUri).await()
                try {
                    for (item in items) {
                        totalItemsScanned++
                        val path = item.uri.path
                        Log.i(TAG, "launch DataItem uri=${item.uri} path=$path")
                        if (!WearSyncPaths.matchesDataPath(path)) {
                            Log.d(TAG, "ignored path=$path")
                            continue
                        }
                        val payload = parsePayloadFromItem(item, path)
                        if (payload != null) {
                            candidates.add(payload)
                        }
                    }
                } finally {
                    items.release()
                }
            }

            Log.i(
                TAG,
                "current DataItems found on launch: scanned=$totalItemsScanned, " +
                    "matching=${candidates.size} for path=${WearSyncPaths.CURRENT_CREATURE}",
            )

            val best = candidates.maxByOrNull { it.updatedAtMillis }
            if (best != null) {
                applyPhonePayload(best.toWatchCreatureState())
            } else {
                Log.w(
                    TAG,
                    "no phone payload on launch at ${WearSyncPaths.CURRENT_CREATURE}; waiting for sync",
                )
            }
        }.onFailure { error ->
            Log.w(TAG, "initial Data Layer load failed; waiting for sync", error)
        }
    }

    companion object {
        private const val TAG = "DinoStepWearWatch"
    }
}
