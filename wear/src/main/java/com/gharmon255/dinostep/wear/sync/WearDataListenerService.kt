package com.gharmon255.dinostep.wear.sync

import android.util.Log
import com.gharmon255.dinostep.wear.WearApplication
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService

/**
 * Receives Data Layer updates while the watch app process is alive (including background).
 */
class WearDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i(TAG, "WearDataListenerService onDataChanged (${dataEvents.count} events)")
        val repository = (application as WearApplication).watchStateRepository
        repository.handleDataEvents(dataEvents)
    }

    companion object {
        private const val TAG = "DinoStepWearWatch"
    }
}
