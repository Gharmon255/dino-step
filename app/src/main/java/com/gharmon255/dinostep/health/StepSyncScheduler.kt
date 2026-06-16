package com.gharmon255.dinostep.health

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object StepSyncScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<StepSyncWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            StepSyncWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
