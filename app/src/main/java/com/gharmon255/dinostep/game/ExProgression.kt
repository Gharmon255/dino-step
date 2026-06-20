package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.EggRarity
import kotlin.math.floor

object ExProgression {
    const val ROSTER_EX_RATE = 0.05
    const val MAX_EX_LEVEL = 50

    fun dripAmount(stepAmount: Int): Int {
        if (stepAmount <= 0) {
            return 0
        }
        return floor(stepAmount * ROSTER_EX_RATE).toInt()
    }

    fun exLevelFromSteps(exSteps: Int): Int {
        if (exSteps <= 0) {
            return 1
        }
        var level = 1
        var accumulated = 0
        while (level < MAX_EX_LEVEL) {
            val required = stepsRequiredForLevel(level + 1)
            if (accumulated + required > exSteps) {
                break
            }
            accumulated += required
            level += 1
        }
        return level
    }

    fun stepsRequiredForLevel(level: Int): Int {
        require(level >= 2) { "Level must be >= 2" }
        return 500 + (level * 100)
    }

    fun applyDrip(collection: List<CompletedCreature>, stepAmount: Int): List<CompletedCreature> {
        val drip = dripAmount(stepAmount)
        if (drip <= 0 || collection.isEmpty()) {
            return collection
        }
        return collection.map { creature ->
            val nextSteps = creature.exSteps + drip
            creature.copy(
                exSteps = nextSteps,
                exLevel = exLevelFromSteps(nextSteps),
            )
        }
    }

    fun newCompletedCreature(
        creature: com.gharmon255.dinostep.model.CreatureDefinition,
        stepsCompleted: Int,
        completedAt: Long,
        eggRarityAtHatch: EggRarity,
        nickname: String? = null,
        id: Long = 0L,
    ): CompletedCreature {
        return CompletedCreature(
            id = id,
            creature = creature,
            stepsCompleted = stepsCompleted,
            completedAt = completedAt,
            nickname = nickname,
            eggRarityAtHatch = eggRarityAtHatch,
            exSteps = 0,
            exLevel = 1,
        )
    }
}
