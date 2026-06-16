package com.gharmon255.dinostep.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gharmon255.dinostep.DinoStepApplication

class StepSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as DinoStepApplication
        app.healthStepSyncEngine.sync()
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "health_connect_step_sync_hourly"
    }
}
