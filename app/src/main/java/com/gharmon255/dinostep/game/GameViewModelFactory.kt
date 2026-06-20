package com.gharmon255.dinostep.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gharmon255.dinostep.battle.BattleRepository
import com.gharmon255.dinostep.cloud.CloudSaveSyncEngine
import com.gharmon255.dinostep.data.AppExperiencePreferences
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.health.HealthStepSyncEngine
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.notifications.StageMilestoneNotifier
import com.gharmon255.dinostep.wear.WearDataLayerPublisher

class GameViewModelFactory(
    private val repository: GameRepository,
    private val developerPreferences: DeveloperPreferences,
    private val appExperiencePreferences: AppExperiencePreferences,
    private val healthConnectRepository: HealthConnectRepository,
    private val healthStepSyncEngine: HealthStepSyncEngine,
    private val wearDataLayerPublisher: WearDataLayerPublisher,
    private val garminCompanionPublisher: GarminCompanionPublisher,
    private val stageMilestoneNotifier: StageMilestoneNotifier,
    private val cloudSaveSyncEngine: CloudSaveSyncEngine,
    private val battleRepository: BattleRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(
                repository = repository,
                developerPreferences = developerPreferences,
                appExperiencePreferences = appExperiencePreferences,
                healthConnectRepository = healthConnectRepository,
                healthStepSyncEngine = healthStepSyncEngine,
                wearDataLayerPublisher = wearDataLayerPublisher,
                garminCompanionPublisher = garminCompanionPublisher,
                stageMilestoneNotifier = stageMilestoneNotifier,
                cloudSaveSyncEngine = cloudSaveSyncEngine,
                battleRepository = battleRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
