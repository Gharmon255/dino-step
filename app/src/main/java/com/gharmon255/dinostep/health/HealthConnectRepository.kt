package com.gharmon255.dinostep.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class HealthConnectRepository(
    private val context: Context,
) {
    val readStepsPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    val permissionContract = PermissionController.createRequestPermissionResultContract()

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (getSdkStatus() == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    fun getSdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    fun isAvailable(): Boolean = getSdkStatus() == HealthConnectClient.SDK_AVAILABLE

    suspend fun resolveUiStatus(): HealthConnectUiStatus = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext HealthConnectUiStatus.Unavailable
        }

        val client = healthConnectClient
            ?: return@withContext HealthConnectUiStatus.Unavailable

        val granted = client.permissionController.getGrantedPermissions()
        if (!granted.containsAll(readStepsPermissions)) {
            HealthConnectUiStatus.PermissionRequired
        } else {
            HealthConnectUiStatus.Ready
        }
    }

    suspend fun readTodayStepTotal(): Result<Long> = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext Result.failure(IllegalStateException("Health Connect is not available"))
        }

        val client = healthConnectClient
            ?: return@withContext Result.failure(IllegalStateException("Health Connect is not available"))

        val granted = client.permissionController.getGrantedPermissions()
        if (!granted.containsAll(readStepsPermissions)) {
            return@withContext Result.failure(SecurityException("Steps read permission not granted"))
        }

        runCatching {
            val startTime = StepTimeUtils.startOfToday()
            val endTime = Instant.now()
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                ),
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        }
    }
}
