package com.gharmon255.dinostep.ui.home

import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal object HomeSyncStatusText {
    fun format(
        isSyncing: Boolean,
        lastSyncTimeMillis: Long?,
        syncStatusMessage: String?,
    ): String {
        if (isSyncing) {
            return "Syncing steps…"
        }
        lastSyncTimeMillis?.let { millis ->
            val elapsed = System.currentTimeMillis() - millis
            return when {
                elapsed < 60_000L -> "Synced just now"
                elapsed < 3_600_000L -> {
                    val minutes = (elapsed / 60_000L).toInt().coerceAtLeast(1)
                    "Synced $minutes min ago"
                }
                else -> {
                    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                    "Synced at ${formatter.format(Date(millis))}"
                }
            }
        }
        return syncStatusMessage ?: "Steps sync automatically in the background"
    }
}
