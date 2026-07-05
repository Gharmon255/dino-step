package com.gharmon255.dinostep.health

import com.gharmon255.dinostep.data.AppExperiencePreferences
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.game.ExProgression
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.notifications.DailyStepGoalReminderScheduler
import com.gharmon255.dinostep.notifications.InactivityPenaltyNotifier
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
    val collection: List<com.gharmon255.dinostep.model.CompletedCreature>? = null,
    val inactivityPenaltyApplied: Boolean = false,
)

class HealthStepSyncEngine(
    private val repository: GameRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val appExperiencePreferences: AppExperiencePreferences,
    private val inactivityPenaltyNotifier: InactivityPenaltyNotifier? = null,
    private val wearDataLayerPublisher: WearDataLayerPublisher? = null,
    private val garminCompanionPublisher: GarminCompanionPublisher? = null,
) {
    private val syncMutex = Mutex()

    suspend fun sync(): HealthStepSyncOutcome = syncMutex.withLock {
        val snapshot = repository.loadOrCreateGame()
        val rollover = performDayRollover(snapshot)
        var activeCreature = rollover.activeCreature
        val playerStats = snapshot.playerStats

        val status = runCatching {
            healthConnectRepository.resolveUiStatus()
        }.getOrElse { error ->
            return@withLock HealthStepSyncOutcome(
                status = HealthConnectUiStatus.Error(
                    error.localizedMessage ?: "Unable to check Health Connect status",
                ),
                appliedDelta = 0,
                message = error.localizedMessage ?: "Unable to check Health Connect status",
                activeCreature = activeCreature,
                playerStats = playerStats,
                inactivityPenaltyApplied = rollover.penalty != null,
            )
        }

        if (status !is HealthConnectUiStatus.Ready) {
            return@withLock HealthStepSyncOutcome(
                status = status,
                appliedDelta = 0,
                message = status.message,
                activeCreature = activeCreature,
                playerStats = playerStats,
                inactivityPenaltyApplied = rollover.penalty != null,
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
                activeCreature = activeCreature,
                playerStats = playerStats,
                inactivityPenaltyApplied = rollover.penalty != null,
            )
        }

        val syncResult = StepSyncCalculator.calculate(
            playerStats = playerStats,
            currentHealthConnectTodaySteps = todaySteps,
        )

        if (syncResult.delta <= 0) {
            DailyStepGoalReminderScheduler.updateAfterSync(
                context = healthConnectRepository.appContext,
                todaySteps = syncResult.currentHealthConnectSteps,
            )
            return@withLock HealthStepSyncOutcome(
                status = status,
                appliedDelta = 0,
                message = "No new steps to sync (Health Connect today: ${syncResult.currentHealthConnectSteps})",
                activeCreature = activeCreature,
                playerStats = playerStats,
                inactivityPenaltyApplied = rollover.penalty != null,
            )
        }

        val previousCreature = activeCreature
        val progression = StepProgression.applySteps(
            activeCreature = activeCreature,
            playerStats = syncResult.updatedStats,
            amount = syncResult.delta,
            countAsFake = false,
        )

        repository.saveActiveCreature(progression.activeCreature)
        repository.savePlayerStats(progression.playerStats)
        val updatedCollection = if (snapshot.collection.isNotEmpty() && syncResult.delta > 0) {
            ExProgression.applyDrip(snapshot.collection, syncResult.delta).also { updated ->
                repository.saveCollection(updated)
            }
        } else {
            snapshot.collection
        }

        val eventType = WearCreaturePayloadMapper.detectEventType(
            previous = previousCreature,
            current = progression.activeCreature,
        )
        wearDataLayerPublisher?.publishActiveCreature(progression.activeCreature, eventType)
        garminCompanionPublisher?.publishActiveCreature(progression.activeCreature, eventType)

        DailyStepGoalReminderScheduler.updateAfterSync(
            context = healthConnectRepository.appContext,
            todaySteps = syncResult.currentHealthConnectSteps,
        )

        return@withLock HealthStepSyncOutcome(
            status = status,
            appliedDelta = syncResult.delta,
            message = "Synced ${syncResult.delta} steps (Health Connect today: ${syncResult.currentHealthConnectSteps})",
            activeCreature = progression.activeCreature,
            playerStats = progression.playerStats,
            collection = updatedCollection,
            inactivityPenaltyApplied = rollover.penalty != null,
        )
    }

    suspend fun evaluateDayRollover(): DayRolloverOutcome =
        performDayRollover(repository.loadOrCreateGame())

    private suspend fun performDayRollover(
        snapshot: com.gharmon255.dinostep.data.GameSnapshot,
    ): DayRolloverOutcome {
        val rollover = DayRolloverEvaluator.evaluateIfNeeded(
            experience = appExperiencePreferences,
            activeCreature = snapshot.activeCreature,
            playerStats = snapshot.playerStats,
            fetchYesterdaySteps = {
                val start = StepTimeUtils.startOfYesterday()
                val end = StepTimeUtils.startOfToday()
                healthConnectRepository.readStepTotalBetween(start, end)
                    .getOrNull()
                    ?.toInt()
            },
        )
        if (rollover.penalty != null) {
            repository.saveActiveCreature(rollover.activeCreature)
            inactivityPenaltyNotifier?.notify(rollover.penalty.yesterdaySteps)
        }
        return rollover
    }
}
