package com.gharmon255.dinostep.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DailyStepGoalReminderSchedulerTest {
    @Test
    fun nextReminderMillis_isLaterTodayWhenBeforeEightPm() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = calendar.timeInMillis

        val next = DailyStepGoalReminderScheduler.nextReminderMillis(now)

        assertTrue(next > now)
        calendar.timeInMillis = next
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun nextReminderMillis_isTomorrowWhenAfterEightPm() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = calendar.timeInMillis

        val next = DailyStepGoalReminderScheduler.nextReminderMillis(now)

        calendar.timeInMillis = next
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY))
        assertTrue(next - now > 20 * 60 * 60 * 1000L)
    }
}
