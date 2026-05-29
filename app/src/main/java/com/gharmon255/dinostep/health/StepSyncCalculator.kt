package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.model.PlayerStats

data class StepSyncDelta(
    val delta: Int,
    val currentHealthConnectSteps: Int,
    val updatedStats: PlayerStats,
)

object StepSyncCalculator {
    fun calculate(
        playerStats: PlayerStats,
        currentHealthConnectTodaySteps: Long,
    ): StepSyncDelta {
        val todayStart = StepTimeUtils.startOfTodayMillis()
        val currentSteps = currentHealthConnectTodaySteps
            .coerceAtLeast(0L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

        val lastSyncedBaseline = if (playerStats.lastSyncDayStartMillis == todayStart) {
            playerStats.lastSyncedStepTotal
        } else {
            0
        }

        val delta = currentSteps - lastSyncedBaseline
        val updatedStats = if (delta > 0) {
            playerStats.copy(
                lastSyncedStepTotal = currentSteps,
                lastSyncDayStartMillis = todayStart,
            )
        } else {
            playerStats
        }

        return StepSyncDelta(
            delta = delta.coerceAtLeast(0),
            currentHealthConnectSteps = currentSteps,
            updatedStats = updatedStats,
        )
    }
}
