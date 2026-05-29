package com.gharmon255.dinostep.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository

class GameViewModelFactory(
    private val repository: GameRepository,
    private val healthConnectRepository: HealthConnectRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository, healthConnectRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
