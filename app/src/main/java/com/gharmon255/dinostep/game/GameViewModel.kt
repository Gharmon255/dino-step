package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {
    var isReady by mutableStateOf(false)
        private set

    var activeCreature by mutableStateOf(repository.createMysteryCommonEgg())
        private set

    var collection by mutableStateOf<List<CompletedCreature>>(emptyList())
        private set

    var playerStats by mutableStateOf(PlayerStats())
        private set

    val totalFakeStepsAdded: Int
        get() = playerStats.totalFakeStepsAdded

    val eggsHatched: Int
        get() = playerStats.eggsHatched

    val completedCount: Int
        get() = playerStats.creaturesCompleted

    val steps: Int
        get() = activeCreature.steps

    val stage: GrowthStage
        get() = activeCreature.stage

    val displayName: String
        get() = activeCreature.displayName

    val creatureEmoji: String
        get() = activeCreature.creature.emoji

    val isRevealed: Boolean
        get() = activeCreature.isRevealed

    val nextMilestone: Int?
        get() = activeCreature.nextMilestone

    val progressPercent: Float
        get() = activeCreature.progressPercent

    val isAdult: Boolean
        get() = activeCreature.isAdult

    val activeCreatureState: ActiveCreatureState
        get() = activeCreature

    init {
        viewModelScope.launch {
            val snapshot = repository.loadOrCreateGame()
            activeCreature = snapshot.activeCreature.normalized()
            collection = snapshot.collection
            playerStats = snapshot.playerStats
            isReady = true
        }
    }

    fun addSteps(amount: Int) {
        if (!isReady) {
            return
        }

        val wasRevealed = activeCreature.isRevealed
        val newSteps = activeCreature.steps + amount
        val nowRevealed = wasRevealed || newSteps >= activeCreature.creature.hatchStep
        val eggsHatchedDelta = if (!wasRevealed && nowRevealed) 1 else 0

        activeCreature = activeCreature.copy(
            steps = newSteps,
            isRevealed = nowRevealed,
        )
        playerStats = playerStats.copy(
            totalFakeStepsAdded = playerStats.totalFakeStepsAdded + amount,
            eggsHatched = playerStats.eggsHatched + eggsHatchedDelta,
        )

        persistActiveAndStats()
    }

    fun claimReward() {
        if (!isReady || !activeCreature.isAdult) {
            return
        }

        val completed = CompletedCreature(
            creature = activeCreature.creature,
            stepsCompleted = activeCreature.steps,
            completedAt = System.currentTimeMillis(),
        )

        collection = collection + completed
        playerStats = playerStats.copy(
            creaturesCompleted = playerStats.creaturesCompleted + 1,
        )
        activeCreature = repository.createMysteryCommonEgg()

        viewModelScope.launch {
            repository.saveCompletedCreature(completed)
            repository.savePlayerStats(playerStats)
            repository.saveActiveCreature(activeCreature)
        }
    }

    private fun persistActiveAndStats() {
        val creature = activeCreature
        val stats = playerStats
        viewModelScope.launch {
            repository.saveActiveCreature(creature)
            repository.savePlayerStats(stats)
        }
    }
}
