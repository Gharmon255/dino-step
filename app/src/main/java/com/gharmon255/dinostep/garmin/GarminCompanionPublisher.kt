package com.gharmon255.dinostep.garmin

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType

interface GarminCompanionPublisher {
    suspend fun publishActiveCreature(
        activeCreature: ActiveCreatureState,
        eventType: WearSyncEventType,
    ): GarminPublishResult

    suspend fun publishPayload(payload: WearCreaturePayload): GarminPublishResult
}
