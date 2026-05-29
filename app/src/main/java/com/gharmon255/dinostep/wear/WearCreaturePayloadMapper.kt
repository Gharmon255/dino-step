package com.gharmon255.dinostep.wear

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearSyncEventType

object WearCreaturePayloadMapper {
    fun fromActiveCreature(
        activeCreature: ActiveCreatureState,
        eventType: WearSyncEventType,
    ): WearCreaturePayload {
        val creature = activeCreature.creature
        val nextMilestone = activeCreature.nextMilestone ?: creature.totalStepsRequired
        val stepsUntilNext = (nextMilestone - activeCreature.steps).coerceAtLeast(0)

        return WearCreaturePayload(
            creatureName = creature.name,
            displayName = activeCreature.displayName,
            stage = activeCreature.stage.name,
            currentSteps = activeCreature.steps,
            nextMilestone = nextMilestone,
            totalStepsRequired = creature.totalStepsRequired,
            progressPercent = activeCreature.progressPercent,
            stepsUntilNextMilestone = stepsUntilNext,
            isRevealed = activeCreature.isRevealed,
            displayEmoji = creature.emoji,
            eventType = eventType,
            updatedAtMillis = System.currentTimeMillis(),
        )
    }

    fun detectEventType(
        previous: ActiveCreatureState,
        current: ActiveCreatureState,
        isClaimReward: Boolean = false,
    ): WearSyncEventType {
        if (isClaimReward) {
            return WearSyncEventType.COMPLETED
        }
        if (!previous.isRevealed && current.isRevealed) {
            return WearSyncEventType.HATCHED
        }
        if (previous.stage != current.stage && current.stage != GrowthStage.EGG) {
            return WearSyncEventType.GREW
        }
        return WearSyncEventType.NONE
    }
}
