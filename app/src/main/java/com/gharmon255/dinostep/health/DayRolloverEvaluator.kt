package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.data.AppExperiencePreferences
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.PlayerStats

data class DayRolloverOutcome(
    val activeCreature: ActiveCreatureState,
    val penalty: DailyActivityPenaltyResult? = null,
)

object DayRolloverEvaluator {
    suspend fun evaluateIfNeeded(
        experience: AppExperiencePreferences,
        activeCreature: ActiveCreatureState,
        playerStats: PlayerStats,
        fetchYesterdaySteps: suspend () -> Int?,
    ): DayRolloverOutcome {
        val todayStart = StepTimeUtils.startOfTodayMillis()
        val lastEvaluatedDay = experience.lastActivityEvaluationDayStartMillis()

        if (lastEvaluatedDay >= todayStart) {
            return DayRolloverOutcome(activeCreature = activeCreature)
        }

        var creature = activeCreature
        var penalty: DailyActivityPenaltyResult? = null

        if (lastEvaluatedDay > 0L) {
            val yesterdaySteps = resolveYesterdaySteps(
                playerStats = playerStats,
                fetchYesterdaySteps = fetchYesterdaySteps,
            )
            if (yesterdaySteps != null) {
                penalty = DailyActivityPenalty.applyIfNeeded(
                    yesterdaySteps = yesterdaySteps,
                    activeCreature = creature,
                )
                penalty?.let { creature = it.creature }
            }
        }

        experience.setLastActivityEvaluationDayStartMillis(todayStart)
        return DayRolloverOutcome(
            activeCreature = creature,
            penalty = penalty,
        )
    }

    /**
     * Returns yesterday's step total for the inactivity check.
     *
     * Always prefers a fresh Health Connect read when available, because a partial in-app sync
     * from earlier in the day can under-count steps the player earned later without reopening
     * the app. When Health Connect cannot be read, falls back to the cached sync total for
     * yesterday. Returns null when neither source is available so callers skip the penalty.
     */
    internal suspend fun resolveYesterdaySteps(
        playerStats: PlayerStats,
        fetchYesterdaySteps: suspend () -> Int?,
    ): Int? {
        val yesterdayStart = StepTimeUtils.startOfYesterdayMillis()
        val cachedYesterdayTotal = if (playerStats.lastSyncDayStartMillis == yesterdayStart) {
            playerStats.lastSyncedStepTotal
        } else {
            null
        }

        val fetched = fetchYesterdaySteps()
        return when {
            fetched != null && cachedYesterdayTotal != null -> maxOf(fetched, cachedYesterdayTotal)
            fetched != null -> fetched
            cachedYesterdayTotal != null -> cachedYesterdayTotal
            else -> null
        }
    }
}
