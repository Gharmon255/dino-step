package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.game.ActiveCreatureState

data class DailyActivityPenaltyResult(
    val creature: ActiveCreatureState,
    val yesterdaySteps: Int,
)

object DailyActivityPenalty {
    const val MINIMUM_DAILY_STEPS = 5_000
    const val PENALTY_REMAINING_STEPS = 500

    fun applyIfNeeded(
        yesterdaySteps: Int,
        activeCreature: ActiveCreatureState,
    ): DailyActivityPenaltyResult? {
        if (yesterdaySteps >= MINIMUM_DAILY_STEPS) {
            return null
        }
        if (!activeCreature.isRevealed && activeCreature.steps <= PENALTY_REMAINING_STEPS) {
            return null
        }

        return DailyActivityPenaltyResult(
            creature = activeCreature.copy(
                steps = PENALTY_REMAINING_STEPS,
                isRevealed = false,
            ),
            yesterdaySteps = yesterdaySteps,
        )
    }
}
