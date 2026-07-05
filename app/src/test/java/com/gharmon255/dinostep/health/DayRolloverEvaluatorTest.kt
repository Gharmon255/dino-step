package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.model.PlayerStats
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DayRolloverEvaluatorTest {
    @Test
    fun resolveYesterdaySteps_prefersFreshHealthReadOverPartialCachedSync() = runBlocking {
        val yesterdayStart = StepTimeUtils.startOfYesterdayMillis()
        val stats = PlayerStats(lastSyncedStepTotal = 2_500, lastSyncDayStartMillis = yesterdayStart)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { 7_200 },
        )

        assertEquals(7_200, resolved)
    }

    @Test
    fun resolveYesterdaySteps_returnsNullWhenNeitherSourceIsAvailable() = runBlocking {
        val stats = PlayerStats(lastSyncedStepTotal = 0, lastSyncDayStartMillis = 0L)

        val resolved = DayRolloverEvaluator.resolveYesterdaySteps(
            playerStats = stats,
            fetchYesterdaySteps = { null },
        )

        assertNull(resolved)
    }
}
