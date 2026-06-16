package com.gharmon255.dinostep.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gharmon255.dinostep.MainActivity
import com.gharmon255.dinostep.R
import com.gharmon255.dinostep.health.DailyActivityPenalty
import java.text.NumberFormat
import java.util.Locale

class InactivityPenaltyNotifier(
    private val context: Context,
) {
    fun notify(yesterdaySteps: Int) {
        if (!canPostNotifications()) {
            return
        }

        ensureChannel()
        val formattedSteps = NumberFormat.getIntegerInstance(Locale.getDefault()).format(yesterdaySteps)
        val body =
            "You walked $formattedSteps steps yesterday. Walk at least " +
                "${NumberFormat.getIntegerInstance(Locale.getDefault()).format(DailyActivityPenalty.MINIMUM_DAILY_STEPS)} " +
                "steps a day to keep growing. Your dino is back in an egg with " +
                "${DailyActivityPenalty.PENALTY_REMAINING_STEPS} steps of progress."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Your dino needs more steps!")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(launchPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun launchPendingIntent(): PendingIntent {
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

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Activity reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Alerts when you miss the daily step goal."
        }
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val CHANNEL_ID = "activity_penalty"
        private const val NOTIFICATION_ID = 4_250
    }
}
