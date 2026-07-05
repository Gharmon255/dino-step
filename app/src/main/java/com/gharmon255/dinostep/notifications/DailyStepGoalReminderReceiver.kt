package com.gharmon255.dinostep.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gharmon255.dinostep.DinoStepApplication
import com.gharmon255.dinostep.MainActivity
import com.gharmon255.dinostep.R
import com.gharmon255.dinostep.health.DailyActivityPenalty
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DailyStepGoalReminderReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != DailyStepGoalReminderScheduler.ACTION_DAILY_STEP_GOAL_REMINDER) {
            return
        }

        val pendingResult = goAsync()
        scope.launch {
            try {
                deliverReminderIfNeeded(context.applicationContext)
            } finally {
                DailyStepGoalReminderScheduler.ensureScheduled(context.applicationContext)
                pendingResult.finish()
            }
        }
    }

    private suspend fun deliverReminderIfNeeded(context: Context) {
        if (!canPostNotifications(context)) {
            return
        }

        val app = context.applicationContext as? DinoStepApplication ?: return
        val todaySteps = app.healthConnectRepository.readTodayStepTotal()
            .getOrNull()
            ?.toInt()
            ?: return

        if (todaySteps >= DailyActivityPenalty.MINIMUM_DAILY_STEPS) {
            DailyStepGoalReminderScheduler.cancel(context)
            return
        }

        ensureChannel(context)
        val formatter = NumberFormat.getIntegerInstance(Locale.getDefault())
        val formattedToday = formatter.format(todaySteps)
        val formattedGoal = formatter.format(DailyActivityPenalty.MINIMUM_DAILY_STEPS)
        val remaining = DailyActivityPenalty.MINIMUM_DAILY_STEPS - todaySteps
        val body =
            "You've walked $formattedToday steps today. Walk ${formatter.format(remaining)} more " +
                "($formattedGoal total) to keep your dino growing."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Keep your dino growing!")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(launchPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun launchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily step reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Evening reminders to hit your daily step goal."
        }
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val CHANNEL_ID = "daily_step_goal"
        private const val NOTIFICATION_ID = 4_261
    }
}
