package com.gharmon255.dinostep.cloud

import android.content.Context

class CloudSyncPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var localRevision: Long
        get() = prefs.getLong(KEY_LOCAL_REVISION, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LOCAL_REVISION, value).apply()
        }

    var lastBackedUpAtMillis: Long?
        get() = prefs.getLong(KEY_LAST_BACKUP, -1L).takeIf { it >= 0L }
        set(value) {
            prefs.edit().putLong(KEY_LAST_BACKUP, value ?: -1L).apply()
        }

    fun nextRevision(): Long {
        val next = localRevision + 1L
        localRevision = next
        return next
    }

    companion object {
        private const val PREFS_NAME = "cloud_sync"
        private const val KEY_LOCAL_REVISION = "local_revision"
        private const val KEY_LAST_BACKUP = "last_backup_at"
    }
}
