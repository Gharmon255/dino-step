package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper
import com.gharmon255.dinostep.wear.WearDataLayerPublisher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class HealthStepSyncOutcome(
    val status: HealthConnectUiStatus,
    val appliedDelta: Int,
    val message: String,
    val activeCreature: com.gharmon255.dinostep.game.ActiveCreatureState? = null,
    val playerStats: com.gharmon255.dinostep.model.PlayerStats? = null,
)

class HealthStepSyncEngine(
    private val repository: GameRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val wearDataLayerPublisher: WearDataLayerPublisher? = null,
    private val garminCompanionPublisher: GarminCompanionPublisher? = null,
) {
    private val syncMutex = Mutex()

    suspend fun sync(): HealthStepSyncOutcome = syncMutex.withLock {
        val status = runCatching {
            healthConnectRepository.resolveUiStatus()
        }.getOrElse { error ->
            return@withLock HealthStepSyncOutcome(
                status = HealthConnectUiStatus.Error(
                    error.localizedMessage ?: "Unable to check Health Connect status",
                ),
                appliedDelta = 0,
                message = error.localizedMessage ?: "Unable to check Health Connect status",
            )
        }

        if (status !is HealthConnectUiStatus.Ready) {
            return@withLock HealthStepSyncOutcome(
                status = status,
                appliedDelta = 0,
                message = status.message,
            )
        }

        val todaySteps = healthConnectRepository.readTodayStepTotal().getOrElse { error ->
            val errorStatus = HealthConnectUiStatus.Error(
                error.localizedMessage ?: "Failed to read steps from Health Connect",
            )
            return@withLock HealthStepSyncOutcome(
                status = errorStatus,
                appliedDelta = 0,
                message = errorStatus.message,
            )
        }

        val snapshot = repository.loadOrCreateGame()
        val syncResult = StepSyncCalculator.calculate(
            playerStats = snapshot.playerStats,
            currentHealthConnectTodaySteps = todaySteps,
        )

        if (syncResult.delta <= 0) {
            return@withLock HealthStepSyncOutcome(
                status = status,
                appliedDelta = 0,
                message = "No new steps to sync (Health Connect today: ${syncResult.currentHealthConnectSteps})",
                activeCreature = snapshot.activeCreature,
                playerStats = snapshot.playerStats,
            )
        }

        val previousCreature = snapshot.activeCreature
        val progression = StepProgression.applySteps(
            activeCreature = snapshot.activeCreature,
            playerStats = syncResult.updatedStats,
            amount = syncResult.delta,
            countAsFake = false,
        )

        repository.saveActiveCreature(progression.activeCreature)
        repository.savePlayerStats(progression.playerStats)

        val eventType = WearCreaturePayloadMapper.detectEventType(
            previous = previousCreature,
            current = progression.activeCreature,
        )
        wearDataLayerPublisher?.publishActiveCreature(progression.activeCreature, eventType)
        garminCompanionPublisher?.publishActiveCreature(progression.activeCreature, eventType)

        return@withLock HealthStepSyncOutcome(
            status = status,
            appliedDelta = syncResult.delta,
            message = "Synced ${syncResult.delta} steps (Health Connect today: ${syncResult.currentHealthConnectSteps})",
            activeCreature = progression.activeCreature,
            playerStats = progression.playerStats,
        )
    }
}
