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
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.GrowthStage

class StageMilestoneNotifier(
    private val context: Context,
) {
    fun notifyIfNeeded(previous: ActiveCreatureState, current: ActiveCreatureState) {
        val milestone = detectMilestone(previous, current) ?: return
        if (!canPostNotifications()) {
            return
        }

        ensureChannel()
        val (title, body) = messageFor(milestone, current)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(launchPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_BASE + milestone.ordinal,
            notification,
        )
    }

    private fun detectMilestone(
        previous: ActiveCreatureState,
        current: ActiveCreatureState,
    ): StageMilestone? {
        if (!previous.isRevealed && current.isRevealed) {
            return StageMilestone.HATCHED
        }
        if (previous.stage == current.stage) {
            return null
        }
        return when (current.stage) {
            GrowthStage.JUVENILE -> StageMilestone.GREW_TO_JUVENILE
            GrowthStage.ADULT -> StageMilestone.GREW_TO_ADULT
            else -> null
        }
    }

    private fun messageFor(
        milestone: StageMilestone,
        current: ActiveCreatureState,
    ): Pair<String, String> {
        val name = current.creature.name
        return when (milestone) {
            StageMilestone.HATCHED -> {
                "Egg hatched!" to "Meet $name! Keep walking to help them grow."
            }
            StageMilestone.GREW_TO_JUVENILE -> {
                "$name is growing up!" to "Your dino reached the juvenile stage."
            }
            StageMilestone.GREW_TO_ADULT -> {
                "Fully grown!" to "$name is ready. Open Stepasaurus to claim your reward egg."
            }
        }
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
            "Creature progress",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Alerts when your egg hatches or your dino grows."
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

    private enum class StageMilestone {
        HATCHED,
        GREW_TO_JUVENILE,
        GREW_TO_ADULT,
    }

    companion object {
        private const val CHANNEL_ID = "creature_progress"
        private const val NOTIFICATION_ID_BASE = 4_200
    }
}
