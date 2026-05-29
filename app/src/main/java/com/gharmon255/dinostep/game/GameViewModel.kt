package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.GrowthStage

class GameViewModel : ViewModel() {
    var activeCreature by mutableStateOf(createMysteryCommonEgg())
        private set

    var collection by mutableStateOf<List<CompletedCreature>>(emptyList())
        private set

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

    fun addSteps(amount: Int) {
        activeCreature = activeCreature.copy(steps = activeCreature.steps + amount)
    }

    fun claimReward() {
        if (!activeCreature.isAdult) {
            return
        }

        val completed = CompletedCreature(
            creature = activeCreature.creature,
            stepsCompleted = activeCreature.steps,
        )
        collection = collection + completed
        activeCreature = createMysteryCommonEgg()
    }

    private fun createMysteryCommonEgg(): ActiveCreatureState {
        return ActiveCreatureState(
            creature = CreatureCatalog.randomCommonCreature(),
            steps = 0,
        )
    }
}
