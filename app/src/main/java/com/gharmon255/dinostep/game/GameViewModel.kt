package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.TinyRaptor

class GameViewModel : ViewModel() {
    var steps by mutableIntStateOf(0)
        private set

    val stage: GrowthStage
        get() = TinyRaptor.stageForSteps(steps)

    val nextMilestone: Int?
        get() = TinyRaptor.nextMilestone(steps)

    val progressPercent: Float
        get() = TinyRaptor.progressPercent(steps)

    val isAdult: Boolean
        get() = stage == GrowthStage.ADULT

    fun addSteps(amount: Int) {
        steps += amount
    }

    fun claimReward() {
        if (isAdult) {
            steps = 0
        }
    }
}
