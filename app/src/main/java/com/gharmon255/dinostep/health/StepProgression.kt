package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.PlayerStats

object StepProgression {
    data class ApplyResult(
        val activeCreature: ActiveCreatureState,
        val playerStats: PlayerStats,
    )

    fun applySteps(
        activeCreature: ActiveCreatureState,
        playerStats: PlayerStats,
        amount: Int,
        countAsFake: Boolean,
    ): ApplyResult {
        if (amount <= 0) {
            return ApplyResult(activeCreature = activeCreature, playerStats = playerStats)
        }

        val wasRevealed = activeCreature.isRevealed
        val newSteps = activeCreature.steps + amount
        val nowRevealed = wasRevealed || newSteps >= activeCreature.creature.hatchStep
        val eggsHatchedDelta = if (!wasRevealed && nowRevealed) 1 else 0

        val updatedCreature = activeCreature.copy(
            steps = newSteps,
            isRevealed = nowRevealed,
        )
        val updatedStats = playerStats.copy(
            totalFakeStepsAdded = if (countAsFake) {
                playerStats.totalFakeStepsAdded + amount
            } else {
                playerStats.totalFakeStepsAdded
            },
            eggsHatched = playerStats.eggsHatched + eggsHatchedDelta,
            lifetimeStepsApplied = playerStats.lifetimeStepsApplied + amount,
        )

        return ApplyResult(
            activeCreature = updatedCreature,
            playerStats = updatedStats,
        )
    }
}
