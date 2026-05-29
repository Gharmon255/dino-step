package com.gharmon255.dinostep.wear

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.shared.visual.RarityTheme
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearStageProgress
import com.gharmon255.dinostep.shared.wear.WearSyncEventType

object WearCreaturePayloadMapper {
    fun fromActiveCreature(
        activeCreature: ActiveCreatureState,
        eventType: WearSyncEventType,
    ): WearCreaturePayload {
        val creature = activeCreature.creature
        val nextMilestone = activeCreature.nextMilestone ?: creature.totalStepsRequired
        val stageProgress = WearStageProgress.calculate(
            stageName = activeCreature.stage.name,
            currentSteps = activeCreature.steps,
            hatchStep = creature.hatchStep,
            juvenileStep = creature.juvenileStep,
            totalStepsRequired = creature.totalStepsRequired,
        )

        val visual = CreatureVisualMapper.visualForActiveCreature(activeCreature)

        return WearCreaturePayload(
            creatureName = creature.name,
            displayName = activeCreature.displayName,
            stage = activeCreature.stage.name,
            currentSteps = activeCreature.steps,
            nextMilestone = nextMilestone,
            totalStepsRequired = creature.totalStepsRequired,
            progressPercent = activeCreature.progressPercent,
            stepsUntilNextMilestone = stageProgress.stepsUntilNextStage,
            stepsUntilNextStage = stageProgress.stepsUntilNextStage,
            nextStageLabel = stageProgress.nextStageLabel,
            isRevealed = activeCreature.isRevealed,
            displayEmoji = visual.displayEmoji,
            speciesShortLabel = visual.speciesShortLabel,
            stageScale = visual.stageScale,
            eggRarity = activeCreature.eggRarity.name,
            creatureRarity = if (activeCreature.isRevealed) creature.rarity.name else "",
            accentColorArgb = RarityTheme.resolveAccentArgb(
                isRevealed = activeCreature.isRevealed,
                eggRarityName = activeCreature.eggRarity.name,
                creatureRarityName = creature.rarity.name.takeIf { activeCreature.isRevealed },
            ),
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
