package com.gharmon255.dinostep.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.wear.WearDataLayerPublisher

class GameViewModelFactory(
    private val repository: GameRepository,
    private val developerPreferences: DeveloperPreferences,
    private val healthConnectRepository: HealthConnectRepository,
    private val wearDataLayerPublisher: WearDataLayerPublisher,
    private val garminCompanionPublisher: GarminCompanionPublisher,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(
                repository = repository,
                developerPreferences = developerPreferences,
                healthConnectRepository = healthConnectRepository,
                wearDataLayerPublisher = wearDataLayerPublisher,
                garminCompanionPublisher = garminCompanionPublisher,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
