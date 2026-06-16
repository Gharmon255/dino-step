package com.gharmon255.dinostep.data

import android.content.Context

class AppExperiencePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCompletedOnboarding(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
    }

    fun lastSeenWhatsNewVersion(): Int =
        prefs.getInt(KEY_WHATS_NEW_VERSION, 0)

    fun setLastSeenWhatsNewVersion(version: Int) {
        prefs.edit()
            .putInt(KEY_WHATS_NEW_VERSION, version)
            .apply()
    }

    fun lastActivityEvaluationDayStartMillis(): Long =
        prefs.getLong(KEY_LAST_ACTIVITY_EVAL_DAY, 0L)

    fun setLastActivityEvaluationDayStartMillis(dayStartMillis: Long) {
        prefs.edit()
            .putLong(KEY_LAST_ACTIVITY_EVAL_DAY, dayStartMillis)
            .apply()
    }

    companion object {
        const val CURRENT_WHATS_NEW_VERSION = 1

        private const val PREFS_NAME = "dino_step_app_experience"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_WHATS_NEW_VERSION = "whats_new_version"
        private const val KEY_LAST_ACTIVITY_EVAL_DAY = "last_activity_eval_day"
    }
}
