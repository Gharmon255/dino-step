package com.gharmon255.dinostep.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.gharmon255.dinostep.health.DailyActivityPenalty
import java.util.Calendar

/**
 * Reminds players to keep walking before midnight if they have not hit the daily step goal yet.
 *
 * The alarm fires once per day in the early evening. The receiver re-reads today's steps from
 * Health Connect so the reminder stays accurate even when the app was not opened recently.
 */
object DailyStepGoalReminderScheduler {
    private const val REQUEST_CODE = 4_260
    const val ACTION_DAILY_STEP_GOAL_REMINDER = "com.gharmon255.dinostep.DAILY_STEP_GOAL_REMINDER"

    fun ensureScheduled(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextReminderMillis()
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            reminderPendingIntent(context),
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(reminderPendingIntent(context))
    }

    fun updateAfterSync(context: Context, todaySteps: Int) {
        if (todaySteps >= DailyActivityPenalty.MINIMUM_DAILY_STEPS) {
            cancel(context)
        } else {
            ensureScheduled(context)
        }
    }

    internal fun nextReminderMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyStepGoalReminderReceiver::class.java).apply {
            action = ACTION_DAILY_STEP_GOAL_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val REMINDER_HOUR = 20
    private const val REMINDER_MINUTE = 0
}
