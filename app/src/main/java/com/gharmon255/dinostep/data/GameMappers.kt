package com.gharmon255.dinostep.data

import com.gharmon255.dinostep.data.local.entity.ActiveCreatureEntity
import com.gharmon255.dinostep.data.local.entity.CompletedCreatureEntity
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.PlayerStats

fun ActiveCreatureEntity.toDomain(): ActiveCreatureState? {
    val creature = CreatureCatalog.byId(creatureId) ?: return null
    return ActiveCreatureState(
        creature = creature,
        steps = currentSteps,
        startedAt = startedAt,
        isRevealed = isRevealed,
    )
}

fun ActiveCreatureState.toEntity(): ActiveCreatureEntity {
    return ActiveCreatureEntity(
        creatureId = creature.id,
        currentSteps = steps,
        startedAt = startedAt,
        isRevealed = isRevealed,
    )
}

fun CompletedCreatureEntity.toDomain(): CompletedCreature? {
    val creature = CreatureCatalog.byId(creatureId) ?: return null
    return CompletedCreature(
        creature = creature,
        stepsCompleted = completedStepTotal,
        completedAt = completedAt,
    )
}

fun CompletedCreature.toEntity(): CompletedCreatureEntity {
    return CompletedCreatureEntity(
        creatureId = creature.id,
        name = creature.name,
        rarity = creature.rarity.name,
        habitat = creature.habitat.name,
        completedStepTotal = stepsCompleted,
        completedAt = completedAt,
    )
}

fun PlayerStatsEntity.toDomain(): PlayerStats {
    return PlayerStats(
        totalFakeStepsAdded = totalFakeStepsAdded,
        eggsHatched = eggsHatched,
        creaturesCompleted = creaturesCompleted,
    )
}

fun PlayerStats.toEntity(): PlayerStatsEntity {
    return PlayerStatsEntity(
        totalFakeStepsAdded = totalFakeStepsAdded,
        eggsHatched = eggsHatched,
        creaturesCompleted = creaturesCompleted,
    )
}
